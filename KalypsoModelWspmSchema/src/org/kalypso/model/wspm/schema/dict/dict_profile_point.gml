<?xml version="1.0" encoding="UTF-8" ?>
<gml:Dictionary xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="
     http://www.opengis.net/om http://dev.bjoernsen.de/ogc/schema/om/1.0.30/om.xsd
     http://www.opengis.net/gml http://dev.bjoernsen.de/ogc/schema/gml/3.1.1/base/gml.xsd
     http://www.opengis.net/swe http://dev.bjoernsen.de/ogc/schema/sweCommon/1.0.30/swe.xsd
     http://www.seegrid.csiro.au/xml/st http://dev.bjoernsen.de/ogc/schema/sweCommon/1.0.30/simpleTypeDerivation.xsd
     http://www.w3.org/1999/xlink http://dev.bjoernsen.de/ogc/schema/gml/3.1.1/xlink/xlinks.xsd" xmlns:xst="http://www.seegrid.csiro.au/xml/st" xmlns:xlink="http://www.w3.org/1999/xlink" xmlns:gml="http://www.opengis.net/gml" xmlns:om="http://www.opengis.net/om" xmlns:swe="http://www.opengis.net/swe" gml:id="components">
  <!-- FIXME: we should reference the (existing) Kalypso phenomena... -->
  <gml:description>Dictionary for profile-observation components. Subtype 'profil-point-property'.</gml:description>
  <gml:name>Profile Point Component Dictionary</gml:name>
  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="BREITE">
      <gml:name>%breite.item.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Breite">
          <gml:description />
          <gml:name>%breite.item.name</gml:name>
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
      <gml:name>%hoehe.item.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Hoehe">
          <gml:description />
          <gml:name>%hoehe.item.name</gml:name>
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
      <gml:name>%rauheit.item.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Rauheit">
          <gml:description />
          <gml:name>%rauheit.item.name</gml:name>
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
      <gml:name>%rauheit_kst.item.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Rauheit_kst">
          <gml:description />
          <gml:name>%rauheit_kst.item.name</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:Number>
          <gml:unitOfMeasure uom="dict_uom.xml#m^(1/3)/s" />
        </swe:Number>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>

  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="RAUHEIT_CLASS">
      <gml:name>%rauheit_class.item.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Rauheit_class">
          <gml:description />
          <gml:name>%rauheit_class.item.name</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:Word>
          <swe:classification />
        </swe:Word>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>
  
  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="RAUHEIT_FACTOR">
      <gml:name>%rauheit_factor.item.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Rauheit">
          <gml:description />
          <gml:name>%rauheit_factor.item.name</gml:name>
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
      <gml:name>%hochwert.item.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Hochwert">
          <gml:description />
          <gml:name>%hochwert.item.name</gml:name>
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
      <gml:name>%rechtswert.item.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Rechtswert">
          <gml:description />
          <gml:name>%rechtswert.item.name</gml:name>
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
    <swe:ItemDefinition gml:id="BEWUCHS_CLASS">
      <gml:name>%bewuchs_class.item.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Bewuchs_class">
          <gml:description />
          <gml:name>%bewuchs_class.item.name</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:Word>
          <swe:classification />
        </swe:Word>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>

  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="UNTERKANTEBRUECKE">
      <gml:name>%unterkantebruecke.item.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Brueckenunterkante">
          <gml:description />
          <gml:name>%unterkantebruecke.item.name</gml:name>
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
      <gml:name>%oberkantebruecke.item.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Oberkantebruecke">
          <gml:description />
          <gml:name>%oberkantebruecke.item.name</gml:name>
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
      <gml:name>%oberkantewehr.item.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Wehr">
          <gml:description />
          <gml:name>%oberkantewehr.item.name</gml:name>
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
      <gml:name>%comment.item.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Comment">
          <gml:description />
          <gml:name>%comment.item.name</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:Word>
          <swe:classification />
        </swe:Word>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>

  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="CODE">
      <gml:name>%code.item.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Code">
          <gml:description />
          <gml:name>%code.item.name</gml:name>
        </swe:Phenomenon>
      </swe:property>
      <swe:representation>
        <swe:Word>
          <swe:classification />
        </swe:Word>
      </swe:representation>
    </swe:ItemDefinition>
  </gml:dictionaryEntry>

  <gml:dictionaryEntry>
    <swe:ItemDefinition gml:id="ID">
      <gml:name>%id.item.name</gml:name>
      <swe:property>
        <swe:Phenomenon gml:id="Phenomenon_Name">
          <gml:description />
          <gml:name>%id.item.name</gml:name>
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