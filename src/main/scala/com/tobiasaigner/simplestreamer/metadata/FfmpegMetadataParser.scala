package com.tobiasaigner.simplestreamer.metadata

import sys.process.{ProcessLogger, Process}

/**
 * @author Tobias Aigner
 */
class FfmpegMetadataParser extends MetadataParser {
  private val DURATION_PATTERN = """.*Duration: (.*), start.*, bitrate: (.*) kb/s.*""".r
  private val RESOLUTION_PATTERN = """.*Video: .*, (\d+)x(\d+).*""".r

  override def parse(filename: String): Metadata = {
    val process = Process("ffmpeg -i " + filename)
    var out = List[String]()
    var stdErr = List[String]()

    process ! ProcessLogger((s) => out ::= s, (s) => stdErr ::= s)

    val metadata = new Metadata()

    for (line <- stdErr) {
      matchPatterns(line, metadata)
    }

    metadata.title = extractTitle(filename)
    metadata.filename = filename

    metadata
  }

  def matchPatterns(str: String, metadata: Metadata) {
    try {
      str match {
        case DURATION_PATTERN(duration, bitrate) => metadata.duration = duration
        metadata.bitrate = convertBitrate(bitrate.toInt)
        case RESOLUTION_PATTERN(resx, resy) => metadata.resolution = (resx.toInt, resy.toInt)
        case _ => ()
      }
    } catch {
      case _ => ()
    }
  }

  private def convertBitrate(rate: Int) = rate * 1024 / 8

  private def extractTitle(name: String): String = name.substring(name.lastIndexOf("/") + 1)
}