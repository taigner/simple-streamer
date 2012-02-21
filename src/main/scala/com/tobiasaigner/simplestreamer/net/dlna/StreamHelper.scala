package com.tobiasaigner.simplestreamer.net.dlna

import java.io.InputStream
import collection.mutable.ListBuffer

/**
 * @author Tobias Aigner
 */
trait StreamHelper {
  def inputStreamToByteArray(is: InputStream): Array[Byte] = {
    val buffer = ListBuffer[Byte]()
    var byte = is.read()
    while (byte != -1) {
      buffer.append(byte.byteValue)
      byte = is.read()
    }
    buffer.toArray
  }
}