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
import java.util.Date;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.zml.core.diagram.base.IZmlLayer;
import org.kalypso.zml.core.diagram.base.IZmlLayerProvider;
import org.kalypso.zml.core.diagram.base.ZmlLayerProviders;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.data.ZmlObsProviderDataHandler;
import org.kalypso.zml.ui.chart.layer.filters.ZmlChartLayerFilters;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener.ContentChangeType;
import de.openali.odysseus.chart.framework.model.figure.impl.PointFigure;
import de.openali.odysseus.chart.framework.model.figure.impl.TextFigure;
import de.openali.odysseus.chart.framework.model.layer.IChartLayerFilter;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;
import de.openali.odysseus.chart.framework.util.img.ChartImageInfo;
import de.openali.odysseus.chart.framework.util.resource.Pair;

/**
 * @author Dirk Kuch
 * @author kimwerner
 */
public class ZmlSinglePointLayer extends AbstractLineLayer implements IZmlLayer
{
  private ZmlSinglePointBean[] m_descriptors;

  private IZmlLayerDataHandler m_handler;

  public ZmlSinglePointLayer( final IZmlLayerProvider provider, final IStyleSet styleset, final URL context )
  {
    super( provider, styleset );
    setup( context );
  }

  @Override
  public IZmlLayerProvider getProvider( )
  {
    return (IZmlLayerProvider)super.getProvider();
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
  public void onObservationChanged( final ContentChangeType type )
  {
    getEventHandler().fireLayerContentChanged( this, type );
  }

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  @Override
  public IDataRange<Double> getDomainRange( )
  {
    if( ArrayUtils.isEmpty( m_descriptors ) )
      return null;

    Number min = Long.MAX_VALUE;
    Number max = -Long.MAX_VALUE;

    for( final ZmlSinglePointBean bean : m_descriptors )
    {
      final DateRange dateRange = bean.getDateRange();

      min = Math.min( dateRange.getFrom().getTime(), min.longValue() );
      max = Math.max( dateRange.getTo().getTime(), max.longValue() );
    }

    return new DataRange( min, max );
  }

  @SuppressWarnings( { "unchecked", "rawtypes" } )
  @Override
  public IDataRange<Double> getTargetRange( final IDataRange<Double> domainIntervall )
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

    return new DataRange( min, max );
  }

  @Override
  public void paint( final GC gc, ChartImageInfo chartImageInfo, final IProgressMonitor monitor )
  {
    if( ArrayUtils.isEmpty( m_descriptors ) )
      return;

    for( final ZmlSinglePointBean descriptor : m_descriptors )
    {
      final Point centerPoint = getCoordinateMapper().logicalToScreen( descriptor.getValue().getDomain(), descriptor.getValue().getTarget() );

      final PointFigure pf = new PointFigure();
      pf.setStyle( descriptor.getPointStyle() );
      pf.setPoints( new Point[] { centerPoint } );

      pf.paint( gc );

      if( descriptor.isShowLabel() )
      {
        final TextFigure textFigure = new TextFigure();
        textFigure.setStyle( descriptor.getTextStyle() );
        textFigure.setText( descriptor.getLabel() );

        // TODO: getPosition according to center point
        textFigure.setPoint( centerPoint );
        textFigure.paint( gc );
      }
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
    m_handler = handler;

    try
    {
      final IObservation observation = (IObservation)handler.getAdapter( IObservation.class );
      if( observation == null )
        return;

      final IParameterContainer parameters = getProvider().getParameterContainer();

      final MetadataList metadata = observation.getMetadataList();
      final Date position = ZmlLayerProviders.getMetadataDate( parameters, "position", metadata ); //$NON-NLS-1$

      final Date start = ZmlLayerProviders.getMetadataDate( parameters, "start", metadata ); //$NON-NLS-1$
      final Date end = ZmlLayerProviders.getMetadataDate( parameters, "end", metadata ); //$NON-NLS-1$

      final Double value = findValue( handler, position );

      if( Objects.isNull( value ) )
      {
        m_descriptors = new ZmlSinglePointBean[] {};
        return;
      }

      final IStyleSet styleSet = getStyleSet();
      final StyleSetVisitor visitor = new StyleSetVisitor( false );
      final IPointStyle pointStyle = visitor.visit( styleSet, IPointStyle.class, 0 );
      final ITextStyle textStyle = visitor.visit( styleSet, ITextStyle.class, 0 );

      // FIXME: label not implemented (always ""), but why a label at all? We should remove that code.
      final ZmlSinglePointBean bean = new ZmlSinglePointBean( "", new Pair<Number, Number>( position.getTime(), value ), new DateRange( start, end ), pointStyle, textStyle, false ); //$NON-NLS-1$
      m_descriptors = new ZmlSinglePointBean[] { bean };
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
    }

  }

  private Double findValue( final IZmlLayerDataHandler provider, final Date position ) throws SensorException
  {
    final IObservation observation = (IObservation)provider.getAdapter( IObservation.class );

    final IChartLayerFilter[] filters = getFilters();
    ZmlChartLayerFilters.initializeFilters( observation, filters );

    final ZmlSinglePointLayerVisitor visitor = new ZmlSinglePointLayerVisitor( position, filters );
    observation.accept( visitor, provider.getRequest(), 1 );

    return visitor.getValue();
  }

  @Override
  public void setLabelDescriptor( final String labelDescriptor )
  {
    // not needed
  }
}
