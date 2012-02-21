package com.tobiasaigner.simplestreamer.net.discovery

import java.net.{DatagramPacket, InetAddress, MulticastSocket}
import com.tobiasaigner.simplestreamer.configurator.DlnaConfig

/**
 * @author Tobias Aigner
 */
class UdpSender(private val config: DlnaConfig) extends DiscoverySender {
  private val UPNP_HOST = InetAddress.getByName(config.discoverySenderHost)
  private val SOCKET_TIME_TO_LIVE = 32
  private val SOCKET_TIMEOUT = 250

  /**
   * Sends multicast messages to specified host.
   *
   * @param messages List of messages to be sent.
   * @param address Receiving host.
   * @param port Receiving port.
   */
  override def sendMulticast(messages: List[String], address: String, port: Int) {
    val socket = createSocket(port)
    messages.foreach {
      msg => socket.send(new DatagramPacket(msg.getBytes, msg.length, InetAddress.getByName(address), port))
    }
    socket.close()
  }

  protected def createSocket(port: Int): MulticastSocket = {
    val socket = new MulticastSocket(port)

    socket.joinGroup(UPNP_HOST)
    socket.setTimeToLive(SOCKET_TIME_TO_LIVE)
    socket.setReuseAddress(true)
    socket.setSoTimeout(SOCKET_TIMEOUT)

    socket
  }
}