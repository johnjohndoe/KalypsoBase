package de.openali.odysseus.chart.ext.base.axis;

import de.openali.odysseus.chart.ext.base.axisrenderer.AxisRendererConfig;
import de.openali.odysseus.chart.ext.base.axisrenderer.ExtendedAxisRenderer;
import de.openali.odysseus.chart.ext.base.axisrenderer.GenericNumberTickCalculator;
import de.openali.odysseus.chart.ext.base.axisrenderer.NumberLabelCreator;
import de.openali.odysseus.chart.framework.exception.MalformedValueException;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

public class IntegerAxis extends AbstractAxis<Integer>
{
  // private final IDataOperator<Number> m_dataOperator = new NumberDataOperator( new NumberComparator() );

  public IntegerAxis( final String id, final POSITION pos )
  {
    this( id, pos, new ExtendedAxisRenderer( id + "_RENDERER", pos, new NumberLabelCreator( "%s" ), new GenericNumberTickCalculator(), new AxisRendererConfig() ) );//$NON-NLS-1$ //$NON-NLS-2$
  }

  public IntegerAxis( final String id, final POSITION pos, final AxisRendererConfig config )
  {
    this( id, pos, new ExtendedAxisRenderer( id + "_RENDERER", pos, new NumberLabelCreator( "%s" ), new GenericNumberTickCalculator(), config ) );//$NON-NLS-1$ //$NON-NLS-2$
  }

  public IntegerAxis( final String id, final POSITION pos, final IAxisRenderer renderer )
  {
    // TODO: create DataOperator<Integer>
    super( id, pos, renderer, null );
  }

  @Override
  public Double logicalToNumeric( Integer value )
  {
    return value.doubleValue();
  }

  @Override
  public Class<Integer> getDataClass( )
  {
    return Integer.class;
  }

  @Override
  public Integer numericToLogical( Double value )
  {
    return value.intValue();
  }

  @Override
  public Integer xmlStringToLogical( final String value ) throws MalformedValueException
  {
    try
    {
      return Integer.parseInt( value );
    }
    catch( final NumberFormatException e )
    {
      e.printStackTrace();
      throw new MalformedValueException( e );
    }
  }
}