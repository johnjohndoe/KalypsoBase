package org.kalypso.commons.java.swing.jtable;

import java.awt.event.ActionEvent;

import javax.swing.JTable;

import org.kalypso.commons.internal.i18n.Messages;

/**
 * SetAllAction
 *
 * @author schlienger
 */
public class SetTopAction extends AbstractObservationTableAction
{
  public SetTopAction( final JTable table )
  {
    super( table, Messages.getString("org.kalypso.commons.java.swing.jtable.SetTopAction.0"), Messages.getString("org.kalypso.commons.java.swing.jtable.SetTopAction.1") ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  @Override
  public void internalActionPerformed( final ActionEvent e )
  {
    final JTable table = getTable();
    final int col = table.getSelectedColumn();
    final int row = table.getSelectedRow();
    final Object value = table.getValueAt( row, col );

    for( int i = 0; i < row; i++ )
      table.setValueAt( value, i, col );
  }
}