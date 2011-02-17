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
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.IAxisRange;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.ObservationTokenHelper;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.layer.IZmlLayer;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.data.impl.DataRange;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.DataOperatorHelper;
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
  private final IDataOperator<Date> m_dateDataOperator = new DataOperatorHelper().getDataOperator( Date.class );

  private IZmlLayerDataHandler m_handler;

  private String m_labelDescriptor;

  private final IDataOperator<Number> m_numberDataOperator = new DataOperatorHelper().getDataOperator( Number.class );

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

  /**
   * @see de.openali.odysseus.chart.ext.base.layer.AbstractLineLayer#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_handler != null )
      m_handler.dispose();

    super.dispose();
  }

  /**
   * @see org.kalypso.zml.ui.chart.layer.themes.IZmlLayer#getDataHandler()
   */
  @Override
  public IZmlLayerDataHandler getDataHandler( )
  {
    return m_handler;
  }

  public IDataOperator<Date> getDateDataOperator( )
  {
    return m_dateDataOperator;
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

      final org.kalypso.ogc.sensor.IAxis dateAxis = AxisUtils.findDateAxis( model.getAxes() );
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

  @Override
  public synchronized ILegendEntry[] getLegendEntries( )
  {
    return createLegendEntries();
  }

  public IDataOperator<Number> getNumberDataOperator( )
  {
    return m_numberDataOperator;
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

      final IAxis axis = m_handler.getValueAxis();
      if( axis == null )
        return null;

      if( Objects.isNull( AxisUtils.findAxis( model.getAxes(), axis.getType() ) ) )
        return null;

      if( domainIntervall == null )
      {
        final IAxisRange range = model.getRange( axis );
        if( range == null )
          return null;

        final IDataRange<Number> numRange = new DataRange<Number>( m_numberDataOperator.logicalToNumeric( (Number) range.getLower() ), m_numberDataOperator.logicalToNumeric( (Number) range.getUpper() ) );

        return numRange;
      }
      else
      {
        final org.kalypso.ogc.sensor.IAxis dateAxis = AxisUtils.findDateAxis( model.getAxes() );

        Number minValue = null;
        Number maxValue = null;
        for( int i = 0; i < model.size(); i++ )
        {

          final Object domainValue = model.get( i, dateAxis );

          if( domainValue == null )
            continue;
          if( minValue == null && ((Date) domainValue).getTime() > domainIntervall.getMin().longValue() )
          {
            minValue = (Number) model.get( i - 1, axis );
          }
          if( maxValue == null && ((Date) domainValue).getTime() > domainIntervall.getMax().longValue() )
          {
            maxValue = (Number) model.get( i, axis );
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
      final ITupleModel model = m_handler.getModel();
      if( model == null )
        return;

      setLineThemeStyles();

      final List<Point> path = new ArrayList<Point>();
      model.accept( new LineLayerModelVisitor( this, path ) );

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
    if( m_handler != null )
      m_handler.dispose();

    m_handler = handler;
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
}
