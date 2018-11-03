/**
 * dropbox.groovy
 *
 * Synchronizes the channel_messages.log file with Dropbox and pushes the wordcount to the NaNoWriMo Wordcount API.
 *
 * First-time setup
 *     Go to https://www.dropbox.com/developers/apps/ and create a new App, then generate an access token for it.
 *     Go to http://nanowrimo.org/api/wordcount and generate a secret key.
 *     Install cURL: http://curl.haxx.se/
 *
 * Input Properties (from NaNoBot.properties)
 * dropbox_accessToken
 *     An OAuth access token used by the app for interacting with the remote file share
 * dropbox_remoteFile
 *     The path to the remote file to synchronize with, e.g. "/NaNoBot/channel_messages.log"
 * nanowrimo_username
 *     This is the NaNoWriMo account to update
 * nanowrimo_secret
 *     This is the API key generated for the corresponding user
 * nanowrimo_host
 *     This is the hostname of the site to publish data to (e.g. 'http://nanowrimo.org' or 'http://campnanowrimo.org')
 *
 * Output Properties
 * dropbox_linecount
 *     The total number of lines written to the file
 * dropbox_wordcount
 *     The total number of words written to the file
 */

@Grab(group='com.dropbox.core', module='dropbox-core-sdk', version='3.0.10')
@Grab(group='commons-codec', module='commons-codec', version='1.11')

import org.apache.commons.codec.digest.DigestUtils
import com.dropbox.core.*
import com.dropbox.core.v2.*
import com.dropbox.core.v2.files.*
import java.io.*
import java.util.Locale

// Set up the Dropbox client
_clientIdentifier = 'NaNoBot/' + VERSION + ' dropbox.groovy/2.0'
DbxRequestConfig _config = new DbxRequestConfig(_clientIdentifier, Locale.getDefault().toString())
DbxClientV2 _client = new DbxClientV2(_config, dropbox_accessToken)

// Pull down the remote file
_tempFileName = dropbox_remoteFile.tokenize('/').last() + '.tmp'
FileOutputStream _outputStream = new FileOutputStream(_tempFileName)
try {
  FileMetadata _downloadedFile = _client.files().downloadBuilder(dropbox_remoteFile).download(_outputStream)
  println("Downloaded file: ${_downloadedFile}")
} catch (e) {
  printn(e.message)
} finally {
  _outputStream.close()
}

// Find the last timestamp in the remote file
_tempFile = new File(_tempFileName)
_lastTimestamp = ''
_foundTimestamp = false
try {
  _lastTimestamp = _tempFile.readLines().last()[0..'2015-01-01 00:00:00,000'.size() -1]
} catch(e) {
  // If the tempfile is empty then we need to append every log line
  _foundTimestamp = true
}
println("Searching for entry: '${_lastTimestamp}'")

// Add any new entries in the message logs to the file
_files = []
new File('.').eachFileMatch(~/channel_messages.*/) { _files << it }
_files.sort()
_files.each { file->
  println("Found file: ${file.name}")

  file.eachLine { line->
    if (_foundTimestamp) {
      _tempFile.append(line + '\n')
    }

    if (line.startsWith(_lastTimestamp + ' - ')) {
      _foundTimestamp = true
    }
  }
}

// The log files on disk don't have the last entry in the Dropbox file (The app was probably moved to another computer)
if (!_foundTimestamp) {
  _files.each { file->
    file.eachLine { line->
      _tempFile.append(line + '\n')
    }
  }
}

// Upload the new version of the file (or create it if it's missing)
FileInputStream _inputStream = new FileInputStream(_tempFile)
try {
  FileMetadata _uploadedFile = _client.files().uploadBuilder(dropbox_remoteFile).withMode(WriteMode.OVERWRITE).uploadAndFinish(_inputStream)
  println("Uploaded: ${_uploadedFile}")
} finally {
  _inputStream.close()
}

// Compute the document's metadata
dropbox_linecount = 0
dropbox_wordcount = 0
for (line in _tempFile.readLines()) {
  dropbox_linecount++
  dropbox_wordcount += line.substring(line.indexOf(' - ') + 2).trim().split('\\s++').size()
}

println("dropbox_linecount: ${dropbox_linecount}")
println("dropbox_wordcount: ${dropbox_wordcount}")

// Perform cleanup
_tempFile.delete()

// Publish the wordcount to the Wordcount API
_hash = DigestUtils.sha1Hex(nanowrimo_secret + nanowrimo_username + dropbox_wordcount)
_body = 'hash=' + _hash + '&name=' + nanowrimo_username + '&wordcount=' + dropbox_wordcount
_url = nanowrimo_host + '/api/wordcount'
_cmd = ['curl', '-X', 'PUT', '--data', _body, _url]

println(_cmd.join(' '))
_response = _cmd.execute().text
println(_response)
