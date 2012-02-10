package org.kalypso.zml.ui.table;

import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.grid.layer.RowHeaderLayer;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.DataLayer;

public class RowHeaderLayerStack extends AbstractLayerTransform
{

  public RowHeaderLayerStack( final BodyLayerStack bodyLayer, final IDataProvider dataProvider )
  {
    final DataLayer dataLayer = new DataLayer( dataProvider, 50, 20 );
    final RowHeaderLayer rowHeaderLayer = new RowHeaderLayer( dataLayer, bodyLayer, bodyLayer.getSelectionLayer() );
    setUnderlyingLayer( rowHeaderLayer );
  }
}