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

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.command.DefaultCommandManager;
import org.kalypso.contribs.eclipse.ui.forms.ToolkitUtils;
import org.kalypso.core.layoutwizard.ILayoutController;
import org.kalypso.core.layoutwizard.ILayoutPageContext;
import org.kalypso.core.layoutwizard.ILayoutPart;
import org.kalypso.core.layoutwizard.ILayoutWizardPage;
import org.kalypso.core.layoutwizard.LayoutFactory;
import org.kalypso.core.layoutwizard.LayoutParser;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypso.util.command.JobExclusiveCommandTarget;

/**
 * A default {@link ILayoutWizardPage} implementation.
 * 
 * @author Gernot Belger
 */
public class LayoutWizardPage extends WizardPage implements ILayoutWizardPage
{
  private final JobExclusiveCommandTarget m_commandTarget = new JobExclusiveCommandTarget( new DefaultCommandManager(), null );

  private ILayoutPart m_layoutPart;

  private ILayoutController[] m_controllers;

  private final LayoutPageContext m_wizardContext;

  private final LayoutWizardActivationHandler m_activationHandler = new LayoutWizardActivationHandler( this );

  public LayoutWizardPage( final String pageName, final URL context, final Arguments arguments )
  {
    super( pageName );

    final IWorkbench workbench = PlatformUI.getWorkbench();
    m_wizardContext = new LayoutPageContext( this, context, m_commandTarget, arguments, workbench );
  }

  @Override
  public void createControl( final Composite parent )
  {
    final FormToolkit toolkit = ToolkitUtils.createToolkit( parent );
    // REMARK: removes the gray border from the wizard page; however does not work nice
    // with pages of different type (with and wo toolkit).
    toolkit.adapt( parent );

    try
    {
      final LayoutParser parser = LayoutFactory.readLayout( m_wizardContext );
      m_layoutPart = parser.getLayoutPart();
      m_controllers = parser.getControllers();

      m_layoutPart.init();

      final Control layoutControl = m_layoutPart.createControl( parent, toolkit );
      setControl( layoutControl );

      for( final ILayoutController controller : m_controllers )
        controller.init();
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }
  }

  @Override
  public void dispose( )
  {
    for( final ILayoutController controller : m_controllers )
      controller.dispose();

    if( m_layoutPart != null )
      m_layoutPart.dispose();

    m_commandTarget.dispose();

    super.dispose();
  }

  @Override
  public void setWizard( final IWizard newWizard )
  {
    final IWizard oldWizard = getWizard();

    super.setWizard( newWizard );

    m_activationHandler.setWizard( oldWizard, newWizard );
  }

  @Override
  public void activate( )
  {
    if( m_layoutPart != null )
      m_layoutPart.activate();
  }

  @Override
  public void deactivate( )
  {
    if( m_layoutPart != null )
      m_layoutPart.deactivate();
  }

  @Override
  public ILayoutPageContext getWizardContext( )
  {
    return m_wizardContext;
  }

  @Override
  public ILayoutPart findLayoutPart( final String id )
  {
    return m_layoutPart.findPart( id );
  }

  @Override
  public IStatus saveData( final boolean doSaveGml, final IProgressMonitor monitor )
  {
    monitor.beginTask( Messages.getString( "LayoutWizardPage_0" ), 2000 ); //$NON-NLS-1$

    try
    {
      if( m_layoutPart != null )
        m_layoutPart.saveData( doSaveGml );
    }
    catch( final CoreException e )
    {
      return e.getStatus();
    }

    return Status.OK_STATUS;
  }
}