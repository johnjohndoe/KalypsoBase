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

import java.io.IOException;
import java.io.LineNumberReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.model.wspm.core.profil.sobek.profiles.ISobekProfileDefData;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekProfileDefTabulatedCrossSection;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekProfileHeight;

/**
 * @author Gernot Belger
 */
public class SobekProfileDefTabulatedCrossSectionParser
{
  private static final String ATTRIBUTE_WM = "wm"; //$NON-NLS-1$

  private static final String ATTRIBUTE_W1 = "w1"; //$NON-NLS-1$

  private static final String ATTRIBUTE_W2 = "w2"; //$NON-NLS-1$

  private static final String ATTRIBUTE_SW = "sw"; //$NON-NLS-1$

  private static final String ATTRIBUTE_GL = "gl"; //$NON-NLS-1$

  private static final String ATTRIBUTE_GU = "gu"; //$NON-NLS-1$

  private static final String ATTRIBUTE_DK = "dk"; //$NON-NLS-1$

  private static final String ATTRIBUTE_DC = "dc"; //$NON-NLS-1$

  private static final String ATTRIBUTE_DB = "db"; //$NON-NLS-1$

  private static final String ATTRIBUTE_DF = "df"; //$NON-NLS-1$

  private static final String ATTRIBUTE_DT = "dt"; //$NON-NLS-1$

  private static final String TOKEN_TBLE = "TBLE"; //$NON-NLS-1$

  private static final String TOKEN_CRDS = "CRDS"; //$NON-NLS-1$

  private final SobekLineParser m_restOfHeader;

  private final LineNumberReader m_reader;

  public SobekProfileDefTabulatedCrossSectionParser( final SobekLineParser restOfHeader, final LineNumberReader reader )
  {
    m_restOfHeader = restOfHeader;
    m_reader = reader;
  }

  public ISobekProfileDefData read( ) throws CoreException, IOException
  {
    final BigDecimal wm = m_restOfHeader.nextDecimalToken( ATTRIBUTE_WM );
    final BigDecimal w1 = m_restOfHeader.nextDecimalToken( ATTRIBUTE_W1 );
    final BigDecimal w2 = m_restOfHeader.nextDecimalToken( ATTRIBUTE_W2 );
    final BigDecimal sw = m_restOfHeader.nextDecimalToken( ATTRIBUTE_SW );

    final BigDecimal gl = m_restOfHeader.nextDecimalToken( ATTRIBUTE_GL );
    m_restOfHeader.nextIntToken( ATTRIBUTE_GU, 0 );

    final SobekProfileHeight[] profileHeights = readTable();

    final SobekLineParser dikeLine = new SobekLineParser( m_reader );
    if( dikeLine.isToken( TOKEN_CRDS.toLowerCase() ) )
      return new SobekProfileDefTabulatedCrossSection( wm, w1, w2, sw, profileHeights, gl, 0 );

    final int dk = dikeLine.nextIntToken( ATTRIBUTE_DK );
    final BigDecimal dc = dikeLine.nextDecimalToken( ATTRIBUTE_DC );
    final BigDecimal db = dikeLine.nextDecimalToken( ATTRIBUTE_DB );
    final BigDecimal df = dikeLine.nextDecimalToken( ATTRIBUTE_DF );
    final BigDecimal dt = dikeLine.nextDecimalToken( ATTRIBUTE_DT );

    final SobekProfileDefTabulatedCrossSection section = new SobekProfileDefTabulatedCrossSection( wm, w1, w2, sw, profileHeights, dk, dc, db, df, dt, gl, 0 );

    final SobekLineParser crdsLine = new SobekLineParser( m_reader );
    crdsLine.expectToken( TOKEN_CRDS.toLowerCase() );

    return section;
  }

  private SobekProfileHeight[] readTable( ) throws IOException, CoreException
  {
    final SobekLineParser startLine = new SobekLineParser( m_reader );
    startLine.expectToken( TOKEN_TBLE );

    final Collection<SobekProfileHeight> heights = new ArrayList<>();
    while( m_reader.ready() )
    {
      final SobekLineParser line = new SobekLineParser( m_reader );
      if( line.isToken( TOKEN_TBLE.toLowerCase() ) )
        break;

      final BigDecimal[] decimals = line.readDecimalsUntilComment( 3 );
      heights.add( new SobekProfileHeight( decimals[0], decimals[1], decimals[2] ) );
    }

    return heights.toArray( new SobekProfileHeight[heights.size()] );
  }
}