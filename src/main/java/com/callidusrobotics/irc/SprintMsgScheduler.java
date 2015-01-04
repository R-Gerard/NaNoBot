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

import org.quartz.SchedulerException;

public class SprintMsgScheduler extends MessageScheduler {
  public static enum MsgMode {
    MODE_10A("10-minute sprints starting at the top of the hour (with 10-minute breaks in-between)"),
    MODE_10B("10-minute sprints starting at the bottom of the hour (with 10-minute breaks in-between)"),
    MODE_20A("20-minute sprints starting at the top and bottom of the hour"),
    MODE_20B("20-minute sprints starting at the quarter and three-quarter marks");

    private String description;

    MsgMode(final String description) {
      this.description = description;
    }

    public String getDescription() {
      return description;
    }
  }

  private static final String START_WARNING_ID = "timer_start_warning";
  private static final String START_ID = "timer_start";
  private static final String FINISH_WARNING_ID = "timer_finish_warning";
  private static final String FINISH_ID = "timer_finish";

  protected String startWarningMsg = "Sprint in 1 minute";
  protected String startMsg = "GO!";
  protected String finishWarningMsg = "1 minute remaining";
  protected String finishMsg = "TIME'S UP!";

  protected MsgMode mode = MsgMode.MODE_10A;

  public SprintMsgScheduler(NaNoBot naNoBot) {
    super(naNoBot);
  }

  public synchronized void setMode(MsgMode mode) {
    if (mode == null) {
      return;
    }

    try {
      scheduler.clear();
    } catch (SchedulerException e) {
      e.printStackTrace();
    }

    this.mode = mode;

    switch (mode) {
      case MODE_10A:
        scheduleMode10A();
        break;

      case MODE_10B:
        scheduleMode10B();
        break;

      case MODE_20A:
        scheduleMode20A();
        break;

      case MODE_20B:
        scheduleMode20B();
        break;

      default:
        break;
    }
  }

  public MsgMode getMode() {
    return mode;
  }

  public void setStartWarningMsg(String startWarningMsg) {
    this.startWarningMsg = startWarningMsg;
  }

  public void setStartMsg(String startMsg) {
    this.startMsg = startMsg;
  }

  public void setFinishWarningMsg(String finishWarningMsg) {
    this.finishWarningMsg = finishWarningMsg;
  }

  public void setFinishMsg(String finishMsg) {
    this.finishMsg = finishMsg;
  }

  protected void scheduleMode10A() {
    scheduleJob(START_WARNING_ID,  "0 19/20 * * * ? *", startWarningMsg);
    scheduleJob(START_ID,          "0 0/20 * * * ? *", startMsg);
    scheduleJob(FINISH_WARNING_ID, "0 9/20 * * * ? *", finishWarningMsg);
    scheduleJob(FINISH_ID,         "0 10/20 * * * ? *", finishMsg);
  }

  protected void scheduleMode10B() {
    scheduleJob(START_WARNING_ID,  "0 9/20 * * * ? *", startWarningMsg);
    scheduleJob(START_ID,          "0 10/20 * * * ? *", startMsg);
    scheduleJob(FINISH_WARNING_ID, "0 19/20 * * * ? *", finishWarningMsg);
    scheduleJob(FINISH_ID,         "0 0/20 * * * ? *", finishMsg);
  }

  protected void scheduleMode20A() {
    scheduleJob(START_WARNING_ID,  "0 29/30 * * * ? *", startWarningMsg);
    scheduleJob(START_ID,          "0 0/30 * * * ? *", startMsg);
    scheduleJob(FINISH_WARNING_ID, "0 19/30 * * * ? *", finishWarningMsg);
    scheduleJob(FINISH_ID,         "0 20/30 * * * ? *", finishMsg);
  }

  protected void scheduleMode20B() {
    scheduleJob(START_WARNING_ID,  "0 14/30 * * * ? *", startWarningMsg);
    scheduleJob(START_ID,          "0 15/30 * * * ? *", startMsg);
    scheduleJob(FINISH_WARNING_ID, "0 4/30 * * * ? *", finishWarningMsg);
    scheduleJob(FINISH_ID,         "0 5/30 * * * ? *", finishMsg);
  }
}
