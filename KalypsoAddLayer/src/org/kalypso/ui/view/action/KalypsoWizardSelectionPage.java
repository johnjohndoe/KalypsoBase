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
package org.kalypso.ui.view.action;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardListSelectionPage;
import org.eclipse.ui.model.AdaptableList;
import org.kalypso.ui.editor.mapeditor.GisMapOutlinePage;

/**
 * @author Christoph Küpferle
 */
@SuppressWarnings("restriction")
public class KalypsoWizardSelectionPage extends WorkbenchWizardListSelectionPage
{
  private final GisMapOutlinePage m_outline;

  private final ISelection m_selection;

  public KalypsoWizardSelectionPage( final IWorkbench aWorkbench, final IStructuredSelection selection, final AdaptableList wizardElts, final String message, final GisMapOutlinePage outlineview )
  {
    super( aWorkbench, selection, wizardElts, message, null );

    m_outline = outlineview;
    m_selection = selection;
  }

  @Override
  protected IWizardNode createWizardNode( final WorkbenchWizardElement element )
  {
    final String windowTitle = KalypsoWizardSelectionPage.this.getWizard().getWindowTitle();
    return new KalypsoAddLayerWizardNode( this, element, windowTitle, m_outline, m_selection );
  }
}