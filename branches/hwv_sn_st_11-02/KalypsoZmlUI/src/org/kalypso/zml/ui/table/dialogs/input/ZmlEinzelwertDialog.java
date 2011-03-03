/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  DenickestraÃŸe 22
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

import java.util.Date;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.sensor.IAxis;
import org.kalypso.ogc.sensor.ITupleModel;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.VALUE_STATUS;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.dialogs.EnhancedTitleAreaDialog;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlEinzelwertDialog extends EnhancedTitleAreaDialog implements IZmlEinzelwertCompositeListener
{
  private static final String SCREEN_SIZE = "zml.input.dialog.screen.size"; // $NON-NLS-1$

  private final ZmlEinzelwertModel m_model;

  private ZmlEinzelwertComposite m_composite;

  private final IZmlTableColumn m_column;

  public ZmlEinzelwertDialog( final Shell shell, final IZmlTableColumn column )
  {
    super( shell );
    m_column = column;
    m_model = new ZmlEinzelwertModel( column );

    setShellStyle( SWT.CLOSE | SWT.MAX | SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL | SWT.RESIZE );

    setHelpAvailable( false );
  }

  /**
   * @see org.eclipse.jface.dialogs.TitleAreaDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
   */
  @Override
  protected final Control createDialogArea( final Composite parent )
  {
    getShell().setText( "Eingabe von Einzelwerten" );

    setTitle( String.format( "Bearbeiten der Zeitreihe: \"%s\"", m_model.getLabel() ) );
    setMessage( "In diesem Dialog können Sie Änderungen an bestehenden Werten vornehmen." );

    final FormToolkit toolkit = new FormToolkit( parent.getDisplay() );

    final Composite base = toolkit.createComposite( parent, SWT.NULL );
    base.setLayout( new GridLayout() );

    final Point screen = getScreenSize( SCREEN_SIZE );

    final GridData data = new GridData( GridData.FILL, GridData.FILL, true, true );
    data.widthHint = screen.x;
    data.heightHint = screen.y;
    base.setLayoutData( data );

    base.addControlListener( new ControlAdapter()
    {
      /**
       * @see org.eclipse.swt.events.ControlAdapter#controlResized(org.eclipse.swt.events.ControlEvent)
       */
      @Override
      public void controlResized( final ControlEvent e )
      {
        setScreenSize( SCREEN_SIZE, base.getSize() );
      }
    } );

    m_composite = new ZmlEinzelwertComposite( base, toolkit, m_model );
    m_composite.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

    toolkit.adapt( parent );

    m_composite.addListener( this );

    return super.createDialogArea( parent );
  }

  /**
   * @see org.eclipse.jface.dialogs.Dialog#okPressed()
   */
  @Override
  protected void okPressed( )
  {
    if( !m_composite.isValid() )
    {
      MessageDialog.openError( getParentShell(), "Ungültige Eingabe", "Eine Verarbeitung ist nicht möglich, da Tabelle eine ungültige Eingabe enthält." );
      return;
    }

    saveChanges();

    super.okPressed();
  }

  private void saveChanges( )
  {
    final IZmlModelColumn modelColumn = m_column.getModelColumn();

    final ZmlEinzelwert[] rows = m_model.getRows();
    for( final ZmlEinzelwert row : rows )
    {
      try
      {
        final int index = findIndex( row, modelColumn );
        modelColumn.update( index, row.getValue(), VALUE_STATUS.eManual );
      }
      catch( final Throwable t )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }
  }

  private int findIndex( final ZmlEinzelwert row, final IZmlModelColumn modelColumn ) throws SensorException
  {
    final Date date = row.getDate();

    final ITupleModel model = modelColumn.getTupleModel();
    final IAxis axis = modelColumn.getIndexAxis();

    for( int index = 0; index < model.size(); index++ )
    {
      try
      {
        final Object objModelDate = model.get( index, axis );
        if( objModelDate instanceof Date )
        {
          final Date modelDate = (Date) objModelDate;
          if( modelDate.equals( date ) )
            return index;
        }
      }
      catch( final Throwable t )
      {
        KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
      }
    }

    return -1;
  }

  /**
   * @see org.kalypso.zml.ui.table.dialogs.input.IZmlEinzelwertCompositeListener#inputChanged(boolean)
   */
  @Override
  public void inputChanged( final boolean valid )
  {
    final Button button = getButton( OK );
    if( button != null )
      button.setEnabled( valid );
  }
}
