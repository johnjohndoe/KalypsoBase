package org.kalypso.grid;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.URL;

import org.kalypso.grid.GeoGridUtilities.Interpolation;
import org.kalypsodeegree.model.coverage.RangeSetFile;
import org.kalypsodeegree.model.elevation.ElevationException;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridCoverage;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

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

  private final RangeSetFile m_rangeSet;

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
    m_rangeSet = rgcFeature.getRangeSet();
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
    if( m_rangeSet == null )
      return null;

    try
    {
      return new URL( m_context, m_rangeSet.getFileName() );
    }
    catch( final IOException e )
    {
      throw new GeoGridException( "Failed to access grid-file", e );
    }
  }

  protected synchronized IGeoGrid getGrid( ) throws GeoGridException
  {
    if( m_grid == null )
    {
      try
      {
        if( m_rangeSet != null )
        {
          final URL url = new URL( m_context, m_rangeSet.getFileName() );
          m_grid = GeoGridUtilities.openGrid( m_rangeSet.getMimeType(), url, m_origin, m_offsetX, m_offsetY, m_sourceCRS, m_writeable );
        }
      }
      catch( final IOException e )
      {
        throw new GeoGridException( "Failed to access grid-file", e );
      }
    }

    return m_grid;
  }

  @Override
  public Envelope getEnvelope( ) throws GeoGridException
  {
    return GeoGridUtilities.toEnvelope( this );
  }

  @Override
  public Coordinate getOrigin( )
  {
    return m_origin;
  }

  @Override
  public int getSizeX( )
  {
    return m_sizeX;
  }

  @Override
  public int getSizeY( )
  {
    return m_sizeY;
  }

  @Override
  public Coordinate getOffsetX( )
  {
    return m_offsetX;
  }

  @Override
  public Coordinate getOffsetY( )
  {
    return m_offsetY;
  }

  @Override
  public void dispose( )
  {
    if( m_grid != null )
      m_grid.dispose();

    m_grid = null;
  }

  @Override
  public void close( ) throws Exception
  {
    // do not dispose the grid, we access it via the weak-cache
    if( m_grid != null )
      m_grid.close();
  }

  @Override
  public double getValueChecked( final int x, final int y ) throws GeoGridException
  {
    if( x < 0 || x >= getSizeX() || y < 0 || y >= getSizeY() )
      return Double.NaN;

    return getValue( x, y );
  }

  @Override
  public IGeoWalkingStrategy getWalkingStrategy( ) throws GeoGridException
  {
    final IGeoGrid grid = getGrid();
    if( grid == null )
      return null;

    return grid.getWalkingStrategy();
  }

  @Override
  public double getValue( final Coordinate crd ) throws GeoGridException
  {
    final IGeoGrid grid = getGrid();
    if( grid == null )
      return Double.NaN;

    return grid.getValue( crd );
  }

  @Override
  public BigDecimal getMax( ) throws GeoGridException
  {
    final IGeoGrid grid = getGrid();
    if( grid == null )
      return null;

    return grid.getMax();
  }

  @Override
  public BigDecimal getMin( ) throws GeoGridException
  {
    final IGeoGrid grid = getGrid();
    if( grid == null )
      return null;

    return grid.getMin();
  }

  @Override
  public GM_Polygon getCell( final int x, final int y, final String targetCRS ) throws GeoGridException
  {
    final IGeoGrid grid = getGrid();
    if( grid == null )
      return null;

    return grid.getCell( x, y, targetCRS );
  }

  @Override
  public String getSourceCRS( )
  {
    return m_sourceCRS;
  }

  @Override
  public GM_Polygon getSurface( final String targetCRS ) throws GeoGridException
  {
    final IGeoGrid grid = getGrid();
    if( grid == null )
      return null;

    return grid.getSurface( targetCRS );
  }

  @Override
  public GM_Envelope getBoundingBox( ) throws ElevationException
  {
    final GM_Polygon surface = getSurface( m_sourceCRS );
    return surface.getEnvelope();
  }

  @Override
  public double getElevation( final GM_Point location ) throws ElevationException
  {
    final Coordinate locationCrd = JTSAdapter.export( location.getPosition() );
    final Coordinate sourceCoordinate = GeoGridUtilities.transformCoordinate( this, locationCrd, location.getCoordinateSystem() );

    return GeoGridUtilities.getValue( this, sourceCoordinate, Interpolation.bilinear );
  }

  @Override
  public double getMinElevation( ) throws ElevationException
  {
    final BigDecimal min = getMin();
    if( min == null )
      return Double.NaN;

    return min.doubleValue();
  }

  @Override
  public double getMaxElevation( ) throws ElevationException
  {
    final BigDecimal max = getMax();
    if( max == null )
      return Double.NaN;

    return max.doubleValue();
  }
}