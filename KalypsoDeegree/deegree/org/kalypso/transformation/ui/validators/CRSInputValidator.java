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
package org.kalypso.transformation.ui.validators;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IInputValidator;
import org.kalypso.commons.databinding.validation.TypedValidator;
import org.kalypso.deegree.i18n.Messages;
import org.kalypso.transformation.CRSHelper;

/**
 * This validator makes sure a existing coordinate system is selected.
 *
 * @author Holger Albert
 */
public class CRSInputValidator extends TypedValidator<String> implements IInputValidator
{
  public CRSInputValidator( )
  {
    super( String.class, IStatus.ERROR, StringUtils.EMPTY );
  }

  @Override
  public String isValid( final String newText )
  {
    if( !CRSHelper.isKnownCRS( newText ) )
      return String.format( Messages.getString( "org.kalypso.transformation.ui.validators.CRSInputValidator.0" ), newText ); //$NON-NLS-1$

    return null;
  }

  @Override
  protected IStatus doValidate( final String value ) throws CoreException
  {
    final String message = isValid( value );
    if( message != null )
      fail( message );

    return Status.OK_STATUS;
  }
}