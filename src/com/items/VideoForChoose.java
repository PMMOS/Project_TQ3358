package com.items;

import java.io.Serializable;

import com.lib.sdk.struct.H264_DVR_FILE_DATA;

public class VideoForChoose implements Serializable {
	private static final long serialVersionUID = 6L;
	private int type;
	private String name;
	private String stime;
	private String etime;
	//监控
	private int devId;
	private int channel;
	//云端
	private String devSn;
	private H264_DVR_FILE_DATA file;
	//本地
	private String videopath;
	private String thumbpath;
	
	public VideoForChoose() {
		super();
	}
	public VideoForChoose(int type, String name, String stime, String etime) {
		super();
		this.type = type;
		this.name = name;
		this.stime = stime;
		this.etime = etime;
	}
	public VideoForChoose(int type, String name, String stime, String etime, int devId, int channel, String devSn,
			H264_DVR_FILE_DATA file, String videopath, String thumbpath) {
		super();
		this.type = type;
		this.name = name;
		this.stime = stime;
		this.etime = etime;
		this.devId = devId;
		this.channel = channel;
		this.devSn = devSn;
		this.file = file;
		this.videopath = videopath;
		this.thumbpath = thumbpath;
	}
	public int getChannel() {
		return channel;
	}
	public void setChannel(int channel) {
		this.channel = channel;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getStime() {
		return stime;
	}
	public void setStime(String stime) {
		this.stime = stime;
	}
	public String getEtime() {
		return etime;
	}
	public void setEtime(String etime) {
		this.etime = etime;
	}
	public int getDevId() {
		return devId;
	}
	public void setDevId(int devId) {
		this.devId = devId;
	}
	public String getDevSn() {
		return devSn;
	}
	public void setDevSn(String devSn) {
		this.devSn = devSn;
	}
	public H264_DVR_FILE_DATA getFile() {
		return file;
	}
	public void setFile(H264_DVR_FILE_DATA file) {
		this.file = file;
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
	@Override
	public String toString() {
		return "VideoForChoose [type=" + type + ", name=" + name + ", stime=" + stime + ", etime=" + etime + ", devId="
				+ devId + ", channel=" + channel + ", devSn=" + devSn + ", file=" + file + ", videopath=" + videopath
				+ ", thumbpath=" + thumbpath + "]";
	}
}
