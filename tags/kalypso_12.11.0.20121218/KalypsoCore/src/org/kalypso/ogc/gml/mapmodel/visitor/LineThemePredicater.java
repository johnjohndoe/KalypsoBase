/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.ogc.gml.mapmodel.visitor;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.ArrayUtils;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.mapmodel.IKalypsoThemePredicate;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_MultiCurve;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * @author Thomas Jung
 */
public class LineThemePredicater implements IKalypsoThemePredicate
{
  private static final QName[] ACCEPTED_GEOMETRIES = new QName[] { GM_Curve.CURVE_ELEMENT, GM_MultiCurve.MULTI_CURVE_ELEMENT };

  @Override
  public boolean decide( final IKalypsoTheme theme )
  {
    if( !(theme instanceof IKalypsoFeatureTheme) )
      return false;

    final IKalypsoFeatureTheme fTheme = (IKalypsoFeatureTheme) theme;
    final IFeatureType featureType = fTheme.getFeatureType();

    final FeatureList featureList = fTheme.getFeatureList();
    if( featureList == null || featureType == null )
      return false;

    /* First check via feature type */
    if( hasAcceptedProperty( featureType ) )
      return true;

    /*
     * Feature type check is not enough, we can have single features that still contain line, even if the list type is
     * more general
     */
    return hasLineElements( featureList );
  }

  private boolean hasLineElements( final FeatureList featureList )
  {
    final Feature owner = featureList.getOwner();
    if( owner == null )
    {
      /*
       * This only happens, if the theme contains a singleton feature. In that case, the first type check was already
       * enough
       */
      return false;
    }

    final GMLWorkspace workspace = owner.getWorkspace();

    int count = 0;

    for( final Object element : featureList )
    {
      final Feature feature = FeatureHelper.getFeature( workspace, element );
      if( feature != null )
      {
        final IFeatureType featureType = feature.getFeatureType();
        if( hasAcceptedProperty( featureType ) )
          return true;
      }

      /* look at maximal 1000 elements, if those have no line, we can assume no one has a line */
      if( count++ > 1000 )
        return false;
    }

    return false;
  }

  private boolean hasAcceptedProperty( final IFeatureType featureType )
  {
    final IValuePropertyType[] allGeomtryProperties = featureType.getAllGeometryProperties();
    for( final IValuePropertyType vpt : allGeomtryProperties )
    {
      if( ArrayUtils.contains( ACCEPTED_GEOMETRIES, vpt.getValueQName() ) )
        return true;
    }

    return false;
  }
}
