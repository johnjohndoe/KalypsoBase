/*--------------- Kalypso-Header ------------------------------------------

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

 --------------------------------------------------------------------------*/

package org.kalypso.simulation.core.ant;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.zml.obslink.TimeseriesLinkType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureVisitor;

/**
 * @author Gernot Belger
 */
public class MapFeature2ZmlMetaVisitor implements FeatureVisitor
{
  private final Logger m_logger = Logger.getLogger( getClass().getName() );

  private final URL m_context;

  private final String m_zmlLink;

  private final Feature2ZmlMapping[] m_mappings;

  public MapFeature2ZmlMetaVisitor( final URL context, final String zmlLink, final Feature2ZmlMapping[] mappings )
  {
    m_context = context;
    m_zmlLink = zmlLink;
    m_mappings = mappings;
  }

  /**
   * @see org.kalypsodeegree.model.feature.FeatureVisitor#visit(org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public final boolean visit( final Feature f )
  {
    final URL observationLocation = getObservationLocation( f );
    if( observationLocation == null )
      return true;

    final IObservation observation = loadObservation( observationLocation );
    if( observation == null )
      return true;

    for( final Feature2ZmlMapping mapping : m_mappings )
    {
      final String value = mapping.resolve( f );
      mapping.apply( observation, value );
    }

    saveObservation( observation, observationLocation );

    return true;
  }

  private URL getObservationLocation( final Feature f )
  {
    // load observation
    if( f.getFeatureType().getProperty( m_zmlLink ) == null )
    {
      m_logger.warning( String.format( "No feature property with name '%s'", m_zmlLink ) ); //$NON-NLS-1$
      return null;
    }

    final Object property = f.getProperty( m_zmlLink );
    if( property == null )
      return null;

    if( !(property instanceof TimeseriesLinkType) )
    {
      m_logger.warning( String.format( "Feature property is not a timeseries link: '%s'", m_zmlLink ) ); //$NON-NLS-1$
      return null;
    }

    final TimeseriesLinkType link = (TimeseriesLinkType) property;
    final String href = link.getHref();
    try
    {
      return UrlResolverSingleton.getDefault().resolveURL( m_context, href );
    }
    catch( final MalformedURLException e )
    {
      e.printStackTrace();
      m_logger.log( Level.SEVERE, String.format( "Invalid link to observation: '%s'", href ), e ); //$NON-NLS-1$
      return null;
    }
  }

  private IObservation loadObservation( final URL observationLocation )
  {
    try
    {
      return ZmlFactory.parseXML( observationLocation, null );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
      m_logger.log( Level.SEVERE, String.format( "Unable to load observation: '%s'", observationLocation ), e ); //$NON-NLS-1$
      return null;
    }
  }

  private void saveObservation( final IObservation observation, final URL destination )
  {
    try
    {
      final IFile destFile = ResourceUtilities.findFileFromURL( destination );
      if( destFile == null )
        return;

      final File javaFile = destFile.getLocation().toFile();
      ZmlFactory.writeToFile( observation, javaFile );
      destFile.refreshLocal( IResource.DEPTH_ONE, new NullProgressMonitor() );
    }
    catch( final SensorException e )
    {
      e.printStackTrace();
      m_logger.log( Level.SEVERE, String.format( "Unable to save observation: '%s'", destination ), e ); //$NON-NLS-1$
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      m_logger.log( Level.SEVERE, String.format( "Failed to refresh workspace for file: '%s'", destination ), e ); //$NON-NLS-1$
    }
  }

}
