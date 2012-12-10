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
package org.kalypso.shape.dbf;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Supported data types of the dbase file.
 * 
 * @author Gernot Belger
 */
public enum FieldType
{
  // See: http://www.dbase.com/knowledgebase/int/db7_file_fmt.htm
// B('B', "Binary", 10, false),
  C('C', "Character", -1, false),
  D('D', "Date", 8, false),
  N('N', "Number", -1, true),
  L('L', "Logical", 1, false),
  M('M', "Memo", 10, false),
// @('@', "Timestamp", 8, false),
// !('!', "Long", 4, false),
// +('+', "Autoincrement", 4, false),
  F('F', "Float", -1, true);
// O('O', "Double", 8, false),

  private final char m_name;

  private final String m_description;

  private final int m_fixedLength;

  private final boolean m_supportDecimal;

  private FieldType( final char name, final String description, final int fixedLength, final boolean supportDecimal )
  {
    m_name = name;
    m_description = description;
    m_fixedLength = fixedLength;
    m_supportDecimal = supportDecimal;
  }

  public String getDescription( )
  {
    return m_description;
  }

  public int getFixedLength( )
  {
    return m_fixedLength;
  }

  public char getName( )
  {
    return m_name;
  }

  /**
   * @see java.lang.Enum#toString()
   */
  @Override
  public String toString( )
  {
    return ToStringBuilder.reflectionToString( this );
  }

  public boolean isSupportDecimal( )
  {
    return m_supportDecimal;
  }

}
