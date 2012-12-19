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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.zml.core.diagram.base.IZmlLayer;
import org.kalypso.zml.core.diagram.base.IZmlLayerProvider;
import org.kalypso.zml.core.diagram.base.ZmlLayerProviders;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.data.ZmlObsProviderDataHandler;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener.ContentChangeType;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSet;
import de.openali.odysseus.chart.framework.util.img.ChartImageInfo;

/**
 * ensures a specific domain date range for a chart diagram
 * 
 * @author Dirk Kuch
 */
public class ZmlDateRangeLayer extends AbstractChartLayer implements IZmlLayer
{
  // private final IDataOperator<Date> m_dateDataOperator = new DataOperatorHelper().getDataOperator( Date.class );

  private IZmlLayerDataHandler m_dataHandler;

  public ZmlDateRangeLayer( final IZmlLayerProvider provider, final URL context )
  {
    super( provider, new StyleSet() );
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
  public void onObservationChanged( final ContentChangeType type )
  {
    getEventHandler().fireLayerContentChanged( this, type );
  }

  @Override
  public void paint( final GC gc, ChartImageInfo chartImageInfo, final IProgressMonitor monitor )
  {
    // nothing to do
  }

  @SuppressWarnings( { "rawtypes", "unchecked" } )
  @Override
  public IDataRange<Double> getDomainRange( )
  {
    if( Objects.isNull( getDateRange() ) )
      return null;

    final Date min = getDateRange().getFrom();
    final Date max = getDateRange().getTo();
    if( Objects.isNull( min, max ) )
      return null;
    IAxis domainAxis = getDomainAxis();
    return new DataRange<>( domainAxis.logicalToNumeric( min ), domainAxis.logicalToNumeric( max ) );
  }

  @Override
  public IDataRange<Double> getTargetRange( final IDataRange<Double> domainIntervall )
  {
    return null;
  }

  @Override
  public void dispose( )
  {
    if( Objects.isNotNull( m_dataHandler ) )
      m_dataHandler.dispose();

    super.dispose();
  }

  @Override
  public IZmlLayerDataHandler getDataHandler( )
  {
    return m_dataHandler;
  }

  @Override
  public void setDataHandler( final IZmlLayerDataHandler handler )
  {
    m_dataHandler = handler;
  }

  @Override
  public void setLabelDescriptor( final String labelDescriptor )
  {
    // nothing to do
  }

  private DateRange getDateRange( )
  {
    final IZmlLayerDataHandler handler = getDataHandler();
    if( Objects.isNull( handler ) )
      return null;

    final IObservation observation = (IObservation)handler.getAdapter( IObservation.class );
    if( Objects.isNull( observation ) )
      return null;

    final IParameterContainer parameters = getProvider().getParameterContainer();

    final MetadataList metadata = observation.getMetadataList();
    final Date start = ZmlLayerProviders.getMetadataDate( parameters, "start", metadata ); //$NON-NLS-1$
    final Date end = ZmlLayerProviders.getMetadataDate( parameters, "end", metadata ); //$NON-NLS-1$

    return new DateRange( start, end );
  }
}
