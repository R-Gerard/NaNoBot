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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.callidusrobotics.irc.scheduler.SprintMsgScheduler;
import com.callidusrobotics.irc.scheduler.SprintMsgScheduler.MsgMode;

public class NaNoBot extends PircBot implements Runnable {

  private transient static final Logger CHANNEL_LOGGER = LoggerFactory.getLogger(NaNoBot.class.getPackage().getName() + ".IrcChannelLog");
  private transient static final Logger PM_LOGGER = LoggerFactory.getLogger(NaNoBot.class.getPackage().getName() + ".IrcPmLog");

  protected SprintMsgScheduler scheduler;
  protected BotProperties botProperties;
  protected String channel;
  protected IrcColor fontColor;
  protected List<String> controlUsers = new ArrayList<String>();

  public static void main(final String[] args) throws FileNotFoundException, IOException, NickAlreadyInUseException, IrcException {
    if (args.length > 0 && "--version".equals(args[0])) {
      System.out.println("NaNoBot version: " + getImplementationVersion());
      System.exit(0);
    }

    final String fileName = args.length > 0 ? args[0] : "./NaNoBot.properties";
    final Properties properties = new Properties();
    properties.load(new FileInputStream(new File(fileName)));

    final NaNoBot naNoBot = new NaNoBot(properties);

    Runtime.getRuntime().addShutdownHook(new Thread() {
      @Override
      public void run() {
        naNoBot.shutdown();
      }
    });

    naNoBot.run();
  }

  public static String getImplementationVersion() {
    final Package myPackage = NaNoBot.class.getPackage();
    final String implementationVersion = myPackage.getImplementationVersion();

    if (implementationVersion == null || StringUtils.isBlank(implementationVersion)) {
      return "DEBUG";
    }

    return implementationVersion;
  }

  public NaNoBot(final Properties properties) throws NickAlreadyInUseException, IOException, IrcException {   
    botProperties = new BotProperties(properties);

    String server = botProperties.getServer();
    channel = botProperties.getChannel();
    String name = botProperties.getLogin();
    boolean verbose = botProperties.isVerbose();

    String[] users = botProperties.getControlUsers();
    for (String user : users) {
      user = user.trim().toLowerCase();
      if (user.length() > 0) {
        controlUsers.add(user);
      }
    }

    fontColor = botProperties.getFontColor();

    Validate.notBlank(server);
    Validate.notBlank(channel);
    Validate.notBlank(name);

    setVerbose(verbose);
    setName(name);
    setLogin(name);

    // Look for a property called "scripts" which should be a comma-separated list
    // scripts = foo.groovy, bar.groovy
    // Then search for a corresponding property for each script, the value of which should be a cron expression
    // TODO: Verify that the specified script files actually exist AND that the cronExpression is valid before adding the keypairs to the map
    Map<String,String> scriptsMap = new HashMap<String,String>();
    String[] scripts = botProperties.getScripts();
    for (String script : scripts) {
      String cronExpression = properties.getProperty(script);
      if (!StringUtils.isBlank(cronExpression)) {
        scriptsMap.put(script, cronExpression);
      }
    }

    scheduler = new SprintMsgScheduler(this, scriptsMap);

    scheduler.setStartWarningMsg(botProperties.getSprintStartWarningMsg());
    scheduler.setStartMsg(botProperties.getSprintStartMsg());
    scheduler.setFinishWarningMsg(botProperties.getSprintFinishWarningMsg());
    scheduler.setFinishMsg(botProperties.getSprintFinishMsg());

    scheduler.setMode(MsgMode.MODE_10A);
  }

  public Properties getProperties() {
    return botProperties.getProperties();
  }

  public List<String> getUsers() {
    User[] users = getUsers(channel);
    List<String> result = new ArrayList<String>(users.length);
    for (User user : users) {
      result.add(user.getNick());
    }

    return result;
  }

  public void sendMessageToChannel(String message) {
    CHANNEL_LOGGER.info(getName() + ": " + message);
    sendMessage(channel, fontColor + message);
  }

  public void sendMessageToUser(String user, String message) {
    PM_LOGGER.info(getName() + " (PRIVMSG TO " + user + "): " + message);
    sendMessage(user, fontColor + message);
  }

  @Override
  protected void onNickChange(String oldNick, String login, String hostname, String newNick) {
    CHANNEL_LOGGER.info("*** " + oldNick + " is now known as " + newNick);
  }

  @Override
  protected void onJoin(String channel, String sender, String login, String hostname) {
    CHANNEL_LOGGER.info("*** " + sender + " joined " + channel);
  }

  @Override
  protected void onPart(String channel, String sender, String login, String hostname) {
    CHANNEL_LOGGER.info("*** " + sender + " left " + channel);
  }

  @Override
  protected void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason) {
    CHANNEL_LOGGER.info("*** " + sourceNick + " quit IRC. (Reason: " + reason + ")");
  }

  @Override
  protected void onAction(String sender, String login, String hostname, String target, String action) {
    CHANNEL_LOGGER.info("/" + sender + " " + action);
  }

  @Override
  protected void onNotice(String sourceNick, String sourceLogin, String sourceHostname, String target, String notice) {
    CHANNEL_LOGGER.info(sourceNick + " announces: " + notice);
  }

  @Override
  protected void onMessage(String channel, String sender, String login, String hostname, String message) {
    CHANNEL_LOGGER.info(sender + ": " + message);
  }

  @Override
  protected void onPrivateMessage(String sender, String login, String hostname, String message) {
    PM_LOGGER.info(sender + ": " + message);

    if (!controlUsers.isEmpty() && !controlUsers.contains(sender.toLowerCase())) {
      sendMessageToUser(sender, "Access Denied");
    }

    String[] tokens = message.split("\\s+");
    for (int i = tokens.length - 1; i >= 0; i--) {
      String token = tokens[i].toLowerCase();

      for (MsgMode mode : MsgMode.values()) {
        if (token.contains(mode.toString().toLowerCase())) {
          sendMessageToUser(sender, "Setting sprint mode to '" + mode + "' (" + mode.getDescription() + ").");
          sendMessageToChannel(sender + " set sprint mode to '" + mode + "' (" + mode.getDescription() + ").");
          scheduler.setMode(mode);
          return;
        }
      }

      if (token.contains("version")) {
        sendMessageToUser(sender, getImplementationVersion());
        return;
      }

      if (token.contains("current") || token.contains("mode")) {
        sendMessageToUser(sender, "My current mode is: " + scheduler.getMode() + ".");
        return;
      }

      if (token.contains("help") || token.contains("explain") || token.contains("?")) {
        sendMessageToUser(sender, "My current mode is: " + scheduler.getMode() + ".");
        sendMessageToUser(sender, "These are my supported modes:");
        for (MsgMode mode : MsgMode.values()) {
          sendMessageToUser(sender, mode + ": " + mode.getDescription() + ".");
        }
      }
    }
  }

  @Override
  public void run() {
    scheduler.start();

    if (botProperties.identServerEnabled()) {
      startIdentServer();
    }

    try {
      while (true) {
        if (!isConnected() || getChannels().length == 0) {
          try {
            disconnect();
            connectAndJoin();
          } catch (Exception e) {
            log(ExceptionUtils.getStackTrace(e));
          }
        }

        Thread.sleep(20000);
      }
    } catch (InterruptedException e) {}

    shutdown();
  }

  private void connectAndJoin() throws NickAlreadyInUseException, IOException, IrcException {
    String server = botProperties.getServer();
    int port = botProperties.getPort();
    String password = botProperties.getPassword();

    connect(server, port, password);
    joinChannel(channel);
    sendMessageToChannel(botProperties.getStartupMsg());
  }

  public void shutdown() {
    sendMessageToChannel(botProperties.getShutdownMsg());
    scheduler.stop();
    quitServer("Shutting down");
    disconnect();
  }
}
