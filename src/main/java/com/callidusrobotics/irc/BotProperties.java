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

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

public class BotProperties {
  protected Properties properties;

  public enum BotPropertyKey {
    SERVER("irc_server", "irc.sorcery.net"),
    PORT("irc_port", "6667"),
    CHANNEL("irc_channel", "#wrimosea"),
    LOGIN("user_login", "NaNoBot"),
    PASSWORD("user_password", ""),
    VERBOSITY("log_verbose", Boolean.TRUE.toString()),
    IDENTSERVER("identServer_enabled", Boolean.TRUE.toString()),
    CONTROL_USERS("controlUsers", ""),
    FONT_COLOR("font_color", IrcColor.BLACK.name()),
    STARTUP_MSG("bot_startupMessage", "Hello, everyone!"),
    SHUTDOWN_MSG("bot_shutdownMessage", "Goodbye, everyone!"),
    SPRINT_START_WARN_MSG("sprint_startWarningMessage", "Sprint in 1 minute"),
    SPRINT_START_MSG("sprint_startMessage", "GO!"),
    SPRINT_FINISH_WARN_MSG("sprint_finishWarningMessage", "1 minute remaining"),
    SPRINT_FINISH_MSG("sprint_finishMessage", "TIME'S UP!"),
    SCRIPTS("scripts", "");

    private String key;
    private String defaultValue;

    BotPropertyKey(String key, String defaultValue) {
      this.key = key;
      this.defaultValue = defaultValue;
    }

    public String getKey() {
      return key;
    }

    public String getDefaultValue() {
      return defaultValue;
    }
  }

  public BotProperties(Properties properties) {
    this.properties = new Properties();

    for (BotPropertyKey prop : BotPropertyKey.values()) {
      String value = properties.getProperty(prop.getKey(), prop.getDefaultValue());
      setProperty(prop, value);
    }
  }

  public Properties getProperties() {
    return properties;
  }

  public void setProperty(BotPropertyKey prop, String value) {
    properties.setProperty(prop.getKey(), value);
  }

  public String getProperty(BotPropertyKey prop) {
    return properties.getProperty(prop.getKey());
  }

  public String getServer() {
    return getProperty(BotPropertyKey.SERVER);
  }

  public int getPort() {
    return Integer.parseInt(getProperty(BotPropertyKey.PORT));
  }

  public String getChannel() {
    return getProperty(BotPropertyKey.CHANNEL);
  }

  public String getLogin() {
    return getProperty(BotPropertyKey.LOGIN);
  }

  public String getPassword() {
    return getProperty(BotPropertyKey.PASSWORD);
  }

  public boolean isVerbose() {
    return Boolean.parseBoolean(getProperty(BotPropertyKey.VERBOSITY));
  }

  public boolean identServerEnabled() {
    return Boolean.parseBoolean(getProperty(BotPropertyKey.IDENTSERVER));
  }

  public IrcColor getFontColor() {
    return IrcColor.valueOf(StringUtils.upperCase(getProperty(BotPropertyKey.FONT_COLOR)));
  }

  public String[] getControlUsers() {
    return getProperty(BotPropertyKey.CONTROL_USERS).split(",\\s*");
  }

  public String getStartupMsg() {
    return getProperty(BotPropertyKey.STARTUP_MSG);
  }

  public String getShutdownMsg() {
    return getProperty(BotPropertyKey.SHUTDOWN_MSG);
  }

  public String getSprintStartWarningMsg() {
    return getProperty(BotPropertyKey.SPRINT_START_WARN_MSG);
  }

  public String getSprintStartMsg() {
    return getProperty(BotPropertyKey.SPRINT_START_MSG);
  }

  public String getSprintFinishWarningMsg() {
    return getProperty(BotPropertyKey.SPRINT_FINISH_WARN_MSG);
  }

  public String getSprintFinishMsg() {
    return getProperty(BotPropertyKey.SPRINT_FINISH_MSG);
  }

  public String[] getScripts() {
    return getProperty(BotPropertyKey.SCRIPTS).split(",\\s*");
  }
}
