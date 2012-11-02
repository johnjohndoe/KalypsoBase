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
package org.kalypso.ogc.gml.outline.handler;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.core.status.StatusDialog2;
import org.kalypso.ogc.gml.map.handlers.MapHandlerUtils;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;

public class ExportLegendWizard extends Wizard implements IWorkbenchWizard
{
  private final ExportLegendData m_data = new ExportLegendData();

  public ExportLegendWizard( )
  {
    setWindowTitle( Messages.getString( "org.kalypso.ogc.gml.outline.handler.LegendExportHandler.2" ) ); //$NON-NLS-1$

    setDialogSettings( DialogSettingsUtils.getDialogSettings( KalypsoGisPlugin.getDefault(), getClass().getName() ) );
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    /* Get the selected elements. */
    final IThemeNode[] nodes = MapHandlerUtils.getSelectedNodes( selection );
    if( nodes.length == 0 )
      throw new IllegalArgumentException( Messages.getString( "org.kalypso.ogc.gml.outline.handler.LegendExportHandler.3" ) ); //$NON-NLS-1$

    final IDialogSettings settings = getDialogSettings();
    m_data.loadState( settings );

    m_data.setNodes( nodes );
    final String fileName = guessFileName( nodes );
    m_data.setFilename( fileName + ".png" ); //$NON-NLS-1$

    final IWizardPage gmlFilePage = new ExportLegendWizardPage( "exportLegend", m_data ); //$NON-NLS-1$
    addPage( gmlFilePage );
  }

  private String guessFileName( final IThemeNode[] nodes )
  {
    /* Ask user for file */
    if( nodes.length == 1 )
      return nodes[0].getLabel();
    else
      return nodes[0].getParent().getLabel();
  }

  @Override
  public boolean performFinish( )
  {
    // TODO: give warning when overwriting file

    final IDialogSettings settings = getDialogSettings();
    m_data.storeState( settings );

    final Display display = getShell().getDisplay();
    final ExportLegendOperation operation = new ExportLegendOperation( m_data, display );

    BusyIndicator.showWhile( display, operation );
    final IStatus result = operation.getResult();
    if( !result.isOK() )
      new StatusDialog2( getShell(), result, getWindowTitle() ).open();

    return true;
  }
}