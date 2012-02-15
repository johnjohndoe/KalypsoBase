package org.kalypso.zml.ui.table.nat.layers;

import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.grid.layer.RowHeaderLayer;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.DataLayer;

import org.kalypso.zml.core.table.model.IZmlModelRow;

public class RowHeaderLayerStack extends AbstractLayerTransform
{

  private final IDataProvider m_provider;

  protected final BodyLayerStack m_bodyLayer;

  public RowHeaderLayerStack( final BodyLayerStack body )
  {
    m_bodyLayer = body;
    m_provider = new IDataProvider()
    {
      @Override
      public void setDataValue( final int columnIndex, final int rowIndex, final Object newValue )
      {
        throw new UnsupportedOperationException();
      }

      @Override
      public int getRowCount( )
      {
        return m_bodyLayer.getProvider().getRowCount();
      }

      @Override
      public Object getDataValue( final int columnIndex, final int rowIndex )
      {
        final IZmlModelRow row = m_bodyLayer.getModel().getRow( rowIndex );
        return row;
      }

      @Override
      public int getColumnCount( )
      {
        return 1;
      }
    };

    final DataLayer dataLayer = new DataLayer( getProvider() );
    final RowHeaderLayer rowHeaderLayer = new RowHeaderLayer( dataLayer, m_bodyLayer, m_bodyLayer.getSelectionLayer() );
    setUnderlyingLayer( rowHeaderLayer );
  }

  public IDataProvider getProvider( )
  {
    return m_provider;
  }
}