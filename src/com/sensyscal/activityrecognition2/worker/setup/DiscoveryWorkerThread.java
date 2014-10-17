package com.sensyscal.activityrecognition2.worker.setup;

import spine.SPINEFactory;
import spine.SPINEManager;

import java.util.Vector;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sensyscal.activityrecognition2.R;
import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;
import com.sensyscal.activityrecognition2.utils.SPINEListenerAdapter;

public class DiscoveryWorkerThread extends SPINEListenerAdapter implements Runnable {
	private Handler handler;

	private SPINEManager manager;
	private int discoveredNodes;

	private Lock lock;
	private Condition discoveryCompleted;

	public DiscoveryWorkerThread(Handler handler) {
		this.handler = handler;
		
		discoveredNodes = 0;
		lock = new ReentrantLock();
		discoveryCompleted = lock.newCondition();

		configureSPINEManager();
	}

	private void configureSPINEManager() {
		try {
			manager = SPINEFactory.getSPINEManagerInstance();
			manager.addListener(this);
		} catch (InstantiationException e) {
			Log.e("SPINEManager error", e.getMessage());
		}
	}

	public void run() {
		showProgressDialog(true);

		discoverNodes();
		if (discoveredNodes > 0)
			updateUI();

		showProgressDialog(false);
		showToastMessage(discoveredNodes + " nodes discovered");
	}

	private void discoverNodes() {
		manager.discoveryWsn(0);

		lock.lock();
		try {
			discoveryCompleted.await();
		} catch (InterruptedException e) {
		}
		lock.unlock();
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
		data.putInt(ActivityRecognitionWidget.SetupTabHandler.CHANGE_AVAILABLE_NODES_TEXTVIEW, discoveredNodes);
		data.putBoolean(ActivityRecognitionWidget.SetupTabHandler.ENABLE_DISCOVERY_BUTTON, false);
		data.putInt(ActivityRecognitionWidget.SetupTabHandler.CHANGE_STATUS_TEXTVIEW, R.string.tab_setup_text_status_standby);
		data.putBoolean(ActivityRecognitionWidget.SetupTabHandler.ENABLE_START_BUTTON, true);

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

	@Override
	public void discoveryCompleted(Vector activeNodes) {
		discoveredNodes = activeNodes.size();
		manager.removeListener(this);

		lock.lock();
		discoveryCompleted.signalAll();
		lock.unlock();
	}
}
