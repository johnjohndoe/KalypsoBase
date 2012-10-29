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
import org.kalypso.model.wspm.core.profil.sobek.profiles.ISobekProfileDefData;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekProfileDef;

/**
 * @author Gernot Belger
 */
public class SobekProfileDefParser
{
  private static final String TOKEN_CRDS = "CRDS"; //$NON-NLS-1$

  private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

  private static final String ATTRIBUTE_NAME = "nm"; //$NON-NLS-1$

  private static final String ATTRIBUTE_TYPE = "ty"; //$NON-NLS-1$

  private final Collection<SobekProfileDef> m_profiles = new ArrayList<>();

  private final File m_profileDefFile;

  public SobekProfileDefParser( final File profileDefFile )
  {
    m_profileDefFile = profileDefFile;
  }

  public SobekProfileDef[] read( final IProgressMonitor monitor ) throws IOException, CoreException
  {
    final LineNumberReader reader = new LineNumberReader( new FileReader( m_profileDefFile ) );
    while( reader.ready() )
    {
      final SobekProfileDef profile = readCRDS( reader );
      if( profile != null )
        m_profiles.add( profile );
    }

    monitor.done();

    return m_profiles.toArray( new SobekProfileDef[m_profiles.size()] );
  }

  private SobekProfileDef readCRDS( final LineNumberReader reader ) throws CoreException, IOException
  {
    final SobekLineParser lineParser = new SobekLineParser( reader );
    lineParser.expectToken( TOKEN_CRDS );

    final String id = lineParser.nextStringToken( ATTRIBUTE_ID );
    final String name = lineParser.nextStringToken( ATTRIBUTE_NAME );
    final int type = lineParser.nextIntToken( ATTRIBUTE_TYPE );

    final ISobekProfileDefData data = parseData( lineParser, reader, type );
    return new SobekProfileDef( id, name, data );
  }

  private ISobekProfileDefData parseData( final SobekLineParser lineParser, final LineNumberReader reader, final int type ) throws CoreException, IOException
  {
    switch( type )
    {
      case 0: // tabulated
        return new SobekProfileDefTabulatedCrossSectionParser( lineParser, reader ).read();

      case 10: // yz table
        return new SobekProfileDefYZTableParser( lineParser, reader ).read();

      case 1: // trapezoidal
      case 2: // open circle
      case 3: // sedredge (2D morfology)
      case 4: // closed circle
      case 6: // egg shaped (width)
      case 11: // asymmetrical trapeziodal
        throw lineParser.throwError( "Sorry, parsing type '%d' is not yet supported.", type ); //$NON-NLS-1$

      case 5: //
      case 9: //
      case 7: // egg shaped 2 (radius) not implemented
      case 8: // closed rectangular not implemented
        throw lineParser.throwError( "Type '%d' not implemented by SOBEK, parsing not possible.", type ); //$NON-NLS-1$

      default:
        throw lineParser.throwError( "Unknown type '%d'", type ); //$NON-NLS-1$
    }
  }
}