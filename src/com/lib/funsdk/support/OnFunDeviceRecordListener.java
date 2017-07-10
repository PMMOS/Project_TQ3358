package com.lib.funsdk.support;

import java.util.List;

import com.lib.funsdk.support.models.FunDevRecordFile;

public interface OnFunDeviceRecordListener extends OnFunListener {

    public void onRequestRecordListSuccess(List<FunDevRecordFile> files);

    public void onRequestRecordListFailed(final Integer errCode);

}
