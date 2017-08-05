package com.lib.funsdk.support;

public interface OnFunRegisterListener extends OnFunListener {

	// 请求发送验证码成功
	public void onRequestSendCodeSuccess();
	
	// 请求发送验证码失败
	public void onRequestSendCodeFailed(final Integer errCode);
	
	// 注册用户成功
	public void onRegisterNewUserSuccess();
	
	// 注册用户失败
	public void onRegisterNewUserFailed(final Integer errCode);
}
