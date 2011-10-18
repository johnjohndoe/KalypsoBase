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

import org.eclipse.swt.graphics.GC;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.metadata.MetadataList;
import org.kalypso.zml.core.diagram.base.LayerProviderUtils;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.data.IZmlLayerProvider;
import org.kalypso.zml.core.diagram.data.ZmlObsProviderDataHandler;
import org.kalypso.zml.core.diagram.layer.IZmlLayer;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.DataOperatorHelper;

/**
 * ensures a specific domain date range for a chart diagram
 * 
 * @author Dirk Kuch
 */
public class ZmlDateRangeLayer extends AbstractChartLayer implements IZmlLayer
{
  private final IDataOperator<Date> m_dateDataOperator = new DataOperatorHelper().getDataOperator( Date.class );

  private IZmlLayerDataHandler m_dataHandler;

  public ZmlDateRangeLayer( final IZmlLayerProvider provider, final URL context )
  {
    super( provider );
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
  public void paint( final GC gc )
  {
    // nothing to do
  }

  @Override
  public IDataRange<Number> getDomainRange( )
  {
    if( Objects.isNull( getDateRange() ) )
      return null;

    final Date min = getDateRange().getFrom();
    final Date max = getDateRange().getTo();
    if( Objects.isNull( min, max ) )
      return null;

    final IDataRange<Number> numRange = new DataRange<Number>( m_dateDataOperator.logicalToNumeric( min ), m_dateDataOperator.logicalToNumeric( max ) );
    return numRange;
  }

  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
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
  protected ILegendEntry[] createLegendEntries( )
  {
    return null;
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

    final IObservation observation = handler.getObservation();
    if( Objects.isNull( observation ) )
      return null;

    final IParameterContainer parameters = getProvider().getParameterContainer();

    final MetadataList metadata = observation.getMetadataList();
    final Date start = LayerProviderUtils.getMetadataDate( parameters, "start", metadata ); //$NON-NLS-1$
    final Date end = LayerProviderUtils.getMetadataDate( parameters, "end", metadata ); //$NON-NLS-1$

    return new DateRange( start, end );
  }

}
