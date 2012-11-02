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
package org.kalypso.commons.databinding.swt;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.databinding.validation.TypedValidator;
import org.kalypso.commons.internal.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class WorkbenchFileValidator extends TypedValidator<IPath>
{
  private boolean m_isOptional;

  public WorkbenchFileValidator( )
  {
    super( IPath.class, IStatus.ERROR, StringUtils.EMPTY );
  }

  @Override
  protected IStatus doValidate( final IPath value ) throws CoreException
  {
    if( value == null || StringUtils.isEmpty( value.toPortableString() ) )
    {
      if( m_isOptional )
        return Status.OK_STATUS;

      fail( Messages.getString("WorkbenchFileValidator_0") ); //$NON-NLS-1$
    }

    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final IResource member = root.findMember( value );
    if( member == null )
      fail( Messages.getString("WorkbenchFileValidator_1") ); //$NON-NLS-1$

    if( !(member instanceof IFile) )
      fail( Messages.getString("WorkbenchFileValidator_2") ); //$NON-NLS-1$

    return Status.OK_STATUS;
  }

  public void setIsOptional( final boolean isOptional )
  {
    m_isOptional = isOptional;
  }
}