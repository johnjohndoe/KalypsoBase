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
package org.kalypso.ui.controls.files;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Text;
import org.kalypso.ui.controls.files.listener.IFileChooserListener;

/**
 * Composite for selecting a file. It is able to create its controls on a composite or group. It will not use dialog
 * settings or other means to store the entered information. It is possible to be informed of the changes by a listener.
 * The selected path can also be set/returned explicitly by functions.
 * 
 * @author Holger Albert
 */
public class FileChooserComposite extends Composite
{
  /**
   * Style constant. If set, no group is shown in this composite.
   */
  public static final int NO_GROUP = SWT.SEARCH;

  /**
   * The listener.
   */
  private List<IFileChooserListener> m_listener;

  /**
   * The file extension filter.
   */
  protected String[] m_extensions;

  /**
   * The list of filter names, or null for no filter names.
   */
  protected String[] m_names;

  /**
   * The title.
   */
  protected String m_title;

  /**
   * The GUI element for entering the path.
   */
  protected Text m_pathText;

  /**
   * The selected path.
   */
  protected String m_path;

  /**
   * The constructor.
   * 
   * @param parent
   *          A widget which will be the parent of the new instance (cannot be null).
   * @param style
   *          The style of widget to construct.
   * @param extensions
   *          The file extension filter.
   * @param names
   *          The list of filter names, or null for no filter names.
   * @param title
   *          The title.
   * @param defaultPath
   *          If not null, this path will be set as default.
   */
  public FileChooserComposite( Composite parent, int style, String[] extensions, String[] names, String title, String defaultPath )
  {
    super( parent, style );

    /* Initialize. */
    m_listener = new ArrayList<IFileChooserListener>();
    m_extensions = extensions;
    m_names = names;
    m_title = title;
    if( m_title == null || m_title.length() == 0 )
      m_title = "Pfad";
    m_pathText = null;
    m_path = defaultPath;

    /* Create the controls. */
    createControls();
  }

  /**
   * This function creates the controls.
   */
  private void createControls( )
  {
    super.setLayout( new FillLayout() );

    /* Create the main group for the panel. */
    Composite main = createMainComposite();

    /* No group? */
    if( (getStyle() & NO_GROUP) != 0 )
    {
      /* Create a label. */
      Label pathLabel = new Label( main, SWT.NONE );
      pathLabel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
      pathLabel.setText( m_title );
    }

    /* Create a text field. */
    m_pathText = new Text( main, SWT.BORDER );
    GridData pathData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    pathData.widthHint = 350;
    m_pathText.setLayoutData( pathData );
    if( m_path != null )
      m_pathText.setText( m_path );

    /* Add a listener. */
    m_pathText.addModifyListener( new ModifyListener()
    {
      /**
       * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
       */
      @Override
      public void modifyText( ModifyEvent e )
      {
        /* Get the source. */
        Text source = (Text) e.getSource();

        /* Store the text. */
        m_path = source.getText();

        /* Fire the path changed event. */
        firePathChangedEvent( m_path );
      }
    } );

    /* Create a button. */
    Button pathButton = new Button( main, SWT.NONE );
    pathButton.setLayoutData( new GridData( SWT.END, SWT.CENTER, false, false ) );
    pathButton.setText( "..." );

    /* Add a listener. */
    pathButton.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( SelectionEvent e )
      {
        /* Get the source. */
        Button source = (Button) e.getSource();

        /* Create the dialog. */
        FileDialog dialog = new FileDialog( source.getParent().getShell(), SWT.OPEN );
        dialog.setText( m_title );
        File f = new File( m_pathText.getText() );
        dialog.setFilterPath( f.getPath() );
        dialog.setFileName( "" );

        /* Only set the file extension filter, if one if available. */
        if( m_extensions != null && m_extensions.length > 0 )
        {
          /* Set the file extension filter. */
          dialog.setFilterExtensions( m_extensions );

          /* Only set the filter names, if the file extensions were updated, too. */
          /* There must be the equal number of filter names then file extensions. */
          if( m_names != null && m_names.length == m_extensions.length )
            dialog.setFilterNames( m_names );
        }

        /* Get the selection of the user. */
        String path = dialog.open();
        if( path == null || path.length() == 0 )
          return;

        /* Adjust the text field. */
        m_pathText.setText( path );
      }
    } );
  }

  /**
   * This function creates the main composite. It takes the decision for a border into account.
   * 
   * @return The main composite.
   */
  private Composite createMainComposite( )
  {
    if( (getStyle() & NO_GROUP) != 0 )
    {
      Composite main = new Composite( this, SWT.NONE );
      GridLayout mainLayout = new GridLayout( 3, false );
      mainLayout.marginHeight = 0;
      mainLayout.marginWidth = 0;
      main.setLayout( mainLayout );

      return main;
    }

    Group main = new Group( this, SWT.NONE );
    main.setLayout( new GridLayout( 2, false ) );
    main.setText( m_title );

    return main;
  }

  /**
   * @see org.eclipse.swt.widgets.Composite#setLayout(org.eclipse.swt.widgets.Layout)
   */
  @Override
  public void setLayout( Layout layout )
  {
    /* Ignore user set layouts, only layout datas are permitted. */
  }

  /**
   * @see org.eclipse.swt.widgets.Widget#dispose()
   */
  @Override
  public void dispose( )
  {
    super.dispose();

    m_listener = null;
    m_title = null;
    m_pathText = null;
    m_path = null;
  }

  /**
   * This function fires the path changed event.
   * 
   * @param path
   *          The new path.
   */
  protected void firePathChangedEvent( String path )
  {
    for( IFileChooserListener listener : m_listener )
      listener.pathChanged( path );
  }

  /**
   * This function adds a file chooser listener.
   * 
   * @param listener
   *          The file chooser listener.
   */
  public void addFileChooserListener( IFileChooserListener listener )
  {
    if( !m_listener.contains( listener ) )
      m_listener.add( listener );
  }

  /**
   * This function removes a file chooser listener.
   * 
   * @param listener
   *          The file chooser listener.
   */
  public void removeFileChooserListener( IFileChooserListener listener )
  {
    if( m_listener.contains( listener ) )
      m_listener.remove( listener );
  }

  /**
   * This function sets the selected path.
   * 
   * @param path
   *          The selected path.
   */
  public void setSelectedPath( String path )
  {
    if( m_pathText == null || m_pathText.isDisposed() )
      return;

    m_pathText.setText( path );
  }

  /**
   * This function returns the selected path.
   * 
   * @return The selected path.
   */
  public String getSelectedPath( )
  {
    return m_path;
  }
}