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
			<title>WS-I Attachments Sample Application 1.0 EA - Retailer Results</title>
		</head>
		<body BGCOLOR="white">
			<xsl:apply-templates/>
		</body>
	</html>
</xsl:template>

<xsl:template match="all-orders">
	<H1 align="center">WS-I Attachments Sample Application 1.0 EA</H1>
	<H1 align="center">Retailer Results</H1>
	<xsl:apply-templates select="environment"/>
	<p/><p/>
	<ol>
		<xsl:for-each select="orders">
			<li>
				<a href="#{generate-id(.)}"><xsl:value-of select="configuration/@vendor"/></a> 
			</li>
		</xsl:for-each>
	</ol>
	<xsl:apply-templates select="orders"/>
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

<xsl:template match="orders">
	<h2><a name="{generate-id(.)}"><xsl:value-of select="orders"/></a> <xsl:number/> (<xsl:value-of select="configuration/@vendor"/>)</h2>
	<ol>
		<xsl:for-each select="order">
			<li> <a href="#{generate-id(.)}">Order<xsl:value-of select="@id"/></a> </li>
		</xsl:for-each>
	</ol>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="configuration">
	<h3>Endpoints</h3>
	<table border="1">
		<xsl:apply-templates/>
	</table>
</xsl:template>

<xsl:template match="serviceURL">
	<tr>
		<td> <xsl:value-of select="@name"/> </td>
		<td> <xsl:value-of select="."/> </td>
	</tr>
</xsl:template>

<xsl:template match="order">
	<h3> <a name="{generate-id(.)}"><xsl:value-of select="order"/></a>Order<xsl:value-of select="@id"/> </h3>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="request">
	<h4>Order Request</h4>
	<table border="1">
		<tr>
			<td bgcolor="#DCD887"> <font size="+1">Product Number </font></td>
			<td bgcolor="#DCD887"> <font size="+1">Quantity </font></td>
		</tr>
		<xsl:apply-templates/>
	</table><p/>
</xsl:template>

<xsl:template match="request-item">
	<tr>
		<td> <xsl:value-of select="product"/> </td>
		<td> <xsl:value-of select="quantity"/> </td>
	</tr>
</xsl:template>

<xsl:template match="response">
	<h4>Order Confirmation</h4>
	<table border="1">
		<tr>
			<td bgcolor="#DCD887"> <font size="+1">Product Number </font></td>
			<td bgcolor="#DCD887"> <font size="+1">Quantity </font></td>
			<td bgcolor="#DCD887"> <font size="+1">Price </font></td>
			<td bgcolor="#DCD887"> <font size="+1">Comment </font></td>
		</tr>
		<xsl:apply-templates/>
	</table><p/>
</xsl:template>

<xsl:template match="response-item">
	<tr>
		<td> <xsl:value-of select="product"/> </td>
		<td> <xsl:value-of select="quantity"/> </td>
		<td> <xsl:text>$</xsl:text><xsl:value-of select="price"/> </td>
		<td> <xsl:value-of select="comment"/> </td>
	</tr>
</xsl:template>

<xsl:template match="logs">
	<h4>Log Entries (<xsl:value-of select="count(log-item)"/>)</h4>
	<table border="1" width="100%">
		<tr>
			<!--
			<td bgcolor="#DCD887"> Timestamp </td>
			-->
			<td bgcolor="#DCD887"> <font size="+1">EventID </font></td>
			<td bgcolor="#DCD887"> <font size="+1">ServiceID </font></td>
			<td bgcolor="#DCD887"> <font size="+1">Event Description </font></td>
		</tr>
		<xsl:apply-templates/>
	</table>
</xsl:template>

<xsl:template match="log-item">
	<tr>
		<!--
		<td> <xsl:value-of select="timestamp"/> </td>
		-->
		<td> <xsl:value-of select="event-id"/> </td>
		<td> <xsl:value-of select="service-id"/> </td>
		<td> <xsl:value-of select="description"/> </td>
	</tr>
</xsl:template>

<xsl:template match="error">
	<h3>Error message</h3>
	<xsl:apply-templates/>
</xsl:template>

<xsl:template match="error-message">
	<xsl:value-of select="."/><br/>
</xsl:template>

<xsl:template match="catalog">
	<table border="1">
		<tr>
			<td bgcolor="#DCD887"> <font size="+1">Product Number </font></td>
			<td bgcolor="#DCD887"> <font size="+1">Name </font></td>
			<td bgcolor="#DCD887"> <font size="+1">Price </font></td>
			<td bgcolor="#DCD887"> <font size="+1">Description </font></td>
		</tr>
		<xsl:apply-templates/>
	</table>
</xsl:template>

<xsl:template match="catalog-item">
	<tr>
	<td> <xsl:value-of select="product"/> </td>
	<td> <xsl:value-of select="name"/> </td>
	<td> <xsl:value-of select="price"/> </td>
	<td> <xsl:value-of select="description"/> </td>
	</tr>
</xsl:template>

</xsl:stylesheet>

