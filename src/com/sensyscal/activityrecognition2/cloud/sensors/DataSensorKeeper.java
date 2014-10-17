package com.sensyscal.activityrecognition2.cloud.sensors;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentLinkedQueue;

public class DataSensorKeeper {

	protected static DataSensorKeeper DATA_SENSOR_KEEPER;
	public static DataSensorKeeper getInstance(){ 
		if(DATA_SENSOR_KEEPER == null) 
			DATA_SENSOR_KEEPER = new DataSensorKeeper();
		return DATA_SENSOR_KEEPER; 
	}
	
	public ConcurrentLinkedQueue<String> valuesQueue = new ConcurrentLinkedQueue<String>();
	
	public Object[] getValues() {
		Object[] values = valuesQueue.toArray();
		return values;
	}
}
