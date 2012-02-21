package com.tobiasaigner.simplestreamer.net.discovery

import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import java.net.MulticastSocket
import org.mockito.MockitoAnnotations.Mock
import org.mockito.MockitoAnnotations

import org.mockito.Mockito._
import org.mockito.Matchers._
import com.tobiasaigner.simplestreamer.configurator.DlnaConfig

/**
 * @author Tobias Aigner
 */
@RunWith(classOf[JUnitRunner])
class UdpSenderTest extends FunSuite with MockitoSugar with BeforeAndAfter {
  private var sender: UdpSender = _

  @Mock
  private val socket: MulticastSocket = null

  before {
    MockitoAnnotations.initMocks(this)

    val config = new DlnaConfig()
    sender = new UdpSender(config) {
      override protected def createSocket(port: Int) = {
        socket
      }
    }
  }

  test("sends multicast messages") {
    val messages = List("msg1", "msg2")

    sender.sendMulticast(messages, "localhost", 80)

    verify(socket, times(2)).send(any())
  }
}