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
package org.kalypso.contribs.ogc31;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import javax.xml.bind.JAXBContext;

import ogc31.www.opengis.net.gml.ObjectFactory;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.jwsdp.JaxbUtilities;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * Just contains the singleton for the JAXBContext of the binding stuff used in this plugin
 * 
 * @author schlienger
 */
public final class KalypsoOGC31JAXBcontext
{
  public final static ObjectFactory GML3_FAC = new ObjectFactory();

  private static JAXBContext s_context = null;

  // create the context on all of these factories else the binding won't work
  public static Class< ? >[] s_contextClasses = new Class[] {//  
  ogc31.www.opengis.net.gml.ObjectFactory.class// 
      , ogc31.www.opengis.net.swe.ObjectFactory.class//
      , ogc31.www.isotc211.org.gmd.ObjectFactory.class//
      , au.csiro.seegrid.xml.st.ObjectFactory.class };

  public static synchronized JAXBContext getContext( )
  {
    if( s_context == null )
    {
      final KalypsoOGC31Plugin plugin = KalypsoOGC31Plugin.getDefault();
      final IPath stateLocation = plugin.getStateLocation();
      final IPath contextLocation = stateLocation.append( "context.ser" );
      final File contextFile = contextLocation.toFile();

      final XStream xstream = new XStream( new DomDriver() );

      if( contextFile.exists() )
      {
        // Context was already generated, read from file
        InputStream ois = null;
        try
        {
          ois = new BufferedInputStream( new FileInputStream( contextFile ) );
          s_context = (JAXBContext) xstream.fromXML( ois );
          ois.close();
        }
        catch( final Throwable e )
        {
          final IStatus status = StatusUtilities.createStatus( IStatus.WARNING, "Failed to retrieve ogc31-context from binary file", e );
          plugin.getLog().log( status );
          contextFile.delete();
        }
        finally
        {
          IOUtils.closeQuietly( ois );
        }
      }

      // Default and fall-back: generate really
      if( s_context == null )
        s_context = JaxbUtilities.createQuiet( s_contextClasses );

      if( false && !contextFile.exists() )
      {
        // Write context into file; next time the platform starts, the context will be read from here
        OutputStream oos = null;
        try
        {
          oos = new BufferedOutputStream( new FileOutputStream( contextFile ) );
          xstream.toXML( s_context, oos );
          oos.close();
        }
        catch( final Throwable e )
        {
          final IStatus status = StatusUtilities.createStatus( IStatus.WARNING, "Failed to store ogc31-context as binary file", e );
          plugin.getLog().log( status );
        }
        finally
        {
          IOUtils.closeQuietly( oos );
        }
      }
    }

    return s_context;
  }
}
