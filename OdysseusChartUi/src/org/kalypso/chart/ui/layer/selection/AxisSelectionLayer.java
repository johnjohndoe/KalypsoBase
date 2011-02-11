/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
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
package org.kalypso.chart.ui.layer.selection;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.ui.layer.selection.utils.AxisOffsetVisitor;
import org.kalypso.chart.ui.layer.selection.utils.FindAxisVisitor;
import org.kalypso.commons.java.lang.Strings;

import de.openali.odysseus.chart.factory.layer.AbstractChartLayer;
import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.registry.IMapperRegistry;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;

/**
 * @author Dirk Kuch
 */
public class AxisSelectionLayer extends AbstractChartLayer
{
  private Point m_position;

  private final ILineStyle m_style;

  public AxisSelectionLayer( final ILayerProvider provider, final ILineStyle style )
  {
    super( provider );
    m_style = style;
  }

  /**
   * @see de.openali.odysseus.chart.factory.layer.AbstractChartLayer#paint(org.eclipse.swt.graphics.GC)
   */
  @Override
  public void paint( final GC gc )
  {
    if( m_position == null )
      return;

    final IAxis targetAxis = getCoordinateMapper().getTargetAxis();
    final IDataRange<Number> targetRange = targetAxis.getNumericRange();

    final Integer y0 = targetAxis.numericToScreen( targetRange.getMin() );
    final Integer y1 = targetAxis.numericToScreen( targetRange.getMax() );

// final ILineStyle style = new LineStyle( 3, new RGB( 0, 255, 0 ), 100, 0F, new float[] { 12, 7 }, LINEJOIN.MITER,
// LINECAP.ROUND, 1, true );

    final PolylineFigure polylineFigure = new PolylineFigure();
    polylineFigure.setStyle( m_style );

    polylineFigure.setPoints( new Point[] { new Point( m_position.x, y0 ), new Point( m_position.x, y1 ) } );
    polylineFigure.paint( gc );
  }

  public IAxis[] getAxes( )
  {
    final IChartModel model = getProvider().getModel();
    final IMapperRegistry registry = model.getMapperRegistry();
    final FindAxisVisitor visitor = new FindAxisVisitor( findAxisIdentifiers() );
    registry.accept( visitor );

    return visitor.getAxes();
  }

  private String[] findAxisIdentifiers( )
  {
    final Set<String> identifiers = new HashSet<String>();

    final IParameterContainer container = getProvider().getParameterContainer();

    final String[] keys = container.findAllKeys( "axis" );
    for( final String key : keys )
    {
      final String value = container.getParameterValue( key, "" );
      if( Strings.isNotEmpty( value ) )
        identifiers.add( value );
    }

    return identifiers.toArray( new String[] {} );
  }

  public void setMousePosition( final Point position )
  {

    final IChartModel model = getProvider().getModel();
    final IMapperRegistry registry = model.getMapperRegistry();

    final AxisOffsetVisitor visitor = new AxisOffsetVisitor( getDomainAxis().getPosition() );
    registry.accept( visitor );

    // TODO handle / update y position
    m_position = new Point( position.x - visitor.getOffset(), position.y );

    getEventHandler().fireLayerContentChanged( this );

    final IAxis axis = getDomainAxis();
    final Number value = axis.screenToNumeric( m_position.x );

    final Date date = new Date( value.longValue() );
    final SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd'T'HH:mm:ss" );

    // TODO date offset

    System.out.println( sdf.format( date ) );

  }
}
