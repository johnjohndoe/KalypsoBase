<?xml version="1.0" encoding="UTF-8" ?>
  <!--
    ========= FILE HEADER KALYPSO ============= * * This file is part of kalypso. * http://www.kalypso.wb.tu-harburg.de/
    * http://ibpm.bjoernsen.de/kalypso/ * * Copyright (C) 2006 by: * * Technical University Hamburg-Harburg (TUHH) *
    Institute of River and coastal engineering * Denickestraße 22 * 21073 Hamburg, Germany * http://www.tuhh.de/wb * *
    and * * Bjoernsen Consulting Engineers (BCE) * Maria Trost 3 * 56070 Koblenz, Germany * http://www.bjoernsen.de * *
    This library is free software; you can redistribute it and/or * modify it under the terms of the GNU Lesser General
    Public * License as published by the Free Software Foundation; either * version 2.1 of the License, or (at your
    option) any later version. * * This library is distributed in the hope that it will be useful, * but WITHOUT ANY
    WARRANTY; without even the implied warranty of * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU *
    Lesser General Public License for more details. * * You should have received a copy of the GNU Lesser General Public
    * License along with this library; if not, write to the Free Software * Foundation, Inc., 59 Temple Place, Suite
    330, Boston, MA 02111-1307 USA * * Contact: * * E-Mail: * belger@bjoernsen.de * schlienger@bjoernsen.de *
    v.doemming@tuhh.de * * * ====================================================== author v.doemming@tuhh.de xsl
    transformation to prepare ogc schemas for jax-binding, generates binding customization into schemas to resolve
    conflicts
  -->
<stylesheet xmlns="http://www.w3.org/1999/XSL/Transform" extension-element-prefixes="gml"
  xmlns:gml="http://www.opengis.net/gml" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns:jxb="http://java.sun.com/xml/ns/jaxb"
  version="1.0"
>
  <output method='xml' indent='yes' media-type="text/xml" encoding="UTF-8" />

  <!--
    tricky: copy root element including all namespace declarations, problem was: if you look from a xml perspective some
    namespace declarations are not used and so xsl will not generate them into the target document, but if you look from
    a xml-schema perspective they are really essential and must not get lost.
  -->
  <template match='/xsd:schema'>
    <copy>
      <for-each select="@*">
        <attribute name="{name()}">
					<value-of select='.' />
				</attribute>
      </for-each>
      <attribute name="jxb:version">1.0</attribute>
      <apply-templates select="/xsd:schema/*" />
    </copy>
  </template>

  <template match='*'>
    <element name="{name()}">
      <for-each select="@*">
        <attribute name="{name()}">
					<value-of select='.' />
				</attribute>
      </for-each>
      <variable name="className" select="@name" />

      <variable name="customizeElement"
        select="boolean(name()='element'
								 and name(../.)='schema'
								 and $className!=''
								 and contains('_abcdefghijklmnopqrstuvwxyz', substring($className,1,1)))" />

      <!-- is global element tag and name starts lowercase or '_' -->
      <if test="$customizeElement">
        <!-- generate a annotation -->
        <element name="xsd:annotation">
          <!-- copy original annotation if there was any -->
          <apply-templates select="./xsd:annotation/*" />
          <call-template name="makeAppinfo">
            <with-param name="className" select="$className" />
          </call-template>
        </element>
        <apply-templates select="./*[not(name()='annotation')]" />
      </if>
      <if test="not($customizeElement)">
        <apply-templates />
      </if>
    </element>
  </template>


  <template name="makeAppinfo">
    <param name="className" />
    <element name="xsd:appinfo">
      <element name="jxb:class">
        <choose>
          <when test="substring($className,1,1)='_'">
            <attribute name="name">
							<value-of select="$className" />2</attribute>
          </when>
          <otherwise>
            <variable name="duplicate">
              <!-- make first character uppercase -->
              <value-of select="translate(substring($className,1,1),'abcdefghijklmnopqrstuvwxyz','ABCDEFGHIJKLMNOPQRSTUVWXYZ')" />
              <value-of select="substring($className,2)" />
            </variable>
            <choose>
              <when test="/xsd:schema/xsd:element/@name=$duplicate or /xsd:schema/xsd:complexType/@name=$duplicate">
                <attribute name="name">
									<value-of select="$className" />2</attribute>
              </when>
              <otherwise>
                <attribute name="name">
									<value-of select="$duplicate" /></attribute>
              </otherwise>
            </choose>
          </otherwise>
        </choose>
      </element>
    </element>
  </template>

  <template match='text()'>
    <value-of select='.' />
  </template>

  <template match='comment()'>
    <comment>
      <value-of select='.' />
    </comment>
  </template>

  <template match='processing-instruction()'>
    <processing-instruction name="{name()}">
      <value-of select='.' />
    </processing-instruction>
  </template>

</stylesheet>