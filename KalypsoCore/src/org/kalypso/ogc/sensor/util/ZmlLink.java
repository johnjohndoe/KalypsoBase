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
package org.kalypso.ogc.sensor.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.core.util.pool.PoolableObjectType;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.request.IRequest;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.zml.obslink.TimeseriesLinkType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathException;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPathUtilities;

/**
 * @author Gernot Belger
 */
public class ZmlLink
{
  private final Feature m_feature;

  private final URL m_context;

  private final GMLXPath m_linkPath;

  /**
   * Constructor, using the features workspace-context to resolve timeseries links.
   */
  public ZmlLink( final Feature feature, final QName linkProperty )
  {
    this( feature, linkProperty, feature.getWorkspace().getContext() );
  }

  public ZmlLink( final Feature feature, final GMLXPath linkPath )
  {
    this( feature, linkPath, feature.getWorkspace().getContext() );
  }

  /**
   * Constructs an new {@link ZmlLink} with a different context.
   */
  public ZmlLink( final ZmlLink link, final URL context )
  {
    this( link.getFeature(), link.getPath(), context );
  }

  /**
   * Alternate constructor, using an external {@link URL} as context to resolve timeseries links.
   */
  public ZmlLink( final Feature feature, final QName linkProperty, final URL context )
  {
    this( feature, new GMLXPath( linkProperty ), context );
  }

  /**
   * Alternate constructor, using an external {@link URL} as context to resolve timeseries links.
   */
  public ZmlLink( final Feature feature, final GMLXPath linkPath, final URL context )
  {
    m_context = context;
    Assert.isNotNull( feature );

    m_feature = feature;
    m_linkPath = linkPath;
  }

  public GMLXPath getPath( )
  {
    return m_linkPath;
  }

  public IObservation loadObservation( ) throws SensorException
  {
    if( m_linkPath == null )
      return null;

    final URL locationURL = getExistingLocation();
    if( locationURL == null )
      return null;

    return ZmlFactory.parseXML( locationURL );
  }

  /**
   * Fetches the observation from the kalypso resource pool, using
   * {@link ResourcePool#getObject(org.kalypso.core.util.pool.IPoolableObjectType)}.
   *
   * @see ResourcePool#getObject(org.kalypso.core.util.pool.IPoolableObjectType.
   */
  public IObservation getObservationFromPool( )
  {
    try
    {
      final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
      final IPoolableObjectType key = getPoolableObjectType();

      return (IObservation) pool.getObject( key );
    }
    catch( final CoreException e )
    {
      // Ignored, we already check via isLinkSet etc. if this obs is valid
      return null;
    }
  }

  public IPoolableObjectType getPoolableObjectType( )
  {
    final TimeseriesLinkType timeseriesLink = getTimeseriesLink();
    if( timeseriesLink == null )
      return null;

    final String href = timeseriesLink.getHref();

    return new PoolableObjectType( "zml", href, m_context, false ); //$NON-NLS-1$
  }

  /**
   * Returns the linked resource as an eclipse {@link IFile}, if this is possible.
   */
  public IFile getFile( )
  {
    final URL targetURL = getLocation();
    if( targetURL == null )
      return null;

    return ResourceUtilities.findFileFromURL( targetURL );
  }

  /**
   * Returns the linked resource as an java file ({@link File}), if this is possible.
   */
  public File getJavaFile( )
  {
    final URL targetURL = getLocation();
    if( targetURL == null )
      return null;

    final File file = FileUtils.toFile( targetURL );
    if( file != null )
      return file;

    final IFile eclipseFile = ResourceUtilities.findFileFromURL( targetURL );
    if( eclipseFile != null )
    {
      final IPath location = eclipseFile.getLocation();
      if( location != null )
        return location.toFile();
    }

    return null;
  }

  public URL getLocation( )
  {
    try
    {
      final TimeseriesLinkType link = getTimeseriesLink();
      if( link == null )
        return null;

      final String href = link.getHref();
      return UrlResolverSingleton.resolveUrl( m_context, href );
    }
    catch( final MalformedURLException ignored )
    {
      return null;
    }
  }

  public URL getExistingLocation( )
  {
    // check if resource exists
    InputStream is = null;

    try
    {
      final URL zmlUrl = getLocation();
      if( zmlUrl == null )
        return null;

      is = zmlUrl.openStream();

      return zmlUrl;
    }
    catch( final IOException ignored )
    {
      return null;
    }
    finally
    {
      IOUtils.closeQuietly( is );
    }
  }

  /**
   * Returns the given property as {@link TimeseriesLinkType}.<br>
   * Links with a blank href are considered as not set.
   */
  public TimeseriesLinkType getTimeseriesLink( )
  {
    try
    {
      // final IFeatureType featureType = m_feature.getFeatureType();
      //
      // final Object linkPT = GMLXPathUtilities.query( m_linkPath, featureType );
      // if( !(linkPT instanceof IPropertyType) )
      // return null;

      if( m_linkPath == null )
        return null;

      final TimeseriesLinkType link = (TimeseriesLinkType) GMLXPathUtilities.query( m_linkPath, m_feature );
      if( link == null )
        return null;

      final String href = link.getHref();
      if( StringUtils.isBlank( href ) )
        return null;

      return link;
    }
    catch( final GMLXPathException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  public void saveObservation( final IObservation obs ) throws CoreException, SensorException
  {
    saveObservation( obs, null );
  }

  /**
   * Saves the given observation to the target resource of this link.
   */
  public void saveObservation( final IObservation obs, final IRequest request ) throws CoreException, SensorException
  {
    final File targetFile = getJavaFile();
    if( targetFile == null )
    {
      final IStatus status = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), Messages.getString("ZmlLink_0"), null ); //$NON-NLS-1$
      throw new CoreException( status );
    }

    targetFile.getParentFile().mkdirs();

    ZmlFactory.writeToFile( obs, targetFile, request );

    /* If the local file is mapped into the workspace, refresh it. */
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
    final IFile[] eclipseFiles = root.findFilesForLocationURI( targetFile.toURI() );
    for( final IFile workspaceFile : eclipseFiles )
      workspaceFile.getParent().refreshLocal( IResource.DEPTH_ONE, new NullProgressMonitor() );
  }

  /**
   * Returns <code>true</code>, iff the link is set and the linked resource really exists.
   */
  public boolean isLinkExisting( )
  {
    return getExistingLocation() != null;
  }

  /**
   * Returns <code>true</code>, if this property is set to a non-<code>null</code> link.
   */
  public boolean isLinkSet( )
  {
    return getTimeseriesLink() != null;
  }

  public String getHref( )
  {
    final TimeseriesLinkType timeseriesLink = getTimeseriesLink();
    if( timeseriesLink == null )
      return null;

    return timeseriesLink.getHref();
  }

  public Feature getFeature( )
  {
    return m_feature;
  }
}
