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
package org.kalypso.zml.ui.chart.layer.themes;

import java.net.URL;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ObservationTokenHelper;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.diagram.base.IZmlLayer;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.data.IZmlLayerProvider;
import org.kalypso.zml.core.diagram.data.ZmlObsProviderDataHandler;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.ext.base.layer.AbstractBarLayer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PolygonFigure;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;

/**
 * @author Dirk Kuch
 * @author kimwerner
 */
public class ZmlBarLayer extends AbstractBarLayer implements IZmlLayer
{
  private IZmlLayerDataHandler m_handler;

  private String m_labelDescriptor;

  private final ZmlBarLayerLegendEntry m_legend = new ZmlBarLayerLegendEntry( this );

  private final ZmlBarLayerRangeHandler m_range = new ZmlBarLayerRangeHandler( this );

  private final IStyleSet m_styleSet;

  public ZmlBarLayer( final IZmlLayerProvider layerProvider, final IStyleSet styleSet, final URL context )
  {
    super( layerProvider, null );
    m_styleSet = styleSet;

    setup( context );
  }

  @Override
  public IZmlLayerProvider getProvider( )
  {
    return (IZmlLayerProvider) super.getProvider();
  }

  private void setup( final URL context )
  {
    final IZmlLayerProvider provider = getProvider();
    final ZmlObsProviderDataHandler handler = new ZmlObsProviderDataHandler( this, provider.getTargetAxisId() );
    try
    {
      handler.load( provider, context );
    }
    catch( final Throwable t )
    {
      t.printStackTrace();
    }

    setDataHandler( handler );
  }

  @Override
  public void dispose( )
  {
    if( Objects.isNotNull( m_handler ) )
      m_handler.dispose();

    super.dispose();
  }

  @Override
  public void onObservationChanged( )
  {
    getEventHandler().fireLayerContentChanged( this );
  }

  @Override
  public synchronized ILegendEntry[] getLegendEntries( )
  {
    return createLegendEntries();
  }

  @Override
  public ILegendEntry[] createLegendEntries( )
  {
    return m_legend.createLegendEntries( getPolygonFigure() );
  }

  @Override
  public IDataRange<Number> getDomainRange( )
  {
    return m_range.getDomainRange();
  }

  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    return m_range.getTargetRange();
  }

  @Override
  public void paint( final GC gc )
  {
    try
    {
      final IObservation observation = m_handler.getObservation();
      if( Objects.isNull( observation ) )
        return;

      final ZmlBarLayerVisitor visitor = new ZmlBarLayerVisitor( this, m_range );
      observation.accept( visitor, m_handler.getRequest(), 1 );

      final PolygonFigure figure = getPolygonFigure();
      final Point[][] points = visitor.getPoints();
      for( final Point[] p : points )
      {
        figure.setPoints( p );
        figure.paint( gc );
      }

    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  @Override
  public IZmlLayerDataHandler getDataHandler( )
  {
    return m_handler;
  }

  @Override
  public void setDataHandler( final IZmlLayerDataHandler handler )
  {
    if( m_handler != null )
      m_handler = handler;

    m_handler = handler;
  }

  @Override
  public void setLabelDescriptor( final String labelDescriptor )
  {
    m_labelDescriptor = labelDescriptor;
  }

  @Override
  public String getTitle( )
  {
    if( m_labelDescriptor == null )
      return super.getTitle();

    final IObservation observation = getDataHandler().getObservation();
    if( observation == null )
      return m_labelDescriptor;

    return ObservationTokenHelper.replaceTokens( m_labelDescriptor, observation, getDataHandler().getValueAxis() );
  }

  @Override
  protected IAreaStyle getAreaStyle( )
  {
    final IStyleSet styleSet = getStyleSet();
    final int index = ZmlLayerHelper.getLayerIndex( getIdentifier() );

    final StyleSetVisitor visitor = new StyleSetVisitor( true );
    final IAreaStyle style = visitor.visit( styleSet, IAreaStyle.class, index );

    return style;
  }

  protected IStyleSet getStyleSet( )
  {
    return m_styleSet;
  }
}
