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

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.SelectionDialog;
import org.kalypso.contribs.eclipse.i18n.Messages;

/**
 * A group for choosing a resource from the workspace.
 * <p>
 * This class is intended to be used in a wizard page.
 * </p>
 * 
 * @author Gernot Belger
 */
public class ResourceChooserGroup
{
  private static final String SETTINGS_PATH = "settings.path"; //$NON-NLS-1$

  private IPath m_path;

  private IDialogSettings m_dialogSettings = null;

  private final IUpdateable m_updateable;

  private final String m_title;

  private final String m_label;

  private SelectionDialog m_dialog = null;

  public ResourceChooserGroup( final IUpdateable updateable, final String title, final String label )
  {
    m_updateable = updateable;
    m_title = title;
    m_label = label;
  }

  public void setDialogSettings( final IDialogSettings dialogSettings )
  {
    m_dialogSettings = dialogSettings;
  }
  
  public void setSelectionDialog( final SelectionDialog dialog )
  {
    m_dialog = dialog;
  }

  public Control createControl( final Composite parent )
  {
    final String lastPathName = m_dialogSettings == null ? null : m_dialogSettings.get( SETTINGS_PATH );

    final Group group = new Group( parent, SWT.NONE );
    group.setLayout( new GridLayout( 3, false ) );
    group.setText( m_title );

    /* theme chooser */
    new Label( group, SWT.NONE ).setText( m_label );
    final Text text = new Text( group, SWT.BORDER );
    text.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    text.setEditable( false );

    final Button button = new Button( group, SWT.NONE );
    button.setText( Messages.getString("org.kalypso.contribs.eclipse.jface.wizard.ResourceChooserGroup.1") ); //$NON-NLS-1$
    button.setToolTipText( Messages.getString("org.kalypso.contribs.eclipse.jface.wizard.ResourceChooserGroup.2") ); //$NON-NLS-1$
    button.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        buttonPressed( text );
      }
    } );

    if( lastPathName != null )
      setPath( text, Path.fromPortableString( lastPathName ) );

    return group;
  }

  protected void buttonPressed( final Text text )
  {
    if( m_dialog.open() != Window.OK )
      return;

    final Object[] result = m_dialog.getResult();
    final IPath path = result.length == 0 ? null : (IPath) result[0];
    setPath( text, path );
  }

  private void setPath( final Text text, final IPath path )
  {
    m_path = path;
    text.setText( m_path == null ? "" : m_path.toOSString() ); //$NON-NLS-1$

    if( m_dialogSettings != null )
      m_dialogSettings.put( SETTINGS_PATH, path.toPortableString() );

    m_updateable.update();
  }

  public IPath getPath( )
  {
    return m_path;
  }

}
