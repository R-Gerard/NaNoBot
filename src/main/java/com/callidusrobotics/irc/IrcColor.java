/**
 * Copyright (C) 2013-2014 Rusty Gerard
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

import org.jibble.pircbot.Colors;

public enum IrcColor {
  // Convert org.jibble.pircbot.Colors from String constants to an enumerated type
  NORMAL(Colors.NORMAL),
  BOLD(Colors.BOLD),
  UNDERLINE(Colors.UNDERLINE),
  REVERSE(Colors.REVERSE),
  WHITE(Colors.WHITE),
  BLACK(Colors.BLACK),
  DARK_BLUE(Colors.DARK_BLUE),
  DARK_GREEN(Colors.DARK_GREEN),
  RED(Colors.RED),
  BROWN(Colors.BROWN),
  PURPLE(Colors.PURPLE),
  OLIVE(Colors.OLIVE),
  YELLOW(Colors.YELLOW),
  GREEN(Colors.GREEN),
  TEAL(Colors.TEAL),
  CYAN(Colors.CYAN),
  BLUE(Colors.BLUE),
  MAGENTA(Colors.MAGENTA),
  DARK_GRAY(Colors.DARK_GRAY),
  LIGHT_GRAY(Colors.LIGHT_GRAY);

  private String colorCode;

  IrcColor(String colorCode) {
    this.colorCode = colorCode;
  }

  @Override
  public String toString() {
    return colorCode;
  }
}
