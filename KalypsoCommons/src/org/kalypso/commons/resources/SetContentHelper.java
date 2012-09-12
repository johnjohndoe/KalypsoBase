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
package org.kalypso.commons.resources;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.commons.KalypsoCommonsPlugin;
import org.kalypso.commons.internal.i18n.Messages;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.lang.CatchRunnable;

/**
 * Helper-Klasse f�r {@link org.eclipse.core.resources.IFile}. This is an abstract class, you must implement the
 * <code>write</code> method.
 *
 * @author belger
 */
public abstract class SetContentHelper
{
  private static final String BCKUP_SUFFIX = "_bckup_"; //$NON-NLS-1$

  final boolean m_doNotBackUp = Boolean.getBoolean( "kalypso.model.product.doNotMakeBckupsOnSave" ); //$NON-NLS-1$

  private String m_newCharset;

  private final String m_title;

  private String m_oldCharset;

  private boolean m_doCompress;

  public SetContentHelper( )
  {
    this( Messages.getString( "org.kalypso.commons.resources.SetContentHelper.0" ) ); //$NON-NLS-1$
  }

  /**
   * @param title
   *          title of the task in the context of the monitor
   */
  public SetContentHelper( final String title )
  {
    m_title = title;
  }

  public void setFileContents( final IFile file, final boolean force, final boolean keepHistory, final IProgressMonitor monitor ) throws CoreException
  {
    setFileContents( file, force, keepHistory, monitor, null );
  }

  public void setFileContents( final IFile file, final boolean force, final boolean keepHistory, final IProgressMonitor monitor, final String charset ) throws CoreException
  {
    m_oldCharset = findCurrentCharset( file );
    m_newCharset = findNewCharset( file, charset, m_oldCharset );

    PipedInputStream m_pis = null;
    boolean wasCreated = false;
    try
    {
      monitor.beginTask( m_title, 2000 );

      final PipedOutputStream pos = new PipedOutputStream();
      m_pis = new PipedInputStream( pos );

      final boolean doCompress = m_doCompress;

      final CatchRunnable innerRunnable = new CatchRunnable()
      {
        @Override
        protected void runIntern( ) throws Throwable
        {
          OutputStreamWriter outputStreamWriter = null;
          try
          {
            final OutputStream os;
            if( doCompress )
              os = new GZIPOutputStream( pos );
            else
              os = pos;

            outputStreamWriter = new OutputStreamWriter( os, getCharset() );
            write( outputStreamWriter );
            outputStreamWriter.close();
          }
          finally
          {
            IOUtils.closeQuietly( outputStreamWriter );
          }
        }
      };
      final Thread innerThread = new Thread( innerRunnable, "SetContentHelper" ); //$NON-NLS-1$
      innerThread.start();

      // set file contents
      if( file.exists() )
      {
        String bckupFileName = ""; //$NON-NLS-1$
        if( !m_doNotBackUp )
        {
          bckupFileName = createBckup( new File( file.getLocationURI() ) );
          file.refreshLocal( 0, monitor );
          file.setContents( m_pis, force, keepHistory, new SubProgressMonitor( monitor, 1000 ) );
          file.refreshLocal( 0, monitor );
        }
        else
        {
          file.setContents( m_pis, force, keepHistory, new SubProgressMonitor( monitor, 1000 ) );
        }

        /*
         * if the operation finished successfully remove the backup copy
         */
        if( !m_doNotBackUp )
        {
          try
          {
            final File bckupFile = new File( bckupFileName );
            try
            {
              bckupFile.delete();
            }
            catch( final Exception e )
            {
              bckupFile.deleteOnExit();
            }
          }
          catch( final Exception e )
          {
            // TODO: handle exception
          }
        }

      }
      else
      {
        file.create( m_pis, force, new SubProgressMonitor( monitor, 1000 ) );
        wasCreated = true;
      }

      // wait for innerThread to stop
      while( innerThread.isAlive() )
      {
        try
        {
          Thread.sleep( 100 );
        }
        catch( final InterruptedException e )
        {
          e.printStackTrace();
        }
      }

      m_pis.close();

      final Throwable thrown = innerRunnable.getThrown();
      if( thrown != null )
        throw new CoreException( StatusUtilities.statusFromThrowable( thrown, Messages.getString( "org.kalypso.commons.resources.SetContentHelper.2" ) ) ); //$NON-NLS-1$
    }
    catch( final CoreException e )
    {
      // if we should create the file, silently delete it now
      if( wasCreated )
      {
        try
        {
          file.delete( true, new NullProgressMonitor() );
        }
        catch( final CoreException ce )
        {
          // Log?

          // ignore
          ce.printStackTrace();
        }
      }

      // rethrow
      throw e;
    }
    catch( final IOException e )
    {
      e.printStackTrace();
      throw new CoreException( new Status( IStatus.ERROR, KalypsoCommonsPlugin.getID(), 0, Messages.getString( "org.kalypso.commons.resources.SetContentHelper.3" ), e ) ); //$NON-NLS-1$
    }
    finally
    {
      IOUtils.closeQuietly( m_pis );
    }

    if( m_newCharset != null && !ObjectUtils.equals( m_oldCharset, m_newCharset ) )
      file.setCharset( m_newCharset, new SubProgressMonitor( monitor, 1000 ) );

    // enclose in finally?
    monitor.done();
  }

  private String findNewCharset( final IFile file, final String charset, final String currentCharset ) throws CoreException
  {
    if( charset != null )
      return charset;

    if( currentCharset != null )
      return currentCharset;

    return file.getParent().getDefaultCharset();
  }

  /**
   * rename the existing original file, save return the name of this renamed file, create empty file to replace original
   * one
   *
   * @param gmlFile
   *          original file to create a backup from
   * @return file name of created backup file
   */
  private String createBckup( final File gmlFile )
  {
    final String fileName = gmlFile.getAbsolutePath();
    final String bckupFileName = fileName + BCKUP_SUFFIX + new Date().getTime();
    gmlFile.renameTo( new File( bckupFileName ) );
    try
    {
      gmlFile.createNewFile();
    }
    catch( final IOException e )
    {
      e.printStackTrace();
    }
    return bckupFileName;
  }

  private String findCurrentCharset( final IFile file ) throws CoreException
  {
    if( file.exists() )
      return file.getCharset();

    return null;
  }

  /**
   * Override this method to provide your business. The writer is closed once write returns.
   */
  protected abstract void write( final OutputStreamWriter writer ) throws Throwable;

  /**
   * @return the charset used for encoding the file
   */
  protected String getCharset( )
  {
    return m_newCharset;
  }

  public void setCompressed( final boolean doCompress )
  {
    m_doCompress = doCompress;
  }
}