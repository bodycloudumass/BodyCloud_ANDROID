package com.sensyscal.activityrecognition2.cloud.utility;

import java.sql.Timestamp;
import java.util.Calendar;

/**
 * Calendar Utility Class
 *
 * @author Marc Whitlow
 *         Colabrativ, Inc.
 *         http://www.colabrativ.com
 */
 public class CalendarUtilities {

	    /**
	     * <p>Checks if two calendars represent the same day ignoring time.</p>
	     * @param cal1  the first calendar, not altered, not null
	     * @param cal2  the second calendar, not altered, not null
	     * @return true if they represent the same day
	     * @throws IllegalArgumentException if either calendar is <code>null</code>
	     */
	    public static boolean isSameDay(Calendar cal1, Calendar cal2) {
	        if (cal1 == null || cal2 == null) {
	            throw new IllegalArgumentException("The dates must not be null");
	        }
	        return (cal1.get(Calendar.ERA) == cal2.get(Calendar.ERA) &&
	                cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
	                cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR));
	    }
	 
    /**
     * Determine the duration between the start and end dates.
     *
     * @param startDate Duration starting date
     * @param endDate Duration end date
     *
     * @return String describing the time between the start and end dates,
     * e.g. 2 years 47 days 6 hours and 1 minute.
     */
    public static String duration(Timestamp startDate, Timestamp endDate) {

        Calendar startCalendar = Calendar.getInstance();
        Calendar endCalendar   = Calendar.getInstance();
        startCalendar.setTime( startDate);
        endCalendar  .setTime( endDate);

        return duration(startCalendar, endCalendar);
    }

    /**
     * Determine the duration between the start and end dates.
     *
     * @param startCalendar Duration starting date
     * @param endCalendar Duration end date
     *
     * @return String describing the time between the start and end dates,
     * e.g. 2 years 1 day 6 hours and 23 minutes.
     */
    public static String duration(Calendar startCalendar, Calendar endCalendar)
    {
        int years 	= endCalendar.get(Calendar.YEAR) 		- startCalendar.get(Calendar.YEAR);
        int days 	= endCalendar.get(Calendar.DAY_OF_YEAR) - startCalendar.get(Calendar.DAY_OF_YEAR);
        int hours 	= endCalendar.get(Calendar.HOUR_OF_DAY) - startCalendar.get(Calendar.HOUR_OF_DAY);
        int mins 	= endCalendar.get(Calendar.MINUTE) 		- startCalendar.get(Calendar.MINUTE);
        int seconds 	= endCalendar.get(Calendar.SECOND) 		- startCalendar.get(Calendar.SECOND);

        if (seconds < 0) {
            mins = mins - 1;
            seconds  = seconds + 60;
        }
        
        if (mins < 0) {
            hours = hours - 1;
            mins  = mins + 60;
        }

        if (hours < 0) {
            days  = days - 1;
            hours = hours + 24;
        }

        // Leap year corrections
        int daysInYear = 365;
        Calendar leapYear = Calendar.getInstance();
        leapYear.set( startCalendar.get(Calendar.YEAR), 11, 31, 23, 59, 59);
        if (leapYear.get(Calendar.DAY_OF_YEAR) == 366) {
            leapYear.set( startCalendar.get(Calendar.YEAR), 1, 29, 23, 59, 59);
            if (startCalendar.before(leapYear))
                daysInYear = 366;
        }

        leapYear.set( endCalendar.get(Calendar.YEAR), 11, 31, 23, 59, 59);
        if (leapYear.get(Calendar.DAY_OF_YEAR) == 366) {
            leapYear.set( endCalendar.get(Calendar.YEAR), 1, 29, 23, 59, 59);
            if (endCalendar.after(leapYear)) {
                daysInYear = 366;
                if (years > 0)
                    days = days - 1;
            }
        }

        if (days < 0) {
            years--;
            days = days + daysInYear;
        }

        StringBuilder durationSB = new StringBuilder();
        if (years > 0) {
            durationSB.append( years);
            addUnits( durationSB, years, "year");
        }

        if (days > 0) {
            durationSB.append( days);
            addUnits( durationSB, days, "day");
        }

        if (hours > 0) {
            durationSB.append( hours);
            addUnits( durationSB, hours, "hour");
        }

        if (mins > 0) {
            durationSB.append( mins);
            addUnits( durationSB, mins, "minute");
        }
        
        if(seconds > 0){
            durationSB.append(seconds);
            addUnits( durationSB, seconds, "second");
        }

        return durationSB.toString();
    }

    private static void addUnits( StringBuilder stringBuilder, int value, String units) {

        stringBuilder.append(" ");
        if (value == 1)
            stringBuilder.append( units + " ");
        else
            stringBuilder.append( units + "s ");
    }
    
 }