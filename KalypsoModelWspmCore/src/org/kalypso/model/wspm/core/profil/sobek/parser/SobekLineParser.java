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

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.text.StrMatcher;
import org.apache.commons.lang3.text.StrTokenizer;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.model.wspm.core.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class SobekLineParser
{
  private final StrTokenizer m_tokenizer;

  private final int m_lineNumber;

  public SobekLineParser( final LineNumberReader reader ) throws IOException, CoreException
  {
    final String line = reader.readLine();
    if( line == null )
      throw SobekParsing.throwError( format( Messages.getString("SobekLineParser_0") ) ); //$NON-NLS-1$

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
    final String formatString = Messages.getString("SobekLineParser_1") + format; //$NON-NLS-1$
    final Object[] argsAndLine = ArrayUtils.addAll( new Object[] { m_lineNumber }, args );
    return String.format( formatString, argsAndLine );
  }

  public void expectToken( final String expectedToken ) throws CoreException
  {
    final String actualToken = nextOrNull();
    if( !expectedToken.equals( actualToken ) )
      throw throwError( Messages.getString("SobekLineParser_2"), expectedToken, actualToken ); //$NON-NLS-1$
  }

  public void expectAttribute( final String expectedAttribute ) throws CoreException
  {
    final String actualAttribute = nextOrNull();
    if( !expectedAttribute.equals( actualAttribute ) )
      throw throwError( Messages.getString("SobekLineParser_3"), expectedAttribute, actualAttribute ); //$NON-NLS-1$
  }

  public String nextStringToken( final String expectedAttribute ) throws CoreException
  {
    expectAttribute( expectedAttribute );

    return expectStringValue( expectedAttribute );
  }

  public String nextOptionalStringToken( final String attributeName, final String defaultValue ) throws CoreException
  {
    if( !isToken( attributeName ) )
      return defaultValue;

    return expectStringValue( attributeName );
  }

  public int nextIntToken( final String attributeName ) throws CoreException
  {
    expectAttribute( attributeName );

    return expectIntValue( attributeName );
  }

  public void nextIntToken( final String attributeName, final int expectedValue ) throws CoreException
  {
    final int value = nextIntToken( attributeName );
    if( value != expectedValue )
      throw throwError( Messages.getString("SobekLineParser_4"), attributeName ); //$NON-NLS-1$
  }

  public BigDecimal nextDecimalToken( final String attributeName ) throws CoreException
  {
    expectAttribute( attributeName );

    return expectDecimalValue( attributeName );
  }

  public BigDecimal nextOptionalDecimalToken( final String attributeName, final BigDecimal defaultValue ) throws CoreException
  {
    if( !isToken( attributeName ) )
      return defaultValue;

    return expectDecimalValue( attributeName );
  }

  public String expectValue( final String attributeName ) throws CoreException
  {
    final String value = nextOrNull();
    if( value == null )
      throw throwError( Messages.getString("SobekLineParser_5"), attributeName ); //$NON-NLS-1$

    return value;
  }

  public String expectStringValue( final String attributeName ) throws CoreException
  {
    return expectValue( attributeName );
  }

  public int expectIntValue( final String attributeName ) throws CoreException
  {
    final String value = expectValue( attributeName );
    final Integer integer = NumberUtils.parseQuietInteger( value );
    if( integer == null )
      throw throwError( Messages.getString("SobekLineParser_6") ); //$NON-NLS-1$

    return integer;
  }

  public BigDecimal expectDecimalValue( final String attributeName ) throws CoreException
  {
    final String token = expectValue( attributeName );
    return parseDecimal( token );
  }

  /**
   * Checks if the next token has a certain value. If it is so, read the token and return <code>true</code>. Else,
   * return <code>false</code>, but the tokenizer is still at the old position.
   */
  public boolean isToken( final String expectedToken )
  {
    final String actualToken = m_tokenizer.nextToken();
    if( expectedToken.equals( actualToken ) )
      return true;

    m_tokenizer.previous();
    return false;
  }

  public BigDecimal[] readDecimalsUntilComment( final int count ) throws CoreException
  {
    final BigDecimal[] decimals = readDecimalsUntilComment();
    if( decimals.length < count )
      throw throwError( format( Messages.getString("SobekLineParser_7") ) ); //$NON-NLS-1$
    if( decimals.length > count )
      throw throwError( format( Messages.getString("SobekLineParser_8") ) ); //$NON-NLS-1$

    return decimals;
  }

  private BigDecimal[] readDecimalsUntilComment( ) throws CoreException
  {
    final Collection<BigDecimal> decimals = new ArrayList<>();
    while( m_tokenizer.hasNext() )
    {
      final String next = m_tokenizer.nextToken();
      if( "<".equals( next ) ) //$NON-NLS-1$
        break;

      final BigDecimal value = NumberUtils.parseQuietDecimal( next );
      if( value == null )
        throw throwError( format( Messages.getString("SobekLineParser_10"), next ) ); //$NON-NLS-1$
      decimals.add( value );
    }

    return decimals.toArray( new BigDecimal[decimals.size()] );
  }

  private BigDecimal parseDecimal( final String token ) throws CoreException
  {
    final BigDecimal decimal = NumberUtils.parseQuietDecimal( token );
    if( decimal == null )
      throw throwError( Messages.getString("SobekLineParser_11") ); //$NON-NLS-1$

    return decimal;
  }

  public String nextTokenOrNull( )
  {
    if( m_tokenizer.hasNext() )
      return m_tokenizer.nextToken();

    return null;
  }
}