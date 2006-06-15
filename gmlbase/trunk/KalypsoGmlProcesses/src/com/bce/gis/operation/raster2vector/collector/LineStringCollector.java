package com.bce.gis.operation.raster2vector.collector;

import java.util.Collection;

import org.deegree.model.feature.FeatureCollection;
import org.deegree.model.feature.FeatureType;
import org.deegree.model.feature.FeatureTypeProperty;
import org.deegree.model.geometry.GM_Exception;
import org.deegree.model.geometry.GM_Object;
import org.deegree_impl.model.geometry.JTSAdapter;

import com.bce.gis.operation.raster2vector.LinkedCoordinate;
import com.bce.gis.operation.raster2vector.LinkedCoordinateException;
import com.bce.gis.operation.raster2vector.SegmentCollector;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;

/**
 * Sammelt LineStrings
 * 
 * @author belger
 */
public class LineStringCollector implements SegmentCollector
{
	private static final FeatureTypeProperty GRENZEN_PROP = FF.createFeatureTypeProperty( FEATURE_BASE_ID + ".hoehe", Double.class.getName(), false );
	
	private static final FeatureType FT = FF.createFeatureType( null, null, FEATURE_BASE_ID + " - FeatureTypeID", new FeatureTypeProperty[] 
	{ 
		SHAPE_PROP,
		ID_PROP, 
		BEZ_PROP,
		GRENZEN_PROP
	} );
	
	private final GeometryFactory m_gf;
	private final double[] m_grenzen;
	private final boolean m_bSimple;
	
	private final FeatureCollection m_fc = FF.createFeatureCollection( "ShapeFeatureCollection", 0 );
	
	public LineStringCollector( final GeometryFactory gf, final double[] grenzen, final boolean bSimple )
	{
		m_grenzen = grenzen;
		m_gf = gf;
		m_bSimple = bSimple;
	}

	/**
	 * @see com.bce.gis.operation.raster2vector.SegmentCollector#addSegment(int, com.bce.gis.operation.raster2vector.LinkedCoordinate, com.bce.gis.operation.raster2vector.LinkedCoordinate, com.vividsolutions.jts.geom.Coordinate, com.vividsolutions.jts.geom.Coordinate)
	 */
	public void addSegment( final int index, final LinkedCoordinate lc0, final LinkedCoordinate lc1, final Coordinate nearC0, final Coordinate nearC1 ) throws LinkedCoordinateException
	{
		try
		{
			if( m_bSimple )
			{
				final Double id = new Double( m_fc.getSize() );
				final Double grenze = new Double( m_grenzen[index] );
				final String name = grenze.toString();
			
				final GM_Object gmGeo = JTSAdapter.wrap( m_gf.createLineString( new Coordinate[] { lc0.crd, lc1.crd } ) );
				m_fc.appendFeature( FF.createFeature( "" + m_fc.getSize(), FT, new Object[] { gmGeo, id, name, grenze } ) );
			}
			else
			{
				lc0.link( lc1 );
				
				if( !lc0.isCircle() )
					return;
				
				final Collection<LineString> newStrings = lc0.getLineStrings( m_gf );
					
				for( final LineString lineString : newStrings )
				{
						final GM_Object gmGeo = JTSAdapter.wrap( lineString );

						final Double id = new Double( m_fc.getSize() );
						final Double grenze = new Double( m_grenzen[index] );
						final String name = "" + m_grenzen[index]; 
							
						m_fc.appendFeature( FF.createFeature( "" + m_fc.getSize(), FT, new Object[] { gmGeo, id, name, grenze } ) );
				}
			}
		}
		catch( GM_Exception e )
		{
			e.printStackTrace();
		}
	}
	
	/**
	 * @see com.bce.gis.operation.raster2vector.SegmentCollector#getFeatures()
	 */
	public FeatureCollection getFeatures()
	{
		return m_fc;
	}
}
