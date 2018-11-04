giphy_apikey = 'dc6zaTOxFJmzC'
giphy_tag = 'cat'

/**
 * giphy.groovy
 *
 * Posts a random image from giphy.com.
 *
 * Input Properties (from NaNoBot.properties)
 * giphy_apikiey
 *     An API key used by the app for interacting with Giphy
 * giphy_tag
 *     The image tag to filter random results with
 */

@Grab(group='com.mashape.unirest', module='unirest-java', version='1.4.9')

import com.mashape.unirest.http.*
import groovy.json.*

_url = 'https://api.giphy.com/v1/gifs/random'

// Print the cURL call for troubleshooting purposes
println("curl '${_url}?api_key=${giphy_apikey}&tag=${giphy_tag}'")

_response = Unirest.get(_url)
                   .queryString('api_key', giphy_apikey)
                   .queryString('tag', giphy_tag)
                   .asString();

_parser = new JsonSlurper()

MESSAGE = _parser.parseText(_response.body)['data']['image_url']

println(MESSAGE)
