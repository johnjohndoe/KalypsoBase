/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.loader;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.contribs.java.net.UrlResolver;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.core.i18n.Messages;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.loader.LoaderException;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.serialize.GmlSerializeException;
import org.kalypso.ogc.gml.serialize.ShapeSerializer;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.visitors.TransformVisitor;

/**
 * @author Belger
 */
public class ShapeLoader extends WorkspaceLoader
{
  private final UrlResolver m_urlResolver = new UrlResolver();

  /**
   * @see org.kalypso.loader.ILoader#getDescription()
   */
  @Override
  public String getDescription( )
  {
    return "ESRI Shape"; //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.loader.AbstractLoader#loadIntern(java.lang.String, java.net.URL,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  protected CommandableWorkspace loadIntern( final IPoolableObjectType key, final IProgressMonitor monitor ) throws LoaderException
  {
    final String location = key.getLocation();
    final URL context = key.getContext();

    /* Files that get deleted at the end of this operation. */
    final List<File> filesToDelete = new ArrayList<File>();

    try
    {
      final String sourceSrs = parseSrs( location );
      final String shpSource = parseSource( location );

      final String taskMsg = Messages.getString( "org.kalypso.ogc.gml.loader.GmlLoader.1", shpSource ); //$NON-NLS-1$
      final SubMonitor moni = SubMonitor.convert( monitor, taskMsg, 100 );
      ProgressUtilities.worked( moni, 1 );

      final URL sourceURL = UrlResolverSingleton.resolveUrl( context, shpSource );

      final URL shpURL = new URL( sourceURL.toExternalForm() + ".shp" );//$NON-NLS-1$
      final URL dbfURL = new URL( sourceURL.toExternalForm() + ".dbf" ); //$NON-NLS-1$
      final URL shxURL = new URL( sourceURL.toExternalForm() + ".shx" ); //$NON-NLS-1$
      final URL prjURL = new URL( sourceURL.toExternalForm() + ".prj" ); //$NON-NLS-1$

      // leider können Shapes nicht aus URL geladen werden -> protocoll checken
      final File sourceFile;
      final IPath resource = ResourceUtilities.findPathFromURL( sourceURL );
      if( resource != null )
        sourceFile = ResourceUtilities.makeFileFromPath( resource );
      else if( sourceURL.getProtocol().startsWith( "file" ) ) //$NON-NLS-1$
        sourceFile = new File( sourceURL.getPath() );
      else
      {
        moni.subTask( Messages.getString( "org.kalypso.ogc.gml.loader.ShapeLoader.0" ) ); //$NON-NLS-1$

        /* If everything else fails, we copy the resources to local files */
        sourceFile = File.createTempFile( "shapeLocalizedFiled", "" ); //$NON-NLS-1$ //$NON-NLS-2$
        final String sourceFilePath = sourceFile.getAbsolutePath();

        final File shpFile = new File( sourceFilePath + ".shp" ); //$NON-NLS-1$
        final File dbfFile = new File( sourceFilePath + ".dbf" ); //$NON-NLS-1$
        final File shxFile = new File( sourceFilePath + ".shx" ); //$NON-NLS-1$

        filesToDelete.add( sourceFile );
        filesToDelete.add( shpFile );
        filesToDelete.add( dbfFile );
        filesToDelete.add( shxFile );

        FileUtils.copyURLToFile( shpURL, shpFile );
        FileUtils.copyURLToFile( dbfURL, dbfFile );
        FileUtils.copyURLToFile( shxURL, shxFile );
      }
      ProgressUtilities.worked( moni, 9 );

      if( sourceFile == null )
        throw new LoaderException( Messages.getString( "org.kalypso.ogc.gml.loader.ShapeLoader.10" ) + shpSource ); //$NON-NLS-1$

      /* Loading Shape */
      final String sourceCrs = ShapeSerializer.loadCrs( prjURL, sourceSrs );
      final String targetCRS = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

      // FIXME: we also need to specify the shape charset
      final GMLWorkspace gmlWorkspace = ShapeSerializer.deserialize( sourceFile.getAbsolutePath(), sourceCrs, moni.newChild( 70, SubMonitor.SUPPRESS_BEGINTASK ) );
      final CommandableWorkspace workspace = new CommandableWorkspace( gmlWorkspace );

      try
      {
        /* Transforming to Kalypso CRS */
        moni.subTask( Messages.getString( "org.kalypso.ogc.gml.loader.GmlLoader.3" ) ); //$NON-NLS-1$
        ProgressUtilities.worked( moni, 1 ); // check cancel
        workspace.accept( new TransformVisitor( targetCRS ), workspace.getRootFeature(), FeatureVisitor.DEPTH_INFINITE );
        ProgressUtilities.worked( moni, 18 ); // check cancel
      }
      catch( final CoreException ce )
      {
        if( !ce.getStatus().matches( IStatus.CANCEL ) )
          ce.printStackTrace();
      }
      catch( final Throwable e1 )
      {
        e1.printStackTrace();
      }

      return workspace;
    }
    catch( final CoreException ce )
    {
      if( !ce.getStatus().matches( IStatus.CANCEL ) )
        ce.printStackTrace();
      throw new LoaderException( ce );
    }
    catch( final GmlSerializeException ge )
    {
      final Throwable cause = ge.getCause();
      if( cause instanceof CoreException )
      {
        final IStatus status = ((CoreException) cause).getStatus();
        if( status.matches( IStatus.CANCEL ) )
          throw new LoaderException( cause );
      }

      ge.printStackTrace();
      throw new LoaderException( ge );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      throw new LoaderException( e );
    }
    finally
    {
      for( final File file : filesToDelete )
        file.delete();
    }
  }

  private String parseSource( final String location )
  {
    // eventuelle vorhandenen Information zum CRS abschneiden
    final int index = location.indexOf( '#' );

    final String shpSource;
    if( index != -1 && index + 1 < location.length() )
      shpSource = location.substring( 0, index );
    else
      shpSource = location;

    return shpSource;
  }

  private String parseSrs( final String location )
  {
    // eventuelle vorhandenen Information zum CRS abschneiden
    final int index = location.indexOf( '#' );

    final String sourceSrs;
    if( index != -1 && index + 1 < location.length() )
      sourceSrs = location.substring( index + 1 );
    else
      sourceSrs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();

    return sourceSrs;
  }

  @Override
  public void save( final IPoolableObjectType key, final IProgressMonitor monitor, final Object data ) throws LoaderException
  {
    final String source = key.getLocation();
    final URL context = key.getContext();

    try
    {
      // TODO: Save in the original coordinate system,
      // not the one from other sources (e.g map, kalypso coordinate system)
      final GMLWorkspace workspace = (GMLWorkspace) data;
      final URL shpURL = m_urlResolver.resolveURL( context, source.split( "#" )[0] ); //$NON-NLS-1$

      final IFile file = ResourceUtilities.findFileFromURL( shpURL );
      if( file != null )
      {
        // FIXME: Must be saved in the source coordinate system (map or prj), not in the kalypso coordinate system.
        ShapeSerializer.serialize( workspace, file.getLocation().toFile().getAbsolutePath(), null );
      }
      else
        throw new LoaderException( Messages.getString( "org.kalypso.ogc.gml.loader.ShapeLoader.12" ) + shpURL ); //$NON-NLS-1$
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
      throw new LoaderException( Messages.getString( "org.kalypso.ogc.gml.loader.ShapeLoader.13" ) + source + "\n" + e.getLocalizedMessage(), e ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    catch( final Throwable e )
    {
      e.printStackTrace();
      throw new LoaderException( Messages.getString( "org.kalypso.ogc.gml.loader.ShapeLoader.15" ) + e.getLocalizedMessage(), e ); //$NON-NLS-1$
    }
  }

  /**
   * @see org.kalypso.loader.AbstractLoader#getResourcesInternal(org.kalypso.core.util.pool.IPoolableObjectType)
   */
  @Override
  public IResource[] getResourcesInternal( final IPoolableObjectType key ) throws MalformedURLException
  {
    final String location = key.getLocation();
    final URL context = key.getContext();
    final String shpSource = parseSource( location );
    final URL sourceURL = UrlResolverSingleton.resolveUrl( context, shpSource );

    IResource shpResource = null;
    IResource dbfResource = null;
    IResource shxResource = null;
    IResource prjResource = null;
    final IPath resource = ResourceUtilities.findPathFromURL( sourceURL );

    if( resource != null )
    {
      final URL shpURL = new URL( sourceURL.toExternalForm() + ".shp" );//$NON-NLS-1$
      final URL dbfURL = new URL( sourceURL.toExternalForm() + ".dbf" ); //$NON-NLS-1$
      final URL shxURL = new URL( sourceURL.toExternalForm() + ".shx" ); //$NON-NLS-1$
      final URL prjURL = new URL( sourceURL.toExternalForm() + ".prj" ); //$NON-NLS-1$

      shpResource = ResourceUtilities.findFileFromURL( shpURL );
      dbfResource = ResourceUtilities.findFileFromURL( dbfURL );
      shxResource = ResourceUtilities.findFileFromURL( shxURL );
      prjResource = ResourceUtilities.findFileFromURL( prjURL );

      return new IResource[] { shpResource, dbfResource, shxResource, prjResource };
    }

    return new IResource[] {};
  }
}