package com.sensyscal.activityrecognition2.worker.monitoring;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.OptionalDataException;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import spine.SPINEFactory;
import spine.SPINEManager;
import spine.datamodel.Address;
import spine.datamodel.BufferedRawData;
import spine.datamodel.Data;
import spine.datamodel.Feature;
import spine.datamodel.FeatureData;
import weka.classifiers.Classifier;
import weka.classifiers.rules.JRip;
import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;
import com.sensyscal.activityrecognition2.utils.Classifiers;
import com.sensyscal.activityrecognition2.utils.SPINEListenerAdapter;
import com.sensyscal.activityrecognition2.utils.Sample;
import com.sensyscal.activityrecognition2.utils.TrainingSetLoader;
import com.sensyscal.activityrecognition2.utils.TrainingSetLoaderFromSd;
import com.sensyscal.activityrecognition2.utils.Utils;
import com.sensyscal.activityrecognition2.worker.setup.StarterWorkerThread;
import com.sensyscal.activityrecognition2.worker.statistics.StatisticsManagerWorkerThread;
import com.sensyscalactivityrecognition2.worker.custom.WekaWorker;

public class MonitoringWorkerThread extends SPINEListenerAdapter implements
		Runnable {
	public static final String TRAINING_SET = "ts.txt";
	public static final String TRAINING_SET_APP = "ts.txt";
	private Handler handler;
	private Activity activity;

	private SPINEManager manager;

	private Lock lock;
	private Condition monitoringCompleted;

	private Hashtable<Integer, Feature[]> superFrame;
	private Hashtable<Integer, int[][]> rawsuperFrame = new Hashtable<Integer, int[][]>();
	public static Classifier cls = null;
	private File cls_file;
	private StatisticsManagerWorkerThread statisticsManagerWorkerThread;
	private int[] buffer_in;
	private static int n = 5, i;
	public static int last_activity = -1;
	public static FileInputStream fis;
	private Sample[] samples;

	public MonitoringWorkerThread(Handler handler, Activity activity,
			StatisticsManagerWorkerThread statisticsManagerWorkerThread) {
		this.handler = handler;
		this.activity = activity;
		this.statisticsManagerWorkerThread = statisticsManagerWorkerThread;

		lock = new ReentrantLock();
		monitoringCompleted = lock.newCondition();

		superFrame = new Hashtable<Integer, Feature[]>();
		buffer_in = new int[n];
		i = 0;
		samples = null;
		configureSPINEManager();
	}

	public void loadClassifier() {
		// TODO Auto-generated method stub
		cls_file = new File(Environment.getExternalStorageDirectory(),ActivityRecognitionWidget.FOLDER+"/"+WekaWorker.FILE_NAME);
		if(cls_file.exists()){
			try {
				fis = new FileInputStream(cls_file);
				ObjectInputStream is = new ObjectInputStream(fis);
				cls = (JRip) is.readObject();
				//cls = (J48) is.readObject();
				//cls = (IBk) is.readObject();
				is.close();
				Log.e("Custom Classifier ",cls.toString());
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OptionalDataException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void configureSPINEManager() {
		try {
			manager = SPINEFactory.getSPINEManagerInstance();
		} catch (InstantiationException e) {
			Log.e("SPINEManager error", e.getMessage());
		}
	}

	public void loadCustomTrainingSet() {
		Vector<String> features = new Vector<String>();

		if (manager.getNodeByPhysicalID(new Address("1")) != null) {
			features.add("1_1_2_1");
			features.add("1_1_3_1");
			features.add("1_1_9_2");
		}

		if (manager.getNodeByPhysicalID(new Address("2")) != null) {
			features.add("2_1_3_1");
			features.add("2_1_4_1");
			features.add("2_1_9_2");
		}

		try {
			TrainingSetLoaderFromSd tsl = new TrainingSetLoaderFromSd(activity,ActivityRecognitionWidget.FOLDER+"/"+TRAINING_SET, features, true, false);
			samples = tsl.getTrainingSet();
		} catch (FileNotFoundException e) {
			Log.e("Training set not found", e.getMessage());
		}
		
		Log.e("Loaded TrainingSet","SD");
	}
	
	public void loadTrainingSet() {
		Vector<String> features = new Vector<String>();

		if (manager.getNodeByPhysicalID(new Address("1")) != null) {
			features.add("1_1_2_2");
			features.add("1_1_3_2");
		}

		if (manager.getNodeByPhysicalID(new Address("2")) != null) {
			features.add("2_1_3_2");
		}

		try {
			TrainingSetLoader tsl = new TrainingSetLoader(activity, TRAINING_SET_APP, features, true, false);
			samples = tsl.getTrainingSet();
		} catch (FileNotFoundException e) {
			Log.e("Training set not found", e.getMessage());
		}
		Log.e("Loaded TrainingSet","App");
	}

	@Override
	public void run() {
		startMonitoring();

		lock.lock();
		try {
			monitoringCompleted.await();
		} catch (InterruptedException e) {
		}
		lock.unlock();
	}

	private void startMonitoring() {
		manager.addListener(this);
		enablePauseButton(true);
	}

	public void pauseMonitoring() {
		manager.removeListener(this);
		changePauseToResumeButton(true);
	}

	public void resumeMonitoring() {
		manager.addListener(this);
		changePauseToResumeButton(false);
	}

	public void stopMonitoring() {
		manager.removeListener(this);

		lock.lock();
		monitoringCompleted.signalAll();
		lock.unlock();

		changeUIActivityImage(-1);
		enablePauseButton(false);
	}

	@Override
	public void received(Data data) {
		Log.e("DATA",data.toString());
		int sourceNode = data.getNode().getPhysicalID().getAsInt();
		if (data instanceof FeatureData) {
			// Log.e("Node: "+sourceNode+"","FeatureData");
			superFrame.put(new Integer(sourceNode),((FeatureData) data).getFeatures());

			int nodesNum = manager.getActiveNodes().size();
			if (nodesNum == 1 || (nodesNum == 2 && superFrame.keySet().size() == 2)) {
				int activityId = classifyActivity(superFrame);
				superFrame = new Hashtable<Integer, Feature[]>();
				buffer_in[i++] = activityId;
				if (i == (n - 1)) {
					print(buffer_in);
					statisticsManagerWorkerThread.updateActivityStat(activityId);
					changeUIActivityImage(activityId);
					i = 0;
				}
			}
		}
		
		  if (data instanceof BufferedRawData) {
		  
		   Log.e("Node: "+sourceNode+"","BufferedRawData");
		  
		  int[][] values = ((BufferedRawData) data).getValues();
		  
		  int[][] superFrameMatrix = new int[1][StarterWorkerThread.T_BUFFER_SIZE]; 
		  int i = 0; 
		  for (int n = 0; n < values.length; n++) { 
			  if (values[n] != null)
				  System.arraycopy(values[n], 0, superFrameMatrix[i++], 0, values[n].length);
			  }
		  
		  for (int k = 0; k < superFrameMatrix[0].length; k++)
			  for (int j = 0; j < superFrameMatrix.length; j++) {
				  ActivityRecognitionWidget.samples.add((long) superFrameMatrix[j][k]);
				//  Log.e("sample["+j+"]",sample[j]+""); 
				  	try { 
				  		String s = superFrameMatrix[j][k]+"\n";
				  		ActivityRecognitionWidget.fos = new FileWriter(new File(Environment.getExternalStorageDirectory(),"raw_input.csv"));
				  		ActivityRecognitionWidget.fos.write(s);
				  		ActivityRecognitionWidget.fos.flush(); 
				  		} catch (IOException e) { 
				  			//TODO Auto-generated catch block e.printStackTrace();
				  		} 
				  	} 
		  }
		
	}
	
	public void receivedCloudData(double input) {
		int activityId = (int)input;
		Log.i("UMASS", "Value added: " + activityId);
		statisticsManagerWorkerThread.updateActivityStat(activityId);
		changeUIActivityImage(activityId);
	}

	private void print(int[] buffer) {
		// TODO Auto-generated method stub
		String activities = "";
		for (int a : buffer)
			activities += a + ";";
		Log.e("Buffered activity...", activities);

	}

	private int classifyActivity(Hashtable<Integer, Feature[]> superFrame) {
		Vector<Feature> features = new Vector<Feature>();
		Iterator<Integer> iterator = superFrame.keySet().iterator();
		while (iterator.hasNext()) {
			Feature[] featuresTmp = (Feature[]) superFrame
					.get((Integer) iterator.next());

			for (int i = 0; i < featuresTmp.length; i++) {
				features.add(featuresTmp[i]);
			}
		}

		Utils.orderFeatureVector(features);

		Vector<Integer> newInstance = new Vector<Integer>();
		for (int j = 0; j < features.size(); j++) {

			Integer[] values = features.elementAt(j).getValues();
			for (int i = 0; i < values.length; i++)
				if (values[i] != null) {
					newInstance.addElement(values[i]);
				}
		}

		int activityId = -1;
		double[] newInstanceArray = new double[newInstance.size()];
		for (int i = 0; i < newInstance.size(); i++)
			newInstanceArray[i] = ((Integer) newInstance.elementAt(i))
					.doubleValue();

		if (ActivityRecognitionWidget.customknn) {
			// knn dell'applicazione
			activityId = Classifiers.knn(samples, newInstanceArray, 1);
			Log.e("ClassifyActivity KNN", activityId + "");
		} 
		else if (ActivityRecognitionWidget.customWeka) {
			// classificatori custom di weka
			newInstanceArray = new double[newInstance.size() + 1];
			newInstanceArray[0] = -1;
			for (int i = 1; i < newInstance.size(); i++) {
				newInstanceArray[i] = ((Integer) newInstance.elementAt(i - 1))
						.doubleValue();
			}

			activityId = Classifiers.customJRipClassifier(newInstanceArray);
			Log.e("ClassifyActivity Custom JRIP", activityId + "");

			// activityid = Classifiers.customJ48Classifier(newInstanceArray);
			// Log.e("ClassifyActivity Custom J48",activityId+"");

			// activityId = Classifiers.customKnnClassifier(newInstanceArray);
			// Log.e("ClassifyActivity Custom Knn",activityId+"");

		}

		else if (ActivityRecognitionWidget.isThigh) {
			Log.e("isThigh",ActivityRecognitionWidget.isThigh+"");
			activityId = Classifiers.knn(samples, newInstanceArray, 1);
			Log.e("ClassifyActivity KNN THIGH - BELT", activityId + "");
		}

		else {
			// jrip statico dell'applicazione
			activityId = Classifiers.jrip(newInstanceArray);
			Log.e("ClassifyActivity JRIP", activityId + "");

			// j48 statico dell'applicazione
			/*
			 * Object[] newInstanceArrayObj = new Object[newInstance.size()];
			 * for (int i = 0; i < newInstance.size(); i++){
			 * newInstanceArrayObj[i] = ((Integer)
			 * newInstance.elementAt(i)).doubleValue(); } activityId =
			 * Classifiers.j48(newInstanceArrayObj);
			 * Log.e("ClassifyActivity J48",activityId+"");
			 */
		}

		return activityId;
	}

	private void changeUIActivityImage(int activityId) {

		Bundle data = new Bundle();
		data.putInt(ActivityRecognitionWidget.MonitoringTabHandler.ACTIVITY_ID,
				activityId);
		ActivityRecognitionWidget.activity_buffer = buffer_in;
		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}

	private void changePauseToResumeButton(boolean change) {
		Bundle data = new Bundle();
		data.putBoolean(
				ActivityRecognitionWidget.MonitoringTabHandler.CHANGE_PAUSE_TO_RESUME_BUTTON,
				change);

		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}

	private void enablePauseButton(boolean enable) {
		Bundle data = new Bundle();
		data.putBoolean(
				ActivityRecognitionWidget.MonitoringTabHandler.ENABLE_PAUSE_BUTTON,
				enable);

		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}
}
