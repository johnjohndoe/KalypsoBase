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
package org.kalypso.zml.ui.chart.layer.provider;

import java.net.URL;
import java.util.Date;

import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.core.diagram.base.LayerProviderUtils;
import org.kalypso.zml.ui.chart.layer.themes.ZmlSinglePointBean;
import org.kalypso.zml.ui.chart.layer.themes.ZmlSinglePointLayer;
import org.kalypso.zml.ui.core.provider.observation.DefaultRequestHandler;
import org.kalypso.zml.ui.core.provider.observation.SynchronousObservationProvider;

import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;
import de.openali.odysseus.chart.framework.util.resource.Pair;

/**
 * @author Dirk Kuch
 */
public class ZmlSinglePointProvider extends AbstractLayerProvider implements ILayerProvider
{
  public static final String ID = "org.kalypso.zml.ui.chart.layer.provider.ZmlSinglePointProvider";

  /**
   * @see de.openali.odysseus.chart.factory.provider.ILayerProvider#getLayer(java.net.URL)
   */
  @Override
  public IChartLayer getLayer( final URL context ) throws ConfigurationException
  {
    final IParameterContainer parameters = getParameterContainer();
    final String href = parameters.getParameterValue( "href", "" ); //$NON-NLS-1$

    try
    {
      final SynchronousObservationProvider provider = new SynchronousObservationProvider( context, href, new DefaultRequestHandler() );

      final Date position = LayerProviderUtils.getMetadataDate( parameters, "position", provider.getObservation().getMetadataList() );
      if( position == null )
        return null;

      final Date start = LayerProviderUtils.getMetadataDate( parameters, "start", provider.getObservation().getMetadataList() );
      final Date end = LayerProviderUtils.getMetadataDate( parameters, "end", provider.getObservation().getMetadataList() );

      final Double value = findValue( provider, position );

      final IStyleSet styleSet = getStyleSet();
      final StyleSetVisitor visitor = new StyleSetVisitor();

      final IPointStyle pointStyle = visitor.visit( styleSet, IPointStyle.class, 0 );
      final ITextStyle textStyle = visitor.visit( styleSet, ITextStyle.class, 0 );

      final ZmlSinglePointBean bean = new ZmlSinglePointBean( "", new Pair<Number, Number>( position.getTime(), value ), new DateRange( start, end ), pointStyle, textStyle, false );
      return new ZmlSinglePointLayer( true, bean );
    }
    catch( final Throwable t )
    {
      throw new ConfigurationException( "Configuring of .kod line layer theme failed.", t );
    }
  }

  private Double findValue( final SynchronousObservationProvider provider, final Date position ) throws SensorException
  {
    final ITupleModel model = provider.getObservation().getValues( provider.getArguments() );

    final IAxis dateAxis = AxisUtils.findDateAxis( model.getAxisList() );
    final IAxis valueAxis = LayerProviderUtils.getValueAxis( provider, getTargetAxisId() );

    double diff = Double.MAX_VALUE;
    Number value = 0;

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

    return value.doubleValue();
  }

}
