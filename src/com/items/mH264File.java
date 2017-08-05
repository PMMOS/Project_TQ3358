package com.items;

import java.io.Serializable;

import com.lib.sdk.struct.H264_DVR_FILE_DATA;

public class mH264File implements Serializable {
	private static final long serialVersionUID = 1L;
	private H264_DVR_FILE_DATA file;

	public mH264File(H264_DVR_FILE_DATA file) {
		super();
		this.file = file;
	}

	public H264_DVR_FILE_DATA getFile() {
		return file;
	}

	public void setFile(H264_DVR_FILE_DATA file) {
		this.file = file;
	}
}
