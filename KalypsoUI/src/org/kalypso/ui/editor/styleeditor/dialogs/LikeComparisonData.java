/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
/*
 * Created on 03.08.2004
 *  
 */
package org.kalypso.ui.editor.styleeditor.dialogs;

public class LikeComparisonData extends AbstractComparisonData
{
  private String literal = null;

  private char escapeChar = '\\';

  private char singleChar = '?';

  private char wildCard = '*';

  public String getLiteral()
  {
    return literal;
  }

  public void setLiteral( String m_literal )
  {
    this.literal = m_literal;
  }

  @Override
  public boolean verify()
  {
    if( literal != null && propertyName != null )
      return true;
    return false;
  }

  public char getEscapeChar()
  {
    return escapeChar;
  }

  public void setEscapeChar( char m_escapeChar )
  {
    this.escapeChar = m_escapeChar;
  }

  public char getSingleChar()
  {
    return singleChar;
  }

  public void setSingleChar( char m_singleChar )
  {
    this.singleChar = m_singleChar;
  }

  public char getWildCard()
  {
    return wildCard;
  }

  public void setWildCard( char m_wildCard )
  {
    this.wildCard = m_wildCard;
  }
}