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
package org.kalypso.module.conversion;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;

/**
 * @author Gernot Belger
 */
public class ProjectConversionOperation extends AbstractLoggingOperation
{
  private final IProjectConverter[] m_converters;

  private final IProject m_project;

  public ProjectConversionOperation( final IProject project, final IProjectConverter[] converters )
  {
    super( "Projektkonvertierung" );

    m_project = project;
    m_converters = converters;
  }

  /**
   * @see org.kalypso.ui.rrm.wizards.conversion.AbstractLoggingOperation#doExecute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  protected void doExecute( final IProgressMonitor monitor ) throws Throwable
  {
    final String taskName = String.format( "Converting project '%s'", m_project.getName() );
    monitor.beginTask( taskName, m_converters.length + 1 );

    try
    {
      for( final IProjectConverter converter : m_converters )
      {
        monitor.subTask( String.format( "Step '%s'", converter.getLabel() ) );

        // REMARK: we halt on real errors; later we might to ask the user if he wishes to continue
        final IStatus status = converter.execute( new SubProgressMonitor( monitor, 1 ) );
        getLog().add( status );
        if( status.matches( IStatus.ERROR ) )
          return;
      }
    }
    finally
    {
      m_project.refreshLocal( IResource.DEPTH_INFINITE, new SubProgressMonitor( monitor, 1 ) );
    }
  }

}
