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
package org.kalypso.ui.layoutwizard;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.catalog.CatalogManager;
import org.kalypso.core.layoutwizard.ILayoutPageContext;
import org.kalypso.core.layoutwizard.ILayoutWizardPage;

/**
 * @author Gernot Belger
 */
public class LayoutPageContext implements ILayoutPageContext
{
  private final ICommandTarget m_commandTarget;

  private final ILayoutWizardPage m_page;

  private final URL m_context;

  private final Arguments m_arguments;

  private final IServiceLocator m_locator;

  public LayoutPageContext( final ILayoutWizardPage page, final URL context, final ICommandTarget commandTarget, final Arguments arguments, final IServiceLocator locator )
  {
    m_page = page;
    m_context = context;
    m_commandTarget = commandTarget;
    m_arguments = arguments;
    m_locator = locator;
  }

  @Override
  public Arguments getArguments( )
  {
    return m_arguments;
  }

  @Override
  public URL getContext( )
  {
    return m_context;
  }

  @Override
  public Shell getShell( )
  {
    return m_page.getShell();
  }

  protected ICommandTarget getCommandTarget( )
  {
    return m_commandTarget;
  }

  @Override
  public void updateButtons( )
  {
    final Display display = getShell().getDisplay();
    if( display.isDisposed() )
      return;

    final IWizardContainer container = getContainer();

    final Runnable updateButtonsRunnable = new Runnable()
    {
      @Override
      public void run( )
      {
        if( !display.isDisposed() )
        {
          container.updateButtons();
        }
      }
    };
    display.asyncExec( updateButtonsRunnable );
  }

  private IWizardContainer getContainer( )
  {
    final IWizard wizard = m_page.getWizard();
    if( wizard == null )
      return null;

    return wizard.getContainer();
  }

  @Override
  public Object getService( final Class api )
  {
    if( m_locator == null )
      return null;

    return m_locator.getService( api );
  }

  @Override
  public boolean hasService( final Class api )
  {
    if( m_locator == null )
      return false;

    return m_locator.hasService( api );
  }

  @Override
  public void postCommand( final ICommand command, final Runnable runnable )
  {
    m_commandTarget.postCommand( command, runnable );
  }

  @Override
  public void reflow( )
  {
    final Control pageControl = m_page.getControl();

    if( !(pageControl instanceof Composite) )
      return;

    if( pageControl.isDisposed() )
      return;

    final Display display = pageControl.getDisplay();
    if( display.isDisposed() )
      return;

    display.asyncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        if( pageControl.isDisposed() )
          return;

        final Composite composite = (Composite)pageControl;
        composite.layout( true, true );
      }
    } );
  }

  @Override
  public ILayoutWizardPage getPage( )
  {
    return m_page;
  }

  @Override
  public URL resolveURI( final String uri ) throws MalformedURLException
  {
    final CatalogManager catalogManager = KalypsoCorePlugin.getDefault().getCatalogManager();
    final String resolvedUri = catalogManager.resolve( uri, uri );
    return UrlResolverSingleton.resolveUrl( m_context, resolvedUri );
  }

  @Override
  public void setProperty( final String key, final Object value )
  {
    throw new UnsupportedOperationException( "Setting property for this context not allowed." ); //$NON-NLS-1$
  }
}