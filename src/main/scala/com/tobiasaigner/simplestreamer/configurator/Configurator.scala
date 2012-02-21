package com.tobiasaigner.simplestreamer.configurator

/**
 * @author Tobias Aigner
 */
trait Configurator {
  type ConfigurationFilename = String

  def load(fileName: ConfigurationFilename): DlnaConfig
}