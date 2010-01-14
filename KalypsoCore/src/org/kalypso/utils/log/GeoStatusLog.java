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
package org.kalypso.utils.log;

import java.io.File;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.gml.binding.commons.IStatusCollection;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.osgi.framework.Bundle;

/**
 * A {@link ILog} which can be serialized as gml.
 * 
 * @author Holger Albert
 */
public class GeoStatusLog implements ILog
{
  /**
   * The log file.
   */
  private File m_logFile;

  /**
   * The workspace.
   */
  private GMLWorkspace m_workspace;

  /**
   * The collection of geo status objects.
   */
  private IStatusCollection m_statusCollection;

  /**
   * The constructor.
   * 
   * @param logFile
   *          The log file.
   */
  public GeoStatusLog( File logFile )
  {
    m_logFile = logFile;
    m_workspace = createWorkspace();
    m_statusCollection = null;
    if( m_workspace != null )
      m_statusCollection = (IStatusCollection) m_workspace.getRootFeature().getAdapter( IStatusCollection.class );
  }

  /**
   * @see org.eclipse.core.runtime.ILog#log(org.eclipse.core.runtime.IStatus)
   */
  @Override
  public void log( IStatus status )
  {
    /* Without a collection of geo status objects, nothing can be done. */
    if( m_statusCollection == null )
      return;

    /* Create the new geo status. */
    m_statusCollection.createGeoStatus( status );
  }

  /**
   * @see org.eclipse.core.runtime.ILog#addLogListener(org.eclipse.core.runtime.ILogListener)
   */
  @Override
  public void addLogListener( ILogListener listener )
  {
  }

  /**
   * @see org.eclipse.core.runtime.ILog#removeLogListener(org.eclipse.core.runtime.ILogListener)
   */
  @Override
  public void removeLogListener( ILogListener listener )
  {
  }

  /**
   * @see org.eclipse.core.runtime.ILog#getBundle()
   */
  @Override
  public Bundle getBundle( )
  {
    return null;
  }

  /**
   * This function serializes the log.
   */
  public void serialize( ) throws Exception
  {
    /* Without a log file, nothing can be done. */
    if( m_logFile == null )
      throw new Exception( "No log file was provided..." );

    /* Without workspace, nothing can be done. */
    if( m_workspace == null )
      throw new Exception( "No workspace was created..." );

    /* Serialize the workspace. */
    GmlSerializer.serializeWorkspace( m_logFile, m_workspace, "UTF-8" );
  }

  /**
   * This function creates the workspace.
   * 
   * @return The workspace or null on error.
   */
  private GMLWorkspace createWorkspace( )
  {
    try
    {
      /* Create the GML workspace which will hold the geo status objects. */
      return FeatureFactory.createGMLWorkspace( IStatusCollection.QNAME, null, null );
    }
    catch( Exception ex )
    {
      ex.printStackTrace();
      return null;
    }
  }
}