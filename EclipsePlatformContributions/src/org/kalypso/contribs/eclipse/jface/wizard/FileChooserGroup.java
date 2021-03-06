/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Bj�rnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universit�t Hamburg-Harburg, Institut f�r Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.contribs.eclipse.jface.wizard;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.util.SafeRunnable;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;

/**
 * A group to choose a file from the file system.
 *
 * @author Gernot Belger
 */
public class FileChooserGroup
{
  public interface FileChangedListener
  {
    void fileChanged( File file );
  }

  private static final String SETTINGS_FILENAME = "fileChooserGroup.filename"; //$NON-NLS-1$

  /**
   * used as Filter like "*.txt" if this dialog can choose directories instead of files
   *
   * @see FileChooserDelegate#getFilterExtensions()
   */
  public static final String DIRECTORY_FILTER_SUFFIX = "DIRECTORY_FILTER_SUFFIX"; //$NON-NLS-1$

  private String m_path;

  private IDialogSettings m_settings;

  private Text m_text;

  private final Set<FileChangedListener> m_listeners = new HashSet<>();

  private final IFileChooserDelegate m_delegate;

  private String m_label;

  public FileChooserGroup( )
  {
    this( new FileChooserDelegateOpen() );
  }

  public FileChooserGroup( final IFileChooserDelegate delegate )
  {
    m_delegate = delegate;
  }

  /**
   * Sets and hence overwrites the default label.<br/>
   * If set to <code>null</code>, no label will be shown.<br/>
   * Must be called before {@link #createControl(Composite, int)} is invoked.
   */
  public void setLabel( final String label )
  {
    m_label = label;
  }

  /**
   * Has no effect if the same listener has already been added.
   */
  public void addFileChangedListener( final FileChangedListener l )
  {
    m_listeners.add( l );
  }

  protected void buttonPressed( )
  {
    final File currentFile = getFile();
    final File newFile = m_delegate.chooseFile( m_text.getShell(), currentFile );
    if( newFile == null )
      return;

    m_text.setText( newFile.getAbsolutePath() );
  }

  public Group createGroup( final Composite parent, final int style )
  {
    final Group group = new Group( parent, style );

    final GridLayout gridLayout = new GridLayout( 3, false );
    group.setLayout( gridLayout );

    createControlsInGrid( group );

    return group;
  }

  /**
   * Creates the controls of this {@link FileChooserGroup} inside the given {@link Composite}.<br>
   * The composite must have a {@link GridLayout} as layout with at least 3 columns.
   */
  public void createControlsInGrid( final Composite parent )
  {
    final Layout layout = parent.getLayout();
    Assert.isTrue( layout instanceof GridLayout );
    final GridLayout gridLayout = (GridLayout) layout;
    Assert.isTrue( gridLayout.numColumns >= 3 );

    // Remove all listeners when disposed; no events can happen anymore, and the client will most certainly do not
    // unregister its listener
    parent.addDisposeListener( new DisposeListener()
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

    if( m_label != null )
    {
      final Label label = new Label( parent, SWT.NONE );
      label.setText( m_label );
    }

    final Text text = new Text( parent, m_delegate.getTextBoxStyle() );
    m_text = text;
    final int textSpan = m_label != null ? gridLayout.numColumns - 2 : gridLayout.numColumns - 1;
    text.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, textSpan, 1 ) );
    m_text.addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( final ModifyEvent e )
      {
        textModified( text.getText() );
      }
    } );

    final Button button = new Button( parent, SWT.NONE );
    button.setText( "..." ); //$NON-NLS-1$
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
    final String filename = getInitialPath();
    if( filename != null )
      m_text.setText( filename );
  }

  private String getInitialPath( )
  {
    final String savedPath;
    if( m_settings == null )
      savedPath = null;
    else
      savedPath = m_settings.get( FileChooserGroup.SETTINGS_FILENAME );

    return m_delegate.getInitialPath( savedPath );
  }

  protected void dispose( )
  {
    m_listeners.clear();
  }

  private void fireFileChanged( final File file )
  {
    for( final FileChangedListener l : m_listeners )
    {
      SafeRunner.run( new SafeRunnable()
      {
        @Override
        public void run( ) throws Exception
        {
          l.fileChanged( file );
        }
      } );
    }
  }

  public void removeFileChangedListener( final FileChangedListener l )
  {
    m_listeners.remove( l );
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

    if( m_text == null || m_text.isDisposed() )
      return;

    final String filename = getInitialPath();
    if( filename == null || filename.equalsIgnoreCase( m_text.getText() ) )
      return;

    final Text text = m_text;
    text.getDisplay().syncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        text.setText( filename );
      }
    } );
  }

  /**
   * Returns the current content of the text control.
   */
  public String getPath( )
  {
    return m_path;
  }

  public File getFile( )
  {
    if( m_path == null || m_path.length() == 0 )
      return null;

    return new File( m_path );
  }

  public void setFile( final File file )
  {
    final String path = file == null ? "" : file.getAbsolutePath();

    if( m_text == null )
      m_path = path;
    else
    {
      final String text = m_text.getText();
      if( !path.equals( text ) )
        m_text.setText( path );
    }
  }

  protected void textModified( final String text )
  {
    m_path = text;

    if( m_settings != null )
      m_settings.put( FileChooserGroup.SETTINGS_FILENAME, text );

    fireFileChanged( getFile() );
  }

  public IMessageProvider validate( )
  {
    return m_delegate.validate( getFile() );
  }
}
