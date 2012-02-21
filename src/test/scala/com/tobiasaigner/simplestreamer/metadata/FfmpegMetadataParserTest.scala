package com.tobiasaigner.simplestreamer.metadata

import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith
import org.junit.Assert._

/**
 * @author Tobias Aigner
 */
@RunWith(classOf[JUnitRunner])
class FfmpegMetadataParserTest extends FunSuite with BeforeAndAfter {
  private var parser: FfmpegMetadataParser = _

  before {
    parser = new FfmpegMetadataParser()
  }

  test("matches utilized metadata patterns") {
    val lines = List("Duration: 00:31:07.47, start: 0.000000, bitrate: 1273 kb/s",
      "Stream #0:0: Video: mpeg4 (Advanced Simple Profile) (XVID / 0x44495658), yuv420p, 640x352 [SAR 1:1 DAR 20:11], 23.98 tbr, 23.98 tbn, 23.98 tbc")
    val expected = new Metadata {
      duration = "00:31:07.47";
      bitrate = 162944;
      resolution = (640, 352)
    }
    val actual = new Metadata

    for (line <- lines) {
      parser.matchPatterns(line, actual)
    }

    assertEquals(actual.duration, expected.duration)
    assertEquals(actual.bitrate, expected.bitrate)
    assertEquals(actual.resolution, expected.resolution)
  }
}