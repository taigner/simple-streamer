package com.tobiasaigner.simplestreamer

import configurator.YamlConfigurator
import metadata.FfmpegMetadataParser
import net.dlna.HttpServerJDK
import net.discovery.{MulticastListener, UdpSender, UpnpDiscovery}
import java.util.concurrent.Executors

/**
 * @author Tobias Aigner
 */
object SimpleStreamer extends App {
  checkArguments(args)

  val currentDirectory: String = System.getProperty("user.dir")
  val config = new YamlConfigurator().load(currentDirectory + "/config.yml")

  config.imageUrl = "icons/tv.png"
  config.folderImageUrl = "icons/folder_video.png"
  config.fileImageUrl = "icons/video.png"

  val metadataParser = new FfmpegMetadataParser
  val metadata = metadataParser.parse(args(0))

  val discoverySender = new UdpSender(config)
  val discoveryListener = new MulticastListener(config)
  val discovery = new UpnpDiscovery(config, discoverySender, discoveryListener)
  val httpServer = new HttpServerJDK(config, metadata)

  httpServer.start()

  discovery.announce(Executors.newScheduledThreadPool(1))
  discovery.startListening()

  addShutdownHook()

  while (true) {
    Thread.sleep(500)
  }

  private def checkArguments(args: Array[String]) {
    if (args.length != 1) {
      println("Must be started with 1 argument. The filename of the movie to be played.")
      System.exit(-1)
    }
  }

  private def addShutdownHook() {
    Runtime.getRuntime.addShutdownHook(new Thread {
      override def run() {
        try {
          discovery.byebye()
          httpServer.stop()
          Thread.sleep(500)
        } catch {
          case e: Exception => {}
        }
      }
    })
  }
}