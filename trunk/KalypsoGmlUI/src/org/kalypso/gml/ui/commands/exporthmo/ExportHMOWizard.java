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
package org.kalypso.gml.ui.commands.exporthmo;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.gml.ui.commands.exportshape.ExportShapeUtils;
import org.kalypso.gml.ui.extensions.FeatureSelectionTester;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.gml.ui.internal.util.GenericFeatureSelection;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.ogc.gml.selection.FeatureSelectionHelper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypso.shape.deegree.GenericShapeDataFactory;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;

public class ExportHMOWizard extends Wizard implements IWorkbenchWizard
{
  private final ExportHMOData m_data = new ExportHMOData();

  public ExportHMOWizard( )
  {
    final String title = Messages.getString( "org.kalypso.ogc.gml.outline.handler.ExportHMOHandler.2" ); //$NON-NLS-1$
    setWindowTitle( title );

    setDialogSettings( DialogSettingsUtils.getDialogSettings( KalypsoGisPlugin.getDefault(), getClass().getName() ) );
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    final IFeatureSelection featureSelection = GenericFeatureSelection.create( selection, null );
    if( featureSelection == null || featureSelection.size() == 0 )
      throw new IllegalStateException( "No features in selection. Please select features for export." ); //$NON-NLS-1$

    final Feature[] features = FeatureSelectionHelper.getFeatures( featureSelection );

    final IFeatureType featureType = GenericShapeDataFactory.findLeastCommonType( features );
    final IValuePropertyType[] tinTypes = FeatureSelectionTester.findGeometryTypes( featureType, GM_TriangulatedSurface.class );
    if( tinTypes.length == 0 )
      throw new IllegalStateException( String.format( Messages.getString( "ExportHMOWizard.0" ), featureType ) ); //$NON-NLS-1$

    final IDialogSettings settings = getDialogSettings();
    m_data.loadState( settings );

    final String fileName = ExportShapeUtils.guessExportFileName( selection );

    m_data.setFilename( fileName + ".hmo" ); //$NON-NLS-1$

    m_data.setFeatures( features );

    final IWizardPage gmlFilePage = new ExportHMOWizardPage( "exportHmo", m_data ); //$NON-NLS-1$
    addPage( gmlFilePage );
  }

  @Override
  public boolean performFinish( )
  {
    // TODO: give warning when overwriting file

    final IDialogSettings settings = getDialogSettings();
    m_data.storeState( settings );

    final ExportHMOOperation operation = new ExportHMOOperation( m_data );
    final IStatus result = RunnableContextHelper.execute( getContainer(), true, false, operation );
    if( !result.isOK() )
      StatusDialog.open( getShell(), result, getWindowTitle() );

    return true;
  }
}