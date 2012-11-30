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
package org.kalypso.commons.databinding.swt;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;
import org.kalypso.commons.internal.i18n.Messages;

/**
 * A selection listener (to be added to a a button), that opens a {@link org.eclipse.swt.widgets.FileDialog} that
 * chooses a file.<br/>
 * When the file has been chosen, it will be set into a given observable value.
 * 
 * @author Gernot Belger
 */
public class FileValueSelectionListener implements SelectionListener
{
  private final List<String> m_names = new ArrayList<>();

  private final List<String[]> m_extensions = new ArrayList<>();

  private final String m_dialogTitle;

  private IObservableValue m_fileValue;

  private final int m_fileDialogStyle;

  /**
   * @param fileDialogStyle
   *          SWT.OPEN or SWT.SAVE. SWT.MULTI is not supported (should implement a different handler).
   */
  public FileValueSelectionListener( final IObservableValue fileValue, final String dialogTitle, final int fileDialogStyle )
  {
    if( fileValue != null )
      setFileValue( fileValue );
    m_dialogTitle = dialogTitle;
    m_fileDialogStyle = fileDialogStyle;
  }

  public void setFileValue( final IObservableValue value )
  {
    Assert.isTrue( value.getValueType() == File.class );

    m_fileValue = value;
  }

  @Override
  public void widgetSelected( final SelectionEvent e )
  {
    handleButtonPressed( e );
  }

  @Override
  public void widgetDefaultSelected( final SelectionEvent e )
  {
    handleButtonPressed( e );
  }

  private void handleButtonPressed( final SelectionEvent e )
  {
    final Shell shell = e.display.getActiveShell();

    final FileDialog dialog = new FileDialog( shell, m_fileDialogStyle );

    dialog.setText( m_dialogTitle );

    final File initialSelection = (File)m_fileValue.getValue();

    if( initialSelection != null )
    {
      dialog.setFilterPath( initialSelection.getParent() );
      dialog.setFileName( initialSelection.getName() );
    }

    dialog.setFilterNames( getFilterNames() );
    dialog.setFilterExtensions( getFilterExtension() );

    final String path = dialog.open();
    if( path == null )
      return;

    m_fileValue.setValue( new File( path ) );
  }

  /**
   * Adds the common filter for all files.
   */
  public void addAllFilter( )
  {
    addFilter( Messages.getString("FileValueSelectionListener_0"), "*.*" );  //$NON-NLS-1$//$NON-NLS-2$
  }

  /**
   * @param name
   *          The filter name, without the trailing extensions (i.e. 'All Files' instead of 'All Files (*.*)'.
   * @param extension
   *          One or more extension, this filter be showing. The extension must be complete i.e. of form *.txt or
   *          'myfile.bmp'.
   */
  public void addFilter( final String name, final String... extensions )
  {
    m_names.add( name );
    m_extensions.add( extensions );
  }

  private String[] getFilterExtension( )
  {
    final String[][] extensions = m_extensions.toArray( new String[m_extensions.size()][] );

    final String[] result = new String[extensions.length];
    for( int i = 0; i < result.length; i++ )
      result[i] = StringUtils.join( extensions[i], ';' );

    return result;
  }

  private String[] getFilterNames( )
  {
    final String[] names = new String[m_names.size()];
    for( int i = 0; i < names.length; i++ )
    {
      final String[] extensions = m_extensions.get( i );
      final String extensionText = StringUtils.join( extensions, ',' );
      names[i] = String.format( "%s (%s)", m_names.get( i ), extensionText ); //$NON-NLS-1$
    }
    return names;
  }
}