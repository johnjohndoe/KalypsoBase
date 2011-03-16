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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ObservationTokenHelper;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.layer.IZmlLayer;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.ext.base.layer.ChartLayerUtils;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.IStyleSetRefernceFilter;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;

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

  protected ZmlLineLayer( final ILayerProvider provider, final IStyleSet styleSet )
  {
    super( provider, styleSet );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer#createLegendEntries()
   */
  @Override
  public ILegendEntry[] createLegendEntries( )
  {
    return m_legend.createLegendEntries();
  }

  @Override
  public void drawLine( final GC gc, final List<Point> path )
  {
    super.drawLine( gc, path );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_data != null )
      m_data.dispose();

    super.dispose();
  }

  /**
   * @see org.kalypso.zml.ui.chart.layer.themes.IZmlLayer#getDataHandler()
   */
  @Override
  public IZmlLayerDataHandler getDataHandler( )
  {
    return m_data;
  }

  public ZmlLineLayerRangeHandler getRangeHandler( )
  {
    return m_range;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
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

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
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

    final String title = ObservationTokenHelper.replaceTokens( m_labelDescriptor, observation, getDataHandler().getValueAxis() );
    return title;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint( final GC gc )
  {
    final IObservation observation = m_data.getObservation();
    if( observation == null )
      return;

    final DateRange range = getRange();
    final Rectangle oldClip = gc.getClipping();
    try
    {
      setClip( gc, range );
      drawLineTheme( gc, observation );
// drawSelectionTheme();
    }
    finally
    {
      gc.setClipping( oldClip );
    }
  }

  private void setClip( final GC gc, final DateRange range )
  {
    if( range == null || range.getFrom() == null && range.getTo() == null )
      return;

    final ICoordinateMapper mapper = getCoordinateMapper();
    final Point screenSize = mapper.getScreenSize();

    Date from = Objects.isNotNull( range.getFrom()) ? ChartLayerUtils.addTimezoneOffset(  range.getFrom()) : null;
    Date to = Objects.isNotNull( range.getTo()) ? ChartLayerUtils.addTimezoneOffset(  range.getTo()) : null;
    
    final int fromScreen = getDomainScreen(from , 0 );
    final int toScreen = getDomainScreen( to, screenSize.x );

    gc.setClipping( fromScreen, 0, toScreen - fromScreen, screenSize.y );
  }

  private int getDomainScreen( final Date domainValue, final int defaultValue )
  {
    if( domainValue == null )
      return defaultValue;

    final Point screen = getCoordinateMapper().numericToScreen( getRangeHandler().getDateDataOperator().logicalToNumeric( domainValue ), 0.0 );
    return screen.x;
  }

  private DateRange getRange( )
  {
    final IRequest request = m_data.getRequest();
    if( request == null )
      return null;

    return request.getDateRange();
  }

  private void drawLineTheme( final GC gc, final IObservation observation )
  {
    try
    {
      setLineThemeStyles();

      final List<Point> path = new ArrayList<Point>();
      observation.accept( new LineLayerModelVisitor( this, path, getFilters() ), null );

      drawLine( gc, path );
      drawPoints( gc, path );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

// private void drawSelectionTheme( )
// {
// final de.openali.odysseus.chart.framework.model.mapper.IAxis domainAxis = getDomainAxis();
// if( domainAxis.getSelection() == null )
// return;
// }

  /**
   * @see org.kalypso.zml.core.diagram.layer.IZmlLayer#setDataHandler(org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler)
   */
  @Override
  public void setDataHandler( final IZmlLayerDataHandler handler )
  {
    if( m_data != null )
      m_data.dispose();

    m_data = handler;
  }

  /**
   * @see org.kalypso.zml.core.diagram.layer.IZmlLayer#setLabelDescriptor(java.lang.String)
   */
  @Override
  public void setLabelDescriptor( final String labelDescriptor )
  {
    m_labelDescriptor = labelDescriptor;
  }

  private void setLineThemeStyles( )
  {
    final IStyleSet styleSet = getStyleSet();
    final int index = ZmlLayerHelper.getLayerIndex( getId() );

    final StyleSetVisitor visitor = new StyleSetVisitor();

    final IPointStyle pointStyle = visitor.findReferences( styleSet, IPointStyle.class, new IStyleSetRefernceFilter()
    {
      @Override
      public boolean accept( final String reference )
      {
        return !reference.toLowerCase().contains( "single" ); //$NON-NLS-1$
      }
    } );
    final ILineStyle lineStyle = visitor.visit( styleSet, ILineStyle.class, index );
    final ITextStyle textStyle = visitor.visit( styleSet, ITextStyle.class, index );

    getPointFigure().setStyle( pointStyle );
    getPolylineFigure().setStyle( lineStyle );
    getTextFigure().setStyle( textStyle );
  }
}
