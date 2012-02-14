package org.kalypso.zml.ui.table.nat.layers;

import net.sourceforge.nattable.data.IColumnAccessor;
import net.sourceforge.nattable.data.IDataProvider;
import net.sourceforge.nattable.data.IRowDataProvider;
import net.sourceforge.nattable.layer.AbstractLayerTransform;
import net.sourceforge.nattable.layer.DataLayer;
import net.sourceforge.nattable.viewport.ViewportLayer;

import org.kalypso.zml.core.table.model.IZmlModel;
import org.kalypso.zml.core.table.model.IZmlModelRow;
import org.kalypso.zml.ui.table.nat.base.ZmlModelDataProvider;
import org.kalypso.zml.ui.table.nat.base.ZmlModelRowAccesor;

public class BodyLayerStack extends AbstractLayerTransform
{
  private final ZmlTableSelectionLayer m_selectionLayer;

  private final IRowDataProvider<IZmlModelRow> m_provider;

  private final IColumnAccessor<IZmlModelRow> m_accessor;

  private final IZmlModel m_model;

  public BodyLayerStack( final IZmlModel model )
  {
    m_model = model;
    m_accessor = new ZmlModelRowAccesor();
    m_provider = new ZmlModelDataProvider( model, m_accessor );

    final DataLayer dataLayer = new DataLayer( m_provider );
    m_selectionLayer = new ZmlTableSelectionLayer( model, dataLayer );
    final ViewportLayer viewportLayer = new ViewportLayer( getSelectionLayer() );
    setUnderlyingLayer( viewportLayer );
  }

  public ZmlTableSelectionLayer getSelectionLayer( )
  {
    return m_selectionLayer;
  }

  public IZmlTableSelection getSelection( )
  {
    return m_selectionLayer;
  }

  public IDataProvider getProvider( )
  {
    return m_provider;
  }

  public IZmlModel getModel( )
  {
    return m_model;
  }
}