package com.tobiasaigner.simplestreamer.net.discovery

/**
 * @author Tobias Aigner
 */
trait DiscoverySender {
  def sendMulticast(messages: List[String], address: String, port: Int)
}

