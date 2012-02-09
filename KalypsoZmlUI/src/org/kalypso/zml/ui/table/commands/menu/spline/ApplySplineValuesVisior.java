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
package org.kalypso.zml.ui.table.commands.menu.spline;

import java.util.Date;

import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.status.KalypsoStati;
import org.kalypso.repository.IDataSourceItem;
import org.kalypso.zml.core.table.model.references.IZmlModelValueCell;
import org.kalypso.zml.core.table.model.references.ZmlValues;
import org.kalypso.zml.core.table.model.transaction.ZmlModelTransaction;
import org.kalypso.zml.core.table.model.visitor.IZmlModelColumnVisitor;

/**
 * @author Dirk Kuch
 */
public class ApplySplineValuesVisior implements IZmlModelColumnVisitor
{
  private final IZmlModelValueCell m_s1;

  private final IZmlModelValueCell m_s2;

  private final Splines m_splines;

  ZmlModelTransaction m_transaction = new ZmlModelTransaction();

  /**
   * @param s1
   *          interval start index
   * @param s2
   *          interval end index
   * @param stuetzstellen
   *          don't overwrite stuetzstellen
   */
  public ApplySplineValuesVisior( final Splines splines, final IZmlModelValueCell s1, final IZmlModelValueCell s2 )
  {
    m_s1 = s1;
    m_s2 = s2;
    m_splines = splines;
  }

  @Override
  public void visit( final IZmlModelValueCell reference ) throws SensorException
  {
    if( ZmlValues.isStuetzstelle( reference ) )
      return;

    final Date index = reference.getIndexValue();
    if( isBefore( index ) )
      return;
    else if( isAfter( index ) )
      return;

    final Double value = m_splines.getValue( index );
    m_transaction.add( reference, value, IDataSourceItem.SOURCE_MANUAL_CHANGED, KalypsoStati.BIT_USER_MODIFIED );
  }

  private boolean isAfter( final Date index ) throws SensorException
  {
    final Date date = m_s2.getIndexValue();

    if( index.after( date ) )
      return true;
    else if( index.equals( date ) )
      return true;

    return false;
  }

  private boolean isBefore( final Date index ) throws SensorException
  {
    final Date date = m_s1.getIndexValue();

    if( index.before( date ) )
      return true;
    else if( index.equals( date ) )
      return true;

    return false;
  }

  public ZmlModelTransaction getTransaction( )
  {
    return m_transaction;
  }

}
