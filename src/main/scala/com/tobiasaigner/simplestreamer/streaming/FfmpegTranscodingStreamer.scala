package com.tobiasaigner.simplestreamer.streaming

import org.apache.commons.exec._
import java.io.{OutputStream, PipedInputStream, DataInputStream, PipedOutputStream}
import org.slf4j.LoggerFactory
import com.tobiasaigner.simplestreamer.configurator.DlnaConfig
import com.tobiasaigner.simplestreamer.metadata.Metadata

/**
 * Starts transcoding and streaming using ffmpeg.
 *
 * @author Tobias Aigner
 */
class FfmpegTranscodingStreamer(private val config: DlnaConfig) extends TranscodingStreamer {
  private val BUFSIZE: Int = 67108864
  private val FFMPEG_COMMAND = "ffmpeg"
  private val ffmpegOptions: Array[String] = config.ffmpegOptions.split(" ")

  private val logger = LoggerFactory.getLogger(classOf[FfmpegTranscodingStreamer])

  override def transcodeAndStream(metadata: Metadata, outputStream: OutputStream) {
    // see http://stackoverflow.com/questions/956323/capturing-large-amounts-of-output-from-apache-commons-exec
    val commandLine = new CommandLine(FFMPEG_COMMAND)
    val options = Array("-i", metadata.filename) ++ ffmpegOptions
    options.foreach(s => commandLine.addArgument(s))

    // pipes for the executed process
    val stdout = new PipedOutputStream()
    val streamHandler = new PumpStreamHandler(stdout, System.err)

    val executor = new DefaultExecutor()
    try {
      val pipedInput = new DataInputStream(new PipedInputStream(stdout))
      executor.setStreamHandler(streamHandler)

      // start process asynchronously
      executor.execute(commandLine, new ExecuteResultHandler() {
        def onProcessComplete(c: Int) { }
        def onProcessFailed(e: ExecuteException) { }
      })

      val buffer = new Array[Byte](BUFSIZE)
      var len: Int = 0

      // stream data and flush it immediately
      while ({ len = pipedInput.read(buffer, 0, BUFSIZE); len != -1 }) {
        outputStream.write(buffer, 0, len)
        outputStream.flush()
      }
      outputStream.close()
    } catch {
      case e => {
        logger.warn("Streaming process failed", e)
        executor.getWatchdog.destroyProcess()
      }
    }
  }
}