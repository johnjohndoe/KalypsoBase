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

import java.util.Date;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.core.diagram.base.LayerProviderUtils;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.layer.IZmlLayer;
import org.kalypso.zml.core.diagram.layer.IZmlLayerFilter;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.TextFigure;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;
import de.openali.odysseus.chart.framework.util.resource.Pair;

/**
 * @author Dirk Kuch
 * @author kimwerner
 */
public class ZmlSinglePointLayer extends AbstractLineLayer implements IZmlLayer
{
  private ZmlSinglePointBean[] m_descriptors;

  private IZmlLayerDataHandler m_handler;

  public ZmlSinglePointLayer( final ILayerProvider provider, final IStyleSet styleset )
  {
    super( provider, styleset );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange<Number> getDomainRange( )
  {
    if( ArrayUtils.isEmpty( m_descriptors ) )
      return null;

    Long min = Long.MAX_VALUE;
    Long max = -Long.MAX_VALUE;

    for( final ZmlSinglePointBean bean : m_descriptors )
    {
      final DateRange dateRange = bean.getDateRange();

      min = Math.min( dateRange.getFrom().getTime(), min );
      max = Math.max( dateRange.getTo().getTime(), max );
    }

    return new DataRange<Number>( min, max );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    if( ArrayUtils.isEmpty( m_descriptors ) )
      return null;

    Number min = Double.MAX_VALUE;
    Number max = -Double.MAX_VALUE;

    for( final ZmlSinglePointBean descriptor : m_descriptors )
    {
      max = Math.max( max.doubleValue(), descriptor.getValue().getTarget().doubleValue() );
      min = Math.min( max.doubleValue(), descriptor.getValue().getTarget().doubleValue() );
    }

    return new DataRange<Number>( min, max );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint( final GC gc )
  {
    if( ArrayUtils.isEmpty( m_descriptors ) )
      return;

    for( final ZmlSinglePointBean descriptor : m_descriptors )
    {
      final Point centerPoint = getCoordinateMapper().numericToScreen( descriptor.getValue().getDomain(), descriptor.getValue().getTarget() );

      getPointFigure().setStyle( descriptor.getPointStyle() );
      getPointFigure().setPoints( new Point[] { centerPoint } );

      getPointFigure().paint( gc );
      if( descriptor.isShowLabel() )
      {
        final TextFigure textFigure = new TextFigure();
        textFigure.setStyle( descriptor.getTextStyle() );
        textFigure.setText( descriptor.getLabel() );

        // TODO: getPosition according to center point
        textFigure.setPoints( new Point[] { centerPoint } );
        textFigure.paint( gc );
      }
    }
  }

  /**
   * @see org.kalypso.zml.core.diagram.layer.IZmlLayer#getDataHandler()
   */
  @Override
  public IZmlLayerDataHandler getDataHandler( )
  {
    return m_handler;
  }

  /**
   * @see org.kalypso.zml.core.diagram.layer.IZmlLayer#setDataHandler(org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler)
   */
  @Override
  public void setDataHandler( final IZmlLayerDataHandler handler )
  {
    m_handler = handler;

    try
    {
      if( handler.getObservation() == null )
        return;

      final IParameterContainer parameters = getProvider().getParameterContainer();

      final Date position = LayerProviderUtils.getMetadataDate( parameters, "position", handler.getObservation().getMetadataList() );

      final Date start = LayerProviderUtils.getMetadataDate( parameters, "start", handler.getObservation().getMetadataList() );
      final Date end = LayerProviderUtils.getMetadataDate( parameters, "end", handler.getObservation().getMetadataList() );

      final Double value = findValue( handler, position );

      if( Objects.isNull( value ) )
      {
        m_descriptors = new ZmlSinglePointBean[] {};
        return;
      }

      final IStyleSet styleSet = getStyleSet();
      final StyleSetVisitor visitor = new StyleSetVisitor();

      final IPointStyle pointStyle = visitor.visit( styleSet, IPointStyle.class, 0 );
      final ITextStyle textStyle = visitor.visit( styleSet, ITextStyle.class, 0 );

      final ZmlSinglePointBean bean = new ZmlSinglePointBean( "", new Pair<Number, Number>( position.getTime(), value ), new DateRange( start, end ), pointStyle, textStyle, false );
      m_descriptors = new ZmlSinglePointBean[] { bean };
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }

  }

  private Double findValue( final IZmlLayerDataHandler provider, final Date position ) throws SensorException
  {
    final ITupleModel model = provider.getModel();

    final IAxis dateAxis = AxisUtils.findDateAxis( model.getAxes() );
    final IAxis valueAxis = provider.getValueAxis();

    double diff = Double.MAX_VALUE;
    Number value = null;

    for( int i = 0; i < model.size(); i++ )
    {
      final Date date = (Date) model.get( i, dateAxis );
      final Number v = (Number) model.get( i, valueAxis );

      final double d = Math.abs( date.getTime() - position.getTime() );
      if( d < diff )
      {
        if( d == 0 )
          return v.doubleValue();

        diff = d;
        value = v;
      }

    }

    if( value == null )
      return null;

    return value.doubleValue();
  }

  /**
   * @see org.kalypso.zml.core.diagram.layer.IZmlLayer#setLabelDescriptor(java.lang.String)
   */
  @Override
  public void setLabelDescriptor( final String labelDescriptor )
  {
    // not needed
  }

  /**
   * @see org.kalypso.zml.core.diagram.layer.IZmlLayer#setFilter(org.kalypso.zml.core.diagram.layer.IZmlLayerFilter[])
   */
  @Override
  public void setFilter( final IZmlLayerFilter filter )
  {
  }
}
