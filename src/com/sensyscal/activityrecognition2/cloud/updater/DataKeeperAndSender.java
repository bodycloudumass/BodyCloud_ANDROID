package com.sensyscal.activityrecognition2.cloud.updater;


import java.sql.Timestamp;

import javax.xml.parsers.ParserConfigurationException;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.DialogInterface;

import com.bodycloud.lib.domain.DataSpecification;
import com.bodycloud.lib.domain.ModalitySpecification;
import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;
import com.sensyscal.activityrecognition2.cloud.sensors.DataFromSensorCatcher;
import com.sensyscal.activityrecognition2.cloud.utility.ActivityRecognitionApp;

public class DataKeeperAndSender {

	protected AndroidClient androidClient;
	
	public Integer idDataset;
	public String name;
	
	public Timestamp startDate;
	public Timestamp endDate;
	
	protected boolean stopped = false;
	
	protected Long processRequestMs;
	protected Long dataSendToServerMs;

	/**
	 * 
	 * @param processRequestMinutes
	 * The number of minutes to wait before to request the data to be processed
	 * 0 = never 
	 * @param dataSendMinutes
	 * The number of minutes to wait before a data segment to be sent to the server
	 * 0 = never (at the end of the data retrieving)
	 * null = default
	 * @throws ParserConfigurationException 
	 */
	public DataKeeperAndSender(final ModalitySpecification modality) throws ParserConfigurationException {
		
		this.androidClient = new AndroidClient(ActivityRecognitionApp.SERVER_URL, modality);
		
		DataSpecification inputSpecification = this.androidClient.getModality().getInputSpecification();
		if(inputSpecification != null)
			for (DataSpecification.InputSource inputSource : inputSpecification.sources()) {
				if(inputSource == DataSpecification.InputSource.HEARTBEAT)
					DataFromSensorCatcher.getInstance().startUsingSensor(DataFromSensorCatcher.class, modality.hashCode());
			}
		
		Thread t = new Thread(this.androidClient);
		t.start();

	}

	public synchronized String getInput(String title, String[] choices){
		
		ShowListThread lalaRun = new ShowListThread(title, choices);
		ActivityRecognitionWidget.getInstance().runOnUiThread(lalaRun);
	    try { wait(); } catch (InterruptedException e) {}

	    return lalaRun.getValueAndDestroy();
	}
	
	public synchronized void DKASNotify(){
		notify();
	}

	class ShowListThread implements Runnable
	{
		AlertDialog alert;
		
		String title;
		String []choices;
		
		private String value;

		public ShowListThread(String title, String[] choices) {
			super();
			this.title = title;
			this.choices = choices;
		}
		
		@Override
		public void run() {
			
			Builder builder = new Builder(ActivityRecognitionWidget.getInstance());
			builder.setTitle(title);
			builder.setItems(choices, new DialogInterface.OnClickListener(){
	            public void onClick(DialogInterface dialog, int position)
	            {
	            	value = choices[position];
	            	DKASNotify();
	            }
	        });

		this.alert=builder.create();
        this.alert.show();
		}
		
		public String getValueAndDestroy(){ 
			this.alert.cancel();
			return value;
		}
	}
	
	public void stopProcess() {
		this.stopped = true;
		
		DataSpecification inputSpecification = this.androidClient.getModality().getInputSpecification();
		for (DataSpecification.InputSource inputSource : inputSpecification.sources()) {
			if(inputSource == DataSpecification.InputSource.HEARTBEAT)
				DataFromSensorCatcher.getInstance().stopUsingSensor(DataFromSensorCatcher.class, this.androidClient.getModality().hashCode());
		}
		
		this.androidClient.stopModalityExecution();
	}
}