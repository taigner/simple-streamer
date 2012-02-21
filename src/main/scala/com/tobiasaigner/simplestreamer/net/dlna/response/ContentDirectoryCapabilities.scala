package com.tobiasaigner.simplestreamer.net.dlna.response

/**
 * @author Tobias Aigner
 */
object ContentDirectoryCapabilities {
  def xml = <s:Envelope xmlns:s="http://schemas.xmlsoap.org/soap/envelope/" s:encodingStyle="http://schemas.xmlsoap.org/soap/encoding/">
    <s:Body>
      <u:GetSortCapabilitiesResponse xmlns:u="urn:schemas-upnp-org:service:ContentDirectory:1">
        <SortCaps></SortCaps>
      </u:GetSortCapabilitiesResponse>
    </s:Body>
  </s:Envelope>
}