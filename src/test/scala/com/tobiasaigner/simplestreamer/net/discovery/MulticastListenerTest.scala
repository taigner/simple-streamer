package com.tobiasaigner.simplestreamer.net.discovery

import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.mockito.MockitoAnnotations.Mock
import org.mockito.MockitoAnnotations
import java.net.MulticastSocket
import org.mockito.Mockito._
import org.mockito.Matchers._
import com.tobiasaigner.simplestreamer.configurator.DlnaConfig

/**
 * @author Tobias Aigner
 */
@RunWith(classOf[JUnitRunner])
class MulticastListenerTest extends FunSuite with MockitoSugar with BeforeAndAfter {
  var listener: MulticastListener = _
  @Mock
  var socket: MulticastSocket = null

  before {
    MockitoAnnotations.initMocks(this)

    val config = new DlnaConfig
    listener = new MulticastListener(config) {
      override protected def createSocket(): MulticastSocket = {
        socket
      }
    }
  }

  after {
    listener.stop()
  }

  test("listens for packets") {
    listener.listen(d => d)
    Thread.sleep(1000)
    verify(socket, atLeastOnce()).receive(any())
  }
}