<?xml version='1.0'?>
<xsl:stylesheet  
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
    xmlns:fo="http://www.w3.org/1999/XSL/Format"
    version="1.0"> 

<xsl:import href="docbook.xsl"/> 


<!-- <?custom-pagebreak?> inserts a page break at this point -->


<xsl:template match="processing-instruction('custom-pagebreak')">
  <fo:block break-before='page'/>
</xsl:template>

<xsl:template name="book.titlepage.separator">
</xsl:template>


<!-- table with colored head -->
<!-- 
<xsl:template match="thead">
    <fo:table-header background-color="#66CCFF">
      <xsl:apply-templates/>
    </fo:table-header>
</xsl:template>
-->  
   

<!--
 <xsl:template match="tbody">
   <fo:table-body>
     <xsl:apply-templates/>
   </fo:table-body>
 </xsl:template>
-->

<!--
<xsl:attribute-set name="header.content.properties">
  <xsl:attribute name="font-family">Helvetica</xsl:attribute>
  <xsl:attribute name="font-size">9pt</xsl:attribute>
</xsl:attribute-set>
-->
<xsl:attribute-set name="header.content.properties">
  <xsl:attribute name="font-family">
    <xsl:value-of select="$body.fontset"></xsl:value-of>
  </xsl:attribute>
  <xsl:attribute name="margin-left">
    <xsl:value-of select="$title.margin.left"></xsl:value-of>
  </xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="section.title.level1.properties">
  <xsl:attribute name="font-size">
    <xsl:value-of select="$body.font.master * 1.8"/>
    <xsl:text>pt</xsl:text>
  </xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="section.title.level2.properties">
  <xsl:attribute name="font-size">12pt</xsl:attribute>
</xsl:attribute-set>


<!--
<xsl:attribute-set name="footer.content.properties">
  <xsl:attribute name="font-family">Helvetica</xsl:attribute>
  <xsl:attribute name="font-size">9pt</xsl:attribute>
</xsl:attribute-set>
-->

<!--
<xsl:attribute-set name="section.level1.properties">
  <xsl:attribute name="break-before">page</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="section.level2.properties">
  <xsl:attribute name="break-before">page</xsl:attribute>
</xsl:attribute-set>
-->

<!--colored and hyphenated links -->
<xsl:template match="ulink">
<fo:basic-link external-destination="{@url}"
        xsl:use-attribute-sets="xref.properties"
        text-decoration="underline"
        color="blue">
        <xsl:choose>
        <xsl:when test="count(child::node())=0">
        <xsl:value-of select="@url"/>
        </xsl:when>
        <xsl:otherwise>
        <xsl:apply-templates/>
        </xsl:otherwise>
        </xsl:choose>
        </fo:basic-link>
</xsl:template> 


<!--Annotation with background -->

<xsl:attribute-set name="admonition.properties">
<xsl:attribute name="background-color">#E0E0E0</xsl:attribute>
<xsl:attribute name="padding">0.1in</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="admonition.title.properties">
<xsl:attribute name="background-color">#E0E0E0</xsl:attribute>
<xsl:attribute name="padding">0.1in</xsl:attribute>
</xsl:attribute-set>


<!--Programlisting with border -->
<!--
<xsl:attribute-set name="monospace.verbatim.properties"
use-attribute-sets="verbatim.properties">
  <xsl:attribute name="font-family">
    <xsl:value-of select="$monospace.font.family"/>
  </xsl:attribute>
  <xsl:attribute name="font-size">
    <xsl:value-of select="$body.font.master * 0.9"/>
    <xsl:text>pt</xsl:text>
  </xsl:attribute>
  <xsl:attribute name="border-color">#0000FF</xsl:attribute>
  <xsl:attribute name="border-style">solid</xsl:attribute>
  <xsl:attribute name="border-width">heavy</xsl:attribute>
</xsl:attribute-set>
-->

<!--Programlisting with background -->

<xsl:attribute-set name="monospace.verbatim.properties"
use-attribute-sets="verbatim.properties">
  <xsl:attribute name="font-family">
    <xsl:value-of select="$monospace.font.family"/>
  </xsl:attribute>
  <xsl:attribute name="font-size">
    <xsl:value-of select="$body.font.master * 0.9"/>
    <xsl:text>pt</xsl:text>
  </xsl:attribute>
  <xsl:attribute name="background-color">#FFFFCC</xsl:attribute>
</xsl:attribute-set>

<xsl:attribute-set name="shade.verbatim.properties">
   border-color="thin black ridge"
   background-color="green"
</xsl:attribute-set>

<!--section title undelined -->
<!--
<xsl:attribute-set name="section.title.properties">
  <xsl:attribute name="border-bottom-style">solid</xsl:attribute>
</xsl:attribute-set>
-->


<!--
<xsl:template name="user.footer.content">
  <div class="footer.date">
    <xsl:value-of select="//bookinfo/date" />
  </div>
</xsl:template>
-->

<xsl:template match="lineannotation">
  <fo:inline font-style="italic">
    <xsl:call-template name="inline.charseq"/>
  </fo:inline>
  </xsl:template>






</xsl:stylesheet> 
