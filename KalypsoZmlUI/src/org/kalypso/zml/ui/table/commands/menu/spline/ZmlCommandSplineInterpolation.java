/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.ui.table.commands.menu.spline;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Status;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.transaction.TupleModelTransaction;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.ui.table.IZmlTable;
import org.kalypso.zml.ui.table.commands.ZmlHandlerUtil;
import org.kalypso.zml.ui.table.nat.layers.IZmlTableSelection;

import com.mxgraph.util.mxPoint;
import com.mxgraph.util.mxSpline;

/**
 * @author Dirk Kuch
 */
public class ZmlCommandSplineInterpolation extends AbstractHandler
{
  /**
   * <pre>
   * 
   *   - - s1 = = = x = = = x = = = s2 - -
   * 
   * s1 -> start point
   * s2 -> end point
   * x  -> fix stuetzstellen
   * =  -> will be replaced by spline interpolation point
   * 
   * </pre>
   */
  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    try
    {
      final IZmlTable table = ZmlHandlerUtil.getTable( event );
      final IZmlTableSelection selection = table.getSelection();
      final IZmlModelValueCell focus = selection.getFocusCell();
      final IZmlModelColumn column = focus.getColumn();
      final IZmlModelValueCell[] selected = selection.getSelectedCells( column );
      if( ArrayUtils.getLength( selected ) < 2 )
        throw new ExecutionException( "Spline-Interpolation fehlgeschlagen - selektieren Sie eine zweite Zelle!" );

      final TupleModelTransaction transaction = new TupleModelTransaction( column.getTupleModel(), column.getMetadata() );

      final IZmlModelValueCell[] intervall = findIntervall( selected );
      final IZmlModelValueCell s1 = intervall[0];
      final IZmlModelValueCell s2 = intervall[1];

      final ZmlStuetstellenVisitor visitor = new ZmlStuetstellenVisitor( s1, s2 );
      column.accept( visitor );

      final IZmlModelValueCell[] stuetzstellen = visitor.getStuetzstellen();

      final List<mxPoint> mxPoints = new ArrayList<mxPoint>();

      final Splines splines = new Splines( new DateRange( s1.getIndexValue(), s2.getIndexValue() ) );

      Collections.addAll( mxPoints, getPoints( splines, s1 ) );
      Collections.addAll( mxPoints, getPoints( splines, stuetzstellen ) );
      Collections.addAll( mxPoints, getPoints( splines, s2 ) );

      final mxSpline mxSpline = new mxSpline( mxPoints );
      splines.apply( mxSpline );

      final ApplySplineValuesVisitor applySplineVisitor = new ApplySplineValuesVisitor( splines, s1, s2, transaction );
      column.accept( applySplineVisitor );

      column.getTupleModel().execute( transaction );
    }
    catch( final SensorException e )
    {
      throw new ExecutionException( "Spline-Interpolation fehlgeschlagen.", e );
    }
    finally
    {
    }

    return Status.OK_STATUS;

  }

  private mxPoint[] getPoints( final Splines splines, final IZmlModelValueCell... references ) throws SensorException
  {
    final List<mxPoint> points = new ArrayList<mxPoint>();

    for( final IZmlModelValueCell reference : references )
    {
      final Date date = reference.getIndexValue();
      final Object value = reference.getValue();

      points.add( new mxPoint( splines.convertDate( date ), ((Number) value).doubleValue() ) );
    }

    return points.toArray( new mxPoint[] {} );
  }

  private IZmlModelValueCell[] findIntervall( final IZmlModelCell[] cells )
  {
    IZmlModelValueCell start = (IZmlModelValueCell) cells[0];
    IZmlModelValueCell end = (IZmlModelValueCell) cells[0];

    for( final IZmlModelCell cell : cells )
    {
      if( cell.getModelIndex() < start.getModelIndex() )
        start = (IZmlModelValueCell) cell;

      if( cell.getModelIndex() > end.getModelIndex() )
        end = (IZmlModelValueCell) cell;
    }

    return new IZmlModelValueCell[] { start, end };
  }
}
