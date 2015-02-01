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

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.callidusrobotics.irc.NaNoBot;

public class ScriptJob extends AbstractJob {

  public static final String SCRIPT_KEY = "SCRIPT";
  private static final String[] ROOTS = new String[] { "./scripts" };

  protected String script;
  protected GroovyScriptEngine gse;
  
  public ScriptJob() {
    try {
      gse = new GroovyScriptEngine(ROOTS);
    } catch (IOException e) {
      e.printStackTrace();
      gse = null;
    }
  }

  @Override
  public void executeDelegate(JobExecutionContext context) throws JobExecutionException {
    if (gse == null) {
      return;
    }
    
    script = data.getString(SCRIPT_KEY);    
    System.out.println("executing script '" + script + "'...");

    // Set each property as a variable binding for the script engine
    Binding binding = new Binding();
    Properties properties = bot.getProperties();
    for (Entry<Object,Object> entry : properties.entrySet()) {
      String key = (String) entry.getKey();
      String value = (String) entry.getValue();

      binding.setVariable(key, value);
    }

    // Add the list of users in the channel as a variable binding for the script engine
    binding.setVariable("USERS", bot.getUsers());

    // Add the bot's version as a variable binding for the script engine
    binding.setVariable("VERSION", NaNoBot.getImplementationVersion());

    try {
      gse.run(script, binding);
    } catch (ResourceException | ScriptException e) {
      throw new JobExecutionException(e);
    }

    // Persist the variable bindings back into the bot's properties
    @SuppressWarnings("unchecked")
    Map<Object,Object> bindingVariables = (Map<Object,Object>) binding.getVariables();
    for (Entry<Object,Object> entry : bindingVariables.entrySet()) {
      String key = (String) entry.getKey();

      if (key.equals("MESSAGE") || key.equals("USERS") || key.equals("VERSION") || key.startsWith("_")) {
        continue;
      }

      String value = entry.getValue() == null ? "" : entry.getValue().toString();
      bot.getProperties().setProperty(key, value);
    }

    // Send the script's message to the channel
    if (binding.hasVariable("MESSAGE")) {
      String message = (String) binding.getVariable("MESSAGE");
      if (!StringUtils.isBlank(message)) {
        bot.sendMessageToChannel(message);
      }
    }
  }
}
