package com.tobiasaigner.simplestreamer.net.discovery

import java.util.concurrent.{ScheduledExecutorService, TimeUnit}
import java.net.DatagramPacket
import org.slf4j.LoggerFactory
import com.tobiasaigner.simplestreamer.net.discovery.messages._
import com.tobiasaigner.simplestreamer.configurator.DlnaConfig

/**
 * Class manages the UPNP discovery process. For that it is built on
 * the DiscoverySender and DiscoveryListener.
 *
 * @author Tobias Aigner
 */
class UpnpDiscovery(private val config: DlnaConfig,
                    private val sender: DiscoverySender,
                    private val listener: DiscoveryListener) {
  private val SENDING_INTERVAL = 10
  private val M_SEARCH = "M-SEARCH"
  private val CONTENT_DIRECTORY = "urn:schemas-upnp-org:service:ContentDirectory:1"
  private val ROOT_DEVICE = "upnp:rootdevice"
  private val MEDIA_SERVER = "urn:schemas-upnp-org:device:MediaServer:"

  private val logger = LoggerFactory.getLogger(classOf[UpnpDiscovery])

  /**
   * Announce device at a fixed interval.
   *
   * @param executor Utilized scheduled executor for sending.
   */
  def announce(executor: ScheduledExecutorService) {
    val messages = List(DiscoveryMessages.rootDeviceMsg(config),
      DiscoveryMessages.usnMsg(config),
      DiscoveryMessages.mediaServerMsg(config),
      DiscoveryMessages.contentDirectoryMsg(config),
      DiscoveryMessages.connectionManagerMsg(config))

    val sendingTask = new Runnable {
      override def run() {
        sender.sendMulticast(messages, config.discoverySenderHost, config.discoverySenderPort)
      }
    }
    executor.scheduleAtFixedRate(sendingTask, 0, SENDING_INTERVAL, TimeUnit.SECONDS)
  }

  /**
   * Send byebye messages for announcing device shutdown. Should be called on JVM shutdown.
   */
  def byebye() {
    val messages = List(DiscoveryMessages.byeRootDevice(config),
      DiscoveryMessages.byeMediaServer(config),
      DiscoveryMessages.byeContentDirectory(config),
      DiscoveryMessages.byeConnectionManager(config))

    logger.info("Sending byebye messages")

    sender.sendMulticast(messages, config.discoverySenderHost, config.discoverySenderPort)
  }

  /**
   * Start listening for discovery messages.
   */
  def startListening() {
    listener.listen(discoveryMessageReceived)
  }

  def stopListening() {
    listener.stop()
  }

  /**
   * Interpret received discovery messages and invoke appropriate action.
   *
   * @param packet Packet that was received.
   */
  def discoveryMessageReceived(packet: DatagramPacket) {
    val message: String = new String(packet.getData)

    if (message.startsWith(M_SEARCH)) {
      val remoteAddress = extractAddress(packet)
      val remotePort = extractPort(packet)

      if (message.contains(CONTENT_DIRECTORY)) {
        sender.sendMulticast(List(DiscoveryMessages.contentDirectoryMsg(config)), remoteAddress, remotePort)
      } else if (message.contains(ROOT_DEVICE)) {
        sender.sendMulticast(List(DiscoveryMessages.rootDeviceMsg(config)), remoteAddress, remotePort)
      } else if (message.contains(MEDIA_SERVER)) {
        sender.sendMulticast(List(DiscoveryMessages.mediaServerMsg(config)), remoteAddress, remotePort)
      }
    }
  }

  private def extractAddress(packet: DatagramPacket): String = packet.getAddress.getHostAddress

  private def extractPort(packet: DatagramPacket): Int = packet.getPort
}
