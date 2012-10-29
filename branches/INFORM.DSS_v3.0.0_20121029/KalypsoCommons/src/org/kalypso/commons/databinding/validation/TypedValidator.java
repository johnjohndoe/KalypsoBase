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
package org.kalypso.commons.databinding.validation;

import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.KalypsoCommonsPlugin;

/**
 * A typed version of {@link IValidator}, implement in order to do a typed validation.<br/>
 *
 * @author Gernot Belger
 */
public abstract class TypedValidator<T> implements IValidator
{
  private final Class<T> m_type;

  private final int m_severity;

  private final String m_message;

  /**
   * The constructor.
   *
   * @param type
   *          Class of type T for type safe cast.
   * @param severity
   *          Severity of IStatus, will be used to create validation failures.
   * @param message
   *          Will be used as message for a status, if validation fails.
   */
  public TypedValidator( final Class<T> type, final int severity, final String message )
  {
    m_type = type;
    m_severity = severity;
    m_message = message;
  }

  protected boolean hasMessage( )
  {
    return m_message != null;
  }

  /**
   * Overwritten in order to do a typed validation.<br/>
   * Implement {@link #doValidate(T)} instead.
   *
   * @see org.eclipse.core.databinding.validation.IValidator#validate(java.lang.Object)
   */
  @Override
  public final IStatus validate( final Object value )
  {
    try
    {
      return doValidate( m_type.cast( value ) );
    }
    catch( final CoreException e )
    {
      return e.getStatus();
    }
  }

  /**
   * Helper to simply fail the validation.<br/>
   * Creates a status from the severity, message given in the constructor of this class.
   */
  protected final void fail( ) throws CoreException
  {
    fail( m_message );
  }

  /**
   * Helper to simply fail the validation.<br/>
   * Creates a status from the severity but uses a specific message.
   */
  protected void fail( final String message ) throws CoreException
  {
    final Status status = new Status( m_severity, KalypsoCommonsPlugin.getID(), message );
    throw new CoreException( status );
  }

  /**
   * Validate the given value.<br/>
   * This method can either return an {@link IStatus} or throw an {@link CoreException} whose status will be returned to
   * the validator.
   *
   * @see #validate(Object)
   */
  protected abstract IStatus doValidate( T value ) throws CoreException;
}
