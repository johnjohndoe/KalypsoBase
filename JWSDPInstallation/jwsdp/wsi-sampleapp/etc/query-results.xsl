<?xml version="1.0" ?>

<!--
 Copyright (c) 2003 Sun Microsystems, Inc.
 All rights reserved. 
-->

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:orderResults="http://java.sun.com/xml/ns/jax-rpc/wsi/order/results"
	xmlns:vendorConfig="http://java.sun.com/xml/ns/jax-rpc/wsi/vendor/config"
	version="1.0">

<xsl:output method="html" indent="yes"/>
<xsl:strip-space elements="description"/>
<xsl:variable name="filler">&#xa0;</xsl:variable>

<xsl:template match="/">
	<html>
		<head>
			<title>WS-I Attachments Sample Application 1.0 EA - Query Results</title>
		</head>
		<body BGCOLOR="white">
			<xsl:apply-templates/>
		</body>
	</html>
</xsl:template>

<xsl:template match="query">
	<H1 align="center">WS-I Attachments Sample Application 1.0 EA</H1>
	<H1 align="center">Query Results</H1>
	<xsl:apply-templates select="environment"/>
	<p/><p/>
	<table border="1" width="100%">
		<tr>
			<td width="30%"><font size="+1">Name</font></td>
			<td width="30%"><font size="+1">Endpoint</font></td>
			<td width="15%"><font size="+1">Role</font></td>
			<td width="25%"><font size="+1">Params</font></td>
		</tr>
		<xsl:apply-templates select="service"/>
	</table>
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

<xsl:template match="service">
	<tr>
		<td width="30%"><xsl:value-of select="name"/></td>
		<td width="30%"><xsl:value-of select="endpoint"/></td>
		<td width="15%"><xsl:value-of select="role"/></td>
		<td width="25%"><xsl:value-of select="params"/></td>
	</tr>
</xsl:template>

</xsl:stylesheet>

