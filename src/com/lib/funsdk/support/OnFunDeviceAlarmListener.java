package com.lib.funsdk.support;

import java.util.List;

import com.lib.funsdk.support.config.AlarmInfo;
import com.lib.funsdk.support.models.FunDevice;

public interface OnFunDeviceAlarmListener extends OnFunListener {

	// 设备状态发生变化
	public void onDeviceAlarmReceived(final FunDevice funDevice);
	
	// 搜索历史报警消息成功
	public void onDeviceAlarmSearchSuccess(final FunDevice funDevice, final List<AlarmInfo> infos);
	
	// 搜索历史报警消息失败
	public void onDeviceAlarmSearchFailed(final FunDevice funDevice, final int errCode);
	
	// 接收到一个局域网报警信息
	public void onDeviceLanAlarmReceived(final FunDevice funDevice, final AlarmInfo alarmInfo);
}
