package de.openali.odysseus.chart.ext.base.axis;

import de.openali.odysseus.chart.ext.base.axisrenderer.AxisRendererConfig;
import de.openali.odysseus.chart.ext.base.axisrenderer.ExtendedAxisRenderer;
import de.openali.odysseus.chart.ext.base.axisrenderer.GenericNumberTickCalculator;
import de.openali.odysseus.chart.ext.base.axisrenderer.NumberLabelCreator;
import de.openali.odysseus.chart.framework.model.data.impl.NumberDataOperator;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.registry.impl.NumberComparator;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

public class GenericLinearAxis extends AbstractAxis<Number>
{
  //private final IDataOperator<Number> m_dataOperator = new NumberDataOperator( new NumberComparator() );

  public GenericLinearAxis( final String id, final POSITION pos )
  {
    this( id, pos, new ExtendedAxisRenderer( id + "_RENDERER", pos, new NumberLabelCreator( "%s" ), new GenericNumberTickCalculator(), new AxisRendererConfig() ));//$NON-NLS-1$ //$NON-NLS-2$
  }

  public GenericLinearAxis( final String id, final POSITION pos, final AxisRendererConfig config )
  {
    this( id, pos, new ExtendedAxisRenderer( id + "_RENDERER", pos, new NumberLabelCreator( "%s" ), new GenericNumberTickCalculator(), config ));//$NON-NLS-1$ //$NON-NLS-2$
  }

  public GenericLinearAxis( final String id, final POSITION pos, final IAxisRenderer renderer )
  {
    super( id, pos, renderer, new NumberDataOperator( new NumberComparator() ) );
   }

  @Override
  public Double logicalToNumeric( Number value )
  {
    return getDataOperator().logicalToNumeric( value );
  }

  @Override
  public Class<Number> getDataClass( )
  {
    return Number.class;
  }

  @Override
  public Number numericToLogical( Double value )
  {
    return getDataOperator().numericToLogical( value );
  }
}