<?xml version="1.0" encoding="UTF-8"?>
<om:Observation xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="
     http://www.opengis.net/om http://dev.bjoernsen.de/ogc/schema/om/1.0.30/om.xsd
     http://www.opengis.net/gml http://dev.bjoernsen.de/ogc/schema/gml/3.1.1/base/gml.xsd
     http://www.w3.org/1999/xlink http://dev.bjoernsen.de/ogc/schema/gml/3.1.1/xlink/xlinks.xsd
     http://www.opengis.net/swe http://dev.bjoernsen.de/ogc/schema/sweCommon/1.0.30/swe.xsd
     "
     xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:gml="http://www.opengis.net/gml" xmlns:om="http://www.opengis.net/om"
     xmlns:swe="http://www.opengis.net/swe"
     
     gml:id="wasserstandsmessung">
               <gml:description/>
               <gml:name></gml:name>
               <om:time/>
               <om:procedure>
               </om:procedure>
               <om:observedProperty>
                    <swe:Phenomenon gml:id="wasserstand">
                         <gml:name>Wasserstand</gml:name>
                    </swe:Phenomenon>
               </om:observedProperty>
               <om:featureOfInterest>
                    <om:Station gml:id="pegelstation">
                         <gml:description/>
                         <gml:name></gml:name>
                         <om:position>
                         </om:position>
                         <om:procedureHosted xlink:href="#wasserstand"/>
                    </om:Station>
               </om:featureOfInterest>
               <om:resultDefinition>
                    <swe:RecordDefinition recordLength="2" gml:id="rd">
                         <gml:name/>
                         <swe:component>
                              <swe:ItemDefinition gml:id="datum">
                                   <gml:name>Datum</gml:name>
                                   <swe:property>
                                        <swe:Phenomenon gml:id="phen_datum">
                                             <gml:name>Datum</gml:name>
                                        </swe:Phenomenon>
                                   </swe:property>
                                   <swe:representation>
                                        <swe:SimpleType>
                                             <st:restriction base="dateTime"
                                                  xmlns:st="http://www.seegrid.csiro.au/xml/st"/>
                                             <swe:frame xlink:href="#zz_europaberlin"/>
                                        </swe:SimpleType>
                                   </swe:representation>
                              </swe:ItemDefinition>
                         </swe:component>
                         <swe:component>
                              <swe:ItemDefinition gml:id="pegelhoehe">
                                   <gml:name>Pegelhoehe</gml:name>
                                   <swe:property>
                                        <swe:Phenomenon gml:id="phen_wasserstand">
                                             <gml:name>Wasserstand</gml:name>
                                        </swe:Phenomenon>
                                   </swe:property>
                                   <swe:representation>
                                        <swe:SimpleType>
                                             <st:restriction base="decimal"
                                                  xmlns:st="http://www.seegrid.csiro.au/xml/st">
                                                  <st:minInclusive value="0.0"/>
                                                  <st:fractionDigits value="2"/>
                                             </st:restriction>
                                             <gml:unitOfMeasure uom="dict_uom.xml#mNN"/>
                                        </swe:SimpleType>
                                   </swe:representation>
                              </swe:ItemDefinition>
                         </swe:component>
                    </swe:RecordDefinition>
               </om:resultDefinition>
               <om:result>
                    <!-- wir benutzen ein CDATA-Block damit der Layout so bleibt, 
                         sonst würde die automatische Formatierung die Sachen durcheinander bringen -->
                    <![CDATA[
2004-04-18T12:03:04Z 170.0
2004-04-19T12:03:04Z 154.8
2004-04-20T12:03:04Z 153.9
2004-04-21T12:03:04Z 155.4
2004-04-22T12:03:04Z 160.0
2004-04-23T12:03:04Z 162.3
2004-04-24T12:03:04Z 165.0
2004-04-25T12:03:04Z 170.0
2004-04-26T12:03:04Z 172.3
2004-04-27T12:03:04Z 170.2
2004-04-28T12:03:04Z 175.6
2004-04-29T12:03:04Z 177.0
2004-04-30T12:03:04Z 176.9
2004-05-01T12:03:04Z 177.6
2004-05-02T12:03:04Z 174.6
2004-05-03T12:03:04Z 175.6
2004-05-04T12:03:04Z 177.0
2004-05-05T12:03:04Z 169.9
2004-05-06T12:03:04Z 165.8
2004-05-07T12:03:04Z 166.8
2004-05-08T12:03:04Z 150.0

]]>
               </om:result>
</om:Observation>
     
