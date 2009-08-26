/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 *
 *  and
 *
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Contact:
 *
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.commons.xml;

/**
 * Global XML Namespace Constants
 * 
 * @author doemming
 */
public interface NS
{
  // the 'XML-Schema' schema:
  public final static String XSD_SCHEMA = "http://www.w3.org/2001/XMLSchema"; //$NON-NLS-1$

  public final static String XSD = "http://www.w3.org/2001/XMLSchema-instance"; //$NON-NLS-1$

  /**
   * Namespace for kalypso appinfo elements inside gml schemas.
   */
  public final static String KALYPSO_APPINFO = "org.kalypso.appinfo"; //$NON-NLS-1$

  /**
   * namespace from namespacedefinitions e.g. xmlns:app="foobar"
   */
  public final static String XML_PREFIX_DEFINITION_XMLNS = "http://www.w3.org/2000/xmlns/"; //$NON-NLS-1$

  public final static String XLINK = "http://www.w3.org/1999/xlink"; //$NON-NLS-1$

  public static final String GML2 = "http://www.opengis.net/gml"; //$NON-NLS-1$

  public static final String GML3 = GML2;

  public static final String SMIL20 = "http://www.w3.org/2001/SMIL20/"; //$NON-NLS-1$

  public static final String SMIL20LANG = "http://www.w3.org/2001/SMIL20/Language"; //$NON-NLS-1$

  public static final String WFS = "http://www.opengis.net/wfs"; //$NON-NLS-1$

  public static final String SWE = "http://www.opengis.net/swe"; //$NON-NLS-1$

  public static final String ST = "http://www.seegrid.csiro.au/xml/st"; //$NON-NLS-1$

  public static final String OM = "http://www.opengis.net/om"; //$NON-NLS-1$

  public static final String XST = "http://www.seegrid.csiro.au/xml/st"; //$NON-NLS-1$

  public static final String GMD = "http://www.isotc211.org/2005/gmd"; //$NON-NLS-1$

  public static final String KALYPSO_TEMPLATES = "types.template.kalypso.org"; //$NON-NLS-1$

  public static final String KALYPSO_MAPVIEW = "gismapview.template.kalypso.org"; //$NON-NLS-1$

  public static final String KALYPSO_OBSVIEW = "obsdiagview.template.kalypso.org"; //$NON-NLS-1$

  public static final String KALYPSO_OBSLINK = "obslink.zml.kalypso.org"; //$NON-NLS-1$

  /**
   * @deprecated Does not belong here, use constants from rrm plug-ins
   */
  @Deprecated
  public static final String KALYPSO_RRM = "http://www.tuhh.de/kalypsoNA"; //$NON-NLS-1$

  public static final String KALYPSO_OM = "http://www.ksp.org/om"; //$NON-NLS-1$

  public static final String CATALOG = "urn:oasis:names:tc:entity:xmlns:xml:catalog"; //$NON-NLS-1$

  // used by GML-Application-Schemas from "Arbeitsgemeinschaft deutscher Vermesser" (ADV)
  public static final String ADV_NAS = "http://www.adv-online.de/nas"; //$NON-NLS-1$

  public static final String SLD = "http://www.opengis.net/sld"; //$NON-NLS-1$

  public static final String OGC = "http://www.opengis.net/ogc"; //$NON-NLS-1$

  public static final String COMMON = "org.kalypso.gml.common"; //$NON-NLS-1$

  public static final String COMMON_SHP = "org.kalypso.gml.common.shape"; //$NON-NLS-1$

  public static final String COMMON_MATH = "org.kalypso.gml.common.math"; //$NON-NLS-1$

  public static final String SWE_EXTENSIONS = "org.kalypso.swe.ext"; //$NON-NLS-1$

  public static final String COMMON_MATHRANGES = "org.kalypso.gml.common.math.ranges"; //$NON-NLS-1$

  public static final String COMMON_COVERAGE = "org.kalypso.gml.common.coverage"; //$NON-NLS-1$
}
