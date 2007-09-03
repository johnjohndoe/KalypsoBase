package org.kalypso.chart.ext.base.axisrenderer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.swt.graphics.Point;
import org.kalypso.chart.framework.logging.Logger;
import org.kalypso.chart.framework.model.data.IDataRange;
import org.kalypso.chart.framework.model.mapper.IAxis;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.DIRECTION;
import org.kalypso.chart.framework.model.mapper.IAxisConstants.ORIENTATION;
import org.kalypso.chart.framework.model.mapper.registry.IMapperRegistry;
import org.kalypso.contribs.eclipse.swt.graphics.GCWrapper;

public class FullNumberTickCalculator
{
	
	
	  /**
	   * Calculates the ticks shown for the given Axis
	   */
	  public static List<Number> calcTicks( GCWrapper gc, final IAxis<Number> axis, final boolean showAll)
	  {
	  
	    if (axis.getDataRange()== null )
	      return null;
	    
		IMapperRegistry registry = axis.getRegistry();
		FullNumberAxisRenderer renderer = (FullNumberAxisRenderer) registry.getRenderer(axis);
		  
	    if( gc == null || axis == null || axis.getPosition() == null )
	      return null;

	    // größtes Tick<-Label berechnen
	    final Point ticklabelSize = renderer.calcTickLabelSize( gc, axis );
	    if( ticklabelSize == null )
	      return null;

	   
	    
	    
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
	    final ArrayList<Double> ticks = new ArrayList<Double>();

	    // Mini- und maximalen Grenz-Wert ermitteln anhand der Größe der Labels
	    int screenMin, screenMax;
	    IDataRange<Number> range=axis.getDataRange(); 
	    
	    Point screenMinMax=getScreenMinMax(axis, range, ticklabelSize);
	    screenMin=screenMinMax.x;
	    screenMax=screenMinMax.y;

	    Logger.trace( axis.getIdentifier() + " ScreenMin: " + screenMin + " ScreenMax:" + screenMax + " ( " + axis.logicalToScreen( range.getMin() ) + " : " + axis.logicalToScreen( range.getMax() ) + ") " );
	    
	    // logischen Mini- und Maximalen wert
	    double logicalMin = axis.screenToLogical( screenMin ).doubleValue();
	    double logicalMax = axis.screenToLogical( screenMax ).doubleValue();
	    
	    
	    
	    
	    
	    Logger.trace( axis.getIdentifier() + " ScreenMin: " + screenMin + " ScreenMax:" + screenMax + " ( " + axis.logicalToScreen( range.getMin() ) + " : " + axis.logicalToScreen( range.getMax() ) + ") " );
	    
	    double logicalRange = Math.abs( logicalMax - logicalMin );

	    // der minimale logische Abstand; TODO: überprüfen, ob das auch mit negativen Werten geht
	    double minLogInterval = Math.abs( axis.screenToLogical( minScreenInterval ).doubleValue() - axis.screenToLogical( 0 ).doubleValue() );

	    
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
	    double normmin = Math.floor( range.getMin().doubleValue() * Math.pow( 10, rangepow ) );
	    // Das Minimum wieder zurückrechnen
	    normmin = normmin * Math.pow( 10, rangepow * (-1) );
	    Logger.logInfo(Logger.TOPIC_LOG_AXIS, "normmin: "+normmin);
	    Logger.trace( axis.getIdentifier() + " Normmin: " + normmin );

	    // Das Maximum normalisieren und aufrunden
	    double normmax = Math.ceil( range.getMax().doubleValue() * Math.pow( 10, rangepow ) );
	    // Das Maximum wieder zurückrechnen
	    normmax = normmax * Math.pow( 10, rangepow * (-1) );
	    Logger.trace( axis.getIdentifier() + " Normmax: " + normmax );
	    Logger.logInfo(Logger.TOPIC_LOG_AXIS, "normmax: "+normmax);
	    
	    // Das Intervall verwendet zunächst nur 10er Schritte
	    double interval = Math.pow( 10, rangepow * (-1) );
	    Logger.trace( axis.getIdentifier() + " Interval (" + axis.getIdentifier() + "): " + interval );
	    Logger.logInfo(Logger.TOPIC_LOG_AXIS, "Interval: "+interval);
	    
	    // hier werden alle Zahlen gespeichert, die als gute Divisoren eines Intervalls gelten
	    // 3 würde z.B. schnell krumme werte erzeugen
	    LinkedList<Integer> goodDivisors = new LinkedList<Integer>();
 
	    goodDivisors.add( 5 );
	    goodDivisors.add( 2 );
	    goodDivisors.add( 2 );
	    
	    
	    

	    int count = 0;
	    double oldi = 0;
	    double normmaxTolerance=normmax;
	    //Die Toleranz von Normmax soll vom Intervall abhängig sein; bei grossem normmax und festem
	    //Toleranzfaktor würden sonst zuviele Intervalle berechnetet
	    double toleranceFactor=0.1*interval;
    	normmaxTolerance=normmax+toleranceFactor;
	    	
	    
	    for( double i = normmin; i <= normmaxTolerance; i += interval )
	    {
	    	Logger.logInfo(Logger.TOPIC_LOG_AXIS, "Schleife i: "+i+":"+normmin+":"+normmaxTolerance);
	      if( count > 0 )
	      {
	    	  Logger.logInfo(Logger.TOPIC_LOG_AXIS, "Top Level: ");
	        findBetweens( oldi, i, minLogInterval, goodDivisors, ticks, 1 );
	      }
	      ticks.add( (new Double( i )) );

	      count++;
	      oldi = i;
	    }
	    Logger.logInfo(Logger.TOPIC_LOG_AXIS, "Letztes i: "+oldi);
	    Logger.logInfo(Logger.TOPIC_LOG_AXIS, "next would be: "+(oldi+interval)+" > "+normmaxTolerance);

	    // in realticks sind jetzt nur die wirklich zu zeichnenden werte drin
	    // wenn man das nicht über neues objekt, sondern ticks.remove macht, gibts eine concurrentmodificationexception
	    LinkedList<Number> realticks = new LinkedList<Number>();
	    for( Double tick : ticks )
	    {
	      double tickdv = tick.doubleValue();

	      //nur wenn alle gezeichnet werden sollen, dürfen die abgeschnittenen Randticks überleben
	      if( showAll ||  (tickdv >= logicalMin) && (tickdv <= logicalMax) )
	      {
	    	
	    	  /*
	    	   * Überprüfen, ob nicht schon ein naher tick vorhanden ist
	    	   * dazu darf der Abstand nicht kleiner als anzahl gültiger nks+2 sein
	    	   */
	    	 
	    	boolean hasequal=false;
	    	double epsilon=Math.pow(10, - (rangepow+4));
	    	for (Number realtick: realticks)
	    	{
	    		if (Math.abs(realtick.doubleValue() - tick) < epsilon)
	    		{
	    			hasequal=true;
	    			break;
	    		}
	    	}
	    	if (!hasequal)
	    		realticks.add( new Double( tickdv ) );
	      }
	    }

	    
	    //Anzahl der Nachkommastellen ausrechnen 
	    NumberFormat nf=new DecimalFormat();
	    
	    
	    // Für das Double-Format wird nun noch die Anzahl der Nachkommastellen aus der Potenz ermittelt
	    
	    if( rangepow >= 0 )
	    {
	      nf.setMaximumFractionDigits( rangepow+4);
	      nf.setMinimumFractionDigits( rangepow+4);
	    }
	    else
	    {
	    	
	   }
	    int digits=testNumberFormat(realticks.toArray(new Double[]{}));
	    nf.setMaximumFractionDigits(digits);
    	nf.setMinimumFractionDigits(digits);
	   
	    renderer.setNumberFormat(nf);
	    renderer.setTickMapElement(axis, realticks);

	    Logger.logInfo(Logger.TOPIC_LOG_AXIS, "Anzahl ticks: "+realticks.size());
	    SortedSet<Number> ss=new TreeSet<Number>(realticks);
	    for (Number realtick: ss)
	    {
	    	Logger.logInfo(Logger.TOPIC_LOG_AXIS, realtick.doubleValue()+"");
	    }
	    return realticks;
	  }

	

	/**
	   * recursive function which divides in interval into a Double of "divisions" and adds the values to a linked list the
	   * divisions have to have a larger range than the param interval
	   */
	  private static void findBetweens( double from, double to, double interval, List<Integer> divisors, List<Double> ticks, int level  )
	  {
	    
        if (interval < 1)
          return;
	    
		  String levelinset="";
	      for (int i =0; i<level; i++)
	      {
	    	  levelinset+="\t";
	      }
	//	System.out.println(levelinset+"Between: "+from+":"+to+":"+interval+"---------------------------------------");
	    for( Integer divisor : divisors )
	    {
	      double betweenrange = Math.abs( from - to ) / divisor.intValue();

	      // TODO: this prevents an endles loop below, if from == to
	      // @Alex:check if this is ok
	      if( betweenrange < 0.000000001 )
	        return;

	      double epsilon=0.00000001;
	      
	      
	      if( interval < betweenrange )
	      {
	        // vorher prüfen
	        int count = 0;
	        double oldi = 0;
	        for( double i = from; i <= to+epsilon; i += betweenrange )
	        {
	          if( count >= 1 )
	          {
	        	  Logger.logInfo(Logger.TOPIC_LOG_AXIS, levelinset+i);
	        	ticks.add( new Double( i ) );
	            findBetweens( oldi, i, interval, divisors, ticks, level+1 );
	          }
	          // Tick setzen
	          count++;
	          oldi = i;
	        }
	        break;
	      }
	    }
	  }
	  
	  
		/**
		 * testet, wieviele Nachkommastellen angezeigt werden müssen
		 * @param ticks
		 * @return
		 */
		public static int testNumberFormat(Double[] ticks)
		{
			//Probleme sind zu viele Nullen der Neunen
			int digits=0;
			
			for (Double tick:ticks)
			{
				boolean allgood=false;
				while (!allgood)
				{
					NumberFormat nf=new DecimalFormat();
					nf.setMaximumFractionDigits(digits);
					nf.setMinimumFractionDigits(digits);
					Double dtick=tick;
					Double stick=0.0;
					try
					{
						stick = ( nf.parse( nf.format( tick ) ) ).doubleValue();
					} catch (ParseException e)
					{
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					double epsilon=Math.abs(dtick-stick);
					Logger.logInfo(Logger.TOPIC_LOG_AXIS, "Ticks: (d/s)"+dtick+":"+stick+":"+epsilon);
					
					if (epsilon < 0.000000001 )
					{
						allgood=true;
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
	  
	  
	  private static Point getScreenMinMax(IAxis<Number> axis, IDataRange<Number> range, Point ticklabelSize)
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
			    return new Point(screenMin, screenMax);
		}
	  
}