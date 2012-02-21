package com.tobiasaigner.simplestreamer.configurator

import org.junit.Assert._
import org.scalatest.{BeforeAndAfter, FunSuite}
import org.scalatest.junit.JUnitRunner
import org.junit.runner.RunWith

/**
 * @author Tobias Aigner
 */
@RunWith(classOf[JUnitRunner])
class DLNAConfigTest extends FunSuite with BeforeAndAfter {
  private var config: DlnaConfig = _

  before {
    config = new DlnaConfig
  }

  test("UUID is always the same (based on MAC-address)") {
    val expectedConfig = new DlnaConfig
    assertEquals(expectedConfig.uuid, config.uuid)
  }
}