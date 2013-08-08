package com.destination.common;

import android.util.Log;

public class Utility {
	public static void warn(String message) {
		Log.w(getCallingMethodName(), message);
	}
	
	public static boolean isNullOrEmpty(String str) {
		return str == null || str == "";
	}
	
	private static String getCallingMethodName() {
		// Take four frames off to get callee's calling method.
		StackTraceElement callingFrame = Thread.currentThread().getStackTrace()[4];
		
		return new StringBuilder()
			.append(callingFrame.getClassName())
			.append('.')
			.append(callingFrame.getMethodName())
			.append("()")
			.toString();
	}
}
