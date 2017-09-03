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

	public static final String LOG_TAG = "lock";
	public static LedActivity ReActivity;

	private static DemoHandler handler;
	private static boolean temp = false;
	
	//private String url="http://192.168.10.87:8080/MyWeb/MyServlet";
	protected String url="http://120.76.219.196:85/lock/operate";
    protected String[] lockdevid = new String[5];

	private static String sid = new String();
	private static String type = new String();
	private static String operate = new String();
	public static int wirelessflag = 0;
    public static int lockoperateflag = 0;
	
	protected static HashMap<String, String> reparams = new HashMap<String, String>();
		
	@Override
    public void onCreate() {
        super.onCreate();

        if (handler == null) {
            handler = new DemoHandler();
        }

        lockdevid[0] = "55 66 77 88";
        lockdevid[1] = "55 66 77 89";
        lockdevid[2] = "55 66 77 90";
        
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
    				type = command.getString("type");
                    Log.d(LOG_TAG, ""+type);
                    int tp = Integer.parseInt(type);
    				switch(tp){
    					case 1: {
                            if(ReActivity.heartpacktask != null){
                                ReActivity.heartpacktask.cancel();
                            }
                            if(ReActivity.warnpacktask != null){
                                ReActivity.warnpacktask.cancel();
                            }
    						if(ReActivity.time != null){
    							ReActivity.time.cancel();
    						}
                            ReActivity.loginfo.logtypeSet(0);
                            ReActivity.warnmsgbuf.clear();
    						ReActivity.time = new Timer();
                            ReActivity.heartpacktask = ReActivity.new HeartpackTask();
                            ReActivity.warnpacktask = ReActivity.new WarnpackTask();
    						ReActivity.time.schedule(ReActivity.heartpacktask, 5000, 60000);
                            ReActivity.time.schedule(ReActivity.warnpacktask, 6000, 20000);
                            
    					}break;
                        case 2: {
                            if(ReActivity.heartpacktask != null){
                                ReActivity.heartpacktask.cancel();
                            }
                            if(ReActivity.warnpacktask != null){
                                ReActivity.warnpacktask.cancel();
                            }
                            if(ReActivity.time != null){
                                ReActivity.time.cancel();
                            }
                        }break;
                        case 8: {
                            //TODO close the lock   
                            wirelessflag = 1;
                            for(int i = 0; i < 5; i++){
                                //ReActivity.lockstatustemp[0] = "1";
                                for(int j = 1; j < 3; j++){
                                    ReActivity.serialssendbuf.add(lockdevid[j]+"|"+"01");
                                    ReActivity.lockstatustemp[2+j] = "1";
                                }
                            }
                        }break;
                        case 9: {
                            //TODO  close the lock
                            wirelessflag = 0;
                            for(int i = 0; i < 5; i++){
                                //ReActivity.lockstatustemp[0] = "1";
                                for(int j = 1; j < 3; j++){
                                    ReActivity.serialssendbuf.add(lockdevid[j]+"|"+"01");
                                    ReActivity.lockstatustemp[2+j] = "1";
                                }
                            }
                        }break;
    					case 11: {
                            sid = command.getString("sid");
    						operate = command.getString("operate");
                            lockoperateflag = 1;
    						if(operate.equals("0")){
		        				//temp = true;
		        				//temp = ReActivity.ledSetOn(1);
		        				for(int i = 0; i < 5; i++){
                                    //for(int i = 0; i < 1; i++){
                                        ReActivity.serialssendbuf.add(lockdevid[1]+"|"+"00");
                                        ReActivity.lockstatustemp[3] = "0";
                                    //}
		        				}
		        				
	        				}else if(operate.equals("1")){
			        			//temp = true;
			        			//temp = ReActivity.ledSetOff(1);
			        			for(int i = 0; i < 5; i++){
                                   //ReActivity.lockstatustemp[0] = "1";
                                    //for(int i = 0; i < 1; i++){
                                        ReActivity.serialssendbuf.add(lockdevid[1]+"|"+"01");
                                        ReActivity.lockstatustemp[3] = "1";
                                    //}
		        				}
			        		}else if(operate.equals("2")){
			        			//temp = true;
			        			//temp = ReActivity.ledSetOn(2);
			        			//ReActivity.mOutputStream.write();
                                for(int i = 0; i < 5; i++){
                                    //for(int i = 1; i < 3; i++){
                                        ReActivity.serialssendbuf.add(lockdevid[2]+"|"+"00");
                                        ReActivity.lockstatustemp[4] = "0";
                                    //}
                                }
			        		}else if(operate.equals("3")){
			        			//temp = true;
			        			//temp = ReActivity.ledSetOff(2);
			        			//ReActivity.mOutputStream.write();
                                for(int i = 0; i < 5; i++){
                                    //for(int i = 1; i < 3; i++){
                                        ReActivity.serialssendbuf.add(lockdevid[2]+"|"+"01");
                                        ReActivity.lockstatustemp[4] = "1";
                                    //}
                                }
			        		}
			        		//int ope = temp?1:0;
			    			reparams.put("sid", sid);
			    			//reparams.put("type", type);
			    			//reparams.put("operate", String.valueOf(ope));
			    			
    					}break;
    					case 30: {
                            Log.d(LOG_TAG, "paramstest");
                            if(ReActivity.testtask != null){
                                ReActivity.testtask.cancel();
                            }
    						if(ReActivity.time != null){
    							ReActivity.time.cancel();
    						}
    						//ReActivity.time = new Timer();
    						//TODO All message send and captureImg
                            ReActivity.loginfo.logtypeSet(1);
                            ReActivity.loginfo.typeflagSet("0");
                            ReActivity.sidcnt = 0;
                            ReActivity.mlocalcapture.setCapturePath(0);
                            ReActivity.time = new Timer();
                            ReActivity.testtask = ReActivity.new TestTask();
                            ReActivity.time.schedule(ReActivity.testtask, 1000, 2000);
    					}break;
    					case 40: {
                            if(ReActivity.paramspacktask != null){
                                ReActivity.paramspacktask.cancel();
                            }
    						if(ReActivity.time != null){ 
                                ReActivity.time.cancel();
    						}
                            ReActivity.loginfo.logtypeSet(2);
    						ReActivity.time = new Timer();
                            ReActivity.paramspacktask = ReActivity.new ParamspackTask();
                            ReActivity.time.schedule(ReActivity.paramspacktask, 5000, 30000);
    						//TODO Send params tirepressure tiretemp
    					}break;
    					default: break;
    				}  				      					  				
    			} catch (JSONException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			Log.d(LOG_TAG, (String) msg.obj);
        		if(ReActivity.tLogView != null){
    				ReActivity.tLogView.append(msg.obj +"\n");
    			}
        	}else if(ReActivity != null && msg.what == 1){
        		if(ReActivity.tLogView != null){
    				ReActivity.tLogView.append(msg.obj +"\n");
    			}
        	}		       	
        }
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
