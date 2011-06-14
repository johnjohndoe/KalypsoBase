package org.kalypso.commons.databinding.viewers;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.property.value.IValueProperty;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author Gernot Belger
 */
public class TextEditingSupport extends ObservableValidatingEditingSupport
{
  public TextEditingSupport( final ColumnViewer viewer, final DataBindingContext dbc, final IValueProperty property )
  {
    super( viewer, dbc, property, new TextCellEditor( (Composite) viewer.getControl() ) );
  }

  /**
   * @see org.eclipse.jface.databinding.viewers.ObservableValueEditingSupport#doCreateCellEditorObservable(org.eclipse.jface.viewers.CellEditor)
   */
  @Override
  protected IObservableValue doCreateCellEditorObservable( final CellEditor cellEditor )
  {
    return SWTObservables.observeText( cellEditor.getControl(), SWT.Modify );
  }
}