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
package org.kalypso.model.wspm.core.profil.sobek.parser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.model.wspm.core.i18n.Messages;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekFrictionDat;

/**
 * @author Gernot Belger
 */
public class SobekFrictionDatParser
{
  private static final String TOKEN_GLFR = "GLFR"; //$NON-NLS-1$

  private static final String TOKEN_BDFR = "BDFR"; //$NON-NLS-1$

  private static final String TOKEN_STFR = "STFR"; //$NON-NLS-1$

  private static final String TOKEN_CRFR = "CRFR"; //$NON-NLS-1$

  private static final String TOKEN_D2FR = "D2FR"; //$NON-NLS-1$

  private final Collection<SobekFrictionDat> m_profiles = new ArrayList<>();

  private final File m_frictionDatFile;

  public SobekFrictionDatParser( final File frictionDatFile )
  {
    m_frictionDatFile = frictionDatFile;
  }

  public SobekFrictionDat[] read( final IProgressMonitor monitor ) throws CoreException, IOException
  {
    final LineNumberReader reader = new LineNumberReader( new FileReader( m_frictionDatFile ) );
    while( reader.ready() )
    {
      final SobekFrictionDat profile = readFriction( reader );
      if( profile != null )
        m_profiles.add( profile );
    }

    ProgressUtilities.done( monitor );

    return m_profiles.toArray( new SobekFrictionDat[m_profiles.size()] );
  }

  private SobekFrictionDat readFriction( final LineNumberReader reader ) throws IOException, CoreException
  {
    final SobekLineParser startLine = new SobekLineParser( reader );
    final String token = startLine.nextTokenOrNull();
    if( token == null )
      return null;

    if( TOKEN_GLFR.equals( token ) )
      return readGLFR( startLine, reader );
    if( TOKEN_BDFR.equals( token ) )
      return readBDFR( startLine, reader );
    if( TOKEN_STFR.equals( token ) )
      return readSTFR( startLine, reader );
    if( TOKEN_CRFR.equals( token ) )
      return readCRFR( startLine, reader );
    if( TOKEN_D2FR.equals( token ) )
      return readD2FR( startLine, reader );

    throw startLine.throwError( Messages.getString("SobekFrictionDatParser_0"), token ); //$NON-NLS-1$
  }

  private SobekFrictionDat readGLFR( final SobekLineParser startLine, final LineNumberReader reader ) throws IOException, CoreException
  {
    SobekParsing.searchForEndToken( TOKEN_GLFR, startLine, reader );
    return null;
  }

  private SobekFrictionDat readBDFR( final SobekLineParser startLine, final LineNumberReader reader ) throws IOException, CoreException
  {
    SobekParsing.searchForEndToken( TOKEN_BDFR, startLine, reader );
    return null;
  }

  private SobekFrictionDat readSTFR( final SobekLineParser startLine, final LineNumberReader reader ) throws IOException, CoreException
  {
    SobekParsing.searchForEndToken( TOKEN_STFR, startLine, reader );
    return null;
  }

  private SobekFrictionDat readD2FR( final SobekLineParser startLine, final LineNumberReader reader ) throws IOException, CoreException
  {
    SobekParsing.searchForEndToken( TOKEN_D2FR, startLine, reader );
    return null;
  }

  private SobekFrictionDat readCRFR( final SobekLineParser startLine, final LineNumberReader reader ) throws IOException, CoreException
  {
    return new SobekFrictionDatCRFRParser( startLine, reader ).read();
  }
}