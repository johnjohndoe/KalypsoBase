<?xml version="1.0" encoding="UTF-8" ?>
<gml:Dictionary xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="
     http://www.opengis.net/om http://dev.bjoernsen.de/ogc/schema/om/1.0.30/om.xsd
     http://www.opengis.net/gml http://dev.bjoernsen.de/ogc/schema/gml/3.1.1/base/gml.xsd
     http://www.opengis.net/swe http://dev.bjoernsen.de/ogc/schema/sweCommon/1.0.30/swe.xsd
     http://www.seegrid.csiro.au/xml/st http://dev.bjoernsen.de/ogc/schema/sweCommon/1.0.30/simpleTypeDerivation.xsd
     http://www.w3.org/1999/xlink http://dev.bjoernsen.de/ogc/schema/gml/3.1.1/xlink/xlinks.xsd"
  xmlns:xst="http://www.seegrid.csiro.au/xml/st" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml" xmlns:om="http://www.opengis.net/om" xmlns:swe="http://www.opengis.net/swe" gml:id="components">
  <!-- FIXME: we should reference the (existing) Kalypso phenomena... -->
  <gml:description>Dictionary for profile-observation components. Subtype 'profil-point-property'.</gml:description>
  <gml:name>Profile Point Component Dictionary</gml:name>
  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="BREITE">
      <gml:name>%breiteItemName</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Breite">
          <gml:description />
          <gml:name>Breite</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:SimpleType>
          <xst:restriction base="double">
            <xst:fractionDigits value="4" />
          </xst:restriction>
          <gml:unitOfMeasure uom="dict_uom.xml#m" />
        </swe:SimpleType>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>
  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="HOEHE">
      <gml:name>%hoeheItemName</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Hoehe">
          <gml:description />
          <gml:name>Höhe</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:Number>
          <gml:unitOfMeasure uom="dict_uom.xml#mNN" />
        </swe:Number>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>
  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="RAUHEIT">
      <gml:name>%rauheit.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Rauheit">
          <gml:description />
          <gml:name>%rauheit.name</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:Number>
          <gml:unitOfMeasure uom="" />
        </swe:Number>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>
  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="RAUHEIT_KST">
      <gml:name>%rauheit_kst.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Rauheit_kst">
          <gml:description />
          <gml:name>%rauheit_kst.name</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:Number>
          <gml:unitOfMeasure uom="" />
        </swe:Number>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>
  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="HOCHWERT">
      <gml:name>%hochwertItemName</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Hochwert">
          <gml:description />
          <gml:name>Hochwert</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:Number>
          <gml:unitOfMeasure uom="dict_uom.xml#m" />
        </swe:Number>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>
  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="RECHTSWERT">
      <gml:name>%rechtswertItemName</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Rechtswert">
          <gml:description />
          <gml:name>Rechtswert</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:Number>
          <gml:unitOfMeasure uom="dict_uom.xml#m" />
        </swe:Number>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>
  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="BEWUCHS_AX">
      <gml:name>AX</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_BewuchsAX">
          <gml:description />
          <gml:name>AX</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:Number>
          <gml:unitOfMeasure uom="dict_uom.xml#m" />
        </swe:Number>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>
  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="BEWUCHS_AY">
      <gml:name>AY</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_BewuchsAY">
          <gml:description />
          <gml:name>AY</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:Number>
          <gml:unitOfMeasure uom="dict_uom.xml#m" />
        </swe:Number>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>
  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="BEWUCHS_DP">
      <gml:name>DP</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_BewuchsDP">
          <gml:description />
          <gml:name>DP</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:Number>
          <gml:unitOfMeasure uom="dict_uom.xml#m" />
        </swe:Number>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>
  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="UNTERKANTEBRUECKE">
      <gml:name>%unterkantebruecke.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Brueckenunterkante">
          <gml:description />
          <gml:name>%unterkantebruecke.name</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:Number>
          <gml:unitOfMeasure uom="dict_uom.xml#mNN" />
        </swe:Number>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>
  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="OBERKANTEBRUECKE">
      <gml:name>%oberkantebruecke.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Oberkantebruecke">
          <gml:description />
          <gml:name>%oberkantebruecke.name</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:Number>
          <gml:unitOfMeasure uom="dict_uom.xml#mNN" />
        </swe:Number>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>
  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="OBERKANTEWEHR">
      <gml:name>%oberkantewehr.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Wehr">
          <gml:description />
          <gml:name>%oberkantewehr.name</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:SimpleType>
          <xst:restriction base="double">
            <xst:fractionDigits value="4" />
          </xst:restriction>
          <gml:unitOfMeasure uom="dict_uom.xml#mNN" />
        </swe:SimpleType>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>
  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="COMMENT">
      <gml:name>%commentItemName</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Comment">
          <gml:description />
          <gml:name>%commentItemUnit</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:Word>
          <swe:classification />
        </swe:Word>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>
</gml:Dictionary>