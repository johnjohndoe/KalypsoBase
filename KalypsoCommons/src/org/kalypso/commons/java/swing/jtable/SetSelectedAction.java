package org.kalypso.commons.java.swing.jtable;

import java.awt.event.ActionEvent;

import javax.swing.JTable;

/**
 * SetAllAction
 *
 * @author schlienger
 */
public class SetSelectedAction extends AbstractObservationTableAction
{
  public SetSelectedAction( final JTable table )
  {
    super( table, "Selektierte Werte setzen", "Setzt die selektierten Werte auf den aktiven Wert" );
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