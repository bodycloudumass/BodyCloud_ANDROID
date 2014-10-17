package com.sensyscal.activityrecognition2.worker.statistics;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;
import com.sensyscal.activityrecognition2.utils.ClassesCodes;
import com.sensyscal.activityrecognition2.worker.log.LoggerUpdaterWorkerThread;

public class StatisticsManagerWorkerThread implements Runnable {
	private int walking;
	private int standing;
	private int sitting;
	private int lying;
	private double total;

	double standingStat;
	double walkingStat;
	double sittingStat;
	double lyingStat;

	private Handler handler;

	private int oldActivityId;
	private Lock lock;
	private Condition monitoringCompleted;

	private LoggerUpdaterWorkerThread loggerUpdaterWorkerThread;

	public StatisticsManagerWorkerThread(Handler handler, LoggerUpdaterWorkerThread loggerUpdaterWorkerThread) {
		this.handler = handler;
		this.loggerUpdaterWorkerThread = loggerUpdaterWorkerThread;

		lock = new ReentrantLock();
		monitoringCompleted = lock.newCondition();
		resetStat();
	}

	private void resetStat() {
		walking = 0;
		standing = 0;
		sitting = 0;
		lying = 0;
		total = 0;

		standingStat = 0;
		walkingStat = 0;
		sittingStat = 0;
		lyingStat = 0;

		oldActivityId = -1;
	}

	public void updateActivityStat(int activityId) {
		boolean isChanged = true;
			switch (activityId) {
				case ClassesCodes.STANDING:
					standing++;
					break;
	
				case ClassesCodes.STANDING_WALKING:
					walking++;
					break;
	
				case ClassesCodes.SITTING:
					sitting++;
					break;
	
				case ClassesCodes.LYING:
					lying++;
					break;
	
				default:
					isChanged = false;
			}
	
			if (isChanged) {
				total++;
	
				updateStatistics();
				updateUIActivityStatistics();
	
				if (activityId != oldActivityId) {
					updateUILog(activityId);
	
					double[] stats = new double[4];
					stats[StatisticsSender.STANDING] = standingStat;
					stats[StatisticsSender.WALKING] = walkingStat;
					stats[StatisticsSender.SITTING] = sittingStat;
					stats[StatisticsSender.LYIING] = lyingStat;
	
					StatisticsSender.updateStatistics(activityId, stats);
					oldActivityId = activityId;
				}
			}
	}

	public void updateUILog(int activityId) {
		StringBuilder str = new StringBuilder();

		SimpleDateFormat date = new SimpleDateFormat("yyyy.MM.dd, HH:mm:ss");
		str.append(date.format(new Date(System.currentTimeMillis())));
		str.append(" - " + ClassesCodes.getClassCaption(activityId));

		loggerUpdaterWorkerThread.updateUILog(str.toString());
	}

	private void updateStatistics() {
		standingStat = getStatistic(standing);
		walkingStat = getStatistic(walking);
		sittingStat = getStatistic(sitting);
		lyingStat = getStatistic(lying);
	}

	private double getStatistic(double val) {
		double stat = (val / total) * 100;

		BigDecimal bd = new BigDecimal(stat);
		bd = bd.setScale(1, BigDecimal.ROUND_HALF_EVEN);

		return bd.doubleValue();
	}

	private void updateUIActivityStatistics() {
		String activityId;
		Bundle data = new Bundle();

		activityId = ActivityRecognitionWidget.StatisticsTabHandler.CHANGE_STANDING_TEXTVIEW;
		data.putDouble(activityId, standingStat);

		activityId = ActivityRecognitionWidget.StatisticsTabHandler.CHANGE_WALKING_TEXTVIEW;
		data.putDouble(activityId, walkingStat);

		activityId = ActivityRecognitionWidget.StatisticsTabHandler.CHANGE_SITTING_TEXTVIEW;
		data.putDouble(activityId, sittingStat);

		activityId = ActivityRecognitionWidget.StatisticsTabHandler.CHANGE_LYING_TEXTVIEW;
		data.putDouble(activityId, lyingStat);

		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}

	private void resetUIActivityStat() {
		Bundle data = new Bundle();
		data.putInt(ActivityRecognitionWidget.StatisticsTabHandler.CHANGE_STANDING_TEXTVIEW, 0);
		data.putInt(ActivityRecognitionWidget.StatisticsTabHandler.CHANGE_WALKING_TEXTVIEW, 0);
		data.putInt(ActivityRecognitionWidget.StatisticsTabHandler.CHANGE_SITTING_TEXTVIEW, 0);
		data.putInt(ActivityRecognitionWidget.StatisticsTabHandler.CHANGE_LYING_TEXTVIEW, 0);

		Message msg = handler.obtainMessage();
		msg.setData(data);
		handler.sendMessage(msg);
	}

	@Override
	public void run() {
		new Thread(loggerUpdaterWorkerThread).start();

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

		resetStat();
		resetUIActivityStat();
		StatisticsSender.resetStatistics();
	}
}
