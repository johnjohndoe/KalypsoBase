package de.openali.odysseus.chart.ext.base.axis;

import org.apache.commons.collections.comparators.ComparableComparator;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.ext.base.axisrenderer.AxisRendererConfig;
import de.openali.odysseus.chart.ext.base.axisrenderer.BooleanLabelCreator;
import de.openali.odysseus.chart.ext.base.axisrenderer.ExtendedAxisRenderer;
import de.openali.odysseus.chart.ext.base.axisrenderer.ITickCalculator;
import de.openali.odysseus.chart.framework.model.data.impl.BooleanDataOperator;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

public class BooleanAxis extends AbstractAxis<Boolean>
{
  /**
   * TODO: TICK_CALCULATOR is used by BooleanAxisRendererProvider and BooleanAxis. Why?
   */
  public static final ITickCalculator TICK_CALCULATOR = new ITickCalculator()
  {
    @Override
    public Double[] calcTicks( final GC gc, final IAxis< ? > ax, final Number minDisplayInterval, final Point tickLabelSize )
    {
      return new Double[] { 0.0, 1.0 };
    }
  };

  public BooleanAxis( final String id, final POSITION pos )
  {
    this( id, pos, new ExtendedAxisRenderer( id + "_RENDERER", pos, new BooleanLabelCreator(), TICK_CALCULATOR, new AxisRendererConfig() ) );//$NON-NLS-1$
  }

  public BooleanAxis( final String id, final POSITION pos, final AxisRendererConfig config )
  {
    this( id, pos, new ExtendedAxisRenderer( id + "_RENDERER", pos, new BooleanLabelCreator(), TICK_CALCULATOR, config ) );//$NON-NLS-1$
  }

  public BooleanAxis( final String id, final POSITION pos, final IAxisRenderer renderer )
  {
    super( id, pos, renderer, new BooleanDataOperator( new ComparableComparator() ) );
  }

  @Override
  public Class<Boolean> getDataClass( )
  {
    return Boolean.class;
  }

  @Override
  public Double logicalToNumeric( final Boolean value )
  {
    return getDataOperator().logicalToNumeric( value );
  }

  @Override
  public Boolean numericToLogical( final Double value )
  {
    return getDataOperator().numericToLogical( value );
  }
}
