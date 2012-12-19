<?xml version="1.0" encoding="UTF-8"?>
<gml:Bag xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="
     http://www.opengis.net/om http://dev.bjoernsen.de/ogc/schema/om/1.0.30/om.xsd
     http://www.opengis.net/gml http://dev.bjoernsen.de/ogc/schema/gml/3.1.1/base/gml.xsd
     http://www.w3.org/1999/xlink http://dev.bjoernsen.de/ogc/schema/gml/3.1.1/xlink/xlinks.xsd
     http://www.opengis.net/swe http://dev.bjoernsen.de/ogc/schema/sweCommon/1.0.30/swe.xsd
     org.kalypso.gml.om ../schema_om.xsd
     "
     xmlns:kom="org.kalypso.gml.om" xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:gml="http://www.opengis.net/gml" xmlns:om="http://www.opengis.net/om"
     xmlns:swe="http://www.opengis.net/swe">

     <gml:member>
          <om:ObservationCollection>
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
                         <om:procedure xlink:href="dict_procedure.xml#wiski"/>
                         <om:observedProperty xlink:href="dict_phenomenon.xml#wasserstand"/>
                         <om:featureOfInterest xlink:href="#pegelstation"/>
                         <!-- hier ist die foi referenziert, kÃ¶nnte aber auch inline sein -->
                         <om:resultDefinition>
                              <swe:RecordDefinition recordLength="2" gml:id="rd">
                                   <gml:name/>
                                   <swe:component xlink:href="dict_components.xml#datum"/>
                                   <swe:component xlink:href="dict_components.xml#pegelhoehe"/>
                              </swe:RecordDefinition>
                         </om:resultDefinition>
                         <om:result>
                              <!-- wir benutzen ein CDATA-Block damit der Layout so bleibt, 
                              sonst würde die automatische Formatierung die Sachen durcheinander bringen -->
                              <![CDATA[
2004-04-18T12:03:04Z 170.0
2004-04-19T12:03:04Z 154.8
2004-04-20T12:03:04Z 153.9
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
                         <om:time>
                              <!-- in time könnte der Gültigkeitsbereich der WQ-Beziehung
                              kodiert sein -->
                              <gml:TimeInstant>
                                   <gml:timePosition>2005-06-06+02:00</gml:timePosition>
                              </gml:TimeInstant>
                         </om:time>
                         <om:procedure xlink:href="dict_procedure.xml#wq-table"/>
                         <om:observedProperty xlink:href="dict_phenomenon.xml#wq-table"/>
                         <om:featureOfInterest xlink:href="#pegelstation"/>
                         <om:resultDefinition xlink:href="dict_resultdef.xml#wq-table"/>
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
]]>
                         </om:result>
                    </om:Observation>
               </om:member>

               <!-- es können noch mehr wq_table Observation gelistet sein,
               je nach Gültigkeitszeitraum -->

               <om:member>
                    <om:Observation gml:id="wq_wechmann1">

                         <!-- als alternativ zur wq_table könnte man eine Wechmann
                         Funktion hiermit abbilden
                         
                         die observedProperty (hier wq-wechmann im phenomenon dictionary) sagt 
                         was das für eine Observation ist: nämlich eine Wechmann-Funktion Observation
                         mit der entsprechende resultDefinition usw. 
                         -->
                         <om:time>
                              <!-- in time könnte der Gültigkeitsbereich der WQ-Beziehung
                                   kodiert sein -->
                              <gml:TimeInstant>
                                   <gml:timePosition>2005-06-06+02:00</gml:timePosition>
                              </gml:TimeInstant>
                         </om:time>
                         <om:procedure xlink:href="dict_procedure.xml#wq-wechmann"/>
                         <om:observedProperty xlink:href="dict_phenomenon.xml#wq-wechmann"/>
                         <om:featureOfInterest xlink:href="#pegelstation"/>
                         <om:resultDefinition xlink:href="dict_resultdef.xml#wq-wechmann"/>
                         <om:result>TODO</om:result>
                    </om:Observation>
               </om:member>

               <!-- es können noch mehr wq_wechmann Observation gelistet sein,
               je nach Gültigkeitszeitraum -->

               <!-- jetzt kommen die Alarmstufen, als einzelne Measurement -->
               <om:member>
                    <om:Measurement>

                         <!-- die observedProperty (hier alarmstufe im phenomenon dictionary) sagt 
                              was das für eine Observation ist.
                         -->

                         <gml:name>Alarmstufe 1</gml:name>
                         <om:time/>
                         <om:procedure xlink:href="dict_procedure.xml#alarmstufeneinteilung"/>
                         <om:observedProperty xlink:href="dict_phenomenon.xml#alarmstufe"/>
                         <om:featureOfInterest xlink:href="#pegelstation"/>
                         <om:result uom="dict_uom.xml#cmaP">20</om:result>
                    </om:Measurement>
               </om:member>
               <om:member>
                    <om:Measurement>
                         <gml:name>Alarmstufe 2</gml:name>
                         <om:time/>
                         <om:procedure xlink:href="dict_procedure.xml#alarmstufeneinteilung"/>
                         <om:observedProperty xlink:href="dict_phenomenon.xml#alarmstufe"/>
                         <om:featureOfInterest xlink:href="#pegelstation"/>
                         <om:result uom="dict_uom.xml#cmaP">30</om:result>
                    </om:Measurement>
               </om:member>
               <om:member>
                    <om:Measurement>
                         <gml:name>Alarmstufe 3</gml:name>
                         <om:time/>
                         <om:procedure xlink:href="dict_procedure.xml#alarmstufeneinteilung"/>
                         <om:observedProperty xlink:href="dict_phenomenon.xml#alarmstufe"/>
                         <om:featureOfInterest xlink:href="#pegelstation"/>
                         <om:result uom="dict_uom.xml#cmaP">40</om:result>
                    </om:Measurement>
               </om:member>
               <om:member>
                    <om:Measurement>
                         <gml:name>Alarmstufe 4</gml:name>
                         <om:time/>
                         <om:procedure xlink:href="dict_procedure.xml#alarmstufeneinteilung"/>
                         <om:observedProperty xlink:href="dict_phenomenon.xml#alarmstufe"/>
                         <om:featureOfInterest xlink:href="#pegelstation"/>
                         <om:result uom="dict_uom.xml#cmaP">50</om:result>
                    </om:Measurement>
               </om:member>
          </om:ObservationCollection>
     </gml:member>

     <gml:member>

          <!-- sollte die FeatureOfInterest woanders definiert sein (z.B. modell GML o.ä.)
          dann bräuchte man kein Bag mehr, sondern die ObservationCollection wurde direkt als
          root-Element dieses XML dienen.
          -->

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
               <om:procedureHosted xlink:href="#wasserstandsmessung"/>
          </om:Station>
     </gml:member>
</gml:Bag>
