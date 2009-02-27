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

  private final String m_targetComponentId;

  private final String m_domainComponentId;

  public TupleResultTooltipLineLayer( TupleResultDomainValueData data, TupleResult result, ILineStyle lineStyle, IPointStyle pointStyle, String[] tooltipComponentIds )
  {
    super( data, lineStyle, pointStyle );
    m_result = result;
    m_domainComponentId = "urn:ogc:gml:dict:kalypso:wspm:sobek:resultLengthSectionObservationDefs#STATION";
    m_targetComponentId = "urn:ogc:gml:dict:kalypso:wspm:sobek:resultLengthSectionObservationDefs#WATERLEVEL";

    IComponent[] components = m_result.getComponents();
    List<IComponent> myComponents = new ArrayList<IComponent>();

    for( IComponent component : components )
    {
      if( ArrayUtils.contains( tooltipComponentIds, component.getId() ) )
        myComponents.add( component );
    }

    m_tooltipComponents = myComponents.toArray( new IComponent[] {} );
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.layer.ITooltipChartLayer#getHover(org.eclipse.swt.graphics.Point)
   */
  public EditInfo getHover( Point pos )
  {
    int tolerance = 5;
    // Punkt finden
    IAxis domainAxis = getDomainAxis();
    String tooltip = "";

    if( domainAxis.getPosition().getOrientation().equals( ORIENTATION.HORIZONTAL ) )
    {
      double p = domainAxis.screenToNumeric( pos.x ).doubleValue();
      double domMin = domainAxis.screenToNumeric( pos.x - tolerance ).doubleValue();
      double domMax = domainAxis.screenToNumeric( pos.x + tolerance ).doubleValue();

      double distance = Double.MAX_VALUE;
      IRecord myRecord = null;

      for( IRecord record : m_result )
      {
        double station = Double.valueOf( record.getValue( 0 ).toString() ); // station

        if( domMin <= station && station <= domMax )
        {
          double d = station - p;

          if( d < distance )
            myRecord = record;
        }
      }

      if( myRecord == null )
        return null;

      int count = m_tooltipComponents.length;

      for( IComponent component : m_tooltipComponents )
      {
        count--;
        Object value = myRecord.getValue( component );

        if( value == null )
          continue;
        else if( value instanceof BigDecimal )
        {
          BigDecimal decimal = (BigDecimal) value;
          double doubleValue = decimal.doubleValue();

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
