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
package org.kalypso.model.wspm.core.gml.validation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.IProfileProvider;
import org.kalypso.model.wspm.core.gml.IProfileProviderListener;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Dirk Kuch
 */
public class ProfileFetureValidationListener implements IProfileProviderListener
{
  private IProfil m_profile;

  private ValidationProfilListener m_listener;

  public ProfileFetureValidationListener( final IProfileFeature feature )
  {
    final Job job = new Job( "Registering initial profile validation listener" )
    {
      @Override
      protected IStatus run( final IProgressMonitor monitor )
      {
        onProfilProviderChanged( feature );

        return Status.OK_STATUS;
      }
    };

    job.setSystem( true );
    job.setUser( false );

    job.schedule();
  }

  @Override
  public void onProfilProviderChanged( final IProfileProvider provider )
  {
    if( Objects.isNotNull( m_profile, m_listener ) )
      m_profile.removeProfilListener( m_listener );

    final IProfil profile = provider.getProfil();
    m_profile = profile;

    if( Objects.isNotNull( m_profile ) )
    {
      final IProfileFeature source = profile.getSource();

      m_listener = new ValidationProfilListener( m_profile, getFile( source ), null, source.getId() );
      m_profile.addProfilListener( m_listener );
    }
  }

  private IFile getFile( final IProfileFeature source )
  {
    final GMLWorkspace workspace = source.getWorkspace();

    return ResourceUtilities.findFileFromURL( workspace.getContext() );
  }

}
