package com.embedsky.httpUtils;

import org.json.JSONObject;
import org.json.JSONException;

public class logInfo{
	private JSONObject lock_val;
	private JSONObject tire_val;
	private String type;
	//private JSONObject gpsx;
	//private JSONObject gpsy;
	
	public logInfo(){
		super();
	}
	
	public void lockSet(lockStruct[] lockstruct){
		lock_val = new JSONObject();
		for(int i=0; i<lockstruct.length;i++){
			try{
				lock_val.put(lockstruct[i].getlockName(), lockstruct[i].getlockStatus());
			} catch (JSONException e1){
				e1.printStackTrace();
			}
		} 
	}

	public void tireSet(tirePressure[] tirepressure){
		tire_val = new JSONObject();
		for (int i=0; i<tirepressure.length; i++){
			try{
				tire_val.put(tirepressure[i].gettireName(), tirepressure[i].gettireVal());
			} catch (JSONException e1){
				e1.printStackTrace();
			}
		}
	}

	public void typeSet(String typenum){
		type = typenum;
	}

	public JSONObject logInfoGet(){
		JSONObject log_val = new JSONObject();
		JSONObject con_val = new JSONObject();
		try{
			con_val.put("lock",lock_val);
			con_val.put("tire_pressure",tire_val);
			log_val.put("content",con_val);
			log_val.put("type",type);
			log_val.put("gpsx","0");
			log_val.put("gpsy","0");
			log_val.put("time",String.valueOf(System.currentTimeMillis()));

		} catch (JSONException e1){
			e1.printStackTrace();
		}
		return log_val;
	}
}
