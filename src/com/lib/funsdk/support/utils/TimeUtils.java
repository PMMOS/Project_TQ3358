package com.lib.funsdk.support.utils;


public class TimeUtils {

	public static String formatTimes(int seconds) {
		int MM = seconds / 60;
		int HH = MM / 60;
		int SS = seconds % 60;
		if (HH == 0 && MM == 0)
			return "00:" + String.format("%02d", SS);
		if (HH == 0)
			return String.format("%02d:%02d", MM, SS);
		else {
			MM -= HH * 60;
			return String.format("%02d:%02d:%02d", HH, MM, SS);
		}
	}

	public static String formatTimes(int hour, int minute, int second) {
		return String.format("%02d:%02d:%02d", hour, minute, second);
	}

	public static String formatTimes(String hour, String minute, String second) {
		return String.format("%02d:%02d:%02d", Integer.parseInt(hour),
				Integer.parseInt(minute), Integer.parseInt(second));
	}

	public static long getLongTimes(String hour, String minute, String second) {
		int nHour = (int) (MyUtils.isInteger(hour) ? Long.parseLong(hour) : 0);
		int nMinute = (int) (MyUtils.isInteger(minute) ? Long.parseLong(minute)
				: 0);
		int nSecond = (int) (MyUtils.isInteger(second) ? Long.parseLong(second)
				: 0);
		return getLongTimes(nHour, nMinute, nSecond);
	}

	public static long getLongTimes(int hour, int minute, int second) {
		long times = hour * 3600 + minute * 60 + second;
		return times;
	}
}
