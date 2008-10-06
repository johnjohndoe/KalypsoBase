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
package org.kalypso.contribs.java.awt;

import java.awt.Component;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.contribs.java.JavaApiContributionsPlugin;

/**
 * A job which calls {@link Component#repaint()} repeatedly.
 * <p>
 * The job runs until {@link #cancel()} has been called.
 * 
 * @author Gernot Belger
 */
public class ComponentRepaintJob extends Job
{
  private final Component m_component;

  private final int m_repaintMillis;

  /**
   * @param component
   *            The component that get reainted.
   * @param repaintMillis
   *            Time in milliseconds between two calls to repaint.
   */
  public ComponentRepaintJob( final Component component, final int repaintMillis )
  {
    super( "Repaint job" );

    m_component = component;
    m_repaintMillis = repaintMillis;
  }

  /**
   * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  protected IStatus run( final IProgressMonitor monitor )
  {
    while( !monitor.isCanceled() )
    {
      try
      {
        m_component.repaint();

        Thread.sleep( m_repaintMillis );
      }
      catch( final InterruptedException e )
      {
        return new Status( IStatus.ERROR, JavaApiContributionsPlugin.getDefault().getBundle().getSymbolicName(), "repaint thread was interrupted", e );
      }
    }

    return Status.OK_STATUS;
  }
}