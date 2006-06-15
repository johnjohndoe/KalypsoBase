package com.bce.gis.index;

import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import org.deegree.graphics.transformation.GeoTransform;
import org.deegree.model.feature.Feature;
import org.deegree.model.geometry.GM_Envelope;
import org.deegree.model.geometry.GM_Object;
import org.deegree.model.geometry.GM_Point;
import org.deegree.model.geometry.GM_Position;
import org.deegree_impl.model.geometry.GeometryFactory;

public class SplitSort
{
	public static boolean showIndexEnv = false;

	private SplitSortContainer myRootContainer = null;

	private List<Feature> m_features = new ArrayList<Feature>();

	private final GeometryFactory m_geometryFactory = new GeometryFactory();

	public SplitSort( final GM_Envelope env )
	{
		myRootContainer = new SplitSortContainer( null, env );
	}

	public boolean add( final Feature f )
	{
		final GM_Envelope env = getEnvelope( m_geometryFactory, f );
		spacialAdd( env, f );
		return m_features.add( f );
	}

	private void spacialAdd( final GM_Envelope env, final Feature object )
	{
		if( env == null )
			return;

		if( myRootContainer == null )
			myRootContainer = new SplitSortContainer( null, env );

		if( myRootContainer.getEnvelope().contains( env ) )
			myRootContainer.add( env, object );
		else
		{
			double maxX = env.getMax().getX();
			double maxY = env.getMax().getY();
			double minX = env.getMin().getX();
			double minY = env.getMin().getY();

			GM_Envelope envRoot = myRootContainer.getEnvelope();

			double maxXroot = envRoot.getMax().getX();
			double maxYroot = envRoot.getMax().getY();
			double minXroot = envRoot.getMin().getX();
			double minYroot = envRoot.getMin().getY();
			GM_Envelope newEnv = m_geometryFactory.createGM_Envelope( minX < minXroot ? minX : minXroot, minY < minYroot ? minY : minYroot, maxX > maxXroot ? maxX : maxXroot, maxY > maxYroot ? maxY : maxYroot );

			SplitSortContainer newRootContainer = new SplitSortContainer( null, newEnv );
			myRootContainer.setParent( newRootContainer );
			newRootContainer.createSubContainers( myRootContainer );
			myRootContainer = newRootContainer;
			myRootContainer.add( env, object );
		}
	}

	public List<Feature> query( GM_Envelope queryEnv, List<Feature> result )
	{
		if( result == null )
			result = new ArrayList<Feature>();
		if( myRootContainer != null )
			result = myRootContainer.query( queryEnv, result );
		return result;
	}

	public List<Feature> query( GM_Position pos, List<Feature> result )
	{
		if( result == null )
			result = new ArrayList<Feature>();
		return query( m_geometryFactory.createGM_Envelope( pos, pos ), result );
	}

	public List<Feature> queryAll( final List<Feature> list )
	{
		List<Feature> result = list;
		if( result == null )
			result = new ArrayList<Feature>();

		result.addAll( m_features );
		return result;
	}

	public static GM_Envelope getEnvelope( final GeometryFactory gf, final Feature f )
	{
		final GM_Object geom = f.getDefaultGeometryProperty();
		final GM_Envelope env;
		if( !( geom instanceof GM_Point ) )
			env = geom.getEnvelope();
		else
		{
			GM_Position pos = ( (GM_Point)geom ).getPosition();
			GM_Envelope env2 = gf.createGM_Envelope( pos, pos );
			env = env2;
		}
		return env;
	}

	public void paint( Graphics g, GeoTransform geoTransform )
	{
		if( myRootContainer != null )
			myRootContainer.paint( g, geoTransform );
	}

	public int rsize()
	{
		if( myRootContainer != null )
			return myRootContainer.rsize();
		return 0;
	}

	public GM_Envelope getBoundingBox()
	{
		// TODO es muss die boundingbox aus den Objecten innerhalb des
		// rootcontainers gebildet werden
		if( myRootContainer != null )
			return myRootContainer.getEnvelope();
		return null;
	}

	public void resort()
	{
		myRootContainer = null;

		GM_Envelope bbox = null;

		for( final Feature f : m_features )
		{
			final GM_Envelope envelope = getEnvelope( m_geometryFactory, f );
			if( bbox == null )
				bbox = envelope;
			else
				bbox = getMerged( bbox, envelope );
		}
		
		if( bbox != null )
		{
			myRootContainer = new SplitSortContainer( null, bbox );
			for( final Feature feature : m_features )
			{
				GM_Envelope envelope = getEnvelope( m_geometryFactory, feature );
				spacialAdd( envelope, feature );
			}
		}
		else
			myRootContainer = null;
	}

	private GM_Envelope getMerged( final GM_Envelope first, GM_Envelope envelope )
	{
		double minx = first.getMin().getX();
		double miny = first.getMin().getY();
		double maxx = first.getMax().getX();
		double maxy = first.getMax().getY();
		if( envelope != null )
		{
			if( envelope.getMin().getX() < minx )
			{
				minx = envelope.getMin().getX();
			}
			if( envelope.getMin().getY() < miny )
			{
				miny = envelope.getMin().getY();
			}
			if( envelope.getMax().getX() > maxx )
			{
				maxx = envelope.getMax().getX();
			}
			if( envelope.getMax().getY() > maxy )
			{
				maxy = envelope.getMax().getY();
			}
		}
		return m_geometryFactory.createGM_Envelope( minx, miny, maxx, maxy );
	}
}