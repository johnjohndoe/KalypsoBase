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
package org.kalypso.ui.editor.styleeditor.graphic;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.validation.ValidationStatus;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.databinding.validation.TypedValidator;
import org.kalypso.commons.java.net.UrlUtilities;
import org.kalypso.contribs.java.net.IUrlResolver2;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.graphics.sld.ExternalGraphic;

/**
 * @author Gernot Belger
 */
public class OnlineResourceValidator extends TypedValidator<String>
{
  private final IStyleInput<ExternalGraphic> m_input;

  public OnlineResourceValidator( final IStyleInput<ExternalGraphic> input )
  {
    super( String.class, IStatus.ERROR, StringUtils.EMPTY );

    m_input = input;
  }

  /**
   * @see org.kalypso.commons.databinding.validation.TypedValidator#doValidate(java.lang.Object)
   */
  @Override
  protected IStatus doValidate( final String value )
  {
    try
    {
      if( StringUtils.isBlank( value ) )
        return ValidationStatus.ok();

      final IUrlResolver2 resolver = m_input.getData().getResolver();
      final URL location = resolver.resolveURL( value );
      if( location == null )
        return ValidationStatus.ok();

      if( !UrlUtilities.checkIsAccessible( location ) )
        return ValidationStatus.warning( String.format( Messages.getString( "OnlineResourceValidator_0" ), location ) ); //$NON-NLS-1$
    }
    catch( final MalformedURLException e )
    {
      return ValidationStatus.error( Messages.getString( "OnlineResourceValidator_1" ), e ); //$NON-NLS-1$
    }

    return ValidationStatus.ok();
  }
}
