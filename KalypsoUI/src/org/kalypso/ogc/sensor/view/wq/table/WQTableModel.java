/*--------------- Kalypso-Header ------------------------------------------

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

 --------------------------------------------------------------------------*/

package org.kalypso.ogc.sensor.view.wq.table;

import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.kalypso.ogc.sensor.timeseries.TimeseriesUtils;

/**
 * @author schlienger
 */
public class WQTableModel extends AbstractTableModel implements TableModel
{
  private final Double m_startW;

  private final Double[] m_Q;

  private final int m_indexOffset;

  private final String m_fromType;

  private final String m_toType;

  public WQTableModel( final String fromType, final String toType, final Double startW, final Double[] Q )
  {
    m_fromType = fromType;
    m_toType = toType;
    m_startW = startW;
    m_Q = Q;

    final int w = m_startW.intValue() / 10 * 10;

    m_indexOffset = w - m_startW.intValue();
  }

  /**
   * @see javax.swing.table.TableModel#getColumnCount()
   */
  @Override
  public int getColumnCount( )
  {
    return 11;
  }

  /**
   * @see javax.swing.table.TableModel#getColumnClass(int)
   */
  @Override
  public Class< ? > getColumnClass( final int columnIndex )
  {
    if( columnIndex == 0 )
      return Integer.class;

    return Number.class;
  }

  /**
   * @see javax.swing.table.TableModel#getColumnName(int)
   */
  @Override
  public String getColumnName( final int columnIndex )
  {
    if( columnIndex == 0 )
    {
      final String fromUnit = TimeseriesUtils.getUnit( m_fromType );
      return String.format( "%s [%s]", m_fromType, fromUnit ); //$NON-NLS-1$
    }

// final String toUnit = TimeseriesUtils.getUnit( m_toType );
// return String.format( "%s%d [%s]", m_toType, columnIndex, toUnit );
    // TODO: makes the columns too big; instead we should show an info with 'all Q in m�/s' or similar
    return String.format( "%s%d", m_toType, columnIndex ); //$NON-NLS-1$
  }

  /**
   * @see javax.swing.table.TableModel#getRowCount()
   */
  @Override
  public int getRowCount( )
  {
    return m_Q.length / 10 + (m_Q.length % 10 == 0 ? 0 : 1) + 1;
  }

  /**
   * @see javax.swing.table.TableModel#getValueAt(int, int)
   */
  @Override
  public Object getValueAt( final int rowIndex, final int columnIndex )
  {
    if( columnIndex == 0 )
    {
      final int w = m_startW.intValue() + rowIndex * 10;
      return new Double( w / 10 * 10 );
    }

    int ix = rowIndex * 10 + columnIndex - 1;
    if( ix < m_indexOffset )
      return null;

    ix += m_indexOffset;
    if( ix < 0 || ix >= m_Q.length )
      return null;

    return m_Q[ix];
  }
}
