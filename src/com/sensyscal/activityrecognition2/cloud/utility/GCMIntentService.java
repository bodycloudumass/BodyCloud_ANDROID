package com.sensyscal.activityrecognition2.cloud.utility;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;

import com.google.android.gcm.GCMBaseIntentService;
import com.google.android.gcm.GCMRegistrar;
import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;
import com.sensyscal.activityrecognition2.ActivityRecognitionWidgetHandler;

public class GCMIntentService extends GCMBaseIntentService{
	Message msg = Message.obtain();
	ActivityRecognitionWidgetHandler ARWH;
	public GCMIntentService() {
        super(ActivityRecognitionApp.SENDER_ID);
    }

    @Override
    protected void onRegistered(Context context, String registrationId) {
    	Log.i(TAG, "Device registered: regId = " + registrationId);
    	
    	new AsyncTask<String, String, String>() {

    		private final int MAX_NUM_ERRORS = 10;
    		int numErrors = 0;
    		
			@Override
			protected String doInBackground(String... params) {

				System.out.println("STO PER ENTRARE NEL WHILE DELLA REGISTRAZIONE");
				while(!GCMRegistrar.isRegisteredOnServer(ActivityRecognitionWidget.getInstance()))
				{
					System.out.println("SONO DENTRO IL WHILE");

					try{

						if(ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN_IS_VALID)
						{
							RESTLetEngine.clientRegistration(params[0]);
							GCMRegistrar.setRegisteredOnServer(ActivityRecognitionWidget.getInstance(), true);
							
							msg.obj=ActivityRecognitionWidgetHandler.REG_SERVER_OK;
							ARWH.sendMessage(msg);
						} else {
					    	Log.e(TAG, "Oauth Token not Valid: sleep for 10 seconds");
							Thread.sleep(10000);
						}

					} catch (Exception e) {
						
						this.numErrors++;

						Log.e(TAG, "Error #" + this.numErrors + " while registering the app to the server: sleep for 5 seconds");

						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {}								
						
						if(this.numErrors == MAX_NUM_ERRORS)
						{
							Log.e(TAG, "Too many errors occurred: sleep for " + (ActivityRecognitionApp.NUM_MAX_ERRORS_WAITING_MS/1000)  + " seconds");

							this.numErrors = 0;
							
							try {
								Thread.sleep(ActivityRecognitionApp.NUM_MAX_ERRORS_WAITING_MS);
							} catch (InterruptedException e1) {}					}
					}
				}
				
				return null;
			}
			
		}.execute(registrationId);
    	
		msg.obj=ActivityRecognitionWidgetHandler.REG_DEV_OK;
    	ARWH.sendMessage(msg);
    	
    	//ServerUtilities.register(context, registrationId);
    }

    @Override
    protected void onUnregistered(Context context, String registrationId) {

    	new AsyncTask<String, String, String>() {

    		private final int MAX_NUM_ERRORS = 10;
    		int numErrors = 0;
    		
			@Override
			protected String doInBackground(String... params) {

				System.out.println("STO PER ENTRARE NEL WHILE DELLA DEREGISTRAZIONE");
				while(GCMRegistrar.isRegisteredOnServer(ActivityRecognitionWidget.getInstance()))
				{
					System.out.println("SONO DENTRO IL WHILE");

					try{

						if(ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN_IS_VALID)
						{
							RESTLetEngine.clientUnregistration(params[0]);
							GCMRegistrar.setRegisteredOnServer(ActivityRecognitionWidget.getInstance(), false);
							msg.obj=ActivityRecognitionWidgetHandler.REG_SERVER_OK;
							ARWH.sendMessage(msg);
						} else Thread.sleep(1000);

					} catch (Exception e) {
												
						this.numErrors++;
						
						try {
							Thread.sleep(5000);
						} catch (InterruptedException e1) {}								
						
						if(this.numErrors == MAX_NUM_ERRORS)
						{
							this.numErrors = 0;
							
							try {
								Thread.sleep(ActivityRecognitionApp.NUM_MAX_ERRORS_WAITING_MS);
							} catch (InterruptedException e1) {}					}
					}
				}
				
				return null;
			}
			
		}.execute(registrationId);
    }

    @Override
    protected void onMessage(Context context, Intent intent) {
        Log.i(TAG, "Received message");
        msg.obj=ActivityRecognitionWidgetHandler.MSG_REC;
		ARWH.sendMessage(msg);
        Bundle bundle = intent.getExtras();

        //String message = getString(R.string.gcm_message);
        //displayMessage(context, message);
        // notifies user
        //generateNotification(context, "Received message");
    }

    @Override
    protected void onDeletedMessages(Context context, int total) {
        Log.i(TAG, "Received deleted messages notification");
        //String message = getString(R.string.gcm_deleted, total);
        //displayMessage(context, message);
        // notifies user
        generateNotification(context, "Received deleted messages notification");
    }

    @Override
    public void onError(Context context, String errorId) {
        Log.i(TAG, "Received error: " + errorId);
        msg.obj=ActivityRecognitionWidgetHandler.REG_DEV_ERROR;
		ARWH.sendMessage(msg);
    }

    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
        // log message
        Log.i(TAG, "Received recoverable error: " + errorId);
        //CommonUtilities.displayMessage(context, "Errore Critico");
        return super.onRecoverableError(context, errorId);
    }

    /**
     * Issues a notification to inform the user that server has sent a message.
     */
    private static void generateNotification(Context context, String message) {
    	/*
        //int icon = R.drawable.ic_stat_gcm;
        long when = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)
                context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(new Icon, message, when);
        String title = context.getString(R.string.app_name);
        Intent notificationIntent = new Intent(context, DemoActivity.class);
        // set intent so it does not start a new activity
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =
                PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
        notificationManager.notify(0, notification);
        */
    }

}
