package com.embedsky.led;

import com.embedsky.httpUtils.httpUtils;
import com.embedsky.httpUtils.lockStruct;
import com.embedsky.httpUtils.logInfo;
import com.embedsky.httpUtils.tirePressure;

import com.embedsky.xmVideo.DeviceLoginFragment;

import com.lib.funsdk.support.FunSupport;

import com.Utils.FunDeviceUtils;
import com.Utils.SharedPreferencesNames.SPNames;
import com.Utils.SharedPreferencesNames.UserInfoItems;
import com.Utils.Utils;

import java.io.File;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection; 
import android.content.SharedPreferences;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;

import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.IMycanService;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.View;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
//import android.widget.Toast;

import com.igexin.sdk.PushManager;

public class LedActivity extends Activity {
	/** Called when the activity is first created. */

	//加载libled.so库，必须放在最前面
	static {
		System.loadLibrary("led");
	}

	private static final String LOG_TAG = "lock";

	//初始化led
	public static native boolean ledInit();
	//关闭led
	public static native boolean ledClose();
	//点亮led
	public static native boolean ledSetOn(int number);
	//灭掉led
	public static native boolean ledSetOff(int number);
	
	private String url="http://120.76.219.196:85/trucklogs/add_log";
	//private String url="http://192.168.10.87:8080/MyWeb/MyServlet";
	private String getuiurl = "http://120.76.219.196:85/getui/postcid";
	private static HashMap<String, String> params = new HashMap<String, String>();
	private static HashMap<String, String> cidparams = new HashMap<String, String>();
	private static lockStruct[] lockstruct = new lockStruct[5];
	private static String[] lockstatustemp = new String[5];
	private static tirePressure[] tirepressure = new tirePressure[2];
	private static logInfo loginfo = new logInfo();
	private static boolean flag;
	private static int cnt;
	private static int cansendPid[] = {0x05,0x0C,0x0D,0x21,0x2F}; 
	private static int canCnt;

	//gps
	Location location;
	private static String gpsx;
	private static String gpsy;

	//can总线
	private static IMycanService mycanservice;
	private static mycanHandler canhandler;
	public static int ret;
	private static int distance0;
	private static int disCnt;

	//video
	private Context context;
	private FunDeviceUtils fdu;
	private FragmentManager fgm;
	private FragmentTransaction fgt;
	private DeviceLoginFragment devlogfragment;
	

	//CheckBox数组，用来存放2个led灯控件
	CheckBox[] cb = new CheckBox[2];
	TextView[] tx = new TextView[6];
	public static TextView tLogView;
	
	
	//退出按钮
	Button btnQuit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		context = LedActivity.this.getApplicationContext();
		
		
		//获取xml中对应的控件
		cb[0] = (CheckBox) findViewById(R.id.cb_Lock1);
		cb[1] = (CheckBox) findViewById(R.id.cb_Lock2);
		
		tx[0] = (TextView) findViewById(R.id.tx_Lock1);
		tx[1] = (TextView) findViewById(R.id.tx_Lock2);
		tx[2] = (TextView) findViewById(R.id.tx_Lock3);
		tx[3] = (TextView) findViewById(R.id.tx_Lock4);
		tx[4] = (TextView) findViewById(R.id.tx_Lock5);
		tx[5] = (TextView) findViewById(R.id.postview);
		
		tLogView =(TextView) findViewById(R.id.receiveview);
		tLogView.setSingleLine(false);
		tLogView.setHorizontallyScrolling(false);
		
		btnQuit = (Button) findViewById(R.id.btnQuit);
		//初始化点击事件对象
		MyClickListener myClickListern = new MyClickListener();
		
		ReGetuiApplication.ReActivity = this;

		// LED1-LED8选中/取消事件
		for (int i = 0; i < 2; i++) {
			cb[i].setOnClickListener(myClickListern);
		}
		
		lockstruct[0] = new lockStruct("up_front","on");
		lockstruct[1] = new lockStruct("up_middle","on");
		lockstruct[2] = new lockStruct("up_back","on");
		lockstruct[3] = new lockStruct("down_left","on");
		lockstruct[4] = new lockStruct("down_right","on");

		tirepressure[0] = new tirePressure("lefttirepressure","0");
		tirepressure[1] = new tirePressure("righttirepressure","0");
		
		for (int i = 0; i < 5; i++){
			lockstatustemp[i] = "on";
		}
		
		// 退出按钮点击事件处理
		btnQuit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(ledClose()){
					finish();
				}
				
			}
		});

		// lock初始化
		if (!ledInit()) {
			new AlertDialog.Builder(this).setTitle("init lock fail").show();
			//lock初始化失败，则使控件不可点击
			for (int i = 0; i < 2; i++)
				cb[i].setEnabled(false);
		}
		
		cnt = 0;
		
		//个推初始化
		PushManager.getInstance().initialize(this.getApplicationContext(),GetuiPushService.class);
		PushManager.getInstance().registerPushIntentService(this.getApplicationContext(),ReIntentService.class);
		String cid = PushManager.getInstance().getClientid(this.getApplicationContext());
		
		if(cid != null){
			//tLogView.append(cid);
			cidparams.put("sid","11");
			cidparams.put("type", "100");
			cidparams.put("cid", cid);
			Log.d(LOG_TAG, cid);
			
			httpUtils.doPostAsyn(getuiurl, cidparams, new httpUtils.HttpCallBackListener() {
	            @Override
	            public void onFinish(String result) {
	                Message message = new Message();
	                message.obj=result;
	                handler.sendMessage(message);
	            }

	            @Override
	            public void onError(Exception e) {
	            }

	        });
		}

		//gps initial
		LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(true);
		criteria.setBearingRequired(false);
		criteria.setCostAllowed(true);
		criteria.setPowerRequirement(Criteria.POWER_LOW);
		String bestProvider = manager.getBestProvider(criteria,true);
		Log.d(LOG_TAG, "bestProvider"+bestProvider);

		location = manager.getLastKnownLocation(bestProvider);

		LocationListener locationlistener = new LocationListener(){

			@Override
			public void onLocationChanged(Location location){
				Log.d(LOG_TAG,location.toString());
				updateLocation(location);
			}

			@Override
			public void onProviderDisabled(String arg0){
				Log.e(LOG_TAG, arg0);
			}

			@Override
			public void onProviderEnabled(String arg0){
				Log.i(LOG_TAG, arg0);
			}

			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2){
				Log.i(LOG_TAG, "onStatusChanged");
			}
		};

		manager.requestLocationUpdates(bestProvider, 1000, 0, locationlistener);

		//can总线初始化
		mycanservice = IMycanService.Stub.asInterface(ServiceManager.getService("mycan"));
		ret = -1;
		
		if (canhandler == null){
			canhandler = new mycanHandler();
		}
		
		canCnt = 0;
		disCnt = 0;
		can_Rev canrev = new can_Rev();
        	Thread rev = new Thread(canrev);
        	rev.start();

        //video initial
        FunSupport.getInstance().init(context);
        SharedPreferences sharedPreferences = getSharedPreferences(SPNames.UserInfo.getValue(), Context.MODE_PRIVATE);
        File f = new File("root"+File.separator+"mnt");
        if(!f.exists()){
        //if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
        	Utils.showToast(context,"no SD card");
        	sharedPreferences.edit().putBoolean(UserInfoItems.hasES.getValue(), false).commit();
        }else{
        	sharedPreferences.edit().putBoolean(UserInfoItems.hasES.getValue(), true).commit();
        	sharedPreferences.edit().putString(UserInfoItems.localPath.getValue(), 
        		"root"+File.separator+"mnt"+File.separator+"xmlocal"+File.separator).commit();
        		//Environment.getExternalStorageDirectory().toString()+File.separator+"xmlocal"+File.separator).commit();
        }
        if(findViewById(R.id.fragment_video) != null){
        	if(savedInstanceState != null){
        		return;
        	}

        	devlogfragment = new DeviceLoginFragment();
        	devlogfragment.setArguments(getIntent().getExtras());
        	fgm = getFragmentManager();
        	fgt = fgm.beginTransaction();
        	fgt.add(R.id.fragment_video, devlogfragment).commit();
        	Log.d(LOG_TAG,"fragment create");
        }
		
		timer.schedule(task, 5000, 60000); // 5s后执行task,经过60s再次执行
	}
	
	Timer timer = new Timer();
	TimerTask task = new TimerTask() {
		@Override
		public void run(){
			for(int i = 0; i < 5; i++){
				if(!lockstatustemp[i].equals(lockstruct[i].getlockStatus())){
					flag = true;
					break;
				}
				//flag = false;
			}
			//Mycan send
			try{
				mycanservice.set_data(0,2);
				mycanservice.set_data(1,1);
				mycanservice.set_data(2,cansendPid[canCnt%5]);
				for(int i = 3; i < 8; i++){
					mycanservice.set_data(i,0);
				}
				mycanservice.mycansend(0x18DB33F1,8,1,0,0,1);
				canCnt += 1;
				canCnt = canCnt==5?0:canCnt;
			}catch(RemoteException e){
				e.printStackTrace();
			}

			loginfo.lockSet(lockstruct);
			loginfo.tireSet(tirepressure);
			//loginfo.typeSet("0");	

			//FileInputStream 			

			//if(flag){
				//params.put("truck_sid", "1");
				//params.put("log", loginfo.logInfoGet().toString());
				System.out.println(loginfo.logInfoGet().toString()+"\n");
				Log.d(LOG_TAG, loginfo.logInfoGet().toString());
				httpUtils.doPostAsyn(url, loginfo.logInfoGet(), new httpUtils.HttpCallBackListener() {
                    @Override
                    public void onFinish(String result) {
                   	Message message = new Message();
                    	message.obj=result;
                    	handler.sendMessage(message);
          
                    }

                    @Override
               	    public void onError(Exception e) {
                    }
		        });
				//params.clear();					
				
		    //}
		}							
	};

	//gps update
	private void updateLocation(Location location){
		if(location != null){
			tLogView.append(location.toString());
			gpsx = Double.toString(location.getLongitude());
			gpsy = Double.toString(location.getLatitude());
			loginfo.gpsSet(gpsx,gpsy);
		}else{
			Log.d(LOG_TAG, "no location object");
		}
	}

	//can总线线程
	private class can_Rev implements Runnable{
	    @Override
	    public void run(){
    		//TODO
		    while(true){
				try{
					//Thread.sleep(1000);
					ret = mycanservice.mycandump(0x00000000,0x00000000);
					if(ret == 0){
						Message msg = new Message();
				    		msg.what = mycanservice.get_id();
						List<Integer> res = new ArrayList<Integer>();
						for(int i=0;i<8;i++){
							res.add(mycanservice.get_data(i));
						}
						msg.obj = res;
						canhandler.sendMessage(msg);
					}
				}catch(RemoteException e){
					//Log.d(TAG,"rev data failed");
					e.printStackTrace();
				}
		    }
		    
    	}
    }		
	
	Handler handler = new Handler(){
		@Override
    	public void handleMessage(Message msg) {
			String s =(String) msg.obj;
	        		//Toast.makeText(LedActivity.this,s,Toast.LENGTH_SHORT).show();
			//System.out.println(s);
			Log.d(LOG_TAG, s);
			if(s != null){				
				tx[5].setText(s+String.valueOf(cnt));	
				cnt += 1;
				//Toast.makeText(LedActivity.this,s,Toast.LENGTH_SHORT).show();
				for (int i = 0; i < 5; i++){
					lockstatustemp[i] = lockstruct[i].getlockStatus();
				}
				flag = false;
			} 
    	}
	};

	//can总线handler
	public class mycanHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
            if (msg.what != 0) {
				long id = (Integer) msg.what + 2147483648L;
				System.out.println(Long.toHexString(id));
				if(id == 0x18FEF433){
					ArrayList<Integer> res =(ArrayList<Integer>) msg.obj;
					int tirepos = res.get(0);
					int tirepre = res.get(1)*8;
					double tiretem = ((int)res.get(2)*256+(int)res.get(3))*0.03125-273;
					double tirev = ((int)res.get(5)*256+(int)res.get(6))*0.1;
					int tiretype = res.get(7) >> 5;
					tirepressure[tirepos].settireVal(String.valueOf(tirepre));
					if(tLogView != null){
						tLogView.append(Long.toHexString(id)+" "+tirepos+" "+tirepre+"kPa "+tiretem+"\u00b0"+"C "+tirev+"Pa/s "+tiretype+"\n");
					}
				}else if(id == 0x18DAF100){
					ArrayList<Integer> res = (ArrayList<Integer>) msg.obj;
					int pid = res.get(2);
					switch (pid){
						case 0x05: int temp = (int)res.get(3)-40;
							  if(tLogView != null){
								tLogView.append(Long.toHexString(id)+" "+Integer.toHexString(pid)+" "+String.valueOf(temp)+"\u00b0"+"C\n");
							  }
							  break;
						case 0x0C: int w = ((int)res.get(3)*256+(int)res.get(4))/4;
							  if(tLogView != null){
								tLogView.append(Long.toHexString(id)+" "+Integer.toHexString(pid)+" "+String.valueOf(w)+"rpm\n");
							  }
							  break;
						case 0x0D: int v = res.get(3);
							  loginfo.speedSet(v);
							  if(tLogView != null){
								tLogView.append(Long.toHexString(id)+" "+Integer.toHexString(pid)+" "+String.valueOf(v)+"km/s\n");
							  }
							  break;
						case 0x21: int distance = (int)res.get(3)*256+(int)res.get(4);
							  if(disCnt == 0){
							  	distance0 = distance;
							  	disCnt = 1;
							  }else{
							  	loginfo.distanceSet(distance-distance0);
							  }
							  if(tLogView != null){
								tLogView.append(Long.toHexString(id)+" "+Integer.toHexString(pid)+" "+String.valueOf(distance)+"km\n");
							  }
						case 0x2F: double fuelLevel = (int)res.get(3)*100/255;
							  loginfo.fuelvolSet(fuelLevel);
							  if(tLogView != null){
								tLogView.append(Long.toHexString(id)+" "+Integer.toHexString(pid)+" "+String.valueOf(fuelLevel)+"%\n");
							  }
							  break;
						default: break;
					}
				}else{
					if(tLogView != null){
						tLogView.append(Long.toHexString(id)+"\n");
					}
				}
				
            }
        }
    }
	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		int keyCode = event.getKeyCode();
		if(KeyEvent.KEYCODE_ENTER==keyCode&& event.getAction() == KeyEvent.ACTION_DOWN)
		{
			lockstruct[4].setlockStatus("off");
			tx[4].setText("lock"+lockstruct[4].getlockName()+"\t\t"+lockstruct[4].getlockStatus());
			return true;
		}else if(KeyEvent.KEYCODE_ENTER==keyCode&& event.getAction() == KeyEvent.ACTION_UP){		
			lockstruct[4].setlockStatus("on");
			tx[4].setText("lock"+lockstruct[4].getlockName()+"\t\t"+lockstruct[4].getlockStatus());
			return true;
		}

		return super.dispatchKeyEvent(event);  
	} 
	
	
	@Override
    public boolean onKeyUp(int keyCode, KeyEvent event){
    	// TODO Auto-generated method stub	
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			lockstruct[0].setlockStatus("on");
			tx[0].setText("lock"+lockstruct[0].getlockName()+"\t\t"+lockstruct[0].getlockStatus());
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			lockstruct[1].setlockStatus("on");
			tx[1].setText("lock"+lockstruct[1].getlockName()+"\t\t"+lockstruct[1].getlockStatus());
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			lockstruct[2].setlockStatus("on");
			tx[2].setText("lock"+lockstruct[2].getlockName()+"\t\t"+lockstruct[2].getlockStatus());
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			lockstruct[3].setlockStatus("on");
			tx[3].setText("lock"+lockstruct[3].getlockName()+"\t\t"+lockstruct[3].getlockStatus());
			return true;
		} 			
		return super.onKeyUp(keyCode, event);
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
    	// TODO Auto-generated method stub
    	
		if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
			lockstruct[0].setlockStatus("off");
			tx[0].setText("lock"+lockstruct[0].getlockName()+"\t\t"+lockstruct[0].getlockStatus());
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
			lockstruct[1].setlockStatus("off");
			tx[1].setText("lock"+lockstruct[1].getlockName()+"\t\t"+lockstruct[1].getlockStatus());
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
			lockstruct[2].setlockStatus("off");
			tx[2].setText("lock"+lockstruct[2].getlockName()+"\t\t"+lockstruct[2].getlockStatus());
			return true;
		} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
			lockstruct[3].setlockStatus("off");
			tx[3].setText("lock"+lockstruct[3].getlockName()+"\t\t"+lockstruct[3].getlockStatus());
			return true;
		} 
		
		return super.onKeyDown(keyCode, event);
    }
    
    @Override
    protected void onDestroy(){
    	fdu.OnDestory();
    	super.onDestroy();
    }

	// 自定义的事件监听器类，用来处理CheckBox选中和取消事件
	public class MyClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			//遍历数组，判断是哪个led控件被选中
			for (int i = 0; i < 2; i++) {
				if (v == cb[i]) {
					//根据选中/取消状态来控制lock的开/关
					//controlLed(i + 1, cb[i].isChecked());
					return;
				}
			}

		}
	}

}
