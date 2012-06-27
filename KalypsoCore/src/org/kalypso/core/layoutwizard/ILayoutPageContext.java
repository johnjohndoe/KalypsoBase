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
package org.kalypso.core.layoutwizard;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.command.ICommandTarget;

/**
 * @author Gernot Belger
 */
public interface ILayoutPageContext extends IServiceLocator, ICommandTarget
{
  /**
   * The context against which path arguments (see {@link getArguments()} should be resolved.
   */
  URL getContext( );

  Arguments getArguments( );

  Shell getShell( );

  void updateButtons( );

  /**
   * Call to relayout the whole layout-structure. Typically called by a {@link ILayoutPart} whose size might have
   * changed.
   */
  void reflow( );

  ILayoutWizardPage getPage( );

  URL resolveURI( String uri ) throws MalformedURLException;

  void setProperty( final String key, final Object value );
}