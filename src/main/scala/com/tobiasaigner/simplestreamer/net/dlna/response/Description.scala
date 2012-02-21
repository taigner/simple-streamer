package com.tobiasaigner.simplestreamer.net.dlna.response

import com.tobiasaigner.simplestreamer.configurator.DlnaConfig

/**
 * @author Tobias Aigner
 */
object Description {
  def xml(config: DlnaConfig) = <root xmlns:dlna="urn:schemas-dlna-org:device-1-0" xmlns="urn:schemas-upnp-org:device-1-0">
    <specVersion>
      <major>1</major>
      <minor>0</minor>
    </specVersion>
    <URLBase>http://{config.httpServerIP}:{config.httpServerPort}/</URLBase>
    <device>
      <dlna:X_DLNADOC xmlns:dlna="urn:schemas-dlna-org:device-1-0">DMS-1.50</dlna:X_DLNADOC>
      <dlna:X_DLNADOC xmlns:dlna="urn:schemas-dlna-org:device-1-0">M-DMS-1.50</dlna:X_DLNADOC>
      <deviceType>urn:schemas-upnp-org:device:MediaServer:1</deviceType>
      <friendlyName>{config.serverDisplayName}</friendlyName>
      <manufacturer>{config.serverDisplayName}</manufacturer>
      <manufacturerURL>http://www.tobiasaigner.com/</manufacturerURL>
      <modelDescription>DLNA Server</modelDescription>
      <modelName>tobiasaigner</modelName>
      <modelNumber>01</modelNumber>
      <modelURL>http://www.tobiasaigner.com</modelURL>
      <serialNumber/>
      <UPC/>
      <UDN>uuid:{config.uuid}</UDN>
      <iconList>
        <icon>
          <mimetype>image/jpeg</mimetype>
          <width>120</width>
          <height>120</height>
          <depth>24</depth>
          <url>/dlna/images/icon_server.png</url>
        </icon>
      </iconList>
      <presentationURL>http://{config.httpServerIP}:{config.httpServerPort}/index.html</presentationURL>
      <serviceList>
        <service>
          <serviceType>urn:schemas-upnp-org:service:ContentDirectory:1</serviceType>
          <serviceId>urn:upnp-org:serviceId:ContentDirectory</serviceId>
          <SCPDURL>/dlna/device/contentdirectory</SCPDURL>
          <controlURL>/dlna/control/contentdirectory</controlURL>
          <eventSubURL>/dlna/event/contentdirectory</eventSubURL>
        </service>
        <service>
          <serviceType>urn:schemas-upnp-org:service:ConnectionManager:1</serviceType>
          <serviceId>urn:upnp-org:serviceId:ConnectionManager</serviceId>
          <SCPDURL>/dlna/device/connectionmanager</SCPDURL>
          <controlURL>/dlna/control/connectionmanager</controlURL>
          <eventSubURL>/dlna/event/connectionmanager</eventSubURL>
        </service>
      </serviceList>
    </device>
  </root>
}
