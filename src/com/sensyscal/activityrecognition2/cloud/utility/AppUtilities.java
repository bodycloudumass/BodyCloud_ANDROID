package com.sensyscal.activityrecognition2.cloud.utility;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Locale;

public final class AppUtilities {

	public static String NULL_DATE = "-";
	
	public static Timestamp getTimeStamp() { return new Timestamp(System.currentTimeMillis()); }

	public static String getTimeStampString(){
		return getTimeStampString(getTimeStamp());
	}
	
	public static String getTimeStampString(Timestamp timestamp){
		if(timestamp == null) return NULL_DATE;
		return String.valueOf(timestamp.getTime());
	}
	
	public static String getStringTime()
	{
    	Calendar c = Calendar.getInstance(Locale.ITALY);
    	return c.get(Calendar.HOUR_OF_DAY) + ":" + 
    				c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);
	}
	
	public static String getStringDateFromMills()
	{
		return getStringDateFromMills(getTimeStamp().getTime());
	}

	public static String getStringDateFromMills(String timestamp)
	{
		if(timestamp == null) return NULL_DATE;
		try{ return getStringDateFromMills(Long.valueOf(timestamp)); }
		catch (Exception e){ return NULL_DATE; }
	}
	
	public static String getStringDateFromMills(Long timestamp)
	{
    	Calendar c = Calendar.getInstance();
    	c.setTimeInMillis(timestamp);
    	
    	if(CalendarUtilities.isSameDay(c, Calendar.getInstance()))
	    	return c.get(Calendar.HOUR_OF_DAY) + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);
    	
    	return c.get(Calendar.DAY_OF_MONTH) + "-" + c.get(Calendar.MONTH) + "-" + 
				c.get(Calendar.YEAR) + " " + c.get(Calendar.HOUR_OF_DAY) + ":" + 
				c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND);
	}
}