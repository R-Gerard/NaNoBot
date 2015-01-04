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
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;

import com.callidusrobotics.irc.SprintMsgScheduler.MsgMode;

public class NaNoBot extends PircBot implements Runnable {

  protected SprintMsgScheduler scheduler;
  protected String channel;
  protected String startupMessage, shutdownMessage;
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

  private static String getImplementationVersion() {
    final Package myPackage = NaNoBot.class.getPackage();
    final String implementationVersion = myPackage.getImplementationVersion();

    if (implementationVersion == null || StringUtils.isBlank(implementationVersion)) {
      return "DEBUG";
    }

    return implementationVersion;
  }

  public NaNoBot(final Properties properties) throws NickAlreadyInUseException, IOException, IrcException {
    String server = properties.getProperty("irc.server", "irc.sorcery.net");
    Integer port = Integer.parseInt(properties.getProperty("irc.port", "6667"));
    channel = properties.getProperty("irc.channel", "#wrimosea");
    String name = properties.getProperty("user.login", "NaNoBot");
    String password = properties.getProperty("user.password");

    boolean verbose = Boolean.parseBoolean(properties.getProperty("log.verbose", Boolean.TRUE.toString()));
    boolean runIdentServer = Boolean.parseBoolean(properties.getProperty("identServer.enabled", Boolean.TRUE.toString()));

    String userList = properties.getProperty("controlUsers", "");
    String[] users = userList.split(",");
    for (String user : users) {
      user = user.trim().toLowerCase();
      if (user.length() > 0) {
        controlUsers.add(user);
      }
    }

    fontColor = IrcColor.valueOf(StringUtils.upperCase(properties.getProperty("font.color", IrcColor.BLACK.name())));

    startupMessage = properties.getProperty("bot.startupMessage", "Hello, everyone!");
    shutdownMessage = properties.getProperty("bot.shutdownMessage", "Goodbye, everyone!");

    String startWarningMsg = properties.getProperty("sprint.startWarningMessage", "Sprint in 1 minute");
    String startMsg = properties.getProperty("sprint.startMessage", "GO!");
    String finishWarningMsg = properties.getProperty("sprint.finishWarningMessage", "1 minute remaining");
    String finishMsg = properties.getProperty("sprint.finishMessage", "TIME'S UP!");

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

    scheduler = new SprintMsgScheduler(this);

    scheduler.setStartWarningMsg(startWarningMsg);
    scheduler.setStartMsg(startMsg);
    scheduler.setFinishWarningMsg(finishWarningMsg);
    scheduler.setFinishMsg(finishMsg);

    scheduler.setMode(MsgMode.MODE_10A);
  }

  public void sendMessage(String message) {
    sendMessage(channel, fontColor + message);
  }

  @Override
  protected void onPrivateMessage(String sender, String login, String hostname, String message) {
    System.out.println("Recieved PM from " + sender + ": " + message);

    if (!controlUsers.isEmpty() && !controlUsers.contains(sender.toLowerCase())) {
      sendMessage(sender, fontColor + "Access Denied");
    }

    String[] tokens = message.split("\\s+");
    for (int i = tokens.length - 1; i >= 0; i--) {
      String token = tokens[i].toLowerCase();

      for (MsgMode mode : MsgMode.values()) {
        if (token.contains(mode.toString().toLowerCase())) {
          sendMessage(sender, fontColor + "Setting sprint mode to '" + mode + "' (" + mode.getDescription() + ").");
          sendMessage(channel, fontColor + sender + " set sprint mode to '" + mode + "' (" + mode.getDescription() + ").");
          scheduler.setMode(mode);
          return;
        }
      }

      if (token.contains("version")) {
        sendMessage(sender, fontColor + getImplementationVersion());
        return;
      }

      if (token.contains("current") || token.contains("mode")) {
        sendMessage(sender, fontColor + "My current mode is: " + scheduler.getMode() + ".");
        return;
      }

      if (token.contains("help") || token.contains("explain") || token.contains("?")) {
        sendMessage(sender, fontColor + "My current mode is: " + scheduler.getMode() + ".");
        sendMessage(sender, fontColor + "These are my supported modes:");
        for (MsgMode mode : MsgMode.values()) {
          sendMessage(sender, fontColor + "" + mode + ": " + mode.getDescription() + ".");
        }
      }
    }
  }

  @Override
  public void run() {
    sendMessage(startupMessage);
    scheduler.start();

    try {
      while (true) {
        Thread.sleep(1000);
      }
    } catch (InterruptedException e) {}

    shutdown();
  }

  public void shutdown() {
    sendMessage(shutdownMessage);
    scheduler.stop();
    quitServer("Shutting down");
    disconnect();
  }
}
