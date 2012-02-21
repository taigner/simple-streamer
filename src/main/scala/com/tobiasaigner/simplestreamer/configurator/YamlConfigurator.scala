package com.tobiasaigner.simplestreamer.configurator

import org.yaml.snakeyaml.Yaml
import java.io.{FileInputStream, BufferedInputStream}
import scala.collection.JavaConverters._

/**
 * @author Tobias Aigner
 */
class YamlConfigurator extends Configurator {
  /**
   * Parse yaml configuration file.
   *
   * @param fileName Path to yaml file.
   * @return Configuration data extracted from parsing the configuration file.
   */
  override def load(fileName: ConfigurationFilename): DlnaConfig = {
    val yaml = new Yaml()
    val map = yaml.load(new BufferedInputStream(new FileInputStream(fileName.asInstanceOf[String]))).asInstanceOf[java.util.HashMap[String, Object]]
    extractConfigurationFromMap(map.asScala.toMap)
  }

  def extractConfigurationFromMap(values: Map[String, Any]): DlnaConfig = {
    val config = new DlnaConfig

    config.httpServerIP = values.get("httpServerIP").get.asInstanceOf[String]
    config.httpServerPort = values.get("httpServerPort").get.asInstanceOf[Int]
    config.serverDisplayName = values.get("serverDisplayName").get.asInstanceOf[String]
    config.ffmpegOptions = values.get("ffmpegOptions").get.asInstanceOf[String]

    val discoveryValues: Map[String, Object] = values.get("discovery").get.asInstanceOf[java.util.HashMap[String, Object]].asScala.toMap
    config.discoverySenderHost = discoveryValues.get("senderHost").get.asInstanceOf[String]
    config.discoverySenderPort = discoveryValues.get("senderPort").get.asInstanceOf[Int]

    config
  }
}