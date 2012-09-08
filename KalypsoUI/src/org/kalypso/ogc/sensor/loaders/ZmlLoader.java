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
package org.kalypso.ogc.sensor.loaders;

import java.net.MalformedURLException;
import java.net.URL;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.java.net.UrlResolver;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.core.util.pool.KeyInfo;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.i18n.Messages;
import org.kalypso.loader.AbstractLoader;
import org.kalypso.loader.LoaderException;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.event.IObservationListener;
import org.kalypso.ogc.sensor.event.ObservationChangeType;
import org.kalypso.ogc.sensor.zml.ZmlFactory;

/**
 * A specific loader for ZML-Files. Loads <code>ZmlObservation</code> objects.
 *
 * @author schlienger
 */
public class ZmlLoader extends AbstractLoader
{
  private final UrlResolver m_urlResolver = new UrlResolver();

  private final IObservationListener m_observationListener = new IObservationListener()
  {
    @Override
    public void observationChanged( final IObservation obs, final Object eventSource, final ObservationChangeType type )
    {
      handleObservationChanged( obs, type );
    }
  };

  @Override
  public Object load( final IPoolableObjectType key, final IProgressMonitor monitor ) throws LoaderException
  {
    final String source = key.getLocation();
    final URL context = key.getContext();

    try
    {
      final URL url = m_urlResolver.resolveURL( context, source );

      monitor.beginTask( Messages.getString( "org.kalypso.ogc.sensor.loaders.ZmlLoader.0" ) + url, IProgressMonitor.UNKNOWN ); //$NON-NLS-1$

      final IObservation observation = ZmlFactory.parseXML( url );
      observation.addListener( m_observationListener );

      return observation;
    }
    catch( final Exception e ) // generic exception caught for simplicity
    {
      // TODO wenn resource geloescht wurde, wird hier ein fehler geworfen
      throw new LoaderException( e );
    }
    finally
    {
      monitor.done();
    }
  }

  @Override
  public void save( final IPoolableObjectType key, final IProgressMonitor monitor, final Object data ) throws LoaderException
  {
    final String source = key.getLocation();
    final URL context = key.getContext();

    try
    {
      if( data == null )
        return;

      final URL url = m_urlResolver.resolveURL( context, source );

      monitor.beginTask( Messages.getString( "org.kalypso.ogc.sensor.loaders.ZmlLoader.1" ) + url, IProgressMonitor.UNKNOWN ); //$NON-NLS-1$

      final IFile file = ResourceUtilities.findFileFromURL( url );
      if( file == null )
        throw new IllegalArgumentException( Messages.getString( "org.kalypso.ogc.sensor.loaders.ZmlLoader.2" ) + url ); //$NON-NLS-1$

      ZmlFactory.writeToFile( (IObservation) data, file );
    }
    catch( final Throwable e ) // generic exception caught for simplicity
    {
      throw new LoaderException( e );
    }
    finally
    {
      monitor.done();
    }
  }

  /**
   * @see org.kalypso.loader.ILoader#getDescription()
   */
  @Override
  public String getDescription( )
  {
    return "ZML"; //$NON-NLS-1$
  }

  @Override
  public IResource[] getResourcesInternal( final IPoolableObjectType key ) throws MalformedURLException
  {
    final String source = key.getLocation();
    final URL context = key.getContext();
    final URL url = m_urlResolver.resolveURL( context, source );
    final IFile file = ResourceUtilities.findFileFromURL( url );
    return new IResource[] { file };
  }

  @Override
  public void release( final Object object )
  {
    final IObservation obs = (IObservation) object;
    obs.removeListener( m_observationListener );
  }

  protected void handleObservationChanged( final IObservation obs, final ObservationChangeType type )
  {
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    final KeyInfo info = pool.getInfo( obs );
    if( info != null )
      info.setDirty( true );
  }

}