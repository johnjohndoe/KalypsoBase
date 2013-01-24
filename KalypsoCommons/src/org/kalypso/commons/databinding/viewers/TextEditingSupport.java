package org.kalypso.commons.databinding.viewers;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.ChangeEvent;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Gernot Belger
 */
public class TextEditingSupport extends ObservableValueEditingSupport
{
  private final TextCellEditor m_cellEditor;

  private final IValueProperty m_property;

  public TextEditingSupport( final ColumnViewer viewer, final DataBindingContext dbc, final IValueProperty property )
  {
    super( viewer, dbc );

    m_property = property;
    m_cellEditor = new TextCellEditor( (Composite) viewer.getControl() );
  }

  /**
   * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
   */
  @Override
  protected TextCellEditor getCellEditor( final Object element )
  {
    return m_cellEditor;
  }

  /**
   * @see org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport#doCreateCellEditorObservable(org.eclipse.jface.viewers.CellEditor)
   */
  @Override
  protected IObservableValue doCreateCellEditorObservable( final CellEditor cellEditor )
  {
    return SWTObservables.observeText( cellEditor.getControl(), SWT.Modify );
  }

  /**
   * @see org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport#doCreateElementObservable(java.lang.Object,
   *      org.eclipse.jface.viewers.ViewerCell)
   */
  @Override
  protected IObservableValue doCreateElementObservable( final Object element, final ViewerCell cell )
  {
    IObservableValue observeableValue = m_property.observe( element );
    observeableValue.addChangeListener( new IChangeListener()
    {
      /**
       * @see org.eclipse.core.databinding.observable.IChangeListener#handleChange(org.eclipse.core.databinding.observable.ChangeEvent)
       */
      @Override
      public void handleChange( ChangeEvent event )
      {
        TextEditingSupport.this.getViewer().refresh();
      }
    } );
    return observeableValue;
  }
}