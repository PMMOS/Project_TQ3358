package com.lib.funsdk.support;

import com.lib.funsdk.support.models.FunDevice;

public interface OnFunDeviceListener extends OnFunListener {

	// 用户设备列表发生变化
	public void onDeviceListChanged();
	
	// 设备状态发生变化
	public void onDeviceStatusChanged(final FunDevice funDevice);
	
	// 用户设备添加成功
	public void onDeviceAddedSuccess();
	
	// 用户设备添加失败
	public void onDeviceAddedFailed(final Integer errCode);
	
	// 用户设备删除成功
	public void onDeviceRemovedSuccess();
	
	// 用户设备删除失败
	public void onDeviceRemovedFailed(final Integer errCode);
	
	// 附近通过AP获取到的设备列表变化
	public void onAPDeviceListChanged();
	
	// 局域内的设备列表变化
	public void onLanDeviceListChanged();
}
