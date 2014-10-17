package com.sensyscalactivityrecognition2.worker.custom;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import spine.SPINEFactory;
import spine.SPINEManager;
import spine.SPINESensorConstants;
import spine.datamodel.Address;
import spine.datamodel.BufferedRawData;
import spine.datamodel.Data;
import spine.datamodel.Node;
import spine.datamodel.functions.BufferedRawDataSpineFunctionReq;
import spine.datamodel.functions.BufferedRawDataSpineSetupFunction;
import spine.datamodel.functions.SpineSetupSensor;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;
import com.sensyscal.activityrecognition2.utils.SPINEListenerAdapter;

public class CustomDataCollectorWorkerThread extends SPINEListenerAdapter implements
		Runnable {

	
	private static final int SAMPLING_TIME = 25;
	private static final short BUFFER_SIZE = 16;
	private static final short SHIFT_SIZE = 16;
	private static final long SLEEP_TIME = 200;
	private static final String SEPARATOR = ";";

	public static final int ACTIVITY_STANDING = 0;
	public static final int ACTIVITY_WALKING = 3;
	public static final int ACTIVITY_SITTING = 10;
	public static final int ACTIVITY_LYING_DOWN = 20;
	
	private static File file;
	private FileWriter fileWriter;
	
	private static boolean isRecording = false;
	private SPINEManager manager;
	private Handler handler;
	private Lock lock;
	private Condition recordingCompleted;
	public static int current_Activity;
	public static List<Integer> registered_activity = new LinkedList<Integer>();
	public static int cont_activity_rec = 0;

	public static String rawStanding = "", rawSitting= "", rawWalking = "", rawLying = "";

	public CustomDataCollectorWorkerThread(Handler handler) {
		// TODO Auto-generated constructor stub
		this.handler = handler;
		lock = new ReentrantLock();
		recordingCompleted = lock.newCondition();
		
		configureSPINEManager();
	}

	private void configureNodes() {
		// TODO Auto-generated method stub
		Node node_1 = manager.getNodeByPhysicalID(new Address("1"));

		if (node_1 != null) {
			SpineSetupSensor sss = new SpineSetupSensor();
			sss.setSensor(SPINESensorConstants.ACC_SENSOR);
			sss.setTimeScale(SPINESensorConstants.MILLISEC);
			sss.setSamplingTime(SAMPLING_TIME);
			manager.setup(node_1, sss);

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
			}

			BufferedRawDataSpineSetupFunction brdsf = new BufferedRawDataSpineSetupFunction();
			brdsf.setSensor(SPINESensorConstants.ACC_SENSOR);
			brdsf.setBufferSize(BUFFER_SIZE);
			brdsf.setShiftSize(SHIFT_SIZE);
			manager.setup(node_1, brdsf);

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
			}

			BufferedRawDataSpineFunctionReq brdfr = new BufferedRawDataSpineFunctionReq();
			brdfr.setSensor(SPINESensorConstants.ACC_SENSOR);
			brdfr.setChannelsBitmask(SPINESensorConstants.CH1_CH2_CH3_ONLY);
			manager.activate(node_1, brdfr);

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
			}
		}

		Node node_2 = manager.getNodeByPhysicalID(new Address("2"));

		if (node_2 != null) {
			SpineSetupSensor sss = new SpineSetupSensor();
			sss.setSensor(SPINESensorConstants.ACC_SENSOR);
			sss.setTimeScale(SPINESensorConstants.MILLISEC);
			sss.setSamplingTime(SAMPLING_TIME);
			manager.setup(node_2, sss);

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
			}

			BufferedRawDataSpineSetupFunction brdsf = new BufferedRawDataSpineSetupFunction();
			brdsf.setSensor(SPINESensorConstants.ACC_SENSOR);
			brdsf.setBufferSize(BUFFER_SIZE);
			brdsf.setShiftSize(SHIFT_SIZE);
			manager.setup(node_2, brdsf);

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
			}

			BufferedRawDataSpineFunctionReq brdfr = new BufferedRawDataSpineFunctionReq();
			brdfr.setSensor(SPINESensorConstants.ACC_SENSOR);
			brdfr.setChannelsBitmask(SPINESensorConstants.CH1_CH2_CH3_ONLY);
			manager.activate(node_2, brdfr);

			try {
				Thread.sleep(SLEEP_TIME);
			} catch (InterruptedException e) {
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

	@Override
	public void run() {
		// TODO Auto-generated method stub
		configureNodes();
		manager.addListener(this);
		manager.startWsn(true, false);
		
		Bundle data = new Bundle();
		data.putString(ActivityRecognitionWidget.CustomTabHandler.CONFIGURATION_FINISHED,"Sensori configurati correttamente.");
		data.putString(ActivityRecognitionWidget.CustomTabHandler.CHANGE_SETUP_BUTTON,"Next");
		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
		
		registered_activity.add(ACTIVITY_STANDING);
		registered_activity.add(ACTIVITY_WALKING);
		registered_activity.add(ACTIVITY_SITTING);
		registered_activity.add(ACTIVITY_LYING_DOWN);
			
		lock.lock();
		try {
			recordingCompleted.await();
		} catch (InterruptedException e) {
		}
		lock.unlock();
	}
	
	public void setRecording() {
		// TODO Auto-generated method stub
		current_Activity = registered_activity.get(cont_activity_rec);
		showToast(getActivity(current_Activity));
		changeSetupButton("Rec");
	}
	
	private void setActivityToWrite() {
		// TODO Auto-generated method stub
		if(current_Activity == ACTIVITY_STANDING)
			rawStanding = "";
		else if(current_Activity == ACTIVITY_WALKING)
			rawWalking = "";
		else if(current_Activity == ACTIVITY_SITTING)
			rawSitting = "";
		else if(current_Activity == ACTIVITY_LYING_DOWN)
			rawLying = "";
	}

	public void startRecording() {
		// TODO Auto-generated method stub
		setActivityToWrite();
		enableButton(false);
		isRecording = true;
		new Thread(new Cronometro()).start();
	}
	
	private void enableButton(boolean enable) {
		Bundle data = new Bundle();
		data.putBoolean(ActivityRecognitionWidget.CustomTabHandler.ENABLE_BUTTON,enable);
		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}
	
	public void showToast(String str) {
		Bundle data = new Bundle();
		data.putString(ActivityRecognitionWidget.CustomTabHandler.SHOW_TOAST_CURRENT_ACTIVITY,str);
		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}
	
	public void changeSetupButton(String str){
		Bundle data = new Bundle();
		data.putString(ActivityRecognitionWidget.CustomTabHandler.CHANGE_SETUP_BUTTON,str);
		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}
	
	private void showProgressDialog(boolean show) {
		Bundle data = new Bundle();
		data.putBoolean(ActivityRecognitionWidget.CustomTabHandler.SHOW_PROGRESS_DIALOG, show);

		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}

	public void stopRecording() {
		manager.removeListener(this);
		lock.lock();
		recordingCompleted.signalAll();
		lock.unlock();
		
		showProgressDialog(true);
		writeRawDataToFile();
		showProgressDialog(false);
	}
	
	
	Hashtable superFrame = new Hashtable();

	public void received(Data data) {
		if(isRecording){
			if (data instanceof BufferedRawData && ((BufferedRawData) data).getSensorCode() == SPINESensorConstants.ACC_SENSOR) {
				int[][] values = ((BufferedRawData) data).getValues();
	
				superFrame.put(new Integer(data.getNode().getPhysicalID()
						.getAsInt()), values);
	
				if (manager.getActiveNodes().size() == superFrame.keySet().size()) {
	
					Object[] keyset = superFrame.keySet().toArray();
					Arrays.sort(keyset);
	
					int[][] superFrameMatrix = new int[3 * manager.getActiveNodes().size()][BUFFER_SIZE];
					int i = 0;
					for (int p = 0; p < keyset.length; p++) {
						int[][] temp = (int[][]) superFrame.get((Integer) keyset[p]);
						if(temp!=null){
							for (int n = 0; n < temp.length; n++) {
								if (temp[n] != null)
									System.arraycopy(temp[n], 0, superFrameMatrix[i++],
											0, temp[n].length);
							}
						}
					}
	
					long[] sample = new long[1 + superFrameMatrix.length];
					sample[0] = current_Activity;
					for (int k = 0; k < superFrameMatrix[0].length; k++) {
						for (int j = 1; j <= superFrameMatrix.length; j++) {
							sample[j] = superFrameMatrix[j - 1][k];
						}
	
						String feats = generateStringToWrite(sample);
						feats += "\n";
						addFeatsToStringActivity(feats);
	
					}
					superFrame = new Hashtable();
				}
			}
		}
	}

	private void addFeatsToStringActivity(String feats) {
		// TODO Auto-generated method stub
		if(current_Activity == ACTIVITY_STANDING){
			rawStanding += feats;
		}
		if(current_Activity == ACTIVITY_WALKING){
			rawWalking += feats;
		}
		if(current_Activity == ACTIVITY_SITTING){
			rawSitting += feats;
		}
		if(current_Activity == ACTIVITY_LYING_DOWN){
			rawLying += feats;
		}
		
	}
	
	private void writeRawDataToFile(){
	
		try {
			file = new File(Environment.getExternalStorageDirectory(),ActivityRecognitionWidget.FOLDER+"/"+ActivityRecognitionWidget.RAWDATA_FILE);
			fileWriter = new FileWriter(file);
			
			// Write Raw Data to file
			fileWriter.write("class;1_1_1_0;1_1_1_1;1_1_1_2;2_1_1_0;2_1_1_1;2_1_1_2\n");
			fileWriter.write(CustomDataCollectorWorkerThread.rawStanding);
			fileWriter.write(CustomDataCollectorWorkerThread.rawWalking);
			fileWriter.write(CustomDataCollectorWorkerThread.rawSitting);
			fileWriter.write(CustomDataCollectorWorkerThread.rawLying);

			fileWriter.flush();
			fileWriter.close();
			// End write raw data
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		
	}

	private String generateStringToWrite(long features[]) {
		String feats = "";

		for (int i = 0; i < features.length - 1; i++)
			feats += features[i] + SEPARATOR;

		return feats += features[features.length - 1];
	}

	public static String getActivity(int activity) {
		String s = "";
		if (activity == 0)
			s+= "Stai in piedi";
		if (activity == 3)
			s+= "Cammina";
		if (activity == 10)
			s+= "Stai seduto";
		if(activity==20)
			s+= "Stai sdraiato";
		s+=" per due minuti;";
		return s;

	}
	

	public static void setCurrentActivity(int current_activity) {
		// TODO Auto-generated method stub
		current_Activity = current_activity;
		
	}

	public long getCurrentActivity() {
		// TODO Auto-generated method stub
		return current_Activity;
	}

	public boolean isRecording() {
		return isRecording;
	}

	public class Cronometro implements Runnable{

		public Cronometro() {
		}

		@Override
		public void run() {
			long start = new Date().getTime();
			long tmp;
			String s = "0:00:00.0";
			while((System.currentTimeMillis()-start <= 120000)){
					tmp = new Date().getTime();
					if (tmp - start > 1000) {
						long diffTime = System.currentTimeMillis() - start;
	
						int decSeconds = (int) (diffTime % 1000 / 100);
						int seconds = (int) (diffTime / 1000 % 60);
						int minutes = (int) (diffTime / 60000 % 60);
						int hours = (int) (diffTime / 3600000);
	
						s = String.format("%d:%02d:%02d.%d", hours, minutes,
								seconds, decSeconds);
						updateCronometer(s);
						
					}
					
			}
			isRecording = false;
			cont_activity_rec++;
			if(cont_activity_rec<4){
				changeSetupButton("Next");
				enableButton(true);
			}
			else{
				changeSetupButton("Personalizza");
				enableButton(true);
			}
			updateCronometer("");
				
		}

		private void updateCronometer(String s) {
			// TODO Auto-generated method stub
			Bundle data = new Bundle();
			data.putString(ActivityRecognitionWidget.CustomTabHandler.UPDATE_CUSTOM_TEXTVIEW_CRONOMETER,s);
			Message msg = handler.obtainMessage();
			msg.setData(data);
			handler.sendMessage(msg);
		}

	}

}
