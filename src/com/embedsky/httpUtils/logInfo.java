package com.embedsky.httpUtils;

import java.util.HashMap;

import org.json.JSONObject;
import org.json.JSONException;

public class logInfo{
	private JSONObject lock_val;
	private tirePressure[] tire_val;
	private String type;
	private String gpsx;
	private String gpsy;
	private String speed;
	private String fuelvol;
	private String haswarn;
	
	public logInfo(){
		super();
		haswarn = "0";
		speed = "0";
		gpsx = "0";
		gpsy = "0";
		fuelvol = "0";
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
		tire_val = new tirePressure[tirepressure.length];
		for (int i=0; i<tirepressure.length; i++){
			tire_val[i] = new tirePressure(tirepressure[i].gettireName(), tirepressure[i].gettireVal());
		}
	}

	public void typeSet(String typenum){
		type = typenum;
	}

	public void gpsSet(String gpsxval, String gpsyval){
		gpsx = gpsxval;
		gpsy = gpsyval;
	}

	public void speedSet(int speedval){
		speed = String.valueOf(speedval);
	}

	public void fuelvolSet(double fuelvolval){
		fuelvol = Double.toString(fuelvolval);
	}

	public HashMap<String, String> logInfoGet(){
		HashMap<String, String> log_val = new HashMap<String, String>();
		//try{
		if(haswarn.equals("1")){
			log_val.put("type",type);
			if(type.equals("1")){
				log_val.put("lock",lock_val.toString());
			}else if(type.equals(3)){
				for(int i = 0; i< tire_val.length; i++){
					log_val.put(tire_val[i].gettireName(),tire_val[i].gettireVal());
				}
			}else if(type.equals(4)){
				log_val.put("fuelvol",fuelvol);
			}
		}

		log_val.put("truck_sid", "1");
		log_val.put("haswarn",haswarn);
		log_val.put("speed",speed);
		log_val.put("gpsx",gpsx);
		log_val.put("gpsy",gpsy);
		log_val.put("time",String.valueOf(System.currentTimeMillis()));

		// } catch (JSONException e1){
		// 	e1.printStackTrace();
		// }
		return log_val;
	}
}
