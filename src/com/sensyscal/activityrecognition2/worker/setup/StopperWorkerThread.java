package com.sensyscal.activityrecognition2.worker.setup;

import spine.SPINEFactory;
import spine.SPINEManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sensyscal.activityrecognition2.R;
import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;
import com.sensyscal.activityrecognition2.worker.log.LoggerUpdaterWorkerThread;
import com.sensyscal.activityrecognition2.worker.monitoring.MonitoringWorkerThread;
import com.sensyscal.activityrecognition2.worker.statistics.StatisticsManagerWorkerThread;

public class StopperWorkerThread implements Runnable {
	private Handler handler;
	private SPINEManager manager;

	private MonitoringWorkerThread monitoringWorkerThread;
	private StatisticsManagerWorkerThread statisticsManagerWorkerThread;
	private LoggerUpdaterWorkerThread loggerUpdaterWorkerThread;

	public StopperWorkerThread(Handler handler, MonitoringWorkerThread monitoringWorkerThread, StatisticsManagerWorkerThread statisticsManagerWorkerThread,
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

		stopMonitoring();

		updateUI();
		showProgressDialog(false);
		showToastMessage("Monitoring stopped");
	}

	private void stopMonitoring() {
		monitoringWorkerThread.stopMonitoring();
		statisticsManagerWorkerThread.stopMonitoring();
		loggerUpdaterWorkerThread.stopMonitoring();

		manager.resetWsn();
	}

	private void showProgressDialog(boolean show) {
		Bundle data = new Bundle();
		data.putBoolean(ActivityRecognitionWidget.SetupTabHandler.SHOW_PROGRESS_DIALOG, show);

		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}

	private void updateUI() {
		Bundle data = new Bundle();
		data.putInt(ActivityRecognitionWidget.SetupTabHandler.CHANGE_AVAILABLE_NODES_TEXTVIEW, 0);
		data.putBoolean(ActivityRecognitionWidget.SetupTabHandler.ENABLE_DISCOVERY_BUTTON, true);
		data.putInt(ActivityRecognitionWidget.SetupTabHandler.CHANGE_STATUS_TEXTVIEW, R.string.tab_setup_text_status_inactive);
		data.putBoolean(ActivityRecognitionWidget.SetupTabHandler.ENABLE_START_BUTTON, false);
		data.putBoolean(ActivityRecognitionWidget.SetupTabHandler.START_TO_STOP_BUTTON, false);

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
}
