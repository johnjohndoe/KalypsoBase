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
package org.kalypso.commons.databinding.validation;

import java.text.ParseException;
import java.text.SimpleDateFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.KalypsoCommonsPlugin;

/**
 * This validator checks, if text matches a given pattern.
 * 
 * @author Holger Albert
 */
public class StringSimpleDateValidator extends TypedValidator<String>
{
  /**
   * Against this pattern vill be validated.
   */
  private final String m_pattern;

  /**
   * The simple date format.
   */
  private final SimpleDateFormat m_dateFormat;

  /**
   * The constructor.
   * 
   * @param severity
   *          Severity of IStatus, will be used to create validation failures.
   * @param message
   *          Will be used as message for a status, if validation fails.
   * @param pattern
   *          Against this pattern vill be validated.
   */
  public StringSimpleDateValidator( final int severity, final String message, final String pattern )
  {
    super( String.class, severity, message );

    m_pattern = pattern;
    m_dateFormat = new SimpleDateFormat( pattern );
  }

  /**
   * @see org.kalypso.commons.databinding.validation.TypedValidator#doValidate(java.lang.Object)
   */
  @Override
  protected IStatus doValidate( final String value )
  {
    try
    {
      if( value == null || value.length() == 0 )
        return new Status( IStatus.OK, KalypsoCommonsPlugin.getID(), "OK" );

      m_dateFormat.parse( value );

      return Status.OK_STATUS;
    }
    catch( final ParseException ex )
    {
      return new Status( IStatus.ERROR, KalypsoCommonsPlugin.getID(), String.format( "Ungültiges Datum für Pattern '%s': %s", m_pattern, ex.getLocalizedMessage() ), ex );
    }
  }
}