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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.commons.internal.i18n.Messages;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;

/**
 * // FIXME: the whole class is now rotten! Instead write direclty into java file; than refresh the workspace; also handle backup stuff! <br>
 * TODO: we need another helper class instead of this stuff...<br/>
 * Helper-Klasse für {@link org.eclipse.core.resources.IFile}. This is an abstract class, you must implement the <code>write</code> method.
 *
 * @author Gernot Belger
 */
public abstract class SetContentHelper
{
  private static final String BCKUP_SUFFIX = "_bckup_"; //$NON-NLS-1$

  private final String m_title;

  private boolean m_doCompress = false;

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

  public void setCompressed( final boolean doCompress )
  {
    m_doCompress = doCompress;
  }

  // FIXME: still ugly: history not kept and force not respected; use only for big files; else write into StringWrite and directly call IFile#setContents
  public void setFileContents( final IFile file, final boolean force, final boolean keepHistory, final IProgressMonitor monitor, final String charset ) throws CoreException
  {
    final String oldCharset = findCurrentCharset( file );
    final String newCharset = findNewCharset( file, charset, oldCharset );

    final File javaFile = file.getLocation().toFile();

    // save file to backup
    final Path filePath = javaFile.toPath();

    final String fileName = javaFile.getAbsolutePath();
    final String backupFileName = fileName + BCKUP_SUFFIX + System.currentTimeMillis();
    final File backupFile = new File( backupFileName );

    try
    {
      writeContents( backupFile, newCharset, monitor );

      /* rename backup to new file */
      Files.move( backupFile.toPath(), filePath, StandardCopyOption.REPLACE_EXISTING );

      file.refreshLocal( IFile.DEPTH_ZERO, new NullProgressMonitor() );

      if( newCharset != null && !ObjectUtils.equals( oldCharset, newCharset ) )
        file.setCharset( newCharset, new SubProgressMonitor( monitor, 1000 ) );
    }
    catch( final Throwable e )
    {
      throw new CoreException( StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.commons.resources.SetContentHelper.2" ) ) ); //$NON-NLS-1$
    }

    // enclose in finally?
    monitor.done();
  }

  private void writeContents( final File file, final String charset, final IProgressMonitor monitor ) throws Throwable
  {
    monitor.beginTask( m_title, 2000 );

    try( OutputStreamWriter writer = new OutputStreamWriter( openStream( file ), charset ) )
    {
      write( writer );
      writer.close();
    }
  }

  private OutputStream openStream( final File file ) throws IOException
  {
    final BufferedOutputStream bos = new BufferedOutputStream( new FileOutputStream( file ) );

    if( m_doCompress )
      return new GZIPOutputStream( bos );
    else
      return bos;
  }

  private String findNewCharset( final IFile file, final String charset, final String currentCharset ) throws CoreException
  {
    if( charset != null )
      return charset;

    if( currentCharset != null )
      return currentCharset;

    // FIXME: why parent, shouldnt we directly use file?
    return file.getParent().getDefaultCharset();
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
}