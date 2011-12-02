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
package org.kalypso.zml.ui.table.dialogs.input.worker;

import java.util.Date;

import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.core.table.model.visitor.IZmlModelColumnVisitor;
import org.kalypso.zml.ui.table.dialogs.input.ZmlEinzelwert;

/**
 * @author Dirk Kuch
 */
public class FindNextValueVisitor implements IZmlModelColumnVisitor
{
  private final Date m_current;

  private IZmlValueReference m_reference;

  public FindNextValueVisitor( final ZmlEinzelwert current )
  {
    m_current = current.getDate();
  }

  /**
   * @see org.kalypso.zml.core.table.model.visitor.IZmlModelColumnVisitor#visit(org.kalypso.zml.core.table.model.references.IZmlValueReference)
   */
  @Override
  public void visit( final IZmlValueReference reference ) throws SensorException, CancelVisitorException
  {
    if( Objects.isNotNull( m_reference ) )
      throw new CancelVisitorException();

    final Date date = reference.getIndexValue();
    if( date.after( m_current ) )
    {
      m_reference = reference;
      throw new CancelVisitorException();
    }
  }

  public IZmlValueReference getReference( )
  {
    return m_reference;
  }
}