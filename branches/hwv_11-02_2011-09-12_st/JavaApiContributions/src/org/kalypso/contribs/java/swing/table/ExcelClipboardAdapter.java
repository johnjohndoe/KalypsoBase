/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.contribs.java.swing.table;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.text.NumberFormat;
import java.util.StringTokenizer;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.KeyStroke;

/**
 * ExcelAdapter enables Copy-Paste Clipboard functionality on JTables. The clipboard data format used by the adapter is
 * compatible with the clipboard format used by Excel. This provides for clipboard interoperability between enabled
 * JTables and Excel.
 * <p>
 * This code is adapted from the Online Article at {see http://www.javaworld.com/javaworld/javatips/jw-javatip77.html}
 */
public final class ExcelClipboardAdapter
{
  public final static String CMD_COPY = "COPY";

  public final static String CMD_PASTE = "PASTE";

  protected final JTable m_table;

  protected final NumberFormat m_nf;

  private final KeyStroke m_ksCopy;

  private final KeyStroke m_ksPaste;

  private final CopyAction m_copyAction;

  private final PasteAction m_pasteAction;

  /**
   * The Excel Adapter is constructed with a JTable on which it enables Copy-Paste and acts as a Clipboard listener.
   */
  public ExcelClipboardAdapter( final JTable table, final NumberFormat nf )
  {
    m_table = table;
    m_nf = nf;

    m_ksCopy = KeyStroke.getKeyStroke( KeyEvent.VK_C, ActionEvent.CTRL_MASK, false );

    m_ksPaste = KeyStroke.getKeyStroke( KeyEvent.VK_V, ActionEvent.CTRL_MASK, false );

    m_table.getInputMap().put( m_ksCopy, CMD_COPY );
    m_copyAction = new CopyAction();
    m_table.getActionMap().put( CMD_COPY, m_copyAction );
    m_table.getInputMap().put( m_ksPaste, CMD_PASTE );
    m_pasteAction = new PasteAction();
    m_table.getActionMap().put( CMD_PASTE, m_pasteAction );
  }

  public void dispose()
  {
    m_table.getInputMap().remove( m_ksCopy );
    m_table.getActionMap().remove( CMD_COPY );
    m_table.getInputMap().remove( m_ksPaste );
    m_table.getActionMap().remove( CMD_PASTE );
  }

  public CopyAction getCopyAction()
  {
    return m_copyAction;
  }

  public PasteAction getPasteAction()
  {
    return m_pasteAction;
  }

  private class CopyAction extends AbstractAction
  {
    public CopyAction()
    {
      super( "Kopieren" );
    }

    @Override
    public void actionPerformed( final ActionEvent e )
    {
      // Check to ensure we have selected only a contiguous block of cells
      final int numcols = m_table.getSelectedColumnCount();
      final int numrows = m_table.getSelectedRowCount();
      final int[] rowsselected = m_table.getSelectedRows();
      final int[] colsselected = m_table.getSelectedColumns();
      if( !( ( numrows - 1 == rowsselected[rowsselected.length - 1] - rowsselected[0] && numrows == rowsselected.length ) && ( numcols - 1 == colsselected[colsselected.length - 1]
          - colsselected[0] && numcols == colsselected.length ) ) )
      {
        JOptionPane.showMessageDialog( null, "Ungültige Copy Selektion", "Ungültige Copy Selektion",
            JOptionPane.ERROR_MESSAGE );
        return;
      }

      final StringBuffer sbf = new StringBuffer();
      for( int i = 0; i < numrows; i++ )
      {
        for( int j = 0; j < numcols; j++ )
        {
          final Object value = m_table.getValueAt( rowsselected[i], colsselected[j] );
          sbf.append( m_nf.format( value ) );

          if( j < numcols - 1 )
            sbf.append( "\t" );
        }

        sbf.append( "\n" );
      }

      final StringSelection stsel = new StringSelection( sbf.toString() );
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents( stsel, stsel );
    }
  }

  private class PasteAction extends AbstractAction
  {
    public PasteAction()
    {
      super( "Einfügen" );
    }

    @Override
    public void actionPerformed( final ActionEvent e )
    {
      final int startRow = ( m_table.getSelectedRows() )[0];
      final int startCol = ( m_table.getSelectedColumns() )[0];
      try
      {
        final String trstring = (String)( Toolkit.getDefaultToolkit().getSystemClipboard().getContents( this )
            .getTransferData( DataFlavor.stringFlavor ) );

        final StringTokenizer st1 = new StringTokenizer( trstring, "\n" );

        for( int i = 0; st1.hasMoreTokens(); i++ )
        {
          final String rowstring = st1.nextToken();

          final StringTokenizer st2 = new StringTokenizer( rowstring, "\t" );
          for( int j = 0; st2.hasMoreTokens(); j++ )
          {
            final String nextToken = st2.nextToken();

            final int rowIndex = startRow + i;
            if( rowIndex < m_table.getRowCount() && startCol + j < m_table.getColumnCount() )
            {
              final int columnIndex = startCol + j;
              if( m_table.isCellEditable( rowIndex, columnIndex ) )
              {
                final Object value = m_nf.parseObject( nextToken );
                m_table.setValueAt( value, rowIndex, columnIndex );
              }
            }
          }
        }
      }
      catch( final Exception ex )
      {
        ex.printStackTrace();
      }
      finally
      {
        m_table.repaint();
      }
    }
  }
}