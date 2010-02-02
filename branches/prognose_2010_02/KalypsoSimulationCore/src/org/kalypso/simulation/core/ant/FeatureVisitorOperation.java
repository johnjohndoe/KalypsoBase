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
package org.kalypso.simulation.core.ant;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.java.net.UrlResolverSingleton;
import org.kalypso.contribs.java.util.logging.ILogger;
import org.kalypso.ogc.gml.serialize.GmlSerializeException;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.simulation.core.KalypsoSimulationCorePlugin;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.visitors.CountFeatureVisitor;
import org.kalypsodeegree_impl.model.feature.visitors.MonitorFeatureVisitor;

/**
 * @author belger
 */
public class FeatureVisitorOperation implements ICoreRunnableWithProgress
{
  private final IFeatureVisitorTask m_visitorTask;

  public FeatureVisitorOperation( final IFeatureVisitorTask visitorTask )
  {
    m_visitorTask = visitorTask;
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress#execute(org.eclipse.core.runtime.IProgressMonitor)
   */
  public final IStatus execute( final IProgressMonitor monitor ) throws InterruptedException
  {
    try
    {
      final String description = m_visitorTask.getVisitorTaskDescription();
      final URL gmlLocation = m_visitorTask.getGmlLocation();

      final IProgressMonitor niceMonitor = new AntProgressMonitor( monitor, description );
      niceMonitor.beginTask( description, 100 );

      niceMonitor.subTask( "Parameter werden validiert" );
      m_visitorTask.validateInput();
      niceMonitor.worked( 10 );

      niceMonitor.subTask( "Lese Konfiguration" );
      final GMLWorkspace workspace = GmlSerializer.createGMLWorkspace( gmlLocation, null );
      niceMonitor.worked( 10 );

      final List<IStatus> stati = executeFeaturePathes( new SubProgressMonitor( niceMonitor, 70 ), workspace );

      if( m_visitorTask.doSaveGml() )
      {
        niceMonitor.subTask( "Schreibe Ergebnis" );
        saveGML( gmlLocation, workspace );
        niceMonitor.worked( 10 );
      }

      return new MultiStatus( KalypsoSimulationCorePlugin.getID(), 0, stati.toArray( new IStatus[stati.size()] ), "", null );
    }
    catch( final Exception e )
    {
      if( e instanceof InterruptedException )
        throw (InterruptedException) e;

      return StatusUtilities.statusFromThrowable( e );
    }
    finally
    {
      monitor.done();
    }
  }

  private List<IStatus> executeFeaturePathes( final IProgressMonitor monitor, final GMLWorkspace workspace ) throws InterruptedException
  {
    final String[] featurePathes = m_visitorTask.getFeaturePathes();
    monitor.beginTask( m_visitorTask.getVisitorTaskDescription(), featurePathes.length );
    monitor.subTask( "wird bearbeitet..." );
    final List<IStatus> stati = new ArrayList<IStatus>();
    for( final String featurePath : featurePathes )
    {
      if( monitor.isCanceled() )
        throw new InterruptedException();

      try
      {
        if( featurePathes.length > 1 )
          monitor.subTask( String.format( "Bearbeite %s", featurePath ) );

        final IStatus result = visitPath( workspace, featurePath, new SubProgressMonitor( monitor, 1, SubProgressMonitor.PREPEND_MAIN_LABEL_TO_SUBTASK ) );
        if( !result.isOK() )
          stati.add( result );
      }
      catch( final IllegalArgumentException e )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( e );
        if( m_visitorTask.doIgnoreIllegalFeaturePath() )
        {
          getLogger().log( Level.WARNING, -1, "Feature wird ignoriert (" + status.getMessage() + ")" );
        }
        else
        {
          getLogger().log( Level.WARNING, -1, status.getMessage() );
          stati.add( status );
        }
      }
      catch( final Throwable t )
      {
        final IStatus status = StatusUtilities.statusFromThrowable( t );
        getLogger().log( Level.SEVERE, -1, status.getMessage() );
        stati.add( status );
      }
    }
    return stati;
  }

  private ILogger getLogger( )
  {
    return m_visitorTask.getLogger();
  }

  private IStatus visitPath( final GMLWorkspace workspace, final String featurePath, final IProgressMonitor monitor ) throws CoreException, InvocationTargetException, InterruptedException
  {
    FeatureVisitor visitor;
    try
    {
      final ILogger logger = getLogger();
      final URL context = m_visitorTask.getContext();
      visitor = m_visitorTask.createVisitor( context, logger );

      // count features
      final int depth = m_visitorTask.getDepth();
      final int count = countFeatures( workspace, featurePath, depth );
      final MonitorFeatureVisitor wrappedVisitor = new MonitorFeatureVisitor( monitor, count, visitor );
      workspace.accept( wrappedVisitor, featurePath, depth );
    }
    catch( final OperationCanceledException e )
    {
      throw new InterruptedException();
    }
    finally
    {
      monitor.done();
    }

    return m_visitorTask.statusFromVisitor( visitor );
  }

  private int countFeatures( final GMLWorkspace workspace, final String featurePath, final int depth )
  {
    final CountFeatureVisitor countFeatureVisitor = new CountFeatureVisitor();
    workspace.accept( countFeatureVisitor, featurePath, depth );
    return countFeatureVisitor.getCount();
  }

  private void saveGML( final URL gmlURL, final GMLWorkspace workspace ) throws IOException, GmlSerializeException
  {
    OutputStreamWriter writer = null;
    try
    {
      writer = UrlResolverSingleton.getDefault().createWriter( gmlURL );
      GmlSerializer.serializeWorkspace( writer, workspace );
      writer.close();
    }
    finally
    {
      IOUtils.closeQuietly( writer );
    }
  }

}
