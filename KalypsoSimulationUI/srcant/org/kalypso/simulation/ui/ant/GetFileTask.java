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
package org.kalypso.simulation.ui.ant;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;

import org.apache.commons.io.FileUtils;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.java.util.logging.ILogger;
import org.kalypso.contribs.java.util.logging.LoggerUtilities;

/**
 * Loads (ant-)properties from a gml.
 * 
 * <pre>
 *  <kalypso.getFileTask sourceDir="C:\MyDataDir" targetFile="C:\MyFiles\data.file" resultProperty="getFileResult"/>
 * </pre>
 * 
 * @author Gernot Belger
 */
public class GetFileTask extends Task
{
  public final static class FileFilter
  {
    private String m_description;

    private String m_pattern;

    public String getDescription( )
    {
      return m_description;
    }

    public void setDescription( final String description )
    {
      m_description = description;
    }

    public String getPattern( )
    {
      return m_pattern;
    }

    public void setPattern( final String pattern )
    {
      m_pattern = pattern;
    }
  }

  private final PropertyAdder m_propertyAdder = new PropertyAdder( this );

  private final List<FileFilter> m_filters = new LinkedList<FileFilter>();

  /** Initial directory for the file open dialog. */
  private File m_sourceDir;

  /** Destination where to copy the chosen file */
  private File m_targetFile;

  private String m_resultProperty;

  public File getTargetFile( )
  {
    return m_targetFile;
  }

  public void setTargetFile( final File targetFile )
  {
    m_targetFile = targetFile;
  }

  public File getSourceDir( )
  {
    return m_sourceDir;
  }

  public void setSourceDir( final File sourceDir )
  {
    m_sourceDir = sourceDir;
  }

  public String getResultProperty( )
  {
    return m_resultProperty;
  }

  public void setResultProperty( final String resultProperty )
  {
    m_resultProperty = resultProperty;
  }

  public FileFilter createFileFilter( )
  {
    final FileFilter ff = new FileFilter();
    m_filters.add( ff );
    return ff;
  }

  /**
   * @see org.apache.tools.ant.Task#execute()
   */
  @Override
  public void execute( ) throws BuildException
  {
    final Project antProject = getProject();
    final ILogger logger = new ILogger()
    {
      /**
       * @see org.kalypso.contribs.java.util.logging.ILogger#log(java.util.logging.Level, int, java.lang.String)
       */
      @Override
      public void log( final Level level, final int msgCode, final String message )
      {
        final String outString = LoggerUtilities.formatLogStylish( level, msgCode, message );
        if( antProject == null )
          System.out.println( outString );
        else
          antProject.log( outString );
      }
    };

    final String taskDesk = getDescription();
    final String msgTitle = taskDesk == null ? "Datei �ffnen" : taskDesk;

    // validieren
    if( m_targetFile == null )
    {
      logger.log( Level.INFO, LoggerUtilities.CODE_NEW_MSGBOX, msgTitle );
      logger.log( Level.SEVERE, LoggerUtilities.CODE_SHOW_MSGBOX, "Property 'targetFile' must be set." );
      return;
    }

    // Ask user for file

    /* Use array in order to be able to return value from runnable */
    final File[] resultFile = new File[1];
    final Display display = PlatformUI.getWorkbench().getDisplay();
    display.syncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        resultFile[0] = askForFile( display );
      }
    } );

    if( resultFile[0] == null )
    {
      logger.log( Level.INFO, LoggerUtilities.CODE_NEW_MSGBOX, msgTitle );
      logger.log( Level.INFO, LoggerUtilities.CODE_SHOW_MSGBOX, "Abbruch durch den Benutzer" );
      return;
    }

    // Datei kopieren
    try
    {
      FileUtils.copyFile( resultFile[0], m_targetFile );
    }
    catch( final IOException e )
    {
      logger.log( Level.INFO, LoggerUtilities.CODE_NEW_MSGBOX, msgTitle );
      logger.log( Level.INFO, LoggerUtilities.CODE_SHOW_DETAILS, "Quelle: " + resultFile[0].getAbsolutePath() );
      logger.log( Level.INFO, LoggerUtilities.CODE_SHOW_DETAILS, "Ziel: " + m_targetFile );
      logger.log( Level.SEVERE, LoggerUtilities.CODE_SHOW_MSGBOX, "Fehler beim Kopieren der Datei: " + e.getLocalizedMessage() );
      return;
    }

    logger.log( Level.INFO, LoggerUtilities.CODE_NONE, "Quelle: " + resultFile[0].getAbsolutePath() );
    logger.log( Level.INFO, LoggerUtilities.CODE_NONE, "Ziel: " + m_targetFile );

    // evtl. ergebnis property setzen
    if( m_resultProperty != null )
      m_propertyAdder.addProperty( m_resultProperty, "true", null );
  }

  protected File askForFile( final Display display )
  {
    final Shell shell = findShell( display );

    final FileDialog dialog = new FileDialog( shell, SWT.OPEN );
    if( m_sourceDir != null )
      dialog.setFilterPath( m_sourceDir.getAbsolutePath() );

    final String resultPath = dialog.open();
    if( resultPath == null )
      return null;

    return new File( resultPath );
  }

  private Shell findShell( final Display display )
  {
    final String shellTitle = JFaceResources.getString( "ProgressMonitorDialog.title" );

    final Shell[] shells = display.getShells();
    // HACK: we are looking for a progress monitor here...
    // Hopefully thing work also for the next eclipse versions...
    for( final Shell shell : shells )
    {
      if( shell.getText().equals( shellTitle ) )
        return shell;
    }

    return shells[0];
  }
}