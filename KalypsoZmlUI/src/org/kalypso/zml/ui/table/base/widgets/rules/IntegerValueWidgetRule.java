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
package org.kalypso.zml.ui.table.base.widgets.rules;

import org.kalypso.contribs.java.lang.NumberUtils;

/**
 * @author Dirk Kuch
 */
public class IntegerValueWidgetRule implements ITextWidgetRule<Integer>
{
  private String m_lastValidationMsg;

  private final String m_format;

  public IntegerValueWidgetRule( )
  {
    this( "%d" ); // $NON-NLS-1$
  }

  public IntegerValueWidgetRule( final String format )
  {
    m_format = format;
  }

  /**
   * @see org.kalypso.zml.ui.table.base.widgets.rules.IWidgetRule#getLastValidationMessage()
   */
  @Override
  public String getLastValidationMessage( )
  {
    return m_lastValidationMsg;
  }

  /**
   * @see org.kalypso.zml.ui.table.base.widgets.rules.IWidgetRule#getFormatedString(java.lang.Object)
   */
  @Override
  public String getFormatedString( final Integer value )
  {
    return String.format( m_format, value );
  }

  /**
   * @see org.kalypso.zml.ui.table.base.widgets.rules.ITextWidgetRule#parseValue(java.lang.String)
   */
  @Override
  public Integer parseValue( final String text )
  {
    return NumberUtils.parseQuietInteger( text );
  }

  /**
   * @see org.kalypso.zml.ui.table.base.widgets.rules.ITextWidgetRule#isValid(java.lang.String)
   */
  @Override
  public boolean isValid( final String text )
  {
    final boolean isInteger = NumberUtils.isInteger( text );
    if( !isInteger )
      m_lastValidationMsg = "Ung�ltiger Zahlenwert";
    else
      m_lastValidationMsg = null;

    return isInteger;
  }

}
