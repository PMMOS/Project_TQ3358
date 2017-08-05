package com.lib.funsdk.support.config;

public interface JsonListener {
	public String getSendMsg();

	public boolean onParse(String json);
}
