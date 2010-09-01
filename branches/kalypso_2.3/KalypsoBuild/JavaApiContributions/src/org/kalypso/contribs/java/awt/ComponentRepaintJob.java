/** This file is part of Kalypso
 *
 *  Copyright (c) 2008 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
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