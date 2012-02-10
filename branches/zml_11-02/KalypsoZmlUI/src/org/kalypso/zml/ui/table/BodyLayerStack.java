package org.kalypso.zml.ui.table;

import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.hideshow.ColumnHideShowLayer;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.reorder.ColumnReorderLayer;
import net.sourceforge.nattable.selection.SelectionLayer;
import net.sourceforge.nattable.viewport.ViewportLayer;

public class BodyLayerStack extends AbstractLayerTransform
{
  private final SelectionLayer m_selectionLayer;

  public BodyLayerStack( final IDataProvider dataProvider )
  {
    final DataLayer bodyDataLayer = new DataLayer( dataProvider );
    final ColumnReorderLayer columnReorderLayer = new ColumnReorderLayer( bodyDataLayer );
    final ColumnHideShowLayer columnHideShowLayer = new ColumnHideShowLayer( columnReorderLayer );
    m_selectionLayer = new SelectionLayer( columnHideShowLayer );
    final ViewportLayer viewportLayer = new ViewportLayer( m_selectionLayer );
    setUnderlyingLayer( viewportLayer );
  }

  public SelectionLayer getSelectionLayer( )
  {
    return m_selectionLayer;
  }
}