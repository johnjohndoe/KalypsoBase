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
package org.kalypso.core.internal.layoutwizard;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.swt.widgets.Shell;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.command.ICommand;
import org.kalypso.core.layoutwizard.ILayoutPageContext;
import org.kalypso.core.layoutwizard.ILayoutWizardPage;

/**
 * @author Gernot Belger
 */
public class ChildWizardContext implements ILayoutPageContext
{
  private final ILayoutPageContext m_context;

  private final Arguments m_arguments;

  public ChildWizardContext( final ILayoutPageContext context )
  {
    this( context, context.getArguments() );
  }

  public ChildWizardContext( final ILayoutPageContext context, final Arguments arguments )
  {
    m_context = context;
    m_arguments = new Arguments( arguments );
  }

  @Override
  public void setProperty( final String key, final Object value )
  {
    m_arguments.put( key, value );
  }

  @Override
  public Arguments getArguments( )
  {
    return m_arguments;
  }

  @Override
  public URL getContext( )
  {
    return m_context.getContext();
  }

  @Override
  public URL resolveURI( final String uri ) throws MalformedURLException
  {
    return m_context.resolveURI( uri );
  }

  @Override
  public Shell getShell( )
  {
    return m_context.getShell();
  }

  @Override
  public void updateButtons( )
  {
    m_context.updateButtons();
  }

  @Override
  public Object getService( final Class api )
  {
    return m_context.getService( api );
  }

  @Override
  public boolean hasService( final Class api )
  {
    return m_context.hasService( api );
  }

  @Override
  public void postCommand( final ICommand command, final Runnable runnable )
  {
    m_context.postCommand( command, runnable );
  }

  @Override
  public void reflow( )
  {
    m_context.reflow();
  }

  @Override
  public ILayoutWizardPage getPage( )
  {
    return m_context.getPage();
  }
}
