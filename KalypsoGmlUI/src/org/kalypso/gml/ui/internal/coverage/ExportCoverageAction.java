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
package org.kalypso.gml.ui.internal.coverage;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.KalypsoGmlUiImages;
import org.kalypso.gml.ui.commands.exportgrid.RectifiedGridCoverageExportWizard;
import org.kalypso.gml.ui.coverage.CoverageManagementWidget;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;

/**
 * @author Gernot Belger
 */
public class ExportCoverageAction extends Action implements IUpdateable
{
  private final CoverageManagementWidget m_widget;

  public ExportCoverageAction( final CoverageManagementWidget widget )
  {
    m_widget = widget;

    setText( Messages.getString( "org.kalypso.gml.ui.map.CoverageManagementWidget.13" ) ); //$NON-NLS-1$
    setImageDescriptor( KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.COVERAGE_EXPORT ) );
  }

  @Override
  public void runWithEvent( final Event event )
  {
    final ICoverage[] selectedCoverages = m_widget.getSelectedCoverages();

    if( ArrayUtils.isEmpty( selectedCoverages ) )
      return;

    // open wizard to locate destination
    final RectifiedGridCoverageExportWizard wizard = new RectifiedGridCoverageExportWizard();
    wizard.init( PlatformUI.getWorkbench(), new StructuredSelection( selectedCoverages ) );

    final WizardDialog wizardDialog = new WizardDialog( event.display.getActiveShell(), wizard );
    wizardDialog.open();
  }

  @Override
  public void update( )
  {
    final ICoverage[] selectedCoverages = m_widget.getSelectedCoverages();
    setEnabled( selectedCoverages.length > 0 );
  }
}