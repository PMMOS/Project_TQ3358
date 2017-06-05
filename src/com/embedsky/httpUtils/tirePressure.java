package com.embedsky.httpUtils;

public class tirePressure {
	private String tireName;
	private String tireVal;
	
	public tirePressure(){
		super();
	}
	
	public tirePressure(String tireName, String tireVal){
		super();
		this.tireName = tireName;
		this.tireVal = tireVal;		
	}
	
	public void settireVal(String tireVal){
		this.tireVal = tireVal;
	}
	
	public String gettireName(){
		return tireName;
	}
	
	public String gettireVal(){
		return tireVal;
	}

}
