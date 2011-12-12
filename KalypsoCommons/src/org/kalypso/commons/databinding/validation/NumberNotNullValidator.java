/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.lang.Objects;

/**
 * This validator checks, if a number was provided.
 * 
 * @author Holger Albert
 */
public class NumberNotNullValidator extends TypedValidator<Number>
{
  public static final String DEFAULT_MESSAGE = "Value should not be negative.";

  /**
   * Same as {@link #NumberNotNegativeValidator(int, String)}, but uses a default message.
   * 
   * @param severity
   *          Severity of IStatus, will be used to create validation failures.
   */
  public NumberNotNullValidator( final int severity )
  {
    this( severity, DEFAULT_MESSAGE );
  }

  /**
   * @param severity
   *          Severity of IStatus, will be used to create validation failures.
   * @param message
   *          Will be used as message for a status, if validation fails.
   */
  public NumberNotNullValidator( final int severity, final String message )
  {
    super( Number.class, severity, message );
  }

  /**
   * @see org.kalypso.commons.databinding.validation.TypedValidator#doValidate(java.lang.Object)
   */
  @Override
  protected IStatus doValidate( final Number value ) throws CoreException
  {
    if( Objects.isNull( value ) )
      fail();

    return Status.OK_STATUS;
  }
}