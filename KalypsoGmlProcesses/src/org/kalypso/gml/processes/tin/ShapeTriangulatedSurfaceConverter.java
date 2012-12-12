/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.gml.processes.tin;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.gml.processes.KalypsoGmlProcessesPlugin;
import org.kalypso.gml.processes.constDelaunay.ConstraintDelaunayHelper;
import org.kalypso.gml.processes.i18n.Messages;
import org.kalypso.ogc.gml.serialize.ShapeSerializer;
import org.kalypso.shape.ShapeType;
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree.model.geometry.GM_MultiSurface;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree.model.geometry.GM_Triangle;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;
import org.kalypsodeegree_impl.gml.binding.shape.AbstractShape;
import org.kalypsodeegree_impl.gml.binding.shape.ShapeCollection;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * @author Holger Albert
 */
public class ShapeTriangulatedSurfaceConverter extends AbstractTriangulatedSurfaceConverter
{
  private final String m_sourceSrs;

  public ShapeTriangulatedSurfaceConverter( final String sourceSrs )
  {
    m_sourceSrs = sourceSrs;
  }

  @Override
  public GM_TriangulatedSurface convert( final URL sourceLocation, IProgressMonitor monitor ) throws CoreException
  {
    /* Monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    try
    {
      /* Monitor. */
      monitor.beginTask( Messages.getString( "ShapeTriangulatedSurfaceConverter_0" ), 100 ); //$NON-NLS-1$

      /* Open shape. */
      final URL shapeURL = new URL( sourceLocation.getProtocol() + ":" + sourceLocation.getPath() ); //$NON-NLS-1$
      final String file2 = shapeURL.getFile();
      final File file = new File( file2 );
      final String absolutePath = file.getAbsolutePath();
      final String shapeBase = FileUtilities.nameWithoutExtension( absolutePath );

      final ShapeCollection shapeCollection = ShapeSerializer.deserialize( shapeBase, m_sourceSrs );
      final ShapeType shapeType = shapeCollection.getShapeType();
      switch( shapeType )
      {
        case POLYGON:
        case POLYGONZ:
        case POLYGONM:
          break;
        default:
          return null;
      }
      final IFeatureBindingCollection<AbstractShape> shapes = shapeCollection.getShapes();
      if( shapes.isEmpty() )
        return GeometryFactory.createGM_TriangulatedSurface( m_sourceSrs );

      /* Convert the gm_surfaces.exterior rings into gm.triangle. */
      final GM_TriangulatedSurface gmSurface = org.kalypsodeegree_impl.model.geometry.GeometryFactory.createGM_TriangulatedSurface( m_sourceSrs );
      for( final AbstractShape shape : shapes )
      {
        final GM_Object geometry = shape.getGeometry();
        if( geometry instanceof GM_MultiSurface )
        {
          final GM_MultiSurface polygonSurface = (GM_MultiSurface)geometry;
          final List<GM_Triangle> triangles = ShapeTriangulatedSurfaceConverter.convertToTriangles( polygonSurface );

          /* Add the triangles into the gm_triangle_surfaces. */
          for( final GM_Triangle triangle : triangles )
          {
            gmSurface.add( triangle );
            monitor.subTask( String.format( Messages.getString( "ShapeTriangulatedSurfaceConverter_1" ), gmSurface.size() ) ); //$NON-NLS-1$
          }
        }
      }

      /* Monitor. */
      monitor.worked( 100 );

      return gmSurface;
    }
    catch( final Exception ex )
    {
      throw new CoreException( new Status( IStatus.ERROR, KalypsoGmlProcessesPlugin.PLUGIN_ID, ex.getLocalizedMessage(), ex ) );
    }
    finally
    {
      /* Monitor. */
      monitor.done();
    }
  }

  private static List<GM_Triangle> convertToTriangles( final GM_MultiSurface polygonSurface )
  {
    final List<GM_Triangle> triangleList = new ArrayList<>( polygonSurface.getSize() );

    final GM_Object[] objects = polygonSurface.getAll();
    for( final GM_Object object : objects )
    {
      if( object instanceof GM_Polygon )
      {
        final GM_Polygon surface = (GM_Polygon)object;
        final GM_Triangle[] triangles = ConstraintDelaunayHelper.triangulateSimple( surface );
        for( final GM_Triangle triangle : triangles )
          triangleList.add( triangle );
      }
    }
    return triangleList;
  }
}