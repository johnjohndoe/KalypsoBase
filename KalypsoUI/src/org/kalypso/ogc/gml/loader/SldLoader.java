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
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.CharEncoding;
import org.apache.tools.ant.filters.StringInputStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.i18n.ResourceBundleUtils;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.java.i18n.I18NBundle;
import org.kalypso.contribs.java.net.IUrlResolver2;
import org.kalypso.contribs.java.net.UrlResolver;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.catalog.CatalogSLD;
import org.kalypso.core.catalog.CatalogUtilities;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.loader.AbstractLoader;
import org.kalypso.loader.ISaveUrnLoader;
import org.kalypso.loader.LoaderException;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.xml.Marshallable;
import org.kalypsodeegree.xml.XMLParsingException;
import org.kalypsodeegree_impl.graphics.sld.SLDFactory;

/**
 * @author schlienger
 */
public class SldLoader extends AbstractLoader implements ISaveUrnLoader
{
  private static final String USER_STORE_DIR = "userDefinedSld"; //$NON-NLS-1$

  private final UrlResolver m_urlResolver = new UrlResolver();

  private I18NBundle m_resourceBundle;

  @Override
  public String getDescription( )
  {
    return "OGC SLD"; //$NON-NLS-1$
  }

  @Override
  public Object load( final IPoolableObjectType key, final IProgressMonitor monitor ) throws LoaderException
  {
    final String source = key.getLocation();
    final URL context = key.getContext();

    try
    {
      monitor.beginTask( Messages.getString( "org.kalypso.ogc.gml.loader.SldLoader.1" ), 1000 ); //$NON-NLS-1$

      if( CatalogUtilities.isCatalogResource( source ) )
        return loadFromCatalog( context, source );

      /* Local url: sld and resources reside at the same location */
      final URL sldLocation = m_urlResolver.resolveURL( context, source );
      return loadFromUrl( sldLocation, sldLocation );
    }
    catch( final IllegalArgumentException e )
    {
      // This one may happen, because the platform-URL-connector does not always throw correct MalformedURLExceptions,
      // but throws this one, when opening the stream...
      e.printStackTrace();

      // REMARK: we no not pass the exception to the next level her (hence the printStackTrace)
      // in order to have a nicer error dialog later (avoids the same line aperaring twice in the details-panel)
      final LoaderException loaderException = new LoaderException( e.getLocalizedMessage() );
      setStatus( loaderException.getStatus() );
      throw loaderException;
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
    catch( final IOException e )
    {
      e.printStackTrace();

      // REMARK: we no not pass the exception to the next level her (hence the printStackTrace)
      // in order to have a nicer error dialog later (avoids the same line aperaring twice in the details-panel)
      final LoaderException loaderException = new LoaderException( e.getLocalizedMessage() );
      setStatus( loaderException.getStatus() );
      throw loaderException;
    }
    catch( final XMLParsingException e )
    {
      e.printStackTrace();

      // REMARK: we no not pass the exception to the next level her (hence the printStackTrace)
      // in order to have a nicer error dialog later (avoids the same line aperaring twice in the details-panel)
      final LoaderException loaderException = new LoaderException( e.getLocalizedMessage() );
      setStatus( loaderException.getStatus() );
      throw loaderException;
    }
    catch( final CoreException e )
    {
      final IStatus status = e.getStatus();
      setStatus( status );
      if( !status.matches( IStatus.CANCEL ) )
        e.printStackTrace();

      throw new LoaderException( e.getStatus() );
    }
    finally
    {
      monitor.done();
    }
  }

  private Object loadFromCatalog( final URL context, final String source ) throws LoaderException, IOException, XMLParsingException
  {
    final IUrlResolver2 resolver = new IUrlResolver2()
    {
      @Override
      public URL resolveURL( final String relativeOrAbsolute ) throws MalformedURLException
      {
        return UrlResolverSingleton.resolveUrl( context, relativeOrAbsolute );
      }
    };

    final CatalogSLD catalog = KalypsoCorePlugin.getDefault().getSLDCatalog();
    final URL catalogURL = catalog.getURL( resolver, source, source );

    /* Check for user saved style */
    final File userFile = findUserFile( source );
    if( userFile != null && userFile.exists() )
    {
      // sld from user location but resources still from catalog location
      return loadFromUrl( userFile.toURI().toURL(), catalogURL );
    }
    /* Fall through, loading from catalog */

    // Try to load the fts from the catalog
    final FeatureTypeStyle fts = catalog.getValue( resolver, source, source );
    if( fts == null )
      throw new LoaderException( String.format( "Failed to resolve urn %s", source ) ); //$NON-NLS-1$

    // get the url, should be the same used for loading the sld
    loadResourceBundle( catalogURL );

    return fts;
  }

  private File findUserFile( final String source )
  {
    if( !CatalogUtilities.isCatalogResource( source ) )
      return null;

    final IPath stateLocation = KalypsoGisPlugin.getDefault().getStateLocation();
    final IPath userStore = stateLocation.append( USER_STORE_DIR );
    final String sourceAsPath = source.replace( ':', IPath.SEPARATOR );
    final IPath userSource = userStore.append( sourceAsPath );
    final IPath userSld = userSource.addFileExtension( "sld" ); //$NON-NLS-1$
    return userSld.toFile();
  }

  private Object loadFromUrl( final URL sldLocation, final URL resourceLocation ) throws IOException, XMLParsingException
  {
    final Object element = SLDFactory.readSLD( sldLocation );
    loadResourceBundle( resourceLocation );
    return element;
  }

  @Override
  public void save( final IPoolableObjectType key, final IProgressMonitor monitor, final Object data ) throws LoaderException
  {
    final URL context = key.getContext();
    final String source = key.getLocation();

    if( data instanceof Marshallable )
    {
      try
      {
        /* create filename */
        final File userFile = findUserFile( source );

        /* Check if already exists -> error */
        if( userFile == null )
          saveToWorkspace( context, source, (Marshallable)data, monitor );
        else
          saveToUserStore( userFile, (Marshallable)data, monitor );
      }
      catch( final MalformedURLException e )
      {
        e.printStackTrace();

        throw new LoaderException( Messages.getString( "org.kalypso.ogc.gml.loader.SldLoader.7" ) + source + "\n" + e.getLocalizedMessage(), e ); //$NON-NLS-1$ //$NON-NLS-2$
      }
      catch( final Throwable e )
      {
        e.printStackTrace();
        throw new LoaderException( Messages.getString( "org.kalypso.ogc.gml.loader.SldLoader.9" ) + e.getLocalizedMessage(), e ); //$NON-NLS-1$
      }
    }
  }

  private void saveToWorkspace( final URL context, final String source, final Marshallable data, final IProgressMonitor monitor ) throws MalformedURLException, CoreException
  {
    final URL styleURL = m_urlResolver.resolveURL( context, source );

    final IFile sldFile = ResourceUtilities.findFileFromURL( styleURL );
    if( sldFile == null )
      throw new LoaderException( Messages.getString( "org.kalypso.ogc.gml.loader.SldLoader.6" ) + styleURL ); //$NON-NLS-1$

    final String charset = sldFile.getCharset();
    final String sldXMLwithHeader = SLDFactory.marshallObject( data, charset );

    sldFile.setContents( new StringInputStream( sldXMLwithHeader, charset ), true, false, monitor );
  }

  private void saveToUserStore( final File userFile, final Marshallable data, final IProgressMonitor monitor ) throws LoaderException
  {
    try
    {
      userFile.getParentFile().mkdirs();

      /* Really save to this location */
      final String sldXMLwithHeader = SLDFactory.marshallObject( data, CharEncoding.UTF_8 );
      FileUtils.writeStringToFile( userFile, sldXMLwithHeader, CharEncoding.UTF_8 );
    }
    catch( final IOException e )
    {
      e.printStackTrace();
      final IStatus status = new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), Messages.getString( "SldLoader.0" ), e ); //$NON-NLS-1$
      throw new LoaderException( status );
    }
    finally
    {
      monitor.done();
    }
  }

  @Override
  public IResource[] getResourcesInternal( final IPoolableObjectType key ) throws MalformedURLException
  {
    final String source = key.getLocation();
    final URL context = key.getContext();

    if( CatalogUtilities.isCatalogResource( source ) )
      return new IResource[0];

    /* Local url */
    final URL sldLocation = m_urlResolver.resolveURL( context, source );
    final IResource resource = ResourceUtilities.findFileFromURL( sldLocation );
    return new IResource[] { resource };
  }

  @Override
  public void release( final Object object )
  {
  }

  private void loadResourceBundle( final URL resourceLocation )
  {
    m_resourceBundle = new I18NBundle( ResourceBundleUtils.loadResourceBundle( resourceLocation ) );
  }

  /**
   * Try to find a resource bundle for the styled layer descriptor.
   */
  public I18NBundle getResourceBundle( )
  {
    return m_resourceBundle;
  }

  @Override
  public boolean isUserSaved( final IPoolableObjectType key )
  {
    final String location = key.getLocation();
    if( location == null )
      return false;

    if( !CatalogUtilities.isCatalogResource( location ) )
      return false;

    final File userFile = findUserFile( location );
    if( userFile == null )
      return false;

    return userFile.exists();
  }

  @Override
  public void resetUserStyle( final IPoolableObjectType key )
  {
    final String location = key.getLocation();
    final File userFile = findUserFile( location );
    if( userFile == null )
      return;

    if( userFile.exists() )
      userFile.delete();
  }
}