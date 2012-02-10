package org.kalypso.zml.ui.table;

import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.grid.layer.ColumnHeaderLayer;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.DataLayer;

public class ColumnHeaderLayerStack extends AbstractLayerTransform
{

  public ColumnHeaderLayerStack( final BodyLayerStack bodyLayer, final IDataProvider dataProvider )
  {
    final DataLayer dataLayer = new DataLayer( dataProvider );
    final ColumnHeaderLayer colHeaderLayer = new ColumnHeaderLayer( dataLayer, bodyLayer, bodyLayer.getSelectionLayer() );
    setUnderlyingLayer( colHeaderLayer );
  }
}