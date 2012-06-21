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
package org.kalypso.zml.core.table.model.editing;

import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.ogc.sensor.transaction.TupleModelTransaction;
import org.kalypso.repository.IDataSourceItem;
import org.kalypso.zml.core.KalypsoZmlCore;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.interpolation.ZmlInterpolation;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;
import org.kalypso.zml.core.table.model.visitor.FindNeighbourStuetzstellenVisitor;

/**
 * updated value will be a new st�tzstelle. update all values between
 * 
 * <pre>
 *           ( update too     )   ( update too  )
 *         x ------------------ x --------------- x
 *      stuetz                stuetz            stuetz
 *      stelle                stelle            stelle
 *        i                    NEW                n
 * 
 * </pre>
 * 
 * @author Dirk Kuch
 */
public class InterpolatedValueEditingStrategy extends AbstractEditingStrategy
{

  public InterpolatedValueEditingStrategy( final ZmlModelViewport viewport )
  {
    super( viewport );
  }

  @Override
  public void setValue( final IZmlModelValueCell cell, final String value )
  {
    try
    {
      /** update current cell */
      final Number targetValue = (Number) getTargetValue( cell, value );

      cell.doUpdate( targetValue, IDataSourceItem.SOURCE_MANUAL_CHANGED, KalypsoStati.BIT_USER_MODIFIED );

      /** update interpolated values before and after */
      final FindNeighbourStuetzstellenVisitor visitor = new FindNeighbourStuetzstellenVisitor( cell );
      cell.getColumn().accept( visitor );

      interpolate( visitor.getBefore(), cell, -1 );
      interpolate( cell, visitor.getAfter(), 1 );
    }
    catch( final SensorException e )
    {
      KalypsoZmlCore.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  private void interpolate( final IZmlModelValueCell before, final IZmlModelValueCell current, final int direction ) throws SensorException
  {
    final IZmlModelValueCell base = Objects.firstNonNull( before, current );
    final IZmlModelColumn column = base.getColumn();

    final TupleModelTransaction transaction = new TupleModelTransaction( column.getTupleModel(), column.getMetadata() );
    try
    {
      final Double defaultValue = ZmlInterpolation.getDefaultValue( column.getMetadata() );

      if( direction < 0 && before == null )
      {
        ZmlInterpolation.fillValue( transaction, column.getValueAxis(), 0, current.getModelIndex(), defaultValue );
      }
      else if( direction >= 0 && current == null )
      {
        if( ZmlInterpolation.isSetLastValidValue( column.getMetadata() ) )
        {
          final Double value = (Double) before.getValue();
          ZmlInterpolation.fillValue( transaction, column.getValueAxis(), before.getModelIndex() + 1, column.size(), value );
        }
        else
          ZmlInterpolation.fillValue( transaction, column.getValueAxis(), before.getModelIndex() + 1, column.size(), defaultValue );
      }
      else
        ZmlInterpolation.interpolate( column.getTupleModel(), transaction, column.getValueAxis(), before.getModelIndex(), current.getModelIndex() );
    }
    finally
    {
      column.getTupleModel().execute( transaction );
    }
  }

  @Override
  public boolean isAggregated( )
  {
    return false;
  }

}
