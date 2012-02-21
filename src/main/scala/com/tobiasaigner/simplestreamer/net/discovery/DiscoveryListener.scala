package com.tobiasaigner.simplestreamer.net.discovery

import java.net.DatagramPacket

/**
 * @author Tobias Aigner
 */
trait DiscoveryListener {
  def listen(callback: DatagramPacket => Unit)
  def stop()
}