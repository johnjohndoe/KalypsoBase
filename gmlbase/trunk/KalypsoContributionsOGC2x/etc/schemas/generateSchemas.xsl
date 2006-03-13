<?xml version="1.0" encoding="UTF-8" ?>
<!--
author doemming@tuhh.de

xsl transformation to prepare ogc schemas for jax-binding
generates binding customization into schemas to resolve conflicts
-->
<stylesheet xmlns="http://www.w3.org/1999/XSL/Transform"
extension-element-prefixes="gml http://www.opengis.net/gml"
 xmlns:abc="http://www.opengis.net/gml"   
 xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
 xmlns:jxb="http://java.sun.com/xml/ns/jaxb"

 version="1.0">
	<output method='xml' 
	indent='yes'
	media-type="text/xml"
	encoding="UTF-8" />

	<!--
	tricky: copy root element including all namespace declarations, allthought that ones 
	that are not used if you look from a xml perspective, but are essential from a xml-schema perspective
	-->
	<template match='/xsd:schema'>
		<copy>
			<for-each select="@*">
				<attribute name="{name()}">
					<value-of select='.'/>
				</attribute>
			</for-each>
			<attribute name="jxb:version">1.0</attribute>
		    <apply-templates select="/xsd:schema/*"/>
	    </copy>
	</template>

<!--                   restriction
			<if test="name()='simpleType'
			 and $className!=''
			 and ./xsd:restriction/@base='string'"
			 >
-->
	<template match='*'  >
    	<element name="{name()}">
			<for-each select="@*">
				<attribute name="{name()}">
					<value-of select='.'/>
				</attribute>
			</for-each>
			<variable name="className" select="@name"/>

			<variable name="customizeSimpleType" 
				select="boolean(name()='simpleType'
			 					and $className!=''
			 					and ./xsd:restriction/@base='string')"/>
			<variable name="customizeElement" 
				select="boolean(name()='element'
								 and name(../.)='schema'
								 and $className!=''
								 and contains('_abcdefghijklmnopqrstuvwxyz', substring($className,1,1)))"/>

			<if test="$customizeSimpleType">
				<element name="xsd:annotation">
					<!-- copy original annotation if there was any -->
					<apply-templates select="./xsd:annotation/*"  />
					<element name="xsd:appinfo">
						<element name="jxb:javaType">
							<attribute name="name">java.lang.String</attribute>				
<!--
							<attribute name="parseMethod">fromString</attribute>				
							<attribute name="printMethod">toString</attribute>				
-->
						</element>
					</element>
				</element>
				<apply-templates select="./*[not(name()='annotation')]" />
			</if> 
			 <!--
			 parseMethod="fromString" printMethod="toString"
			 -->

			<!-- is global element tag and name starts lowercase or '_' -->
			<if test="$customizeElement">
				<!-- generate a annotation -->
				<element name="xsd:annotation">
					<!-- copy original annotation if there was any -->
					<apply-templates select="./xsd:annotation/*"  />
					<call-template name="makeAppinfo">
						<with-param name="className" select="$className"/>
					</call-template> 
				</element>
				<apply-templates select="./*[not(name()='annotation')]" />
			</if>
			<if test="not($customizeElement or $customizeSimpleType)">
				<apply-templates  />
			</if>
		</element>
	</template>
	<!--
			and not(name()='simpleType' and name(./xsd:annotation)='annotation'">
	-->
	
	
	<template name="makeAppinfo">
		<param name="className"/>
		<element name="xsd:appinfo">
			<element name="jxb:class">
				<choose>
					<when test="substring($className,1,1)='_'">
						<attribute name="name"><value-of select="$className"/>2</attribute>
					</when>
					<otherwise>
						<variable name="duplicate">
							<value-of select="translate(substring($className,1,1),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
							<value-of select="substring($className,2)"/>
						</variable>
						<if test="/xsd:schema/xsd:element/@name=$duplicate or /xsd:schema/xsd:complexType/@name=$duplicate">
							<attribute name="name"><value-of select="$className"/>2</attribute>
						</if>
					</otherwise>
				</choose>
			</element>
		</element>
	</template> 
	
	<template match='text()'  >
        <value-of select='.'/>
	</template>
	
	<template match='comment()'  >
        <comment>
                <value-of select='.'/>
        </comment>
	</template>
	
	<template match='processing-instruction()'  >
        <processing-instruction name="{name()}">
                <value-of select='.'/>
        </processing-instruction>
	</template>

</stylesheet>	