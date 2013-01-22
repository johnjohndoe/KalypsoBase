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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekNetworkD12;
import org.kalypso.model.wspm.core.profil.sobek.profiles.SobekNetworkD12Point;

/**
 * @author Gernot Belger
 */
public class SobekNetworkD12Parser
{
  private static final String TOKEN_DOMN = "DOMN"; //$NON-NLS-1$

  private static final String TOKEN_GFLS = "GFLS"; //$NON-NLS-1$

  private static final String TOKEN_PT12 = "PT12"; //$NON-NLS-1$

  private static final String TOKEN_LM12 = "LM12"; //$NON-NLS-1$

  private static final String TOKEN_LI12 = "LI12"; //$NON-NLS-1$

  private static final String ATTRIBUTE_ID = "id"; //$NON-NLS-1$

  private static final String ATTRIBUTE_NM = "nm"; //$NON-NLS-1$

  private static final String ATTRIBUTE_CI = "ci"; //$NON-NLS-1$

  private static final String ATTRIBUTE_LC = "lc"; //$NON-NLS-1$

  private static final String ATTRIBUTE_PX = "px"; //$NON-NLS-1$

  private static final String ATTRIBUTE_PY = "py"; //$NON-NLS-1$

  private static final String ATTRIBUTE_MC = "mc"; //$NON-NLS-1$

  private static final String ATTRIBUTE_MR = "mr"; //$NON-NLS-1$

  private final File m_networkD12File;

  private final SobekNetworkD12 m_network = new SobekNetworkD12();

  private final String m_networkSRS;

  public SobekNetworkD12Parser( final File networkD12File, final String networkSRS )
  {
    m_networkD12File = networkD12File;
    m_networkSRS = networkSRS;
  }

  public SobekNetworkD12 read( final IProgressMonitor monitor ) throws CoreException, IOException
  {
    final LineNumberReader reader = new LineNumberReader( new FileReader( m_networkD12File ) );

    // just skip first line
    new SobekLineParser( reader );

    final SobekLineParser line = new SobekLineParser( reader );
    line.expectToken( TOKEN_DOMN );

    while( reader.ready() )
      readParts( reader );

    ProgressUtilities.done( monitor );

    return m_network;
  }

  private void readParts( final LineNumberReader reader ) throws CoreException, IOException
  {
    final SobekLineParser startLine = new SobekLineParser( reader );
    final String token = startLine.nextTokenOrNull();
    if( token == null )
      return;
    if( TOKEN_DOMN.toLowerCase().equals( token ) )
      return;

    if( TOKEN_GFLS.equals( token ) )
    {
      SobekParsing.searchForEndToken( TOKEN_GFLS, startLine, reader );
      return;
    }

    if( TOKEN_PT12.equals( token ) )
    {
      readPT12( startLine );
      return;
    }

    if( TOKEN_LM12.equals( token ) )
    {
      SobekParsing.searchForEndToken( TOKEN_LM12, startLine, reader );
      return;
    }

    if( TOKEN_LI12.equals( token ) )
    {
      SobekParsing.searchForEndToken( TOKEN_LI12, startLine, reader );
      return;
    }

    throw startLine.throwError( "Unexpected token '%s'", token ); //$NON-NLS-1$
  }

  private void readPT12( final SobekLineParser line ) throws CoreException
  {
    // PT12 id '1' nm '1' ci '16' lc 107.586463742351 px 3396413.61741709 py 5701235.18117374 mc 112 mr 372 pt12

    final String id = line.nextStringToken( ATTRIBUTE_ID );
    final String name = line.nextStringToken( ATTRIBUTE_NM );
    final String carrierID = line.nextStringToken( ATTRIBUTE_CI );

    final BigDecimal lc = line.nextDecimalToken( ATTRIBUTE_LC );

    final BigDecimal px = line.nextDecimalToken( ATTRIBUTE_PX );
    final BigDecimal py = line.nextDecimalToken( ATTRIBUTE_PY );

    final int mc = line.nextIntToken( ATTRIBUTE_MC );
    final int mr = line.nextIntToken( ATTRIBUTE_MR );

    line.expectToken( TOKEN_PT12.toLowerCase() );

    m_network.add( new SobekNetworkD12Point( id, name, carrierID, lc, px, py, mc, mr, m_networkSRS ) );
  }
}