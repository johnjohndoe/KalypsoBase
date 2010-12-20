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
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.ext.base.layer.AbstractBarLayer;
import de.openali.odysseus.chart.ext.base.layer.ChartLayerUtils;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PolygonFigure;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.DataOperatorHelper;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;

/**
 * @author Dirk Kuch
 * @author kimwerner
 */
public class ZmlBarLayer extends AbstractBarLayer
{
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

  private ITupleModel m_model;

  private final IDataOperator<Date> m_dateDataOperator = new DataOperatorHelper().getDataOperator( Date.class );

  private final IAxis m_valueAxis;

  private final IObsProvider m_provider;

  private final IDataOperator m_targetDataOperator;

  public ZmlBarLayer( final ICoordinateMapper coordinateMapper, final IDataOperator targetDataOperator, final IObsProvider provider, final IAxis valueAxis, final IAreaStyle style )
  {
    super( style );

    m_targetDataOperator = targetDataOperator;

    m_provider = provider;
    m_valueAxis = valueAxis;
    setCoordinateMapper( coordinateMapper );

    synchronized( provider )
    {
      provider.addListener( m_observationProviderListener );

      if( !provider.isLoaded() )
      {
        setVisible( false );
      }
    }
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractBarLayer#dispose()
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

  @Override
  public ILegendEntry[] createLegendEntries( )
  {
    final List<ILegendEntry> entries = new ArrayList<ILegendEntry>();
    final PolygonFigure pf = getPolygonFigure();
    if( pf.getStyle().isVisible() )
    {

      final LegendEntry entry = new LegendEntry( this, getTitle() )
      {
        @Override
        public void paintSymbol( final GC gc, final Point size )
        {
          final int height = size.x;
          final int width = size.y;
          ArrayList<Point> path = new ArrayList<Point>();
          path.add( new Point( 0, height ) );
          path.add( new Point( 0, height / 2 ) );
          path.add( new Point( width / 2, height / 2 ) );
          path.add( new Point( width / 2, height ) );
          pf.setPoints( path.toArray( new Point[] {} ) );
          pf.paint( gc );

          path = new ArrayList<Point>();
          path.add( new Point( width / 2, height ) );
          path.add( new Point( width / 2, 0 ) );
          path.add( new Point( width, 0 ) );
          path.add( new Point( width, height ) );
          pf.setPoints( path.toArray( new Point[] {} ) );
          pf.paint( gc );
        }

      };

      entries.add( entry );
    }
    return entries.toArray( new ILegendEntry[] {} );
  }

  protected void onObservationLoaded( )
  {
    m_model = null;
    final IObservation observation = m_provider.getObservation();
    setVisible( observation != null );
  }

  protected void onObservationChanged( )
  {
    m_model = null;
    getEventHandler().fireLayerContentChanged( this );
  }

  private ITupleModel getModel( ) throws SensorException
  {
    if( m_model == null )
      m_model = m_provider.getObservation().getValues( m_provider.getArguments() );

    return m_model;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange<Number> getDomainRange( )
  {
    try
    {
      final org.kalypso.ogc.sensor.IAxis dateAxis = AxisUtils.findDateAxis( getModel().getAxisList() );
      final IAxisRange range = getModel().getRange( dateAxis );
      final Date min = (Date) range.getLower();
      final Date max = (Date) range.getUpper();

      final IDataRange<Number> numRange = new DataRange<Number>( m_dateDataOperator.logicalToNumeric( min ), m_dateDataOperator.logicalToNumeric( max ) );
      return numRange;
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
      /** hack for polder control which consists of boolean values */
      final Class< ? > dataClass = m_valueAxis.getDataClass();
      if( Boolean.class.equals( dataClass ) )
        return new DataRange<Number>( 0, 1 );

      final IAxisRange range = getModel().getRange( m_valueAxis );

      final Number max = m_targetDataOperator.logicalToNumeric( range.getUpper() );

      final IDataRange<Number> numRange = new DataRange<Number>( 0, Math.max( 1.0, max.doubleValue() ) );
      return numRange;
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
      final org.kalypso.ogc.sensor.IAxis dateAxis = AxisUtils.findDateAxis( getModel().getAxisList() );
      final PolygonFigure pf = getPolygonFigure();
      final Point base = getCoordinateMapper().numericToScreen( 0.0, 0.0 );

      Point lastScreen = null;
      for( int i = 0; i < getModel().size(); i++ )
      {
        try
        {
          final Object domainValue = getModel().get( i, dateAxis );
          final Object targetValue = getModel().get( i, m_valueAxis );
          if( domainValue == null || targetValue == null )
            continue;

          final Number logicalDomain = m_dateDataOperator.logicalToNumeric( ChartLayerUtils.addTimezoneOffset( (Date) domainValue ) );
          final Number logicalTarget = m_targetDataOperator.logicalToNumeric( targetValue );
          final Point screen = getCoordinateMapper().numericToScreen( logicalDomain, logicalTarget );

          // don't draw empty lines only rectangles
          if( screen.y != base.y )
          {
            final int lastScreenX = lastScreen == null ? screen.x : lastScreen.x;
            pf.setPoints( new Point[] { new Point( lastScreenX, base.y ), new Point( lastScreenX, screen.y ), screen, new Point( screen.x, base.y ) } );
            pf.paint( gc );
          }
          lastScreen = screen;
        }
        catch( final Throwable t )
        {
          KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
        }
      }
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }
}
