<?xml version="1.0" encoding="UTF-8" ?>
<gml:Dictionary xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="
     http://www.opengis.net/gml http://dev.bjoernsen.de/ogc/schema/gml/3.1.1/base/gml.xsd
     http://www.opengis.net/swe http://dev.bjoernsen.de/ogc/schema/sweCommon/1.0.30/swe.xsd
     http://www.w3.org/1999/xlink http://dev.bjoernsen.de/ogc/schema/gml/3.1.1/xlink/xlinks.xsd"
     xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:gml="http://www.opengis.net/gml"
     xmlns:swe="http://www.opengis.net/swe" gml:id="resultdefinitions">

     <gml:name>Allgemeine KALYPSO Resultdefinitions</gml:name>

     <gml:dictionaryEntry>
          <swe:GridDefinition dimension="2" gml:id="wq-table">
               <gml:name>WQ-Tabelle Datenblock Spezifikation</gml:name>
               <swe:map>
                    <!-- map kann eine href  enthalten - der IndexArray sollte irgendwie ausgelager werden-->
                    <swe:IndexArray gml:id="wq_index" arrayLength="unbounded">
                         <gml:name/>
                    </swe:IndexArray>
               </swe:map>
               <swe:tupleMap>
                    <swe:RecordDefinition gml:id="wq_rd" recordLength="2">
                         <gml:name/>
                         <swe:component xlink:href="dict_components.xml#pegelhoehe"/> 
                         <swe:component xlink:href="dict_components.xml#abflussmenge"/> 
                    </swe:RecordDefinition>
               </swe:tupleMap>
          </swe:GridDefinition>
     </gml:dictionaryEntry>
     
     <gml:dictionaryEntry>
          <swe:GridDefinition dimension="4" gml:id="wq-wechmann">
               <gml:name>Wechmann-Funktion Datenblock Spezifikation</gml:name>
               <swe:map>
                    <!-- map kann eine href  enthalten - der IndexArray sollte irgendwie ausgelager werden-->
                    <swe:IndexArray gml:id="wechmann_index" arrayLength="unbounded">
                         <gml:name/>
                    </swe:IndexArray>
               </swe:map>
               <swe:tupleMap>
                    <swe:RecordDefinition gml:id="wechmann_rd" recordLength="4">
                         <gml:name/>
                         <swe:component xlink:href="dict_components.xml#wechmann_w1"/>
                         <swe:component xlink:href="dict_components.xml#wechmann_lnk1"/>
                         <swe:component xlink:href="dict_components.xml#wechmann_k2"/>
                         <swe:component xlink:href="dict_components.xml#wechmann_wgr"/>
                    </swe:RecordDefinition>
               </swe:tupleMap>
          </swe:GridDefinition>
     </gml:dictionaryEntry>

</gml:Dictionary>
