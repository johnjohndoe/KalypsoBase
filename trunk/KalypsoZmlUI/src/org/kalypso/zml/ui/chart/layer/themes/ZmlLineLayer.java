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
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IAxisRange;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.provider.IObsProvider;
import org.kalypso.ogc.sensor.provider.IObsProviderListener;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.DataOperatorHelper;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * @author Dirk Kuch
 * @author kimwerner
 */
public class ZmlLineLayer extends AbstractLineLayer
{
  private ITupleModel m_model;

  private final IDataOperator<Date> m_dateDataOperator = new DataOperatorHelper().getDataOperator( Date.class );

  private final IDataOperator<Number> m_numberDataOperator = new DataOperatorHelper().getDataOperator( Number.class );

  private final IAxis m_valueAxis;

  private final IObsProvider m_provider;

  private final IObsProviderListener m_observationProviderListener = new IObsProviderListener()
  {
    @Override
    public void observationReplaced( )
    {
      onObservationLoaded();
    }

    /**
     * @see org.kalypso.ogc.sensor.template.IObsProviderListener#observationChangedX(java.lang.Object)
     */
    @Override
    public void observationChanged( final Object source )
    {
      onObservationChanged();
    }
  };

  public ZmlLineLayer( final IObsProvider provider, final IAxis valueAxis, final ILineStyle lineStyle, final IPointStyle pointStyle )
  {
    super( lineStyle, pointStyle );
    m_provider = provider;
    m_valueAxis = valueAxis;

    provider.addListener( m_observationProviderListener );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_provider != null )
    {
      m_provider.removeListener( m_observationProviderListener );
      m_provider.dispose();
    }

    super.dispose();
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractChartLayer#isVisible()
   */
  @Override
  public boolean isVisible( )
  {
    if( !super.isVisible() )
      return false;
    // FIXME: what IS that???? Does this makes any sense??? Please AT LEAST comment such strange stuff!
// else if( getTargetRange( null ) == null )
// return false;
// else if( getDomainRange() == null )
// return false;

    return true;
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer#createLegendEntries()
   */
  @Override
  public ILegendEntry[] createLegendEntries( )
  {
    final LegendEntry le = new LegendEntry( this, getTitle() )
    {
      @Override
      public void paintSymbol( final GC gc, final Point size )
      {
        final int sizeX = size.x;
        final int sizeY = size.y;

        final ArrayList<Point> path = new ArrayList<Point>();
        path.add( new Point( 0, sizeX / 2 ) );
        path.add( new Point( sizeX / 5, sizeY / 2 ) );
        path.add( new Point( sizeX / 5 * 2, sizeY / 4 ) );
        path.add( new Point( sizeX / 5 * 3, sizeY / 4 * 3 ) );
        path.add( new Point( sizeX / 5 * 4, sizeY / 2 ) );
        path.add( new Point( sizeX, sizeY / 2 ) );

        drawLine( gc, path );
      }

    };

    return new ILegendEntry[] { le };
  }

  private ITupleModel getModel( ) throws SensorException
  {
    if( m_model == null )
    {
      final IRequest request = m_provider.getArguments();
      final IObservation observation = m_provider.getObservation();
      if( observation != null )
        m_model = observation.getValues( request );
    }

    return m_model;
  }

  protected void onObservationLoaded( )
  {
    m_model = null;
    final IObservation observation = m_provider.getObservation();
    setVisible( observation != null );

    getEventHandler().fireLayerVisibilityChanged( this );
    getEventHandler().fireLayerContentChanged( this );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange<Number> getDomainRange( )
  {
    try
    {
      final ITupleModel model = getModel();
      if( model == null )
        return null;

      final org.kalypso.ogc.sensor.IAxis dateAxis = AxisUtils.findDateAxis( model.getAxisList() );
      final IAxisRange range = model.getRange( dateAxis );
      if( range == null )
        return null;

      final Date min = (Date) range.getLower();
      final Date max = (Date) range.getUpper();

      return new DataRange<Number>( m_dateDataOperator.logicalToNumeric( min ), m_dateDataOperator.logicalToNumeric( max ) );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );

      return null;
    }
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getTargetRange()
   */
  @Override
  public IDataRange<Number> getTargetRange( final IDataRange<Number> domainIntervall )
  {
    try
    {
      final ITupleModel model = getModel();
      if( model == null )
        return null;

      if( domainIntervall == null )
      {
        final IAxisRange range = model.getRange( m_valueAxis );
        if( range == null )
          return null;

        final IDataRange<Number> numRange = new DataRange<Number>( m_numberDataOperator.logicalToNumeric( (Number) range.getLower() ), m_numberDataOperator.logicalToNumeric( (Number) range.getUpper() ) );

        return numRange;
      }
      else
      {
        final org.kalypso.ogc.sensor.IAxis dateAxis = AxisUtils.findDateAxis( model.getAxisList() );

        Number minValue = null;
        Number maxValue = null;
        for( int i = 0; i < model.size(); i++ )
        {

          final Object domainValue = model.get( i, dateAxis );

          if( domainValue == null )
            continue;
          if( minValue == null && ((Date) domainValue).getTime() > domainIntervall.getMin().longValue() )
          {
            minValue = (Number) model.get( i - 1, m_valueAxis );
          }
          if( maxValue == null && ((Date) domainValue).getTime() > domainIntervall.getMax().longValue() )
          {
            maxValue = (Number) model.get( i, m_valueAxis );
          }
        }
        return new DataRange<Number>( m_numberDataOperator.logicalToNumeric( minValue ), m_numberDataOperator.logicalToNumeric( maxValue ) );
      }
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );

      return null;
    }
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint( final GC gc )
  {
    try
    {
      final ITupleModel model = getModel();
      if( model == null )
        return;

      final org.kalypso.ogc.sensor.IAxis dateAxis = AxisUtils.findDateAxis( model.getAxisList() );
      final List<Point> path = new ArrayList<Point>();

      for( int i = 0; i < model.size(); i++ )
      {
        try
        {
          final Object domainValue = model.get( i, dateAxis );
          final Object targetValue = model.get( i, m_valueAxis );
          if( domainValue == null || targetValue == null )
            continue;

          final Point screen = getCoordinateMapper().numericToScreen( m_dateDataOperator.logicalToNumeric( (Date) domainValue ), m_numberDataOperator.logicalToNumeric( (Double) targetValue ) );
          path.add( screen );
        }
        catch( final SensorException e )
        {
          KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
        }
      }

      drawLine( gc, path );
      drawPoints( gc, path );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  protected void onObservationChanged( )
  {
    m_model = null;
    getEventHandler().fireLayerContentChanged( this );
  }

}
