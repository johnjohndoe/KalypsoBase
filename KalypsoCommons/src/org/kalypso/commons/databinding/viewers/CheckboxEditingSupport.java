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
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.contribs.eclipse.jface.viewers.CheckboxCellEditor;

/**
 * @author Holger Albert
 */
public class CheckboxEditingSupport extends ObservableValueEditingSupport
{
  private IValueProperty m_property;

  private CheckboxCellEditor m_cellEditor;

  public CheckboxEditingSupport( ColumnViewer viewer, DataBindingContext dbc, IValueProperty property )
  {
    super( viewer, dbc );

    m_property = property;
    m_cellEditor = new CheckboxCellEditor( (Composite) viewer.getControl() );
  }

  /**
   * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
   */
  @Override
  public CheckboxCellEditor getCellEditor( Object element )
  {
    return m_cellEditor;
  }

  /**
   * @see org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport#doCreateCellEditorObservable(org.eclipse.jface.viewers.CellEditor)
   */
  @Override
  protected IObservableValue doCreateCellEditorObservable( CellEditor cellEditor )
  {
    return SWTObservables.observeSelection( m_cellEditor.getControl() );
  }

  /**
   * @see org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport#doCreateElementObservable(java.lang.Object,
   *      org.eclipse.jface.viewers.ViewerCell)
   */
  @Override
  protected IObservableValue doCreateElementObservable( Object element, ViewerCell cell )
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
        CheckboxEditingSupport.this.getViewer().refresh();
      }
    } );
    return observeableValue;
  }
}