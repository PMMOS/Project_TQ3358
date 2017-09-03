package com.embedsky.httpUtils;


public class lockStruct {
	private String lockName;
	private String lockStatus;
	private String powerVal;
	
	public lockStruct(){
		super();
	}
	
	public lockStruct(String lockName, String lockStatus, String powerVal){
		super();
		this.lockName = lockName;
		this.lockStatus = lockStatus;
		this.powerVal = powerVal;
	}

	public void setlockName(String lockName){
		this.lockName = lockName;
	}
	
	public void setlockStatus(String lockStatus){
		this.lockStatus = lockStatus;
	} 

	public void setpowerVal(String powerVal){
		this.powerVal = powerVal;
	}
	
	public String getlockName(){
		return lockName;
	}
	
	public String getlockStatus(){
		return lockStatus;
	} 

	public String getpowerVal(){
		return powerVal;
	}

}
