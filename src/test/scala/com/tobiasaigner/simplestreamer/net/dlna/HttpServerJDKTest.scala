package com.tobiasaigner.simplestreamer.net.dlna

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.mock.MockitoSugar
import java.net.InetSocketAddress
import com.sun.net.httpserver.HttpServer
import org.mockito.{MockitoAnnotations, Mock}

import org.mockito.Mockito._
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import com.tobiasaigner.simplestreamer.configurator.DlnaConfig
import com.tobiasaigner.simplestreamer.metadata.Metadata

/**
 * @author Tobias Aigner
 */
@RunWith(classOf[JUnitRunner])
class HttpServerJDKTest extends FunSuite with MockitoSugar with BeforeAndAfter {
  private var server: HttpServerJDK = _
  private var config: DlnaConfig = _
  private var metadata: Metadata = _

  @Mock
  private val httpServer: HttpServer = null

  before {
    MockitoAnnotations.initMocks(this)

    config = new DlnaConfig
    metadata = new Metadata

    // create with mocked server
    server = new HttpServerJDK(config, metadata) {
      override def createServer(address: InetSocketAddress): HttpServer = {
        httpServer
      }
    }
    server.setServer(httpServer)
  }

  test("starts server") {
    server.start()

    verify(httpServer).start()
  }

  test("stops server") {
    server.stop()

    verify(httpServer).stop(0)
  }
}