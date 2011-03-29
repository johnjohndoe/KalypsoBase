package org.kalypso.commons.databinding.viewers;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
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

  public ComboViewerEditingSupport( final ColumnViewer viewer, final DataBindingContext dbc, final IValueProperty property, final int style )
  {
    super( viewer, dbc );

    m_property = property;
    m_cellEditor = new ComboBoxViewerCellEditor( (Composite) viewer.getControl(), style );
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
    return m_property.observe( element );
  }
}