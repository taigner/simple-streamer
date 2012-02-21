package com.tobiasaigner.simplestreamer.net.dlna.response

import xml.Elem

/**
 * @author Tobias Aigner
 */
trait ResponseBuilder {
  def buildResponse(elem: Elem): String = XmlHeader.header + elem.toString()
}

object XmlHeader {
  val header = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n"
}