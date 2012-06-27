/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.gml.schema.schemata;

import java.net.URL;
import java.util.Map;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;
import org.kalypso.contribs.java.net.AbstractUrlCatalog;
import org.kalypsodeegree_impl.gml.binding.shape.ShapeCollection;

/**
 * Dieser Katalog gibt fest verdrahtet die Schemata hier im Code zurück. Die gleichen Schemata (zumindest obslink)
 * werden auch fürs Binding benutzt und sind dadurch endlich wirklich nur noch einmal vorhanden.<br>
 * This catalog resolves some schemas that are related to Kalypso, but not to OGC.<br>
 * 
 * @author Gernot Belger
 */
public class DeegreeUrlCatalog extends AbstractUrlCatalog
{
  public final static String NS_UPDATE_OBSERVATION_MAPPING = "http://org.kalypso.updateObservationMapping"; //$NON-NLS-1$

  public static final QName QNAME_MAPPING_COLLECTION = new QName( NS_UPDATE_OBSERVATION_MAPPING, "MappingCollection" ); //$NON-NLS-1$

  public static final QName QNAME_MAPPING_OBSERVATION = new QName( NS_UPDATE_OBSERVATION_MAPPING, "MappingObservation" ); //$NON-NLS-1$

  public static final QName RESULT_LIST_PROP = new QName( NS_UPDATE_OBSERVATION_MAPPING, "mappingMember" ); //$NON-NLS-1$

  public static final QName RESULT_TS_IN_PROP = new QName( NS_UPDATE_OBSERVATION_MAPPING, "inObservationLink" ); //$NON-NLS-1$

  public static final QName RESULT_TS_OUT_PROP = new QName( NS_UPDATE_OBSERVATION_MAPPING, "outObservationLink" ); //$NON-NLS-1$

  public static final String PREFIX_OBSLINK = "obslink"; //$NON-NLS-1$

  public static final String NAMESPACE_ZML_OBSLINK = "obslink.zml.kalypso.org"; //$NON-NLS-1$

  public static final String NAMESPACE_ZML_INLINE = "inline.zml.kalypso.org"; //$NON-NLS-1$

  @Override
  protected void fillCatalog( final Class< ? > myClass, final Map<String, URL> catalog, final Map<String, String> prefixes )
  {
    // HACK: to retrieve the right schema locations for each version, we put pseudo-namespaces into
    // the catalog.
    // the normal gml namespace should now never be used.
    // if you have other needs, contact me. Gernot

    // GML - Version 2.1
    catalog.put( NS.GML2 + "#2", getClass().getResource( "gml2_2002/feature.xsd" ) ); //$NON-NLS-1$

    // OBSLINK
    catalog.put( NAMESPACE_ZML_OBSLINK, getClass().getResource( "obslink/obslink.xsd" ) ); //$NON-NLS-1$
    prefixes.put( NAMESPACE_ZML_OBSLINK, PREFIX_OBSLINK ); //$NON-NLS-1$

    catalog.put( NAMESPACE_ZML_INLINE, getClass().getResource( "obslink/zmlinline.xsd" ) ); //$NON-NLS-1$
    prefixes.put( NAMESPACE_ZML_INLINE, "inlinezml" ); //$NON-NLS-1$

    // XLINK
    catalog.put( NS.XLINK, getClass().getResource( "gml2_2002/xlinks.xsd" ) ); //$NON-NLS-1$
    prefixes.put( NS.XLINK, "xlink" ); //$NON-NLS-1$

    // WFS
    catalog.put( NS.WFS, getClass().getResource( "wfs1.1.0/wfs1.1.0.xsd" ) ); //$NON-NLS-1$
    prefixes.put( NS.WFS, "wfs" ); //$NON-NLS-1$

    // Common
    catalog.put( NS.COMMON, getClass().getResource( "commons/commons.xsd" ) ); //$NON-NLS-1$
    prefixes.put( NS.COMMON, "common" ); //$NON-NLS-1$

    catalog.put( NS.COMMON_MATH, getClass().getResource( "commons/math.xsd" ) ); //$NON-NLS-1$
    prefixes.put( NS.COMMON_MATH, "math" ); //$NON-NLS-1$

    catalog.put( NS.SWE_EXTENSIONS, getClass().getResource( "commons/sweExtensions.xsd" ) ); //$NON-NLS-1$
    prefixes.put( NS.SWE_EXTENSIONS, "sweExt" ); //$NON-NLS-1$

    catalog.put( NS.COMMON_MATHRANGES, getClass().getResource( "commons/mathRanges.xsd" ) ); //$NON-NLS-1$
    prefixes.put( NS.COMMON_MATHRANGES, "mathRanges" ); //$NON-NLS-1$

    catalog.put( NS.COMMON_COVERAGE, getClass().getResource( "commons/coverage.xsd" ) ); //$NON-NLS-1$
    prefixes.put( NS.COMMON_COVERAGE, "cov" ); //$NON-NLS-1$

    catalog.put( ShapeCollection.SHP_NAMESPACE_URI, getClass().getResource( "shape/shape.xsd" ) ); //$NON-NLS-1$
    prefixes.put( ShapeCollection.SHP_NAMESPACE_URI, "shp" ); //$NON-NLS-1$

    // Update Observation Mapping
    catalog.put( NS_UPDATE_OBSERVATION_MAPPING, getClass().getResource( "updateObservationMapping/V1.0/updateObservationMapping.xsd" ) ); //$NON-NLS-1$
  }
}