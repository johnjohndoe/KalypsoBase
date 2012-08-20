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

/**
 * This validator fails if a number is provided but is outside a validity range
 *
 * @author Holger Albert
 */
public class NumberRangeValidator extends TypedValidator<Number>
{
  public static final String DEFAULT_MESSAGE = "Value should not be negative.";

  private final double m_minValue;

  private final double m_maxValue;

  public NumberRangeValidator( final int severity, final double minValue, final double maxValue )
  {
    this( severity, minValue, maxValue, null );
  }

  /**
   * @param severity
   *          Severity of IStatus, will be used to create validation failures.
   * @param minValue
   *          Minimal allowed value (inclusive)
   * @param maxValue
   *          Maximal allowed value (inclusive)
   */
  public NumberRangeValidator( final int severity, final double minValue, final double maxValue, final String message )
  {
    super( Number.class, severity, message );

    m_minValue = minValue;
    m_maxValue = maxValue;
  }

  @Override
  protected IStatus doValidate( final Number value ) throws CoreException
  {
    final boolean isValid = isValid( value );
    if( isValid )
      return Status.OK_STATUS;

    if( hasMessage() )
      fail();

    final String message = String.format( "Value outside valid range [%f - %f]", m_minValue, m_maxValue );
    fail( message );
    return null;
  }

  private boolean isValid( final Number value )
  {
    if( value == null )
      return true;

    final double dblVal = value.doubleValue();
    if( !Double.isNaN( m_minValue ) && dblVal < m_minValue )
      return false;

    if( !Double.isNaN( m_maxValue ) && dblVal > m_maxValue )
      return false;

    return true;
  }
}