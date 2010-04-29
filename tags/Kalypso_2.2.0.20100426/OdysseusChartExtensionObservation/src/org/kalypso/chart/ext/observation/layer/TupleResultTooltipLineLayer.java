package org.kalypso.chart.ext.observation.layer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.ext.observation.data.TupleResultDomainValueData;
import org.kalypso.commons.java.util.StringUtilities;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;
import org.kalypso.observation.result.TupleResult;

import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IPointStyle;

/**
 * ATTENTION: This is a hack! Some componente ids are hard coded, so roght now this can not be used outside of Nofdp
 * 
 * @author Dirk Kuch
 * 
 */
public class TupleResultTooltipLineLayer extends TupleResultLineLayer implements ITooltipChartLayer
{

  private final TupleResult m_result;

  private final IComponent[] m_tooltipComponents;

  public TupleResultTooltipLineLayer( final TupleResultDomainValueData data, final TupleResult result, final ILineStyle lineStyle, final IPointStyle pointStyle, final String[] tooltipComponentIds )
  {
    super( data, lineStyle, pointStyle );
    m_result = result;

    final IComponent[] components = m_result.getComponents();
    final List<IComponent> myComponents = new ArrayList<IComponent>();

    for( final IComponent component : components )
    {
      if( ArrayUtils.contains( tooltipComponentIds, component.getId() ) )
        myComponents.add( component );
    }

    m_tooltipComponents = myComponents.toArray( new IComponent[] {} );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer#getHover(org.eclipse.swt.graphics.Point)
   */
  public EditInfo getHover( final Point pos )
  {
    final int tolerance = 5;
    // Punkt finden
    final IAxis domainAxis = getDomainAxis();
    String tooltip = "";

    if( domainAxis.getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
    {
      final double p = domainAxis.screenToNumeric( pos.x ).doubleValue();
      final double domMin = domainAxis.screenToNumeric( pos.x - tolerance ).doubleValue();
      final double domMax = domainAxis.screenToNumeric( pos.x + tolerance ).doubleValue();

      final double distance = Double.MAX_VALUE;
      IRecord myRecord = null;

      for( final IRecord record : m_result )
      {
        final double station = Double.valueOf( record.getValue( 0 ).toString() ); // station

        if( domMin <= station && station <= domMax )
        {
          final double d = station - p;

          if( d < distance )
            myRecord = record;
        }
      }

      if( myRecord == null )
        return null;

      int count = m_tooltipComponents.length;

      for( final IComponent component : m_tooltipComponents )
      {
        count--;
        final Object value = myRecord.getValue( component );

        if( value == null )
          continue;
        else if( value instanceof BigDecimal )
        {
          final BigDecimal decimal = (BigDecimal) value;
          final double doubleValue = decimal.doubleValue();

          tooltip += String.format( "%s: %.2f", component.getName(), doubleValue );
        }
        else
        {
          tooltip += String.format( "%s: %s", component.getName(), value.toString() );
        }

        if( count > 0 )
          tooltip += "\n";
      }

      StringUtilities.chop( tooltip );
    }

    // Labeltext zusammenstellen
    return new EditInfo( this, null, null, null, tooltip, pos );
  }
}
