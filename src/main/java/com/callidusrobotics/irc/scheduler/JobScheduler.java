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

package com.callidusrobotics.irc.scheduler;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;

import com.callidusrobotics.irc.NaNoBot;

public class JobScheduler {

  private NaNoBot naNoBot;
  private Scheduler scheduler;

  public JobScheduler(NaNoBot naNoBot) {
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

  public void clear() {
    try {
      scheduler.clear();
    } catch (SchedulerException e) {
      e.printStackTrace();
    }
  }

  private void scheduleJob(String id, String cronExpression, String group, JobDetail jobDetail) {
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

  public void scheduleMessageJob(String id, String cronExpression, String message) {
    JobDetail jobDetail = JobBuilder.newJob(MessageJob.class).withIdentity(id + "_job", "messages").build();

    jobDetail.getJobDataMap().put(MessageJob.BOT_KEY, naNoBot);
    jobDetail.getJobDataMap().put(MessageJob.MESSAGE_KEY, message);

    scheduleJob(id, cronExpression, "messages", jobDetail);
  }

  public void scheduleScriptJob(String id, String cronExpression, String script) {
    JobDetail jobDetail = JobBuilder.newJob(ScriptJob.class).withIdentity(id + "_job", "scripts").build();

    jobDetail.getJobDataMap().put(ScriptJob.BOT_KEY, naNoBot);
    jobDetail.getJobDataMap().put(ScriptJob.SCRIPT_KEY, script);

    scheduleJob(id, cronExpression, "scripts", jobDetail);
  }
}
