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

import org.eclipse.core.runtime.CoreException;
import org.kalypso.model.wspm.core.profil.sobek.profiles.ISobekProfileDefData;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekProfileDefYZTable;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekYZPoint;

/**
 * @author Gernot Belger
 */
public class SobekProfileDefYZTableParser
{
  private static final String ATTRIBUTE_ST = "st"; //$NON-NLS-1$

  private static final String ATTRIBUTE_LT = "lt"; //$NON-NLS-1$

  private static final String ATTRIBUTE_SW = "sw"; //$NON-NLS-1$

  private static final String ATTRIBUTE_GL = "gl"; //$NON-NLS-1$

  private static final String ATTRIBUTE_GU = "gu"; //$NON-NLS-1$

  private static final String ATTRIBUTE_YZ = "yz"; //$NON-NLS-1$

  private static final String TOKEN_TBLE = "TBLE"; //$NON-NLS-1$

  private static final String TOKEN_CRDS = "CRDS"; //$NON-NLS-1$

  private final SobekLineParser m_lineParser;

  private final LineNumberReader m_reader;

  public SobekProfileDefYZTableParser( final SobekLineParser restOfHeader, final LineNumberReader reader )
  {
    m_lineParser = restOfHeader;
    m_reader = reader;
  }

  public ISobekProfileDefData read( ) throws CoreException, IOException
  {
    final int st = m_lineParser.nextIntToken( ATTRIBUTE_ST );
    if( st != 0 && st != 1 )
      throw m_lineParser.throwError( "Invalid argument for st: %d", st ); //$NON-NLS-1$

    m_lineParser.expectAttribute( ATTRIBUTE_LT );

    // REMEARK: sobek document considers a variant with nothing behind the 'SW' but i have nevber seens this.
    m_lineParser.nextIntToken( ATTRIBUTE_SW, 0 );

    final BigDecimal swSecond = m_lineParser.expectDecimalValue( ATTRIBUTE_SW );

    // REMARK: sobek documentation sais, gl and gu go behind the table, but this is not true in our examples
    m_lineParser.nextIntToken( ATTRIBUTE_GL, 0 );
    m_lineParser.nextIntToken( ATTRIBUTE_GU, 0 );
    m_lineParser.expectAttribute( ATTRIBUTE_LT );
    m_lineParser.expectAttribute( ATTRIBUTE_YZ );

    final SobekProfileDefYZTable yzTable = new SobekProfileDefYZTable( st, swSecond );
    readTable( yzTable );

    final SobekLineParser crdsLine = new SobekLineParser( m_reader );
    crdsLine.expectToken( TOKEN_CRDS.toLowerCase() );

    return yzTable;
  }

  private void readTable( final SobekProfileDefYZTable yzTable ) throws CoreException, IOException
  {
    final SobekLineParser lineParser = new SobekLineParser( m_reader );
    lineParser.expectToken( TOKEN_TBLE );

    readTableContent( yzTable );
  }

  private void readTableContent( final SobekProfileDefYZTable yzTable ) throws IOException, CoreException
  {
    while( m_reader.ready() )
    {
      final SobekLineParser line = new SobekLineParser( m_reader );
      if( line.isToken( TOKEN_TBLE.toLowerCase() ) )
        return;

      final BigDecimal[] values = line.readDecimalsUntilComment( 2 );
      final BigDecimal y = values[0];
      final BigDecimal z = values[1];
      yzTable.addPoint( new SobekYZPoint( y, z ) );
    }
  }
}
