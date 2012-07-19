package de.openali.odysseus.chart.ext.base.layer;

import java.util.Map;

import de.openali.odysseus.chart.ext.base.data.AbstractDomainIntervalValueData;
import de.openali.odysseus.chart.framework.model.data.DataRange;
import de.openali.odysseus.chart.framework.model.data.IDataOperator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.DataOperatorHelper;
import de.openali.odysseus.chart.framework.model.style.IAreaStyle;
import de.openali.odysseus.chart.framework.model.style.IStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSet;

/**
 * @author alibu
 */
public class DomainIntervalBarLayer extends AbstractBarLayer
{
  private final AbstractDomainIntervalValueData< ? , ? > m_dataContainer;

  public DomainIntervalBarLayer( final ILayerProvider provider, final AbstractDomainIntervalValueData< ? , ? > data, final IAreaStyle areaStyle )
  {
    super( provider, new StyleSet() );

    getStyleSet().addStyle( "area", areaStyle ); //$NON-NLS-1$

    m_dataContainer = data;
  }

  @Override
  protected IBarLayerPainter createPainter( final BarPaintManager paintManager )
  {
    final AbstractDomainIntervalValueData< ? , ? > dataContainer = getDataContainer();

    final int screenHeight = getTargetAxis().getScreenHeight();

    final IStyleSet styleSet = getStyleSet();
    final Map<String, IStyle> styles = styleSet.getStyles();
    final String[] styleNames = styles.keySet().toArray( new String[styles.size()] );

    return new DomainIntervallBarPainter( this, paintManager, dataContainer, screenHeight, styleNames );
  }

  protected AbstractDomainIntervalValueData< ? , ? > getDataContainer( )
  {
    return m_dataContainer;
  }

  @Override
  public IDataRange< ? > getTargetRange( final IDataRange< ? > domainIntervall )
  {
    final IDataRange< ? > targetRange = getDataContainer().getTargetRange();

    if( targetRange == null )
      return null;

    final IDataOperator dop = new DataOperatorHelper().getDataOperator( getTargetAxis().getDataClass() );
    return DataRange.create( dop.logicalToNumeric( targetRange.getMin() ), dop.logicalToNumeric( targetRange.getMax() ) );
  }

  @Override
  public IDataRange< ? > getDomainRange( )
  {
    final IDataRange< ? > domainRange = getDataContainer().getDomainRange();
    if( domainRange == null )
      return null;
    final IDataOperator dop = new DataOperatorHelper().getDataOperator( getDomainAxis().getDataClass() );
    return DataRange.create( dop.logicalToNumeric( domainRange.getMin() ), dop.logicalToNumeric( domainRange.getMax() ) );
  }
}