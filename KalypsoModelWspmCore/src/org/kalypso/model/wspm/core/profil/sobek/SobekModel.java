/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.model.wspm.core.profil.sobek;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekProfile;
import org.kalypso.model.wspm.core.profil.sobek.struct.SobekStruct;

/**
 * This class contains a collection of sobek profiles. It can also provides functionality for several tasks, as writing
 * to a file and so on.
 * 
 * @author Holger Albert
 */
public class SobekModel implements ISobekConstants
{
  private final List<SobekProfile> m_profiles = new ArrayList<>();

  private final List<SobekStruct> m_structs = new ArrayList<>();

  public void addProfile( final SobekProfile profile )
  {
    m_profiles.add( profile );
  }

  public void addStruct( final SobekStruct struct )
  {
    m_structs.add( struct );
  }

  /**
   * This function initializes this model with help of the given provider.
   * 
   * @param provider
   *          This provider helps, creating the profiles.
   * @param monitor
   *          A progress monitor.
   */
  public void initFrom( final AbstractSobekProvider provider, final IProgressMonitor monitor ) throws Exception
  {
    if( provider == null )
      throw new IllegalArgumentException( "Argument 'provider' is missing..." ); //$NON-NLS-1$

    /* Clear all old profiles. */
    /* If an error occurs later on, this model will be definitly empty. */
    m_profiles.clear();

    /* Add all profiles. */
    final SobekProfile[] profiles = provider.getSobekProfiles( monitor );
    m_profiles.addAll( Arrays.asList( profiles ) );
  }

  /**
   * This function writes the profiles to the files 'profile.dat' and 'profile.def' into the given folder.
   * 
   * @param destinationFolder
   *          The destination folder.
   */
  public void writeTo( final File destinationFolder ) throws IOException, CoreException
  {
    writeProfiles( destinationFolder );
    writeStructs( destinationFolder );
  }

  private void writeProfiles( final File destinationFolder ) throws IOException, CoreException
  {
    /* Create the file handles */
    final File profileDatFile = new File( destinationFolder, ISobekConstants.PROFILE_DAT );
    final File profileDefFile = new File( destinationFolder, ISobekConstants.PROFILE_DEF );
    final File frictionDatFile = new File( destinationFolder, ISobekConstants.FRICTION_DAT );

    try( PrintWriter datWriter = new PrintWriter( profileDatFile ); PrintWriter defWriter = new PrintWriter( profileDefFile ); PrintWriter frictionWriter = new PrintWriter( frictionDatFile ); )
    {
      for( final SobekProfile profile : m_profiles )
      {
        /* Validate the profile. */
        final IStatus status = profile.validate();
        if( status.getSeverity() > IStatus.WARNING )
          throw new CoreException( status );

        /* Serialize the data of the profile. */
        profile.serializeProfileDat( datWriter );
        profile.serializeProfileDef( defWriter );
        profile.serializeFrictionDat( frictionWriter );
      }
    }
  }

  private void writeStructs( final File destinationFolder ) throws IOException
  {
    /* Create the file handles */
    final File structDatFile = new File( destinationFolder, ISobekConstants.STRUCT_DAT );
    final File structDefFile = new File( destinationFolder, ISobekConstants.STRUCT_DEF );
    final File profileDefFile = new File( destinationFolder, ISobekConstants.PROFILE_DEF );

    try( PrintWriter datWriter = new PrintWriter( structDatFile );
        PrintWriter defWriter = new PrintWriter( structDefFile );
        PrintWriter profileDefWriter = new PrintWriter( new FileWriter( profileDefFile, true ) ) )
    {
      for( final SobekStruct struct : m_structs )
      {
        /* Validate the profile. */

        // FIXME

//        final IStatus status = profile.validate();
//        if( status.getSeverity() > IStatus.WARNING )
//          throw new CoreException( status );

        struct.serializeStructDat( datWriter );
        struct.serializeStructDef( defWriter, profileDefWriter );
      }
    }
  }

  public SobekProfile[] getProfiles( )
  {
    return m_profiles.toArray( new SobekProfile[m_profiles.size()] );
  }

  public SobekStruct[] getStructs( )
  {
    return m_structs.toArray( new SobekStruct[m_structs.size()] );
  }
}