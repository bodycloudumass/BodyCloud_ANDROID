package com.sensyscal.activityrecognition2.cloud.services;

import android.app.Service;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gcm.GCMRegistrar;
import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;
import com.sensyscal.activityrecognition2.cloud.utility.ActivityRecognitionApp;

public class GCMRegistrationRefreshService extends Service {
	
	protected static String TAG = "GCMRegistrationRefreshService";

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}
	
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onStart(Intent intent, int startId) {
		//Toast.makeText(this, "GCM Token Service Started", Toast.LENGTH_LONG).show();
		
        new AsyncTask<String, String, String>(){

			@Override
			protected String doInBackground(String... params) {

	        	Log.i(TAG, "Started");

				while(true)
				{
			        GCMRegistrar.checkDevice(ActivityRecognitionWidget.getInstance());
			        GCMRegistrar.checkManifest(ActivityRecognitionWidget.getInstance());
			        //final String regId = GCMRegistrar.getRegistrationId(ActivityRecognitionWidget.getInstance());
			        
			        GCMRegistrar.unregister(ActivityRecognitionWidget.getInstance());
			        
			        GCMRegistrar.register(ActivityRecognitionWidget.getInstance(), ActivityRecognitionApp.getInstance().SENDER_ID);
			        
		        	Log.i(TAG, "Google Token Received: sleep for 30 minutes");
			        // have a rest for 30 minutes
					try {
						Thread.sleep(30*60*1000);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
        }.execute("");
	}
}
