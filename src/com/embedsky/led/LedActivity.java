package com.embedsky.led;

import com.embedsky.httpUtils.httpUtils;
import com.embedsky.httpUtils.lockStruct;
import com.embedsky.httpUtils.logInfo;
import com.embedsky.httpUtils.picUpload;
import com.embedsky.httpUtils.tirePressure;

import com.interfaces.mLocalCaptureCallBack;
import com.interfaces.mPictureCallBack;

import com.embedsky.serialport.CH340AndroidDriver;

import com.embedsky.xmVideo.DeviceLoginFragment;
import com.embedsky.xmVideo.OnscreenPlayFragment;

import com.lib.funsdk.support.FunSupport;

import com.Utils.FunDeviceUtils;
import com.Utils.SharedPreferencesNames.SPNames;
import com.Utils.SharedPreferencesNames.UserInfoItems;
import com.Utils.Utils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONObject;
import org.json.JSONException;

import Decoder.BASE64Decoder;
import Decoder.BASE64Encoder;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection; 
import android.content.SharedPreferences;

import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;

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
import android.widget.Toast;

import com.igexin.sdk.PushManager;

public class LedActivity extends Activity implements mPictureCallBack{
	/** Called when the activity is first created. */

	//加载libled.so库，必须放在最前面
	static {
		System.loadLibrary("led");
	}

	private static final String LOG_TAG = "lock";

	//handler msg.what
	private final int MESSAGE_GETUI = 0x100;
	private final int MESSAGE_HEARTPACKAGE = 0x101;
	private final int MESSAGE_FILEUPLOAD = 0x102;
	private final int MESSAGE_WARNPACKAGE = 0x103;
	private final int MESSAGE_PARAMSPACKAGE = 0x104;
	private final int MESSAGE_TESTPACKAGE = 0x105;
	private final int MESSAGE_LOCKCMDOPERATE = 0x106;
	private final int MESSAGE_USB_INSERT = 0x107;
	private final int MESSAGE_USB_UNINSERT = 0x108;

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
	private String picurl = "http://120.76.219.196:80/file/upload";
	private String testurl = "http://120.76.219.196:85/test/getTruckLogResp";

	private static HashMap<String, String> params = new HashMap<String, String>();
	private static HashMap<String, String> cidparams = new HashMap<String, String>(); 
	protected static logInfo loginfo = new logInfo(); //data package uploaded
	private static int[] warntypecnt = new int[10];
	protected static LinkedList<HashMap<String, String>> warnmsgbuf = new LinkedList<HashMap<String, String>>();
	private static LinkedList<HashMap<String, String>> testmsgbuf = new LinkedList<HashMap<String, String>>();
	private static boolean flag;
	private static int cnt;	

	//Timer timer = new Timer();
	Timer cansendtime = new Timer();
	Timer time = new Timer();
	ExecutorService executorService = Executors.newFixedThreadPool(3);
	protected HeartpackTask heartpacktask;
	protected ParamspackTask paramspacktask;
	protected WarnpackTask warnpacktask;
	protected TestTask testtask;

	//gps
	Location location;
	private static String gpsx;
	private static String gpsy;
	private static float speed = 0;

	//can总线
	private static IMycanService mycanservice;
	private static mycanHandler canhandler;
	public static int ret; //can receive result
	private static int distance0; //get the first distance data
	private static int disCnt; //distance counter
	private static double fuelleveltemp = 100;
	private static int cansendPid[] = {0x05,0x0C,0x0D,0x21,0x2F};  //can pid
	private static int canCnt; //can pid counter
	private static tirePressure[] tirepressure = new tirePressure[2];

	//video
	public mLocalCaptureCallBack mlocalcapture;
	private Context context;
	private FunDeviceUtils fdu;
	private FragmentManager fgm;
	private FragmentTransaction fgt;
	private DeviceLoginFragment devlogfragment;
	private static String[] snapsid = new String[3];
	protected static int sidcnt;
	
	//serials
	private static final String ACTION_USB_PERMISSION = "com.embedsky.USB_PERMISSION";
	protected CH340AndroidDriver ch340AndroidDriver;
	private final int baurt = 4800;
	private final int BUF_SIZE = 64;
	private final int LEN = 64;
	//protected SerialPort mSerialPort;
	//protected OutputStream mOutputStream;
	//private InputStream mInputStream;
	private ReadThread mReadThread;
	private ReGetuiApplication app;//get packdata methods
	private static lockStruct[] lockstruct = new lockStruct[5];
	public static String[] lockstatustemp = new String[5];
	private static int lockwarncnt;
	private static int leakstatuscnt;

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
		app = (ReGetuiApplication) getApplicationContext();
		
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
		
		lockstruct[0] = new lockStruct("up_front","1");
		lockstruct[1] = new lockStruct("up_middle","1");
		lockstruct[2] = new lockStruct("up_back","1");
		lockstruct[3] = new lockStruct("down_left","1");
		lockstruct[4] = new lockStruct("down_right","1");
		loginfo.lockSet(lockstruct);

		tirepressure[0] = new tirePressure("lefttirepressure","0","lefttiretemp","0");
		tirepressure[1] = new tirePressure("righttirepressure","0","righttiretemp","0");
		loginfo.tireSet(tirepressure);
		
		for (int i = 0; i < 5; i++){
			lockstatustemp[i] = "0";
		}

		for (int i = 0; i < warntypecnt.length; i++){
			warntypecnt[i] = 0;
		}
		
		// 退出按钮点击事件处理
		btnQuit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//if(ledClose()){
					finish();
				//}
				
			}
		});

		// lock初始化
		//if (!ledInit()) {
			//new AlertDialog.Builder(this).setTitle("init lock fail").show();
			//lock初始化失败，则使控件不可点击
			//for (int i = 0; i < 2; i++)
				//cb[i].setEnabled(false);
		//}
		
		cnt = 0;
		
		//个推初始化
		PushManager.getInstance().initialize(this.getApplicationContext(),GetuiPushService.class);
		PushManager.getInstance().registerPushIntentService(this.getApplicationContext(),ReIntentService.class);
		String cid = PushManager.getInstance().getClientid(this.getApplicationContext());
		//String cid = new String();
		if(cid != null){
			//tLogView.append(cid);
			cidparams.put("trucknumber","川C1234");
			cidparams.put("type", "100");
			cidparams.put("cid", cid);
			Log.d(LOG_TAG, cid);
			
			httpUtils.doPostAsyn(getuiurl, cidparams, new httpUtils.HttpCallBackListener() {
	            @Override
	            public void onFinish(String result) {
	                Message message = new Message();
	                message.what = MESSAGE_GETUI;
	                message.obj=result;
	                handler.sendMessage(message);
	            }

	            @Override
	            public void onError(Exception e) {
	            }
	        });
		}

		//serials initial
		registerReceiver(mUsbDeviceReceiver, new IntentFilter(
				UsbManager.ACTION_USB_DEVICE_ATTACHED));
		registerReceiver(mUsbDeviceReceiver, new IntentFilter(
				UsbManager.ACTION_USB_DEVICE_DETACHED));
		ch340AndroidDriver = new CH340AndroidDriver(
				(UsbManager) getSystemService(Context.USB_SERVICE), this,
				ACTION_USB_PERMISSION);
		//initUSB();
		Intent i = getIntent();
		String action = i.getAction();
		if (action.equals("android.hardware.usb.action.USB_DEVICE_ATTACHED")) {
			Log.d(LOG_TAG, "init USB");
			initUSB();
		}
		lockwarncnt = 0;
		leakstatuscnt = 0;
		
		//gps initial
		LocationManager manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		criteria.setAccuracy(Criteria.ACCURACY_FINE);
		criteria.setAltitudeRequired(true);
		criteria.setSpeedRequired(true);
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
		ReadThread mReadThread = new ReadThread();
		CanRev mCanRev = new CanRev();
		executorService.execute(mReadThread);
		executorService.execute(mCanRev);

        //video initial
        sidcnt = 0;
        FunSupport.getInstance().init(context);
        SharedPreferences sharedPreferences = getSharedPreferences(SPNames.UserInfo.getValue(), Context.MODE_PRIVATE);
        File f = new File(File.separator+"mnt");
        if(!f.exists()){
        //if(!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
        	Utils.showToast(context,"no pic file");
        	sharedPreferences.edit().putBoolean(UserInfoItems.hasES.getValue(), false).commit();
        }else{
        	sharedPreferences.edit().putBoolean(UserInfoItems.hasES.getValue(), true).commit();
        	sharedPreferences.edit().putString(UserInfoItems.localPath.getValue(), 
        		File.separator+"mnt"+File.separator+"xmlocal"+File.separator).commit();
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
		
		//timer.schedule(task, 5000, 60000); // 5s后执行task,经过60s再次执行
		//heartpacktask = new HeartpackTask();
		//warnpacktask = new WarnpackTask();
		cansendtime.schedule(cansendtask, 1000, 1000);
		//time.schedule(heartpacktask, 5000, 60000);
		//time.schedule(warnpacktask, 6000, 20000);
	}

	//init USB to serials
	private void initUSB() {
		UsbDevice device = ch340AndroidDriver.EnumerateDevice();// 枚举设备，获取USB设备
		ch340AndroidDriver.OpenDevice(device);// 打开并连接USB
		if (ch340AndroidDriver.isConnected()) {
			boolean flags = ch340AndroidDriver.UartInit();// 初始化串口
			if (!flags) {
				Log.e(LOG_TAG, "Init Uart Error");
				/*Toast.makeText(LedActivity.this, "Init Uart Error",
						Toast.LENGTH_SHORT).show();*/
			} else {// 配置串口
				if (ch340AndroidDriver.SetConfig(baurt, (byte) 8, (byte) 1,
						(byte) 0, (byte) 0)) {
					Log.e(LOG_TAG, "Uart Configed");
				}
			}
		} else {
			Log.e(LOG_TAG, "ch340AndroidDriver not connected");
		}
	}

	@Override
	public void onAttachFragment(Fragment fragment){
		try{
			mlocalcapture = (mLocalCaptureCallBack) fragment;
		} catch (ClassCastException e){
			throw new ClassCastException(fragment.toString()+"must implements mLocalCaptureCallBack");
		}
		super.onAttachFragment(fragment);
	}
	
	@Override
	public void uploadPicture(String path){
		File pic = new File(path);
		if(pic.exists()){
			try{
				FileInputStream inputfile = new FileInputStream(pic);
				FileOutputStream outputfile = new FileOutputStream(path+".b64");
				BASE64Encoder base64encoder = new BASE64Encoder();
				BASE64Decoder base64decoder = new BASE64Decoder();
				try{
					byte[] buf = new byte[inputfile.available()];
					//Log.d(LOG_TAG, String.valueOf(inputfile.available()));
					inputfile.read(buf);
					inputfile.close();
					String picdata = base64encoder.encode(buf);
					//Log.d(LOG_TAG,"encode success");
					//Log.d(LOG_TAG, String.valueOf(picdata.length()));
					picUpload picupload = new picUpload(picdata, "png");
					byte[] outbuffer = picdata.getBytes();
					//Log.d(LOG_TAG,String.valueOf(outbuffer.length));
					outputfile.write(outbuffer);
					//outputfile.write(base64decoder.decodeBuffer(picdata));
					outputfile.close();

					httpUtils.doPostAsyn(picurl, picupload.picUploadGet(), new httpUtils.HttpCallBackListener() {
	            		@Override
	            		public void onFinish(String result) {
	                		Message message = new Message();
	                		message.what = MESSAGE_FILEUPLOAD;
	                		message.obj=result;
	                		handler.sendMessage(message);
	            		}

	            		@Override
	            		public void onError(Exception e) {
	            			//sidcnt = 0;
	            		}

	        		});

				}catch(IOException e){
					Log.e(LOG_TAG,"encode failed");
				}
			}catch(FileNotFoundException e){
				//throw new FileNotFoundException("file not found");\
				Log.e(LOG_TAG,"file not found");
			}	
		}else{
			Log.d(LOG_TAG,"PIC not existed");
		}
	}

	//can send
	TimerTask cansendtask = new TimerTask(){
		@Override
		public void run(){
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
				//Log.d(LOG_TAG, "Mycan SEND");
			}catch(RemoteException e){
				e.printStackTrace();
			}
		}
	};

	//heart package upload
	public class HeartpackTask extends TimerTask {
		@Override
		public void run(){
			//TODO is not necessary
			if(loginfo.haswarnGet().equals("0")){
				for (int i = 0; i < warntypecnt.length; i++){
					warntypecnt[i] = 0;
				}
			}
			HashMap<String, String> tem = loginfo.logInfoGet();
			Log.d(LOG_TAG, tem.toString());
			httpUtils.doPostAsyn(url, tem, new httpUtils.HttpCallBackListener() {
                @Override
                public void onFinish(String result) {
               	Message message = new Message();
               		message.what = MESSAGE_HEARTPACKAGE;
                	message.obj = result;
                	handler.sendMessage(message);
                }

                @Override
           	    public void onError(Exception e) {
                }
	        });
		}							
	}

	//warnpackage upload
	public class WarnpackTask extends TimerTask {
		@Override
		public void run() {
			if (!warnmsgbuf.isEmpty()){
				Log.d(LOG_TAG, warnmsgbuf.get(0).toString());
				httpUtils.doPostAsyn(url, warnmsgbuf.get(0), new httpUtils.HttpCallBackListener() {
	                @Override
	                public void onFinish(String result) {
	               	Message message = new Message();
	               		message.what = MESSAGE_WARNPACKAGE;
	                	message.obj = result;
	                	handler.sendMessage(message);
	                	warnmsgbuf.removeFirst();
	                }

	                @Override
	           	    public void onError(Exception e) {
	                }
	        	});
			}
		}
	}

	//parameters upload
	public class ParamspackTask extends TimerTask {
		@Override
		public void run() {
			Log.d(LOG_TAG, loginfo.logInfoGet().toString());
			httpUtils.doPostAsyn(url, loginfo.logInfoGet(), new httpUtils.HttpCallBackListener() {
                @Override
                public void onFinish(String result) {
               	Message message = new Message();
               		message.what = MESSAGE_PARAMSPACKAGE;
                	message.obj = result;
                	handler.sendMessage(message);
                }

                @Override
           	    public void onError(Exception e) {
                }
	        });
		}
	}

	//test upload
    public class TestTask extends TimerTask {
    	@Override
    	public void run(){
			if(!testmsgbuf.isEmpty()){
				Log.d(LOG_TAG, testmsgbuf.get(0).toString());
				httpUtils.doPostAsyn(testurl, testmsgbuf.get(0), new httpUtils.HttpCallBackListener() {
                    @Override//testurl
                    public void onFinish(String result) {
                   	Message message = new Message();
                   		message.what = MESSAGE_TESTPACKAGE;
                    	message.obj = result;
                    	handler.sendMessage(message);
                    	testmsgbuf.removeFirst();
                    }

                    @Override
               	    public void onError(Exception e) {
               	    	Log.e(LOG_TAG, "test SEND FAILED");
                    }
		        });
			}
    		
    	}
    }

	//gps update
	private void updateLocation(Location location){
		if(location != null){
			//tLogView.append(location.toString());
			gpsx = String.format("%.9f", location.getLongitude());
			gpsy = String.format("%.9f", location.getLatitude());
			speed = location.getSpeed();
			loginfo.gpsSet(gpsx,gpsy);
			loginfo.gpsspeedSet((int)speed);
			Log.d("GPS", "gpsx: "+gpsx+"\t"+"gpsy: "+gpsy+"\t"+"gpsspeed: "+speed);
		}else{
			Log.d(LOG_TAG, "no location object");
		}
	}

	//serials read thread
	private class ReadThread implements Runnable{
		@Override
		public void run(){
			List<String> data = new ArrayList<String>();
			LinkedList<Integer> siz = new LinkedList<Integer>();
			int sum = 0x00;
			int mystatus = 0x00;
			int buflen = 0;
			while(!Thread.currentThread().isInterrupted()){
				try{
					Thread.sleep(50);
				}catch(InterruptedException e){
					e.printStackTrace();
				}
				int size;
				String hv;
				//try{
					byte[] buffer = new byte[64];
					if (ch340AndroidDriver == null) return;
					size = ch340AndroidDriver.ReadData(buffer, LEN);
					if(size > 0){
						for(int i=0; i<size;i++){
							int s = buffer[i] & 0xFF;
							siz.add(s);
						}
						//Log.d("Serials", "data rev: "+ bytes2HexString(buffer, size));
						//if(siz.size() > 11){
						while(!siz.isEmpty()){
							switch(mystatus){
								case 0x00:
									if(siz.get(0) == 0x80){
										data.clear();
										hv = Integer.toHexString(siz.get(0));
										if(hv.length()<2){
											data.add("0"+hv);
										}else{
											data.add(hv);
										}
										buflen = 0;
										sum = 0x00;
										mystatus = 0x01;
									}
									siz.removeFirst();
									break;
								case 0x01:
									if(siz.get(0) == 0x05 || siz.get(0) == 0x07){
										hv = Integer.toHexString(siz.get(0));
										if(hv.length()<2){
											data.add("0"+hv);
										}else{
											data.add(hv);
										}
										sum += siz.get(0);
										mystatus = 0x02;
										siz.removeFirst();
									}else{
										data.clear();
										mystatus = 0x00;
									}
									break;
								case 0x02:
									if(siz.get(0) == 0x07){
										hv = Integer.toHexString(siz.get(0));
										if(hv.length()<2){
											data.add("0"+hv);
										}else{
											data.add(hv);
										}
										sum += siz.get(0);
										mystatus = 0x03;
										buflen = siz.get(0);
										siz.removeFirst();
									}else{
										data.clear();
										mystatus = 0x00;
									}
									break;
								case 0x03:
									if(siz.get(0) == 0x02){//netId
										hv = Integer.toHexString(siz.get(0));
										if(hv.length()<2){
											data.add("0"+hv);
										}else{
											data.add(hv);
										}
										sum += siz.get(0);
										mystatus = 0x04;
										buflen -= 1;
										siz.removeFirst();
									}else{
										data.clear();
										mystatus = 0x00;
									}
									break;
								case 0x04:
									if(siz.size() > 3){
										for(int i = 0; i < 4; i++){
											hv = Integer.toHexString(siz.get(i));
											if(hv.length()<2){
												data.add("0"+hv);
											}else{
												data.add(hv);
											}
											sum += siz.get(i);
											buflen -= 1;
										}
										for(int i = 0; i < 4; i++){
											siz.removeFirst();
										}
										mystatus = 0x05;
									}else{
										data.clear();
										mystatus = 0x00;
									}
									break;
								case 0x05:
									hv = Integer.toHexString(siz.get(0));
									if(hv.length()<2){
										data.add("0"+hv);
									}else{
										data.add(hv);
									}
									sum += siz.get(0);
									mystatus = 0x06;
									buflen -= 1;
									siz.removeFirst();
									break;
								case 0x06:
									hv = Integer.toHexString(siz.get(0));
									if(hv.length()<2){
										data.add("0"+hv);
									}else{
										data.add(hv);
									}
									sum += siz.get(0);
									buflen -= 1;
									if(buflen == 0x00){
										mystatus = 0x07;
										siz.removeFirst();
									}else{
										data.clear();
										mystatus = 0x00;
									}
									break;
								case 0x07:
									if((sum & 0xFF) == (siz.get(0) & 0xFF)){
										hv = Integer.toHexString(siz.get(0));
										if(hv.length()<2){
											data.add("0"+hv);
										}else{
											data.add(hv);
										}
										mystatus = 0x08;
										siz.removeFirst();
									}else{
										data.clear();
										mystatus = 0x00;
									}
									break;
								case 0x08:
									if(siz.get(0) == 0x81){
										hv = Integer.toHexString(siz.get(0));
										if(hv.length()<2){
											data.add("0"+hv);
										}else{
											data.add(hv);
										}
										//Log.d(LOG_TAG, "send before"+data.toString());
										ArrayList<String> datatemp = new ArrayList<String>();
										for(int i = 0; i < data.size(); i++){
											datatemp.add(data.get(i));
										}
										Message msg = new Message();
										msg.what = 1;
										msg.obj = datatemp;
										sehandler.sendMessage(msg);
										//data.clear();
										siz.removeFirst();
									}else{
										data.clear();
									}
									mystatus = 0x00;
									break;
								default:
									data.clear();
									if(siz.get(0) == 0x80){
										hv = Integer.toHexString(siz.get(0));
										if(hv.length()<2){
											data.add("0"+hv);
										}else{
											data.add(hv);
										}
										buflen = 0;
										sum = 0x00;
										mystatus = 0x01;
										siz.removeFirst();
									}else{
										mystatus = 0x00;
									}
									break;
							}
						}
							//siz.clear();
						//}	
						//Log.d(LOG_TAG,String.valueOf(size)+" "+siz.toString());
					}

				/*}catch (IOException e){
					e.printStackTrace();
					return;
				}*/
			}
		}
	}

	//can总线线程
	private class CanRev implements Runnable{
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

    //serials handler
    Handler sehandler = new Handler(){
    	@Override
    	public void handleMessage(Message msg){	
    		if(msg.what == 1) {
    			ArrayList<String> data = (ArrayList<String>) msg.obj;
	    		Log.d("Serials", data.toString());
	    		String flag = data.get(8);
	    		String devid = data.get(4)+data.get(5)+data.get(6)+data.get(7);
	    		if(flag.equals("00") && (!data.get(1).equals("05"))){
	    			if(devid.equals("55667788")){	
	    				if(data.get(9).equals("00")){
							lockstruct[0].setlockStatus("0");
							tx[0].setText("lock"+lockstruct[0].getlockName()+"\t\t"+lockstruct[0].getlockStatus());
						}else if(data.get(9).equals("01")){
							lockstruct[0].setlockStatus("1");
							tx[0].setText("lock"+lockstruct[0].getlockName()+"\t\t"+lockstruct[0].getlockStatus());
						}
					}else if(devid.equals("55667789")){
						if(data.get(9).equals("00")){
							lockstruct[3].setlockStatus("0");
							tx[3].setText("lock"+lockstruct[3].getlockName()+"\t\t"+lockstruct[3].getlockStatus());
						}else if(data.get(9).equals("01")){
							lockstruct[3].setlockStatus("1");
							tx[3].setText("lock"+lockstruct[3].getlockName()+"\t\t"+lockstruct[3].getlockStatus());
						}
					}else if(devid.equals("55667790")){
						if(data.get(9).equals("00")){
							lockstruct[4].setlockStatus("0");
							tx[4].setText("lock"+lockstruct[4].getlockName()+"\t\t"+lockstruct[4].getlockStatus());
						}else if(data.get(9).equals("01")){
							lockstruct[4].setlockStatus("1");
							tx[4].setText("lock"+lockstruct[4].getlockName()+"\t\t"+lockstruct[4].getlockStatus());
						}
					}
					//TODO Compare status 
					//warnflagSet and mlocalcapture.setCapturePath(0)
					//operate success or failed
					loginfo.lockSet(lockstruct);
					for(int i = 0 ; i < lockstruct.length; i++){
						if(!lockstruct[i].getlockStatus().equals(lockstatustemp[i])){
							lockwarncnt += 1 ;
							break;
						}
						if( i == lockstruct.length-1) {
							lockwarncnt = 0;
						}
					}
					if(app.lockoperateflag == 0){
						if(lockwarncnt > 20 && app.wirelessflag == 1){
							if(warntypecnt[1] < 1) {
								loginfo.typeflagSet("1");
								sidcnt = 0;
								mlocalcapture.setCapturePath(0);
								warntypecnt[1] += 1;
							}
							lockwarncnt = 20;
						}
					}else{
						if(lockwarncnt > 20) {
							app.reparams.put("operate", "1"); //operate failed
							lockwarncnt = 0;
						}else{
							app.reparams.put("operate", "0"); //operate success
						}
						Log.d(LOG_TAG, app.reparams.toString());
						httpUtils.doPostAsyn(app.url, app.reparams, new httpUtils.HttpCallBackListener() {
				            @Override
				            public void onFinish(String result) {
				                Message message = new Message();
				                message.what = MESSAGE_LOCKCMDOPERATE;
				                message.obj=result;
				                handler.sendMessage(message);  
				            }

				            @Override
				            public void onError(Exception e) {
				            }

					    });
					    app.lockoperateflag = 0;
					}
					
	    		}else if(flag.equals("01")){
	    			//if(devid.equals("55667788")){
	    				int leakstatusval = Integer.parseInt(data.get(9),16);
	    				loginfo.leakstatusSet(String.valueOf(leakstatusval));
	    				//TODO Compare leakstatus and send 
	    				if(leakstatusval > 64 && app.wirelessflag == 1){
	    					leakstatuscnt += 1;
	    					if(warntypecnt[1] < 1 && leakstatuscnt > 20){
	    						loginfo.haswarnSet("1");
	    						loginfo.typeSet("2");
	    						warnmsgbuf.add(loginfo.logInfoGet());
	    						warntypecnt[1] += 1;
	    						leakstatuscnt = 0;
	    					}
	    				}else{
	    					leakstatuscnt = 0;
	    				}
	    			//}
	    		}else if(flag.equals("02")){//weight data

	    		}else if(flag.equals("03")){//power 
	    			int powerval = Integer.parseInt(data.get(9),16);
	    			Log.d("Serials", "powerval: "+String.valueOf(powerval));
	    			if(powerval < 10){
	    				tLogView.append("warn powerval: "+String.valueOf(powerval));
	    			}
	    		}	
    		}
    		
    	}
    };	
	
	//http handler
	Handler handler = new Handler(){
		@Override
    	public void handleMessage(Message msg) {
    		switch(msg.what){
    			case MESSAGE_HEARTPACKAGE:{
    				String s =(String) msg.obj;
	        		//Toast.makeText(LedActivity.this,s,Toast.LENGTH_SHORT).show();
					//System.out.println(s);
					Log.d(LOG_TAG, "heartpacktask"+s);
					if(s != null){				
						tx[5].setText(s+String.valueOf(cnt));	
						cnt += 1;
						//Toast.makeText(LedActivity.this,s,Toast.LENGTH_SHORT).show();
					} 
    			}break;
    			case MESSAGE_FILEUPLOAD:{
    				String s =(String) msg.obj;
    				Log.d(LOG_TAG, s);
    				try{
    					JSONObject js = new JSONObject(s);
    					JSONObject cont = js.getJSONObject("content");
    					int picsid = cont.getInt("sid");
    					if(sidcnt < 3){
    						snapsid[sidcnt++] = String.valueOf(picsid);
    					}
    					if(sidcnt == 3){
    						loginfo.snapshotSet(snapsid);
    						sidcnt = 0;
    						if(loginfo.typeflagGet() != null){
    							Log.d(LOG_TAG, "typeflagGet"+loginfo.typeflagGet());
    							if(loginfo.typeflagGet().equals("0")){
    								loginfo.haswarnSet("1");
    								loginfo.typeSet("1");
									testmsgbuf.add(loginfo.logInfoGet());
    							}else{
    								loginfo.haswarnSet("1");
    								loginfo.typeSet(loginfo.typeflagGet());
    								warnmsgbuf.add(loginfo.logInfoGet());
    							}
    							loginfo.typeflagSet(null);
    							
    						}
    						//TODO trig capture end and delete the capture file
    						mlocalcapture.setCapturePath(2);
    					}
    					Log.d(LOG_TAG, String.valueOf(picsid)+"\t"+String.valueOf(sidcnt));
    				}catch(JSONException e){
    					e.printStackTrace();
    				}		
    			}break;
    			case MESSAGE_GETUI:{
    				String s =(String) msg.obj;
    				Log.d(LOG_TAG, "getuicidup"+s);
    			}break;
    			case MESSAGE_WARNPACKAGE:{
    				String s =(String) msg.obj;
    				Log.d(LOG_TAG, "warn"+s);
    			}break;
    			case MESSAGE_PARAMSPACKAGE:{
    				String s =(String) msg.obj;
    				Log.d(LOG_TAG, "params"+s);
    			}break;
    			case MESSAGE_TESTPACKAGE: {
    				String s =(String) msg.obj;
    				Log.d(LOG_TAG, "test"+s);
    			}break;
    			case MESSAGE_LOCKCMDOPERATE: {
    				String s =(String) msg.obj;
    				Log.d(LOG_TAG, "lockcmd"+s);
    			}break;
    			case MESSAGE_USB_INSERT: {
    				initUSB();
    				Log.d(LOG_TAG, "initUSB");
    			}break;
    			case MESSAGE_USB_UNINSERT: {
    				ch340AndroidDriver.CloseDevice();
    				Log.d(LOG_TAG, "CloseDevice");
    			}break;
    			default: break;
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
					//Log.d(LOG_TAG, res.toString());
					int tirepos = res.get(0);
					double tirepre = ((int)res.get(1))*8;
					double tiretem = ((int)res.get(2)*256+(int)res.get(3))*0.03125-273;
					double tirev = ((int)res.get(5)*256+(int)res.get(6))*0.1;
					int tiretype = res.get(7) >> 5;
					if(tirepos < tirepressure.length){
						tirepressure[tirepos].settireVal(String.format("%.3f",tirepre));
						tirepressure[tirepos].settireTempVal(String.format("%.2f", tiretem));
					}
					loginfo.tireSet(tirepressure);
					if(tLogView != null){
						 //Log.d("tirepressure", Long.toHexString(id)+" "+tirepos+" "+String.format("%.3f",tirepre)+"kPa "+String.format("%.2f", tiretem)
						 	//+"\u00b0"+"C "+tirev+"Pa/s "+tiretype+"\n");
						//tLogView.append(Long.toHexString(id)+" "+tirepos+" "+tirepre+"kPa "+tiretem+"\u00b0"+"C "+tirev+"Pa/s "+tiretype+"\n");
					}
					//TODO Compare the tirevalue and tiretemperature
				}else if(id == 0x18FEF533){
					if(warntypecnt[3] < 2){
						loginfo.haswarnSet("1");
						loginfo.typeSet("3");
						warnmsgbuf.add(loginfo.logInfoGet());
						warntypecnt[3] += 1;
					}
					
				}else if(id == 0x18DAF100){
					ArrayList<Integer> res = (ArrayList<Integer>) msg.obj;
					int pid = res.get(2);
					switch (pid){
						case 0x05: int temp = (int)res.get(3)-40;
							  if(tLogView != null){
								//Log.d("OBD", Long.toHexString(id)+" "+Integer.toHexString(pid)+" "+String.valueOf(temp)+"\u00b0"+"C\n");
							  }
							  break;
						case 0x0C: int w = ((int)res.get(3)*256+(int)res.get(4))/4;
							  if(tLogView != null){
								//Log.d("OBD", Long.toHexString(id)+" "+Integer.toHexString(pid)+" "+String.valueOf(w)+"rpm\n");
							  }
							  break;
						case 0x0D: int v = res.get(3);
							  loginfo.speedSet(v);
							  //TODO Compare the speed to get stop/high-speed/exhausted drive/...
							  //and send
							  if(v == 0 && speed == 0 ) {
							  	if(warntypecnt[6] < 2) {
									loginfo.haswarnSet("1");
								  	loginfo.typeSet("6");
								  	warnmsgbuf.add(loginfo.logInfoGet());
								  	warntypecnt[6] += 1;
							  	}
							  }else if(v > 80 || speed > 80) {
							  	if(warntypecnt[5] < 2) {
									loginfo.haswarnSet("1");
								  	loginfo.typeSet("5");
								  	warnmsgbuf.add(loginfo.logInfoGet());
								  	warntypecnt[5] += 1;
							  	}
							  }
							  if(tLogView != null){
								//Log.d("OBD", Long.toHexString(id)+" "+Integer.toHexString(pid)+" "+String.valueOf(v)+"km/s\n");
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
								//Log.d("OBD", Long.toHexString(id)+" "+Integer.toHexString(pid)+" "+String.valueOf(distance)+"km\n");
							  }
							  break;
						case 0x2F: double fuelLevel = (int)res.get(3)*100/255;
							  loginfo.fuelvolSet(fuelLevel);
							  //TODO To Compare the fuellevel and send
							  if(fuelLevel - fuelleveltemp > 1){
							  	if(warntypecnt[4] < 1 ) {
							  		loginfo.typeflagSet("4");
							  		sidcnt = 0;
							  		mlocalcapture.setCapturePath(0);
							  		warntypecnt[4] += 1 ;
							  	}
							  }
							  fuelleveltemp = fuelLevel;
							  if(tLogView != null){
								//Log.d("OBD", Long.toHexString(id)+" "+Integer.toHexString(pid)+" "+String.valueOf(fuelLevel)+"%\n");
							  }
							  break;
						default: break;
					}
				}else{
					if(tLogView != null){
						//tLogView.append(Long.toHexString(id)+"\n");
						//Log.d(LOG_TAG, Long.toHexString(id)+"\n");
					}
				}
				
            }
        }
    }

    private final BroadcastReceiver mUsbDeviceReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			//Toast.makeText(LedActivity.this, action, Toast.LENGTH_LONG).show();
			Log.e(LOG_TAG, "action:" + action);
			if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
				UsbDevice deviceFound = (UsbDevice) intent
						.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				Toast.makeText(
						LedActivity.this,
						"ACTION_USB_DEVICE_ATTACHED: \n"
								+ deviceFound.toString(), Toast.LENGTH_LONG)
						.show();
				handler.sendEmptyMessage(MESSAGE_USB_INSERT);
			} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
				UsbDevice device = (UsbDevice) intent
						.getParcelableExtra(UsbManager.EXTRA_DEVICE);
				Toast.makeText(LedActivity.this,
						"ACTION_USB_DEVICE_DETACHED: \n" + device.toString(),
						Toast.LENGTH_LONG).show();
				handler.sendEmptyMessage(MESSAGE_USB_UNINSERT);
			}
		}
	};

	/*
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
    }*/
    
    @Override
    protected void onDestroy(){
    	fdu.OnDestory();
    	ch340AndroidDriver.CloseDevice();
    	unregisterReceiver(mUsbDeviceReceiver);
    	super.onDestroy();
    }

	// 自定义的事件监听器类，用来处理CheckBox选中和取消事件
	public class MyClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			//遍历数组，判断是哪个led控件被选中
			
			if (v == cb[0]) {
				//根据选中/取消状态来控制lock的开/关
				//controlLed(i + 1, cb[i].isChecked());
				if(cb[0].isChecked()){
					//mlocalcapture.setCapturePath(0);
					loginfo.speedSet(150);
					HashMap<String, String> tem = loginfo.logInfoGet();
					Log.d(LOG_TAG, tem.toString());
					httpUtils.doPostAsyn(url, tem, new httpUtils.HttpCallBackListener() {
                    @Override
                    public void onFinish(String result) {
                   	Message message = new Message();
                   		message.what = MESSAGE_HEARTPACKAGE;
                    	message.obj = result;
                    	handler.sendMessage(message);
                    }

                    @Override
               	    public void onError(Exception e) {
                    }
		        });
					loginfo.speedSet(0);
				}
				
			}else if(v == cb[1]){
				if(cb[1].isChecked()){
					try{
						for(int i = 1; i < 3; i++){
							ch340AndroidDriver.WriteData(app.packdata(app.lockdevid[i], "00"), 
															app.packdata(app.lockdevid[i], "00").length);
						}
						//mOutputStream.write(app.packdata("55 66 77 88", "00"));
						//mOutputStream.write('\n');
					} catch (IOException e){
						e.printStackTrace();
						Log.e(LOG_TAG,"send failed");
					}
					
					//mlocalcapture.setCapturePath(2);
				}else{
					try{
						for(int i = 1; i < 3; i++){
							ch340AndroidDriver.WriteData(app.packdata(app.lockdevid[i], "01"), 
															app.packdata(app.lockdevid[i], "01").length);
						}
						//mOutputStream.write(app.packdata("55 66 77 88", "01"));
						//mOutputStream.write('\n');
					} catch (IOException e){
						e.printStackTrace();
						Log.e(LOG_TAG,"send failed");
					}
				}
			}
			
			return;
		}
	}

	public String bytes2HexString(byte[] buf, int length){
		String result = new String();
		if(buf != null){
			for(int i = 0; i < length; i++) {
				result = result + ((Integer.toHexString(buf[i] < 0? buf[i]+256 : buf[i])).length() == 1 ? 
							"0"+(Integer.toHexString(buf[i] < 0? buf[i]+256 : buf[i])) : 
							(Integer.toHexString(buf[i] < 0? buf[i]+256 : buf[i]))) + " ";
			}
			return result;
		}
		return "";
	}

}
