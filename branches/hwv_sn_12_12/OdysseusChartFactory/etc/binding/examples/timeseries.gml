<?xml version="1.0" encoding="UTF-8"?>
<om:ObservationCollection xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="
     http://www.opengis.net/om http://dev.bjoernsen.de/ogc/schema/om/1.0.30/om.xsd
     http://www.opengis.net/gml http://dev.bjoernsen.de/ogc/schema/gml/3.1.1/base/gml.xsd
     http://www.w3.org/1999/xlink http://dev.bjoernsen.de/ogc/schema/gml/3.1.1/xlink/xlinks.xsd
     http://www.opengis.net/swe http://dev.bjoernsen.de/ogc/schema/sweCommon/1.0.30/swe.xsd
     http://www.ksp.org/om ../../schemas/original/kalypso/omExtensions.xsd
     "
     xmlns:kom="http://www.ksp.org/om" xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:gml="http://www.opengis.net/gml" xmlns:om="http://www.opengis.net/om"
     xmlns:swe="http://www.opengis.net/swe">

     <gml:name>Wasserstandsmessung mit W/Q-Beziehung und Alarmstufen</gml:name>
     <om:time><!-- für uns bringts nix denke ich, angabe ist aber erforderlich sonst dok nicht gültig --></om:time>

     <om:member>
          <om:Observation gml:id="wasserstandsmessung">
               <!-- die reine Zeitreihe wird als CommonObservation
                    modelliert. Als resultDefinition kommt DataDefinition
                    zum Einsatz. Es ermöglicht die inline-Spezifikation
                    des Datenblock im result.
               -->
               <gml:metaDataProperty gml:remoteSchema="schema_om.xsd">
                    <kom:metaData>
                         <name>Gewässer</name>
                         <value>Schwarze Elster</value>
                    </kom:metaData>
               </gml:metaDataProperty>
               <gml:metaDataProperty gml:remoteSchema="schema_om.xsd">
                    <kom:metaData>
                         <name>Kennziffer</name>
                         <value>553050</value>
                    </kom:metaData>
               </gml:metaDataProperty>
               <gml:metaDataProperty gml:remoteSchema="schema_om.xsd">
                    <kom:metaData>
                         <name>Szenario</name>
                         <value/>
                    </kom:metaData>
               </gml:metaDataProperty>
               <gml:metaDataProperty gml:remoteSchema="schema_om.xsd">
                    <kom:metaData>
                         <name>Vorhersage</name>
                         <value type="DateRangeMarker">
                              <dateFrom>2005-05-18T11:00:00</dateFrom>
                              <dateTo>2005-08-24T11:00:00</dateTo>
                         </value>
                    </kom:metaData>
               </gml:metaDataProperty>
               <gml:metaDataProperty gml:remoteSchema="schema_om.xsd">
                    <kom:metaData>
                         <name>OCS-ID</name>
                         <value type="URI"
                              >kalypso-ocs:wiski://HVZ_Modellierung_Saale.Wasserstand.553050</value>
                    </kom:metaData>
               </gml:metaDataProperty>
               <gml:description/>
               <gml:name>Bad Liebenwerda.W.15</gml:name>
               <om:time/>
               <om:procedure>
                    <om:ObservationProcedure gml:id="wiski">
                         <gml:description>Schnittstelle zum Wiski Webdata Provider (WDP) der Firma
                              Kisters AG, Aachen</gml:description>
                         <gml:name>Wiski</gml:name>
                         <om:method/>
                    </om:ObservationProcedure>
               </om:procedure>
               <om:observedProperty>
                    <swe:Phenomenon gml:id="wasserstand">
                         <gml:name>Wasserstand</gml:name>
                    </swe:Phenomenon>
               </om:observedProperty>
               <om:featureOfInterest>
                    <om:Station gml:id="pegelstation">
                         <!-- das Feature (featureOfInterest) -->
                         <gml:description/>
                         <gml:name>Löben</gml:name>
                         <om:position>
                              <!-- TODO: ist die Frage ob man mit dem SRS so als URN kodiert zu Recht kommt -->
                              <gml:Point srsName="urn:kalypso:def:crs:EPSG:5:31468">
                                   <gml:pos>4596940 5710280</gml:pos>
                              </gml:Point>
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
                                   <swe:property xlink:href="#wasserstand"/>
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

]]></om:result>
          </om:Observation>
     </om:member>

<!-- Das hier geht über Marcs Beispiel hinaus - das sind imaginäre Niederschläge vom Ali -->

     <om:member>
          <om:Observation gml:id="niederschlagsmessung">
               <!-- die reine Zeitreihe wird als CommonObservation
                    modelliert. Als resultDefinition kommt DataDefinition
                    zum Einsatz. Es ermöglicht die inline-Spezifikation
                    des Datenblock im result.
               -->
               <gml:description/>
               <gml:name>Bad Liebenwerda.W.15</gml:name>
               <om:time/>
               <om:procedure>
                    <om:ObservationProcedure gml:id="wiski">
                         <gml:description>Schnittstelle zum Wiski Webdata Provider (WDP) der Firma
                              Kisters AG, Aachen</gml:description>
                         <gml:name>Wiski</gml:name>
                         <om:method/>
                    </om:ObservationProcedure>
               </om:procedure>
               <om:observedProperty>
                    <swe:Phenomenon gml:id="niederschlag">
                         <gml:name>Niederschlag</gml:name>
                    </swe:Phenomenon>
               </om:observedProperty>
               <om:featureOfInterest>
                    <om:Station gml:id="pegelstation">
                         <!-- das Feature (featureOfInterest) -->
                         <gml:description/>
                         <gml:name>Löben</gml:name>
                         <om:position>
                              <!-- TODO: ist die Frage ob man mit dem SRS so als URN kodiert zu Recht kommt -->
                              <gml:Point srsName="urn:kalypso:def:crs:EPSG:5:31468">
                                   <gml:pos>4596940 5710280</gml:pos>
                              </gml:Point>
                         </om:position>
                         <om:procedureHosted xlink:href="#niederschlag"/>
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
                              <swe:ItemDefinition gml:id="niederschlag">
                                   <gml:name>Niederschlag</gml:name>
                                   <swe:property xlink:href="#niederschlag"/>
                                   <swe:representation>
                                        <swe:SimpleType>
                                             <st:restriction base="decimal"
                                                  xmlns:st="http://www.seegrid.csiro.au/xml/st">
                                                  <st:minInclusive value="0.0"/>
                                                  <st:fractionDigits value="2"/>
                                             </st:restriction>
                                             <gml:unitOfMeasure uom="dict_uom.xml#mm"/>
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
2004-04-18T12:03:04Z 0.0
2004-04-19T12:03:04Z 10
2004-04-20T12:03:04Z 13
2004-04-21T12:03:04Z 2
2004-04-22T12:03:04Z 6
2004-04-23T12:03:04Z 14
2004-04-24T12:03:04Z 20

2004-04-26T12:03:04Z 23
2004-04-27T12:03:04Z 4
2004-04-28T12:03:04Z 20
2004-04-29T12:03:04Z 15
2004-04-30T12:03:04Z 50
2004-05-01T12:03:04Z 45
2004-05-02T12:03:04Z 40
2004-05-03T12:03:04Z 0
2004-05-04T12:03:04Z 32
2004-05-05T12:03:04Z 35
2004-05-06T12:03:04Z 0
2004-05-07T12:03:04Z 23
2004-05-08T12:03:04Z 12

]]></om:result>
          </om:Observation>
     </om:member>



     <om:member>

          <om:Observation gml:id="wq_table1">

               <!-- die WQ-Beziehung wird als Observation modelliert
                    weil man hier für die resultDefinition auf eines in einem
                    dictionnary gespeicherte Spezifikation zurückgreift. Das geht nicht
                    für CommonObservation (DataDefinitionType leitet nicht von DefinitionType ab).
                    Schade. 
                    
                    die observedProperty (hier wq-table im phenomenon dictionary) sagt 
                    was das für eine Observation ist: nämlich eine WQ-Table Observation
                    mit der entsprechende resultDefinition usw. 
               -->

               <gml:description>Beinhaltet die WQ-Beziehung</gml:description>
               <gml:name>WQ</gml:name>
               <om:time>
                    <!-- in time könnte der Gültigkeitsbereich der WQ-Beziehung
                         kodiert sein -->
                    <gml:TimeInstant>
                         <gml:timePosition>2005-06-06+02:00</gml:timePosition>
                    </gml:TimeInstant>
               </om:time>
               <om:procedure>
                    <om:ObservationProcedure gml:id="proc-wq-table">
                         <gml:description>KALYPSO WQ-Tabelle</gml:description>
                         <gml:name>WQ-Tabelle</gml:name>
                         <om:method/>
                    </om:ObservationProcedure>
               </om:procedure>
               <om:observedProperty>
                    <swe:Phenomenon gml:id="phen-wq-table">
                         <gml:name>WQ-Tabelle</gml:name>
                    </swe:Phenomenon>
               </om:observedProperty>
               <om:featureOfInterest xlink:href="#pegelstation"/>
               <om:resultDefinition>
                    <swe:RecordDefinition gml:id="wq_rd" recordLength="2">
                         <gml:name/>
                         <swe:component xlink:href="#pegelhoehe"/>
                         <swe:component>
                              <swe:ItemDefinition gml:id="abflussmenge">
                                   <gml:name/>
                                   <swe:property>
                                        <swe:Phenomenon gml:id="phen_abfluss">
                                             <gml:name>Abfluss</gml:name>
                                        </swe:Phenomenon>
                                   </swe:property>
                                   <swe:representation>
                                        <swe:SimpleType>
                                             <st:restriction base="decimal"
                                                  xmlns:st="http://www.seegrid.csiro.au/xml/st">
                                                  <st:minInclusive value="0.0"/>
                                                  <st:fractionDigits value="4"/>
                                             </st:restriction>
                                             <gml:unitOfMeasure uom="#m3s"/>
                                        </swe:SimpleType>
                                   </swe:representation>
                              </swe:ItemDefinition>
                         </swe:component>
                    </swe:RecordDefinition>
               </om:resultDefinition>
               <!-- Ich nehme mal an, dass die einheit für die Pegelhöhe in cm angegeben ist - das muss man im Dictionairy anpassen -->
               <om:result><![CDATA[
-69.0 0.0
-68.0 0.0
-67.0 0.0
-66.0 0.0
-65.0 0.0
-64.0 0.0010
-63.0 0.0020
-62.0 0.0030
-61.0 0.0040
-60.0 0.0050 
-59.0 0.0055 
-58.0 0.0057 
-57.0 0.0060 
-56.0 0.0063 
-55.0 0.0069 
-54.0 0.0072 
-53.0 0.0077 
-52.0 0.0083 
-51.0 0.0087 
-50.0 0.0090 
]]>
                    
               </om:result>
          </om:Observation>
     </om:member>
     
<!--
     <om:member>
          <om:Measurement>
               <gml:name>Alarmstufe 1</gml:name>
               <om:time/>
               <om:procedure>
               <om:ObservationProcedure gml:id="alarmstufeneinteilung">
               <gml:name/>
               <om:method/>
               </om:ObservationProcedure>
               </om:procedure>
               <om:observedProperty>
               <swe:Phenomenon gml:id="alarmstufe">
               <gml:name>Alarmstufe</gml:name>
               </swe:Phenomenon>
               </om:observedProperty>
               <om:featureOfInterest xlink:href="#pegelstation"/>
               <om:result uom="dict_uom.xml#cmaP">20</om:result>
          </om:Measurement>
     </om:member>
     <om:member>
          <om:Measurement>
          <gml:name>Alarmstufe 2</gml:name>
          <om:time/>
          <om:procedure xlink:href="#alarmstufeneinteilung"/>
          <om:observedProperty xlink:href="#alarmstufe"/>
          <om:featureOfInterest xlink:href="#pegelstation"/>
          <om:result uom="dict_uom.xml#cmaP">30</om:result>
          </om:Measurement>
          </om:member>
          <om:member>
          <om:Measurement>
          <gml:name>Alarmstufe 3</gml:name>
          <om:time/>
          <om:procedure xlink:href="#alarmstufeneinteilung"/>
          <om:observedProperty xlink:href="#alarmstufe"/>
          <om:featureOfInterest xlink:href="#pegelstation"/>
          <om:result uom="dict_uom.xml#cmaP">40</om:result>
          </om:Measurement>
          </om:member>
          <om:member>
          <om:Measurement>
          <gml:name>Alarmstufe 4</gml:name>
          <om:time/>
          <om:procedure xlink:href="#alarmstufeneinteilung"/>
          <om:observedProperty xlink:href="#alarmstufe"/>
          <om:featureOfInterest xlink:href="#pegelstation"/>
          <om:result uom="dict_uom.xml#cmaP">50</om:result>
          </om:Measurement>
          </om:member>
          
          -->
</om:ObservationCollection>
