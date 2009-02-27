package de.belger.swtchart.util;

/**
 * @author Fichtner
 */
public class MathUtil
{
  public static enum RoundMethod
  {
    UP
    {
      @Override
      public double round( final double d )
      {
        return Math.ceil( d );
      }
    },
    DOWN
    {
      @Override
      public double round( final double d )
      {
        return Math.floor( d );
      }
    },
    HALF_UP
    {
      @Override
      public double round( final double d )
      {
        return Math.rint( d );
      }
    };

    public abstract double round( final double d );
  }

  public static double round( double d, final RoundMethod method )
  {
    int faktor = 1;
    boolean negative = false;

    if( d == 0 || Double.isInfinite( d ) )
      return d;

    if( d < 0 )
    {
      d = -d;
      negative = true;
    }

    if( d < 1 )
    {
      while( d < 1 )
      {
        d = d * 10;
        faktor = faktor * 10;
      }

      d = method.round( d );
      d = d / faktor;
    }
    else if( d > 10 )
    {
      while( d > 10 )
      {
        d = d / 10;
        faktor = faktor * 10;
      }

      d = method.round( d );

      d = d * faktor;
    }
    else
      d = method.round( d );

    return negative ? -d : d;
  }

  /**
   * Gibt die Anzahl der Stellen nach dem Komma bis zur ersten Zahl != 0
   */
  public static int scale( double d )
  {
    double abs = Math.abs( d );

    if( abs > 1 )
      return 0;

    double bruchTeil = abs - Math.floor( abs );
    int scale = 0;

    if( bruchTeil > 0 )
    {
      while( bruchTeil < 1 )
      {
        bruchTeil = bruchTeil * 10;
        scale++;
      }
    }

    return scale;
  }

  /**
   * Rundet die Zahl d auf die Position scale mit einer bestimmten Rundungsmethode roundMethod
   */
  public static double setScale( double d, final int scale, final RoundMethod method )
  {
    int c = 0;
    long faktor = 1;

    while( c < scale )
    {
      d = d * 10;
      faktor = faktor * 10;
      c++;
    }

    d = method.round( d );

    // den Originalwert wiederherstellen
    d = d / faktor;

    return d;
  }

  public static final double nanMin( final double v1, final double v2 )
  {
    if( Double.isNaN( v1 ) )
      return v2;

    if( Double.isNaN( v2 ) )
      return v1;

    return Math.min( v1, v2 );
  }

  public static final double nanMax( final double v1, final double v2 )
  {
    if( Double.isNaN( v1 ) )
      return v2;

    if( Double.isNaN( v2 ) )
      return v1;

    return Math.max( v1, v2 );
  }
}
