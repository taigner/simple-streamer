package com.tobiasaigner.simplestreamer.net.dlna

import com.sun.net.httpserver.HttpServer
import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.net.{InetAddress, InetSocketAddress}
import com.tobiasaigner.simplestreamer.configurator.DlnaConfig
import com.tobiasaigner.simplestreamer.metadata.Metadata

/**
 * @author Tobias Aigner
 */
class HttpServerJDK(private val config: DlnaConfig, private val metadata: Metadata) {
  private var server: HttpServer = _
  private val logger = LoggerFactory.getLogger(classOf[HttpServerJDK])

  def start() {
    logger.info("ip {}", config.httpServerIP)
    val address: InetSocketAddress = new InetSocketAddress(InetAddress.getByName(config.httpServerIP), config.httpServerPort)
    server = createServer(address)

    server.createContext("/", new HttpServerJDKHandler(config, metadata))
    server.setExecutor(Executors.newCachedThreadPool())
    server.start()

    logger.info("Http Server is listening on port " + config.httpServerPort)
  }

  def stop() {
    logger.info("Stopping Http Server")
    server.stop(0)
  }

  protected def createServer(address: InetSocketAddress): HttpServer = HttpServer.create(address, 0)

  def setServer(server: HttpServer) {
    this.server = server
  }
}
