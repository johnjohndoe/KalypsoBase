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

import java.text.SimpleDateFormat;
import java.util.Date;

import org.kalypso.zml.ui.table.base.widgets.IWidgetRule;

/**
 * @author Dirk Kuch
 */
public class DateWidgetRule implements IWidgetRule
{

  /**
   * @see org.kalypso.zml.ui.table.base.widgets.ITextBoxRule#isValid(java.lang.String)
   */
  @Override
  public boolean isValid( final String text )
  {
    // TODO Auto-generated method stub
    return false;
  }

  /**
   * @see org.kalypso.zml.ui.table.base.widgets.ITextBoxRule#getLastValidationMessage()
   */
  @Override
  public String getLastValidationMessage( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypso.zml.ui.table.base.widgets.ITextBoxRule#getFormatedString(java.lang.Object)
   */
  @Override
  public String getFormatedString( final Object value )
  {
    if( value instanceof Date )
    {
// "yyyy-MM-dd'T'HH:mm:ss"
      final SimpleDateFormat sdf = new SimpleDateFormat( "yyyy-MM-dd" );

      return sdf.format( (Date) value );
    }

    return "";
  }

  /**
   * @see org.kalypso.zml.ui.table.base.widgets.ITextBoxRule#getValue(java.lang.String)
   */
  @Override
  public Object getValue( final String value )
  {
    // TODO Auto-generated method stub
    return null;
  }

}
