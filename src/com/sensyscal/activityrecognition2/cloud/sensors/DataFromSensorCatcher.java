package com.sensyscal.activityrecognition2.cloud.sensors;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;

import android.content.Intent;
import android.os.IBinder;
import android.util.Log;


class MyThread extends Thread{

	@Override
	public void run() {
		Log.e("DEBUG NOSTRO", "sn nel RUN di MyThread");
		
			//DataSensorKeeper.getInstance().valuesQueue.add(String.valueOf(2000 + (randomGenerator.nextInt() % 600)));
		
		String sampleFile = ActivityRecognitionWidget.sampleType;
		InputStream is = DataFromSensorCatcher.class.getResourceAsStream(sampleFile);
		 BufferedReader	br = new BufferedReader(new InputStreamReader(is));
			
			try {
				Log.i("DEBUG NOSTRO", "sto cominciando a leggere dal file ecg");
				String line = br.readLine();
				while(line != null){
					DataSensorKeeper.getInstance().valuesQueue.add(line);
					line = br.readLine();
					try {
						Thread.sleep(5);
					} catch (InterruptedException e) {Log.e("DEBUG NOSTRO", "InterruptedException");}							
				}
			} catch (IOException e1) {
				Log.e("DEBUG NOSTRO", "Errore con la lettura da file ecg");
			}
	}
	
	
}


public class DataFromSensorCatcher extends AbstractSensorService {
	private String type;
	
	protected static DataFromSensorCatcher DATA_FROM_SENSOR_CATHER;
	public static DataFromSensorCatcher getInstance(){ 
		if(DATA_FROM_SENSOR_CATHER == null) 
			DATA_FROM_SENSOR_CATHER = new DataFromSensorCatcher();
		return DATA_FROM_SENSOR_CATHER; 
	}
	
	protected final static String TAG = "DataFromSensorCather";

	//protected DataKeeperAndSender dataKeeper;
	//public ConcurrentLinkedQueue<String> valuesQueue = new ConcurrentLinkedQueue<String>();

	MyThread asyncTask = new MyThread();
	Random randomGenerator = new Random(new Date().getTime());

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);

		//this.valuesQueue = new ConcurrentLinkedQueue<String>();
		DataSensorKeeper.getInstance().valuesQueue = new ConcurrentLinkedQueue<String>();
		
		Log.i("DEBUG NOSTRO", "DataFromSensorCatcher Started");
		asyncTask.start();
	}
	
	
	public void stop() {
		try {
			//this.asyncTask.stop();
		} catch (Exception e) {			
		}
		
		Log.i(TAG, "Terminated");
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	public void setType(String t) {
		type = t;
		Log.i("UMASS","Type set to : " + type);
	}
	
}
