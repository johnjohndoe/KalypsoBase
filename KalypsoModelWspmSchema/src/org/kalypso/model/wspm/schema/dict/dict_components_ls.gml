<?xml version="1.0" encoding="UTF-8" ?>
<gml:Dictionary xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
     xmlns:st="http://www.seegrid.csiro.au/xml/st" xmlns:xlink="http://www.w3.org/1999/xlink"
     xmlns:gml="http://www.opengis.net/gml" xmlns:om="http://www.opengis.net/om"
     xmlns:swe="http://www.opengis.net/swe" gml:id="components">

     <gml:description>Ein Dictionary für die Modellierung von Zeitreihendaten</gml:description>
     <gml:name>Zeitreihenkomponente</gml:name>

     <!-- Length Section components -->
     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSectionStation">
               <gml:name>%LengthSectionStation.item.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-stat">
                         <gml:description>%phen-stat.description</gml:description>
                         <gml:name>%phen-stat.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="4"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#km"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

    <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSectionText">
               <gml:name>%LengthSectionText_name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-text">
                         <gml:description>%LengthSectionText_description</gml:description>
                         <gml:name>%phen-text_name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:Word>
                         <swe:classification/>
                    </swe:Word>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSectionRunOff">
               <gml:name>%LengthSectionRunOff.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen_runoff">
                         <gml:description>%phen_runoff.description</gml:description>
                         <gml:name>%phen_runoff.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m³/s"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSectionWaterlevel">
               <gml:name>%LengthSectionWaterlevel.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen_waterlevel">
                         <gml:description>%phen_waterlevel.description</gml:description>
                         <gml:name>%phen_waterlevel.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="2"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#mNN"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSectionProfileType">
               <gml:name>%LengthSectionProfileType.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-kenn">
                         <gml:name>%phen-kenn.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:Word>
                         <swe:restriction>
                              <st:pattern value="n|b|w|t|k|e|m|i"/>
                         </swe:restriction>
                         <swe:classification>foo</swe:classification>
                    </swe:Word>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSectionGround">
               <gml:name>%LengthSectionGround.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-sohle">
                         <gml:name>%phen-sohle.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#mNN"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_hen">
               <gml:name>%LengthSection_hen.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-hen">
                         <gml:name>%phen-hen.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#mNN"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_h_BV">
               <gml:name>%LengthSection_h_BV.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-h_bv">
                         <gml:name>%phen-h_bv.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#mNN"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_Boe_li">
               <gml:name>%LengthSection_Boe_li.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-boe_li">
                         <gml:name>%phen-boe_li.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#mNN"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_Boe_re">
               <gml:name>%LengthSection_Boe_re.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-boe_re">
                         <gml:name>%phen-boe_re.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#mNN"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_v_m">
               <gml:name>%LengthSection_v_m.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-v_m">
                         <gml:name>%phen-v_m.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m/s"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_v_li">
               <gml:name>%LengthSection_v_li.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-v_li">
                         <gml:name>%phen-v_li.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m/s"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_v_fl">
               <gml:name>%LengthSection_v_fl.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-v_fl">
                         <gml:name>%phen-v_fl.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m/s"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_v_re">
               <gml:name>%LengthSection_v_re.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-v_re">
                         <gml:name>%phen-v_re.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m/s"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_tau_fl">
               <gml:name>%LengthSection_tau_fl.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-tau_fl">
                         <gml:name>%phen-tau_fl.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="2"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#N/m²"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

	<!-- Q -->
     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_Q_li">
               <gml:name>%LengthSection_Q_li.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-Q_li">
                         <gml:name>%phen-Q_li.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m³/s"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>
     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_Q_fl">
               <gml:name>%LengthSection_Q_fl.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-Q_fl">
                         <gml:name>%phen-Q_fl.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m³/s"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>
     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_Q_re">
               <gml:name>%LengthSection_Q_re.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-Q_re">
                         <gml:name>%phen-Q_re.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m³/s"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

	<!--  LAMBDA -->
     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_lamb_li">
               <gml:name>%LengthSection_lamb_li.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-lam_li">
                         <gml:name>%phen-lam_li.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="4"/>
                         </st:restriction>
                         <swe:noScale>true</swe:noScale>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_lamb_fl">
               <gml:name>%LengthSection_lamb_fl.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-lam_fl">
                         <gml:name>%phen-lam_fl.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="4"/>
                         </st:restriction>
                         <swe:noScale>true</swe:noScale>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_lamb_re">
               <gml:name>%LengthSection_lamb_re.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-lam_re">
                         <gml:name>%phen-lam_re.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="4"/>
                         </st:restriction>
                         <swe:noScale>true</swe:noScale>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_f_li">
               <gml:name>%LengthSection_f_li.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-fl_li">
                         <gml:name>%phen-fl_li.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m²"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_f_fl">
               <gml:name>%LengthSection_f_fl.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-f_fl">
                         <gml:name>%phen-f_fl.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m²"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_f_re">
               <gml:name>%LengthSection_f_re.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-f_re">
                         <gml:name>%phen-f_re.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m²"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_f">
               <gml:name>%LengthSection_f.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-f">
                         <gml:name>%phen-f.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m²"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>
     
     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_br">
               <gml:name>%LengthSection_br.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-br">
                         <gml:name>%phen-br.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_br_li">
               <gml:name>%LengthSection_br_li.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-br_li">
                         <gml:name>%phen-br_li.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_br_fl">
               <gml:name>%LengthSection_br_fl.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-br_fl">
                         <gml:name>%phen-br_fl.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_br_re">
               <gml:name>%LengthSection_br_re.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-br_re">
                         <gml:name>%phen-br_re.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0.0"/>
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_WeirOK">
               <gml:name>%LengthSection_WeirOK.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-wehrok">
                         <gml:name>%phen-wehrok.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#mNN"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_BridgeOK">
               <gml:name>%LengthSection_BridgeOK.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-brueckok">
                         <gml:name>%phen-brueckok.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#mNN"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_BridgeUK">
               <gml:name>%LengthSection_BridgeUK.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-brueckuk">
                         <gml:name>%phen-brueckuk.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#mNN"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_BridgeWidth">
               <gml:name>%LengthSection_BridgeWidth.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-brueckb">
                         <gml:name>%phen-brueckb.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0"/>
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_RohrDN">
               <gml:name>%LengthSection_RohrDN.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-rohrdn">
                         <gml:name>%phen-rohrdn.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0"/>
                              <st:fractionDigits value="3"/>
                         </st:restriction>
                         <gml:unitOfMeasure uom="dict_uom.xml#m"/>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <!-- ALPHA -->
     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_AlphaIW">
               <gml:name>%LengthSection_AlphaIW.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-alphaIW">
                         <gml:name>%phen-alphaIW.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0"/>
                              <st:fractionDigits value="5"/>
                         </st:restriction>
                         <swe:noScale>true</swe:noScale>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_AlphaEW">
               <gml:name>%LengthSection_AlphaEW.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-alphaEW">
                         <gml:name>%phen-alphaEW.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:minInclusive value="0"/>
                              <st:fractionDigits value="5"/>
                         </st:restriction>
                         <swe:noScale>true</swe:noScale>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <!-- I_REIB -->
     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_I_Reib">
               <gml:name>%LengthSection_I_Reib.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-I_Reib">
                         <gml:name>%phen-I_Reib.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="5"/>
                         </st:restriction>
                         <swe:noScale>true</swe:noScale>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

     <gml:dictionaryEntry>
          <swe:ItemDefinition gml:id="LengthSection_froude">
               <gml:name>%LengthSection_froude.name</gml:name>
               <swe:property>
                    <swe:Phenomenon gml:id="phen-roude">
                         <gml:name>%phen-froude.name</gml:name>
                    </swe:Phenomenon>
               </swe:property>
               <swe:representation>
                    <swe:SimpleType>
                         <st:restriction base="decimal">
                              <st:fractionDigits value="2"/>
                         </st:restriction>
                    </swe:SimpleType>
               </swe:representation>
          </swe:ItemDefinition>
     </gml:dictionaryEntry>

</gml:Dictionary>
