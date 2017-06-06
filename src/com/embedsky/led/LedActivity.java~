package com.embedsky.led;

import com.embedsky.httpUtils.httpUtils;
import com.embedsky.httpUtils.lockStruct;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;
import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection; 
import android.os.Bundle;
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
	private String getuiurl = "http://120.76.219.196:85/getui/post";
	private static HashMap<String, String> params = new HashMap<String, String>();
	private static HashMap<String, String> cidparams = new HashMap<String, String>();
	private static lockStruct[] lockstruct = new lockStruct[5];
	private static String[] lockstatustemp = new String[5];
	private static boolean flag;
	private static int cnt;

	//can总线
	private static IMycanService mycanservice;
	private static mycanHandler canhandler;
	public static int ret;
	

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
			tLogView.append(cid);
			cidparams.put("truck_sid", "2");
			cidparams.put("cid", cid);
			
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

		//can总线初始化
		mycanservice = IMycanService.Stub.asInterface(ServiceManager.getService("mycan"));
		ret = -1;
		
		if (canhandler == null){
			canhandler = new mycanHandler();
		}
		

		can_Rev canrev = new can_Rev();
        	Thread rev = new Thread(canrev);
        	rev.start();
		
		timer.schedule(task, 5000, 5000); // 5s后执行task,经过5s再次执行
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
				flag = false;
			}
			if(flag){
				params.put("truck_sid", "1");
				JSONObject lock_val = new JSONObject();
				for(int i=0; i<5; i++){
					try {
						lock_val.put(lockstruct[i].getlockName(), lockstruct[i].getlockStatus());
					} catch (JSONException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				JSONObject tire_val = new JSONObject();
				try {
					tire_val.put("left", "0");
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					tire_val.put("right", "0");
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				JSONObject con_val = new JSONObject();
				try {
					con_val.put("tire_pressure", tire_val);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try {
					con_val.put("lock",lock_val);
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				JSONObject log_val = new JSONObject();
				try{
					log_val.put("type","0");
				}catch (JSONException e1){
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try{
					log_val.put("content",con_val);
				}catch (JSONException e1){
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try{
					log_val.put("gpsx","0");
				}catch (JSONException e1){
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				try{
					log_val.put("gpsy","0");
				}catch (JSONException e1){
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				try {
					log_val.put("time",String.valueOf(System.currentTimeMillis()));
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				
				params.put("log", log_val.toString());
				httpUtils.doPostAsyn(url, params, new httpUtils.HttpCallBackListener() {
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
				
		       }
		}	
						
	};

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
			    		msg.what = 1;
					msg.obj = mycanservice.get_id();
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
			 System.out.println(s);
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
            		if (msg.what == 1) {
				long id = (Integer) msg.obj + 2147483648L;
				System.out.println(Long.toHexString(id));
				if(tLogView != null){
					tLogView.append("rev success\t"+Long.toHexString(id)+"\n");
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

	/*******************************************/
	// 功能：LOCK开/关处理
	// 参数：
	// number :LOCK编号
	// on:true;	off:false
	/*******************************************/
	/*private void controlLed(int number, boolean on) {
		if (on) {
			ledSetOn(number);
		} else {
			ledSetOff(number);
		}
	}*/

}
