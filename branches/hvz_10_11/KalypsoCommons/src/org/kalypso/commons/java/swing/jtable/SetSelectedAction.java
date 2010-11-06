package org.kalypso.commons.java.swing.jtable;

import java.awt.event.ActionEvent;

import javax.swing.JTable;

import org.kalypso.commons.internal.i18n.Messages;

/**
 * SetAllAction
 *
 * @author schlienger
 */
public class SetSelectedAction extends AbstractObservationTableAction
{
  public SetSelectedAction( final JTable table )
  {
    super( table, Messages.getString("org.kalypso.commons.java.swing.jtable.SetSelectedAction.0"), Messages.getString("org.kalypso.commons.java.swing.jtable.SetSelectedAction.1") ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public void internalActionPerformed( final ActionEvent e )
  {
    final JTable table = getTable();
    final int col = table.getSelectedColumn();
    final int row = table.getSelectedRow();
    final int[] rows = table.getSelectedRows();
    final Object value = table.getValueAt( row, col );

    for( final int row2 : rows )
      table.setValueAt( value, row2, col );
  }
}