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
package org.kalypso.contribs.eclipse.jface.wizard;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.kalypso.contribs.eclipse.EclipsePlatformContributionsPlugin;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.wizard.FileChooserGroup.FileChooserDelegate.FILE_CHOOSER_GROUP_TYPE;

/**
 * A group to choose a file from the file system.
 *
 * @author Gernot Belger
 */
public class FileChooserGroup
{
  static public class FileChooserDelegate
  {
    private final FILE_CHOOSER_GROUP_TYPE m_type;

    public enum FILE_CHOOSER_GROUP_TYPE
    {
      eOpen,
      eSave
    }

    public FileChooserDelegate( final FILE_CHOOSER_GROUP_TYPE type )
    {
      m_type = type;
    }

    public int getTextBoxStyle( )
    {
      switch( m_type )
      {
        case eOpen:
          return SWT.BORDER;

        case eSave:
          return SWT.BORDER | SWT.READ_ONLY;

        default:
          throw new UnsupportedOperationException();
      }
    }

    public String getButtonText( )
    {
      switch( m_type )
      {
        case eOpen:
          return "Datei ausw‰hlen";

        case eSave:
          return "Datei speichern unter";

        default:
          throw new UnsupportedOperationException();
      }
    }

    public int getFileDialogType( )
    {
      switch( m_type )
      {
        case eOpen:
          return SWT.OPEN;

        case eSave:
          return SWT.SAVE;

        default:
          throw new UnsupportedOperationException();
      }
    }

    /**
     * overwrite
     */
    public String[] getFilterExtensions( )
    {
      return new String[] {};
    }

    /**
     * overwrite
     */
    public String[] getFilterNames( )
    {
      return new String[] {};
    }

    public String updateFileName( final FileDialog dialog, final String newFilename )
    {
      switch( m_type )
      {
        case eOpen:
          return newFilename;

        case eSave:
          final String[] extensions = dialog.getFilterExtensions();
          return newFilename + "." + extensions[0];

        default:
          throw new UnsupportedOperationException();
      }
    }
  }

  private static final String SETTINGS_FILENAME = "fileChooserGroup.filename";

  private File m_file;

  private IDialogSettings m_settings;

  private Text m_text;

  private final Set<FileChangedListener> m_listeners = new HashSet<FileChangedListener>();

  private final FileChooserDelegate m_delegate;

  public interface FileChangedListener
  {
    public void fileChanged( final File file );
  }

  public FileChooserGroup( )
  {
    this( new FileChooserDelegate( FILE_CHOOSER_GROUP_TYPE.eOpen ) );
  }

  public FileChooserGroup( final FileChooserDelegate delegate )
  {
    m_delegate = delegate;
  }

  protected void dispose( )
  {
    m_listeners.clear();
  }

  /**
   * Sets the dialog settings used to remember the last entered filename.
   * 
   * @param settings
   *          If <code>null</code>, the filename will not be stored.
   */
  public void setDialogSettings( final IDialogSettings settings )
  {
    m_settings = settings;

    if( (m_settings != null) && (m_text != null) && !m_text.isDisposed() )
    {
      final String filename = m_settings.get( FileChooserGroup.SETTINGS_FILENAME );
      if( filename != null )
      {
        final Text text = m_text;
        text.getDisplay().syncExec( new Runnable()
        {
          public void run( )
          {
            text.setText( filename );
          }
        } );
      }
    }
  }

  public Group createControl( final Composite parent, final int style )
  {
    final Group group = new Group( parent, style );
    // Remove all listeners when disposed; no events can happen anymore, and the client will most certainly do not
    // unregister its listener
    group.addDisposeListener( new DisposeListener()
    {
      /**
       * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
       */
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        FileChooserGroup.this.dispose();
      }
    } );

    final GridLayout gridLayout = new GridLayout();
    gridLayout.numColumns = 3;

    group.setLayout( gridLayout );

    final Label label = new Label( group, SWT.NONE );
    label.setText( "&Datei" );

    final Text text = new Text( group, m_delegate.getTextBoxStyle() );
    m_text = text;
    text.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    m_text.addModifyListener( new ModifyListener()
    {
      public void modifyText( final ModifyEvent e )
      {
        textModified( text.getText() );
      }
    } );

    final Button button = new Button( group, SWT.NONE );
    button.setText( "..." );
    button.setToolTipText( m_delegate.getButtonText() );
    button.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        buttonPressed();
      }
    } );

    /* Restore settings */
    if( m_settings != null )
    {
      final String filename = m_settings.get( FileChooserGroup.SETTINGS_FILENAME );
      if( filename != null )
        m_text.setText( filename );
    }

    return group;
  }

  protected void textModified( final String text )
  {
    setFile( new File( text ) );
  }

  protected void buttonPressed( )
  {
    final String currentFilename = m_text.getText();

    final FileDialog dialog = new FileDialog( m_text.getShell(), m_delegate.getFileDialogType() );
    dialog.setFileName( currentFilename );
    dialog.setText( m_delegate.getButtonText() );

    dialog.setFilterExtensions( m_delegate.getFilterExtensions() );
    dialog.setFilterNames( m_delegate.getFilterNames() );

    final String newFilename = dialog.open();
    if( newFilename == null )
      return;

    m_text.setText( m_delegate.updateFileName( dialog, newFilename ) );
  }

  private void setFile( final File file )
  {
    if( m_file == file )
      return;

    m_file = file;

    if( m_settings != null )
      m_settings.put( FileChooserGroup.SETTINGS_FILENAME, file.getAbsolutePath() );

    fireFileChanged( m_file );
  }

  private void fireFileChanged( final File file )
  {
    for( final FileChangedListener l : m_listeners )
      try
      {
        l.fileChanged( file );
      }
      catch( final Throwable e )
      {
        EclipsePlatformContributionsPlugin.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
      }
  }

  /**
   * Has no effekt if the same listener has already been added.
   */
  public void addFileChangedListener( final FileChangedListener l )
  {
    m_listeners.add( l );
  }

  public void removeFileChangedListener( final FileChangedListener l )
  {
    m_listeners.remove( l );
  }
}
