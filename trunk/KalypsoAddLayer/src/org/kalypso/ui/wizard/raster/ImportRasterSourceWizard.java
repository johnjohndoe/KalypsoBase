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

package org.kalypso.ui.wizard.raster;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.action.AddThemeCommand;
import org.kalypso.ui.i18n.Messages;
import org.kalypso.ui.wizard.IKalypsoDataImportWizard;

public class ImportRasterSourceWizard extends Wizard implements IKalypsoDataImportWizard
{
  private ICommandTarget m_outlineviewer;

  private ImportRasterSourceWizardPage m_page;

  private IProject m_project;

  private IKalypsoLayerModell m_mapModel;

  public ImportRasterSourceWizard( )
  {
    super();
  }

  /**
   * @see org.kalypso.ui.wizard.IKalypsoDataImportWizard#setMapModel(org.kalypso.ogc.gml.IKalypsoLayerModell)
   */
  @Override
  public void setMapModel( final IKalypsoLayerModell modell )
  {
    m_mapModel = modell;
    m_project = m_mapModel.getProject();
  }

  @Override
  public void addPages( )
  {
    m_page = new ImportRasterSourceWizardPage( "Add RasterDataModel", Messages.getString( "org.kalypso.ui.wizard.raster.ImportRasterSourceWizard.0" ), ImageProvider.IMAGE_KALYPSO_ICON_BIG ); //$NON-NLS-1$ //$NON-NLS-2$
    if( m_project != null )
      m_page.setProject( m_project );
    addPage( m_page );

  }

  /**
   * @see org.eclipse.jface.wizard.Wizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    final IPath filePath = m_page.getFilePath();
    final boolean useDefaultStyle = m_page.checkDefaultStyle();
    final IKalypsoLayerModell mapModell = m_mapModel;
    final String source = getRelativeProjectPath( filePath );
    final String stylePath = useDefaultStyle ? null : getRelativeProjectPath( m_page.getStylePath() );
    final String styleName = useDefaultStyle ? null : m_page.getStyleName();

    final ICommandTarget outlineviewer = m_outlineviewer;
    final ICoreRunnableWithProgress operation = new ICoreRunnableWithProgress()
    {
      @Override
      public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException
      {
        try
        {
          // TODO: analyse gml in order to set featurePath
          // - is root feature a grid
          // - find all properties pointing to a grid

          if( mapModell == null )
            return StatusUtilities.createErrorStatus( Messages.getString( "org.kalypso.ui.wizard.raster.ImportRasterSourceWizard.1" ) ); //$NON-NLS-1$

          final String themeName = filePath.lastSegment();
          final String type = "gml"; //$NON-NLS-1$
          final String featurePath = ""; //$NON-NLS-1$
          final AddThemeCommand command = new AddThemeCommand( mapModell, themeName, type, featurePath, source );

          if( stylePath != null )
            command.addStyle( styleName, stylePath );
          outlineviewer.postCommand( command, null );
        }
        catch( final Throwable t )
        {
          throw new InvocationTargetException( t );
        }

        return Status.OK_STATUS;

      }
    };

    final IStatus status = RunnableContextHelper.execute( getContainer(), true, false, operation );
    KalypsoAddLayerPlugin.getDefault().getLog().log( status );
    ErrorDialog.openError( getShell(), getWindowTitle(), Messages.getString( "org.kalypso.ui.wizard.raster.ImportRasterSourceWizard.2" ), status ); //$NON-NLS-1$

    return status.isOK();
  }

  private String getRelativeProjectPath( final IPath path )
  {
    return "project:/" + path.removeFirstSegments( 1 ).toString(); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ui.wizard.data.IKalypsoDataImportWizard#setOutlineViewer(org.kalypso.ogc.gml.outline.GisMapOutlineViewer)
   */
  @Override
  public void setCommandTarget( final ICommandTarget commandTarget )
  {
    m_outlineviewer = commandTarget;
  }

  /**
   * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench,
   *      org.eclipse.jface.viewers.IStructuredSelection)
   */
  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    // nothing
  }
}