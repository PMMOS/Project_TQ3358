package com.items;

import org.json.JSONException;
import org.json.JSONObject;

public class LocalRecord {
	private String videopath;
	private String thumbpath;
	private String starttime;
	private String endtime;
	private int devid;
	private String devip;
	private int channel;
	public LocalRecord() {
		super();
	}
	public LocalRecord(String videopath, String thumbpath, String starttime, String endtime, int devid, String devip,
			int channel) {
		super();
		this.videopath = videopath;
		this.thumbpath = thumbpath;
		this.starttime = starttime;
		this.endtime = endtime;
		this.devid = devid;
		this.devip = devip;
		this.channel = channel;
	}
	public String getVideopath() {
		return videopath;
	}
	public void setVideopath(String videopath) {
		this.videopath = videopath;
	}
	public String getThumbpath() {
		return thumbpath;
	}
	public void setThumbpath(String thumbpath) {
		this.thumbpath = thumbpath;
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
		return "LocalRecord [videopath=" + videopath + ", thumbpath=" + thumbpath + ", starttime=" + starttime
				+ ", endtime=" + endtime + ", devid=" + devid + ", devip=" + devip + ", channel=" + channel + "]";
	}
	public JSONObject getJson(){
		try {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("videopath", this.videopath);
			jsonObject.put("thumbpath", this.thumbpath);
			jsonObject.put("starttime", this.starttime);
			jsonObject.put("endtime", this.endtime);
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
			this.videopath = jsonObject.getString("videopath");
			this.thumbpath = jsonObject.getString("thumbpath");
			this.starttime = jsonObject.getString("starttime");
			this.endtime = jsonObject.getString("endtime");
			this.devid = jsonObject.getInt("devid");
			this.devip = jsonObject.getString("devip");
			this.channel = jsonObject.getInt("channel");
			return true;
		} catch (JSONException e) {
			return false;
		}
	}
}
