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
package org.kalypso.ui.wizard.shape;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.i18n.ResourceBundleUtils;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.core.runtime.PathUtils;
import org.kalypso.contribs.java.i18n.I18NBundle;
import org.kalypso.contribs.java.net.IUrlResolver2;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.catalog.CatalogSLD;
import org.kalypso.core.catalog.CatalogSLDUtils;
import org.kalypso.core.status.StatusDialog;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.shape.FileMode;
import org.kalypso.shape.ShapeFile;
import org.kalypso.shape.ShapeFileUtils;
import org.kalypso.shape.ShapeType;
import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.shape.deegree.GenericShapeDataFactory;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.action.AddThemeCommand;
import org.kalypso.ui.addlayer.internal.util.AddLayerUtils;
import org.kalypso.ui.i18n.Messages;
import org.kalypso.ui.wizard.AbstractDataImportWizard;
import org.kalypso.ui.wizard.shape.ImportShapeFileData.StyleImport;
import org.kalypsodeegree.xml.Marshallable;
import org.kalypsodeegree.xml.XMLParsingException;
import org.kalypsodeegree_impl.gml.binding.shape.ShapeCollection;
import org.kalypsodeegree_impl.graphics.sld.SLDFactory;

import com.google.common.base.Charsets;

/**
 * Imports an external file into the workspace and adds it to a map modell.
 *
 * @author Gernot Belger
 */
public class ImportShapeOperation
{
  private final ImportShapeFileData m_data;

  private final IPath m_shapePath;

  private final IPath m_sldPath;

  public ImportShapeOperation( final ImportShapeFileData data )
  {
    m_data = data;

    m_shapePath = m_data.getShapeFile().getPath().removeFileExtension();
    m_sldPath = m_shapePath.removeFileExtension().addFileExtension( ImportShapeFileData.EXTENSIONS_SLD );
  }

  public boolean checkPrecondition( final Shell shell, final String windowTitle )
  {
    final StyleImport styleImportType = m_data.getStyleImportType();

    final IFile sldFile = PathUtils.toFile( m_sldPath );
    if( sldFile.exists() && styleImportType == StyleImport.generateDefault )
    {
      final String message = String.format( Messages.getString("ImportShapeOperation_0"), sldFile.getName() ); //$NON-NLS-1$
      if( !MessageDialog.openConfirm( shell, windowTitle, message ) )
        return false;
    }

    return true;
  }

  public ICommand createCommand( final IKalypsoLayerModell mapModell ) throws CoreException
  {
    // Add Layer to mapModell
    final IPath mapPath = AddLayerUtils.getPathForMap( mapModell );

    /* Add new theme to map */
    final String relativeShapePath = AddLayerUtils.makeRelativeOrProjectRelative( mapPath, m_shapePath );

    final String themeName = FileUtilities.nameWithoutExtension( m_shapePath.lastSegment() );
    final String fileName = relativeShapePath + '#' + m_data.getSrs(); //$NON-NLS-1$

    final int insertionIndex = m_data.getInsertionIndex();

    final String featurePath = ShapeCollection.MEMBER_FEATURE_LOCAL;
    final AddThemeCommand command = new AddThemeCommand( mapModell, themeName, "shape", featurePath, fileName, insertionIndex ); //$NON-NLS-1$

    try
    {
      /* Add style according to choice */
      if( !addStyleCommand( mapPath, m_shapePath, command ) )
        return null;

      return command;
    }
    catch( final Exception e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoAddLayerPlugin.getId(), Messages.getString("ImportShapeOperation_1"), e ); //$NON-NLS-1$
      throw new CoreException( status );
    }
  }

  private boolean addStyleCommand( final IPath mapPath, final IPath shapePath, final AddThemeCommand command ) throws CoreException, IOException, DBaseException, XMLParsingException
  {
    final StyleImport styleImportType = m_data.getStyleImportType();
    switch( styleImportType )
    {
      case useDefault:
        /* Nothing to do, the them will add an default style automatically */
        return true;

      case generateDefault:
        return generateDefaultStyle( mapPath, shapePath, command );

      case selectExisting:
        /* Add reference to existing style file */
        final IPath stylePath = m_data.getStyleFile().getPath();
        final String relativeStylePath = AddLayerUtils.makeRelativeOrProjectRelative( mapPath, stylePath );
        final String styleName = m_data.getStyleName();
        command.addStyle( styleName, relativeStylePath );
        return true;
    }

    return true;
  }

  /**
   * Copies a registered default style into the workspace under the name of the shape file.
   */
  private boolean generateDefaultStyle( final IPath mapPath, final IPath shapePath, final AddThemeCommand command ) throws CoreException, IOException, DBaseException, XMLParsingException
  {
    ShapeFile shpFile = null;
    try
    {
      final IPath sldPath = shapePath.removeFileExtension().addFileExtension( ImportShapeFileData.EXTENSIONS_SLD );

      final IFile shapeFile = PathUtils.toFile( shapePath );
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

      /* load and replace translation strings with their content */
      final Object sld = SLDFactory.readSLD( ftsURL );
      final I18NBundle translation = readTranslationBundle( ftsURL );
      final SLDTranslationResolver sldResolver = new SLDTranslationResolver( translation );
      sldResolver.resolve( sld );

      /* marshall as xml */
      final String charset = Charsets.UTF_8.name();
      final IFile sldFile = PathUtils.toFile( m_sldPath );
      final String sldXml = SLDFactory.marshallObject( (Marshallable) sld, charset );

      /* Save to file */
      final File sldLocalFile = sldFile.getLocation().toFile();
      FileUtils.writeStringToFile( sldLocalFile, sldXml, charset );
      sldFile.refreshLocal( IResource.DEPTH_ZERO, null );

      /* add style to map */
      final String relativeSldPath = AddLayerUtils.makeRelativeOrProjectRelative( mapPath, sldPath );
      command.addStyle( null, relativeSldPath );
      return true;
    }
    finally
    {
      ShapeFileUtils.closeQuiet( shpFile );
    }
  }

  private I18NBundle readTranslationBundle( final URL ftsURL )
  {
    return new I18NBundle( ResourceBundleUtils.loadResourceBundle( ftsURL ) );
  }

  public boolean executeOnWizard( final AbstractDataImportWizard wizard )
  {
    final Shell shell = wizard.getShell();
    final String title = wizard.getWindowTitle();

    final IKalypsoLayerModell mapModell = wizard.getMapModel();

    if( !checkPrecondition( shell, title ) )
      return false;

    try
    {
      final ICommand command = createCommand( mapModell );
      if( command == null )
        return false;

      wizard.postCommand( command, null );

      return true;
    }
    catch( final CoreException e )
    {
      final IStatus status = e.getStatus();
      KalypsoAddLayerPlugin.getDefault().getLog().log( status );
      StatusDialog.open( shell, status, title );
      return false;
    }
  }
}