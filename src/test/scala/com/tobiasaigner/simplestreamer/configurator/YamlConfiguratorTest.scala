package com.tobiasaigner.simplestreamer.configurator

import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.junit.Assert._
import java.util.HashMap

/**
 * @author Tobias Aigner
 */
@RunWith(classOf[JUnitRunner])
class YamlConfiguratorTest extends FunSuite with BeforeAndAfter {
  private var configurator: YamlConfigurator = _

  before {
    configurator = new YamlConfigurator
  }

  test("extracts config data from parsed map") {
    val expected = new DlnaConfig {
      httpServerIP = "httpIp";
      httpServerPort = 80;
      serverDisplayName = "test";
      ffmpegOptions = "options";
      discoverySenderHost = "discoveryHost";
      discoverySenderPort = 100
    }

    val discovery: HashMap[String, Any] = new HashMap[String, Any]()
    discovery.put("senderHost", "discoveryHost")
    discovery.put("senderPort", 100)

    val map = Map("httpServerIP" -> "httpIp",
      "httpServerPort" -> 80,
      "serverDisplayName" -> "test",
      "ffmpegOptions" -> "options",
      "discovery" -> discovery)

    assertEquals(expected, configurator.extractConfigurationFromMap(map))
  }
}