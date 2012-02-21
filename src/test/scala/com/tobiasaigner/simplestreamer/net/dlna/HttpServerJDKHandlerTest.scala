package com.tobiasaigner.simplestreamer.net.dlna

import org.scalatest.{BeforeAndAfter, FunSuite}
import java.net.URI
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.mockito.ArgumentCaptor
import org.junit.Assert._
import com.sun.net.httpserver.{Headers, HttpExchange}
import xml.{Elem, XML}
import java.io.{ByteArrayInputStream, OutputStream}
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.tobiasaigner.simplestreamer.configurator.DlnaConfig
import com.tobiasaigner.simplestreamer.metadata.Metadata

/**
 * @author Tobias Aigner
 */
@RunWith(classOf[JUnitRunner])
class HttpServerJDKHandlerTest extends FunSuite with BeforeAndAfter with StreamHelper {
  private var handler: HttpServerJDKHandler = _
  private var config: DlnaConfig = _
  private var metadata: Metadata = _

  before {
    config = new DlnaConfig() {
      serverDisplayName = "Scala DLNA"
    }
    metadata = new Metadata()
    handler = new HttpServerJDKHandler(config, metadata)
  }

  test("handles description fetch request") {
    val (exchange, _, outputStream) = setupExchange("GET", "/dlna/device/description")

    val captor = ArgumentCaptor.forClass(classOf[Array[Byte]])

    handler.handle(exchange)

    verify(outputStream).write(captor.capture())

    val responseXml = createXmlFromCaptor(captor)

    assertEquals("urn:schemas-upnp-org:device:MediaServer:1", (responseXml \\ "deviceType").text)
    assertEquals(config.serverDisplayName, (responseXml \\ "friendlyName").text)
    assertEquals("uuid:" + config.uuid, (responseXml \\ "UDN").text)
  }

  test("handles fetch content directory") {
    val (exchange, _, outputStream) = setupExchange("GET", "/dlna/device/contentdirectory")

    handler.handle(exchange)

    verify(outputStream).write(anyObject[Array[Byte]]())
  }

  test("handles fetch connection manager") {
    val (exchange, _, outputStream) = setupExchange("GET", "/dlna/device/connectionmanager")

    handler.handle(exchange)

    verify(outputStream).write(anyObject[Array[Byte]]())
  }

  test("handles POST content directory") {
    val requestXml =
      <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
        <s:Body>
        </s:Body>
      </s:Envelope>

    val (exchange, _, outputStream) = setupExchange("POST", "/dlna/control/contentdirectory", Map("SOAPACTION" -> "urn:schemas-upnp-org:service:ContentDirectory:1#Browse"), Some(requestXml))
    val captor = ArgumentCaptor.forClass(classOf[Array[Byte]])

    handler.handle(exchange)

    verify(outputStream).write(captor.capture())

    val responseXml = createXmlFromCaptor(captor)

    assertFalse((responseXml \\ "GetSortCapabilitiesResponse").isEmpty)
  }

  test("handles directory root folder browsing") {
    val requestXml =
      <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
        <s:Body>
          <u:Browse xmlns:u="urn:schemas-upnp-org:service:ContentDirectory:1">
            <ObjectID>0</ObjectID>
            <BrowseFlag>BrowseDirectChildren</BrowseFlag>
            <Filter>
              dc:title,av:mediaClass,dc:date,@childCount,res,upnp:class,res@resolution,upnp:album,upnp:genre,upnp:albumArtURI,upnp:albumArtURI@dlna:profileID,dc:creator,res@size,res@duration,res@bitrate,res@protocolInfo
            </Filter>
            <StartingIndex>0</StartingIndex>
            <RequestedCount>10</RequestedCount>
            <SortCriteria></SortCriteria>
          </u:Browse>
        </s:Body>
      </s:Envelope>

    val (exchange, _, outputStream) = setupExchange("POST", "/dlna/control/contentdirectory",
      Map("SOAPACTION" -> "urn:schemas-upnp-org:service:ContentDirectory:1#Browse"), Some(requestXml))

    val captor = ArgumentCaptor.forClass(classOf[Array[Byte]])

    handler.handle(exchange)

    verify(outputStream).write(captor.capture())

    val responseXml = createXmlFromCaptor(captor)

    assertTrue((responseXml \\ "Result").text.contains("<dc:title>Content</dc:title>"))
  }

  test("handles POST connection manager") {
    val (exchange, _, outputStream) = setupExchange("POST", "/dlna/control/connectionmanager")
    val captor = ArgumentCaptor.forClass(classOf[Array[Byte]])

    handler.handle(exchange)

    verify(outputStream).write(captor.capture())

    val responseXml = createXmlFromCaptor(captor)
    assertTrue((responseXml \\ "Source").text.startsWith("http-get:*"))
  }

  test("handles fetch icon") {
    val (exchange, _, outputStream) = setupExchange("GET", "/dlna/images/icon_server.png")
    config.imageUrl = "icons/tv.png"

    handler.handle(exchange)

    verify(outputStream).write(anyObject[Array[Byte]]())
  }

  test("handles fetch stream info") {
    val (exchange, headers, _) = setupExchange("HEAD", "stream")

    handler.handle(exchange)

    verify(headers).set(org.mockito.Matchers.eq("contentFeatures.dlna.org"), anyString())
  }

  private def setupExchange(method: String, uri: String, headersMap: Map[String, String] = Map(), requestXml: Option[Elem] = None): (HttpExchange, Headers, OutputStream) = {
    val exchange: HttpExchange = mock(classOf[HttpExchange])
    val responseHeaders: Headers = mock(classOf[Headers])
    val outputStream: OutputStream = mock(classOf[OutputStream])

    val requestHeaders = new Headers

    headersMap.foreach {
      case (key, value) => requestHeaders.set(key, value)
    }

    when(exchange.getRequestHeaders).thenReturn(requestHeaders)
    when(exchange.getRequestMethod).thenReturn(method)
    when(exchange.getRequestURI).thenReturn(new URI(uri))

    if (requestXml.isDefined) {
      val requestXmlString = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" + requestXml.get.toString()
      when(exchange.getRequestBody).thenReturn(new ByteArrayInputStream(requestXmlString.getBytes))
    }

    when(exchange.getResponseHeaders).thenReturn(responseHeaders)
    when(exchange.getResponseBody).thenReturn(outputStream)

    (exchange, responseHeaders, outputStream)
  }

  private def createXmlFromCaptor(captor: ArgumentCaptor[Array[Byte]]) = {
    XML.loadString(new String(captor.getValue))
  }
}