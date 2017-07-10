package com.lib.funsdk.support;

/**
 * Created by Jeff on 16/4/21.
 */
public interface OnFunDeviceTalkListener extends OnFunListener {

    /**
     * 申请开始传输说话内容成功
     */
    public void onDeviceStartTalkSuccess();

    /**
     * 申请开始传输说话内容失败
     * @param errCode 失败返回代码
     */
    public void onDeviceStartTalkFailed(int errCode);

    /**
     * 申请停止传输说话内容成功
     */
    public void onDeviceStopTalkSuccess();

    /**
     * 申请停止传输说话内容失败
     * @param errCode 失败返回代码
     */
    public void onDeviceStopTalkFailed(int errCode);


}
