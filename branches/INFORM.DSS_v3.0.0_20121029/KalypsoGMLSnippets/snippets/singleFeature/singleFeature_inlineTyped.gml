<?xml version="1.0" encoding="UTF-8"?>
<SimpleFeature gml:id="root" xmlns="org.kalypso.gml.snippets.singleFeature_inlineTyped" xmlns:gml="http://www.opengis.net/gml"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
		xsi:schemaLocation="org.kalypso.gml.snippets.singleFeature_inlineTyped singleFeature_inlineTyped.xsd">

		<!--  metaDataProperty not (yet) correctly supported by kalypso -->
		<!-- gml:metaDataProperty></gml:metaDataProperty-->
		<gml:description>A nice description of this feature</gml:description>
		<gml:name>A good name</gml:name>
		<!-- boundedBy should at least have a gml:Null content, but this is not (yet) supported by kalypso -->
		<!-- The gm ldoes not validate at this point, put kalypso still parses it, reading a null envelope here -->
		<gml:boundedBy><!--gml:Null/--></gml:boundedBy>
		<gml:location></gml:location>
		<value>0.001</value>
</SimpleFeature>