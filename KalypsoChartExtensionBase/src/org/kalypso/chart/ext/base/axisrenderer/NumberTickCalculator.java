package org.kalypso.chart.ext.base.axisrenderer;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

public class NumberTickCalculator
{

  /**
   * Calculates the ticks shown for the given Axis *
   * 
   * @param minDisplayInterval
   *            interval division should stop when intervals become smaller than this value
   */
  @SuppressWarnings("cast")
  public static void calcTicks( final GCWrapper gc, final IAxis<Number> axis, final double minDisplayInterval )
  {
    final IMapperRegistry registry = axis.getRegistry();
    final NumberAxisRenderer renderer = (NumberAxisRenderer) registry.getRenderer( axis );

    if( axis.getLogicalRange() == null )
      renderer.setTickMapElement( axis, null );

    if( (gc == null) || (axis == null) || (axis.getPosition() == null) )
      renderer.setTickMapElement( axis, null );

    // größtes Tick<-Label berechnen
    final Point ticklabelSize = renderer.calcTickLabelSize( gc, axis );
    if( ticklabelSize == null )
      renderer.setTickMapElement( axis, null );

    // TickLabelGröße + 2 wegen Rundungsfehlern beim positionieren
    /*
     * minimaler Bildschirmabstand zwischen zwei labels
     */
    final int minScreenInterval;
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      minScreenInterval = ticklabelSize.x;
    else
      minScreenInterval = ticklabelSize.y;

    // Collection für Ticks
    // final TreeMap<Integer, SortedSet<Double>> ticks = new TreeMap<Integer, SortedSet<Double>>();
    final SortedSet<Double> ticks = new TreeSet<Double>();

    // Mini- und maximalen Grenz-Wert ermitteln anhand der Grï¿½ï¿½e der Labels
    int screenMin, screenMax;
    final IDataRange<Number> range = axis.getLogicalRange();

    final Point screenMinMax = NumberTickCalculator.getScreenMinMax( axis, range, ticklabelSize );
    screenMin = screenMinMax.x;
    screenMax = screenMinMax.y;

    // logischen Mini- und Maximalen wert
    final double logicalMin = axis.screenToLogical( screenMin ).doubleValue();
    final double logicalMax = axis.screenToLogical( screenMax ).doubleValue();

    final double logicalRange = Math.abs( logicalMax - logicalMin );

    // der minimale logische Abstand; TODO: überprüfen, ob das auch mit negativen Werten geht
    final double minLogInterval = Math.abs( axis.screenToLogical( minScreenInterval ).doubleValue() - axis.screenToLogical( 0 ).doubleValue() );

    // Herausfinden, in welchem 10erPotenz-Bereich sich die Range befinden
    int rangepow = 0;
    // Normalisierte Range
    double normrange = logicalRange;
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
    // Das Minimum wieder zurï¿½ckrechnen
    normmin = normmin * Math.pow( 10, (rangepow - 1) * (-1) );

    // Das Maximum normalisieren und aufrunden
    double normmax = Math.ceil( range.getMax().doubleValue() * Math.pow( 10, rangepow - 1 ) );
    // Das Maximum wieder zurï¿½ckrechnen
    normmax = normmax * Math.pow( 10, (rangepow - 1) * (-1) );

    // Zum feststellen, Ã¼ber wieviele Grundintervalle iteriert werden soll
    final int normmid = (int) ((normmax - normmin) * Math.pow( 10, rangepow - 1 ));

    // Das Intervall verwendet zunï¿½chst nur 10er Schritte
    double interval = Math.pow( 10, rangepow * (-1) - 1 );
    if( interval < minDisplayInterval )
      interval = minDisplayInterval;

    // hier werden alle Zahlen gespeichert, die als gute Divisoren eines Intervalls gelten
    // 3 wï¿½rde z.B. schnell krumme werte erzeugen
    final LinkedList<Integer> goodDivisors = new LinkedList<Integer>();

    goodDivisors.add( 5 );
    goodDivisors.add( 2 );

    ticks.add( new Double( normmin ) );
    ticks.add( new Double( normmax ) );

    if( normmid == 1 )
      NumberTickCalculator.findBetweens( normmin, normmax, minLogInterval, goodDivisors, ticks, minDisplayInterval, axis );
    else
    {
      final double normmiddle = normmin + (normmax - normmin) / 2;
      ticks.add( new Double( normmiddle ) );
      NumberTickCalculator.findBetweens( normmin, normmiddle, minLogInterval, goodDivisors, ticks, minDisplayInterval, axis );
      NumberTickCalculator.findBetweens( normmiddle, normmax, minLogInterval, goodDivisors, ticks, minDisplayInterval, axis );
    }
    renderer.setTickMapElement( axis, (Number[]) ticks.toArray( new Number[] {} ) );
  }

  /**
   * recursive function which divides in interval into a Double of "divisions" and adds the values to a linked list the
   * divisions have to have a larger range than the param interval *
   * 
   * @param minDisplayInterval
   *            if value is greater than 0, the interval division should stop when intervals become smaller than this
   *            value
   * @param axis
   */
  private static void findBetweens( final double from, final double to, final double interval, final List<Integer> divisors, final SortedSet<Double> ticks, final double minDisplayInterval, final IAxis<Number> axis )
  {
    final IDataRange<Number> dataRange = axis.getLogicalRange();

    for( final Integer divisor : divisors )
    {
      final double betweenrange = Math.abs( from - to ) / divisor.intValue();

      // this prevents an endles loop below, if from == to
      if( betweenrange < 0.000000001 )
        return;

      if( (interval < betweenrange) && (betweenrange >= minDisplayInterval) )
      {
        // vorher prï¿½fen
        int count = 0;

        // Damit der letzte Wert nicht drin ist, wird sicherheitshalber der halbe Wertabstand abgezogen
        for( double i = from; i <= to - 0.5 * betweenrange; i += betweenrange )
          // nur berechenen, wenn wenigstens eine Intervallgrenze im sichtbaren Bereich liegt
          // oder die Intervallgrenzen den sichtbaren Bereich umschliessen
          if( (((i >= dataRange.getMin().doubleValue()) && (i <= dataRange.getMax().doubleValue())) || ((i + betweenrange >= dataRange.getMin().doubleValue()) && (i + betweenrange <= dataRange.getMax().doubleValue())))
              || ((i <= dataRange.getMin().doubleValue()) && (i + betweenrange >= dataRange.getMax().doubleValue()))

          )
          {

            // Der erste Wert ist schon auf hÃ¶herer Ebene drin -> nicht einfÃ¼gen
            if( count > 0 )
              ticks.add( new Double( i ) );

            // Divisoren rotieren, damit in der nÃ¤chsten Ebene Ã¼ber den nÃ¤chsten iteriert wird
            final ArrayList<Integer> newDivisors = new ArrayList<Integer>();
            final int divIndex = divisors.indexOf( divisor );
            final List<Integer> tail = divisors.subList( divIndex + 1, divisors.size() );
            final List<Integer> head = divisors.subList( 0, divIndex + 1 );
            newDivisors.addAll( tail );
            newDivisors.addAll( head );

            // Ãœber ZwischenrÃ¤ume weiter iterieren
            NumberTickCalculator.findBetweens( i, i + betweenrange, interval, newDivisors, ticks, minDisplayInterval, axis );
            count++;

          }
          else
          {
            count++;
          }
        // Tick setzen
        break;

      }
    }
  }

  private static Point getScreenMinMax( final IAxis<Number> axis, final IDataRange<Number> range, final Point ticklabelSize )
  {
    int screenMin;
    int screenMax;

    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
    {
      if( axis.getDirection() == DIRECTION.POSITIVE )
      {
        screenMin = (int) (axis.logicalToScreen( range.getMin() ) + Math.ceil( 0.5 * ticklabelSize.x ));
        screenMax = (int) (axis.logicalToScreen( range.getMax() ) - Math.ceil( 0.5 * ticklabelSize.x ));
      }
      else
      {

        screenMin = (int) (axis.logicalToScreen( range.getMin() ) - Math.ceil( 0.5 * ticklabelSize.x ));
        screenMax = (int) (axis.logicalToScreen( range.getMax() ) + Math.ceil( 0.5 * ticklabelSize.x ));
      }
    }
    else if( axis.getDirection() == DIRECTION.POSITIVE )
    {
      screenMin = (int) (axis.logicalToScreen( range.getMin() ) - Math.ceil( 0.5 * ticklabelSize.y ));
      screenMax = (int) (axis.logicalToScreen( range.getMax() ) + Math.ceil( 0.5 * ticklabelSize.y ));
    }
    else
    {
      screenMin = (int) (axis.logicalToScreen( range.getMin() ) + Math.ceil( 0.5 * ticklabelSize.y ));
      screenMax = (int) (axis.logicalToScreen( range.getMax() ) - Math.ceil( 0.5 * ticklabelSize.y ));
    }
    return new Point( screenMin, screenMax );
  }

  /*
   * private static Double[] flattenTicks( final SortedMap<Integer, SortedSet<Double>> tickSets ) { final SortedSet<Double>
   * myList = new TreeSet<Double>(); final Set<Entry<Integer, SortedSet<Double>>> entrySet = tickSets.entrySet();
   * for( final Entry<Integer, SortedSet<Double>> entry : entrySet ) for( final Double tick : entry.getValue() )
   * myList.add( tick ); return myList.toArray( new Double[] {} ); }
   */
}