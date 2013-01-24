/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.zml.core.base;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Stroke;
import java.net.URL;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.java.awt.ColorUtilities;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.core.util.pool.KeyInfo;
import org.kalypso.core.util.pool.PoolableObjectType;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ObservationTokenHelper;
import org.kalypso.ogc.sensor.provider.PooledObsProvider;
import org.kalypso.zml.obslink.TimeseriesLinkType;

/**
 * @author belger
 */
public class TSLinkWithName implements IZmlSourceElement
{
  private String m_identifier;

  private URL m_context;

  private final String m_name;

  private final TimeseriesLinkType m_obsLink;

  private final TimeserieFeatureProperty m_property;

  private PooledObsProvider m_provider;

  public TSLinkWithName( final String identifier, final URL context, final String name, final TimeseriesLinkType obsLink, final TimeserieFeatureProperty property )
  {
    m_identifier = identifier;
    m_context = context;
    m_name = name;
    m_obsLink = obsLink;
    m_property = property;
  }

  /**
   * it's possible to update identifiers - this feature is needed for multiple selection
   */
  public void setIdentifier( final String identifier )
  {
    m_identifier = identifier;
  }

  @Override
  public String getIdentifier( )
  {
    return m_identifier;
  }

  public Color getColor( )
  {
    final String color = m_property.getColor();
    if( Objects.isNotNull( color ) )
      return ColorUtilities.decodeWithAlpha( color );

    return null;
  }

  public URL getContext( )
  {
    return m_context;
  }

  public String getHref( )
  {
    final String href = m_obsLink.getHref();
    final String filter = m_property.getFilter();
    if( StringUtils.isNotEmpty( filter ) )
      return href + "?" + filter;

    return href;
  }

  protected TimeserieFeatureProperty getProperties( )
  {
    return m_property;
  }

  protected TimeseriesLinkType getTimerseriesLinkType( )
  {
    return m_obsLink;
  }

  public String getLinktype( )
  {
    return m_obsLink.getLinktype();
  }

  public String getName( )
  {
    return m_name;
  }

  public Stroke getStroke( )
  {
    final float width = getLineWidth();
    final float[] dash = getLineDash();

    return new BasicStroke( width, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 10.0f, dash, 1f );
  }

  private float[] getLineDash( )
  {
    final String strDash = m_property.getLineDash();
    if( StringUtils.isEmpty( strDash ) )
      return null;

    final float dash = (float) NumberUtils.parseQuietDouble( strDash );
    return new float[] { dash, dash };
  }

  private float getLineWidth( )
  {
    final String width = m_property.getLineWidth();
    if( StringUtils.isEmpty( width ) )
      return 1f;

    return (float) NumberUtils.parseQuietDouble( width );
  }

  public boolean isEditable( )
  {
    return m_property.isEditable();
  }

  public boolean isShowLegend( )
  {
    return m_property.getShowLegend();
  }

  public void setContext( final URL context )
  {
    m_context = context;
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof TSLinkWithName )
    {
      final TSLinkWithName other = (TSLinkWithName) obj;

      final EqualsBuilder builder = new EqualsBuilder();
      builder.append( getIdentifier(), other.getIdentifier() );
      builder.append( getContext(), other.getContext() );
      builder.append( getHref(), other.getHref() );

      return builder.isEquals();
    }

    return super.equals( obj );
  }

  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( getClass().getName() );
    builder.append( getIdentifier() );
    builder.append( getContext() );
    builder.append( getHref() );

    return builder.toHashCode();
  }

  @Override
  public String toString( )
  {
    return String.format( "link %s", getHref() );
  }

  @Override
  public synchronized PooledObsProvider getObsProvider( )
  {
    if( Objects.isNotNull( m_provider ) )
      return m_provider;

    final PoolableObjectType type = new PoolableObjectType( "zml", getHref(), getContext(), true ); //$NON-NLS-1$
    m_provider = new PooledObsProvider( type );

    return m_provider;
  }

  @Override
  public IPoolableObjectType getPoolKey( )
  {
    final PooledObsProvider provider = getObsProvider();

    return provider.getPoolKey();
  }

  @Override
  public boolean isDirty( )
  {
    final PooledObsProvider provider = getObsProvider();
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();

    final IObservation observation = provider.getObservation();
    if( Objects.isNull( observation ) )
      return false;

    final KeyInfo info = pool.getInfo( observation );
    if( Objects.isNull( info ) )
      return false;

    return info.isDirty();
  }

  @Override
  public String getLabel( final IAxis axis )
  {
    final String tokenizedName = getName();
    final PooledObsProvider provider = getObsProvider();
    if( Objects.isNull( provider ) )
      return tokenizedName;

    final IObservation observation = provider.getObservation();
    if( Objects.isNull( observation ) )
      return tokenizedName;

    return ObservationTokenHelper.replaceTokens( tokenizedName, observation, axis );
  }

  @Override
  public synchronized void dispose( )
  {
    if( Objects.isNotNull( m_provider ) )
      m_provider.dispose();
  }
}
