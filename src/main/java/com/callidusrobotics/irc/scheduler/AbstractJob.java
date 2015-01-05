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

import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.callidusrobotics.irc.NaNoBot;

public abstract class AbstractJob implements Job {

  public static final String BOT_KEY = "BOT";

  protected NaNoBot bot;
  protected JobDataMap data;

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    data = context.getJobDetail().getJobDataMap();
    bot = (NaNoBot) data.get(BOT_KEY);

    executeDelegate(context);
  }

  public abstract void executeDelegate(JobExecutionContext context) throws JobExecutionException;
}
