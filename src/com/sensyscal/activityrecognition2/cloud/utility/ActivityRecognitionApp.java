package com.sensyscal.activityrecognition2.cloud.utility;

import java.util.HashMap;

import javax.xml.parsers.ParserConfigurationException;

import org.restlet.engine.Engine;

import android.app.Application;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gcm.GCMRegistrar;
import com.bodycloud.lib.domain.ModalitySpecification;
import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;
import com.sensyscal.activityrecognition2.cloud.services.GCMRegistrationRefreshService;
import com.sensyscal.activityrecognition2.cloud.services.OauthTokenValidatorService;
import com.sensyscal.activityrecognition2.cloud.updater.DataKeeperAndSender;
import com.sensyscal.activityrecognition2.cloud.updater.SUAReport;

public class ActivityRecognitionApp extends Application {

	public static final String SENDER_ID = "282468236533";

	public final static int SENDING_DATA_NUM_MAX_ERRORS = 5;
	public final static long NUM_MAX_ERRORS_WAITING_MS = Long.valueOf(5 * 60 * 1000);

	public final static Long DEFAULT_NUM_MIN_DATA_SENDING = Long.valueOf(1);

	public static final String TAG = "SENSORUPDATER";

	public static final String SERVER_URL = "https://bodycloudumass.appspot.com";
	
	public String GOOGLE_ACCOUNT_EMAIL;
	public String GOOGLE_ACCOUNT_TOKEN;
	public boolean GOOGLE_ACCOUNT_TOKEN_IS_VALID = false;
		
	protected static ActivityRecognitionApp APP_STATE;
	public static ActivityRecognitionApp getInstance(){ 
		if(APP_STATE == null) APP_STATE = new ActivityRecognitionApp();
		return APP_STATE; 
	}
	
	protected HashMap<Integer, DataKeeperAndSender> runningDataSenders = new HashMap<Integer, DataKeeperAndSender>();
	protected HashMap<String, SUAReport> receivedReports = new HashMap<String, SUAReport>();
	
	public ActivityRecognitionApp() {}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		
		ActivityRecognitionWidget.getInstance().stopService(new Intent(ActivityRecognitionWidget.getInstance(), OauthTokenValidatorService.class));
		ActivityRecognitionWidget.getInstance().stopService(new Intent(ActivityRecognitionWidget.getInstance(), GCMRegistrationRefreshService.class));
	}

	public void initApp() {
		System.setProperty("java.net.preferIPv6Addresses", "false");
		
		
		this.initRestLet();
		this.initGCM();
		
		ActivityRecognitionWidget.getInstance().startService(new Intent(ActivityRecognitionWidget.getInstance(), OauthTokenValidatorService.class));
		
		ActivityRecognitionWidget.getInstance().startService(new Intent(ActivityRecognitionWidget.getInstance(), GCMRegistrationRefreshService.class));
		
	}
	
	public void initRestLet()
	{
		Engine.getInstance().getRegisteredClients().clear();
        Engine.getInstance().getRegisteredClients().add(new org.restlet.ext.net.HttpClientHelper(null));
	}

	public void initGCM()
	{
		GCMRegistrar.setRegisteredOnServer(ActivityRecognitionWidget.getInstance(), false);
	}

	public int getNumRunningProcesses() { return this.runningDataSenders.size(); }

	public void startProcess(ModalitySpecification modality) throws ParserConfigurationException {
		this.runningDataSenders.put(modality.hashCode(), 
				new DataKeeperAndSender(modality));

		Log.i("NOSTRO", "Process " + modality.hashCode() + " Started");
	}
	
	public void stopProcess(int processId){ 
		try{
			this.runningDataSenders.get(processId).stopProcess();
			this.runningDataSenders.remove(processId); 
			Toast.makeText(ActivityRecognitionWidget.getInstance(), "Process terminated!", Toast.LENGTH_LONG).show();
			
			Log.i("NOSTRO", "Process " + processId + " Terminated");
		} catch(NullPointerException e){}
	}

	public HashMap<Integer, DataKeeperAndSender> getRunningDataSenders() {
		return runningDataSenders;
	}

	public void setRunningDataSenders(
			HashMap<Integer, DataKeeperAndSender> runningDataSenders) {
		this.runningDataSenders = runningDataSenders;
	}

	public HashMap<String, SUAReport> getReceivedReports() {
		return receivedReports;
	}

	public void setReceivedReports(HashMap<String, SUAReport> receivedReports) {
		this.receivedReports = receivedReports;
	}
	
}
