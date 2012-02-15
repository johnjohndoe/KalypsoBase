package org.kalypso.grid;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;

import javax.xml.namespace.QName;

import org.kalypso.commons.xml.NS;
import org.kalypsodeegree.model.coverage.RangeSetFile;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridCoverage;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;

/**
 * {@link IGeoGrid} implementation based on {@link org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridCoverage}s.<br>
 * This implementation analyzes the wrapped coverage and generates a suitable grid, to which all calls are delegated.
 *
 * @author Gernot Belger
 */
public class RectifiedGridCoverageGeoGrid implements IGeoGrid
{
  private final int m_sizeX;

  private final int m_sizeY;

  private final Coordinate m_origin;

  private final Coordinate m_offsetX;

  private final Coordinate m_offsetY;

  private final String m_sourceCRS;

  private final Object m_rangeSet;

  private final URL m_context;

  private IGeoGrid m_grid;

  private final boolean m_writeable;

  public RectifiedGridCoverageGeoGrid( final RectifiedGridCoverage rgcFeature )
  {
    this( rgcFeature, null );
  }

  public RectifiedGridCoverageGeoGrid( final RectifiedGridCoverage rgcFeature, final URL context )
  {
    this( rgcFeature, context, false );
  }

  protected RectifiedGridCoverageGeoGrid( final RectifiedGridCoverage rgcFeature, final URL context, final boolean writeable )
  {
    m_writeable = writeable;
    if( context == null )
      m_context = rgcFeature.getWorkspace().getContext();
    else
      m_context = context;

    final RectifiedGridDomain domain = rgcFeature.getGridDomain();
    m_rangeSet = rgcFeature.getProperty( new QName( NS.GML3, "rangeSet" ) );
    GM_Point origin = null;
    try
    {
      origin = domain.getOrigin( null );
    }
    catch( final Exception e )
    {
      // Ignore: will never happen, as we are giving 'null' to getOrigin; change this if there will ever be a crs for
      // domain
      e.printStackTrace();
    }
    m_origin = new Coordinate( origin.getX(), origin.getY() );
    m_offsetX = new Coordinate( domain.getOffsetX().getGeoX(), domain.getOffsetX().getGeoY() );
    m_offsetY = new Coordinate( domain.getOffsetY().getGeoX(), domain.getOffsetY().getGeoY() );
    m_sourceCRS = domain.getCoordinateSystem();

    m_sizeX = domain.getNumColumns();
    m_sizeY = domain.getNumRows();
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getValue(int, int)
   */
  @Override
  public final double getValue( final int x, final int y ) throws GeoGridException
  {
    final IGeoGrid grid = getGrid();
    if( grid == null )
      return Double.NaN;

    return grid.getValue( x, y );
  }

  public URL getGridURL( ) throws GeoGridException
  {
    if( m_rangeSet != null )
    {
      try
      {
        if( m_rangeSet instanceof RangeSetFile )
        {
          final RangeSetFile file = (RangeSetFile) m_rangeSet;
          return new URL( m_context, file.getFileName() );
        }
        else
          throw new UnsupportedOperationException( "Only FileSet rangeSets supported by now" );
      }
      catch( final IOException e )
      {
        throw new GeoGridException( "Could not access grid-file", e );
      }
    }

    return null;
  }

  protected synchronized IGeoGrid getGrid( ) throws GeoGridException
  {
    if( m_grid == null )
    {
      try
      {
        if( m_rangeSet instanceof RangeSetFile )
        {
          final RangeSetFile file = (RangeSetFile) m_rangeSet;
          final URL url = new URL( m_context, file.getFileName() );

          m_grid = GeoGridUtilities.openGrid( file.getMimeType(), url, m_origin, m_offsetX, m_offsetY, m_sourceCRS, m_writeable );
        }
        else
          throw new UnsupportedOperationException( "Only FileSet rangeSets supported by now" );
      }
      catch( final IOException e )
      {
        throw new GeoGridException( "Could not access grid-file", e );
      }
    }

    return m_grid;
  }

  @Override
  public Envelope getEnvelope( ) throws GeoGridException
  {
    return GeoGridUtilities.toEnvelope( this );
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getOrigin()
   */
  @Override
  public Coordinate getOrigin( )
  {
    return m_origin;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getSizeX()
   */
  @Override
  public int getSizeX( )
  {
    return m_sizeX;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getSizeY()
   */
  @Override
  public int getSizeY( )
  {
    return m_sizeY;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getOffsetX()
   */
  @Override
  public Coordinate getOffsetX( )
  {
    return m_offsetX;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getOffsetY()
   */
  @Override
  public Coordinate getOffsetY( )
  {
    return m_offsetY;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_grid != null )
      m_grid.dispose();
  }

  @Override
  public void close( ) throws Exception
  {
    // do not dispose the grid, we access it via the weak-cache
    if( m_grid != null )
      m_grid.close();
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getValueChecked(int, int)
   */
  @Override
  public double getValueChecked( final int x, final int y ) throws GeoGridException
  {
    if( x < 0 || x >= getSizeX() || y < 0 || y >= getSizeY() )
      return Double.NaN;

    return getValue( x, y );
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getWalkingStrategy()
   */
  @Override
  public IGeoWalkingStrategy getWalkingStrategy( ) throws GeoGridException
  {
    final IGeoGrid grid = getGrid();
    if( grid == null )
      return null;

    return grid.getWalkingStrategy();
  }

  /**
   * @see org.kalypso.grid.IGeoValueProvider#getValue(com.vividsolutions.jts.geom.Coordinate)
   */
  @Override
  public double getValue( final Coordinate crd ) throws GeoGridException
  {
    final IGeoGrid grid = getGrid();
    if( grid == null )
      return Double.NaN;

    return grid.getValue( crd );
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getMax()
   */
  @Override
  public BigDecimal getMax( ) throws GeoGridException
  {
    final IGeoGrid grid = getGrid();
    if( grid == null )
      return null;

    return grid.getMax();
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getMin()
   */
  @Override
  public BigDecimal getMin( ) throws GeoGridException
  {
    final IGeoGrid grid = getGrid();
    if( grid == null )
      return null;

    return grid.getMin();
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#setMax(java.math.BigDecimal)
   */
  @Override
  public void setMax( final BigDecimal maxValue ) throws GeoGridException
  {
    final IGeoGrid grid = getGrid();
    if( grid != null )
      grid.setMax( maxValue );
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#setMin(java.math.BigDecimal)
   */
  @Override
  public void setMin( final BigDecimal minValue ) throws GeoGridException
  {
    final IGeoGrid grid = getGrid();
    if( grid != null )
      grid.setMin( minValue );
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getCell(int, int, java.lang.String)
   */
  @Override
  public GM_Surface< ? > getCell( final int x, final int y, final String targetCRS ) throws GeoGridException
  {
    final IGeoGrid grid = getGrid();
    if( grid == null )
      return null;

    return grid.getCell( x, y, targetCRS );
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getSourceCRS()
   */
  @Override
  public String getSourceCRS( )
  {
    return m_sourceCRS;
  }

  /**
   * @see org.kalypso.grid.IGeoGrid#getSurface(java.lang.String)
   */
  @Override
  public GM_Surface< ? > getSurface( final String targetCRS ) throws GeoGridException
  {
    final IGeoGrid grid = getGrid();
    if( grid == null )
      return null;

    return grid.getSurface( targetCRS );
  }
}