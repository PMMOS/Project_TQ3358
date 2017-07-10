package com.lib.funsdk.support;

/**
 * Created by Jeff on 16/4/11.
 */
public interface OnFunChangePasswListener extends OnFunListener {

    //密码修改成功
    public void onChangePasswSuccess();

    //密码修改失败
    public void onChangePasswFailed(final Integer errCode);

}
