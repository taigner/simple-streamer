package com.tobiasaigner.simplestreamer.metadata

import java.util.Date
import java.text.SimpleDateFormat

/**
 * Value object for metadata information of a video file.
 *
 * @author Tobias Aigner
 */
class Metadata {
  var filename: String = _

  var title: String = _
  var resolution: (Int, Int) = _
  var bitrate: Int = _
  var duration: String = _
  var date: String = createDateString

  // always send date of today. used date format e.g. "2011-09-13T19:11:24".
  private def createDateString = {
    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()).replace(" ", "T")
  }
}