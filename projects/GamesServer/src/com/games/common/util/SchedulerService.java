package com.games.common.util;

import java.util.Date;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class SchedulerService {
	 private ScheduledExecutorService service;
	 
	 public SchedulerService(ScheduledExecutorService service) {
		 this.service = service;
	 }
	 
	 public void countdown(Runnable command, long howLong) {
		 service.schedule(command, howLong, TimeUnit.MILLISECONDS);
	 }
	 
	 public void daily(Runnable command, int hourOfDay, int minuteOfHour, int secondOfMinite) {
		 long delay = Utils.getDelay(hourOfDay, minuteOfHour, secondOfMinite);
		 long period = 1000 * 60 * 60 * 24;
		 service.scheduleAtFixedRate(command, delay, period, TimeUnit.MILLISECONDS);
	 }
	 
	 public void daily(Runnable command, int hourOfDay, int minuteOfHour, int secondOfMinite, int interval) {
		 long delay = Utils.getDelayToNextTimepoint(hourOfDay, minuteOfHour, secondOfMinite, interval);
		 service.scheduleAtFixedRate(command, delay, interval, TimeUnit.MILLISECONDS);
	 }
	 
	 public void weekly(Runnable command, int dayOfWeek, int hourOfDay, int minuteOfHour, int secondOfMinite) {
		 long delay = Utils.getDelay(dayOfWeek, hourOfDay, minuteOfHour, secondOfMinite);
		 long period = 1000 * 60 * 60 * 24 * 7;
		 service.scheduleAtFixedRate(command, delay, period, TimeUnit.MILLISECONDS);
	 }
	 
	 public void oneDay(Runnable command, int year, int month, int day, int hour, int minute, int second) {
		 long delay = Utils.getDate(year, month, day, hour, minute, second).getTime() - System.currentTimeMillis();
		 service.schedule(command, delay, TimeUnit.MILLISECONDS);
	 }
	 
	 public void oneDay(Runnable command, Date date) {
		 long delay = date.getTime() - System.currentTimeMillis();
		 service.schedule(command, delay, TimeUnit.MILLISECONDS);
	 }
	 
	 
}
