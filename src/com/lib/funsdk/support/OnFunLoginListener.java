package com.lib.funsdk.support;

public interface OnFunLoginListener extends OnFunListener {

	// 用户登录成功
	public void onLoginSuccess();
	
	// 用户登录失败
	public void onLoginFailed(final Integer errCode);
	
	// 用户登出
	public void onLogout();
}
