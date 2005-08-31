<?xml version="1.0" ?>

<!--
 Copyright (c) 2003 Sun Microsystems, Inc.
 All rights reserved. 
-->

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	version="1.0">

<xsl:output method="html" indent="yes"/>
<xsl:strip-space elements="description"/>

<xsl:template match="/">
	<html>
		<head>
			<title>WS-I Attachments Sample Application 1.0 EA - Catalog Results</title>
		</head>
		<body BGCOLOR="white">
			<xsl:apply-templates/>
		</body>
	</html>
</xsl:template>

<xsl:template match="catalog">
	<H1 align="center">WS-I Attachments Sample Application 1.0 EA</H1>
	<H1 align="center">Catalog Results</H1>
	<xsl:apply-templates select="environment"/>
	<xsl:apply-templates select="catalog-with-images"/>
	<xsl:apply-templates select="catalog-with-details"/>
</xsl:template>

<xsl:template match="environment">
	<table border="1" width="60%">
		<tr> <td width="40%"><font size="+1">Timestamp</font></td> <td width="60%"> <xsl:value-of select="timestamp"/></td> </tr>
		<tr> <td width="40%"><font size="+1">Runtime Name</font></td> <td width="60%"> <xsl:value-of select="runtime-name"/></td> </tr>
		<tr> <td width="40%"><font size="+1">Runtime Version</font></td> <td width="60%"> <xsl:value-of select="runtime-version"/></td> </tr>
		<tr> <td width="40%"><font size="+1">Operating System Name</font></td> <td width="60%"> <xsl:value-of select="os-name"/></td> </tr>
		<tr> <td width="40%"><font size="+1">Operating System Version</font></td> <td width="60%"> <xsl:value-of select="os-version"/></td> </tr>
	</table>
</xsl:template>

<xsl:template match="catalog-with-images">
	<p/><p/>
	<h2> Catalog with thumbnail images </h2>
	<table border="1" width="60%">
		<tr>
			<th>Number</th>
			<th>Name</th>
			<th>Description</th>
			<th>Category</th>
			<th>Brand</th>
			<th>Price</th>
			<th>Thumbnail</th>
		</tr>
		<xsl:apply-templates/>
	</table>
</xsl:template>

<xsl:template match="catalog-item">
	<xsl:variable name="image">
		<xsl:value-of select="thumbnail"/>
	</xsl:variable>
	<tr>
		<td valign="top"><xsl:value-of select="number"/></td>
		<td valign="top"><xsl:value-of select="name"/></td>
		<td valign="top"><xsl:value-of select="description"/></td>
		<td valign="top"><xsl:value-of select="category"/></td>
		<td valign="top"><xsl:value-of select="brand"/></td>
		<td valign="top"><xsl:value-of select="price"/></td>
		<td valign="top">
			<xsl:choose>
				<xsl:when test="number != '605010'">
					<xsl:value-of select="concat('&lt;img src=', '&#x22;', 'file:///', $image, '&#x22;', '/&gt;')" disable-output-escaping="yes"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>No image</xsl:text>
				</xsl:otherwise>
			</xsl:choose>
		</td>
	</tr>
</xsl:template>

<xsl:template match="catalog-with-details">
	<br/> <br/>
	<h2> Catalog Details </h2>
	<table border="1" width="60%">
		<tr>
			<th>Number</th>
			<th>Weight</th>
			<th>Weight Unit</th>
			<th>Width</th>
			<th>Height</th>
			<th>Depth</th>
			<th>Dimensions Unit</th>
			<th>Picture</th>
			<th>Specsheet</th>
		</tr>
		<xsl:apply-templates/>
	</table>
</xsl:template>

<xsl:template match="catalog-item-detail">
	<xsl:variable name="image">
		<xsl:value-of select="picture"/>
	</xsl:variable>
	<xsl:variable name="specsheet">
		<xsl:value-of select="specsheet"/>
	</xsl:variable>
	<tr>
		<td valign="top"><xsl:value-of select="number"/></td>
		<td valign="top"><xsl:value-of select="weight"/></td>
		<td valign="top"><xsl:value-of select="weight-unit"/></td>
		<td valign="top"><xsl:value-of select="width"/></td>
		<td valign="top"><xsl:value-of select="height"/></td>
		<td valign="top"><xsl:value-of select="depth"/></td>
		<td valign="top"><xsl:value-of select="dimensions-unit"/></td>
		<td valign="top">
			<xsl:value-of select="concat('&lt;img src=', '&#x22;', 'file:///', $image, '&#x22;', '/&gt;')" disable-output-escaping="yes"/>
		</td>
		<td>
			<xsl:value-of select="concat('&lt;a href=file://', $specsheet, '/&gt;here&lt;/a&gt;')" disable-output-escaping="yes"/>
		</td>
	</tr>
</xsl:template>

</xsl:stylesheet>

