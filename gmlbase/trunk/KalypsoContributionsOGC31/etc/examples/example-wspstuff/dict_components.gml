<?xml version="1.0" encoding="UTF-8" ?>
<gml:Dictionary xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xsi:schemaLocation="
     http://www.opengis.net/om http://dev.bjoernsen.de/ogc/schema/om/1.0.30/om.xsd
     http://www.opengis.net/gml http://dev.bjoernsen.de/ogc/schema/gml/3.1.1/base/gml.xsd
     http://www.opengis.net/swe http://dev.bjoernsen.de/ogc/schema/sweCommon/1.0.30/swe.xsd
     http://www.seegrid.csiro.au/xml/st http://dev.bjoernsen.de/ogc/schema/sweCommon/1.0.30/simpleTypeDerivation.xsd
     http://www.w3.org/1999/xlink http://dev.bjoernsen.de/ogc/schema/gml/3.1.1/xlink/xlinks.xsd"
     xmlns:st="http://www.seegrid.csiro.au/xml/st" xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:gml="http://www.opengis.net/gml" xmlns:om="http://www.opengis.net/om"
     xmlns:swe="http://www.opengis.net/swe" gml:id="components">

     <gml:description>Ein Dictionary für die Modellierung von
     Längsschnitte</gml:description>
     <gml:name>LS-komponente</gml:name>


     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="stat">
               <gml:name>Station</gml:name>
               <swe:property/>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#km"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>
     
     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="kenn">
               <gml:name>Kennung</gml:name>
               <swe:property/>
               <swe:representation>
                    <swe:Word>
                         <swe:classification gml:remoteSchema="dict_kennung.xml"/>
                    </swe:Word>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="Sohle">
               <gml:name>Sohlhöhe</gml:name>
               <swe:property/>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="2"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#mNN"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>
     
     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="h_WSP">
               <gml:name>Höhe WSP</gml:name>
               <swe:property />
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="2"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="Boe_li">
               <gml:name>Böchung Links</gml:name>
               <swe:property />
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="2"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#mNN"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="Boe_re">
               <gml:name>Böchung Rechts</gml:name>
               <swe:property />
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="2"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#mNN"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>
     
     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="v_m">
               <gml:name>Mittlere Geschwindigkeit</gml:name>
               <swe:property />
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="2"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m/s"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>
     
     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="h_BV">
               <gml:name>Bordvoll Höhe</gml:name>
               <swe:property />
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="2"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#mNN"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>
     
<!-- TODO ergänzen für:

     hen               tau_fl lamb_li lamb_fl lamb_re      f_li      f_fl      f_re     br_li     br_fl     br_re    WehrOK  BrueckOK  BrueckUK   BrueckB    RohrDN
     mNN                N/m^2       -       -       -       m^2       m^2         m         m         m         m       mNN       mNN       mNN         m         m

 -->

</gml:Dictionary>
