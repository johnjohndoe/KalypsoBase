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
package org.kalypso.module.conversion.internal;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.module.conversion.AbstractLoggingOperation;
import org.kalypso.module.conversion.IProjectConversionOperation;
import org.kalypso.module.conversion.IProjectConverter;
import org.kalypso.module.internal.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class ProjectConversionOperation extends AbstractLoggingOperation implements IProjectConversionOperation
{
  private final IProjectConverter m_converter;

  private final IProject m_project;

  public ProjectConversionOperation( final IProject project, final IProjectConverter converter )
  {
    super( Messages.getString( "ProjectConversionOperation.0" ) ); //$NON-NLS-1$

    m_project = project;
    m_converter = converter;
  }

  /**
   * @see org.kalypso.module.conversion.IProjectConversionOperation#preConversion(org.eclipse.swt.widgets.Shell)
   */
  @Override
  public IStatus preConversion( final Shell shell )
  {
    return m_converter.preConversion( shell );
  }

  /**
   * @see org.kalypso.ui.rrm.wizards.conversion.AbstractLoggingOperation#doExecute(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  protected void doExecute( final IProgressMonitor monitor ) throws Exception
  {
    final String taskName = String.format( Messages.getString( "ProjectConversionOperation.1" ), m_project.getName(), m_converter.getLabel() ); //$NON-NLS-1$
    monitor.beginTask( taskName, 100 );

    try
    {
      final IStatus status = m_converter.execute( new SubProgressMonitor( monitor, 90 ) );
      getLog().add( status );
    }
    finally
    {
      m_project.refreshLocal( IResource.DEPTH_INFINITE, new SubProgressMonitor( monitor, 10 ) );
    }
  }

}