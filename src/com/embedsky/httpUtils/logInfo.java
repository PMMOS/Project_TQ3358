package com.embedsky.httpUtils;

import java.util.HashMap;

import android.util.Log;

import org.json.JSONObject;
import org.json.JSONException;

public class logInfo{
	private static final String LOG_TAG = "loginfo";
	//haswarn=0 warntype=null heartpackages
	private String gpsx;
	private String gpsy;
	private String speed;
	private String distance;
	private String fuelvol;
	private tirePressure[] tire_val;
	private String haswarn;

	private String warntype;
	//haswarn=1 warntype=1 lock exception and capture
	private String lock_val;
	//haswarn=1 warntype=2 leakage
	private String leakstatus;
	//haswarn=1 warntype=3 tirepressure warn
	//haswarn=1 warntype=4 oil stolen and capture
	//haswarn=1 warntype=5 high speed
	//haswarn=1 warntype=6 stop drive
	//haswarn=1 warntype=7 exhausted drive
	//haswarn=1 warntype=8 accident
	//haswarn=1 warntype=9 overload
	//capture sid
	private String[] snapshot = new String[3];
	
	public logInfo(){
		super();
		haswarn = "0";
		speed = "0";
		gpsx = "0";
		gpsy = "0";
		fuelvol = "0";
		distance = "0";
	}
	
	public void lockSet(lockStruct[] lockstruct){
		lock_val = new String();
		for(int i=0; i<lockstruct.length;i++){
			lock_val += lockstruct[i].getlockStatus()+"-";
		}
		lock_val = lock_val.substring(0, lock_val.length()-1);
	}

	public void tireSet(tirePressure[] tirepressure){
		tire_val = new tirePressure[tirepressure.length];
		for (int i=0; i<tirepressure.length; i++){
			tire_val[i] = new tirePressure(tirepressure[i].gettireName(), tirepressure[i].gettireVal());
		}
	}

	public void typeSet(String typenum){
		warntype = typenum;
	}

	public void gpsSet(String gpsxval, String gpsyval){
		gpsx = gpsxval;
		gpsy = gpsyval;
	}

	public void leakstatusSet(String leakstatus){
		this.leakstatus = leakstatus;
		haswarn = "1";
		warntype = "2";
	}

	public void speedSet(int speedval){
		if (speedval == 20){
			haswarn = "1";
			warntype = "5";
		}else{
			haswarn = "0";
			warntype = null;
		}
		speed = String.valueOf(speedval);
	}

	public void fuelvolSet(double fuelvolval){
		fuelvol = Double.toString(fuelvolval);
	}

	public void distanceSet(int distanceval){
		distance = String.valueOf(distanceval);
	}

	public void snapshotSet(String[] snapshot){
		for(int i = 0; i < 3; i++){
			this.snapshot[i] = new String(snapshot[i]);
		}
	}

	public HashMap<String, String> logInfoGet(){
		HashMap<String, String> log_val = new HashMap<String, String>();
		//try{
		if(haswarn.equals("1")){
			log_val.put("warntype",warntype);
			if(warntype.equals("1")){
				log_val.put("lock",lock_val.toString());
			}else if(warntype.equals("2")){
				log_val.put("leakstatus", leakstatus);
			}
		}

		for(int i = 0; i< tire_val.length; i++){
			log_val.put(tire_val[i].gettireName(),tire_val[i].gettireVal());
		}
		log_val.put("trucknumber", "3");
		log_val.put("haswarn",haswarn);
		log_val.put("speed",speed);
		log_val.put("gpsx",gpsx);
		log_val.put("gpsy",gpsy);
		log_val.put("fuelvol",fuelvol);
		log_val.put("distance",distance);
		log_val.put("time",String.valueOf(System.currentTimeMillis()));

		// } catch (JSONException e1){
		// 	e1.printStackTrace();
		// }
		return log_val;
	}
}
