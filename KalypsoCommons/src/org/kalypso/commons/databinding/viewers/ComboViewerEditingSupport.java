package org.kalypso.commons.databinding.viewers;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.IValueChangeListener;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxViewerCellEditor;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Gernot Belger
 */
public class ComboViewerEditingSupport extends ObservableValueEditingSupport
{
  private final ComboBoxViewerCellEditor m_cellEditor;

  private final IValueProperty m_property;

  private IValueChangeListener m_changeListener;

  public ComboViewerEditingSupport( final ColumnViewer viewer, final DataBindingContext dbc, final IValueProperty property, final int style )
  {
    super( viewer, dbc );

    m_property = property;
    m_cellEditor = new ComboBoxViewerCellEditor( (Composite) viewer.getControl(), style );
  }

  /**
   * @param changeListener
   *          (Optional) listener. If set to non-<code>null</code>, this listener will be informed about value changes
   *          comming from this editor.
   */
  public void setValueChangeListener( IValueChangeListener changeListener )
  {
    m_changeListener = changeListener;
  }

  /**
   * @see org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
   */
  @Override
  public ComboBoxViewerCellEditor getCellEditor( final Object element )
  {
    return m_cellEditor;
  }

  /**
   * @see org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport#doCreateCellEditorObservable(org.eclipse.jface.viewers.CellEditor)
   */
  @Override
  protected IObservableValue doCreateCellEditorObservable( final CellEditor cellEditor )
  {
    return ViewerProperties.singleSelection().observe( m_cellEditor.getViewer() );
  }

  /**
   * @see org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport#doCreateElementObservable(java.lang.Object,
   *      org.eclipse.jface.viewers.ViewerCell)
   */
  @Override
  protected IObservableValue doCreateElementObservable( final Object element, final ViewerCell cell )
  {
    IObservableValue observe = m_property.observe( element );
    if( m_changeListener != null )
      observe.addValueChangeListener( m_changeListener );
    return observe;
  }
}