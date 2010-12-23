package org.kalypso.ui.wizard.raster;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.ogc.gml.IKalypsoLayerModell;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.action.AddThemeCommand;
import org.kalypsodeegree_impl.gml.binding.commons.CoverageCollection;

/**
 * @author Gernot Belger
 */
public final class ImportRasterOperation implements ICoreRunnableWithProgress
{
  private final String m_styleName;

  private final ICommandTarget m_commandTarget;

  private final IKalypsoLayerModell m_mapModell;

  private final IFile m_coverageFile;

  private final IFile m_mapFile;

  private final IFile m_styleFile;

  public ImportRasterOperation( final IFile coverageFile, final IFile styleFile, final String styleName, final ICommandTarget commandTarget, final IKalypsoLayerModell mapModell )
  {
    m_coverageFile = coverageFile;
    m_styleFile = styleFile;
    m_styleName = styleName;

    final URL context = mapModell.getContext();
    m_mapFile = ResourceUtilities.findFileFromURL( context );

    m_commandTarget = commandTarget;
    m_mapModell = mapModell;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException, CoreException
  {
    try
    {
      monitor.beginTask( "Create Grid Theme", 100 );

      createCoverageFile( new SubProgressMonitor( monitor, 10 ) );

      // TODO: analyse gml in order to set featurePath
      // - is root feature a grid
      // - find all properties pointing to a grid

      createStyleFile( new SubProgressMonitor( monitor, 10 ) );

      final String relativeHrefGml = getMapRelativeHref( m_coverageFile );
      final String relativeHrefSld = getMapRelativeHref( m_styleFile );

      final String themeName = FilenameUtils.removeExtension( m_coverageFile.getName() );
      final String type = "gml"; //$NON-NLS-1$
      final String featurePath = "coverageMember"; //$NON-NLS-1$
      final AddThemeCommand command = new AddThemeCommand( m_mapModell, themeName, type, featurePath, relativeHrefGml );

      command.addStyle( m_styleName, relativeHrefSld );
      m_commandTarget.postCommand( command, null );
      return Status.OK_STATUS;
    }
    catch( final CoreException e )
    {
      throw e;
    }
    catch( final Throwable t )
    {
      throw new InvocationTargetException( t );
    }
    finally
    {
      monitor.done();
    }
  }

  private void createStyleFile( final IProgressMonitor monitor ) throws CoreException
  {
    if( m_styleFile.exists() )
    {
      monitor.done();
      return;
    }

    try
    {
      final URL templateSLD = getClass().getResource( "resources/emptyRasterSymbolizer.sld" );
      final File file = m_styleFile.getLocation().toFile();
      FileUtils.copyURLToFile( templateSLD, file );
      m_styleFile.refreshLocal( IResource.DEPTH_INFINITE, new NullProgressMonitor() );
    }
    catch( final CoreException e )
    {
      throw e;
    }
    catch( final IOException e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoAddLayerPlugin.getId(), "Failed to create sld file", e );
      throw new CoreException( status );
    }
  }

  private String getMapRelativeHref( final IFile file )
  {
    final IPath relativPath = ResourceUtilities.makeRelativ( m_mapFile, file );
    return relativPath.toString();
  }

  private void createCoverageFile( final IProgressMonitor monitor ) throws CoreException
  {
    if( m_coverageFile.exists() )
    {
      monitor.done();
      return;
    }

    try
    {
      GmlSerializer.createGmlFile( CoverageCollection.QNAME, null, m_coverageFile, monitor, null );
    }
    catch( final GMLSchemaException e )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoAddLayerPlugin.getId(), "Failed to create data file", e );
      throw new CoreException( status );
    }
  }
}