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
package org.kalypso.model.wspm.core.profil.base.interpolation;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.model.wspm.core.KalypsoModelWspmCorePlugin;
import org.kalypso.model.wspm.core.i18n.Messages;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.base.ExtrapolateMissingCoordinatesVisitor;
import org.kalypso.model.wspm.core.profil.base.InterpolateMissingCoordinatesVisitor;

/**
 * @author Dirk Kuch
 */
public class FillMissingProfileGeocoordinatesRunnable implements ICoreRunnableWithProgress
{

  private final IProfil m_profile;

  public FillMissingProfileGeocoordinatesRunnable( final IProfil profile )
  {
    m_profile = profile;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {

    m_profile.accept( new InterpolateMissingCoordinatesVisitor(), 1 );
    m_profile.accept( new ExtrapolateMissingCoordinatesVisitor(), 1 );

    return new Status( IStatus.OK, KalypsoModelWspmCorePlugin.getID(), Messages.getString("FillMissingProfileGeocoordinatesRunnable_0") ); //$NON-NLS-1$
  }

}