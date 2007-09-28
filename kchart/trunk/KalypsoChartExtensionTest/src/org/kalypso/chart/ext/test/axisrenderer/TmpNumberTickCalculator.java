package org.kalypso.chart.ext.test.axisrenderer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Map.Entry;

import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

public class TmpNumberTickCalculator
{

  /**
   * Calculates the ticks shown for the given Axis *
   * 
   * @param minDisplayInterval
   *            interval division should stop when intervals become smaller than this value
   */
  public static TreeMap<Integer, SortedSet<Double>> calcTicks( GCWrapper gc, final IAxis<Number> axis, double minDisplayInterval )
  {

    if( axis.getDataRange() == null )
      return null;

    final IMapperRegistry registry = axis.getRegistry();
    final TmpNumberAxisRenderer renderer = (TmpNumberAxisRenderer) registry.getRenderer( axis );

    if( gc == null || axis == null || axis.getPosition() == null )
      return null;

    // gr��tes Tick<-Label berechnen
    final Point ticklabelSize = renderer.calcTickLabelSize( gc, axis );
    if( ticklabelSize == null )
      return null;

    // TickLabelGr��e + 2 wegen Rundungsfehlern beim positionieren
    /*
     * minimaler Bildschirmabstand zwischen zwei labels
     */
    final int minScreenInterval;
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL )
      minScreenInterval = ticklabelSize.x;
    else
      minScreenInterval = ticklabelSize.y;

    // Collection f�r Ticks
    final TreeMap<Integer, SortedSet<Double>> ticks = new TreeMap<Integer, SortedSet<Double>>();

    // Mini- und maximalen Grenz-Wert ermitteln anhand der Gr��e der
    // Labels
    int screenMin, screenMax;
    final IDataRange<Number> range = axis.getDataRange();

    final Point screenMinMax = getScreenMinMax( axis, range, ticklabelSize );
    screenMin = screenMinMax.x;
    screenMax = screenMinMax.y;

    Logger.trace( axis.getIdentifier() + " ScreenMin: " + screenMin + " ScreenMax:" + screenMax + " ( " + axis.logicalToScreen( range.getMin() ) + " : " + axis.logicalToScreen( range.getMax() )
        + ") " );

    // logischen Mini- und Maximalen wert
    final double logicalMin = axis.screenToLogical( screenMin ).doubleValue();
    final double logicalMax = axis.screenToLogical( screenMax ).doubleValue();

    Logger.trace( axis.getIdentifier() + " ScreenMin: " + screenMin + " ScreenMax:" + screenMax + " ( " + axis.logicalToScreen( range.getMin() ) + " : " + axis.logicalToScreen( range.getMax() )
        + ") " );

    final double logicalRange = Math.abs( logicalMax - logicalMin );

    // der minimale logische Abstand; TODO: �berpr�fen, ob das auch mit
    // negativen Werten geht
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

    Logger.trace( axis.getIdentifier() + " logicalRange: " + logicalRange );

    Logger.trace( axis.getIdentifier() + " Normrange: " + normrange );
    Logger.trace( axis.getIdentifier() + " Rangepow: " + rangepow );

    // Das Minimum normalisieren und abrunden
    double normmin = Math.floor( range.getMin().doubleValue() * Math.pow( 10, rangepow - 1 ) );
    // Das Minimum wieder zur�ckrechnen
    normmin = normmin * Math.pow( 10, (rangepow - 1) * (-1) );
    Logger.logInfo( Logger.TOPIC_LOG_AXIS, "normmin: " + normmin );
    Logger.trace( axis.getIdentifier() + " Normmin: " + normmin );

    // Das Maximum normalisieren und aufrunden
    double normmax = Math.ceil( range.getMax().doubleValue() * Math.pow( 10, rangepow - 1 ) );
    // Das Maximum wieder zur�ckrechnen
    normmax = normmax * Math.pow( 10, (rangepow - 1) * (-1) );
    Logger.trace( axis.getIdentifier() + " Normmax: " + normmax );
    Logger.logInfo( Logger.TOPIC_LOG_AXIS, "normmax: " + normmax );
    System.out.println( axis.getIdentifier() + " Normmin: " + normmin );
    System.out.println( axis.getIdentifier() + " Normmax: " + normmax );

    // Zum feststellen, über wieviele Grundintervalle iteriert werden soll
    final int normmid = (int) ((normmax - normmin) * Math.pow( 10, rangepow - 1 ));
    System.out.println( "Normmax - Normmin: " + normmid );

    // Das Intervall verwendet zun�chst nur 10er Schritte
    double interval = Math.pow( 10, rangepow * (-1) - 1 );
    if( interval < minDisplayInterval )
      interval = minDisplayInterval;
    Logger.trace( axis.getIdentifier() + " Interval (" + axis.getIdentifier() + "): " + interval );
    Logger.logInfo( Logger.TOPIC_LOG_AXIS, "Interval: " + interval );

    // hier werden alle Zahlen gespeichert, die als gute Divisoren eines
    // Intervalls gelten
    // 3 w�rde z.B. schnell krumme werte erzeugen
    final LinkedList<Integer> goodDivisors = new LinkedList<Integer>();

    goodDivisors.add( 5 );
    goodDivisors.add( 2 );

    new TreeMap<Integer, SortedSet<Double>>();
    final int count = 0;
    final double oldi = 0;
    final int level = 0;

    final SortedSet<Double> firstticks = new TreeSet<Double>();
    ticks.put( new Integer( level ), firstticks );
    firstticks.add( new Double( normmin ) );
    firstticks.add( new Double( normmax ) );

    if( normmid == 1 )
    {
      findBetweens( normmin, normmax, minLogInterval, goodDivisors, ticks, level + 1, minDisplayInterval, axis );
    }
    // normmid==2
    else
    {
      final double normmiddle = normmin + (normmax - normmin) / 2;
      System.out.println( normmiddle );
      firstticks.add( new Double( normmiddle ) );
      findBetweens( normmin, normmiddle, minLogInterval, goodDivisors, ticks, level + 1, minDisplayInterval, axis );
      findBetweens( normmiddle, normmax, minLogInterval, goodDivisors, ticks, level + 1, minDisplayInterval, axis );
    }

    /*
     * for( double i = normmin; i <= normmax-0.5*interval; i += interval ) { Logger.logInfo(Logger.TOPIC_LOG_AXIS,
     * "Schleife i: "+i+":"+normmin+":"+normmax); Logger.logInfo(Logger.TOPIC_LOG_AXIS, "Top Level: "); findBetweens( i,
     * i+interval, minLogInterval, goodDivisors, ticks, level+1, minDisplayInterval ); firstticks.add( (new Double( i )) );
     * 
     * count++; oldi = i; }
     */
    firstticks.add( new Double( normmin ) );
    firstticks.add( new Double( normmax ) );
    Logger.logInfo( Logger.TOPIC_LOG_AXIS, "Letztes i: " + oldi );
    Logger.logInfo( Logger.TOPIC_LOG_AXIS, "next would be: " + (oldi + interval) + " > " + normmax );

    outputTicks( ticks );

    /*
     * // in realticks sind jetzt nur die wirklich zu zeichnenden werte drin // wenn man das nicht �ber neues objekt,
     * sondern ticks.remove macht, gibts eine concurrentmodificationexception LinkedList<Number> realticks = new
     * LinkedList<Number>(); for( Double tick : ticks ) { double tickdv = tick.doubleValue();
     * 
     * boolean hasequal=false; double epsilon=Math.pow(10, - (rangepow+4)); for (Number realtick: realticks) { if
     * (Math.abs(realtick.doubleValue() - tick) < epsilon) { hasequal=true; break; } } if (!hasequal) realticks.add( new
     * Double( tickdv ) ); }
     */

    // Anzahl der Nachkommastellen ausrechnen
    final NumberFormat nf = new DecimalFormat();
    final NumberFormat oldnf = renderer.getNumberFormat();

    // F�r das Double-Format wird nun noch die Anzahl der Nachkommastellen
    // aus der Potenz ermittelt

    if( rangepow >= 0 )
    {
      nf.setMaximumFractionDigits( rangepow + 4 );
      nf.setMinimumFractionDigits( rangepow + 4 );
    }

    final int digits = testNumberFormat( flattenTicks( ticks ) );
    nf.setMaximumFractionDigits( digits );
    nf.setMinimumFractionDigits( digits );
    renderer.setNumberFormat( nf );
    // Nochmal rechnen mit neuem NumberFormat <- sehr doof
    if( axis.getPosition().getOrientation() == ORIENTATION.HORIZONTAL && oldnf.getMinimumFractionDigits() < digits && oldnf.getMaximumFractionDigits() < digits )
      return calcTicks( gc, axis, minDisplayInterval );
    /*
     * renderer.setTickMapElement(axis, realticks);
     * 
     * Logger.logInfo(Logger.TOPIC_LOG_AXIS, "Anzahl ticks: "+realticks.size()); SortedSet<Number> ss=new TreeSet<Number>(realticks);
     * for (Number realtick: ss) { Logger.logInfo(Logger.TOPIC_LOG_AXIS, realtick.doubleValue()+""); } return realticks;
     */
    return ticks;
  }

  private static void outputTicks( TreeMap<Integer, SortedSet<Double>> ticks )
  {
    final Set<Entry<Integer, SortedSet<Double>>> entrySet = ticks.entrySet();
    for( final Entry<Integer, SortedSet<Double>> entry : entrySet )
    {
      final Integer key = entry.getKey();
      final SortedSet<Double> valueSet = entry.getValue();
      System.out.print( "Level " + key + ": " );
      for( final Double double1 : valueSet )
      {
        System.out.print( double1 + " | " );
      }
      System.out.println();
    }
    System.out.println( "\n-----------------" );

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
  private static void findBetweens( double from, double to, double interval, List<Integer> divisors, TreeMap<Integer, SortedSet<Double>> ticks, int level, double minDisplayInterval, IAxis<Number> axis )
  {
    final IDataRange<Number> dataRange = axis.getDataRange();

    String levelinset = "";
    for( int i = 0; i < level; i++ )
    {
      levelinset += "\t";
    }
    for( final Integer divisor : divisors )
    {
      final double betweenrange = Math.abs( from - to ) / divisor.intValue();

      // TODO: this prevents an endles loop below, if from == to
      // @Alex:check if this is ok
      if( betweenrange < 0.000000001 )
        return;

      if( interval < betweenrange && betweenrange >= minDisplayInterval )
      {
        // vorher pr�fen
        int count = 0;
        double oldi = 0;

        SortedSet<Double> sortedSet = ticks.get( new Integer( level ) );
        if( sortedSet == null )
        {
          sortedSet = new TreeSet<Double>();
          ticks.put( new Integer( level ), sortedSet );
        }

        // Damit der letzte Wert nicht drin ist, wird sicherheitshalber
        // der halbe Wertabstand abgezogen
        for( double i = from; i <= to - 0.5 * betweenrange; i += betweenrange )
        {
          // nur berechenen, wenn wenigstens eine Intervallgrenze im
          // sichtbaren Bereich liegt
          // oder die Intervallgrenzen den sichtbaren Bereich
          // umschliessen
          if( ((i >= dataRange.getMin().doubleValue() && i <= dataRange.getMax().doubleValue()) || (i + betweenrange >= dataRange.getMin().doubleValue() && i + betweenrange <= dataRange.getMax().doubleValue()))
              || (i <= dataRange.getMin().doubleValue() && i + betweenrange >= dataRange.getMax().doubleValue())

          )
          {

            Logger.logInfo( Logger.TOPIC_LOG_AXIS, levelinset + i );
            // Der erste Wert ist schon auf höherer Ebene drin ->
            // nicht einfügen
            if( count > 0 )
              sortedSet.add( new Double( i ) );

            // Divisoren rotieren, damit in der nächsten Ebene
            // über den nächsten iteriert wird
            final ArrayList<Integer> newDivisors = new ArrayList<Integer>();
            final int divIndex = divisors.indexOf( divisor );
            final List<Integer> tail = divisors.subList( divIndex + 1, divisors.size() );
            final List<Integer> head = divisors.subList( 0, divIndex + 1 );
            newDivisors.addAll( tail );
            newDivisors.addAll( head );

            // Über Zwischenräume weiter iterieren
            findBetweens( i, i + betweenrange, interval, newDivisors, ticks, level + 1, minDisplayInterval, axis );
            count++;
            oldi = i;

          }
          else
          {
            count++;
            oldi = i;
            System.out.println( "Nicht sichtbar: " + i + " " + (i + betweenrange) + " " + dataRange.getMin() + " " + dataRange.getMax() );
          }
          // Tick setzen

        }
        break;

      }
    }
  }

  /**
   * testet, wieviele Nachkommastellen angezeigt werden m�ssen
   * 
   * @param ticks
   * @return
   */
  public static int testNumberFormat( Number[] ticks )
  {
    // Probleme sind zu viele Nullen der Neunen
    int digits = 0;

    for( final Number tick : ticks )
    {
      boolean allgood = false;
      while( !allgood )
      {
        final NumberFormat nf = new DecimalFormat();
        nf.setMaximumFractionDigits( digits );
        nf.setMinimumFractionDigits( digits );
        final Number dtick = tick;
        Number stick = new Double( 0.0 );
        try
        {
          stick = (nf.parse( nf.format( tick ) )).doubleValue();
        }
        catch( final ParseException e )
        {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
        final double epsilon = Math.abs( dtick.doubleValue() - stick.doubleValue() );
        Logger.logInfo( Logger.TOPIC_LOG_AXIS, "Ticks: (d/s)" + dtick + ":" + stick + ":" + epsilon );

        if( epsilon < 0.000000001 )
        {
          allgood = true;
          break;
        }

        else
        {
          digits++;
        }

      }
    }

    return digits;
  }

  private static Point getScreenMinMax( IAxis<Number> axis, IDataRange<Number> range, Point ticklabelSize )
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
    else
    {
      if( axis.getDirection() == DIRECTION.POSITIVE )
      {
        screenMin = (int) (axis.logicalToScreen( range.getMin() ) - Math.ceil( 0.5 * ticklabelSize.y ));
        screenMax = (int) (axis.logicalToScreen( range.getMax() ) + Math.ceil( 0.5 * ticklabelSize.y ));
      }
      else
      {
        screenMin = (int) (axis.logicalToScreen( range.getMin() ) + Math.ceil( 0.5 * ticklabelSize.y ));
        screenMax = (int) (axis.logicalToScreen( range.getMax() ) - Math.ceil( 0.5 * ticklabelSize.y ));
      }

    }
    return new Point( screenMin, screenMax );
  }

  private static Double[] flattenTicks( SortedMap<Integer, SortedSet<Double>> tickSets )
  {
    final SortedSet<Double> myList = new TreeSet<Double>();
    final Set<Entry<Integer, SortedSet<Double>>> entrySet = tickSets.entrySet();
    for( final Entry<Integer, SortedSet<Double>> entry : entrySet )
    {
      for( final Double tick : entry.getValue() )
      {
        myList.add( tick );
      }
    }
    return myList.toArray( new Double[] {} );
  }
}