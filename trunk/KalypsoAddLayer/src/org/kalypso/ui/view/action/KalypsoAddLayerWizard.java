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

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.IWorkbenchGraphicConstants;
import org.eclipse.ui.internal.WorkbenchImages;
import org.eclipse.ui.model.AdaptableList;
import org.kalypso.ui.addlayer.MapExtensions;
import org.kalypso.ui.editor.mapeditor.GisMapOutlinePage;
import org.kalypso.ui.i18n.Messages;

/**
 * This class extends the ImportWizard. This enables to call the import wizard from any action. Entry point to kalypso
 * import wizards.
 */
@SuppressWarnings("restriction")
public class KalypsoAddLayerWizard extends Wizard
{
  private final GisMapOutlinePage m_outlineviewer;

  private final IWorkbench m_workbench;

  private final IStructuredSelection m_selection;

  /**
   * Returns the import wizards that are available for invocation.
   */
  public KalypsoAddLayerWizard( final GisMapOutlinePage outlineviewer, final IStructuredSelection selection, final IWorkbench workbench, final String wizardSelectionId )
  {
    m_outlineviewer = outlineviewer;
    m_selection = selection;
    m_workbench = workbench;

    final AdaptableList wizards = MapExtensions.getAvailableWizards( wizardSelectionId );

    setWindowTitle( Messages.getString( "org.kalypso.ui.view.action.KalypsoAddLayerWizard.2" ) ); //$NON-NLS-1$
    setDefaultPageImageDescriptor( WorkbenchImages.getImageDescriptor( IWorkbenchGraphicConstants.IMG_WIZBAN_IMPORT_WIZ ) );
    setNeedsProgressMonitor( true );

    final String message = Messages.getString( "org.kalypso.ui.view.action.KalypsoAddLayerWizard.0" ); //$NON-NLS-1$
    final KalypsoWizardSelectionPage page = new KalypsoWizardSelectionPage( m_workbench, m_selection, wizards, message, m_outlineviewer );
    page.setDescription( Messages.getString( "org.kalypso.ui.view.action.KalypsoAddLayerWizard.1" ) ); //$NON-NLS-1$
    addPage( page );
  }

  @Override
  public boolean performFinish( )
  {
    return true;
  }
}