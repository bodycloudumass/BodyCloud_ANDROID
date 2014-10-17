package com.sensyscal.activityrecognition2;

import android.os.Handler;
import android.os.Message;


public class ActivityRecognitionWidgetHandler extends Handler{

	ActivityRecognitionWidget activity;
	
	public ActivityRecognitionWidgetHandler() {
		this.activity = ActivityRecognitionWidget.getInstance();
	}	
	
	public static String LOGIN_OK = "LOG IN OK!!";
	public static String REG_SERVER_OK = "Register Server OK!!";
	public static String REG_DEV_OK = "Register Device OK!!";
	public static String REG_DEV_ERROR = "Register Device ERROR!!";
	public static String MSG_REC = "Message Received OK!!";
	
}