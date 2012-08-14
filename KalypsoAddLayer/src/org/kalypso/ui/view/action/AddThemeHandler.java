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
package org.kalypso.ui.view.action;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.kalypso.ogc.gml.map.handlers.MapHandlerUtils;
import org.kalypso.ui.editor.mapeditor.GisMapOutlinePage;

/**
 * @author Gernot Belger
 */
public class AddThemeHandler extends AbstractHandler
{
  /* id of an extension ot 'org.kalypso.ui.addLayerWizard#wizardSelection', if not set, a default id is used. */
  private static final String PARAMETER_WIZARD_SELECTION = "wizardSelectioinId"; //$NON-NLS-1$

  private static final String WIZARD_SELECTION_DEFAULT = "org.kalypso.ui.addlayer.wizardselection.default"; //$NON-NLS-1$

  @Override
  public Object execute( final ExecutionEvent event )
  {
    final IEvaluationContext context = (IEvaluationContext) event.getApplicationContext();

    final String wizardSelectionId = getWizardSelectionId( event );

    final IWorkbenchWindow activeWorkbenchWindow = (IWorkbenchWindow) context.getVariable( ISources.ACTIVE_WORKBENCH_WINDOW_NAME );
    final Shell shell = (Shell) context.getVariable( ISources.ACTIVE_SHELL_NAME );
    final IStructuredSelection selection = (IStructuredSelection) context.getVariable( ISources.ACTIVE_CURRENT_SELECTION_NAME );

    final GisMapOutlinePage viewer = MapHandlerUtils.getMapOutline( context );

    final IWorkbench workbench = activeWorkbenchWindow.getWorkbench();

    final KalypsoAddLayerWizard wizard = new KalypsoAddLayerWizard( viewer, selection, workbench, wizardSelectionId );
    wizard.setForcePreviousAndNextButtons( true );
    final WizardDialog dialog = new WizardDialog( shell, wizard );
    dialog.open();

    return null;
  }

  private String getWizardSelectionId( final ExecutionEvent event )
  {
    final String wizardSelectionId = event.getParameter( PARAMETER_WIZARD_SELECTION );
    if( StringUtils.isBlank( wizardSelectionId ) )
      return WIZARD_SELECTION_DEFAULT;

    return wizardSelectionId;
  }
}