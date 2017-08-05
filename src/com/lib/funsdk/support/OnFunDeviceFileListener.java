package com.lib.funsdk.support;

import com.lib.funsdk.support.models.FunDevice;

public interface OnFunDeviceFileListener extends OnFunListener {

	//  文件下载成功
	public void onDeviceFileDownCompleted(final FunDevice funDevice, final String path, final int nSeq);

    public void onDeviceFileDownProgress(final int totalSize, final int progress, final int nSeq);

    public void onDeviceFileDownStart(final boolean isStartSuccess, final int nSeq);
}
