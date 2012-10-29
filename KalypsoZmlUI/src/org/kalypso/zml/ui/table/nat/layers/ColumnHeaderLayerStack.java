package org.kalypso.zml.ui.table.nat.layers;

import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.grid.layer.ColumnHeaderLayer;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.DataLayer;

import org.kalypso.zml.core.table.model.IZmlModelColumn;
import org.kalypso.zml.core.table.model.view.ZmlModelViewport;

public class ColumnHeaderLayerStack extends AbstractLayerTransform
{
  private final IDataProvider m_provider;

  protected final BodyLayerStack m_bodyLayer;

  public ColumnHeaderLayerStack( final BodyLayerStack bodyLayer )
  {
    m_bodyLayer = bodyLayer;
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
        return 1;
      }

      @Override
      public Object getDataValue( final int columnIndex, final int rowIndex )
      {
        final ZmlModelViewport model = m_bodyLayer.getModel();
        final IZmlModelColumn[] columns = model.getColumns();

        return columns[columnIndex];
      }

      @Override
      public int getColumnCount( )
      {
        return m_bodyLayer.getProvider().getColumnCount();
      }
    };

    final DataLayer dataLayer = new DataLayer( getProvider() );
    final ColumnHeaderLayer colHeaderLayer = new ColumnHeaderLayer( dataLayer, m_bodyLayer, m_bodyLayer.getSelectionLayer() );
    setUnderlyingLayer( colHeaderLayer );
  }

  public IDataProvider getProvider( )
  {
    return m_provider;
  }
}
