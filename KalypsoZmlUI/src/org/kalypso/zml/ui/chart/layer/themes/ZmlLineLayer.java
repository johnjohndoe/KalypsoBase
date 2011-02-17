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
import java.util.List;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationTokenHelper;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.filter.DefaultZmlFilter;
import org.kalypso.zml.core.diagram.layer.IZmlLayer;
import org.kalypso.zml.core.diagram.layer.IZmlLayerFilter;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
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

  private IZmlLayerFilter m_filter = new DefaultZmlFilter();

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
    drawLineTheme( gc );
    drawSelectionTheme( gc );
  }

  private void drawLineTheme( final GC gc )
  {
    try
    {
      final ITupleModel model = m_data.getModel();
      if( model == null )
        return;

      setLineThemeStyles();

      final List<Point> path = new ArrayList<Point>();
      model.accept( new LineLayerModelVisitor( this, path, m_filter ) );

      drawLine( gc, path );
      drawPoints( gc, path );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  private void drawSelectionTheme( final GC gc )
  {
    final de.openali.odysseus.chart.framework.model.mapper.IAxis domainAxis = getDomainAxis();
    if( domainAxis.getSelection() == null )
      return;

  }

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
        return !reference.contains( "single" ); //$NON-NLS-1$
      }
    } );
    final ILineStyle lineStyle = visitor.visit( styleSet, ILineStyle.class, index );
    final ITextStyle textStyle = visitor.visit( styleSet, ITextStyle.class, index );

    getPointFigure().setStyle( pointStyle );
    getPolylineFigure().setStyle( lineStyle );
    getTextFigure().setStyle( textStyle );
  }

  /**
   * @see org.kalypso.zml.core.diagram.layer.IZmlLayer#setFilter(org.kalypso.zml.core.diagram.layer.IZmlLayerFilter)
   */
  @Override
  public void setFilter( final IZmlLayerFilter filter )
  {
    m_filter = filter;

    getEventHandler().fireLayerContentChanged( this );
  }
}
