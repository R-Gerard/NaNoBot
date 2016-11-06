/* giphy.groovy
 *
 * Posts a random image from giphy.com.
 *
 * First-time setup
 *     Install cURL: http://curl.haxx.se/
 *
 * Input Properties (from NaNoBot.properties)
 * giphy_apikiey
 *     An API key used by the app for interacting with Giphy
 * giphy_tag
 *     The image tag to filter random results with
 */

import groovy.json.*

_url = 'http://api.giphy.com/v1/gifs/random?api_key=' + giphy_apikey + '&tag=' + giphy_tag
_cmd = ['curl', _url]

println(_cmd.join(' '))
_response = _cmd.execute().text
println(_response)

_parser = new JsonSlurper()

MESSAGE = _parser.parseText(_response)['data']['image_url']

println(MESSAGE)
