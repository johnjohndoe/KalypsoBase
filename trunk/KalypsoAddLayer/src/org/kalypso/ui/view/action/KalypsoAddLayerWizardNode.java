/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.ui.view.action;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerDropAdapter;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbenchWizard;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardElement;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardNode;
import org.eclipse.ui.internal.dialogs.WorkbenchWizardSelectionPage;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.outline.GisMapOutlineDropData;
import org.kalypso.ui.addlayer.IKalypsoDataImportWizard;
import org.kalypso.ui.editor.mapeditor.GisMapOutlinePage;

/**
 * @author Gernot Belger
 */
@SuppressWarnings("restriction")
final class KalypsoAddLayerWizardNode extends WorkbenchWizardNode
{
  private final String m_windowTitle;

  private final GisMapOutlinePage m_outline;

  private final ISelection m_selection;

  KalypsoAddLayerWizardNode( final WorkbenchWizardSelectionPage aWizardPage, final WorkbenchWizardElement element, final String windowTitle, final GisMapOutlinePage outline, final ISelection selection )
  {
    super( aWizardPage, element );

    m_windowTitle = windowTitle;
    m_outline = outline;
    m_selection = selection;
  }

  @Override
  public IWorkbenchWizard createWizard( ) throws CoreException
  {
    /* Find the right map modell */
    final IKalypsoLayerModell mapModell = findMapModell();

    final IKalypsoDataImportWizard newWizard = (IKalypsoDataImportWizard) getWizardElement().createWizard();
    newWizard.setCommandTarget( m_outline );

    final Object selectedTheme = getSelectedTheme();

    final GisMapOutlineDropData data = GisMapOutlineDropData.fromCurrentSelectionNonNull( mapModell, selectedTheme, ViewerDropAdapter.LOCATION_ON );

    newWizard.setMapModel( data.getLayerModel(), data.getInsertionIndex() );

    if( newWizard instanceof Wizard )
      ((Wizard) newWizard).setWindowTitle( m_windowTitle );

    return newWizard;
  }

  private Object getSelectedTheme( )
  {
    if( m_selection instanceof IStructuredSelection )
    {
      final IStructuredSelection sel = (IStructuredSelection) m_selection;
      return sel.getFirstElement();
    }

    return null;
  }

  private IKalypsoLayerModell findMapModell( )
  {
    if( m_selection instanceof StructuredSelection )
    {
      final Object firstElement = ((IStructuredSelection) m_selection).getFirstElement();
      if( firstElement instanceof IKalypsoLayerModell )
        return (IKalypsoLayerModell) firstElement;

      if( firstElement instanceof IKalypsoTheme )
        return (IKalypsoLayerModell) ((IKalypsoTheme) firstElement).getMapModell();
    }

    /* Without valid selection, new themes go top-level */
    return m_outline.getMapPanel().getMapModell();
  }
}