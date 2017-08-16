package com.embedsky.led;

import com.embedsky.serialport.SerialPort;
import com.embedsky.serialport.SerialPortFinder;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.embedsky.httpUtils.httpUtils;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Timer;

import org.json.JSONObject;
import org.json.JSONException;

public class ReGetuiApplication extends Application {

	public static final String LOG_TAG = "lockApp";
	public static LedActivity ReActivity;

	private static DemoHandler handler;
	private static boolean temp = false;
	
	//private String url="http://192.168.10.87:8080/MyWeb/MyServlet";
	private String url="http://120.76.219.196:85/lock/operate";
	private static String sid = new String();
	private static String type = new String();
	private static String operate = new String();
	public static int modeselect;
	
	private static HashMap<String, String> reparams = new HashMap<String, String>();
		
	@Override
    public void onCreate() {
        super.onCreate();

        if (handler == null) {
            handler = new DemoHandler();
         }
        
    }
	
	public static void sendMessage(Message msg) {
        handler.sendMessage(msg);
    }

    public class DemoHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
        	if(ReActivity != null && msg.what == 0){ 
        		try {
    				JSONObject command = new JSONObject((String) msg.obj);
    				sid = command.getString("sid");
    				type = command.getString("type");
    				
    				switch(Integer.parseInt(type)){
    					case 0: {
    						if(ReActivity.time != null){
    							ReActivity.time.cancel();
    						}
    						ReActivity.time = new Timer();
    						ReActivity.time.schedule(ReActivity.heartpacktask, 5000, 60000);
    					}break;
    					case 1: {
    						operate = command.getString("operate");
    						if(operate.equals("0")){
		        				//temp = true;
		        				temp = ReActivity.ledSetOn(1);
		        				try{
		        					ReActivity.mOutputStream.write(packdata("55 66 77 88", "00"));
		        					ReActivity.mOutputStream.write('\n');
		        				}catch (IOException e){
		        					temp = false;
		        					e.printStackTrace();
		        				}
		        				
	        				}else if(operate.equals("1")){
			        			//temp = true;
			        			temp = ReActivity.ledSetOff(1);
			        			try{
		        					ReActivity.mOutputStream.write(packdata("55 66 77 88", "01"));
		        					ReActivity.mOutputStream.write('\n');
		        				}catch (IOException e){
		        					temp = false;
		        					e.printStackTrace();
		        				}
			        		}else if(operate.equals("2")){
			        			//temp = true;
			        			temp = ReActivity.ledSetOn(2);
			        			//ReActivity.mOutputStream.write();
			        		}else if(operate.equals("3")){
			        			//temp = true;
			        			temp = ReActivity.ledSetOff(2);
			        			//ReActivity.mOutputStream.write();
			        		}
			        		int ope = temp?1:0;
			    			reparams.put("sid", sid);
			    			reparams.put("type", type);
			    			reparams.put("operate", String.valueOf(ope));
			    			httpUtils.doPostAsyn(url, reparams, new httpUtils.HttpCallBackListener() {
					            @Override
					            public void onFinish(String result) {
					                Message message = new Message();
					                message.obj=result;
					                rehandler.sendMessage(message);
					            }

					            @Override
					            public void onError(Exception e) {
					            }

					        });
    					}break;
    					case 2: {
    						if(ReActivity.time != null){
    							ReActivity.time.cancel();
    						}
    						//ReActivity.time = new Timer();
    						//TODO All message send
    					}break;
    					case 3: {
    						if(ReActivity.time != null){
    							ReActivity.time.cancel();
    						}
    						ReActivity.time = new Timer();
    						//TODO Send params tirepressure tiretemp
    					}break;
    					default: break;
    				}  				      					  				
    			} catch (JSONException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			
        		if(ReActivity.tLogView != null){
    				ReActivity.tLogView.append(msg.obj + "\t"+ temp +"\n");
    			}
        	}else if(ReActivity != null && msg.what == 1){
        		if(ReActivity.tLogView != null){
    				ReActivity.tLogView.append(msg.obj +"\n");
    			}
        	}		       	
        }
        
        Handler rehandler = new Handler(){
        	@Override
        	public void handleMessage(Message msg){
        		String s =(String) msg.obj;
        		Log.d(LOG_TAG, s);
        		// if(LedActivity.tLogView != null){
        		// 	LedActivity.tLogView.append(s+"\n");
        		// }
        	}
        };
    }

    public byte[] packdata(String devid, String data){
    	String temp = "80 55 07 02 "+devid+" 00 "+data;
    	String[] subtemp = temp.split(" ");
    	byte[] tempbyte = new byte[subtemp.length+2];
    	for(int i = 0; i < subtemp.length; i++){
    		if(subtemp[i].length() != 2){
    			tempbyte[i] = 00;
    			continue;
    		}
    		try{
    			tempbyte[i] = (byte)Integer.parseInt(subtemp[i], 16);
    		}catch (Exception e){
    			tempbyte[i] = 00;
    			continue;
    		}
    	}
    	byte sum = 0;
    	for(int i = 1; i < subtemp.length; i++){
    		sum += tempbyte[i];
    	}
    	tempbyte[subtemp.length] = sum;
    	tempbyte[subtemp.length+1] = (byte) 0x81;
    	List<String> re = new ArrayList<String>();
    	for(int i = 0; i<tempbyte.length; i++){
    		String s = Integer.toHexString(tempbyte[i]&0xFF);
			if(s.length()<2){
				re.add("0"+s);
			}else{
				re.add(s);
			}
    	}
    	Log.d(LOG_TAG, re.toString());
    	return tempbyte;
    }

}
