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
 *  g.belger@bjoernsen.de
 *  m.schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *
 *  ---------------------------------------------------------------------------*/
package org.kalypso.ui.repository;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.core.runtime.IStatusCollector;
import org.kalypso.contribs.eclipse.core.runtime.StatusCollector;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.sensor.DateRange;
import org.kalypso.ogc.sensor.IObservation;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.ogc.sensor.cache.ObservationCache;
import org.kalypso.ogc.sensor.request.ObservationRequest;
import org.kalypso.ogc.sensor.view.ObservationViewHelper;
import org.kalypso.ogc.sensor.zml.ZmlFactory;
import org.kalypso.repository.IRepository;
import org.kalypso.repository.IRepositoryItem;
import org.kalypso.repository.RepositoryException;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypso.utils.log.GeoStatusLog;

/**
 * This class dumps a repository kompletely into the filesystem.
 * 
 * @author Holger Albert
 */
public class RepositoryDumper implements ICoreRunnableWithProgress
{
  private final IStatusCollector m_stati = new StatusCollector( KalypsoGisPlugin.getId() );

  private final File m_directory;

  private final IRepositoryItem m_root;

  public RepositoryDumper( final File directory, final IRepositoryItem root )
  {
    m_directory = directory;
    m_root = root;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws InvocationTargetException, InterruptedException
  {
    monitor.beginTask( Messages.getString( "org.kalypso.ogc.sensor.view.DumpExtendedHandler.5" ), IProgressMonitor.UNKNOWN ); //$NON-NLS-1$

    Writer structureWriter = null;

    try
    {
      /* Create the structure file. */
      final File structureFile = new File( m_directory, "structure.txt" ); //$NON-NLS-1$

      /* The writer to save the file. */
      structureWriter = new OutputStreamWriter( new FileOutputStream( structureFile ), "UTF-8" ); //$NON-NLS-1$

      /* Dump upwards */
      final File baseDirectory = dumpUpwards( structureWriter );
      FileUtils.forceMkdir( baseDirectory );

      /* Do the dump into the filesystem. */
      dumpExtendedRecursive( structureWriter, baseDirectory, m_root, monitor );

      structureWriter.close();

      /* Update monitor. */
      monitor.worked( 800 );

      /* Create the result status. */
      final IStatus status = m_stati.asMultiStatusOrOK( Messages.getString( "RepositoryDumper.0" ), Messages.getString( "RepositoryDumper.1" ) ); //$NON-NLS-1$ //$NON-NLS-2$

      /* Writes the log. */
      writeLogQuietly( status );

      return status;
    }
    catch( final InterruptedException e )
    {
      throw e;
    }
    catch( final Exception e )
    {
      throw new InvocationTargetException( e );
    }
    finally
    {
      IOUtils.closeQuietly( structureWriter );
      monitor.done();
    }
  }

  private void writeLogQuietly( final IStatus status )
  {
    try
    {
      /* Log the status, if possible. */
      final GeoStatusLog log = new GeoStatusLog( new File( m_directory, "export.log" ) ); //$NON-NLS-1$
      log.log( status );
      log.serialize();
    }
    catch( final CoreException ex )
    {
      /* Ignore. */
      ex.printStackTrace();
    }
  }

  private File dumpUpwards( final Writer structureWriter ) throws RepositoryException, IOException
  {
    final IRepositoryItem[] parentChain = findParentChain( m_root );
    ArrayUtils.reverse( parentChain );

    File currentDir = m_directory;

    for( final IRepositoryItem parentItem : parentChain )
    {
      /* The name will be used as filename. */
      final String name = FileUtilities.resolveValidFileName( parentItem.getName() );
      final File zmlFile = new File( currentDir, name + ".zml" ); //$NON-NLS-1$

      dumpItem( structureWriter, zmlFile, parentItem );

      /* This is the directory, where the children are placed. */
      currentDir = new File( currentDir, name );
      FileUtils.forceMkdir( currentDir );
    }

    return currentDir;
  }

  private IRepositoryItem[] findParentChain( final IRepositoryItem item ) throws RepositoryException
  {
    final List<IRepositoryItem> items = new ArrayList<>();

    IRepositoryItem currentItem = item;
    while( currentItem != null && !(currentItem instanceof IRepository) )
    {
      items.add( currentItem );
      currentItem = currentItem.getParent();
    }

    if( !items.isEmpty() )
      items.remove( 0 );

    // BIG HACK: remove last element, as this is the ServiceRepositoryItem -> we do not want it for PSI-Fake
    if( !items.isEmpty() )
      items.remove( items.size() - 1 );

    return items.toArray( new IRepositoryItem[items.size()] );
  }

  /**
   * Creates the dump structure in the file-system and into one structure file <br/>
   * REMARK: this uses the file format which is compatible to the Kalypso-PSICompact-Fake implementation. So exported
   * repositories can directly be included via that repository implementation.
   * 
   * @param structureWriter
   * @param directory
   *          The choosen directory.
   * @param monitor
   *          A progress monitor.
   * @throws InterruptedException
   * @throws RepositoryException
   */
  private void dumpExtendedRecursive( final Writer structureWriter, final File directory, final IRepositoryItem item, final IProgressMonitor monitor ) throws InterruptedException, RepositoryException, IOException
  {
    /* If the user canceled the operation, abort. */
    if( monitor.isCanceled() )
      throw new InterruptedException();

    try
    {
      /* Write entry for structure file */
      final String identifier = item.getIdentifier();
      monitor.subTask( identifier );

      /* The name will be used as filename. */
      final String name = FileUtilities.resolveValidFileName( item.getName() );
      final File zmlFile = new File( directory, name + ".zml" ); //$NON-NLS-1$

      dumpItem( structureWriter, zmlFile, item );

      /* This is the directory, where the children are placed. */
      final File newDirectory = new File( directory, name );
      FileUtils.forceMkdir( newDirectory );

      final IRepositoryItem[] items = item.getChildren();
      if( items != null )
      {
        for( final IRepositoryItem item2 : items )
          dumpExtendedRecursive( structureWriter, newDirectory, item2, monitor );
      }

      monitor.worked( 1 );
    }
    catch( final NoSuchElementException ex )
    {
      ex.printStackTrace();

      final String msg = String.format( Messages.getString( "RepositoryDumper.3" ), item.getName() ); //$NON-NLS-1$
      m_stati.add( IStatus.ERROR, msg, ex );
    }
    finally
    {
      monitor.worked( 1 );
    }
  }

  public void dumpItem( final Writer structureWriter, final File zmlFile, final IRepositoryItem item )
  {
    for( int i = 0; i < 2; i++ )
    {
      try
      {
        Thread.sleep( 100 );
        doDump( structureWriter, zmlFile, item );
        /* Return on first success */
        return;
      }
      catch( final IOException e )
      {
        e.printStackTrace();

        final String msg = String.format( Messages.getString( "RepositoryDumper.4" ), item.getIdentifier() ); //$NON-NLS-1$
        m_stati.add( IStatus.ERROR, msg, e );
        /* Not twice! */
        return;
      }
      catch( final Throwable e )
      {
        e.printStackTrace();

        final int severity = i == 0 ? IStatus.WARNING : IStatus.ERROR;
        final String msg = String.format( Messages.getString( "RepositoryDumper.5" ), i + 1, item.getIdentifier() ); //$NON-NLS-1$
        m_stati.add( severity, msg, e );
      }
    }
  }

  private void doDump( final Writer structureWriter, final File zmlFile, final IRepositoryItem item ) throws IOException, SensorException
  {
    final String structIdentifier = buildStructIdentifier( item );

    structureWriter.write( structIdentifier );
    structureWriter.write( ';' );

    final IObservation observation = ObservationCache.getInstance().getObservationFor( item );
    if( observation != null )
    {
      final String relativePathTo = FileUtilities.getRelativePathTo( m_directory, zmlFile );
      /*
       * We write a unix style path here, as this is more generally recognized. Also, we can directly use the
       * structure.txt for PSI-Fake
       */
      final String unixPath = FilenameUtils.separatorsToUnix( relativePathTo );

      structureWriter.write( unixPath );
      structureWriter.write( ';' );
      structureWriter.write( observation.getName() );

      /* Dump if neccessary. */
      final DateRange dra = ObservationViewHelper.makeDateRange( item );

      ZmlFactory.writeToFile( observation, zmlFile, new ObservationRequest( dra ) );
    }

    structureWriter.write( "\n" ); //$NON-NLS-1$
  }

  /**
   * Hacky: in order to directly use the structure.txt for the psi-fake impl, we tweak the written id.
   */
  private static String buildStructIdentifier( final IRepositoryItem item )
  {
    final String identifier = item.getIdentifier();

    final int index = identifier.indexOf( "://" ); //$NON-NLS-1$
    if( index == -1 )
      return identifier;

    return identifier.substring( index + 3 );
  }

  public static String buildStructIdentifier( final String identifier, final String rootIdentifier )
  {
    final String structIdentifier = identifier.substring( rootIdentifier.length() );
    return structIdentifier;
  }
}