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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
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
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;
import de.openali.odysseus.chart.framework.model.style.impl.LineStyle;

/**
 * @author Dirk Kuch
 */
public class AxisSelectionLayer extends AbstractChartLayer
{
  private Point m_position;

  public AxisSelectionLayer( final ILayerProvider provider )
  {
    super( provider );
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

    final ILineStyle style = new LineStyle( 3, new RGB( 255, 0, 0 ), 100, 0F, new float[] { 12, 7 }, LINEJOIN.MITER, LINECAP.ROUND, 1, true );

    final PolylineFigure polylineFigure = new PolylineFigure();
    polylineFigure.setStyle( style );
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
    m_position = position;
// final IAxis domainAxis = getDomainAxis();
// final int screenValue = domainAxis.getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) ? point.x :
// point.y;
// m_selection = screenToNumeric( screenValue );

    getEventHandler().fireLayerContentChanged( this );
  }
}
