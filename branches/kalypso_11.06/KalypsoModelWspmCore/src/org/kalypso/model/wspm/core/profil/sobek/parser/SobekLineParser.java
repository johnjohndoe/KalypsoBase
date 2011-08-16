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

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.text.StrMatcher;
import org.apache.commons.lang.text.StrTokenizer;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.java.lang.NumberUtils;

/**
 * @author Gernot Belger
 */
public class SobekLineParser
{
  private final StrTokenizer m_tokenizer;

  private final int m_lineNumber;

  public SobekLineParser( final LineNumberReader reader ) throws IOException
  {
    final String line = reader.readLine();
    m_tokenizer = new StrTokenizer( line, StrMatcher.spaceMatcher(), StrMatcher.singleQuoteMatcher() );
    m_lineNumber = reader.getLineNumber();
  }

  private String nextOrNull( )
  {
    if( m_tokenizer.hasNext() )
      return m_tokenizer.nextToken();
    return null;
  }

  public CoreException throwError( final String format, final Object... args )
  {
    return SobekParsing.throwError( format( format, args ) );
  }

  private String format( final String format, final Object... args )
  {
    final String formatString = "Line %d: " + format;
    final Object[] argsAndLine = ArrayUtils.addAll( new Object[] { m_lineNumber }, args );
    return String.format( formatString, m_lineNumber, argsAndLine );
  }

  public void expectToken( final String expectedToken ) throws CoreException
  {
    final String actualToken = nextOrNull();
    if( !expectedToken.equals( actualToken ) )
      throw throwError( "expected token '%s'", expectedToken );
  }

  public void expectAttribute( final String expectedAttribute ) throws CoreException
  {
    final String actualAttribute = nextOrNull();
    if( !expectedAttribute.equals( actualAttribute ) )
      throw throwError( "expected parameter '%s'", expectedAttribute );
  }

  public String nextStringToken( final String expectedAttribute ) throws CoreException
  {
    expectAttribute( expectedAttribute );

    return expectStringValue( expectedAttribute );
  }

  public int nextIntToken( final String attributeName ) throws CoreException
  {
    expectAttribute( attributeName );

    return expectIntValue( attributeName );
  }

  private String expectValue( final String attributeName ) throws CoreException
  {
    final String value = nextOrNull();
    if( value == null )
      throw throwError( "Missing value for parameter '%s'", attributeName );

    return value;
  }

  private String expectStringValue( final String attributeName ) throws CoreException
  {
    return expectValue( attributeName );
  }

  private int expectIntValue( final String attributeName ) throws CoreException
  {
    final String value = expectValue( attributeName );
    final Integer integer = NumberUtils.parseQuietInteger( value );
    if( integer == null )
      throw throwError( "Value for parameter '%s' must be an integer." );

    return integer;
  }

}