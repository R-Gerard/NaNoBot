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

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

public class MessageJob extends AbstractJob {

  public static final String BOT_KEY = "BOT";
  public static final String MESSAGE_KEY = "MESSAGE";

  protected String message;

  @Override
  public void executeDelegate(JobExecutionContext context) throws JobExecutionException {
    message = data.getString(MESSAGE_KEY);

    bot.sendMessageToChannel(message);
  }
}
