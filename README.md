NaNoBot
=======

IRC chatbot for adminstering "sprints" (timed sessions with breaks).

NaNoBot is built on top of PircBot and Quartz.
* http://www.jibble.org/pircbot.php
* http://quartz-scheduler.org/


Starting NaNoBot
----------------

Start NaNoBot from the command line using
```
java -jar NaNoBot.jar
```

Depending on your firewall settings you may need to open ports 6667 and 113 to TCP traffic.


Checking The Executable Version
-------------------------------

You can check the version of your NaNoBot executable from the command line using
```
java -jar NaNoBot.jar --version
```


Properties File Format
----------------------

NaNoBot expects a Java properties file called `NaNoBot.properties` to exist in the current working directory.

You can override the properties file name/location at the command line. Example:
```
java -jar NaNoBot.jar /path/to/foo.properties
```

Summary of NaNoBot.properties:

| Property | Description | Default value |
| -------- | ----------- | ------------- |
| irc.server | The hostname of the server to connect to | irc.sorcery.net |
| irc.port | The port number to connect to on the server | 6667 |
| irc.channel | The name of the channel to broadcast messages to | #wrimosea |
| identServer.enabled | If this should start an ident server (RFC 1413) on port 113 | true |
| log.verbose | If verbose logging on the client should be used | true |
| user.login | The login to use to join the server AND the nick to display | NaNoBot |
| user.password | The password to use to join the server | N/A |
| controlUsers | A comma-separated list of user nicks that may send PMs to NaNoBot. An empty list means all users may PM NaNoBot. NaNoBot replies, "Access denied" to unauthorized users. | empty (all users may PM NaNoBot) |
| font.color | The friendly-name of the IRC color code to use when sending messages | BLACK |
| bot.startupMessage | The message to broadcast to the channel when the bot joins the server | Hello, everyone! |
| bot.shutdownMessage | The message to broadcast to the channel when the bot leaves the server | Goodbye, everyone! |
| sprint.startWarningMessage | The message to broadcast to the channel when a sprint is about to start | Sprint in 1 minute |
| sprint.startMessage | The message to broadcast to the channel when a sprint has started | GO! |
| sprint.finishWarningMessage | The message to broadcast to the channel when a sprint is about to end | 1 minute remaining |
| sprint.finishMessage | The message to broadcast to the channel when a sprint has ended | TIME'S UP! |


Sprint Modes
------------

NaNoBot supports several types of sprints or "modes". Each mode does the following:
* 1 minute prior to the start of the sprint, NaNoBot broadcasts "start warning" message to the channel
* At the start of the sprint, NaNoBot broadcasts a "start" message to the channel
* 1 minute prior to the end of the sprint, NaNoBot broadcasts a "stop warning" message to the channel
* At the end of the sprint, NaNoBot broadcasts a "stop" message to the channel


Controlling Sprint Modes
------------------------

Send NaNoBot a private message to view/change modes.

| Private Message | Description | Aliases |
| --------------- | ----------- | ------- |
| version | Displays NaNoBot's version | N/A |
| help | Displays NaNoBot's current mode and the name and description of each mode it supports | "explain", '?' |
| mode | Displays NaNoBot's current mode | "current" |
| {mode name} | Sets NaNoBot's current mode to the desired mode | N/A |

Examples:
```
/msg NaNoBot version

NaNoBot 1.0.0

/msg NaNoBot mode

NaNoBot	My current mode is: MODE_10A.

/msg NaNoBot MODE_20A

NaNoBot	Setting sprint mode to 'MODE_20A' (20-minute sprints starting at the top and bottom of the hour).

/msg NaNoBot explain

NaNoBot	My current mode is: MODE_20A.
NaNoBot	These are my supported modes:
NaNoBot	MODE_10A: 10-minute sprints starting at the top of the hour (with 10-minute breaks in-between).
NaNoBot	MODE_10B: 10-minute sprints starting at the bottom of the hour (with 10-minute breaks in-between).
NaNoBot	MODE_20A: 20-minute sprints starting at the top and bottom of the hour.
NaNoBot	MODE_20B: 20-minute sprints starting at the quarter and three-quarter marks.
```


Compiling NaNoBot from source
-----------------------------

Get Apache Maven (http://maven.apache.org/) and run `mvn package` from the root directory of the project.
