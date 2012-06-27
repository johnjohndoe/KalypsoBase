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
package org.kalypso.core.layoutwizard;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.core.internal.layoutwizard.ChildWizardContext;

/**
 * @author Gernot Belger
 */
public final class LayoutFactory
{
  private static final String PROP_LAYOUT_LOCATION = "pageLayout"; //$NON-NLS-1$

  private LayoutFactory( )
  {
    throw new UnsupportedOperationException();
  }

  public static ILayoutPageContext createChildContext( final ILayoutPageContext defaultContext )
  {
    return new ChildWizardContext( defaultContext );
  }

  public static ILayoutPageContext createChildContext( final ILayoutPageContext defaultContext, final Arguments specificArgs )
  {
    return new ChildWizardContext( defaultContext, specificArgs );
  }

  public static LayoutParser readLayout( final ILayoutPageContext context )
  {
    final LayoutParser layoutParser = new LayoutParser( context );
    layoutParser.read();
    return layoutParser;
  }

  static URL findLayoutLocation( final ILayoutPageContext context ) throws CoreException
  {
    final Arguments arguments = context.getArguments();

    final String relativeLayoutLocation = arguments.getProperty( PROP_LAYOUT_LOCATION );
    if( relativeLayoutLocation == null )
    {
      final String message = String.format( Messages.getString("LayoutFactory_0"), PROP_LAYOUT_LOCATION ); //$NON-NLS-1$
      final IStatus status = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), message );
      throw new CoreException( status );
    }

    try
    {
      return context.resolveURI( relativeLayoutLocation );
    }
    catch( final MalformedURLException e )
    {
      final String message = String.format( Messages.getString("LayoutFactory_1"), relativeLayoutLocation ); //$NON-NLS-1$
      final IStatus status = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), message, e );
      throw new CoreException( status );
    }
  }

}