package com.games.common.util;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import gnu.trove.map.TIntIntMap;
import gnu.trove.map.hash.TIntIntHashMap;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

public class Utils {
	private static final Logger logger = Logger.getLogger(Utils.class);
	
    public static final String INBORN_LOG_CONFIG = "config/log4j.properties";	

	/**
	 * get the localhost's ip
	 * 
	 */
	public static String getLocalHostIp() {
		String ip = "127.0.0.1";
		try {
			InetAddress localHostAddress = InetAddress.getLocalHost();
			ip = localHostAddress.getHostAddress();
		} catch (Exception ex) {
			System.out.println("fectch localhost ip error.");
		}
		return ip;
	}
	

	public static void main(String[] args) {
		PropertyConfigurator.configure(INBORN_LOG_CONFIG);
	}

    public static Date initDate() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.YEAR, 1970);
        return c.getTime();
    }
	
	public static boolean isSameDay(long date1, long date2) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTimeInMillis(date1);
		cal2.setTimeInMillis(date2);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);		
	}
	
	public static boolean isSameDay(Date date1, Date date2) {
		Calendar cal1 = Calendar.getInstance();
		Calendar cal2 = Calendar.getInstance();
		cal1.setTime(date1);
		cal2.setTime(date2);
		return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR)
				&& cal1.get(Calendar.DAY_OF_YEAR) == cal2.get(Calendar.DAY_OF_YEAR);
	}
	
	public static String makeString(int... codePoint) {
	    StringBuffer sb = new StringBuffer();
	    for (int cp : codePoint)
	        sb.appendCodePoint(cp);
	    return sb.toString();
	}
	
	// month start from 0, that is JANUARY is 0;
	// day start from 1;
    public static Date getDate(int year, int month, int day, int hour, int minute, int second) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(0);
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month);
        cal.set(Calendar.DAY_OF_MONTH, day);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.set(Calendar.SECOND, second);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }
    
	public static Calendar getEarliestDate(Calendar cal, int dayOfWeek,
			int hourOfDay, int minuteOfHour, int secondOfMinite) {
		// YEAR + DAY_OF_WEEK + WEEK_OF_YEAR
		int currentDayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
		int currentHour = cal.get(Calendar.HOUR_OF_DAY);
		int currentMinute = cal.get(Calendar.MINUTE);
		int currentSecond = cal.get(Calendar.SECOND);

		if (dayOfWeek < currentDayOfWeek
				|| dayOfWeek == currentDayOfWeek && hourOfDay < currentHour
				|| hourOfDay == currentHour && minuteOfHour < currentMinute
				|| minuteOfHour == currentMinute && secondOfMinite < currentSecond) {
			cal.add(Calendar.WEEK_OF_YEAR, 1);
		}

		cal.set(Calendar.DAY_OF_WEEK, dayOfWeek);
		cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
		cal.set(Calendar.MINUTE, minuteOfHour);
		cal.set(Calendar.SECOND, secondOfMinite);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	public static long getDelay(int dayOfWeek, int hourOfDay, int minuteOfHour, int secondOfMinite) {
		Calendar curDate = Calendar.getInstance();
		long cur = curDate.getTime().getTime();
		Calendar earliestDate = getEarliestDate(curDate, dayOfWeek, hourOfDay, minuteOfHour, secondOfMinite);
		long earliest = earliestDate.getTime().getTime();
		return earliest - cur;
	}
	
	public static Calendar getEarliestDate(Calendar cal, int hourOfDay,
			int minuteOfHour, int secondOfMinite) {
		// YEAR + DAY_OF_YEAR
		int currentHour = cal.get(Calendar.HOUR_OF_DAY);
		int currentMinute = cal.get(Calendar.MINUTE);
		int currentSecond = cal.get(Calendar.SECOND);

		if (hourOfDay < currentHour 
				|| hourOfDay == currentHour && minuteOfHour < currentMinute
				|| minuteOfHour == currentMinute && secondOfMinite < currentSecond) {
			cal.add(Calendar.DAY_OF_YEAR, 1);
		}

		cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
		cal.set(Calendar.MINUTE, minuteOfHour);
		cal.set(Calendar.SECOND, secondOfMinite);
		cal.set(Calendar.MILLISECOND, 0);
		return cal;
	}
	
	public static Calendar getNextDate(Calendar cal, int hourOfDay,
			int minuteOfHour, int secondOfMinite, int interval) {
		long now = cal.getTimeInMillis();		
		cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
		cal.set(Calendar.MINUTE, minuteOfHour);
		cal.set(Calendar.SECOND, secondOfMinite);
		cal.set(Calendar.MILLISECOND, 0);		
		long start = cal.getTimeInMillis();
		while (start < now) {
			start += interval;
		}
		cal.setTimeInMillis(start);
		return cal;
	}
	
	public static long getDelay(int hourOfDay, int minuteOfHour, int secondOfMinite) {
		Calendar curDate = Calendar.getInstance();
		long cur = curDate.getTime().getTime();
		Calendar earliestDate = getEarliestDate(curDate, hourOfDay, minuteOfHour, secondOfMinite);
		long earliest = earliestDate.getTime().getTime();
		return earliest - cur;
	}
	
	public static long getDelayToNextTimepoint(int hourOfDay, int minuteOfHour, int secondOfMinite, int interval) {
		Calendar curDate = Calendar.getInstance();
		long cur = curDate.getTime().getTime();
		Calendar earliestDate = getNextDate(curDate, hourOfDay, minuteOfHour, secondOfMinite, interval);
		long earliest = earliestDate.getTime().getTime();
		return earliest - cur;
	}
	
	public static Date getToday() {
		Calendar today = Calendar.getInstance();
		today.set(Calendar.HOUR_OF_DAY, 0);
		today.set(Calendar.MINUTE, 0);
		today.set(Calendar.SECOND, 0);
		today.set(Calendar.MILLISECOND, 0);
		return today.getTime();
	}
	

}
