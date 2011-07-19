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

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * A selection listener (to be added to a a button), that opens a {@link org.eclipse.swt.widgets.FileDialog} that
 * chooses a file.<br/>
 * When the file has been chosen, it will be set into a given observable value.
 * 
 * @author Gernot Belger
 */
public class DirectoryValueSelectionListener implements SelectionListener
{
  private final String m_dialogTitle;

  private IObservableValue m_dirValue;

  private final String m_dialogMessage;

  public DirectoryValueSelectionListener( final IObservableValue dirTextValue, final String dialogTitle, final String dialogMessage )
  {
    if( dirTextValue != null )
      setDirectoryValue( dirTextValue );

    m_dialogTitle = dialogTitle;
    m_dialogMessage = dialogMessage;
  }

  public void setDirectoryValue( final IObservableValue value )
  {
    Assert.isTrue( value.getValueType() == String.class );

    m_dirValue = value;
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

    final DirectoryDialog dialog = new DirectoryDialog( shell, SWT.SAVE );
    dialog.setText( m_dialogTitle );
    dialog.setMessage( m_dialogMessage );

    final String initialSelection = (String) m_dirValue.getValue();

    if( initialSelection != null )
      dialog.setFilterPath( initialSelection );

    final String selectedDirectory = dialog.open();
    if( selectedDirectory != null )
      m_dirValue.setValue( selectedDirectory );
  }
}