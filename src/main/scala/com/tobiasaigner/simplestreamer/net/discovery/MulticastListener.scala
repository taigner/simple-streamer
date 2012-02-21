package com.tobiasaigner.simplestreamer.net.discovery

import java.net.{DatagramPacket, SocketTimeoutException, InetAddress, MulticastSocket}
import com.tobiasaigner.simplestreamer.configurator.DlnaConfig
import java.util.concurrent.{ExecutorService, Executors}

/**
 * @author Tobias Aigner
 */
class MulticastListener(private val config: DlnaConfig) extends DiscoveryListener {
  private val executor: ExecutorService = createExecutor()

  private val UDP_BUFFER_SIZE = 1024
  private val TIME_TO_LIVE = 4

  /**
   * Starts listening for data on discovery port.
   *
   * @param callback If data is received the callback function is invoked.
   */
  override def listen(callback: DatagramPacket => Unit) {
    val socket = createSocket()

    val buffer = Array.ofDim[Byte](UDP_BUFFER_SIZE)
    val data = new DatagramPacket(buffer, buffer.length)

    executor.execute(new Runnable() {
      override def run() {
        while (!executor.isShutdown) {
          try {
            socket.receive(data)
            callback(data)
          } catch {
            case e: SocketTimeoutException => ()
          }
        }
        socket.close()
      }
    })
  }

  override def stop() {
    executor.shutdown()
  }

  protected def createSocket(): MulticastSocket = {
    val socket = new MulticastSocket(config.discoverySenderPort)
    val multicastAddressGroup = InetAddress.getByName(config.discoverySenderHost)

    socket.joinGroup(multicastAddressGroup)
    socket.setTimeToLive(TIME_TO_LIVE)
    socket.setReuseAddress(true)

    socket
  }

  protected def createExecutor(): ExecutorService = Executors.newCachedThreadPool()
}