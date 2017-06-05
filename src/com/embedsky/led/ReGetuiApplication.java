package com.embedsky.led;

import android.app.Application;
import android.os.Handler;
import android.os.Message;

import com.embedsky.httpUtils.httpUtils;

import java.util.HashMap;

import org.json.JSONObject;
import org.json.JSONException;

public class ReGetuiApplication extends Application {
	
	public static LedActivity ReActivity;
	
	private static DemoHandler handler;
	public static StringBuilder payloadData = new StringBuilder();
	public static String payload = new String();
	private static boolean temp = false;
	
	//private String url="http://192.168.10.87:8080/MyWeb/MyServlet";
	private String url="http://120.76.219.196:85/lock/operate";
	private static String sid = new String();
	private static String operate = new String();
	
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
        	payloadData.append((String) msg.obj);
        	payloadData.append("\n");
        	if(ReActivity != null && msg.what == 0){ 
        		try {
    				JSONObject lockCommand = new JSONObject((String) msg.obj);
    				sid = lockCommand.getString("sid");
    				operate = lockCommand.getString("operate");
    				  				      		
	        		if(operate.equals("0")){
	        			temp = true;
	        			//temp = LedActivity.ledSetOn(1);
	        		}else if(operate.equals("1")){
	        			temp = true;
	        			//temp = LedActivity.ledSetOff(1);
	        		}else if(operate.equals("2")){
	        			temp = true;
	        			//temp = LedActivity.ledSetOn(2);
	        		}else if(operate.equals("3")){
	        			temp = true;
	        			//temp = LedActivity.ledSetOff(2);
	        		}	        		
    	        		  				
    			} catch (JSONException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    			}
    			int ope = temp?1:0;
    			reparams.put("sid", sid);
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
        	
        	}
			if(LedActivity.tLogView != null){
    			LedActivity.tLogView.append(msg.obj + "\t"+ temp +"\n");
    		}
			       	
        }
        
        Handler rehandler = new Handler(){
        	@Override
        	public void handleMessage(Message msg){
        		String s =(String) msg.obj;
        		if(LedActivity.tLogView != null){
        			LedActivity.tLogView.append(s+"\n");
        		}
        	}
        };
    }

}
