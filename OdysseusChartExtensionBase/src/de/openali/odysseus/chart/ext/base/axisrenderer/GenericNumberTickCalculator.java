package de.openali.odysseus.chart.ext.base.axisrenderer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;

import de.openali.odysseus.chart.framework.model.data.IDataRange;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ORIENTATION;

public class GenericNumberTickCalculator implements ITickCalculator
{

  /**
   * Calculates the ticks shown for the given Axis *
   * 
   * @param minDisplayInterval
   *          interval division should stop when intervals become smaller than this value
   */
  @Override
  public Number[] calcTicks( final GC gc, final IAxis axis, final Number minDisplayInterval, final Point ticklabelSize )
  {
    final IDataRange<Number> range = axis.getNumericRange();
    final Number max = range.getMax();
    final Number min = range.getMin();
    if( max == null || Double.isNaN( max.doubleValue() ) || min == null || Double.isNaN( min.doubleValue() ) )
      return new Number[] {};


    // Collection f�r Ticks
    // final TreeMap<Integer, SortedSet<Double>> ticks = new TreeMap<Integer, SortedSet<Double>>();
    final SortedSet<Number> ticks = new TreeSet<Number>();

    // Mini- und maximalen Grenz-Wert ermitteln anhand der Gr��e der Labels
    int screenMin, screenMax;

    final Point screenMinMax = GenericNumberTickCalculator.getScreenMinMax( axis, range, ticklabelSize );
    screenMin = screenMinMax.x;
    screenMax = screenMinMax.y;

    // logischen Mini- und Maximalen wert
    final double numericMin = axis.screenToNumeric( screenMin ).doubleValue();
    final double numericMax = axis.screenToNumeric( screenMax ).doubleValue();

    final double numericRange = Math.abs( numericMax - numericMin );
    if( Double.isNaN( numericRange ) )
      return new Number[] {};

    // der minimale logische Abstand anhand des Ticklabels;
    final double minLogInterval = Math.abs( axis.screenToNumeric( ticklabelSize.x ).doubleValue() - axis.screenToNumeric( 0 ).doubleValue() );

    // Herausfinden, in welchem 10erPotenz-Bereich sich die Range befinden
    int rangepow = 0;
    // Normalisierte Range

    double normrange = numericRange;
    if( normrange != 0 )
    {
      while( normrange >= 10 )
      {
        normrange /= 10;
        rangepow--;
      }
      while( normrange < 1 )
      {
        normrange *= 10;
        rangepow++;
      }
    }

    // Das Minimum normalisieren und abrunden
    double normmin = Math.floor( range.getMin().doubleValue() * Math.pow( 10, rangepow - 1 ) );
    // Das Minimum wieder zur�ckrechnen
    normmin = normmin * Math.pow( 10, (rangepow - 1) * (-1) );

    // Das Maximum normalisieren und aufrunden
    double normmax = Math.ceil( range.getMax().doubleValue() * Math.pow( 10, rangepow - 1 ) );
    // Das Maximum wieder zur�ckrechnen
    normmax = normmax * Math.pow( 10, (rangepow - 1) * (-1) );

    // Zum feststellen, über wieviele Grundintervalle iteriert werden soll
    final int normmid = (int) ((normmax - normmin) * Math.pow( 10, rangepow - 1 ));

    // Das Intervall verwendet zun�chst nur 10er Schritte
    double interval = Math.pow( 10, rangepow * (-1) - 1 );
    if( minDisplayInterval != null && interval < minDisplayInterval.doubleValue() )
    {
      interval = minDisplayInterval.doubleValue();
    }

    // hier werden alle Zahlen gespeichert, die als gute Divisoren eines Intervalls gelten
    // 3 w�rde z.B. schnell krumme werte erzeugen
    final LinkedList<Integer> goodDivisors = new LinkedList<Integer>();

    goodDivisors.add( 5 );
    goodDivisors.add( 2 );

    ticks.add( new Double( normmin ) );
    ticks.add( new Double( normmax ) );

    if( normmid == 1 )
    {
      findBetweens( normmin, normmax, minLogInterval, goodDivisors, ticks, minDisplayInterval, axis );
    }
    else
    {
      final double normmiddle = normmin + (normmax - normmin) / 2;
      ticks.add( new Double( normmiddle ) );
      findBetweens( normmin, normmiddle, minLogInterval, goodDivisors, ticks, minDisplayInterval, axis );
      findBetweens( normmiddle, normmax, minLogInterval, goodDivisors, ticks, minDisplayInterval, axis );
    }

    final Number[] numTicks = ticks.toArray( new Number[] {} );
    return numTicks;

  }

  /**
   * recursive function which divides in interval into a Double of "divisions" and adds the values to a linked list the
   * divisions have to have a larger range than the param interval *
   * 
   * @param minDisplayInterval
   *          if value is greater than 0, the interval division should stop when intervals become smaller than this
   *          value
   * @param axis
   */
  private static void findBetweens( final double from, final double to, final double interval, final List<Integer> divisors, final SortedSet<Number> ticks, final Number minDisplayInterval, final IAxis axis )
  {
    final IDataRange<Number> dataRange = axis.getNumericRange();

    for( final Integer divisor : divisors )
    {
      final double betweenrange = Math.abs( from - to ) / divisor.intValue();

      // this prevents an endles loop below, if from == to
      if( betweenrange < 0.000000001 )
      {
        return;
      }

      double minDispInterval = 0;
      if( minDisplayInterval != null )
      {
        minDispInterval = minDisplayInterval.doubleValue();
      }

      if( (interval < betweenrange) && (betweenrange >= minDispInterval) )
      {
        // vorher pr�fen
        int count = 0;

        // Damit der letzte Wert nicht drin ist, wird sicherheitshalber der halbe Wertabstand abgezogen
        for( double i = from; i <= to - 0.5 * betweenrange; i += betweenrange )
        {
          // nur berechenen, wenn wenigstens eine Intervallgrenze im sichtbaren Bereich liegt
          // oder die Intervallgrenzen den sichtbaren Bereich umschliessen
          if( (((i >= dataRange.getMin().doubleValue()) && (i <= dataRange.getMax().doubleValue())) || ((i + betweenrange >= dataRange.getMin().doubleValue()) && (i + betweenrange <= dataRange.getMax().doubleValue())))
              || ((i <= dataRange.getMin().doubleValue()) && (i + betweenrange >= dataRange.getMax().doubleValue()))

          )
          {

            // Der erste Wert ist schon auf höherer Ebene drin -> nicht einfügen
            if( count > 0 )
            {
              ticks.add( new Double( i ) );
            }

            // Divisoren rotieren, damit in der nächsten Ebene über den nächsten iteriert wird
            final ArrayList<Integer> newDivisors = new ArrayList<Integer>();
            final int divIndex = divisors.indexOf( divisor );
            final List<Integer> tail = divisors.subList( divIndex + 1, divisors.size() );
            final List<Integer> head = divisors.subList( 0, divIndex + 1 );
            newDivisors.addAll( tail );
            newDivisors.addAll( head );

            // �ber Zwischenr�ume weiter iterieren
            GenericNumberTickCalculator.findBetweens( i, i + betweenrange, interval, newDivisors, ticks, minDisplayInterval, axis );
            count++;

          }
          else
          {
            count++;
          }
        }
        // Tick setzen
        break;

      }
    }
  }

  private static Point getScreenMinMax( final IAxis axis, final IDataRange<Number> range, final Point ticklabelSize )
  {
    int screenMin;
    int screenMax;

    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      if( axis.getDirection() == DIRECTION.POSITIVE )
      {
        screenMin = (int) (axis.numericToScreen( range.getMin() ) + Math.ceil( 0.5 * ticklabelSize.x ));
        screenMax = (int) (axis.numericToScreen( range.getMax() ) - Math.ceil( 0.5 * ticklabelSize.x ));
      }
      else
      {

        screenMin = (int) (axis.numericToScreen( range.getMin() ) - Math.ceil( 0.5 * ticklabelSize.x ));
        screenMax = (int) (axis.numericToScreen( range.getMax() ) + Math.ceil( 0.5 * ticklabelSize.x ));
      }
    }
    else if( axis.getDirection() == DIRECTION.POSITIVE )
    {
      screenMin = (int) (axis.numericToScreen( range.getMin() ) - Math.ceil( 0.5 * ticklabelSize.y ));
      screenMax = (int) (axis.numericToScreen( range.getMax() ) + Math.ceil( 0.5 * ticklabelSize.y ));
    }
    else
    {
      screenMin = (int) (axis.numericToScreen( range.getMin() ) + Math.ceil( 0.5 * ticklabelSize.y ));
      screenMax = (int) (axis.numericToScreen( range.getMax() ) - Math.ceil( 0.5 * ticklabelSize.y ));
    }
    return new Point( screenMin, screenMax );
  }

}