package org.kalypso.gml.processes.raster2vector.ringtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import org.kalypso.gml.processes.raster2vector.collector.PolygonCollector.Interval;

import com.vividsolutions.jts.algorithm.PointInRing;
import com.vividsolutions.jts.algorithm.SIRtreePointInRing;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @author Gernot Belger
 */
public class RingTreeElement
{
  /* value of the border of the outer ring */
  public final double lineValue;

  private Interval m_polygonIntervall = null;

  public final LinearRing linearRing;

  public final Polygon polygon;

  public final PointInRing pir;

  public final Coordinate innerCrd;

  private final Collection<RingTreeElement> m_children = new ArrayList<RingTreeElement>();

  private final Collection<RingTreeElement> m_unmodChildren = Collections.unmodifiableCollection( m_children );

  private final String m_id;

  @SuppressWarnings("hiding")
  public RingTreeElement( final LinearRing lr, final Polygon polygon, final double lineValue, final Coordinate innerCrd, final String id )
  {
    linearRing = lr;
    this.polygon = polygon;
    this.lineValue = lineValue;
    this.innerCrd = innerCrd;
    m_id = id;

    pir = (lr == null) ? null : new SIRtreePointInRing( lr );
  }

  public String getId( )
  {
    return m_id;
  }

  public void addChild( final RingTreeElement rte )
  {
    m_children.add( rte );
  }

  public void removeChild( final RingTreeElement rte )
  {
    m_children.remove( rte );
  }

  public Collection<RingTreeElement> children( )
  {
    return m_unmodChildren;
  }

  @Override
  public String toString( )
  {
    return "[" + lineValue + ", " + linearRing + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public Integer initIntervals( final Interval[] intervals )
  {
    final SortedSet<Integer> intervalSet = new TreeSet<Integer>();
    for( final RingTreeElement child : m_children )
    {
      final Integer childInterval = child.initIntervals( intervals );
      if( childInterval != null )
        intervalSet.add( childInterval );
    }

    if( Double.isNaN( lineValue ) )
      return null;

    if( innerCrd == null )
    {
// System.out.println();
    }

    if( !isInnerCrdInChild() )
    {
      // get interval from value of the inner coordinate
      for( int i = 0; i < intervals.length; i++ )
      {
        if( intervals[i].contains( innerCrd.z ) )
        {
          m_polygonIntervall = intervals[i];
          return i;
        }
      }

      return null;
    }

    /*
     * If the inner crd is inside a child, we have a strange algorithm, that determines our own interval depending on
     * the interval lof its children. TODO: explanation....
     */

    System.out.println( "No good inner coordinate for: " + m_id );
    switch( intervalSet.size() )
    {
      case 0:

        double childValue = Double.NaN;
        for( final RingTreeElement child : m_children )
        {
          if( !Double.isNaN( child.lineValue ) )
          {
            childValue = child.lineValue;
            break;
          }
        }

        if( !Double.isNaN( childValue ) )
        {
          final double min = Math.min( lineValue, childValue );
          final double max = Math.max( lineValue, childValue );
          final Interval guessedInterval = new Interval( min, max );
          for( int i = 0; i < intervals.length; i++ )
          {
            if( Double.compare( intervals[i].getMin(), guessedInterval.getMin() ) == 0 && Double.compare( intervals[i].getMax(), guessedInterval.getMax() ) == 0 )
            {
              m_polygonIntervall = intervals[i];
              return i;
            }
          }
        }

        // we have only Double.NaN holes... what to do?
        // for now, we try to use the inner coordinate....

        // get interval from value of the inner coordinate
        for( int i = 0; i < intervals.length; i++ )
        {
          if( intervals[i].contains( innerCrd.z ) )
          {
            m_polygonIntervall = intervals[i];
            return i;
          }
        }

        throw new IllegalStateException( "This may not happen...." );

      case 1:
        final Integer interval = intervalSet.first();
        final Interval childInterval = intervals[interval];
        final double distMin = Math.abs( childInterval.getMin() - lineValue );
        final double distMax = Math.abs( childInterval.getMax() - lineValue );

        if( distMin < distMax )
        {
          m_polygonIntervall = intervals[interval - 1];
          return interval - 1;
        }
        else
        {
          m_polygonIntervall = intervals[interval + 1];
          return interval + 1;
        }

      case 2:
        final int first = intervalSet.first();

/* removed because of assertion failed - 100 classes */
// Assert.isTrue( last - first == 2 );
        m_polygonIntervall = intervals[first + 1];
        return first + 1;

      default:
        throw new IllegalStateException( "This may not happen...." );
    }
  }

  private boolean isInnerCrdInChild( )
  {
    for( final RingTreeElement child : m_children )
    {
      if( child.pir.isInside( innerCrd ) )
        return true;
    }

    return false;
  }

  public Polygon[] getAsPolygon( )
  {
    final Geometry result = getPolygonInternal();

    if( result.isEmpty() )
      return null;

    if( result instanceof Polygon )
      return new Polygon[] { (Polygon) result };

    if( result instanceof MultiPolygon )
    {
      final MultiPolygon mp = (MultiPolygon) result;
      final Polygon[] mpArray = new Polygon[mp.getNumGeometries()];
      for( int i = 0; i < mpArray.length; i++ )
        mpArray[i] = (Polygon) mp.getGeometryN( i );
      return mpArray;
    }

    System.out.println( "Strange result: " + result );
    return null;
  }

  private Geometry getPolygonInternal( )
  {
    Geometry result = (Geometry) polygon.clone();
    for( final RingTreeElement child : m_children )
      result = result.difference( child.polygon );
    return result;
  }

  public boolean hasChildren( )
  {
    return !m_children.isEmpty();
  }

  public RingTreeElement getFirstChild( )
  {
    return m_children.iterator().next();
  }

  public boolean contains( final RingTreeElement element )
  {
    return polygon.contains( element.polygon );
  }

  public Interval getPolygonIntervall( )
  {
    return m_polygonIntervall;
  }

}
