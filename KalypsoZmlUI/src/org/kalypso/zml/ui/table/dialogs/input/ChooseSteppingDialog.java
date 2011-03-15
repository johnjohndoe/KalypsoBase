/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.zml.ui.table.dialogs.input;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author kuch
 */
public class ChooseSteppingDialog extends TitleAreaDialog
{
  protected Integer m_selection;

  private final FormToolkit m_toolkit;

  private final Integer[] m_steppings;

  protected final Date m_current;

  public ChooseSteppingDialog( final Shell parentShell, final ZmlEinzelwert current, final Integer[] steppings, final FormToolkit toolkit )
  {
    super( parentShell );

    m_current = current.getDate();

    m_steppings = steppings;
    m_toolkit = toolkit;
  }

  /**
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createContents(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createContents( final Composite parent )
  {
    final Control control = super.createContents( parent );

    setTitle( "Zeitschrittweite" );
    setMessage( "Bestimmen Sie die Zeitschrittweite für das nächste Eingabefeld" );

    return control;
  }

  /**
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected Control createDialogArea( final Composite parent )
  {
    final Composite composite = (Composite) super.createDialogArea( parent );
    composite.setLayout( new GridLayout() );

    if( ArrayUtils.isEmpty( m_steppings ) )
    {
      m_toolkit.createLabel( composite, "Keine gültiegen Zeitschrittweiten gefunden" );
      return composite;
    }

    m_toolkit.createLabel( composite, "Zeitschrittweite" );
    final ComboViewer viewer = new ComboViewer( composite, SWT.BORDER | SWT.READ_ONLY | SWT.SINGLE );
    viewer.getCombo().setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    viewer.setLabelProvider( new LabelProvider()
    {
      @Override
      public String getText( final Object element )
      {
        final Integer step = (Integer) element;

        final Calendar calendar = Calendar.getInstance();
        calendar.setTime( m_current );
        calendar.add( Calendar.HOUR_OF_DAY, step );

        final SimpleDateFormat sdf = new SimpleDateFormat( "dd.MM.yyyy HH:mm" ); //$NON-NLS-1$

        return String.format( "+ %02d Stunde(n)      (%s)", step, sdf.format( calendar.getTime() ) );
      }
    } );
    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.setInput( m_steppings );

    viewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        m_selection = (Integer) selection.getFirstElement();
      }
    } );

    viewer.setSelection( new StructuredSelection( m_steppings[0] ) );

    m_toolkit.adapt( composite );

    return composite;
  }

  public Integer getOffset( )
  {
    return m_selection;
  }
}
