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
package org.kalypso.zml.ui.chart.layer.themes;

import java.util.Date;
import java.util.List;

import org.eclipse.swt.graphics.Point;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.timeseries.AxisUtils;
import org.kalypso.ogc.sensor.visitor.ITupleModelVisitor;
import org.kalypso.ogc.sensor.visitor.ITupleModelVisitorValue;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.core.diagram.layer.IZmlLayerFilter;
import org.kalypso.zml.ui.KalypsoZmlUI;

import de.openali.odysseus.chart.ext.base.layer.ChartLayerUtils;

/**
 * @author Dirk Kuch
 */
public class LineLayerModelVisitor implements ITupleModelVisitor
{
  private final ZmlLineLayer m_layer;

  private final List<Point> m_path;

  private IAxis m_dateAxis;

  private final IZmlLayerFilter m_filter;

  public LineLayerModelVisitor( final ZmlLineLayer layer, final List<Point> path, final IZmlLayerFilter filter )
  {
    m_layer = layer;
    m_path = path;
    m_filter = filter;
  }

  private IAxis getValueAxis( )
  {
    final IZmlLayerDataHandler handler = m_layer.getDataHandler();

    return handler.getValueAxis();
  }

  private IAxis getDateAxis( ) throws SensorException
  {
    if( m_dateAxis == null )
    {
      final IZmlLayerDataHandler handler = m_layer.getDataHandler();
      final ITupleModel model = handler.getModel();
      final IAxis[] axes = model.getAxes();

      m_dateAxis = AxisUtils.findDateAxis( axes );
    }

    return m_dateAxis;
  }

  /**
   * @see org.kalypso.ogc.sensor.visitor.ITupleModelVisitor#visit(org.kalypso.ogc.sensor.visitor.ITupleModelVisitorValue)
   */
  @Override
  public void visit( final ITupleModelVisitorValue container )
  {
    try
    {

      final IAxis dateAxis = getDateAxis();
      final IAxis valueAxis = getValueAxis();
      if( Objects.isNull( dateAxis, valueAxis ) )
        return;

      if( !container.hasAxis( getDateAxis().getType(), valueAxis.getType() ) )
        return;

      final Object dateObject = container.get( dateAxis );
      final Object valueObject = container.get( valueAxis );
      if( Objects.isNull( dateObject, valueObject ) || isFiltered( valueObject ) )
        return;

      final Date adjusted = ChartLayerUtils.addTimezoneOffset( (Date) dateObject );

      final Point screen = m_layer.getCoordinateMapper().numericToScreen( m_layer.getRangeHandler().getDateDataOperator().logicalToNumeric( adjusted ), m_layer.getRangeHandler().getNumberDataOperator().logicalToNumeric( (Double) valueObject ) );
      m_path.add( screen );
    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  private boolean isFiltered( final Object valueObject )
  {
    if( !(valueObject instanceof Number) )
      return true;

    final Number value = (Number) valueObject;

    return m_filter.isFiltered( value );
  }
}
