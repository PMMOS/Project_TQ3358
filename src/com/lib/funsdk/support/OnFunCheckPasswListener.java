package com.lib.funsdk.support;

/**
 * Created by Jeff on 4/14/16.
 */
public interface OnFunCheckPasswListener extends OnFunListener {

    //密码验证成功
    public void onCheckPasswSuccess(String returnData);

    //密码验证失败
    public void onCheckPasswFailed(int errCode, String returnData);
}
