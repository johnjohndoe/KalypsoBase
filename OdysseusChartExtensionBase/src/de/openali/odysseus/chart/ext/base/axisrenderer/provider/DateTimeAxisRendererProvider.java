package de.openali.odysseus.chart.ext.base.axisrenderer.provider;

import org.joda.time.DateTimeFieldType;

import de.openali.odysseus.chart.ext.base.axisrenderer.DateTimeAxisField;
import de.openali.odysseus.chart.ext.base.axisrenderer.DateTimeLabelCreator;
import de.openali.odysseus.chart.ext.base.axisrenderer.DateTimeTickCalculator;
import de.openali.odysseus.chart.ext.base.axisrenderer.IDateTimeAxisField;
import de.openali.odysseus.chart.ext.base.axisrenderer.IDateTimeAxisFieldProvider;
import de.openali.odysseus.chart.ext.base.axisrenderer.ILabelCreator;
import de.openali.odysseus.chart.ext.base.axisrenderer.ITickCalculator;
import de.openali.odysseus.chart.framework.model.data.IDataRange;

/**
 * @author kimwerner
 */
public class DateTimeAxisRendererProvider extends AbstractGenericAxisRendererProvider
{
  public static final String ID = "de.openali.odysseus.chart.ext.base.axisrenderer.provider.DateTimeAxisRendererProvider";

  private final IDateTimeAxisFieldProvider m_axisFieldProvider = new IDateTimeAxisFieldProvider()
  {

    @Override
    public IDateTimeAxisField getDateTimeAxisField( final IDataRange<Number> range )
    {
      final long dr = range.getMax().longValue() - range.getMin().longValue();
      final long sec = 1000;
      final long min = 60 * sec;
      final long hour = 60 * min;
      final long day = 24 * hour;

      if( dr < 5 * sec )
        return new DateTimeAxisField( DateTimeFieldType.millisOfSecond(), "dd.MM\nHH:mm:ss:SSS", new int[] { 1, 10, 100, 500 }, new int[] {} );//$NON-NLS-1$
      if( dr < 3 * min )
        return new DateTimeAxisField( DateTimeFieldType.secondOfMinute(), "dd.MM\nHH:mm:ss", new int[] { 1, 5, 10, 15, 20, 30 }, new int[] { 0, 5, 15, 30 } );//$NON-NLS-1$
      else if( dr < 2 * hour )
        return new DateTimeAxisField( DateTimeFieldType.minuteOfHour(), "dd.MM\nHH:mm", new int[] { 1, 5, 10, 15, 20, 30 }, new int[] { 0, 5, 15, 30 } );//$NON-NLS-1$
      else if( dr < 7 * hour )
        return new DateTimeAxisField( DateTimeFieldType.minuteOfHour(), "dd.MM\nHH:mm", new int[] { 1, 15, 30 }, new int[] { 0, 15, 30, 45 } );//$NON-NLS-1$
      else if( dr < 3 * day )
        return new DateTimeAxisField( DateTimeFieldType.minuteOfDay(), "dd.MM\nHH:mm", new int[] { 1, 15, 30 }, new int[] {} );//$NON-NLS-1$
      else if( dr < 10 * day )
        return new DateTimeAxisField( DateTimeFieldType.hourOfDay(), "dd.MM\nHH:mm", new int[] { 1, 3, 6, 12 }, new int[] { 12 } );//$NON-NLS-1$
      else if( dr < 180 * day )
        return new DateTimeAxisField( DateTimeFieldType.dayOfMonth(), "dd.MM", new int[] {}, new int[] {} );//$NON-NLS-1$
      else
        return new DateTimeAxisField( DateTimeFieldType.monthOfYear(), "dd.MM.YYYY", new int[] { 1, 2, 3, 4, 6 }, new int[] { 6 } );//$NON-NLS-1$

    }
  };

  /**
   * @see de.openali.odysseus.chart.ext.base.axisrenderer.provider.AbstractGenericAxisRendererProvider#getLabelCreator()
   */
  @Override
  public ILabelCreator getLabelCreator( )
  {
    return new DateTimeLabelCreator( m_axisFieldProvider );
  }

  /**
   * @see de.openali.odysseus.chart.ext.base.axisrenderer.provider.AbstractGenericAxisRendererProvider#getTickCalculator()
   */
  @Override
  public ITickCalculator getTickCalculator( )
  {
    return new DateTimeTickCalculator( m_axisFieldProvider );
  }

}
