package com.tobiasaigner.simplestreamer.configurator

import java.util.UUID.nameUUIDFromBytes
import java.util.UUID.randomUUID
import java.net.{NetworkInterface, InetAddress}

/**
 * Basically a container for required configuration data.
 *
 * @author Tobias Aigner
 */
class DlnaConfig {
  var serverDisplayName: String = _
  var imageUrl: String = _
  var folderImageUrl: String = _
  var fileImageUrl: String = _
  var discoverySenderHost: String = _
  var discoverySenderPort: Int = _
  protected var translatedHttpServerIP: String = _
  var httpServerPort: Int = _
  var ffmpegOptions: String = _

  val uuid: String = generateUuid()

  def httpServerIP_=(ip: String) {
    if (ip.equals("auto")) {
      translatedHttpServerIP = determineLocalIp()
    }
  }

  def httpServerIP = translatedHttpServerIP

  private def generateUuid(): String = {
    val localhost = InetAddress.getLocalHost
    val networkInterface = NetworkInterface.getByInetAddress(localhost)
    if (networkInterface != null) {
      val mac = networkInterface.getHardwareAddress
      if (mac != null) {
        return nameUUIDFromBytes(mac).toString
      }
      return randomUUID().toString
    }
    randomUUID().toString
  }

  private def determineLocalIp() = InetAddress.getLocalHost.getHostAddress

  override def equals(other: Any): Boolean = other match {
    case that: DlnaConfig =>
      (that canEqual this) &&
        (this.serverDisplayName == that.serverDisplayName) && (this.imageUrl == that.imageUrl) &&
        (this.discoverySenderHost == that.discoverySenderHost) && (this.discoverySenderPort == that.discoverySenderPort) &&
        (this.httpServerIP == that.httpServerIP) && (this.httpServerPort == that.httpServerPort) &&
        (this.ffmpegOptions == that.ffmpegOptions) &&
        (this.uuid == that.uuid)
  }

  def canEqual(other: Any) = other.isInstanceOf[DlnaConfig]
}