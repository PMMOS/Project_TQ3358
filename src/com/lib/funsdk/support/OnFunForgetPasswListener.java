package com.lib.funsdk.support;

/**
 * Created by Jeff on 4/15/16.
 */
public interface OnFunForgetPasswListener extends OnFunListener {

    // 请求发送验证码成功
    public void onRequestCodeSuccess();

    // 请求发送验证码失败
    public void onRequestCodeFailed(final Integer errCode);

    // 验证码验证成功
    public void onVerifyCodeSuccess();

    // 验证码验证失败
    public void onVerifyFailed(final Integer errCode);

    // 密码重置成功
    public void onResetPasswSucess();

    // 密码重置失败
    public void onResetPasswFailed(final Integer errCode);
}
