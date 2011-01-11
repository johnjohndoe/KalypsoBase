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
import org.kalypso.ogc.sensor.IAxisRange;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.layer.IZmlLayer;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.ext.base.layer.AbstractBarLayer;
import de.openali.odysseus.chart.ext.base.layer.ChartLayerUtils;
import de.openali.odysseus.chart.factory.provider.ILayerProvider;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PolygonFigure;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.DataOperatorHelper;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;

/**
 * @author Dirk Kuch
 * @author kimwerner
 */
public class ZmlBarLayer extends AbstractBarLayer implements IZmlLayer
{
  private final IDataOperator<Date> m_dateDataOperator = new DataOperatorHelper().getDataOperator( Date.class );

  private final IDataOperator<Number> m_targetDataOperator = new DataOperatorHelper().getDataOperator( Number.class );

  private final IZmlLayerDataHandler m_handler;

  protected ZmlBarLayer( final ILayerProvider layerProvider, final IZmlLayerDataHandler handler, final IAreaStyle style )
  {
    super( layerProvider, style );

    m_handler = handler;
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractBarLayer#dispose()
   */
  @Override
  public void dispose( )
  {
    m_handler.dispose();

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

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.IChartLayer#getDomainRange()
   */
  @Override
  public IDataRange<Number> getDomainRange( )
  {
    try
    {
      final ITupleModel model = m_handler.getModel();
      if( model == null )
        return null;

      final org.kalypso.ogc.sensor.IAxis dateAxis = AxisUtils.findDateAxis( model.getAxisList() );
      final IAxisRange range = model.getRange( dateAxis );
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
      final ITupleModel model = m_handler.getModel();
      if( model == null )
        return null;

      /** hack for polder control which consists of boolean values */
      final Class< ? > dataClass = m_handler.getValueAxis().getDataClass();
      if( Boolean.class.equals( dataClass ) )
        return new DataRange<Number>( 0, 1 );

      final IAxisRange range = model.getRange( m_handler.getValueAxis() );

      final Number max = m_targetDataOperator.logicalToNumeric( (Number) range.getUpper() );

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
      final ITupleModel model = m_handler.getModel();
      if( model == null )
        return;

      final org.kalypso.ogc.sensor.IAxis dateAxis = AxisUtils.findDateAxis( model.getAxisList() );
      final PolygonFigure pf = getPolygonFigure();
      final Point base = getCoordinateMapper().numericToScreen( 0.0, 0.0 );

      Point lastScreen = null;
      for( int i = 0; i < model.size(); i++ )
      {
        try
        {
          final Object domainValue = model.get( i, dateAxis );
          final Object targetValue = model.get( i, m_handler.getValueAxis() );
          if( domainValue == null || targetValue == null )
            continue;

          final Number logicalDomain = m_dateDataOperator.logicalToNumeric( ChartLayerUtils.addTimezoneOffset( (Date) domainValue ) );
          final Number logicalTarget = m_targetDataOperator.logicalToNumeric( (Number) targetValue );
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

  /**
   * @see org.kalypso.zml.ui.chart.layer.themes.IZmlLayer#getDataHandler()
   */
  @Override
  public IZmlLayerDataHandler getDataHandler( )
  {
    return m_handler;
  }
}
