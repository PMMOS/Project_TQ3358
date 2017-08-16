package com.embedsky.httpUtils;

public class tirePressure {
	private String tireName;
	private String tireVal;
	private String tireTempName;
	private String tireTempVal;
	
	public tirePressure(){
		super();
	}
	
	public tirePressure(String tireName, String tireVal, String tireTempName, String tireTempVal){
		super();
		this.tireName = tireName;
		this.tireVal = tireVal;	
		this.tireTempName = tireTempName;
		this.tireTempVal = tireTempVal;	
	}
	
	public void settireName(String tireName){
		this.tireName = tireName;
	}

	public void settireVal(String tireVal){
		this.tireVal = tireVal;
	}

	public void settireTempName(String tireTempName){
		this.tireTempName = tireTempName;
	}

	public void settireTempVal(String tireTempVal){
		this.tireTempVal = tireTempVal;
	}
	
	public String gettireName(){
		return tireName;
	}
	
	public String gettireVal(){
		return tireVal;
	}

	public String gettireTempName(){
		return tireTempName;
	}

	public String gettireTempVal(){
		return tireTempVal;
	}

}
