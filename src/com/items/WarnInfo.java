package com.items;

import org.json.JSONException;
import org.json.JSONObject;

public class WarnInfo {
	//basic info
	private String starttime; 	//yyyy-mm-dd HH:mm:ss
	private String endtime; 	//yyyy-mm-dd HH:mm:ss
	private int deviceid;
	private int channel;
	private String devicename; 	//IP:name/channel
	//capture info
	private String savepath;
	private String thumbpath;
	public WarnInfo() {
		super();
	}
	public int getChannel() {
		return channel;
	}
	public void setChannel(int channel) {
		this.channel = channel;
	}
	public String getStarttime() {
		return starttime;
	}
	public void setStarttime(String starttime) {
		this.starttime = starttime;
	}
	public String getEndtime() {
		return endtime;
	}
	public void setEndtime(String endtime) {
		this.endtime = endtime;
	}
	public int getDeviceid() {
		return deviceid;
	}
	public void setDeviceid(int deviceid) {
		this.deviceid = deviceid;
	}
	public String getDevicename() {
		return devicename;
	}
	public void setDevicename(String devicename) {
		this.devicename = devicename;
	}
	public String getSavepath() {
		return savepath;
	}
	public void setSavepath(String savepath) {
		this.savepath = savepath;
	}
	public String getThumbpath() {
		return thumbpath;
	}
	public void setThumbpath(String thumbpath) {
		this.thumbpath = thumbpath;
	}
	@Override
	public String toString() {
		return "WarnInfo [starttime=" + starttime + ", endtime=" + endtime + ", deviceid=" + deviceid + ", channel="
				+ channel + ", devicename=" + devicename + ", savepath=" + savepath + ", thumbpath=" + thumbpath + "]";
	}
	public JSONObject getJson(){
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("starttime", this.starttime);
			jsonObject.put("endtime", this.endtime);
			jsonObject.put("deviceid", this.deviceid);
			jsonObject.put("channel", this.channel);
			jsonObject.put("devicename", this.devicename);
			jsonObject.put("savepath", this.savepath);
			jsonObject.put("thumbpath", this.thumbpath);
			return jsonObject;
		} catch (JSONException e) {
			return null;
		}
	}
	public boolean fromJson(JSONObject jsonObject){
		try {
			this.starttime = jsonObject.getString("starttime");
			this.endtime = jsonObject.getString("endtime");
			this.deviceid = jsonObject.getInt("deviceid");
			this.channel = jsonObject.getInt("channel");
			this.devicename = jsonObject.getString("devicename");
			this.savepath = jsonObject.getString("savepath");
			this.thumbpath = jsonObject.getString("thumbpath");
			return true;
		} catch (JSONException e) {
			return false;
		}
	}
}
