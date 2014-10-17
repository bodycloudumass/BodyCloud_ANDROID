package com.sensyscal.activityrecognition2.worker.log;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;

import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;

public class LoggerUpdaterWorkerThread implements Runnable {
	private Handler handler;
	private Lock lock;
	private Condition monitoringCompleted;

	public LoggerUpdaterWorkerThread(Handler handler) {
		this.handler = handler;

		lock = new ReentrantLock();
		monitoringCompleted = lock.newCondition();
	}

	@Override
	public void run() {
		lock.lock();
		try {
			monitoringCompleted.await();
		} catch (InterruptedException e) {
		}
		lock.unlock();
	}

	public void stopMonitoring() {
		lock.lock();
		monitoringCompleted.signalAll();
		lock.unlock();

		resetUILog();
	}

	public void updateUILog(String str) {
		Bundle data = new Bundle();
		data.putString(ActivityRecognitionWidget.LogTabHandler.UPDATE_LOG_TEXTAREA, str);

		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}

	private void resetUILog() {
		Bundle data = new Bundle();
		data.putBoolean(ActivityRecognitionWidget.LogTabHandler.RESET_LOG_TEXTAREA, true);
		
		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}
}
