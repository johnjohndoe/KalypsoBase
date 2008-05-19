package org.kalypso.gml.processes.raster2vector.ringtree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

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
  public final int index;

  public final LinearRing linearRing;

  public final Polygon polygon;

  public final PointInRing pir;

  public final Coordinate innerCrd;

  private final Collection<RingTreeElement> m_children = new ArrayList<RingTreeElement>();

  private final Collection<RingTreeElement> m_unmodChildren = Collections.unmodifiableCollection( m_children );

  public RingTreeElement( final LinearRing lr, final Polygon polygon, final int index, final Coordinate innerCrd )
  {
    this.linearRing = lr;
    this.polygon = polygon;
    this.index = index;
    this.innerCrd = innerCrd;

    this.pir = (lr == null) ? null : new SIRtreePointInRing( lr );
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
    return "[" + index + ", " + linearRing + "]"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }

  public Polygon[] getAsPolygon( )
  {
    Geometry result = (Geometry) polygon.clone();
    for( final RingTreeElement child : m_children )
      result = result.difference( child.polygon );

    if( result.isEmpty() )
      return null;

    if( result instanceof Polygon )
      return new Polygon[] { (Polygon) result };

    if( result instanceof MultiPolygon )
    {
      MultiPolygon mp = (MultiPolygon) result;
      final Polygon[] mpArray = new Polygon[mp.getNumGeometries()];
      for( int i = 0; i < mpArray.length; i++ )
        mpArray[i] = (Polygon) mp.getGeometryN( i );
      return mpArray;
    }

    System.out.println( "Strange result: " + result );
    return null;
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

}
