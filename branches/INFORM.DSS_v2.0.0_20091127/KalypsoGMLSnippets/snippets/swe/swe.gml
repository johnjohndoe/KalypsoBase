<?xml version="1.0" encoding="WINDOWS-1252"?>
<swe:ItemDefinition xmlns:st="http://www.seegrid.csiro.au/xml/st" 
					xmlns:xlink="http://www.w3.org/1999/xlink" 
					xmlns:gml="http://www.opengis.net/gml" 
					xmlns:xs="http://www.w3.org/2001/XMLSchema-instance" 
					xmlns:swe="http://www.opengis.net/swe" 
					gml:id="root">
   <gml:name>Station</gml:name>
   <swe:property>
        <swe:Phenomenon gml:id="phen-stat">
             <gml:description>Gewässerstationierung</gml:description>
             <gml:name>Stationierung</gml:name>
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
