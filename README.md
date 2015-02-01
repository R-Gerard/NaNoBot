NaNoBot
=======

IRC chatbot for adminstering "sprints" (timed sessions with breaks).

NaNoBot is built on top of PircBot, Quartz, and Groovy.
* http://www.jibble.org/pircbot.php
* http://quartz-scheduler.org/
* http://groovy.codehaus.org/


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
| irc_server | The hostname of the server to connect to | irc.sorcery.net |
| irc_port | The port number to connect to on the server | 6667 |
| irc_channel | The name of the channel to broadcast messages to | #wrimosea |
| identServer_enabled | If this should start an ident server (RFC 1413) on port 113 | true |
| log_verbose | If verbose logging on the client should be used | true |
| user_login | The login to use to join the server AND the nick to display | NaNoBot |
| user_password | The password to use to join the server | N/A |
| controlUsers | A comma-separated list of user nicks that may send PMs to NaNoBot. An empty list means all users may PM NaNoBot. NaNoBot replies, "Access denied" to unauthorized users. | empty (all users may PM NaNoBot) |
| font_color | The friendly-name of the IRC color code to use when sending messages | BLACK |
| bot_startupMessage | The message to broadcast to the channel when the bot joins the server | Hello, everyone! |
| bot_shutdownMessage | The message to broadcast to the channel when the bot leaves the server | Goodbye, everyone! |
| sprint_startWarningMessage | The message to broadcast to the channel when a sprint is about to start | Sprint in 1 minute |
| sprint_startMessage | The message to broadcast to the channel when a sprint has started | GO! |
| sprint_finishWarningMessage | The message to broadcast to the channel when a sprint is about to end | 1 minute remaining |
| sprint_finishMessage | The message to broadcast to the channel when a sprint has ended | TIME'S UP! |
| scripts | A comma-separated list of Groovy scripts to schedule for execution | N/A |


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

Executing Groovy Scripts
------------------------
NaNoBot can be scheduled to execute arbitrary Groovy scripts, with each script on its own Quartz schedule.

To enable scripts, you must specify the `scripts` property in the properties file with a comma-separated list of strings. Each of these strings corresponds to a property name and a file placed in the `scripts` directory alongside the NaNoBot jar.

```
scripts = hourly.groovy, daily.groovy, weekly.groovy

hourly.groovy = 0 0 * * * ? *
daily.groovy = 0 0 0 * * ? *
weekly.groovy = 0 0 0 * * SUN *
```

The filesystem would look like this:
```
├───NaNoBot.jar
├───NaNoBot.properties
└───scripts/
    ├───daily.groovy
    ├───hourly.groovy
    └───weekly.groovy
```

Writing Groovy Scripts
----------------------
Prior to launching each script, NaNoBot pre-populates numerous variable bindings for the Groovy script engine:
* Each valid keypair in the properties file (the key must be a valid Groovy identifier)
* USERS The list of users currently in the channel
* VERSION The current version of NaNoBot

Upon completion of the script, all variable bindings are persisted to the NaNoBot properties for subsequent runs (either by the same script or another script) except for the following:
* USERS
* MESSAGE
* VERSION
* Any variable beginning with an underscore (_) character

If the script writes to a variable called "MESSAGE" then NaNoBot will broadcast that message to the channel.


Logging
-------
On startup, NaNoBot creates two rolling log files: `channel_messages.log` and `private_messages.log`.

| File | Description | Rotation Strategy |
| ---- | ----------- | ----------------- |
| channel_messages.log | Record of messages sent to the channel (both by NaNoBot and other users). | Daily; 30 days of history |
| private_messages.log | Record of private messages sent to NaNoBot, as well as its replies. | Daily; 5 days of history |


Compiling NaNoBot from source
-----------------------------

Get Apache Maven (http://maven.apache.org/) and run `mvn package` from the root directory of the project.
