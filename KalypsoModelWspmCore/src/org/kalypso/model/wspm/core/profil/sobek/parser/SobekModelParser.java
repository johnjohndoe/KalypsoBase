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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.model.wspm.core.i18n.Messages;
import org.kalypso.model.wspm.core.profil.sobek.ISobekConstants;
import org.kalypso.model.wspm.core.profil.sobek.SobekModel;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekFrictionDat;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekNetworkD12;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekNetworkD12Point;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekProfile;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekProfileDat;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekProfileDef;

/**
 * @author Gernot Belger
 */
public class SobekModelParser
{
  /* definition-id -> profile */
  private final Map<String, SobekProfile> m_profileIndex = new HashMap<>();

  private final File m_sobekProjectDir;

  private final String m_networkSRS;

  public SobekModelParser( final File sobekProjectDir, final String networkSRS )
  {
    m_sobekProjectDir = sobekProjectDir;
    m_networkSRS = networkSRS;
  }

  public SobekModel read( final IProgressMonitor monitor ) throws CoreException, IOException
  {
    monitor.beginTask( Messages.getString( "SobekModelParser_0" ), 100 ); //$NON-NLS-1$

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

  private SobekProfileDat[] readProfileDat( final IProgressMonitor monitor ) throws IOException, CoreException
  {
    final File profileDatFile = new File( m_sobekProjectDir, ISobekConstants.PROFILE_DAT );
    if( !profileDatFile.isFile() )
      return new SobekProfileDat[0];

    final SobekProfileDatParser parser = new SobekProfileDatParser( profileDatFile );
    return parser.read( monitor );
  }

  private SobekFrictionDat[] readFrictionDat( final IProgressMonitor monitor ) throws CoreException, IOException
  {
    final File frictionDatFile = new File( m_sobekProjectDir, ISobekConstants.FRICTION_DAT );
    if( !frictionDatFile.isFile() )
      return new SobekFrictionDat[0];

    final SobekFrictionDatParser parser = new SobekFrictionDatParser( frictionDatFile );
    return parser.read( monitor );
  }

  private SobekNetworkD12 readNetworkD12( final IProgressMonitor monitor ) throws CoreException, IOException
  {
    final File networkD12File = new File( m_sobekProjectDir, ISobekConstants.NETWORK_D12 );
    if( !networkD12File.isFile() )
      return null;

    final SobekNetworkD12Parser parser = new SobekNetworkD12Parser( networkD12File, m_networkSRS );
    return parser.read( monitor );
  }

  private SobekModel createModel( final SobekProfileDef[] profileDefs, final SobekProfileDat[] profileDats, final SobekFrictionDat[] frictionDats, final SobekNetworkD12 network )
  {
    for( final SobekProfileDef profileDef : profileDefs )
    {
      final String defId = profileDef.getId();
      final SobekProfile profile = createOrGetProfile( defId );
      final SobekProfile profileWithDef = profile.setDefinition( profileDef );
      m_profileIndex.put( defId, profileWithDef );
    }

    for( final SobekProfileDat profileDat : profileDats )
    {
      final String defId = profileDat.getDi();
      final SobekProfile profile = createOrGetProfile( defId );
      final SobekProfile profileWithDat = profile.setData( profileDat );
      m_profileIndex.put( defId, profileWithDat );
    }

    for( final SobekFrictionDat frictionDat : frictionDats )
    {
      final String defId = frictionDat.getCsID();
      final SobekProfile profile = createOrGetProfile( defId );
      final SobekProfile profileWithDat = profile.setFriction( frictionDat );
      m_profileIndex.put( defId, profileWithDat );
    }

    final SobekNetworkD12Point[] points = network.getPoints();
    for( final SobekNetworkD12Point point : points )
    {
      final String defId = point.getID();
      final SobekProfile profile = createOrGetProfile( defId );
      final SobekProfile profileWithDat = profile.setNetworkPoint( point );
      m_profileIndex.put( defId, profileWithDat );
    }

    /* Create the SOBEK model */
    final SobekModel sobekModel = new SobekModel();

    final Collection<SobekProfile> profiles = m_profileIndex.values();
    for( final SobekProfile profile : profiles )
      sobekModel.addProfile( profile );

    return sobekModel;
  }

  private SobekProfile createOrGetProfile( final String definitionId )
  {
    if( !m_profileIndex.containsKey( definitionId ) )
      m_profileIndex.put( definitionId, new SobekProfile( null, null, null, null ) );

    return m_profileIndex.get( definitionId );
  }
}