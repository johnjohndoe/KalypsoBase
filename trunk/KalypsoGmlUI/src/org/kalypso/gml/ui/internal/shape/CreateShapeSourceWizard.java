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
package org.kalypso.gml.ui.internal.shape;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.ui.wizard.AbstractDataImportWizard;
import org.kalypso.ui.wizard.shape.ImportShapeFileData;
import org.kalypso.ui.wizard.shape.ImportShapeFileData.StyleImport;
import org.kalypso.ui.wizard.shape.ImportShapeOperation;
import org.kalypsodeegree.KalypsoDeegreePlugin;

/**
 * Create a new shape file and directly add is as theme to the map.
 *
 * @author Gernot Belger
 */
public class CreateShapeSourceWizard extends AbstractDataImportWizard
{
  private final ShapeFileNewData m_shapeFileNewData = new ShapeFileNewData();

  public CreateShapeSourceWizard( )
  {
    setNeedsProgressMonitor( true );
    setHelpAvailable( false );

    setWindowTitle( Messages.getString( "ShapeFileNewWizard_0" ) ); //$NON-NLS-1$
  }

  @Override
  public void addPages( )
  {
    m_shapeFileNewData.init( getDialogSettings() );

    addPage( new ShapeFileNewFilePage( "filepath", m_shapeFileNewData ) ); //$NON-NLS-1$
    addPage( new ShapeFileNewSignaturePage( "signature", m_shapeFileNewData ) ); //$NON-NLS-1$
  }

  @Override
  public boolean performFinish( )
  {
    m_shapeFileNewData.storeSettings( getDialogSettings() );

    final IStatus result = doFinish();
    if( result.matches( IStatus.CANCEL ) )
      return false;

    if( !result.isOK() )
      StatusDialog.open( getShell(), result, getWindowTitle() );

    if( result.matches( IStatus.ERROR ) )
      return false;

    /* add new theme */
    final IPath shapeFilePath = m_shapeFileNewData.getShpFile().getFullPath();

    final ImportShapeFileData importData = new ImportShapeFileData();
    importData.setInsertionIndex( getInsertionIndex() );

    importData.setSrs( KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );

    // REMARK: setting the shape path after the import type has the effect, that, if an sld file already exists,
    // it will always be reused. This is intended.
    importData.setStyleImportType( StyleImport.generateDefault );
    importData.getShapeFile().setPath( shapeFilePath );

    final ImportShapeOperation addThemeOperation = new ImportShapeOperation( importData );
    return addThemeOperation.executeOnWizard( this );
  }

  public IStatus doFinish( )
  {
    try
    {
      final IFile baseFile = m_shapeFileNewData.getShpFile();

      final Shell shell = getShell();
      final String title = getWindowTitle();

      if( !ShapeFileNewWizard.askForAndDeleteExistingFiles( shell, title, baseFile ) )
        return Status.CANCEL_STATUS;

      final ShapeFileNewOperation operation = new ShapeFileNewOperation( m_shapeFileNewData );
      return RunnableContextHelper.execute( getContainer(), true, false, operation );
    }
    catch( final CoreException e )
    {
      return e.getStatus();
    }
  }
}