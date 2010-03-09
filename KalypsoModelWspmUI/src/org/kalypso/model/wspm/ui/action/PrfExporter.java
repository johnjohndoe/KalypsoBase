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
package org.kalypso.model.wspm.ui.action;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.serializer.IProfilSink;
import org.kalypso.model.wspm.core.profil.serializer.ProfilSerializerUtilitites;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;

/**
 * @author Gernot Belger
 */
public class PrfExporter
{
  private final File m_exportDirectory;

  // FIXME: where to get this flag from...?
  private final boolean wspwinFileNames = true;

  public PrfExporter( final File exportDirectory )
  {
    m_exportDirectory = exportDirectory;
  }

  public IStatus export( final IProfil[] profiles, final IProgressMonitor monitor ) throws CoreException
  {
    final String id = PluginUtilities.id( KalypsoModelWspmUIPlugin.getDefault() );
    final MultiStatus resultStatus = new MultiStatus( id, 1, org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.action.ExportProfilePrfAction.0" ), null ); //$NON-NLS-1$

    monitor.beginTask( org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.action.ExportProfilePrfAction.1" ), profiles.length ); //$NON-NLS-1$

    final IProfilSink sink = KalypsoModelWspmCoreExtensions.createProfilSink( "prf" ); //$NON-NLS-1$

    for( final IProfil profile : profiles )
    {
      final String profileName = profile.getName();

      monitor.subTask( profileName );

      final String fileName = createWspWinFileName( profile ) + ".prf";
      final String cleanFileName = cleanupFilename( fileName );
      final File file = new File( m_exportDirectory, cleanFileName );

      try
      {
        ProfilSerializerUtilitites.writeProfile( sink, profile, file );
      }
      catch( final IOException e )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( e );
        resultStatus.add( status );
      }

      monitor.worked( 1 );
      if( monitor.isCanceled() )
        return new Status( IStatus.CANCEL, id, 1, org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.action.ExportProfilePrfAction.2" ), null ); //$NON-NLS-1$
    }
    return resultStatus;
  }

  private String createWspWinFileName( final IProfil profile )
  {
    if( wspwinFileNames )
    {
      final double station = profile.getStation();
      final String stationString = String.format( "%.4f", station ).replace( '.', '+' ).replace( ' ', '0' );

      return String.format( "%s.prf", stationString );
    }
    else
      return profile.getName();
  }

  private String cleanupFilename( final String fileName )
  {
    String result = fileName;
    result = fileName.replace( '#', '_' );
    result = fileName.replace( ':', '_' );
    result = fileName.replace( ' ', '_' );
    result = fileName.replace( ' ', '_' );
    return result;
  }

}
