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
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekProfileDat;

/**
 * @author Gernot Belger
 */
public class SobekProfileDatParser
{
  private static final String TOKEN_CRSN = "CRSN"; //$NON-NLS-1$

  private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

  private static final String ATTRIBUTE_DI = "di"; //$NON-NLS-1$

  private static final String ATTRIBUTE_RL = "rl"; //$NON-NLS-1$

  private static final String ATTRIBUTE_LL = "ll"; //$NON-NLS-1$

  private static final String ATTRIBUTE_RS = "rs"; //$NON-NLS-1$

  private static final String ATTRIBUTE_LS = "ls"; //$NON-NLS-1$

  private final Collection<SobekProfileDat> m_profiles = new ArrayList<>();

  private final File m_profileDatFile;

  public SobekProfileDatParser( final File profileDatFile )
  {
    m_profileDatFile = profileDatFile;
  }

  public SobekProfileDat[] read( final IProgressMonitor monitor ) throws IOException, CoreException
  {
    final LineNumberReader reader = new LineNumberReader( new FileReader( m_profileDatFile ) );
    while( reader.ready() )
    {
      final SobekProfileDat profile = readCRSN( reader );
      if( profile != null )
        m_profiles.add( profile );
    }

    ProgressUtilities.done( monitor );

    return m_profiles.toArray( new SobekProfileDat[m_profiles.size()] );
  }

  // CRSN id '115.4835_SE' di '115.4835_SE' rl 0 rs 107.19 ls 108.00 crsn
  private SobekProfileDat readCRSN( final LineNumberReader reader ) throws CoreException, IOException
  {
    final SobekLineParser lineParser = new SobekLineParser( reader );
    lineParser.expectToken( TOKEN_CRSN );

    final String id = lineParser.nextStringToken( ATTRIBUTE_ID );
    final String di = lineParser.nextStringToken( ATTRIBUTE_DI );
    final BigDecimal rl = lineParser.nextDecimalToken( ATTRIBUTE_RL );
    final BigDecimal ll = lineParser.nextOptionalDecimalToken( ATTRIBUTE_LL, null );
    final BigDecimal rs = lineParser.nextOptionalDecimalToken( ATTRIBUTE_RS, null );
    final BigDecimal ls = lineParser.nextOptionalDecimalToken( ATTRIBUTE_LS, null );

    lineParser.expectToken( TOKEN_CRSN.toLowerCase() );

    return new SobekProfileDat( id, di, rl, ll, rs, ls );
  }
}
