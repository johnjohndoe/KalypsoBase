/*
 * --------------- Kalypso-Header --------------------------------------------------------------------
 * 
 * This file is part of kalypso. Copyright (C) 2004, 2005 by:
 * 
 * Technical University Hamburg-Harburg (TUHH) Institute of River and coastal engineering Denickestr. 22 21073 Hamburg,
 * Germany http://www.tuhh.de/wb
 * 
 * and
 * 
 * Bjoernsen Consulting Engineers (BCE) Maria Trost 3 56070 Koblenz, Germany http://www.bjoernsen.de
 * 
 * This library is free software; you can redistribute it and/or modify it under the terms of the GNU Lesser General
 * Public License as published by the Free Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 * 
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License along with this library; if not, write to
 * the Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
 * 
 * Contact:
 * 
 * E-Mail: belger@bjoernsen.de schlienger@bjoernsen.de v.doemming@tuhh.de
 * 
 * ---------------------------------------------------------------------------------------------------
 */
package org.kalypso.ogc.sensor.view.observationDialog;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.core.status.StatusDialog;

/**
 * @author Gernot Belger
 */
public abstract class AbstractObservationAction extends Action implements IObservationAction
{
  private ObservationViewer m_viewer;

  public AbstractObservationAction( )
  {
    setText( getLabel() );
    setToolTipText( getTooltip() );
  }

  @Override
  public final void init( final ObservationViewer viewer )
  {
    m_viewer = viewer;
  }

  protected abstract String getLabel( );

  protected abstract String getTooltip( );

  protected final ObservationViewer getViewer( )
  {
    return m_viewer;
  }

  @Override
  public final void runWithEvent( final Event event )
  {
    final IStatus result = execute();
    if( result.isOK() || result.matches( IStatus.CANCEL ) )
      return;

    final Shell shell = m_viewer.getShell();
    new StatusDialog( shell, result, getLabel() ).open();
  }

  protected abstract IStatus execute( );
}
