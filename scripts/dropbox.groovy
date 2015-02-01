/**
 * dropbox.groovy
 *
 * Synchronizes the channel_messages.log file with Dropbox.
 *
 * First-time setup
 *     Go to https://www.dropbox.com/developers/apps/ and create a new App, then generate an access token for it.
 *
 * Input Properties (from NaNoBot.properties)
 * dropbox_accessToken
 *     An OAuth access token used by the app for interacting with the remote file share
 * dropbox_remoteFile
 *     The path to the remote file to synchronize with, e.g. "/NaNoBot/channel_messages.log"
 *
 * Output Properties
 * dropbox_linecount
 *     The total number of lines written to the file
 * dropbox_wordcount
 *     The total number of words written to the file
 */

@Grab(group='com.dropbox.core', module='dropbox-core-sdk', version='1.7.7')

import com.dropbox.core.*
import java.io.*
import java.util.Locale

// Set up the Dropbox client
//println("dropbox_accessToken: ${dropbox_accessToken}")
//println("dropbox_remoteFile: ${dropbox_remoteFile}")

_clientIdentifier = 'NaNoBot/' + VERSION + ' dropbox.groovy/1.0'
DbxRequestConfig _config = new DbxRequestConfig(_clientIdentifier, Locale.getDefault().toString())
DbxClient _client = new DbxClient(_config, dropbox_accessToken)

// Pull down the remote file
_tempFileName = dropbox_remoteFile.tokenize('/').last() + '.tmp'
FileOutputStream _outputStream = new FileOutputStream(_tempFileName)
try {
  DbxEntry.File _downloadedFile = _client.getFile(dropbox_remoteFile, null, _outputStream)
  println("Downloaded file: ${_downloadedFile}")
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
new File('.').eachFileMatch(~/channel_messages.*/) { file->
  println("Found file: ${file.name}")

  file.eachLine { line->
    if (_foundTimestamp) {
      _tempFile.append(line + '\n')
      //println("Appended: ${line}")
    }

    if (line.startsWith(_lastTimestamp + ' - ')) {
      _foundTimestamp = true
    }
  }
}

// The log files on disk don't have the last entry in the Dropbox file (The app was probably moved to another computer)
if (!_foundTimestamp) {
  new File('.').eachFileMatch(~/channel_messages.*/) { file->
    file.eachLine { line->
      _tempFile.append(line + '\n')
      //println("Appended: ${line}")
    }
  }
}

// Upload the new version of the file (or create it if it's missing)
FileInputStream _inputStream = new FileInputStream(_tempFile)
try {
  DbxEntry.File _uploadedFile = _client.uploadFile(dropbox_remoteFile, DbxWriteMode.update(), _tempFile.length(), _inputStream)
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
