package com.tobiasaigner.simplestreamer.net.discovery.messages

import com.tobiasaigner.simplestreamer.configurator.DlnaConfig

/**
 * @author Tobias Aigner
 */
object DiscoveryMessages {
  private val LF = "\r\n"

  private val HEADER = "NOTIFY * HTTP/1.1" + LF
  private val CACHE_CONTROL = "CACHE-CONTROL: max-age=1800" + LF
  private val DESCRIPTION_RESOURCE = "/dlna/device/description"
  private val ALIVE = "NTS: ssdp:alive" + LF
  private val SERVER = "SERVER: DLNA, UPnP/1.0, DLNA1.0" + LF + LF
  private val BYE_BYE = "NTS: ssdp:byebye" + LF

  def rootDeviceMsg(config: DlnaConfig) =
    HEADER +
      createHost(config) +
      "NT: upnp:rootdevice" + LF +
      ALIVE +
      createLocation(config) +
      "USN: uuid:" + config.uuid + "::upnp:rootdevice" + LF +
      CACHE_CONTROL + SERVER

  def usnMsg(config: DlnaConfig) =
    HEADER +
      createHost(config) +
      "NT: uuid:" + config.uuid + LF +
      ALIVE +
      createLocation(config) +
      createUsn(config, "") +
      CACHE_CONTROL + SERVER

  def mediaServerMsg(config: DlnaConfig) =
    HEADER +
      createHost(config) +
      "NT: urn:schemas-upnp-org:device:MediaServer:1" + LF +
      ALIVE +
      createLocation(config) +
      createUsn(config, "::urn:schemas-upnp-org:device:MediaServer:1") +
      CACHE_CONTROL + SERVER

  def contentDirectoryMsg(config: DlnaConfig) =
    HEADER +
      createHost(config) +
      "NT: urn:schemas-upnp-org:service:ContentDirectory:1" + LF +
      ALIVE +
      createLocation(config) +
      createUsn(config, "::urn:schemas-upnp-org:service:ContentDirectory:1") +
      CACHE_CONTROL + SERVER

  def connectionManagerMsg(config: DlnaConfig) =
    HEADER +
      createHost(config) +
      "NT: urn:schemas-upnp-org:service:ConnectionManager:1" + LF +
      ALIVE +
      createLocation(config) +
      createUsn(config, "::urn:schemas-upnp-org:service:ConnectionManager:1") +
      CACHE_CONTROL + SERVER

  def byeRootDevice(config: DlnaConfig) =
    HEADER +
      createHost(config) +
      "NT: upnp:rootdevice" + LF +
      BYE_BYE +
      createUsn(config, "::upnp:rootdevice") + LF

  def byeMediaServer(config: DlnaConfig) =
    HEADER +
      createHost(config) +
      "NT: urn:schemas-upnp-org:device:MediaServer:1" + LF +
      BYE_BYE +
      createUsn(config, "::urn:schemas-upnp-org:device:MediaServer:1") + LF

  def byeContentDirectory(config: DlnaConfig) =
    HEADER +
      createHost(config) +
      "NT: urn:schemas-upnp-org:service:ContentDirectory:1" + LF +
      BYE_BYE +
      createUsn(config, "::urn:schemas-upnp-org:service:ContentDirectory:1") + LF

  def byeConnectionManager(config: DlnaConfig) =
    HEADER +
      createHost(config) +
      "NT: urn:schemas-upnp-org:service:ConnectionManager:1" + LF +
      BYE_BYE +
      createUsn(config, "::urn:schemas-upnp-org:service:ConnectionManager:1") + LF

  private def createHost(config: DlnaConfig) = "HOST: " + config.discoverySenderHost + ":" + config.discoverySenderPort + LF

  private def createLocation(config: DlnaConfig) = "LOCATION: http://" + config.httpServerIP + ":" + config.httpServerPort + DESCRIPTION_RESOURCE + LF

  private def createUsn(config: DlnaConfig, urn: String) = "USN: uuid:" + config.uuid + urn + LF
}