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
import java.util.ResourceBundle;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.tools.ant.filters.StringInputStream;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.i18n.ResourceBundleUtils;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.java.net.IUrlResolver2;
import org.kalypso.contribs.java.net.UrlResolver;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.catalog.CatalogSLD;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.i18n.Messages;
import org.kalypso.loader.AbstractLoader;
import org.kalypso.loader.ISaveAsLoader;
import org.kalypso.loader.LoaderException;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.xml.Marshallable;
import org.kalypsodeegree.xml.XMLParsingException;
import org.kalypsodeegree_impl.graphics.sld.SLDFactory;

/**
 * @author schlienger
 */
public class SldLoader extends AbstractLoader implements ISaveAsLoader
{
  private final UrlResolver m_urlResolver = new UrlResolver();

  private ResourceBundle m_resourceBundle;

  private IResource[] m_resources = new IResource[] {};

  /**
   * @see org.kalypso.loader.ILoader#getDescription()
   */
  @Override
  public String getDescription( )
  {
    return "OGC SLD"; //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.loader.ILoader#load(org.kalypso.core.util.pool.IPoolableObjectType,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public Object load( final IPoolableObjectType key, final IProgressMonitor monitor ) throws LoaderException
  {
    final String source = key.getLocation();
    final URL context = key.getContext();

    try
    {
      monitor.beginTask( Messages.getString( "org.kalypso.ogc.gml.loader.SldLoader.1" ), 1000 ); //$NON-NLS-1$

      if( source.startsWith( "urn" ) ) //$NON-NLS-1$
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
    final URL userURL = findUserUrl( context, source );
    if( userURL != null )
    {
      final File userFile = FileUtils.toFile( userURL );
      if( userFile != null && userFile.exists() )
      {
        // sld from user location but resources still from catalog location
        return loadFromUrl( userURL, catalogURL );
      }

      /* Fall through, loading from catalog */
    }

    // Try to load the fts from the catalog
    final FeatureTypeStyle fts = catalog.getValue( resolver, source, source );
    if( fts == null )
      throw new LoaderException( String.format( "Failed to resolve urn %s", source ) ); //$NON-NLS-1$

    // get the url, should be the same used for loading the sld
    loadResourceBundle( catalogURL );

    return fts;
  }

  private URL findUserUrl( final URL context, final String source )
  {
    // TODO Auto-generated method stub
    return null;
  }

  private Object loadFromUrl( final URL sldLocation, final URL resourceLocation ) throws IOException, XMLParsingException
  {
    final Object element = SLDFactory.readSLD( sldLocation );
    setResources( sldLocation );
    loadResourceBundle( resourceLocation );
    return element;
  }

  private void setResources( final URL sldLocation )
  {
    final IResource resource = ResourceUtilities.findFileFromURL( sldLocation );
    if( resource == null )
      m_resources = new IResource[] {};
    else
      m_resources = new IResource[] { resource };
  }

  @Override
  public void save( final IPoolableObjectType key, final IProgressMonitor monitor, final Object data ) throws LoaderException
  {
    final String source = key.getLocation();
    final URL context = key.getContext();

    if( data instanceof Marshallable )
    {
      IFile sldFile = null;
      try
      {
        final URL styleURL = m_urlResolver.resolveURL( context, source );

        sldFile = ResourceUtilities.findFileFromURL( styleURL );
        if( sldFile == null )
          throw new LoaderException( Messages.getString( "org.kalypso.ogc.gml.loader.SldLoader.6" ) + styleURL ); //$NON-NLS-1$

        final String charset = sldFile.getCharset();
        final String sldXMLwithHeader = marshallObject( data, charset );

        sldFile.setContents( new StringInputStream( sldXMLwithHeader, charset ), true, false, monitor );
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

  @Override
  public IResource[] getResourcesInternal( final IPoolableObjectType key )
  {
    return m_resources;
  }

  @Override
  public void release( final Object object )
  {
  }

  private void loadResourceBundle( final URL resourceLocation )
  {
    m_resourceBundle = ResourceBundleUtils.loadResourceBundle( resourceLocation );
  }

  /**
   * Try to find a resource bundle for the styled layer descriptor.
   */
  public ResourceBundle getResourceBundle( )
  {
    return m_resourceBundle;
  }

  @Override
  public void saveAs( final IPoolableObjectType key, final IProgressMonitor monitor, final Object object ) throws LoaderException
  {
    try
    {
      final String location = key.getLocation();

      final String monitorMessage = String.format( "Saving '%s'", location );
      monitor.beginTask( monitorMessage, IProgressMonitor.UNKNOWN );

      /* create filename */
      final URL userLocation = findUserUrl( key.getContext(), location );
      final File userFile = FileUtils.toFile( userLocation );

      /* Check if already exists -> error */
      if( userFile == null || userFile.exists() )
      {
        /* Should never happen */
        // TODO: we need an extra method in the interface that checks, if we already have savedAs
        throw new LoaderException( "User version of this file already exists" ); //$NON-NLS-1$
      }

      /* Really save to this location */
      final String sldXMLwithHeader = marshallObject( object, CharEncoding.UTF_8 );
      FileUtils.writeStringToFile( userFile, sldXMLwithHeader );

      /* Just for formal reasons, should already be empty */
      setResources( null );
    }
    catch( final IOException e )
    {
      e.printStackTrace();
      final IStatus status = new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), "Failed to save SLD", e );
      throw new LoaderException( status );
    }
    finally
    {
      monitor.done();
    }
  }

  protected String marshallObject( final Object object, final String charset ) throws LoaderException
  {
    if( !(object instanceof Marshallable) )
      throw new LoaderException( "Wrong kind of object" ); //$NON-NLS-1$

    final Marshallable marshallable = (Marshallable) object;
    final String sldXML = marshallable.exportAsXML();
    final String sldXMLwithHeader = "<?xml version=\"1.0\" encoding=\"" + charset + "\"?>" + sldXML; //$NON-NLS-1$ //$NON-NLS-2$
    return sldXMLwithHeader;
  }
}