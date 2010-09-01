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
package org.kalypso.ogc.gml;

import java.util.Formatter;
import java.util.List;
import java.util.Properties;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.Assert;
import org.kalypso.core.i18n.Messages;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * {@link IKalypsoThemeInfo} implementation for {@link IKalypsoFeatureTheme}s.<br>
 * This implementation is based on a formatstring tha may contain the <code>${property:XXX}</code> notations.<br>
 * <br>
 * This class is intended to be overwritten by specialised implementations.
 * 
 * @author Gernot Belger
 */
public class FeatureThemeInfo implements IKalypsoThemeInfo, IKalypsoFeatureThemeInfo
{
  private IKalypsoFeatureTheme m_theme = null;

  private String m_format;

  private QName m_geom;

  private String m_noHitString;

  /**
   * Needed for construction via extension-point.
   */
  public FeatureThemeInfo( )
  {
    // empty
  }

  public FeatureThemeInfo( final KalypsoFeatureTheme theme, final Properties props )
  {
    init( theme, props );
  }

  /**
   * Final, because called from a constructor.
   * 
   * @see org.kalypso.ogc.gml.IKalypsoThemeInfo#init(org.kalypso.ogc.gml.IKalypsoTheme)
   */
  @Override
  public final void init( final IKalypsoTheme theme, final Properties props )
  {
    Assert.isLegal( theme instanceof IKalypsoFeatureTheme );

    m_theme = (IKalypsoFeatureTheme) theme;

    m_noHitString = props.getProperty( "noFeatureMsg", "-" ); //$NON-NLS-1$ $NON-NLS-2$
    m_format = props.getProperty( "m_format" ); //$NON-NLS-1$
    final String geomStr = props.getProperty( "geometry" ); //$NON-NLS-1$
    m_geom = geomStr == null ? null : QName.valueOf( geomStr );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoThemeInfo#appendInfo(java.util.Formatter,
   *      org.kalypsodeegree.model.geometry.GM_Position)
   */
  @Override
  public void appendInfo( final Formatter formatter, final GM_Position pos )
  {
    Assert.isNotNull( m_theme, Messages.getString( "org.kalypso.ogc.gml.FeatureThemeInfo.2" ) ); //$NON-NLS-1$

    // not yet implemented
    appendQuickInfo( formatter, pos );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoThemeInfo#appendQuickInfo(java.util.Formatter,
   *      org.kalypsodeegree.model.geometry.GM_Position)
   */
  @Override
  @SuppressWarnings("unchecked")
  public void appendQuickInfo( final Formatter formatter, final GM_Position pos )
  {
    Assert.isNotNull( m_theme, Messages.getString( "org.kalypso.ogc.gml.FeatureThemeInfo.4" ) ); //$NON-NLS-1$

    final FeatureList featureList = m_theme.getFeatureList();
    if( featureList == null )
    {
      formatter.format( Messages.getString( "org.kalypso.ogc.gml.FeatureThemeInfo.5" ) ); //$NON-NLS-1$
      return;
    }

    final GMLWorkspace workspace = featureList.getParentFeature().getWorkspace();
    // TODO: the query by position does not return the excepted result.... :-( does not works for point geometries
    final List< ? > foundFeatures = featureList.query( pos, null );
    final Feature feature = findFeature( workspace, foundFeatures, pos );
    formatInfo( formatter, feature );
  }

  private Feature findFeature( final GMLWorkspace workspace, final List< ? > foundFeatures, final GM_Position pos )
  {
    int leastDistanceIndex = -1;
    double leastDistance = Double.MAX_VALUE;
    for( int i = foundFeatures.size() - 1; i >= 0; i-- )
    {
      final Object object = foundFeatures.get( i );
      final Feature feature = FeatureHelper.resolveLinkedFeature( workspace, object );
      final GM_Object geom = getGeom( feature );

      if( geom == null )
        continue;

      if( geom instanceof GM_Point )
      {
        final double distance = pos.getDistance( ((GM_Point) geom).getPosition() );
        if( distance < leastDistance )
        {
          leastDistance = distance;
          leastDistanceIndex = i;
        }
      }
      else if( geom.contains( pos ) )
        return feature;
    }

    if( leastDistanceIndex != -1 )
    {
      final Object object = foundFeatures.get( leastDistanceIndex );
      return FeatureHelper.resolveLinkedFeature( workspace, object );
    }

    return null;
  }

  private GM_Object getGeom( final Feature feature )
  {
    if( m_geom == null )
      return feature.getDefaultGeometryPropertyValue();

    final Object property = feature.getProperty( m_geom );
    if( property instanceof GM_Object )
      return (GM_Object) property;

    return null;
  }

  /**
   * Writes the info into the formatter for the given feature<br>
   * Intended to be overwritten by specialised implementations.
   */
  @Override
  public void formatInfo( final Formatter formatter, final Feature feature )
  {
    final String label = getInfo( feature );
    formatter.format( "%s", label ); //$NON-NLS-1$
  }

  /**
   * Returns the format-string (for the formatter) for the given feature.<br>
   * Intended to be overwritten by specialized implementations.
   */
  protected String getInfo( final Feature feature )
  {
    if( feature == null )
      return m_noHitString;

    if( m_format == null )
      return FeatureHelper.getAnnotationValue( feature, IAnnotation.ANNO_LABEL );

    return FeatureHelper.tokenReplace( feature, m_format );
  }

}
