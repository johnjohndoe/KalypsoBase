package org.kalypsodeegree_impl.tools.refinement;

import java.util.ArrayList;
import java.util.List;

import org.kalypsodeegree.model.geometry.GM_AbstractSurfacePatch;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_LineString;
import org.kalypsodeegree.model.geometry.GM_MultiPoint;
import org.kalypsodeegree.model.geometry.GM_MultiSurface;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

public class Refinement
{

  private static final double MAX_DISTANCE = .000001;

  public GM_Polygon[] doRefine( final GM_MultiSurface[] inputSurfaces, final GM_Object inputGeom ) throws GM_Exception
  {
    if( inputGeom instanceof GM_Curve )
    {
      return doRefineCurve( inputSurfaces, (GM_Curve)inputGeom );
    }
    else if( inputGeom instanceof GM_Polygon )
    {
      return doRefineSurface( inputSurfaces, (GM_Polygon)inputGeom );
    }
    else
    {
      return new GM_Polygon[0];
    }
  }

  private GM_Polygon[] doRefineSurface( final GM_MultiSurface[] inputSurfaces, final GM_Polygon inputSurface ) throws GM_Exception
  {
    final GM_Position[] exteriorRing = inputSurface.getSurfacePatch().getExteriorRing();
    final GM_Curve curve = GeometryFactory.createGM_Curve( exteriorRing, inputSurface.getCoordinateSystem() );
    return doRefine( inputSurfaces, curve );
  }

  private GM_Polygon[] doRefineCurve( final GM_MultiSurface[] inputSurfaces, final GM_Curve inputCurve ) throws GM_Exception
  {
    final List<GM_Polygon> list = new ArrayList<>();

    if( inputCurve.getStartPoint().equals( inputCurve.getEndPoint() ) )
    {
      final GM_LineString lineString = inputCurve.getAsLineString();
      final GM_Polygon surface = GeometryFactory.createGM_Surface( lineString.getPositions(), null, inputCurve.getCoordinateSystem() );
      GM_Object remainingSurface = surface;
      for( final GM_MultiSurface multiSurface : inputSurfaces )
      {
        for( final GM_Polygon gm_SurfacePatch : multiSurface.getAllSurfaces() )
        {
          if( remainingSurface != null )
          {
            remainingSurface = remainingSurface.difference( gm_SurfacePatch );
          }
        }
      }
      if( remainingSurface instanceof GM_MultiSurface )
      {
        final GM_Polygon[] allSurfaces = ((GM_MultiSurface)remainingSurface).getAllSurfaces();
        for( final GM_Polygon gm_Surface : allSurfaces )
          list.add( gm_Surface );
      }
      else if( remainingSurface instanceof GM_Polygon )
      {
        list.add( (GM_Polygon)remainingSurface );
      }
    }

    /* consider each surface */
    for( final GM_MultiSurface polygonSurface : inputSurfaces )
    {
      final GM_Object[] objects = polygonSurface.getAll();
      for( final GM_Object object : objects )
      {
        if( object instanceof GM_Polygon )
        {
          final GM_Polygon surface = (GM_Polygon)object;
          final GM_AbstractSurfacePatch surfacePatch = surface.getSurfacePatch();

          /* clip refinement curve with surface patch */
          final GM_Object preIntersection = inputCurve.intersection( surface );
          if( preIntersection instanceof GM_Point )
            continue;

          // convert patch to curve and get intersection points with refinment curve
          final GM_Position[] exterior = surfacePatch.getExteriorRing();
          final GM_Curve curve = GeometryFactory.createGM_Curve( exterior, surfacePatch.getCoordinateSystem() );
          final GM_Object intersection = curve.intersection( inputCurve );
          // this intersection just gives the x- and y-values, not the z!
          // so we have to compute the z-value ourselfes

          if( intersection instanceof GM_MultiPoint )
          {
            final List<GM_Point> pointList = new ArrayList<>();

            final GM_MultiPoint multiPoint = (GM_MultiPoint)intersection;
            final GM_Point[] points = multiPoint.getAllPoints();

            for( final GM_Point point : points )
            {
              if( Double.isNaN( point.getZ() ) )
                pointList.add( RefinementUtils.interpolateZ( point, exterior ) );
              else
                pointList.add( point );
            }

            final GM_Point[] intersectionPoints = pointList.toArray( new GM_Point[pointList.size()] );

            /* we consider only intersections that have one or two intersection points */
            if( intersectionPoints.length == 2 )
            {
              /* split surface */
              final GM_Position[] poses = new GM_Position[intersectionPoints.length];
              for( int j = 0; j < intersectionPoints.length; j++ )
                poses[j] = intersectionPoints[j].getPosition();

              final GM_Polygon[] surfaces = RefinementUtils.splitSurfacePatch( surfacePatch, poses );
              for( final GM_Polygon surface2 : surfaces )
                list.add( surface2 );
            }
            else
            {
              list.clear();
              return list.toArray( new GM_Polygon[list.size()] );
            }
          }

          else if( intersection instanceof GM_Point )
          {
            final List<GM_Point> pointList = new ArrayList<>();
            final GM_Point point = (GM_Point)intersection;

            if( Double.isNaN( point.getZ() ) )
              pointList.add( RefinementUtils.interpolateZ( point, exterior ) );
            else
              pointList.add( point );

            // TODO: find a good second intersection point on the patch
            // right now, we take the first point on the patch that does not lie on the segment that we want to split
            final String crs = point.getCoordinateSystem();
            final GM_Curve[] segments = RefinementUtils.getPositionsAsCurves( exterior, crs );

            for( int j = 0; j < segments.length; j++ )
            {
              final GM_Curve splitSegment = segments[j];
              final GM_Object gmobject = splitSegment.intersection( point );
              if( gmobject != null || splitSegment.distance( point ) < MAX_DISTANCE * 2 )
              {
                final GM_Point startPoint = splitSegment.getAsLineString().getStartPoint();
                final GM_Point endPoint = splitSegment.getAsLineString().getEndPoint();

                GM_Curve segment2 = segments[j];
                if( j > 0 )
                  segment2 = segments[j - 1];
                else
                  segment2 = segments[j + 1];

                final GM_Point startPoint2 = segment2.getAsLineString().getAsLineString().getStartPoint();
                final GM_Point endPoint2 = segment2.getAsLineString().getAsLineString().getEndPoint();

                if( startPoint2.equals( startPoint ) )
                  pointList.add( endPoint2 );
                else if( endPoint2.equals( startPoint ) )
                  pointList.add( startPoint2 );
                else if( startPoint2.equals( endPoint ) )
                  pointList.add( endPoint2 );
                else if( endPoint2.equals( endPoint ) )
                  pointList.add( startPoint2 );

                // TODO: check for not wanted intersections

                break;
              }
            }

            final GM_Point[] intersectionPoints = pointList.toArray( new GM_Point[pointList.size()] );

            /* split surface */
            final GM_Position[] poses = new GM_Position[intersectionPoints.length];
            for( int j = 0; j < intersectionPoints.length; j++ )
              poses[j] = intersectionPoints[j].getPosition();

            final GM_Polygon[] surfaces = RefinementUtils.splitSurfacePatch( surfacePatch, poses );
            for( final GM_Polygon surface2 : surfaces )
              list.add( surface2 );
          }
        }
      }
    }
    return list.toArray( new GM_Polygon[list.size()] );
  }

}
