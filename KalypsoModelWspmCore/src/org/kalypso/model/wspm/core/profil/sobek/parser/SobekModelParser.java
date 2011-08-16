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
package org.kalypso.model.wspm.core.profil.sobek.parser;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.model.wspm.core.profil.sobek.ISobekConstants;
import org.kalypso.model.wspm.core.profil.sobek.SobekModel;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekFrictionDat;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekNetworkD12;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekProfileDat;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekProfileDef;

/**
 * @author Gernot Belger
 */
public class SobekModelParser
{
  private final File m_sobekProjectDir;

  public SobekModelParser( final File sobekProjectDir )
  {
    m_sobekProjectDir = sobekProjectDir;
  }

  public SobekModel read( final IProgressMonitor monitor ) throws CoreException, IOException
  {
    monitor.beginTask( "Reading SOBEK project", 100 );

    final SobekProfileDef[] profileDefs = readProfileDef( new SubProgressMonitor( monitor, 25 ) );
    final SobekProfileDat[] profileDats = readProfileDat( new SubProgressMonitor( monitor, 25 ) );
    final SobekFrictionDat[] frictionDats = readFrictionDat( new SubProgressMonitor( monitor, 25 ) );
    final SobekNetworkD12 network = readNetworkD12( new SubProgressMonitor( monitor, 25 ) );

    final SobekModel sobekModel = createModel( profileDefs, profileDats, frictionDats, network );

    ProgressUtilities.done( monitor );

    return sobekModel;
  }

  private SobekProfileDef[] readProfileDef( final IProgressMonitor monitor ) throws IOException, CoreException
  {
    final File profileDefFile = new File( m_sobekProjectDir, ISobekConstants.PROFILE_DEF );
    if( !profileDefFile.isFile() )
      return new SobekProfileDef[0];

    final SobekProfileDefParser parser = new SobekProfileDefParser( profileDefFile );
    return parser.read( monitor );
  }

  private SobekProfileDat[] readProfileDat( final IProgressMonitor monitor )
  {
    final File profileDatFile = new File( m_sobekProjectDir, ISobekConstants.PROFILE_DAT );
    if( !profileDatFile.isFile() )
      return new SobekProfileDat[0];

    final SobekProfileDatParser parser = new SobekProfileDatParser( profileDatFile );
    return parser.read( monitor );
  }

  private SobekFrictionDat[] readFrictionDat( final IProgressMonitor monitor )
  {
    final File frictionDatFile = new File( m_sobekProjectDir, ISobekConstants.FRICTION_DAT );
    if( !frictionDatFile.isFile() )
      return new SobekFrictionDat[0];

    final SobekFrictionDatParser parser = new SobekFrictionDatParser( frictionDatFile );
    return parser.read( monitor );
  }

  private SobekNetworkD12 readNetworkD12( final IProgressMonitor monitor )
  {
    final File networkD12File = new File( m_sobekProjectDir, ISobekConstants.NETWORK_D12 );
    if( !networkD12File.isFile() )
      return null;

    final SobekNetworkD12Parser parser = new SobekNetworkD12Parser( networkD12File );
    return parser.read( monitor );
  }

  private SobekModel createModel( final SobekProfileDef[] profileDefs, final SobekProfileDat[] profileDats, final SobekFrictionDat[] frictionDats, final SobekNetworkD12 network )
  {
    // TODO Auto-generated method stub
    return new SobekModel();
  }
}