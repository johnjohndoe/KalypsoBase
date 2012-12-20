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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
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
  private final File m_logFile;

  /**
   * The workspace.
   */
  private final GMLWorkspace m_workspace;

  /**
   * The collection of geo status objects.
   */
  private IStatusCollection m_statusCollection;

  private final String m_bundleID;

  /**
   * The constructor.
   * 
   * @param logFile
   *          The log file.
   * @deprecated Use {@link #GeoStatusLog(File, String)} instead.
   */
  @Deprecated
  public GeoStatusLog( final File logFile )
  {
    this( logFile, null );
  }

  public GeoStatusLog( final File logFile, final String bundleID )
  {
    m_logFile = logFile;
    m_bundleID = bundleID;
    m_workspace = createWorkspace();
    m_statusCollection = null;
    if( m_workspace != null )
      m_statusCollection = (IStatusCollection)m_workspace.getRootFeature().getAdapter( IStatusCollection.class );
  }

  /**
   * @deprecated Use {@link #GeoStatusLog(IFile, String)} instead.
   */
  @Deprecated
  public GeoStatusLog( final IFile iFile )
  {
    this( iFile.getLocation().toFile() );
  }

  public GeoStatusLog( final IFile iFile, final String bundleID )
  {
    this( iFile.getLocation().toFile(), bundleID );
  }

  @Override
  public void log( final IStatus status )
  {
    /* Without a collection of geo status objects, nothing can be done. */
    if( m_statusCollection == null )
      return;

    /* Create the new geo status. */
    m_statusCollection.createGeoStatus( status );
  }

  @Override
  public void addLogListener( final ILogListener listener )
  {
  }

  @Override
  public void removeLogListener( final ILogListener listener )
  {
  }

  @Override
  public Bundle getBundle( )
  {
    return Platform.getBundle( m_bundleID );
  }

  /**
   * This function serializes the log.
   */
  public void serialize( ) throws CoreException
  {
    /* Without a log file, nothing can be done. */
    if( m_logFile == null )
      throw new CoreException( new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), Messages.getString( "GeoStatusLog_0" ) ) ); //$NON-NLS-1$

    /* Without workspace, nothing can be done. */
    if( m_workspace == null )
      throw new CoreException( new Status( IStatus.WARNING, KalypsoCorePlugin.getID(), Messages.getString( "GeoStatusLog_1" ) ) ); //$NON-NLS-1$

    try
    {
      /* Serialize the workspace. */
      GmlSerializer.serializeWorkspace( m_logFile, m_workspace, "UTF-8" ); //$NON-NLS-1$
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      final String message = String.format( Messages.getString( "GeoStatusLog_3" ), m_logFile ); //$NON-NLS-1$
      throw new CoreException( new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), message ) );
    }
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
    catch( final Exception ex )
    {
      ex.printStackTrace();
      return null;
    }
  }
}