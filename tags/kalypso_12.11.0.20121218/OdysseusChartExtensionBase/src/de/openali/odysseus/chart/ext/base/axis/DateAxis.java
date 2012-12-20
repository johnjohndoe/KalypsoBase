package de.openali.odysseus.chart.ext.base.axis;

import java.util.Date;

import org.apache.commons.collections.comparators.ComparableComparator;

import de.openali.odysseus.chart.ext.base.axisrenderer.AxisRendererConfig;
import de.openali.odysseus.chart.ext.base.axisrenderer.ExtendedAxisRenderer;
import de.openali.odysseus.chart.ext.base.axisrenderer.GenericNumberTickCalculator;
import de.openali.odysseus.chart.ext.base.axisrenderer.NumberLabelCreator;
import de.openali.odysseus.chart.framework.model.data.impl.DateDataOperator;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

public class DateAxis extends AbstractAxis<Date>
{
  // private final IDataOperator<Number> m_dataOperator = new NumberDataOperator( new NumberComparator() );

  public DateAxis( final String id, final POSITION pos )
  {
    this( id, pos, new ExtendedAxisRenderer( id + "_RENDERER", pos, new NumberLabelCreator( "%s" ), new GenericNumberTickCalculator(), new AxisRendererConfig() ) );//$NON-NLS-1$ //$NON-NLS-2$
  }

  public DateAxis( final String id, final POSITION pos, final AxisRendererConfig config )
  {
    this( id, pos, new ExtendedAxisRenderer( id + "_RENDERER", pos, new NumberLabelCreator( "%s" ), new GenericNumberTickCalculator(), config ) );//$NON-NLS-1$ //$NON-NLS-2$
  }

  @SuppressWarnings( { "unchecked" } )
  public DateAxis( final String id, final POSITION pos, final IAxisRenderer renderer )
  {
    super( id, pos, renderer, new DateDataOperator( new ComparableComparator(), "dd.MM.yyyy HH:mm" ) );// new CalendarDataOperator( new ComparableComparator(), "dd.MM.yyyy HH:mm" );
  }

  @Override
  public Double logicalToNumeric( Date value )
  {
    return getDataOperator().logicalToNumeric( value );
  }

  @Override
  public Class<Date> getDataClass( )
  {
    return Date.class;
  }

  @Override
  public Date numericToLogical( Double value )
  {
    return getDataOperator().numericToLogical( value );
  }
}