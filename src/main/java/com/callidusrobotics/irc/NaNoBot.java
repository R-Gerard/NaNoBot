/**
 * Copyright (C) 2013 Rusty Gerard
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.callidusrobotics.irc;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.Validate;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class NaNoBot extends PircBot implements Runnable {

  protected Scheduler scheduler;
  protected String channel;
  protected String startupMessage;

  public static void main(final String[] args) throws FileNotFoundException, IOException, NickAlreadyInUseException, IrcException {
    final String fileName = args.length > 0 ? args[0] : "./NaNoBot.properties";
    final Properties properties = new Properties();
    properties.load(new FileInputStream(new File(fileName)));

    final NaNoBot naNoBot = new NaNoBot(properties);
    naNoBot.run();
  }

  public NaNoBot(final Properties properties) throws NickAlreadyInUseException, IOException, IrcException {
    String server = properties.getProperty("irc.server", "irc.sorcery.net");
    Integer port = Integer.parseInt(properties.getProperty("irc.port", "6667"));
    channel = properties.getProperty("irc.channel", "#wrimosea");
    String name = properties.getProperty("user.login", "NaNoBot");
    String password = properties.getProperty("user.password");

    startupMessage = properties.getProperty("bot.startupMessage", "Hello, World!");

    boolean verbose = Boolean.parseBoolean(properties.getProperty("log.verbose", Boolean.TRUE.toString()));
    boolean runIdentServer = Boolean.parseBoolean(properties.getProperty("identServer.enabled", Boolean.TRUE.toString()));

    Validate.notBlank(server);
    Validate.notNull(port);
    Validate.notBlank(channel);
    Validate.notBlank(name);

    setVerbose(verbose);
    setName(name);
    setLogin(name);

    if (runIdentServer) {
      startIdentServer();
    }

    connect(server, port, password);
    joinChannel(channel);

    initScheduler();

    // TODO: Read these from the config file and create them dynamically
    scheduleJob("timer_start_warning", "0 9/20 * 1/1 * ? *", "Sprint in 1 minute");
    scheduleJob("timer_start", "0 10/20 * 1/1 * ? *", "GO!");
    scheduleJob("timer_finish_warning", "0 19/20 * 1/1 * ? *", "1 minute remaining");
    scheduleJob("timer_finish", "0 0/20 * 1/1 * ? *", "TIME'S UP!");
  }

  public void run() {
    sendMessage(channel, startupMessage);
    startScheduler();

    try {
      while (true) {
        Thread.sleep(1000);
      }
    } catch (InterruptedException e) {}

    stopScheduler();
    disconnect();
  }

  protected void initScheduler() {
    SchedulerFactory schedulerFactactory = new StdSchedulerFactory();

    try {
      scheduler = schedulerFactactory.getScheduler();
    } catch (SchedulerException e) {
      e.printStackTrace();
    }
  }

  protected void startScheduler() {
    try {
      scheduler.start();
    } catch (SchedulerException e) {
      e.printStackTrace();
    }
  }

  protected void stopScheduler() {
    try {
      scheduler.shutdown(true);
    } catch (SchedulerException e) {
      e.printStackTrace();
    }
  }

  protected void scheduleJob(String id, String cronExpression, String message) {
    JobDetail jobDetail = JobBuilder.newJob(MessageJob.class).withIdentity(id + "_job", "messages").build();

    jobDetail.getJobDataMap().put(MessageJob.BOT_KEY, this);
    jobDetail.getJobDataMap().put(MessageJob.CHANNEL_KEY, channel);
    jobDetail.getJobDataMap().put(MessageJob.MESSAGE_KEY, message);

    Trigger trigger = TriggerBuilder.newTrigger()
        .withIdentity(id + "_trigger", "messages")
        .withSchedule(CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionIgnoreMisfires())
        .forJob(jobDetail)
        .build();

    try {
      scheduler.scheduleJob(jobDetail, trigger);
      System.out.println("Scheduled job " + id);
    } catch (SchedulerException e) {
      e.printStackTrace();
    }
  }
}
