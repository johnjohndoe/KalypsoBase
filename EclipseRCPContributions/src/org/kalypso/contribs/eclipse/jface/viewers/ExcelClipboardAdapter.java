package org.kalypso.contribs.eclipse.jface.viewers;

import java.awt.Toolkit;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * ExcelAdapter enables Copy-Paste Clipboard functionality on JTables. The clipboard data format
 * used by the adapter is compatible with the clipboard format used by Excel. This provides for
 * clipboard interoperability between enabled JTables and Excel.
 * <p>
 * This code is adapted from the Online Article at {see
 * http://www.javaworld.com/javaworld/javatips/jw-javatip77.html}
 */
public final class ExcelClipboardAdapter
{
  private final TableViewer m_viewer;

  /**
   * The Excel Adapter is constructed with a JTable on which it enables Copy-Paste and acts as a
   * Clipboard listener.
   */
  public ExcelClipboardAdapter( final TableViewer viewer )
  {
    m_viewer = viewer;
  }

  /**
   * Copies the currently selected TableItems into the System-Clipboard. Non continous selections
   * will produce a continous paste.
   */
  public void doCopy( )
  {
    final Table table = m_viewer.getTable();

    final StringBuffer sbf = new StringBuffer();

    final int columnCount = table.getColumnCount();
    for( int i = 0; i < table.getItemCount(); i++ )
    {
      if( table.isSelected( i ) )
      {
        final TableItem item = table.getItem( i );
        for( int j = 0; j < columnCount; j++ )
        {
          final String text = item.getText( j );
          sbf.append( text );

          if( j < columnCount - 1 )
            sbf.append( "\t" );
        }
        sbf.append( "\n" );
      }
    }

    final StringSelection stsel = new StringSelection( sbf.toString() );
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents( stsel, stsel );
  }

  public void doPaste( final int startRow, final int startCol, final boolean refreshViewer )
  {
    final ICellModifier cellModifier = m_viewer.getCellModifier();
    final Object[] columnProperties = m_viewer.getColumnProperties();
    final Table table = m_viewer.getTable();

    final String trstring = getClipboardString();
    if( trstring == null )
      return;

    final StringTokenizer st1 = new StringTokenizer( trstring, "\n" );

    for( int i = 0; st1.hasMoreTokens(); i++ )
    {
      final int row = startRow + i;
      if( row >= table.getItemCount() )
        continue;

      final Object element = m_viewer.getElementAt( row );

      final String rowstring = st1.nextToken();

      final StringTokenizer st2 = new StringTokenizer( rowstring, "\t" );
      for( int j = 0; st2.hasMoreTokens(); j++ )
      {
        final int col = startCol + j;
        if( col >= columnProperties.length )
          continue;

        final String newText = st2.nextToken();

        final String property = "" + columnProperties[col];
        if( cellModifier.canModify( element, property ) )
          cellModifier.modify( element, property, newText );
      }
    }

    if( refreshViewer )
      ViewerUtilities.refresh( m_viewer, true );
  }

  /**
   * @return null, falls etwas nicht geklappt hat.
   */
  private String getClipboardString( )
  {
    try
    {
      return (String)(Toolkit.getDefaultToolkit().getSystemClipboard().getContents( this )
          .getTransferData( DataFlavor.stringFlavor ));
    }
    catch( Exception e )
    {
      Logger.getLogger( getClass().getName() ).log( Level.SEVERE,
          "Zwischenablage konnte nicht ausgelesen werden.", e );

      return "";
    }
  }
}