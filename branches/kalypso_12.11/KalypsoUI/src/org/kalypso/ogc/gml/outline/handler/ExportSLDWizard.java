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

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.core.status.StatusDialog2;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.xml.Marshallable;

public class ExportSLDWizard extends Wizard implements IWorkbenchWizard
{
  private final ExportSLDData m_data = new ExportSLDData();

  public ExportSLDWizard( )
  {
    setWindowTitle( Messages.getString( "ExportSLDWizard_0" ) ); //$NON-NLS-1$

    setDialogSettings( DialogSettingsUtils.getDialogSettings( KalypsoGisPlugin.getDefault(), getClass().getName() ) );
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    final Object firstElement = selection.getFirstElement();
    final Marshallable elementToExport = getMarshallable( firstElement );
    if( elementToExport == null )
    {
      final String msg = String.format( "Selection must adapt to '%s'", Marshallable.class.getName() ); //$NON-NLS-1$
      throw new IllegalArgumentException( msg );
    }

    final IDialogSettings settings = getDialogSettings();
    m_data.loadState( settings );

    final String fileName = guessFileName( firstElement );
    m_data.setFilename( fileName + ".sld" ); //$NON-NLS-1$

    m_data.setMarshallable( elementToExport );

    final IWizardPage gmlFilePage = new ExportSLDWizardPage( "exportSld", m_data ); //$NON-NLS-1$
    addPage( gmlFilePage );
  }

  private Marshallable getMarshallable( final Object element )
  {
    if( element instanceof Marshallable )
      return (Marshallable)element;

    if( element instanceof IAdaptable )
      return (Marshallable)((IAdaptable)element).getAdapter( Marshallable.class );

    return null;
  }

  private String guessFileName( final Object element )
  {
    if( element instanceof IThemeNode )
      return ((IThemeNode)element).getLabel();

    return null;
  }

  @Override
  public boolean performFinish( )
  {
    // TODO: give warning when overwriting file

    final IDialogSettings settings = getDialogSettings();
    m_data.storeState( settings );

    final ExportSLDOperation operation = new ExportSLDOperation( m_data );
    final IStatus result = RunnableContextHelper.execute( getContainer(), true, false, operation );
    if( !result.isOK() )
      new StatusDialog2( getShell(), result, getWindowTitle() ).open();

    return true;
  }
}