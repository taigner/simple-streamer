package com.tobiasaigner.simplestreamer.net.dlna

import com.sun.net.httpserver.{Headers, HttpExchange, HttpHandler}
import org.slf4j.LoggerFactory
import xml.XML
import com.tobiasaigner.simplestreamer.net.dlna.response._
import com.tobiasaigner.simplestreamer.configurator.DlnaConfig
import com.tobiasaigner.simplestreamer.metadata.Metadata
import com.tobiasaigner.simplestreamer.streaming.FfmpegTranscodingStreamer

/**
 * @author Tobias Aigner
 */
class HttpServerJDKHandler(private val config: DlnaConfig, private val metadata: Metadata) extends HttpHandler with StreamHelper with ResponseBuilder {
  private val logger = LoggerFactory.getLogger(classOf[HttpServerJDKHandler])

  private val ACCEPT_RANGES = ("Accept-Ranges" -> "bytes")
  private val NO_CACHE = ("Cache-Control" -> "no-cache")

  private val RESPONSE_HEADERS = Map("Content-Type" -> "text/xml; charset=\"utf-8\"",
    NO_CACHE,
    "Expires" -> "0",
    ACCEPT_RANGES,
    "Connection" -> "keep-alive")

  private def setupGETResponseHeaders(headers: Headers) {
    RESPONSE_HEADERS.foreach(header => headers.set(header._1, header._2))
  }

  private def setupImageResponseHeaders(headers: Headers) {
    headers.set("Content-Type", "image/png")
    headers.set(ACCEPT_RANGES._1, ACCEPT_RANGES._2)
  }

  private def setContentType(headers: Headers) {
    headers.set("Content-Type", RESPONSE_HEADERS.get("Content-Type").get)
  }

  private def responseDeviceDescription(exchange: HttpExchange) {
    val headers = exchange.getResponseHeaders
    setupGETResponseHeaders(headers)

    val responseBody = exchange.getResponseBody
    val response = buildResponse(Description.xml(config))
    exchange.sendResponseHeaders(200, response.getBytes.length)
    responseBody.write(response.getBytes)
    responseBody.close()
  }

  private def responseIcon(exchange: HttpExchange, imageUrl: String) {
    val headers = exchange.getResponseHeaders
    setupImageResponseHeaders(headers)

    val responseBody = exchange.getResponseBody

    var data: Array[Byte] = inputStreamToByteArray(ClassLoader.getSystemResourceAsStream(imageUrl))
    exchange.sendResponseHeaders(200, data.length)
    responseBody.write(data)
    responseBody.close()
  }

  private def responseContentDirectory(exchange: HttpExchange) {
    val headers = exchange.getResponseHeaders
    setupGETResponseHeaders(headers)

    val responseBody = exchange.getResponseBody
    val body = buildResponse(ContentDirectory.xml)
    exchange.sendResponseHeaders(200, body.getBytes.length)
    responseBody.write(body.getBytes)
    responseBody.close()
  }

  private def responseConnectionManager(exchange: HttpExchange) {
    val headers = exchange.getResponseHeaders
    setupGETResponseHeaders(headers)

    val responseBody = exchange.getResponseBody
    val body = buildResponse(ConnectionManager.xml)
    exchange.sendResponseHeaders(200, body.getBytes.length)
    responseBody.write(body.getBytes)
    responseBody.close()
  }

  private def responsePostContentDirectory(exchange: HttpExchange) {
    val responseHeaders = exchange.getResponseHeaders
    val responseBody = exchange.getResponseBody
    var responseBodyData: String = ""
    setContentType(responseHeaders)

    // see http://libdlna.sourcearchive.com/documentation/0.2.3/dlna_8h-source.html
    val requestXml = XML.load(exchange.getRequestBody)
    // do we have a browse request?
    if (!(requestXml \\ "BrowseFlag").isEmpty) {
      // is this a browse directory request?
      if ("0/0".equalsIgnoreCase((requestXml \\ "ObjectID").text)) {
        responseBodyData = buildResponse(ContentDirectoryBrowse.xml(config, metadata))
        // otherwise it is treated as a browse root directory request
      } else {
        responseBodyData = buildResponse(ContentDirectoryRoot.xml(config, metadata))
      }
    } else {
      responseBodyData = buildResponse(ContentDirectoryCapabilities.xml)
    }

    exchange.sendResponseHeaders(200, responseBodyData.getBytes.length)
    responseBody.write(responseBodyData.getBytes)
    responseBody.close()
  }

  private def responsePostConnectionManager(exchange: HttpExchange) {
    val headers = exchange.getResponseHeaders
    setContentType(headers)

    val responseBody = exchange.getResponseBody
    val body = buildResponse(ConnectionManagerPost.xml)
    exchange.sendResponseHeaders(200, body.getBytes.length)
    responseBody.write(body.getBytes)
    responseBody.close()
  }

  private def responseStreamInfo(exchange: HttpExchange) {
    // see http://libdlna.sourcearchive.com/documentation/0.2.3/dlna_8h-source.html for DLNA.ORG_FLAGS
    val headers = exchange.getResponseHeaders
    headers.set("Content-Type", "video/mpeg")
    headers.set(NO_CACHE._1, NO_CACHE._2)
    headers.set("contentFeatures.dlna.org", "DLNA.ORG_PN=MPEG_TS_SD_EU_ISO;DLNA.ORG_OP=10;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=01500000000000000000000000000000")
    headers.set("transferMode.dlna.org", "Streaming")
    exchange.sendResponseHeaders(200, -1)
  }

  private def responsePlayStream(exchange: HttpExchange) {
    // see http://libdlna.sourcearchive.com/documentation/0.2.3/dlna_8h-source.html for DLNA.ORG_FLAGS
    val responseHeaders = exchange.getResponseHeaders
    responseHeaders.set("TransferMode.DLNA.ORG", "Streaming")
    responseHeaders.set("Content-Type", "video/mpeg")
    responseHeaders.set("ContentFeatures.DLNA.ORG", "DLNA.ORG_PN=MPEG_TS_SD_EU_ISO;DLNA.ORG_OP=10;DLNA.ORG_CI=1;DLNA.ORG_FLAGS=01500000000000000000000000000000")
    responseHeaders.set(NO_CACHE._1, NO_CACHE._2)
    exchange.sendResponseHeaders(200, 0)

    // the tv sends the play request twice. however, I'm not sure why this happens.
    new FfmpegTranscodingStreamer(config).transcodeAndStream(metadata, exchange.getResponseBody)
  }

  // dispatch requests
  override def handle(exchange: HttpExchange) {
    logger.info("Received HTTP Request: " + exchange.getRequestMethod + " " + exchange.getRequestURI)

    exchange.getRequestMethod match {
      case "GET" => exchange.getRequestURI.toString match {
        case "/dlna/device/description" => responseDeviceDescription(exchange)
        case "/dlna/device/contentdirectory" => responseContentDirectory(exchange)
        case "/dlna/device/connectionmanager" => responseConnectionManager(exchange)
        case "/dlna/stream/1" => responsePlayStream(exchange)
        case "/dlna/images/icon_server.png" => responseIcon(exchange, config.imageUrl)
        case "/dlna/folder/image.png" => responseIcon(exchange, config.folderImageUrl)
        case "/thumbnails/stream/1/image.png" => responseIcon(exchange, config.fileImageUrl)
        case _ => responseIcon(exchange, config.imageUrl)
      }
      case "POST" => exchange.getRequestURI.toString match {
        case "/dlna/control/contentdirectory" => responsePostContentDirectory(exchange)
        case "/dlna/control/connectionmanager" => responsePostConnectionManager(exchange)
      }
      case "HEAD" => responseStreamInfo(exchange)
    }
  }
}


