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

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.IPageChangingListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.dialogs.PageChangingEvent;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.commands.ICommandService;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.wizard.view.WizardView;
import org.kalypso.core.layoutwizard.ILayoutPageContext;
import org.kalypso.core.layoutwizard.ILayoutWizardPage;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * Handles activation / deactivation of ILayoutWizardPage when page changes in wizard.
 * 
 * @author Gernot Belger
 */
public class LayoutWizardActivationHandler
{
  /**
   * This listeners reports failure of command executions within this page <br>
   * TODO: may now be moved to a more central place.
   */
  private final IExecutionListener m_cmdExecutionListener = new IExecutionListener()
  {
    @Override
    public void notHandled( final String commandId, final org.eclipse.core.commands.NotHandledException exception )
    {
    }

    @Override
    public void postExecuteFailure( final String commandId, final ExecutionException exception )
    {
      handleExecutionFailure( exception );
    }

    @Override
    public void postExecuteSuccess( final String commandId, final Object returnValue )
    {
    }

    @Override
    public void preExecute( final String commandId, final ExecutionEvent event )
    {
    }
  };

  private final IPageChangingListener m_pageChangingListener = new IPageChangingListener()
  {
    @Override
    public void handlePageChanging( final PageChangingEvent event )
    {
      if( event.getCurrentPage() == m_page )
        event.doit = LayoutWizardActivationHandler.this.handlePageChanging();
    }
  };

  private final IPageChangedListener m_pageChangedListener = new IPageChangedListener()
  {
    @Override
    public void pageChanged( final PageChangedEvent event )
    {
      if( event.getSelectedPage() == m_page )
        handlePageSelected();
    }
  };

  final ILayoutWizardPage m_page;

  public LayoutWizardActivationHandler( final ILayoutWizardPage page )
  {
    Assert.isNotNull( page );

    m_page = page;
  }

  public void setWizard( final IWizard oldWizard, final IWizard newWizard )
  {
    unhookWizard( oldWizard );

    if( newWizard == null )
      return;

    hookWizard( newWizard );
  }

  private void hookWizard( final IWizard newWizard )
  {
    final ILayoutPageContext wizardContext = m_page.getWizardContext();
    if( wizardContext == null )
      return;

    // TODO: not good: will answer to any command execution
    // We should restrict this somehow to commands from the map toolbar
    final ICommandService cmdService = (ICommandService)wizardContext.getService( ICommandService.class );
    if( cmdService != null )
      cmdService.addExecutionListener( m_cmdExecutionListener );

    final IWizardContainer container = newWizard.getContainer();
    if( container instanceof WizardView )
    {
      final WizardView view = (WizardView)container;
      view.addPageChangingListener( m_pageChangingListener );
      view.addPageChangedListener( m_pageChangedListener );
    }
    else if( container instanceof WizardDialog )
    {
      final WizardDialog dialog = (WizardDialog)container;
      dialog.addPageChangingListener( m_pageChangingListener );
      dialog.addPageChangedListener( m_pageChangedListener );
    }
  }

  private void unhookWizard( final IWizard oldWizard )
  {
    if( oldWizard == null )
      return;

    final ICommandService cmdService = (ICommandService)m_page.getWizardContext().getService( ICommandService.class );
    if( cmdService != null )
      cmdService.removeExecutionListener( m_cmdExecutionListener );

    final IWizardContainer container = oldWizard.getContainer();
    if( container instanceof WizardView )
    {
      final WizardView view = (WizardView)container;
      view.removePageChangingListener( m_pageChangingListener );
      view.removePageChangedListener( m_pageChangedListener );
    }
    else if( container instanceof WizardDialog )
    {
      final WizardDialog dialog = (WizardDialog)container;
      dialog.removePageChangingListener( m_pageChangingListener );
      dialog.removePageChangedListener( m_pageChangedListener );
    }
  }

  protected void handleExecutionFailure( final Exception exception )
  {
    /* Avoid swt thread access */
    if( Display.getCurrent() == null )
      return;

    final IWizardPage currentPage = m_page.getWizard().getContainer().getCurrentPage();
    if( m_page == currentPage )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( exception );
      StatusDialog.open( m_page.getShell(), status, Messages.getString( "LayoutWizardActivationHandler_0" ) ); //$NON-NLS-1$
    }
  }

  /**
   * Will be called if this page is about to be closed (i.e another page is selected).<br>
   * Default behaviour is to unhook any registered context-activations.<br>
   * Client who want to override should call the super implementation, if they return <code>true</code>.
   * 
   * @return <code>false</code>, if the page shall not be exited now.
   */
  protected boolean handlePageChanging( )
  {
    m_page.deactivate();
    return true;
  }

  /**
   * Will be called after this page has been selected.<br>
   * Default behaviour is to regsiter context-activations for this page if necessary (for example the map-context is
   * registered here, if the the page has any map.<br>
   * Client who want to override should always call the super implementation.
   */
  protected void handlePageSelected( )
  {
    m_page.activate();
  }
}
