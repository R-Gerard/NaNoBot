/**
 * hello.groovy
 *
 * Sends the message "Hello, World!" to the channel.
 *
 * Input Properties (from NaNoBot.properties)
 * user_login
 *     The app's nickname in the channel
 *
 */
_randomUser = USERS.get(new Random().nextInt() % USERS.size())

if (_randomUser.equals(user_login)) {
  _randomUser = 'World'
}

// Be careful of org.codehaus.groovy.runtime.GStringImpl cannot be cast to java.lang.String
MESSAGE = 'Hello, ' + _randomUser + '!'
