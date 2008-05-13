package com.bce.apps.wspfli;

import java.math.BigDecimal;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import org.kalypso.gml.processes.i18n.Messages;

/**
 * TableModel für die Grenzen
 * 
 * @author belger
 */
public class GrenzenModel extends AbstractTableModel
{
  final Vector<BigDecimal> m_data = new Vector<BigDecimal>();

  public GrenzenModel( final double[] grenzen )
  {
    setData( grenzen );
  }

  public void setData( final double[] grenzen )
  {
    m_data.clear();

    if( grenzen != null )
    {
      for( int i = 0; i < grenzen.length; i++ )
        m_data.add( new BigDecimal( grenzen[i] ).setScale( 2, BigDecimal.ROUND_HALF_UP ) );
    }

    fireTableStructureChanged();
  }

  /**
   * @see javax.swing.table.TableModel#getRowCount()
   */
  public int getRowCount( )
  {
    return m_data.size();
  }

  /**
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  public int getColumnCount( )
  {
    return 1;
  }

  /**
   * @see javax.swing.table.TableModel#getColumnName(int)
   */
  @Override
  public String getColumnName( int columnIndex )
  {
    checkColumn( columnIndex );

    return Messages.getString("GrenzenModel.0"); //$NON-NLS-1$
  }

  /**
   * @see javax.swing.table.TableModel#getColumnClass(int)
   */
  @Override
  public Class< ? > getColumnClass( int columnIndex )
  {
    checkColumn( columnIndex );

    return BigDecimal.class;
  }

  /**
   * @see javax.swing.table.TableModel#isCellEditable(int, int)
   */
  @Override
  public boolean isCellEditable( int rowIndex, int columnIndex )
  {
    checkRow( rowIndex );
    checkColumn( columnIndex );

    return true;
  }

  /**
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  public Object getValueAt( int rowIndex, int columnIndex )
  {
    checkRow( rowIndex );
    checkColumn( columnIndex );

    return m_data.elementAt( rowIndex );
  }

  /**
   * @see javax.swing.table.TableModel#setValueAt(java.lang.Object, int, int)
   */
  @Override
  public void setValueAt( Object aValue, int rowIndex, int columnIndex )
  {
    checkRow( rowIndex );
    checkColumn( columnIndex );

    m_data.setElementAt( (BigDecimal) aValue, rowIndex );
  }

  public void addRow( final double step )
  {
    final double newVal = (m_data.size() == 0 ? step : m_data.get( m_data.size() - 1 ).doubleValue() + step);

    m_data.add( new BigDecimal( newVal ).setScale( 2, BigDecimal.ROUND_HALF_UP ) );

    fireTableRowsInserted( m_data.size() - 1, m_data.size() - 1 );
  }

  public void removeRow( final int rowIndex )
  {
    checkRow( rowIndex );

    m_data.remove( rowIndex );

    fireTableRowsDeleted( rowIndex, rowIndex );
  }

  private void checkColumn( final int index ) throws IllegalArgumentException
  {
    if( index != 0 )
      throw new IllegalArgumentException( Messages.getString("GrenzenModel.1") + index ); //$NON-NLS-1$
  }

  private void checkRow( final int index ) throws IllegalArgumentException
  {
    if( index < 0 || index >= m_data.size() )
      throw new IllegalArgumentException( Messages.getString("GrenzenModel.2") + index ); //$NON-NLS-1$
  }

  public double[] getData( )
  {
    final double[] grenzen = new double[m_data.size()];

    for( int i = 0; i < m_data.size(); i++ )
    {
      final BigDecimal val = m_data.get( i );
      grenzen[i] = val.doubleValue();
    }

    return grenzen;
  }

  public void addFront( final double step )
  {
    final double newVal = (m_data.size() == 0 ? -step : m_data.get( 0 ).doubleValue() - step);

    m_data.add( 0, new BigDecimal( newVal ).setScale( 2, BigDecimal.ROUND_HALF_UP ) );

    fireTableRowsInserted( 0, 0 );
  }

}
