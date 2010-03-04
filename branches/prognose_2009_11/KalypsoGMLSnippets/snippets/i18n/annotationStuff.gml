<?xml version="1.0" encoding="UTF-8"?>
<AdvancedCollection xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml" xmlns:xs="http://www.w3.org/2001/XMLSchema-instance" xmlns="org.kalypso.gml.snippets.annotationStuff" xs:schemaLocation="org.kalypso.gml.snippets.annotationStuff annotationStuff.xsd" gml:id="root">
 <gml:description>The collection contains only its subfeatures and has no other property.</gml:description>
 <gml:name>A simple collection of features.</gml:name>
 <singleMember>
  <SimpleSubFeature gml:id="SimpleSubFeature12090356548640">
   <value>0.0</value>
  </SimpleSubFeature>
 </singleMember>
 <memberInline>
  <SimpleSubFeature gml:id="SimpleSubFeature12090248643781">
   <gml:description>This is the first member of the list.</gml:description>
   <gml:name>A sub feature</gml:name>
   <value>1.2</value>
  </SimpleSubFeature>
 </memberInline>
 <memberInline>
  <SimpleSubFeature gml:id="SimpleSubFeature12090248942181">
   <gml:description>This is the second member of the list.	</gml:description>
   <gml:name>another sub feature</gml:name>
   <value>2.0</value>
  </SimpleSubFeature>
 </memberInline>
 <memberLinked xlink:href="#SimpleSubFeature12090248643781"/>
 <memberLinked xlink:href="#SimpleSubFeature12090263029271"/>
 <memberMixed>
  <SimpleSubFeature gml:id="SimpleSubFeature12090263029271">
   <gml:description>An inline member of this mixed list.</gml:description>
   <gml:name>Yet another feature</gml:name>
   <value>17.0</value>
  </SimpleSubFeature>
 </memberMixed>
</AdvancedCollection>
