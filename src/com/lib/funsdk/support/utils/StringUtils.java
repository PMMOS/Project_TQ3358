/**
 * Android_NetSdk
 * StringUtils.java
 * Administrator
 * TODO
 * 2015-4-10
 */
package com.lib.funsdk.support.utils;

/**
 * Android_NetSdk
 * StringUtils.java
 * @author huangwanshui
 * TODO
 * 2015-4-10
 */
public class StringUtils {
	
	public static boolean contrast(String str1, String str2) {
		if(str1 == null && str2 != null)
			return false;
		else if(str1 != null && str2 == null)
			return false;
		else if(str1 == null && str2 == null)
			return true;
		else if(str1.equals(str2))
			return true;
		else
			return false;
	}
	
	public static boolean isStringNULL(String str) {
		if(str == null || str.equals("") || str.equals("null"))
			return true;
		else
			return false;
	}
}
