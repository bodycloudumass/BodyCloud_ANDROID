package com.sensyscal.activityrecognition2.cloud.updater;


import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import javax.xml.parsers.ParserConfigurationException;

import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;
import android.util.Log;

import com.bodycloud.lib.client.BaseClient;
import com.bodycloud.lib.domain.ModalitySpecification;
import com.bodycloud.lib.rest.ext.InstancesRepresentation;
import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;
import com.sensyscal.activityrecognition2.cloud.sensors.DataSensorKeeper;
import com.sensyscal.activityrecognition2.cloud.services.OauthTokenValidatorService;
import com.sensyscal.activityrecognition2.cloud.utility.ActivityRecognitionApp;
import com.sensyscal.activityrecognition2.worker.monitoring.MonitoringWorkerThread;

public class AndroidClient extends BaseClient {
	protected static final String TAG = "KD-CLOUD_CLIENT";
	
	public AndroidClient(String url, ModalitySpecification modality)
			throws ParserConfigurationException {
		super(url,modality);
	}
	
	public AndroidClient(String url)
			throws ParserConfigurationException {
		super(url);
	}

	@Override
	public Instances getData() {
		Instances instances = null;
		if (this.getModality().getName().equals(ActivityRecognitionWidget.RAW_DATA_FEED)) {
			
			try{
				ArrayList<Attribute> info = super.getModality().getInputSpecification().getAttrInfo();
				String entityname = "raw_data3";
				instances = new Instances(entityname, info, 500);
		
				for (int i = 0; i < DataSensorKeeper.getInstance().valuesQueue.size(); i++)
				{
					Object[] vals = DataSensorKeeper.getInstance().getValues();
					StringTokenizer tokenizer = new StringTokenizer((String)vals[i]);
					double[] double1 = new double[3];
					int x = 0;
					while (tokenizer.hasMoreTokens()) {
						double1[x] = Double.valueOf(((String)tokenizer.nextToken()));
						x++;
					}
					Instance instance = new DenseInstance(1, new double[]{ double1[0], double1[1], double1[2]});
					instances.add(instance);
				}
			}finally{
				
			}
		return instances;
		}
		if (this.getModality().getName().equals(ActivityRecognitionWidget.FEATURE_DATA_FEED)) {
			
			try{
				ArrayList<Attribute> info = super.getModality().getInputSpecification().getAttrInfo();
				String entityname = "feature_data";
				instances = new Instances(entityname, info, 500);
		
				for (int i = 0; i < DataSensorKeeper.getInstance().valuesQueue.size(); i++)
				{
					Object[] vals = DataSensorKeeper.getInstance().getValues();
					StringTokenizer tokenizer = new StringTokenizer((String)vals[i]);
					double[] double1 = new double[3];
					int x = 0;
					while (tokenizer.hasMoreTokens()) {
						double1[x] = Double.valueOf(((String)tokenizer.nextToken()));
						x++;
					}
					Instance instance = new DenseInstance(1, new double[]{ double1[0], double1[1], double1[2]});
					instances.add(instance);
				}
			}finally{
				
			}
		return instances;
		}
		
		else if (this.getModality().getName().equals(ActivityRecognitionWidget.POSITION_DATA_FEED)) {
			try{
				ArrayList<Attribute> info = super.getModality().getInputSpecification().getAttrInfo();
				String entityname = "position data";
				instances = new Instances(entityname, info, 500);
				
				for (int i = 0; i < DataSensorKeeper.getInstance().valuesQueue.size(); i++)
				{
					Object[] vals = DataSensorKeeper.getInstance().getValues();
					StringTokenizer tokenizer = new StringTokenizer((String)vals[i]);
					double[] double1 = new double[3];
					int x = 0;
					while (tokenizer.hasMoreTokens()) {
						double1[x] = Double.valueOf(((String)tokenizer.nextToken()));
						x++;
					}
				
				Instance instance = new DenseInstance(1, new double[]{ double1[0]});
				instances.add(instance);
				}
			}finally{
				
			}
		}
		return instances;
	}

	@Override
	public void beforeRequest() {
		
		int i=1;
		while(!ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN_IS_VALID)
			try { Thread.sleep(5000*i++); } catch (InterruptedException e){}
		
		//Log.i("NOSTRO", "inside beforeRequest - token value: " + ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);
		//super.setAccessToken(ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);
		
		Log.i("NOSTRO DEBUG", "inside beforeRequest - email: " + ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_EMAIL + 
				" - token: " + ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);
		super.setAuthentication(ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_EMAIL, ActivityRecognitionApp.getInstance().GOOGLE_ACCOUNT_TOKEN);
	}
	
	@Override
	public String handleChoice(String title, String[] choices) {
		
		DataKeeperAndSender sender = ActivityRecognitionApp.getInstance().getRunningDataSenders().get(getModality().hashCode());
		return sender.getInput(title, choices);
	}

	@Override
	public void log(String msg) {
		Log.i(TAG, msg);
		
	}

	@Override
	public void log(String msg, Throwable tr) {
		Log.d(TAG, msg, tr);
	}
	
	public void displayData(String response) {
		Log.i("UMASS", "Response is: " + response);
		Scanner scanner = new Scanner(response);
		String heading = scanner.nextLine();
		MonitoringWorkerThread workerThread = ActivityRecognitionWidget.getInstance().getMonitoringWorkerThread();
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			StringTokenizer tokenizer = new StringTokenizer(line, ",");
			workerThread.receivedCloudData(Double.valueOf(tokenizer.nextToken()));
			
		}
	}

	@Override
	public void handleResourceException(Status status, ResourceException re) {

		if(status.equals(Status.CLIENT_ERROR_NOT_FOUND)) return;
			
		if(status.equals(Status.CLIENT_ERROR_UNAUTHORIZED))
			try {
				OauthTokenValidatorService.updareUserToken(ActivityRecognitionWidget.getInstance());
			} catch (Exception e) {
				Log.e(TAG, "Cannot get new user token");
			}
		
		boolean solved = false;
		int i=1;
		while(!solved)
		{
			try {
				this.retryRequest();
				solved = true;
			} catch (Exception e) {
				try {
					Log.e(TAG, "Cannot execute action: sleepinf for " + (5000 * i) + " ms");
					Log.i("UMASS", "Exception value in handleResourceException: " + e.toString());
					Thread.sleep(5000 * i++);
				} catch (InterruptedException e1) {}
			}
		}
	}

	@Override
	public void report(Document view) {
		// TODO Auto-generated method stub
		
	}

}
