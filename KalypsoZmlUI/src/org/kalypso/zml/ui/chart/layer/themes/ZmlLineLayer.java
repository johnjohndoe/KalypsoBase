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

import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.Date;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ObservationTokenHelper;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.zml.core.diagram.base.IZmlLayer;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.data.IZmlLayerProvider;
import org.kalypso.zml.core.diagram.data.ZmlObsProviderDataHandler;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.ClipHelper;
import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.IStyleSetRefernceFilter;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;
import de.openali.odysseus.chart.framework.util.resource.IPair;

/**
 * @author Dirk Kuch
 * @author kimwerner
 */
public class ZmlLineLayer extends AbstractLineLayer implements IZmlLayer
{
  private IZmlLayerDataHandler m_data;

  private String m_labelDescriptor;

  private final ZmlLineLayerRangeHandler m_range = new ZmlLineLayerRangeHandler( this );

  private final ZmlLineLayerLegendEntry m_legend = new ZmlLineLayerLegendEntry( this );

  private ILineStyle m_lineStyle;

  public ZmlLineLayer( final IZmlLayerProvider provider, final IStyleSet styleSet, final URL context )
  {
    super( provider, styleSet );

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
  public void onObservationChanged( )
  {
    getEventHandler().fireLayerContentChanged( this );
  }

  @Override
  public ILegendEntry[] createLegendEntries( )
  {
    return m_legend.createLegendEntries();
  }

  @Override
  public void dispose( )
  {
    if( m_data != null )
      m_data.dispose();

    super.dispose();
  }

  @Override
  public IZmlLayerDataHandler getDataHandler( )
  {
    return m_data;
  }

  public ZmlLineLayerRangeHandler getRangeHandler( )
  {
    return m_range;
  }

  @Override
  public IDataRange<Number> getDomainRange( )
  {
    return m_range.getDomainRange();
  }

  @Override
  public synchronized ILegendEntry[] getLegendEntries( )
  {
    return createLegendEntries();
  }

  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    return m_range.getTargetRange( domainIntervall );
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
  public void paint( final GC gc )
  {
    try
    {
      final IPair<Number, Number>[] points = getFilteredPoints( null );
      if( points.length == 1 )
        paintSinglePoint( gc, points[0] );
      else if( points.length > 1 )
        paintPoints( gc, points );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  @SuppressWarnings("unchecked")
  IPair<Number, Number>[] getFilteredPoints( final IDataRange<Number> domainIntervall ) throws SensorException
  {
    final IObservation observation = m_data.getObservation();
    if( observation == null )
      return new IPair[0];

    final LineLayerModelVisitor visitor = new LineLayerModelVisitor( this, getFilters(), domainIntervall );
    observation.accept( visitor, null, 1 );
    return visitor.getPoints();
  }

  @SuppressWarnings("unchecked")
  private void paintSinglePoint( final GC gc, final IPair<Number, Number> point )
  {
    final IPointStyle pointStyle = getSinglePointStyle();
    if( pointStyle == null )
      return;

    final Rectangle2D clip = getClip();
    if( clip != null && !clip.contains( point.getDomain().doubleValue(), point.getTarget().doubleValue() ) )
      return;

    final Point[] screenPoints = toScreen( point );

    final PointFigure pf = getPointFigure();
    pf.setStyle( pointStyle );
    pf.setPoints( screenPoints );
    pf.paint( gc );
  }

  private void paintPoints( final GC gc, final IPair<Number, Number>[] points )
  {
    final ClipHelper helper = new ClipHelper( getClip() );
    final IPair<Number, Number>[][] clippedPoints = helper.clipAsLine( points );
    for( final IPair<Number, Number>[] subPoints : clippedPoints )
    {
      final Point[] screenPoints = toScreen( subPoints );
      paintFigures( gc, screenPoints );
    }
  }

  private void paintFigures( final GC gc, final Point[] points )
  {
    final ILineStyle lineStyle = getLineStyle();
    if( lineStyle != null )
    {
      final PolylineFigure lf = getPolylineFigure();
      lf.setStyle( lineStyle );
      lf.setPoints( points );
      lf.paint( gc );
    }

    final IPointStyle pointStyle = getMyPointStyle();
    if( pointStyle != null )
    {
      final PointFigure pf = getPointFigure();
      pf.setStyle( pointStyle );
      pf.setPoints( points );
      pf.paint( gc );
    }
  }

  Rectangle2D getClip( )
  {
    final DateRange range = getRange();
    if( range == null || range.getFrom() == null && range.getTo() == null )
      return null;

    final Date from = Objects.isNotNull( range.getFrom() ) ? range.getFrom() : null;
    final Date to = Objects.isNotNull( range.getTo() ) ? range.getTo() : null;

    // REMARK: using Long.MAX_VALUE instead of Double.MAX_VALUE, else we might
    // get problems with JTS-intersection later
    final Number defaultDateMin = -Long.MAX_VALUE;
    final Number defaultDateMax = Long.MAX_VALUE;
    final double domainMin = getDomainNumeric( from, defaultDateMin ).doubleValue();
    final double domainMax = getDomainNumeric( to, defaultDateMax ).doubleValue();

    final double targetMin = -Long.MAX_VALUE;
    final double targetMax = Long.MAX_VALUE;

    final double width = domainMax - domainMin;
    final double height = targetMax - targetMin;

    return new Rectangle2D.Double( domainMin, targetMin, width, height );
  }

  private Number getDomainNumeric( final Date domainValue, final Number defaultValue )
  {
    if( domainValue == null )
      return defaultValue;

    return getRangeHandler().getDateDataOperator().logicalToNumeric( domainValue );
  }

  private DateRange getRange( )
  {
    final IRequest request = m_data.getRequest();
    if( request == null )
      return null;

    return request.getDateRange();
  }

  @Override
  public void setDataHandler( final IZmlLayerDataHandler handler )
  {
    if( m_data != null )
      m_data.dispose();

    m_data = handler;
  }

  @Override
  public void setLabelDescriptor( final String labelDescriptor )
  {
    m_labelDescriptor = labelDescriptor;
  }

  private IPointStyle getMyPointStyle( )
  {
    final IStyleSet styleSet = getStyleSet();

    // FIXME: strange! we need better helper classes here...
    final StyleSetVisitor visitor = new StyleSetVisitor( false );

    return visitor.findReferences( styleSet, IPointStyle.class, new IStyleSetRefernceFilter()
    {
      @Override
      public boolean accept( final String reference )
      {
        return !reference.toLowerCase().contains( "singlepoint" ); //$NON-NLS-1$
      }
    } );
  }

  private IPointStyle getSinglePointStyle( )
  {
    final IStyleSet styleSet = getStyleSet();

    // FIXME: strange! we need better helper classes here...
    final StyleSetVisitor visitor = new StyleSetVisitor( false );

    return visitor.findReferences( styleSet, IPointStyle.class, new IStyleSetRefernceFilter()
    {
      @Override
      public boolean accept( final String reference )
      {
        return reference.toLowerCase().contains( "singlepoint" ); //$NON-NLS-1$
      }
    } );
  }

  ILineStyle getLineStyle( )
  {
    if( Objects.isNotNull( m_lineStyle ) )
      return m_lineStyle;

    final IStyleSet styleSet = getStyleSet();

    // FIXME: strange! we need better helper classes here...
    final int index = ZmlLayerHelper.getLayerIndex( getIdentifier() );
    final StyleSetVisitor visitor = new StyleSetVisitor( false );
    m_lineStyle = visitor.visit( styleSet, ILineStyle.class, index );

    return m_lineStyle;
  }

  private Point[] toScreen( final IPair<Number, Number>... points )
  {
    final ICoordinateMapper coordinateMapper = getCoordinateMapper();
    final Point[] screenPoints = new Point[points.length];
    for( int i = 0; i < screenPoints.length; i++ )
      screenPoints[i] = coordinateMapper.numericToScreen( points[i].getDomain(), points[i].getTarget() );
    return screenPoints;
  }
}
