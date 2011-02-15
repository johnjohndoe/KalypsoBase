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
package org.kalypso.contribs.eclipse.jface.dialog;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

/**
 * @author doemming
 */
public class ScrolledTextInformationDialog extends TitleAreaDialog
{
  private final String m_content;

  private final String m_message;

  private final String m_title;

  public ScrolledTextInformationDialog( final Shell parentShell, final String title, final String message, String content )
  {
    super( parentShell );
    setShellStyle( getShellStyle() | SWT.RESIZE );
    m_title = title;
    m_message = message;
    m_content = content;
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( final Composite parent )
  {
    setTitle( m_title );
    setMessage( m_message );

    final GridData gridData = new GridData( GridData.FILL_BOTH );
    gridData.grabExcessHorizontalSpace = true;
    gridData.grabExcessVerticalSpace = true;

    final GridData gridData2 = new GridData( GridData.FILL_BOTH );
    gridData2.grabExcessHorizontalSpace = true;
    gridData2.grabExcessVerticalSpace = true;

    final Composite top = (Composite) super.createDialogArea( parent );
    top.setLayout( new GridLayout() );

    final Text text = new Text( top, SWT.READ_ONLY | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL );
    text.setText( m_content );
    text.setLayoutData( gridData2 );
    text.setEnabled( true );
    text.setVisible( true );
    return top;
  }
}
