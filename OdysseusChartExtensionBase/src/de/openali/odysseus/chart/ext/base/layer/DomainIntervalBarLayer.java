package de.openali.odysseus.chart.ext.base.layer;

import java.util.Map;

import de.openali.odysseus.chart.ext.base.data.AbstractDomainIntervalValueData;
import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
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
  public IDataRange<Double> getTargetRange( final IDataRange<Double> domainIntervall )
  {
    return getNumericRange( getTargetAxis(), getDataContainer().getTargetRange() );
  }

  @Override
  public IDataRange<Double> getDomainRange( )
  {
    return getNumericRange( getDomainAxis(), getDataContainer().getDomainRange() );
  }

  protected EditInfo getEditInfo( @SuppressWarnings( "unused" ) final int index )
  {
    // TODO: for now, no info yet
    return null;
  }
}