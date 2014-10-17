package com.sensyscalactivityrecognition2.worker.custom;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.sql.Timestamp;

import weka.classifiers.Classifier;
import weka.classifiers.lazy.IBk;
import weka.classifiers.rules.JRip;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;
import com.sensyscal.activityrecognition2.ActivityRecognitionWidget.MonitoringTabHandler;
import com.sensyscal.activityrecognition2.worker.monitoring.MonitoringWorkerThread;


public class WekaWorker {

	private static Classifier cls;
	public static Instances trainingset;
	private Handler handler;
	static Timestamp ts_start = new Timestamp(System.currentTimeMillis());
	static Timestamp ts , ts1;
	public  static String FILE_NAME = "CustomClassifier.txt";

	public WekaWorker(Handler handler) {
		this.handler = handler;
	}

	public void addestra() {
		// TODO Auto-generated method stub
		try {
			
			DataSource source = new DataSource(Environment.getExternalStorageDirectory()+"/"+ActivityRecognitionWidget.TS_FILE);
			trainingset = source.getDataSet();
			trainingset.setClassIndex(0);
			//runJ48();
			runJRip();
			saveClassifier(cls);
			Log.i("Weka Thread - Classifier",cls.toString());

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static void saveClassifier(Classifier classifier) {
		// TODO Auto-generated method stub
		try {
			if(MonitoringWorkerThread.fis!=null)
				MonitoringWorkerThread.fis.close();
			File file = new File(Environment.getExternalStorageDirectory(),FILE_NAME);
			FileOutputStream fos;
			fos = new FileOutputStream(file);
			ObjectOutputStream os = new ObjectOutputStream(fos);
			os.writeObject(classifier);
			os.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void runJRip() {
		// TODO Auto-generated method stub
		ts1 = new Timestamp(System.currentTimeMillis());
		cls = new JRip();
		try {
			cls.buildClassifier(trainingset);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ts = new Timestamp(System.currentTimeMillis());
		Log.i("JRip"," -> Impiegati: "+(ts.getTime()-ts1.getTime())+" ms;\n");	
	}
	
	public static void runJ48() throws Exception{
		ts1 = new Timestamp(System.currentTimeMillis());
		cls = new J48();
		try {
			cls.buildClassifier(trainingset);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ts = new Timestamp(System.currentTimeMillis());
		Log.i("J48"," -> Impiegati: "+(ts.getTime()-ts1.getTime())+" ms;\n");	
	}
	
	private void runKnn() {
		// TODO Auto-generated method stub
		ts1 = new Timestamp(System.currentTimeMillis());
		cls = new IBk();
		try {
			cls.buildClassifier(trainingset);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ts = new Timestamp(System.currentTimeMillis());
		Log.i("Knn"," -> Impiegati: "+(ts.getTime()-ts1.getTime())+" ms;\n");	
	}


}
