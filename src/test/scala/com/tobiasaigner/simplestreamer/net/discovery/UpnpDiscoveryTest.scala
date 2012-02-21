package com.tobiasaigner.simplestreamer.net.discovery

import org.scalatest.{BeforeAndAfter, FunSuite}
import java.net.{InetSocketAddress, DatagramPacket}
import org.scalatest.junit.JUnitRunner
import org.scalatest.mock.MockitoSugar
import org.junit.runner.RunWith
import org.jmock.lib.concurrent.DeterministicScheduler

import org.mockito.Mockito._
import org.mockito.Matchers._
import org.mockito.{Mock, MockitoAnnotations}
import com.tobiasaigner.simplestreamer.configurator.DlnaConfig

/**
 * @author Tobias Aigner
 */
@RunWith(classOf[JUnitRunner])
class UpnpDiscoveryTest extends FunSuite with MockitoSugar with BeforeAndAfter {
  private var discovery: UpnpDiscovery = _
  private var config: DlnaConfig = _

  @Mock
  private val sender: DiscoverySender = null
  @Mock
  private val listener: DiscoveryListener = null

  private val ROOT_DEVICE_MSG = "NOTIFY * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\nNT: upnp:rootdevice\r\nNTS: ssdp:alive\r\nLOCATION: http://127.0.0.1:80/dlna/device/description\r\nUSN: uuid:%s::upnp:rootdevice\r\nCACHE-CONTROL: max-age=1800\r\nSERVER: DLNA, UPnP/1.0, DLNA1.0\r\n\r\n"
  private val USN_MSG = "NOTIFY * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\nNT: uuid:%s\r\nNTS: ssdp:alive\r\nLOCATION: http://127.0.0.1:80/dlna/device/description\r\nUSN: uuid:%s\r\nCACHE-CONTROL: max-age=1800\r\nSERVER: DLNA, UPnP/1.0, DLNA1.0\r\n\r\n"
  private val MEDIA_SERVER_MSG = "NOTIFY * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\nNT: urn:schemas-upnp-org:device:MediaServer:1\r\nNTS: ssdp:alive\r\nLOCATION: http://127.0.0.1:80/dlna/device/description\r\nUSN: uuid:%s::urn:schemas-upnp-org:device:MediaServer:1\r\nCACHE-CONTROL: max-age=1800\r\nSERVER: DLNA, UPnP/1.0, DLNA1.0\r\n\r\n"
  private val CONTENT_DIRECTORY_MSG = "NOTIFY * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\nNT: urn:schemas-upnp-org:service:ContentDirectory:1\r\nNTS: ssdp:alive\r\nLOCATION: http://127.0.0.1:80/dlna/device/description\r\nUSN: uuid:%s::urn:schemas-upnp-org:service:ContentDirectory:1\r\nCACHE-CONTROL: max-age=1800\r\nSERVER: DLNA, UPnP/1.0, DLNA1.0\r\n\r\n"
  private val CONNECTION_MANAGER_MSG = "NOTIFY * HTTP/1.1\r\nHOST: 239.255.255.250:1900\r\nNT: urn:schemas-upnp-org:service:ConnectionManager:1\r\nNTS: ssdp:alive\r\nLOCATION: http://127.0.0.1:80/dlna/device/description\r\nUSN: uuid:%s::urn:schemas-upnp-org:service:ConnectionManager:1\r\nCACHE-CONTROL: max-age=1800\r\nSERVER: DLNA, UPnP/1.0, DLNA1.0\r\n\r\n"

  before {
    MockitoAnnotations.initMocks(this)

    config = new DlnaConfig() {
      translatedHttpServerIP = "127.0.0.1";
      httpServerIP = "127.0.0.1";
      httpServerPort = 80;
      discoverySenderHost = "239.255.255.250";
      discoverySenderPort = 1900;
    }

    discovery = new UpnpDiscovery(config, sender, listener)
  }

  test("sends discovery messages") {
    val expected = List(ROOT_DEVICE_MSG format config.uuid,
      USN_MSG format(config.uuid, config.uuid),
      MEDIA_SERVER_MSG format config.uuid,
      CONTENT_DIRECTORY_MSG format config.uuid,
      CONNECTION_MANAGER_MSG format config.uuid)

    val executor = new DeterministicScheduler()

    discovery.announce(executor)

    executor.runNextPendingCommand()

    verify(sender).sendMulticast(expected, config.discoverySenderHost, config.discoverySenderPort)
  }

  test("starts listening") {
    discovery.startListening()
    discovery.stopListening()

    verify(listener).listen(anyObject())
    verify(listener).stop()
  }

  test("receives discovery messages") {
    discovery.startListening()

    verify(listener).listen(anyObject())
  }

  test("responds to received root device multicast messages") {
    val expected = "NOTIFY * HTTP/1.1\r\n" +
      "HOST: 239.255.255.250:1900\r\n" +
      "NT: upnp:rootdevice\r\n" +
      "NTS: ssdp:alive\r\n" +
      "LOCATION: http://127.0.0.1:80/dlna/device/description\r\n" +
      "USN: uuid:%s::upnp:rootdevice\r\n" +
      "CACHE-CONTROL: max-age=1800\r\n" +
      "SERVER: DLNA, UPnP/1.0, DLNA1.0\r\n\r\n"

    val data = "M-SEARCH\r\nupnp:rootdevice"
    val packet = new DatagramPacket(data.getBytes, 0, data.length)
    packet.setSocketAddress(new InetSocketAddress("239.255.255.250", 1900))

    discovery.discoveryMessageReceived(packet)

    verify(sender).sendMulticast(org.mockito.Matchers.eq(List(expected format config.uuid)), any(), any())
  }

  test("responds to received multimedia server multicast messages") {
    val expected = "NOTIFY * HTTP/1.1\r\n" +
      "HOST: 239.255.255.250:1900\r\n" +
      "NT: urn:schemas-upnp-org:device:MediaServer:1\r\n" +
      "NTS: ssdp:alive\r\n" +
      "LOCATION: http://127.0.0.1:80/dlna/device/description\r\n" +
      "USN: uuid:%s::urn:schemas-upnp-org:device:MediaServer:1\r\n" +
      "CACHE-CONTROL: max-age=1800\r\n" +
      "SERVER: DLNA, UPnP/1.0, DLNA1.0\r\n\r\n"

    val data = "M-SEARCH\r\nurn:schemas-upnp-org:device:MediaServer:1"
    val packet = new DatagramPacket(data.getBytes, 0, data.length)
    packet.setSocketAddress(new InetSocketAddress("239.255.255.250", 1900))

    discovery.discoveryMessageReceived(packet)

    verify(sender).sendMulticast(org.mockito.Matchers.eq(List(expected format config.uuid)), any(), any())
  }

  test("responds to received content directory multicast messages") {
    val expected = "NOTIFY * HTTP/1.1\r\n" +
      "HOST: 239.255.255.250:1900\r\n" +
      "NT: urn:schemas-upnp-org:service:ContentDirectory:1\r\n" +
      "NTS: ssdp:alive\r\n" +
      "LOCATION: http://127.0.0.1:80/dlna/device/description\r\n" +
      "USN: uuid:%s::urn:schemas-upnp-org:service:ContentDirectory:1\r\n" +
      "CACHE-CONTROL: max-age=1800\r\n" +
      "SERVER: DLNA, UPnP/1.0, DLNA1.0\r\n\r\n"

    val data = "M-SEARCH\r\nurn:schemas-upnp-org:service:ContentDirectory:1"
    val packet = new DatagramPacket(data.getBytes, 0, data.length)
    packet.setSocketAddress(new InetSocketAddress("239.255.255.250", 1900))

    discovery.discoveryMessageReceived(packet)

    verify(sender).sendMulticast(org.mockito.Matchers.eq(List(expected format config.uuid)), any(), any())
  }

  test("sends bye messages") {
    val expected = List(createByeRootDevice(), createByeMediaServer(), createByeContentDirectory(), createByeConnectionManager())

    discovery.byebye()

    verify(sender).sendMulticast(expected, config.discoverySenderHost, config.discoverySenderPort)
  }


  private def createByeRootDevice() = "NOTIFY * HTTP/1.1\r\n" +
    createHost("239.255.255.250:1900") +
    "NT: upnp:rootdevice\r\n" +
    "NTS: ssdp:byebye\r\n" +
    createUsn(config.uuid, "::upnp:rootdevice")

  private def createByeMediaServer() = "NOTIFY * HTTP/1.1\r\n" +
    createHost("239.255.255.250:1900") +
    "NT: urn:schemas-upnp-org:device:MediaServer:1\r\n" +
    "NTS: ssdp:byebye\r\n" +
    createUsn(config.uuid, "::urn:schemas-upnp-org:device:MediaServer:1")

  private def createByeContentDirectory() = "NOTIFY * HTTP/1.1\r\n" +
    createHost("239.255.255.250:1900") +
    "NT: urn:schemas-upnp-org:service:ContentDirectory:1\r\n" +
    "NTS: ssdp:byebye\r\n" +
    createUsn(config.uuid, "::urn:schemas-upnp-org:service:ContentDirectory:1")

  private def createByeConnectionManager() = "NOTIFY * HTTP/1.1\r\n" +
    createHost("239.255.255.250:1900") +
    "NT: urn:schemas-upnp-org:service:ConnectionManager:1\r\n" +
    "NTS: ssdp:byebye\r\n" +
    createUsn(config.uuid, "::urn:schemas-upnp-org:service:ConnectionManager:1")

  private def createHost(sender: String) = "HOST: " + sender + "\r\n"

  private def createUsn(uuid: String, urn: String) = "USN: uuid:" + uuid + urn + "\r\n\r\n"
}