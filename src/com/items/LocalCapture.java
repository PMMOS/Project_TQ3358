package com.items;

import org.json.JSONException;
import org.json.JSONObject;

public class LocalCapture {
	private String path;
	private String thumbpath;
	private String time;
	private int devid;
	private String devip;
	private int channel;
	public LocalCapture() {
		super();
	}
	public LocalCapture(String path, String thumbpath, String time, int devid, String devip, int channel) {
		super();
		this.path = path;
		this.thumbpath = thumbpath;
		this.time = time;
		this.devid = devid;
		this.devip = devip;
		this.channel = channel;
	}
	public String getThumbpath() {
		return thumbpath;
	}
	public void setThumbpath(String thumbpath) {
		this.thumbpath = thumbpath;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public int getDevid() {
		return devid;
	}
	public void setDevid(int devid) {
		this.devid = devid;
	}
	public String getDevip() {
		return devip;
	}
	public void setDevip(String devip) {
		this.devip = devip;
	}
	public int getChannel() {
		return channel;
	}
	public void setChannel(int channel) {
		this.channel = channel;
	}
@Override
	public String toString() {
		return "LocalCapture [path=" + path + ", thumbpath=" + thumbpath + ", time=" + time + ", devid=" + devid
				+ ", devip=" + devip + ", channel=" + channel + "]";
	}
	public JSONObject getJson(){
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("path", this.path);
			jsonObject.put("thumbpath", this.thumbpath);
			jsonObject.put("time", this.time);
			jsonObject.put("devid", this.devid);
			jsonObject.put("devip", this.devip);
			jsonObject.put("channel", this.channel);
			return jsonObject;
		} catch (JSONException e) {
			return null;
		}
	}
	public boolean fromJson(JSONObject jsonObject){
		try {
			this.path = jsonObject.getString("path");
			this.thumbpath = jsonObject.getString("thumbpath");
			this.time = jsonObject.getString("time");
			this.devid = jsonObject.getInt("devid");
			this.devip = jsonObject.getString("devip");
			this.channel = jsonObject.getInt("channel");
			return true;
		} catch (JSONException e) {
			return false;
		}
	}
}
