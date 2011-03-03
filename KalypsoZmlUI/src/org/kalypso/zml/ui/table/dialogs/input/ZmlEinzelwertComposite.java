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

import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.validators.IntegerInputValidator;
import org.kalypso.contribs.eclipse.swt.layout.LayoutHelper;
import org.kalypso.ogc.sensor.SensorException;
import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.references.IZmlValueReference;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.base.widgets.EnhancedComboViewer;
import org.kalypso.zml.ui.table.base.widgets.EnhancedTextBox;
import org.kalypso.zml.ui.table.base.widgets.IAbstractEnhancedWidget;
import org.kalypso.zml.ui.table.base.widgets.IAbstractEnhancedWidgetChangeListener;
import org.kalypso.zml.ui.table.base.widgets.IEnhancedTextBoxListener;
import org.kalypso.zml.ui.table.base.widgets.rules.DateWidgetRule;
import org.kalypso.zml.ui.table.base.widgets.rules.DoubeValueWidgetRule;
import org.kalypso.zml.ui.table.base.widgets.rules.TimeWidgetRule;
import org.kalypso.zml.ui.table.dialogs.input.worker.FindNextValueVisitor;

/**
 * @author Dirk Kuch
 */
public class ZmlEinzelwertComposite extends Composite implements IZmlEinzelwertModelListener, IAbstractEnhancedWidgetChangeListener
{
  private static final Image IMG_ADD = new Image( null, ZmlEinzelwertComposite.class.getResourceAsStream( "icons/add.png" ) );

  private static final Image IMG_ADD_ONE = new Image( null, ZmlEinzelwertComposite.class.getResourceAsStream( "icons/add_one.png" ) );

  private static final Image IMG_REMOVE = new Image( null, ZmlEinzelwertComposite.class.getResourceAsStream( "icons/remove.png" ) );

  protected final ZmlEinzelwertModel m_model;

  private Composite m_base;

  private final FormToolkit m_toolkit;

  private final Set<IZmlEinzelwertCompositeListener> m_listeners = new LinkedHashSet<IZmlEinzelwertCompositeListener>();

  public ZmlEinzelwertComposite( final Composite parent, final FormToolkit toolkit, final ZmlEinzelwertModel model )
  {
    super( parent, SWT.NULL );
    m_toolkit = toolkit;
    m_model = model;

    setLayout( LayoutHelper.createGridLayout() );

    render();
    toolkit.adapt( this );

    m_model.addListener( this );
  }

  public void addListener( final IZmlEinzelwertCompositeListener listener )
  {
    m_listeners.add( listener );
  }

  protected void render( )
  {
    if( isDisposed() )
      return;

    if( m_base != null && !m_base.isDisposed() )
      m_base.dispose();

    m_base = m_toolkit.createComposite( this );
    m_base.setLayout( LayoutHelper.createGridLayout() );
    m_base.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

    final ScrolledForm form = m_toolkit.createScrolledForm( m_base );
    form.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

    final Composite body = form.getBody();
    body.setLayout( LayoutHelper.createGridLayout( 5 ) );

    m_toolkit.createLabel( body, "Datum" );
    m_toolkit.createLabel( body, "Uhrzeit" );
    m_toolkit.createLabel( body, "" );
    m_toolkit.createLabel( body, "Wert" );
    m_toolkit.createLabel( body, "" );

    final ZmlEinzelwert[] rows = m_model.getRows();
    for( final ZmlEinzelwert row : rows )
    {
      addRow( body, m_toolkit, row );
    }

    form.layout();
    form.reflow( true );

    this.layout();
  }

  private void addRow( final Composite base, final FormToolkit toolkit, final ZmlEinzelwert row )
  {
    try
    {
      final Date date = row.getDate();
      final Date[] existing = m_model.getExistingDateValues();
      final Date[] dayAnchors = getDayAnchors( existing );

      final EnhancedComboViewer<Date> viewerDay = new EnhancedComboViewer<Date>( base, toolkit, new DateWidgetRule() );
      viewerDay.addListener( this );
      row.addWidget( viewerDay );
      viewerDay.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
      viewerDay.setInput( dayAnchors );
      viewerDay.setSelection( findSelectedAnchor( row, dayAnchors ) );

      final EnhancedComboViewer<Date> viewerTime = new EnhancedComboViewer<Date>( base, toolkit, new TimeWidgetRule() );
      viewerTime.addListener( this );
      row.addWidget( viewerTime );
      viewerTime.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
      viewerTime.setFilter( new ViewerFilter()
      {
        @Override
        public boolean select( final Viewer viewer, final Object parentElement, final Object element )
        {
          if( element instanceof Date )
          {
            final Date d = (Date) element;

            final Date selection = viewerDay.getSelection();
            if( selection != null )
            {
              return ZmlEinzelwertHelper.compareDayAnchor( d, selection );
            }
          }

          return false;
        }
      } );

      viewerTime.setInput( existing );
      viewerTime.setSelection( date );

      viewerDay.addListener( new ISelectionChangedListener()
      {
        @Override
        public void selectionChanged( final SelectionChangedEvent event )
        {
          final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
          final Date selected = (Date) selection.getFirstElement();

          final Calendar baseCalendar = Calendar.getInstance();
          baseCalendar.setTime( date );

          final Calendar calendar = Calendar.getInstance();
          calendar.setTime( selected );

          calendar.set( Calendar.HOUR_OF_DAY, baseCalendar.get( Calendar.HOUR_OF_DAY ) );
          calendar.set( Calendar.MINUTE, baseCalendar.get( Calendar.MINUTE ) );
          calendar.set( Calendar.SECOND, baseCalendar.get( Calendar.SECOND ) );
          calendar.set( Calendar.MILLISECOND, baseCalendar.get( Calendar.MILLISECOND ) );

          row.setDate( calendar.getTime() );
        }
      } );

      viewerTime.addListener( new ISelectionChangedListener()
      {
        @Override
        public void selectionChanged( final SelectionChangedEvent event )
        {
          final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
          final Date selected = (Date) selection.getFirstElement();
          row.setDate( selected );
        }
      } );

      toolkit.createLabel( base, "" ).setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );

      final EnhancedTextBox<Double> textBox = new EnhancedTextBox<Double>( base, toolkit, new DoubeValueWidgetRule() );
      textBox.addListener( this );
      row.addWidget( textBox );
      textBox.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, false ) );
      textBox.setText( row.getValue() );

      textBox.addListener( new IEnhancedTextBoxListener<Double>()
      {
        @Override
        public void valueChanged( final Double value )
        {
          row.setValue( value );
        }
      } );

      addRowControls( base, toolkit, row );

    }
    catch( final SensorException e )
    {
      KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e ) );
    }
  }

  private void addRowControls( final Composite parent, final FormToolkit toolkit, final ZmlEinzelwert row )
  {
    final ToolBar toolBar = new ToolBar( parent, SWT.FLAT | SWT.RIGHT_TO_LEFT );
    final GridData layoutData = new GridData( GridData.END, GridData.FILL, false, false );
    layoutData.widthHint = layoutData.minimumWidth = 150;
    toolBar.setLayoutData( layoutData );
    toolkit.adapt( toolBar );

    final ToolItem itemRemove = new ToolItem( toolBar, SWT.PUSH );
    itemRemove.setImage( IMG_REMOVE );
    itemRemove.setToolTipText( "Wert entfernen" );

    itemRemove.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        try
        {
          m_model.removeRow( row );
        }
        catch( final Throwable t )
        {
          KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
        }
      }
    } );

    if( m_model.size() == 1 )
      itemRemove.setEnabled( false );

    final ToolItem itemAdd = new ToolItem( toolBar, SWT.PUSH );
    itemAdd.setImage( IMG_ADD );
    itemAdd.setToolTipText( "Eingabefeld hinzufügen" );

    itemAdd.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        final InputDialog dialog = new InputDialog( toolBar.getShell(), "Schrittweite", "Bitte geben Sie den Stunden-Offset (Schrittweite) des nächsten Wertes an", "1", new IntegerInputValidator() );
        final int status = dialog.open();
        if( Window.OK == status )
        {
          try
          {
            final Integer stepping = Integer.valueOf( dialog.getValue() );
            m_model.addRow( row.getDate(), stepping );
          }
          catch( final Throwable t )
          {
            KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
          }
        }
      }
    } );

    final ToolItem itemAddOne = new ToolItem( toolBar, SWT.PUSH );
    itemAddOne.setImage( IMG_ADD_ONE );
    itemAddOne.setToolTipText( "Eingabe nächster Wert" );

    itemAddOne.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        try
        {
          final IZmlModelColumn column = m_model.getColumn();
          final FindNextValueVisitor visitor = new FindNextValueVisitor( row );
          column.accept( visitor );

          final IZmlValueReference reference = visitor.getReference();
          if( Objects.isNotNull( reference ) )
          {
            final Object object = reference.getValue();
            if( object instanceof Number )
              m_model.addRow( new ZmlEinzelwert( m_model, (Date) reference.getIndexValue(), ((Number) object).doubleValue() ) );
            else
              m_model.addRow( new ZmlEinzelwert( m_model, (Date) reference.getIndexValue(), 0.0 ) );
          }

        }
        catch( final SensorException e1 )
        {
          KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( e1 ) );
        }
      }
    } );
  }

  private Date findSelectedAnchor( final ZmlEinzelwert row, final Date[] dayAnchors )
  {
    final Date base = row.getDate();
    if( base == null )
      return null;

    for( final Date anchor : dayAnchors )
    {
      if( ZmlEinzelwertHelper.compareDayAnchor( base, anchor ) )
        return anchor;
    }

    return null;
  }

  private Date[] getDayAnchors( final Date[] existing )
  {
    final Set<Date> anchors = new TreeSet<Date>();

    for( final Date date : existing )
    {
      final Calendar calendar = Calendar.getInstance();
      calendar.setTime( date );

      calendar.set( Calendar.HOUR_OF_DAY, 0 );
      calendar.set( Calendar.MINUTE, 0 );
      calendar.set( Calendar.MILLISECOND, 0 );

      anchors.add( calendar.getTime() );
    }

    return anchors.toArray( new Date[] {} );
  }

  /**
   * @see org.kalypso.zml.ui.table.dialogs.input.IZmlEinzelwertModelListener#modelChangedEvent()
   */
  @Override
  public void modelChangedEvent( )
  {
    new UIJob( "" )
    {
      @Override
      public final IStatus runInUIThread( final IProgressMonitor monitor )
      {
        render();

        return Status.OK_STATUS;
      }
    }.schedule();
  }

  public boolean isValid( )
  {
    final ZmlEinzelwert[] rows = m_model.getRows();
    for( final ZmlEinzelwert row : rows )
    {
      if( !row.isValid() )
        return false;
    }

    return true;
  }

  /**
   * @see org.kalypso.zml.ui.table.base.widgets.IAbstractEnhancedWidgetChangeListener#widgetChanged(org.kalypso.zml.ui.table.base.widgets.IAbstractEnhancedWidget)
   */
  @Override
  public void widgetChanged( final IAbstractEnhancedWidget widget )
  {
    final boolean valid = isValid();

    final IZmlEinzelwertCompositeListener[] listeners = m_listeners.toArray( new IZmlEinzelwertCompositeListener[] {} );
    for( final IZmlEinzelwertCompositeListener listener : listeners )
    {
      listener.inputChanged( valid );
    }
  }
}
