package com.lib.funsdk.support.models;

import com.embedsky.led.R;

public enum FunDevStatus {

	STATUS_UNKNOWN(0, R.string.device_stauts_unknown),
	STATUS_ONLINE(1, R.string.device_stauts_online),
	STATUS_OFFLINE(2, R.string.device_stauts_offline);
	
	private int mStatusId;
	private int mStatusResId;
	
	private FunDevStatus(int id, int resId) {
		mStatusId = id;
		mStatusResId = resId;
	}
	
	public static FunDevStatus getStatus(int id) {
		for ( FunDevStatus streamType : FunDevStatus.values() ) {
			if ( streamType.getSatusId() == id ) {
				return streamType;
			}
		}
		return null;
	}
	
	public int getSatusId() {
		return mStatusId;
	}
	
	public int getStatusResId() {
		return mStatusResId;
	}
}
