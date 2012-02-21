package com.tobiasaigner.simplestreamer.metadata

/**
 * @author Tobias Aigner
 */
trait MetadataParser {
  def parse(filename: String): Metadata
}