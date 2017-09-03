package com.embedsky.httpUtils;

import java.util.HashMap;

import android.util.Log;

import org.json.JSONObject;
import org.json.JSONException;

public class logInfo{
	private static final String LOG_TAG = "lock";

	private int logtype;
	private String trucknumber;

	//haswarn=0 warntype=null heartpackages
	private String gpsx;
	private String gpsy;
	private int speed;
	private int gpsspeed;
	private String distance;
	private String fuelvol;
	private tirePressure[] tire_val;
	private String haswarn;

	private String warntype;
	//haswarn=1 warntype=1 lock exception and capture
	private String lock_val;
	private String pow_val;
	//haswarn=1 warntype=2 leakage//
	private String leakstatus;
	//haswarn=1 warntype=3 tirepressure warn//
	//haswarn=1 warntype=4 oil stolen and capture//
	//haswarn=1 warntype=5 high speed//
	//haswarn=1 warntype=6 stop drive//
	//haswarn=1 warntype=7 exhausted drive
	//haswarn=1 warntype=8 accelerate exception
	//haswarn=1 warntype=9 decelerate exception
	//haswarn=1 warntype=10 accident
	//haswarn=1 warntype=11 overload
	//capture sid
	private String[] snapshot = new String[3];
	private String typeflag;
	
	public logInfo(){
		super();
		trucknumber = "川C1234";
		haswarn = "0";
		warntype = "0";
		speed = 0;
		gpsspeed = 0;
		gpsx = "0";
		gpsy = "0";
		fuelvol = "0";
		distance = "0";
		logtype = 0;
		leakstatus = "128";
	}

	public void trucknumberSet(String trucknumber){
		this.trucknumber = trucknumber;
	}

	public void logtypeSet(int logtype){
		this.logtype = logtype;
	}
	
	public void lockSet(lockStruct[] lockstruct){
		lock_val = new String();
		pow_val = new String();
		//if(!warntype.equals("1")){
			for(int i=0; i<lockstruct.length;i++){
				lock_val += lockstruct[i].getlockStatus()+"-";
				pow_val += lockstruct[i].getpowerVal()+"-";
			}
			lock_val = lock_val.substring(0, lock_val.length()-1);
			pow_val = pow_val.substring(0, pow_val.length()-1);
		//}
	}

	public void tireSet(tirePressure[] tirepressure){
		tire_val = new tirePressure[tirepressure.length];
		for (int i=0; i<tirepressure.length; i++){
			tire_val[i] = new tirePressure(tirepressure[i].gettireName(), tirepressure[i].gettireVal(), 
				tirepressure[i].gettireTempName(), tirepressure[i].gettireTempVal());
		}
	}

	public void typeflagSet(String typeflag){
		this.typeflag = typeflag;
	}

	public String typeflagGet(){
		if(typeflag.equals("1") || typeflag.equals("4") || typeflag.equals("0")){
			return typeflag;
		}
		return null;
	}

	public void haswarnSet(String haswarn){
		this.haswarn = haswarn;
	}

	public String haswarnGet(){
		return haswarn;
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
		//haswarn = "1";
		//warntype = "2";
	}

	public void speedSet(int speedval){
		if (speedval == 150){
			haswarn = "1";
			warntype = "5";
		}else{
			haswarn = "0";
			warntype = null;
		}
		speed = speedval;
	}

	public void gpsspeedSet(int gpsspeed) {
		this.gpsspeed = gpsspeed;
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
		int speedtemp = 0;
		if(logtype == 0){ //thread1 upload packages normally
			//try{
			if(haswarn.equals("1")){
				log_val.put("warntype",warntype);
				if(warntype.equals("1")){
					for(int i = 0; i < 3; i++){
						log_val.put("snapshot"+String.valueOf(i+1), snapshot[i]);
					}
				}else if(warntype.equals("2")){
					log_val.put("leakstatus", leakstatus);
				}else if(warntype.equals("4")){
					for(int i = 0; i < 3; i++){
						log_val.put("snapshot"+String.valueOf(i+1), snapshot[i]);
					}
				}
				warntype = "0";
			}

			log_val.put("lock",lock_val);
			log_val.put("battery",pow_val);
			for(int i = 0; i< tire_val.length; i++){
				log_val.put(tire_val[i].gettireName(),tire_val[i].gettireVal());
				log_val.put(tire_val[i].gettireTempName(),tire_val[i].gettireTempVal());
			}
			log_val.put("trucknumber", trucknumber);
			log_val.put("haswarn",haswarn);
			
			speedtemp = speed != 0 ? speed:gpsspeed;
			log_val.put("speed",String.valueOf(speedtemp));

			log_val.put("gpsx",gpsx);
			log_val.put("gpsy",gpsy);
			log_val.put("fuelvol",fuelvol);
			log_val.put("distance",distance);
			log_val.put("time",String.valueOf(System.currentTimeMillis()));
			haswarn = "0";
			Log.d(LOG_TAG, "canbus: "+fuelvol+"\t"+speed+"\t"+distance+"\t"+lock_val);
		// } catch (JSONException e1){
		// 	e1.printStackTrace();
		// }
		}else if(logtype == 1){ //thread2 test get all the message including snapshot
			log_val.put("warntype",warntype);
			log_val.put("lock",lock_val);
			log_val.put("battery",pow_val);
			//Log.d(LOG_TAG, haswarn+ "\t"+warntype);
			log_val.put("leakstatus", leakstatus);
			for(int i = 0; i < 3; i++){
				log_val.put("snapshot"+String.valueOf(i+1), snapshot[i]);
			}
			for(int i = 0; i< tire_val.length; i++){
				log_val.put(tire_val[i].gettireName(),tire_val[i].gettireVal());
				log_val.put(tire_val[i].gettireTempName(),tire_val[i].gettireTempVal());
			}
			log_val.put("trucknumber", trucknumber);
			log_val.put("haswarn",haswarn);

			speedtemp = speed != 0 ? speed:gpsspeed;
			log_val.put("speed",String.valueOf(speedtemp));
			
			log_val.put("gpsx",gpsx);
			log_val.put("gpsy",gpsy);
			log_val.put("fuelvol",fuelvol);
			log_val.put("distance",distance);
			log_val.put("time",String.valueOf(System.currentTimeMillis()));
			Log.d(LOG_TAG, "canbus: "+fuelvol+"\t"+speed+"\t"+distance+"\t"+lock_val);
		}else{ //thread3 test get the tirepressure and temperature data
			for(int i = 0; i< tire_val.length; i++){
				log_val.put(tire_val[i].gettireName(),tire_val[i].gettireVal());
				log_val.put(tire_val[i].gettireTempName(),tire_val[i].gettireTempVal());
			}
			log_val.put("time",String.valueOf(System.currentTimeMillis()));
		}
		
		
		return log_val;
	}
}
