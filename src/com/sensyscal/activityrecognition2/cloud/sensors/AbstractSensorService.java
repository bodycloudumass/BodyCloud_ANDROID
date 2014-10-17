package com.sensyscal.activityrecognition2.cloud.sensors;

import android.app.Service;
import android.content.Intent;

import com.sensyscal.activityrecognition2.ActivityRecognitionWidget;

public abstract class AbstractSensorService extends Service {

	//public static List<Integer> users = new LinkedList<Integer>();
	
	public void startUsingSensor(Class<?> clazz, int id){ 
		ActivityRecognitionWidget.getInstance().startService(new Intent(ActivityRecognitionWidget.getInstance(), clazz));
	}
	
	public void stopUsingSensor(Class<?> clazz, int id){
		ActivityRecognitionWidget.getInstance().stopService(new Intent(ActivityRecognitionWidget.getInstance(), 
				clazz));
	}
	
	/*
	public void startUsingSensor(Class<?> clazz, int id) {
		AbstractSensorService.users.add(id);
		System.out.println("ADDED NELLA LISTA ID: " + id);
		if(AbstractSensorService.users.size() == 1){
			ActivityRecognitionWidget.getInstance().startService(new Intent(ActivityRecognitionWidget.getInstance(), 
					clazz));
		}
	}
	
	
	public void stopUsingSensor(Class<?> clazz, int id){
		System.out.println("REMOVE NELLA LISTA ID: " + id);
		
		AbstractSensorService.users.remove(id);
		if(AbstractSensorService.users.size() == 0) 
			ActivityRecognitionWidget.getInstance().stopService(new Intent(ActivityRecognitionWidget.getInstance(), 
					clazz));
	}*/
}
