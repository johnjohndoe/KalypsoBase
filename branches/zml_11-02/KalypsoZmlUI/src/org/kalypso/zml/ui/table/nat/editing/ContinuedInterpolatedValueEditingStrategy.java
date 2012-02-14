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
package org.kalypso.zml.ui.table.nat.editing;

import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.repository.IDataSourceItem;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.core.table.model.VisibleZmlModelFacade;
import org.kalypso.zml.core.table.model.references.IZmlModelCell;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.references.ZmlValues;
import org.kalypso.zml.core.table.model.transaction.ZmlModelTransaction;
import org.kalypso.zml.ui.KalypsoZmlUI;

/**
 * updated value will be a new stützstelle. update all values between s_new and s_next. set updated values to s_new
 * value
 * 
 * <pre>
 *                                 ( update too  )
 *         x ------------------ x --------------- x
 *      stuetz                stuetz            stuetz
 *      stelle                stelle            stelle
 *        s_1                  s_new            s_next
 * 
 * </pre>
 * 
 * @author Dirk Kuch
 */
public class ContinuedInterpolatedValueEditingStrategy extends AbstractEditingStrategy
{

  public ContinuedInterpolatedValueEditingStrategy( final VisibleZmlModelFacade facade, final IZmlModelColumn column )
  {
    super( facade, column );
  }

// @Override
// public String getValue( final IZmlModelRow row )
// {
// try
// {
// final IZmlModelCell reference = row.get( getColumn() );
// if( Objects.isNull( reference ) )
//        return ""; //$NON-NLS-1$
// else if( !(reference instanceof IZmlModelValueCell) )
// return "";
//
// final IZmlModelValueCell cell = (IZmlModelValueCell) reference;
// final Object value = cell.getValue();
//
// //
//      return String.format( style.getTextFormat() == null ? "%s" : style.getTextFormat(), value ); //$NON-NLS-1$
// }
// catch( final Throwable t )
// {
// t.printStackTrace();
// }
//
// return null;
// }

  @Override
  public void setValue( final IZmlModelRow row, final String value )
  {
    try
    {
      /** update current cell */
      final IZmlModelCell reference = row.get( getColumn() );
      if( !(reference instanceof IZmlModelValueCell) )
        return;

      final IZmlModelValueCell current = (IZmlModelValueCell) reference;

      final Number targetValue = getTargetValue( value );

      final ZmlModelTransaction transaction = new ZmlModelTransaction();

      try
      {
        transaction.add( current, targetValue, IDataSourceItem.SOURCE_MANUAL_CHANGED, KalypsoStati.BIT_USER_MODIFIED );

        IZmlModelCell next = getModel().findNextCell( current );

        while( next instanceof IZmlModelValueCell )
        {
          final IZmlModelValueCell ref = (IZmlModelValueCell) next;
          if( ZmlValues.isStuetzstelle( ref ) )
            break;

          transaction.add( ref, targetValue, IDataSourceItem.SOURCE_INTERPOLATED_WECHMANN_VALUE, KalypsoStati.BIT_OK );
          next = getModel().findNextCell( next );
        }
      }
      finally
      {
        transaction.execute();
      }

    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  @Override
  public boolean isAggregated( )
  {
    return false;
  }

}
