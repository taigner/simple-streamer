package com.tobiasaigner.simplestreamer.streaming

import java.io.OutputStream
import com.tobiasaigner.simplestreamer.metadata.Metadata

/**
 * @author Tobias Aigner
 */
trait TranscodingStreamer {
  def transcodeAndStream(metadata: Metadata, outputStream: OutputStream)
}