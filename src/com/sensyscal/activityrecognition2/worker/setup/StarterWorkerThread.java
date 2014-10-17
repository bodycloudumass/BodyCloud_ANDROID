package com.sensyscal.activityrecognition2.worker.setup;

import spine.SPINEFactory;
import spine.SPINEFunctionConstants;
import spine.SPINEManager;
import spine.SPINESensorConstants;
import spine.datamodel.Address;
import spine.datamodel.Feature;
import spine.datamodel.Node;
import spine.datamodel.functions.BufferedRawDataSpineFunctionReq;
import spine.datamodel.functions.BufferedRawDataSpineSetupFunction;
import spine.datamodel.functions.FeatureSpineFunctionReq;
import spine.datamodel.functions.FeatureSpineSetupFunction;
import spine.datamodel.functions.SpineSetupSensor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sensyscal.activityrecognition2.R;
import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;
import com.sensyscal.activityrecognition2.worker.log.LoggerUpdaterWorkerThread;
import com.sensyscal.activityrecognition2.worker.monitoring.MonitoringWorkerThread;
import com.sensyscal.activityrecognition2.worker.statistics.StatisticsManagerWorkerThread;

public class StarterWorkerThread implements Runnable {
	private static final int SAMPLING_TIME = 25;
	private static final short WINDOW_SIZE = 40;
	private static final short SHIFT_SIZE = 20;
	
	public static final short T_BUFFER_SIZE = 16;	
	private static final short T_SHIFT_SIZE = 16;

	private Handler handler;
	private SPINEManager manager;

	private MonitoringWorkerThread monitoringWorkerThread;
	private StatisticsManagerWorkerThread statisticsManagerWorkerThread;
	private LoggerUpdaterWorkerThread loggerUpdaterWorkerThread;

	public StarterWorkerThread(Handler handler, MonitoringWorkerThread monitoringWorkerThread, StatisticsManagerWorkerThread statisticsManagerWorkerThread,
			LoggerUpdaterWorkerThread loggerUpdaterWorkerThread) {
		this.handler = handler;
		this.monitoringWorkerThread = monitoringWorkerThread;
		this.statisticsManagerWorkerThread = statisticsManagerWorkerThread;
		this.loggerUpdaterWorkerThread = loggerUpdaterWorkerThread;

		configureSPINEManager();
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
		showProgressDialog(true);

		configureNodes();
		startMonitoring();

		updateUI();
		showProgressDialog(false);
		showToastMessage("Monitoring started");
	}

	private void configureNodes() {

		FeatureSpineFunctionReq sfr = new FeatureSpineFunctionReq();
		FeatureSpineSetupFunction ssf = new FeatureSpineSetupFunction();
		SpineSetupSensor sss = new SpineSetupSensor();
		
		
		Node node_1 = manager.getNodeByPhysicalID(new Address("1"));
		if(node_1!=null){
	
			sss.setSensor(SPINESensorConstants.ACC_SENSOR);
			sss.setTimeScale(SPINESensorConstants.MILLISEC);
			sss.setSamplingTime(SAMPLING_TIME);
			manager.setup(node_1, sss);
			
			try { Thread.sleep(200); } catch (InterruptedException e) {}		
			
			ssf.setSensor(SPINESensorConstants.ACC_SENSOR);
			ssf.setWindowSize(WINDOW_SIZE);
			ssf.setShiftSize(SHIFT_SIZE);
			manager.setup(node_1, ssf);
			
			try {Thread.sleep(200);} catch (InterruptedException e) {}
	
			sfr.setSensor(SPINESensorConstants.ACC_SENSOR);
			
			if(ActivityRecognitionWidget.isThigh){
				sfr.add(new Feature(SPINEFunctionConstants.MAX, SPINESensorConstants.CH3_ONLY));
				sfr.add(new Feature(SPINEFunctionConstants.MIN, SPINESensorConstants.CH3_ONLY));
			}
			
			else{
				sfr.add(new Feature(SPINEFunctionConstants.MAX, SPINESensorConstants.CH2_ONLY));
				sfr.add(new Feature(SPINEFunctionConstants.MIN, SPINESensorConstants.CH2_ONLY));
				sfr.add(new Feature(SPINEFunctionConstants.TOTAL_ENERGY, SPINESensorConstants.CH3_ONLY));
			}
			
			manager.activate(node_1, sfr);
			
			try {Thread.sleep(200);} catch (InterruptedException e) {}
		}

		Node node_2 = manager.getNodeByPhysicalID(new Address("2"));
		
		if (node_2 != null) {
			sss = new SpineSetupSensor();
			sss.setSensor(SPINESensorConstants.ACC_SENSOR);
			sss.setTimeScale(SPINESensorConstants.MILLISEC);
			sss.setSamplingTime(SAMPLING_TIME);
			manager.setup(node_2, sss);
			
			try {Thread.sleep(200);} catch (InterruptedException e) {}

			ssf = new FeatureSpineSetupFunction();
			ssf.setSensor(SPINESensorConstants.ACC_SENSOR);
			ssf.setWindowSize(WINDOW_SIZE);
			ssf.setShiftSize(SHIFT_SIZE);
			manager.setup(node_2, ssf);
			
			try {Thread.sleep(200);} catch (InterruptedException e) {}

			sfr = new FeatureSpineFunctionReq();
			sfr.setSensor(SPINESensorConstants.ACC_SENSOR);
			
			if(ActivityRecognitionWidget.isThigh){
				sfr.add(new Feature(SPINEFunctionConstants.MIN, SPINESensorConstants.CH3_ONLY));
			}
			
			else{
				sfr.add(new Feature(SPINEFunctionConstants.MIN, SPINESensorConstants.CH2_ONLY));
				sfr.add(new Feature(SPINEFunctionConstants.RANGE, SPINESensorConstants.CH2_ONLY));
				sfr.add(new Feature(SPINEFunctionConstants.TOTAL_ENERGY, SPINESensorConstants.CH3_ONLY));
			}
			
			manager.activate(node_2, sfr);
			
			try {Thread.sleep(200);} catch (InterruptedException e) {}
		}
		
		/*node_1 = manager.getNodeByPhysicalID(new Address("1"));
		if(node_1!=null){
			
			BufferedRawDataSpineSetupFunction brdsf = new BufferedRawDataSpineSetupFunction();
			brdsf.setSensor(SPINESensorConstants.ACC_SENSOR);
			brdsf.setBufferSize(T_BUFFER_SIZE);
			brdsf.setShiftSize(T_SHIFT_SIZE);
			manager.setup(node_1, brdsf);
			
			try { Thread.sleep(200); } catch (InterruptedException e) {}
			
			BufferedRawDataSpineFunctionReq brdfr = new BufferedRawDataSpineFunctionReq();
			brdfr.setSensor(SPINESensorConstants.ACC_SENSOR);
			brdfr.setChannelsBitmask(SPINESensorConstants.CH2_ONLY);					
			manager.activate(node_1, brdfr);
			
			try { Thread.sleep(200); } catch (InterruptedException e) {}
		}
		*/
	}

	private void startMonitoring() {

		if(ActivityRecognitionWidget.isThigh)
			monitoringWorkerThread.loadTrainingSet();

		new Thread(monitoringWorkerThread).start();
		new Thread(statisticsManagerWorkerThread).start();
		new Thread(loggerUpdaterWorkerThread).start();

		manager.startWsn(true, false);
	}

	private void showProgressDialog(boolean show) {
		Bundle data = new Bundle();
		data.putBoolean(ActivityRecognitionWidget.SetupTabHandler.SHOW_PROGRESS_DIALOG, show);

		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}

	private void showToastMessage(String str) {
		Bundle data = new Bundle();
		data.putString(ActivityRecognitionWidget.SetupTabHandler.SHOW_TOAST, str);

		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}

	private void updateUI() {
		Bundle data = new Bundle();
		data.putBoolean(ActivityRecognitionWidget.SetupTabHandler.START_TO_STOP_BUTTON, true);
		data.putInt(ActivityRecognitionWidget.SetupTabHandler.CHANGE_STATUS_TEXTVIEW, R.string.tab_setup_text_status_active);

		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}
}
