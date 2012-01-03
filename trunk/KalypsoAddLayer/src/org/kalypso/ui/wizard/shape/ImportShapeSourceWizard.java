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
package org.kalypso.ui.wizard.shape;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.namespace.QName;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.commons.java.net.UrlUtilities;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.PathUtils;
import org.kalypso.contribs.java.net.IUrlResolver2;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.catalog.CatalogSLD;
import org.kalypso.core.catalog.CatalogSLDUtils;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ogc.gml.serialize.ShapeSerializer;
import org.kalypso.shape.FileMode;
import org.kalypso.shape.ShapeFile;
import org.kalypso.shape.ShapeFileUtils;
import org.kalypso.shape.ShapeType;
import org.kalypso.shape.deegree.GenericShapeDataFactory;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.action.AddThemeCommand;
import org.kalypso.ui.i18n.Messages;
import org.kalypso.ui.wizard.IKalypsoDataImportWizard;
import org.kalypso.ui.wizard.shape.ImportShapeFileImportPage.StyleImport;

/**
 * @author Kuepferle
 */
public class ImportShapeSourceWizard extends Wizard implements IKalypsoDataImportWizard
{
  private ImportShapeFileImportPage m_page;

  private ICommandTarget m_outlineviewer;

  private IKalypsoLayerModell m_modell;

  private final String m_title;

  public ImportShapeSourceWizard( )
  {
    m_title = Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeSourceWizard.0" ); //$NON-NLS-1$
  }

  /**
   * @see org.eclipse.jface.wizard.IWizard#performFinish()
   */
  @Override
  public boolean performFinish( )
  {
    // Add Layer to mapModell
    final IKalypsoLayerModell mapModell = m_modell;
    final URL mapContext = m_modell.getContext();
    final IFile mapFile = ResourceUtilities.findFileFromURL( mapContext );

    /* Add new theme to map */
    final IFile shapeFile = m_page.getShapeFile();
    final String shapePath = m_page.getRelativeShapePath( mapFile );
    final String themeName = FileUtilities.nameWithoutExtension( shapeFile.getName() );
    final String fileName = shapePath + '#' + m_page.getCRS(); //$NON-NLS-1$

    final AddThemeCommand command = new AddThemeCommand( mapModell, themeName, "shape", ShapeSerializer.PROPERTY_FEATURE_MEMBER.getLocalPart(), fileName ); //$NON-NLS-1$

    /* Add style according to choice */
    if( !addStyleCommand( mapFile, shapeFile, command ) )
      return false;

    /* Do it */
    m_outlineviewer.postCommand( command, null );

    return true;
  }

  private boolean addStyleCommand( final IFile mapFile, final IFile shapeFile, final AddThemeCommand command )
  {
    final StyleImport styleImportType = m_page.getStyleImportType();
    switch( styleImportType )
    {
      case useDefault:
        /* Nothing to do, the them will add an default style automatically */
        return true;

      case generateDefault:
        return generateDefaultStyle( mapFile, shapeFile, command );

      case selectExisting:
        /* Add reference to existing style file */
        final String stylePath = m_page.getRelativeStylePath( mapFile );
        final String styleName = m_page.getStyleName();
        command.addStyle( styleName, stylePath );
        return true;
    }

    return true;
  }

  /**
   * Copies a registered default style into the workspace under the name of the shape file.
   */
  private boolean generateDefaultStyle( final IFile mapFile, final IFile shapeFile, final AddThemeCommand command )
  {
    ShapeFile shpFile = null;
    try
    {
      final IPath sldPath = shapeFile.getFullPath().removeFileExtension().addFileExtension( "sld" );
      final IFile sldFile = PathUtils.toFile( sldPath );
      if( sldFile.exists() )
      {
        final String message = String.format( "A style file with the same name (%s) already exists and will be overwritten.\nContinue?", sldFile.getName() );
        if( !MessageDialog.openConfirm( getShell(), m_title, message ) )
          return false;
      }

      final String shapeBasePath = shapeFile.getLocation().removeFileExtension().toOSString();
      shpFile = new ShapeFile( shapeBasePath, Charset.defaultCharset(), FileMode.READ );
      final ShapeType shapeType = shpFile.getShapeType();
      shpFile.close();
      final QName geometryType = GenericShapeDataFactory.findGeometryType( shapeType );

      final String ftsURN = CatalogSLDUtils.getDefaultStyleURN( geometryType );

      final CatalogSLD styleCatalog = KalypsoCorePlugin.getDefault().getSLDCatalog();

      final IUrlResolver2 resolver = new IUrlResolver2()
      {
        @Override
        public URL resolveURL( final String relativeOrAbsolute ) throws MalformedURLException
        {
          return new URL( relativeOrAbsolute );
        }
      };

      final URL ftsURL = styleCatalog.getURL( resolver, ftsURN, ftsURN );

      final String sldContent = UrlUtilities.toString( ftsURL, "UTF-8" );
      if( sldFile.exists() )
        sldFile.setContents( IOUtils.toInputStream( sldContent ), false, true, new NullProgressMonitor() );
      else
        sldFile.create( IOUtils.toInputStream( sldContent ), false, new NullProgressMonitor() );

      final String relativeSldPath = ImportShapeFileImportPage.makeRelativeOrProjectRelative( mapFile, sldPath );
      command.addStyle( null, relativeSldPath );
      return true;
    }
    catch( final CoreException e )
    {
      final IStatus status = e.getStatus();
      KalypsoAddLayerPlugin.getDefault().getLog().log( status );
      new StatusDialog( getShell(), status, m_title ).open();
      return false;
    }
    catch( final Exception e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoAddLayerPlugin.getId(), "Unexpected error while adding a style", e );
      KalypsoAddLayerPlugin.getDefault().getLog().log( status );
      new StatusDialog( getShell(), status, m_title ).open();
      return false;
    }
    finally
    {
      ShapeFileUtils.closeQuiet( shpFile );
    }

  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    // do nothing
  }

  @Override
  public void addPages( )
  {
    m_page = new ImportShapeFileImportPage( "shapefileimport", m_title, ImageProvider.IMAGE_KALYPSO_ICON_BIG ); //$NON-NLS-1$

    final IProject project = ResourceUtilities.findProjectFromURL( m_modell.getContext() );
    m_page.setProjectSelection( project );

    addPage( m_page );
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
   * @see org.kalypso.ui.wizard.IKalypsoDataImportWizard#setMapModel(org.kalypso.ogc.gml.IKalypsoLayerModell)
   */
  @Override
  public void setMapModel( final IKalypsoLayerModell modell )
  {
    m_modell = modell;
  }

}