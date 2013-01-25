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

public class NaNoBot extends PircBot implements Runnable {
  
  protected String channel;

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
    boolean verbose = Boolean.parseBoolean(properties.getProperty("log.verbose", Boolean.TRUE.toString()));
    boolean runIdentServer = Boolean.parseBoolean(properties.getProperty("identServer.enabled", Boolean.TRUE.toString()));
    
    Validate.notBlank(server);
    Validate.notNull(port);
    Validate.notBlank(channel);
    Validate.notBlank(name);
    
    if (runIdentServer) {
      startIdentServer();
    }
    
    setVerbose(verbose);
    setName(name);
    setLogin(name);
    connect(server, port, password);
    joinChannel(channel);
  }

  public void run() {
    sendMessage(channel, "Hello, World!");
    
    try {
      Thread.sleep(5000);
    } catch (InterruptedException e) {}
    
    disconnect();
  }
}
