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
package de.openali.odysseus.service.ods.operation;

import org.eclipse.swt.widgets.Display;

import de.openali.odysseus.service.ods.util.DisplayHelper;
import de.openali.odysseus.service.ows.exception.OWSException;

/**
 * Abstract class for operations making use of the SWT-Display; we have to secure that these operations run in the
 * display thread, so thats what this abstract implementation handles; OWSException handling differs a bit, as we can
 * not declare throws for the Runnable's run operation: Instead of being thrown, the exception is announced via
 * setException() and will be thrown by the execute operation
 * 
 * @author burtscher1
 */
public abstract class AbstractODSDisplayOperation extends AbstractODSOperation implements Runnable
{

  private OWSException m_exception = null;

  @Override
  public void execute( ) throws OWSException
  {
    final DisplayHelper dh = DisplayHelper.getInstance();
    final Display d = dh.getDisplay();
    d.syncExec( this );
    final OWSException e = getException();
    if( e != null )
      throw e;
  }

  public void setException( final OWSException e )
  {
    m_exception = e;
  }

  public OWSException getException( )
  {
    return m_exception;
  }

}
