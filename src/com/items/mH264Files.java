package com.items;

import java.io.Serializable;
import java.util.List;

import com.lib.sdk.struct.H264_DVR_FILE_DATA;

public class mH264Files implements Serializable {
	private static final long serialVersionUID = 2L;
	private List<H264_DVR_FILE_DATA> files;

	public mH264Files(List<H264_DVR_FILE_DATA> files) {
		super();
		this.files = files;
	}

	public List<H264_DVR_FILE_DATA> getFiles() {
		return files;
	}

	public void setFiles(List<H264_DVR_FILE_DATA> files) {
		this.files = files;
	}
}
