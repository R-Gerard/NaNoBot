/**
 * Copyright (C) 2013-2015 Rusty Gerard
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

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

public class MessageScheduler {

  protected NaNoBot naNoBot;
  protected Scheduler scheduler;

  public MessageScheduler(NaNoBot naNoBot) {
    this.naNoBot = naNoBot;
    SchedulerFactory schedulerFactactory = new StdSchedulerFactory();

    try {
      scheduler = schedulerFactactory.getScheduler();
    } catch (SchedulerException e) {
      e.printStackTrace();
    }
  }

  public void start() {
    try {
      scheduler.start();
    } catch (SchedulerException e) {
      e.printStackTrace();
    }
  }

  public void stop() {
    try {
      scheduler.shutdown(true);
    } catch (SchedulerException e) {
      e.printStackTrace();
    }
  }

  public void scheduleJob(String id, String cronExpression, String message) {
    JobDetail jobDetail = JobBuilder.newJob(MessageJob.class).withIdentity(id + "_job", "messages").build();

    jobDetail.getJobDataMap().put(MessageJob.BOT_KEY, naNoBot);
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
