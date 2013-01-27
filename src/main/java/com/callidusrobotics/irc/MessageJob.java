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

import org.jibble.pircbot.PircBot;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MessageJob implements Job {

  public static final String BOT_KEY = "BOT";
  public static final String CHANNEL_KEY = "CHANNEL";
  public static final String MESSAGE_KEY = "MESSAGE";

  protected PircBot bot;
  protected String channel, message;

  public MessageJob() {}

  @Override
  public void execute(JobExecutionContext context) throws JobExecutionException {
    JobDataMap data = context.getJobDetail().getJobDataMap();

    bot = (PircBot) data.get(BOT_KEY);
    channel = data.getString(CHANNEL_KEY);
    message = data.getString(MESSAGE_KEY);

    bot.sendMessage(channel, message);
  }

}
