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
package org.kalypso.gml.ui.map;

import java.util.Formatter;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.Assert;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.grid.GeoGridUtilities;
import org.kalypso.grid.IGeoGrid;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoThemeInfo;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Show the height value of a coverage as info.
 * 
 * @author Gernot Belger
 */
public class CoverageThemeInfo implements IKalypsoThemeInfo
{
  private static final String DEFAULT_FORMAT_STRING = Messages.getString( "org.kalypso.gml.ui.map.CoverageThemeInfo.0" ); //$NON-NLS-1$

  /**
   * Value of the property for a format string.<br>
   * A {@link Formatter}-style format string, can only contain one variable of type f. Example: <code>Value: %.3f</code>
   * .
   */
  public final static String PROP_FORMAT = "format"; //$NON-NLS-1$

  private IKalypsoFeatureTheme m_theme;

  private String m_formatString;

  /**
   * @see org.kalypso.ogc.gml.IKalypsoThemeInfo#init(org.kalypso.ogc.gml.IKalypsoTheme, java.util.Properties)
   */
  public void init( final IKalypsoTheme theme, final Properties props )
  {
    Assert.isLegal( theme instanceof IKalypsoFeatureTheme );
    m_theme = (IKalypsoFeatureTheme) theme;

    final IFeatureType featureType = m_theme.getFeatureType();
    Assert.isLegal( GMLSchemaUtilities.substitutes( featureType, ICoverage.QNAME ) );
    m_formatString = initFormatString( props );
  }

  protected String initFormatString( final Properties props )
  {
    return props.getProperty( PROP_FORMAT, DEFAULT_FORMAT_STRING );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoThemeInfo#appendInfo(java.util.Formatter,
   *      org.kalypsodeegree.model.geometry.GM_Position)
   */
  public void appendInfo( final Formatter formatter, final GM_Position pos )
  {
    Assert.isNotNull( m_theme );

    appendQuickInfo( formatter, pos );
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoThemeInfo#appendQuickInfo(java.util.Formatter,
   *      org.kalypsodeegree.model.geometry.GM_Position)
   */
  public void appendQuickInfo( final Formatter formatter, final GM_Position pos )
  {
    try
    {
      final Double value = getValue( pos );
      if( value == null )
        return;
      formatter.format( getFormatString(), value );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      formatter.format( "Fehler: %s%n", e.toString() ); //$NON-NLS-1$
    }
  }

  // extracted getting the value for easier class extending
  protected Double getValue( final GM_Position pos ) throws Exception
  {
    Assert.isNotNull( m_theme );

    final CommandableWorkspace workspace = m_theme.getWorkspace();
    final FeatureList featureList = m_theme.getFeatureList();
    if( featureList == null )
      return null;

    final List< ? > coverages = featureList.query( pos, null );
    for( final Object object : coverages )
    {
      /* Search for the first grid which provides a value */
      final Feature feature = FeatureHelper.getFeature( workspace, object );
      final ICoverage coverage = (ICoverage) feature.getAdapter( ICoverage.class );
      final IGeoGrid grid = GeoGridUtilities.toGrid( coverage );
      final Coordinate crd = JTSAdapter.export( pos );
      final Coordinate gridCrd = GeoGridUtilities.transformCoordinate( grid, crd, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );

      final double value = GeoGridUtilities.getValueChecked( grid, gridCrd );
      if( !Double.isNaN( value ) )
        return value;
    }
    return null;
  }

  protected String getFormatString( )
  {
    return m_formatString;
  }

  protected String getDefaultFormatString( )
  {
    return DEFAULT_FORMAT_STRING;
  }
}
