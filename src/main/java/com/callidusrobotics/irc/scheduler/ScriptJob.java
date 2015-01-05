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

import java.io.IOException;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

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

    Binding binding = new Binding();
    // TODO: Get bot's properties and set each key,value pair as a variable bindings

    try {
      gse.run(script, binding);
    } catch (ResourceException | ScriptException e) {
      throw new JobExecutionException(e);
    }

    String message = (String) binding.getVariable("message");
    if (!StringUtils.isBlank(message)) {
      bot.sendMessage(message);
    }
  }
}
