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
package org.kalypso.gml.ui.internal.coverage;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.KalypsoGmlUiImages;
import org.kalypso.gml.ui.coverage.CoverageManagementWidget;
import org.kalypso.gml.ui.coverage.ImportCoverageUtilities;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.gml.ui.internal.coverage.imports.ImportCoveragesWizard;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;

/**
 * @author Gernot Belger
 */
public class AddCoverageAction extends Action implements IUpdateable
{
  private final CoverageManagementWidget m_widget;

  public AddCoverageAction( final CoverageManagementWidget widget )
  {
    m_widget = widget;

    setText( Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.11" ) ); //$NON-NLS-1$
    setImageDescriptor( KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_ADD ) );
  }

  @Override
  public void runWithEvent( final Event event )
  {
    final Shell shell = event.display.getActiveShell();

    final ICoverageCollection coverages = m_widget.getCoverageCollection();
    final boolean m_allowUserChangeDataFolder = m_widget.isAllowUserChangeDataFolder();

    final IContainer gridFolder = m_widget.findGridFolder();

    final ImportCoveragesWizard wizard = new ImportCoveragesWizard();
    wizard.init( coverages, gridFolder, m_allowUserChangeDataFolder );
    final WizardDialog wizardDialog = new WizardDialog( shell, wizard );
    if( wizardDialog.open() != Window.OK )
      return;

    /* Get the new coverages. */
    final ICoverage[] newCoverages = wizard.getNewCoverages();

    /* Zoom to the new coverages. */
    ImportCoverageUtilities.zoomToCoverages( newCoverages, m_widget );

    /* Selct the new coverages in the widget. */
    m_widget.handleCoveragesAdded( newCoverages );

    /* Save the coverages. */
    final IStatus status = ImportCoverageUtilities.saveCoverages( m_widget );

    /* Show an error dialog. */
    ErrorDialog.openError( shell, Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.11" ), Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.19" ), status ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public void update( )
  {
    final ICoverage[] allCoverages = m_widget.getCoverages();
    setEnabled( allCoverages != null );
  }
}
