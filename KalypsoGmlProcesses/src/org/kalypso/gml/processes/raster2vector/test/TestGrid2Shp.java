/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.gml.processes.raster2vector.test;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.junit.Ignore;
import org.junit.Test;
import org.kalypso.commons.java.util.zip.ZipUtilities;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.gml.processes.raster2vector.Raster2Lines;
import org.kalypso.gml.processes.raster2vector.Raster2LinesWalkingStrategy;
import org.kalypso.gml.processes.raster2vector.collector.CollectorDataProvider;
import org.kalypso.gml.processes.raster2vector.collector.LineStringCollector;
import org.kalypso.gml.processes.raster2vector.collector.PolygonCollector;
import org.kalypso.gml.processes.raster2vector.collector.SegmentCollector;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.grid.ConvertAscii2Binary;
import org.kalypso.grid.GeoGridUtilities;
import org.kalypso.grid.IGeoGrid;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.shape.ShapeDataException;
import org.kalypso.shape.ShapeFile;
import org.kalypso.shape.ShapeType;
import org.kalypso.shape.dbf.DBFField;
import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.shape.dbf.FieldType;
import org.kalypso.shape.dbf.IDBFField;
import org.kalypso.shape.deegree.GM_Object2Shape;
import org.kalypso.shape.geometry.ISHPGeometry;
import org.kalypso.shape.shp.SHPException;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.coverage.RangeSetFile;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridCoverage;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain;

import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * JUnit Test Case for converting a grid into line and polygon shape.<br>
 * This test extracts demo input data (grid) from resources and converts them into shape files. <br>
 * <br>
 * Run this test as plug-in test.
 *
 * @author Thomas Jung
 */
public class TestGrid2Shp
{
  private static final GeometryFactory GF = new GeometryFactory();

  @Test
  // Ignore for normal testing, this takes much too long
  @Ignore
  public void testRiskModel( ) throws Exception
  {
    // unzip test data into workspace
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    final IProject project = workspace.getRoot().getProject( "GridConvertingTest" ); //$NON-NLS-1$
    project.create( new NullProgressMonitor() );

    final URL zipLocation = getClass().getResource( "/etc/test/resources/raster2vector/grids.zip" ); //$NON-NLS-1$
    ZipUtilities.unzip( zipLocation, project, new NullProgressMonitor() );

    // run test model
    final IFolder importDataFolder = project.getFolder( "grids" ); //$NON-NLS-1$

    final IGeoGrid raster = getTestGrid( importDataFolder );

    final boolean bSimple = false;
    final boolean bIsolines = false;

    final double[] grenzen = new double[] { 0.0, 0.5, 1000 };

    final SegmentCollector collector = bIsolines ? (SegmentCollector) new LineStringCollector( GF, grenzen, bSimple ) : new PolygonCollector( GF, grenzen, bSimple );
    final Raster2Lines r2l = new Raster2Lines( collector, grenzen );

    new Raster2LinesWalkingStrategy().walk( raster, r2l, null, new NullProgressMonitor() );

    final CollectorDataProvider[] data = collector.getData();

    // Shape schreiben
    if( collector instanceof PolygonCollector )
      writePolygonShape( data, importDataFolder );
    else
      writeLineShape( data, importDataFolder );

  }

  private void writeLineShape( final CollectorDataProvider[] data, final IFolder importDataFolder ) throws DBaseException, IOException, ShapeDataException, SHPException
  {
    final IDBFField doubleField = new DBFField( "ID", FieldType.N, (short) 10, (short) 3 ); //$NON-NLS-1$
    final IDBFField stringField = new DBFField( "NAME", FieldType.C, (short) 100, (short) 0 ); //$NON-NLS-1$
    final IDBFField[] fields = new IDBFField[] { doubleField, stringField };

    final ShapeType shapeType = ShapeType.POLYLINEZ;
    final String shapeBase = importDataFolder.getLocation() + "export_isoline"; //$NON-NLS-1$

    try (final ShapeFile shape = ShapeFile.create( shapeBase, shapeType, Charset.defaultCharset(), fields ))
    {
      final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
      final GM_Object2Shape gm2shp = new GM_Object2Shape( shapeType, crs );

      for( final CollectorDataProvider element : data )
      {
        final GM_Curve line = (GM_Curve) element.getGeometry();
        final ISHPGeometry geom = gm2shp.convert( line );

        final Double id = element.getId();
        final String[] name = element.getName();

        final Object[] shapeData = new Object[] { id, name[0] };
        shape.addFeature( geom, shapeData );
      }
    }
  }

  private void writePolygonShape( final CollectorDataProvider[] data, final IFolder importDataFolder ) throws DBaseException, IOException, ShapeDataException, SHPException
  {
    final IDBFField doubleField = new DBFField( "ID", FieldType.N, (short) 10, (short) 3 ); //$NON-NLS-1$
    final IDBFField fromField = new DBFField( "FROM", FieldType.N, (short) 10, (short) 3 ); //$NON-NLS-1$
    final IDBFField toField = new DBFField( "TO", FieldType.N, (short) 10, (short) 3 ); //$NON-NLS-1$
    final IDBFField rangeField = new DBFField( "RANGE", FieldType.C, (short) 100, (short) 0 ); //$NON-NLS-1$
    final IDBFField internalField = new DBFField( "INTERNALID", FieldType.C, (short) 100, (short) 0 ); //$NON-NLS-1$
    final IDBFField[] fields = new IDBFField[] { doubleField, fromField, toField, rangeField, internalField };

    final ShapeType shapeType = ShapeType.POLYGONZ;
    final String shapeBase = importDataFolder.getLocation() + "export_polygon_"; //$NON-NLS-1$

    try (final ShapeFile shape = ShapeFile.create( shapeBase, shapeType, Charset.defaultCharset(), fields ))
    {
      final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
      final GM_Object2Shape gm2shp = new GM_Object2Shape( shapeType, crs );

      for( final CollectorDataProvider element : data )
      {
        final GM_Polygon line = (GM_Polygon) element.getGeometry();
        final ISHPGeometry geom = gm2shp.convert( line );

        final Double id = element.getId();
        final Double[] borders = element.getBorders();
        final Double from = borders[0];
        final Double to = borders[1];
        final String[] name = element.getName();

        final Object[] shapeData = new Object[] { id, from, to, name[0], name[1] };
        shape.addFeature( geom, shapeData );
      }
    }
  }

  private IGeoGrid getTestGrid( final IFolder importDataFolder ) throws Exception, MalformedURLException, IOException
  {
    // load models
    final IFile covCollFile = importDataFolder.getFile( new Path( "covColl.gml" ) ); //$NON-NLS-1$
    final GMLWorkspace covCollWorkspace = GmlSerializer.createGMLWorkspace( ResourceUtilities.createURL( covCollFile ), null );

    final IFile file = importDataFolder.getFile( "floodZones.asc" ); //$NON-NLS-1$

    final String binFileName = file.getName() + ".bin"; //$NON-NLS-1$
    final String dstFileName = binFileName;
    final IFile dstRasterIFile = importDataFolder.getFile( dstFileName );
    final File dstRasterFile = dstRasterIFile.getRawLocation().toFile();
    final RectifiedGridDomain gridDomain = importAsBinaryRaster( file.getLocation().toFile(), dstRasterFile, "EPSG:28992", new NullProgressMonitor() ); //$NON-NLS-1$

    final IFeatureType ft = GMLSchemaUtilities.getFeatureTypeQuiet( RectifiedGridCoverage.QNAME );
    final Feature rootFeature = covCollWorkspace.getRootFeature();
    final ICoverageCollection covColl = (ICoverageCollection) rootFeature.getAdapter( ICoverageCollection.class );

    final IRelationType parentRelation = (IRelationType) covColl.getFeatureType().getProperty( ICoverageCollection.QNAME_PROP_COVERAGE_MEMBER );
    final Feature coverageFeature = covCollWorkspace.createFeature( covColl, parentRelation, ft );
    final RectifiedGridCoverage coverage = (RectifiedGridCoverage) coverageFeature;
    covColl.getCoverages().add( coverage );

    final RangeSetFile rangeSetFile = new RangeSetFile( binFileName );
    rangeSetFile.setMimeType( "image/bin" ); //$NON-NLS-1$

    covColl.getCoverages().add( coverage );
    coverage.setRangeSet( rangeSetFile );
    coverage.setGridDomain( gridDomain );
    coverage.setName( binFileName );
    coverage.setDescription( "ASCII-Import" ); //$NON-NLS-1$

    return GeoGridUtilities.toGrid( coverage );
  }

  private static RectifiedGridDomain importAsBinaryRaster( final File srcFile, final File dstFile, final String sourceCRS, final IProgressMonitor monitor ) throws IOException, CoreException
  {
    final ConvertAscii2Binary ascii2Binary = new ConvertAscii2Binary( srcFile.toURI().toURL(), dstFile, 2, sourceCRS );
    ascii2Binary.doConvert( monitor );
    final RectifiedGridDomain gridDomain = ascii2Binary.getGridDomain();
    return gridDomain;
  }
}
