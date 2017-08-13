package com.embedsky.httpUtils;


public class lockStruct {
	private String lockName;
	private String lockStatus;
	
	public lockStruct(){
		super();
	}
	
	public lockStruct(String lockName, String lockStatus){
		super();
		this.lockName = lockName;
		this.lockStatus = lockStatus;
	}

	public void setlockName(String lockName){
		this.lockName = lockName;
	}
	
	public void setlockStatus(String lockStatus){
		this.lockStatus = lockStatus;
	} 
	
	public String getlockName(){
		return lockName;
	}
	
	public String getlockStatus(){
		return lockStatus;
	} 

}
