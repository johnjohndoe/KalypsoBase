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
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.commons.performance.TimeLogger;
import org.kalypso.commons.resources.SetContentHelper;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.contribs.java.net.IUrlResolver;
import org.kalypso.contribs.java.net.UrlResolver;
import org.kalypso.core.KalypsoCoreDebug;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.core.util.pool.IModelAdaptor;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.core.util.pool.ModelAdapterExtension;
import org.kalypso.loader.LoaderException;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.visitors.TransformVisitor;

/**
 * L�dt einen GMLWorkspace aus einem GML
 *
 * @author Belger
 */
public class GmlLoader extends WorkspaceLoader
{
  private final IUrlResolver m_urlResolver = new UrlResolver();

  @Override
  protected CommandableWorkspace loadIntern( final IPoolableObjectType key, final IProgressMonitor monitor ) throws LoaderException
  {
    final String source = key.getLocation();
    final URL context = key.getContext();

    try
    {
      final URL gmlURL = m_urlResolver.resolveURL( context, source );
      final String taskMsg = Messages.getString( "org.kalypso.ogc.gml.loader.GmlLoader.1", source ); //$NON-NLS-1$
      final SubMonitor moni = SubMonitor.convert( monitor, taskMsg, 1000 );

      /* Initialise */
      final List<IStatus> resultList = new ArrayList<>();
      final TimeLogger perfLogger = KalypsoCoreDebug.PERF_SERIALIZE_GML.isEnabled() ? new TimeLogger( Messages.getString( "org.kalypso.ogc.gml.loader.GmlLoader.2" ) ) : null; //$NON-NLS-1$
      final PooledXLinkFeatureProviderFactory factory = new PooledXLinkFeatureProviderFactory();
      ProgressUtilities.worked( moni, 10 );

      /* Loading GML */
      final GMLWorkspace gmlWorkspace = GmlSerializer.createGMLWorkspace( gmlURL, factory, moni.newChild( 700, SubMonitor.SUPPRESS_BEGINTASK ) );
      ProgressUtilities.worked( moni, 0 );

      /* Transforming to Kalypso CRS */
      moni.subTask( Messages.getString( "org.kalypso.ogc.gml.loader.GmlLoader.3" ) ); //$NON-NLS-1$
      final String targetCRS = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
      final TransformVisitor transformVisitor = new TransformVisitor( targetCRS );
      gmlWorkspace.accept( transformVisitor, gmlWorkspace.getRootFeature(), FeatureVisitor.DEPTH_INFINITE );
      ProgressUtilities.worked( moni, 200 );

      if( perfLogger != null )
      {
        perfLogger.takeInterimTime();
        perfLogger.printCurrentTotal( Messages.getString( "org.kalypso.ogc.gml.loader.GmlLoader.4" ) ); //$NON-NLS-1$
      }

      /* Adapting if necessary */
      moni.subTask( Messages.getString( "org.kalypso.ogc.gml.loader.GmlLoader.0" ) ); //$NON-NLS-1$
      final IResource gmlFile = getResources( key )[0];
      final GMLWorkspace adaptedWorkspace = adaptWorkspace( key, moni.newChild( 80 ), resultList, gmlWorkspace, gmlFile );

      /* Hook for Loader stuff */
      final CommandableWorkspace workspace = new CommandableWorkspace( adaptedWorkspace );

      setStatus( StatusUtilities.createStatus( resultList, Messages.getString( "org.kalypso.ogc.gml.loader.GmlLoader.9", gmlURL.toExternalForm() ) ) ); //$NON-NLS-1$

      ProgressUtilities.done( moni );

      return workspace;
    }
    catch( final CoreException ce )
    {
      final IStatus status = ce.getStatus();
      setStatus( status );
      if( !status.matches( IStatus.CANCEL ) )
        ce.printStackTrace();

      throw new LoaderException( ce.getStatus() );
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();

      // REMARK: we no not pass the exception to the next level her (hence the printStackTrace)
      // in order to have a nicer error dialog later (avoids the same line aperaring twice in the details-panel)
      final LoaderException loaderException = new LoaderException( e.getLocalizedMessage() );
      setStatus( loaderException.getStatus() );
      throw loaderException;
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      // REMARK: we no not pass the exception to the next level her (hence the printStackTrace)
      // in order to have a nicer error dialog later (avoids the same line aperaring twice in the details-panel)
      final LoaderException loaderException = new LoaderException( e.getLocalizedMessage() );
      setStatus( loaderException.getStatus() );
      throw loaderException;
    }
  }

  private GMLWorkspace adaptWorkspace( final IPoolableObjectType key, final IProgressMonitor monitor, final List<IStatus> resultList, GMLWorkspace workspace, final IResource gmlFile ) throws LoaderException, CoreException
  {
    final Feature rootFeature = workspace.getRootFeature();
    final IModelAdaptor[] modelAdaptors = ModelAdapterExtension.getModelAdaptor( rootFeature.getFeatureType().getQName() );

    final SubMonitor moni = SubMonitor.convert( monitor, Messages.getString( "org.kalypso.ogc.gml.loader.GmlLoader.5" ), 5 + modelAdaptors.length ); //$NON-NLS-1$

    for( final IModelAdaptor modelAdaptor : modelAdaptors )
    {
      workspace = modelAdaptor.adapt( workspace, moni.newChild( 1 ) );
      resultList.add( modelAdaptor.getResult() );
      ProgressUtilities.worked( moni, 0 );
    }

    final MultiStatus adaptStatus = new MultiStatus( KalypsoCorePlugin.getID(), -1, resultList.toArray( new IStatus[resultList.size()] ), "", null ); //$NON-NLS-1$

    if( !adaptStatus.isOK() )
    {
      // some adaptation occurred, so directly save workspace
      // but create a backup (.bak) of old workspace
      moni.subTask( Messages.getString( "org.kalypso.ogc.gml.loader.GmlLoader.6" ) ); //$NON-NLS-1$

      if( gmlFile != null )
      {
        final IPath backupPath = gmlFile.getFullPath().addFileExtension( "bak" ); //$NON-NLS-1$
        backup( gmlFile, backupPath, moni.newChild( 2, SubMonitor.SUPPRESS_SETTASKNAME ), 0, resultList );
      }

      moni.subTask( Messages.getString( "org.kalypso.ogc.gml.loader.GmlLoader.8" ) ); //$NON-NLS-1$
      save( key, moni.newChild( 3, SubMonitor.SUPPRESS_SETTASKNAME ), workspace );
    }
    return workspace;
  }

  private void backup( final IResource gmlFile, final IPath backupPath, final IProgressMonitor monitor, final int count, final List<IStatus> resultList )
  {
    final IWorkspaceRoot root = gmlFile.getWorkspace().getRoot();
    final IPath currentTry = backupPath.addFileExtension( "" + count ); //$NON-NLS-1$
    final IResource backupResource = root.findMember( currentTry );
    if( backupResource != null )
    {
      backup( gmlFile, backupResource.getFullPath(), monitor, count + 1, resultList );
    }
    else
    {
      try
      {
        resultList.add( new Status( IStatus.INFO, KalypsoCorePlugin.getID(), Messages.getString( "org.kalypso.ogc.gml.loader.GmlLoader.13", gmlFile.getName(), currentTry.toOSString() ) ) ); //$NON-NLS-1$
        gmlFile.copy( currentTry, false, monitor );
      }
      catch( final CoreException e )
      {
        resultList.add( e.getStatus() );
      }
    }
  }

  @Override
  public String getDescription( )
  {
    return Messages.getString( "org.kalypso.ogc.gml.loader.GmlLoader.14" ); //$NON-NLS-1$
  }

  @Override
  public void save( final IPoolableObjectType key, final IProgressMonitor monitor, final Object data ) throws LoaderException
  {
    final String source = key.getLocation();
    final URL context = key.getContext();

    try
    {
      final GMLWorkspace workspace = (GMLWorkspace) data;

      final URL gmlURL = m_urlResolver.resolveURL( context, source );

      // ists im Workspace?
      final IFile file = ResourceUtilities.findFileFromURL( gmlURL );
      if( file != null )
        saveWorkspaceToFile( workspace, file );
      else if( gmlURL.getProtocol().equals( "file" ) ) //$NON-NLS-1$
      {
        // TODO: is this really ever used?
        final OutputStreamWriter w = new FileWriter( new File( gmlURL.getFile() ) );
        GmlSerializer.serializeWorkspace( w, workspace );
      }
      else
        throw new LoaderException( Messages.getString( "org.kalypso.ogc.gml.loader.GmlLoader.16" ) + gmlURL ); //$NON-NLS-1$
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();

      throw new LoaderException( Messages.getString( "org.kalypso.ogc.gml.loader.GmlLoader.17" ) + source + "\n" + e.getLocalizedMessage(), e ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    catch( final Throwable e )
    {
      e.printStackTrace();
      throw new LoaderException( Messages.getString( "org.kalypso.ogc.gml.loader.GmlLoader.19" ) + e.getLocalizedMessage(), e ); //$NON-NLS-1$
    }
  }

  private void saveWorkspaceToFile( final GMLWorkspace workspace, final IFile file ) throws CoreException
  {
    final SetContentHelper helper = new SetContentHelper()
    {
      @Override
      protected void write( final OutputStreamWriter writer ) throws Throwable
      {
        GmlSerializer.serializeWorkspace( writer, workspace );
      }
    };

    helper.setCompressed( GmlSerializer.isGZ( file.getName() ) );

    helper.setFileContents( file, false, true, new NullProgressMonitor() );
  }

  @Override
  protected IResource[] getResourcesInternal( final IPoolableObjectType key ) throws MalformedURLException
  {
    final String source = key.getLocation();
    final URL context = key.getContext();
    final URL gmlURL = m_urlResolver.resolveURL( context, source );
    final IResource gmlFile = ResourceUtilities.findFileFromURL( gmlURL );
    return new IResource[] { gmlFile };
  }
}