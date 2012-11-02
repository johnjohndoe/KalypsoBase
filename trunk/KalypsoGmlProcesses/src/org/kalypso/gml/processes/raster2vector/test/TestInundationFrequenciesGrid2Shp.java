/*----------------    FILE HEADER KALYPSO ------------------------------------------

import java.io.File;

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

import javax.xml.namespace.QName;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.kalypso.grid.AscGridExporter;
import org.kalypso.grid.ConvertAscii2Binary;
import org.kalypso.grid.FlattenToCategoryGrid;
import org.kalypso.grid.GeoGridException;
import org.kalypso.grid.GeoGridUtilities;
import org.kalypso.grid.GridCategoryWrapper;
import org.kalypso.grid.IGeoGrid;
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
import org.kalypsodeegree.model.feature.IFeatureBindingCollection;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Polygon;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridCoverage;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
/**
 * JUnit Test Case for converting a grid into line and polygon shape.<br>
 * This test extracts demo input data (grid) from resources and converts them into shape files. <br>
 * <br>
 * Run this test as plug-in test.
 *
 * @author Thomas Jung
 */
public class TestInundationFrequenciesGrid2Shp
{
  private static final GeometryFactory GF = new GeometryFactory();

  @Test
  // Takes too much time for normal testing
  @Ignore
  public void testRiskModel( ) throws Exception
  {
    // unzip test data into workspace
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    final IProject project = workspace.getRoot().getProject( "InundationFrequenciesTest" ); //$NON-NLS-1$
    project.create( new NullProgressMonitor() );

    final URL zipLocation = getClass().getResource( "/etc/test/resources/raster2vector/inundationDepthRaster.zip" ); //$NON-NLS-1$
// final URL zipLocation = getClass().getResource( "/etc/test/resources/raster2vector/flattentestgrids.zip" );
    ZipUtilities.unzip( zipLocation, project, new NullProgressMonitor() );

    // run test model
// final IFolder importDataFolder = project.getFolder( "flattentestgrids" );
    final IFolder importDataFolder = project.getFolder( "inundationDepthRaster" ); //$NON-NLS-1$

    //
    ICoverageCollection[] collections = getTestGrid( importDataFolder );

    /* create some categories with some values ====================================================================== */
    final GridCategoryWrapper gridCategory1 = new GridCategoryWrapper( 1 );
    final GridCategoryWrapper gridCategory2 = new GridCategoryWrapper( 2 );
    final GridCategoryWrapper gridCategory3 = new GridCategoryWrapper( 3 );
    final GridCategoryWrapper gridCategory4 = new GridCategoryWrapper( 4 );
    final GridCategoryWrapper gridCategory5 = new GridCategoryWrapper( 5 );
    final GridCategoryWrapper gridCategory6 = new GridCategoryWrapper( 6 );

    // add grids to categories
    final GridCategoryWrapper[] gridCategories = new GridCategoryWrapper[6];
    gridCategories[0] = gridCategory1;
    gridCategories[1] = gridCategory2;
    gridCategories[2] = gridCategory3;
    gridCategories[3] = gridCategory4;
    gridCategories[4] = gridCategory5;
    gridCategories[5] = gridCategory6;

    for( int i = 0; i < collections.length; i++ )
    {
      final ICoverageCollection collection = collections[i];
      final IFeatureBindingCollection<ICoverage> coverages = collection.getCoverages();
      for( int j = 0; j < coverages.size(); j++ )
      {
        final ICoverage coverage = coverages.get( j );
        final IGeoGrid grid = GeoGridUtilities.toGrid( coverage );

        gridCategories[i].addGeoGrid( grid );
      }
    }

    /* write the envelope as shape ====================================================================== */
    final boolean intersection = true;
    final Geometry globalEnv = GeoGridUtilities.getCommonGridEnvelopeForCollections( collections, intersection );
    final GM_Object gmObject = JTSAdapter.wrap( globalEnv );

    final String shapeBase1 = importDataFolder.getLocation() + "grid_envelope"; //$NON-NLS-1$
    writeEnvelopeShape( gmObject.getEnvelope(), shapeBase1 );

    collections = null;

    /* create the grid ====================================================================== */
    final FlattenToCategoryGrid grid = GeoGridUtilities.getFlattedGrid( gridCategories, intersection );

    /* export it ====================================================================== */
    final AscGridExporter gridExporter = new AscGridExporter( -9999, 3 );
    gridExporter.export( grid, new File( importDataFolder.getLocation() + "_flatten.asc" ), new NullProgressMonitor() ); //$NON-NLS-1$

    /* vectorize it ====================================================================== */
    vectorize( importDataFolder, grid );

  }

  private void vectorize( final IFolder importDataFolder, final IGeoGrid raster ) throws GeoGridException, DBaseException, IOException, ShapeDataException, SHPException
  {
    final boolean bSimple = false;
    final boolean bIsolines = false;

    final double[] grenzen = new double[] { 0, 0.5, 1.5, 2.5, 3.5, 4.5, 5.5, 6.5, 20 };

    final SegmentCollector collector = bIsolines ? (SegmentCollector) new LineStringCollector( GF, grenzen, bSimple ) : new PolygonCollector( GF, grenzen, bSimple );
    final Raster2Lines r2l = new Raster2Lines( collector, grenzen );

    new Raster2LinesWalkingStrategy().walk( raster, r2l, null, new NullProgressMonitor() );

    final CollectorDataProvider[] data = collector.getData();

    // Shape schreiben
    if( collector instanceof PolygonCollector )
    {
      final String shapeBase = importDataFolder.getLocation() + "grid_polygons"; //$NON-NLS-1$
      writePolygonShape( data, shapeBase );
    }
    else
    {
      final String shapeBase = importDataFolder.getLocation() + "grid_lines"; //$NON-NLS-1$
      writeLineShape( data, shapeBase );
    }
  }

  private void writeLineShape( final CollectorDataProvider[] data, final String shapeBase ) throws DBaseException, IOException, ShapeDataException, SHPException
  {
    final IDBFField doubleField = new DBFField( "ID", FieldType.N, (short) 10, (short) 3 ); //$NON-NLS-1$
    final IDBFField stringField = new DBFField( "NAME", FieldType.C, (short) 100, (short) 0 ); //$NON-NLS-1$
    final IDBFField[] fields = new IDBFField[] { doubleField, stringField };

    final ShapeType shapeType = ShapeType.POLYLINEZ;
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

  private void writeEnvelopeShape( final GM_Envelope env, final String shapeBase ) throws DBaseException, IOException, GM_Exception, ShapeDataException, SHPException
  {
    final IDBFField doubleField = new DBFField( "ID", FieldType.N, (short) 10, (short) 3 ); //$NON-NLS-1$
    final IDBFField[] fields = new IDBFField[] { doubleField };

    final ShapeType shapeType = ShapeType.POLYGONZ;
    try (final ShapeFile shape = ShapeFile.create( shapeBase, shapeType, Charset.defaultCharset(), fields ))
    {
      final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
      final GM_Object2Shape gm2shp = new GM_Object2Shape( shapeType, crs );

      final GM_Polygon surface = org.kalypsodeegree_impl.model.geometry.GeometryFactory.createGM_Surface( env, crs );
      final ISHPGeometry geom = gm2shp.convert( surface );

      final Double id = 1.0;

      final Object[] shapeData = new Object[] { id };
      shape.addFeature( geom, shapeData );
    }
  }

  private void writePolygonShape( final CollectorDataProvider[] data, final String shapeBase ) throws DBaseException, IOException, ShapeDataException, SHPException
  {
    final IDBFField doubleField = new DBFField( "ID", FieldType.N, (short) 10, (short) 3 ); //$NON-NLS-1$
    final IDBFField fromField = new DBFField( "FROM", FieldType.N, (short) 10, (short) 3 ); //$NON-NLS-1$
    final IDBFField toField = new DBFField( "TO", FieldType.N, (short) 10, (short) 3 ); //$NON-NLS-1$
    final IDBFField rangeField = new DBFField( "RANGE", FieldType.C, (short) 10, (short) 0 ); //$NON-NLS-1$
    final IDBFField[] fields = new IDBFField[] { doubleField, fromField, toField, rangeField };

    final ShapeType shapeType = ShapeType.POLYLINEZ;
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

        final Object[] shapeData = new Object[] { id, from, to, name[0] };

        shape.addFeature( geom, shapeData );
      }
    }
  }

  private ICoverageCollection[] getTestGrid( final IFolder importDataFolder ) throws Exception, MalformedURLException, IOException
  {
    final ICoverageCollection[] collections = new ICoverageCollection[6];

    final URL gmlContext = ResourceUtilities.createURL( importDataFolder );

// final GMLWorkspace covCollWorkspace = GmlSerializer.createGMLWorkspace( ResourceUtilities.createURL( covCollFile ),
// null );

    final IFile[] files = new IFile[collections.length];
// files[0] = importDataFolder.getFile( "test1.asc" );
// files[1] = importDataFolder.getFile( "test2.asc" );
// files[2] = importDataFolder.getFile( "test3.asc" );
// files[3] = importDataFolder.getFile( "test4.asc" );
// files[4] = importDataFolder.getFile( "test5.asc" );
// files[5] = importDataFolder.getFile( "test6.asc" );
    files[0] = importDataFolder.getFile( "wt_hq100.asc" ); //$NON-NLS-1$
    files[1] = importDataFolder.getFile( "wt_hq50.asc" ); //$NON-NLS-1$
    files[2] = importDataFolder.getFile( "wt_hq18.asc" ); //$NON-NLS-1$
    files[3] = importDataFolder.getFile( "wt_hq5_18m15.asc" ); //$NON-NLS-1$
    files[4] = importDataFolder.getFile( "wt_hq2_18m25.asc" ); //$NON-NLS-1$
    files[5] = importDataFolder.getFile( "wt_hq1_18m30.asc" ); //$NON-NLS-1$

    for( int i = 0; i < collections.length; i++ )
    {
      final GMLWorkspace covCollWorkspace = FeatureFactory.createGMLWorkspace( new QName( "org.kalypso.gml.common.coverage", "CoverageCollection" ), gmlContext, null ); //$NON-NLS-1$ //$NON-NLS-2$
      final Feature rootFeature = covCollWorkspace.getRootFeature();
      final ICoverageCollection covColl = (ICoverageCollection) rootFeature.getAdapter( ICoverageCollection.class );

      final IFile file = files[i];
      final String binFileName = file.getName() + ".bin"; //$NON-NLS-1$
      final String dstFileName = binFileName;
      final IFile dstRasterIFile = importDataFolder.getFile( dstFileName );
      final File dstRasterFile = dstRasterIFile.getRawLocation().toFile();
      final RectifiedGridDomain gridDomain = importAsBinaryRaster( file.getLocation().toFile(), dstRasterFile, "EPSG:31467", new NullProgressMonitor() ); //$NON-NLS-1$

      final IFeatureType ft = GMLSchemaUtilities.getFeatureTypeQuiet( RectifiedGridCoverage.QNAME );
      final IRelationType parentRelation = (IRelationType) covColl.getFeatureType().getProperty( ICoverageCollection.QNAME_PROP_COVERAGE_MEMBER );
      final Feature coverageFeature = covCollWorkspace.createFeature( covColl, parentRelation, ft );
      final RectifiedGridCoverage coverage = (RectifiedGridCoverage) coverageFeature;

      final RangeSetFile rangeSetFile = new RangeSetFile( binFileName );
      rangeSetFile.setMimeType( "image/bin" ); //$NON-NLS-1$

      covColl.getCoverages().add( coverage );
      coverage.setRangeSet( rangeSetFile );
      coverage.setGridDomain( gridDomain );
      coverage.setName( binFileName );
      coverage.setDescription( "ASCII-Import" ); //$NON-NLS-1$

      collections[i] = covColl;
    }
    return collections;
  }

  private static RectifiedGridDomain importAsBinaryRaster( final File srcFile, final File dstFile, final String sourceCRS, final IProgressMonitor monitor ) throws IOException, CoreException
  {
    final ConvertAscii2Binary ascii2Binary = new ConvertAscii2Binary( srcFile.toURI().toURL(), dstFile, 2, sourceCRS );
    ascii2Binary.doConvert( monitor );
    final RectifiedGridDomain gridDomain = ascii2Binary.getGridDomain();
    return gridDomain;
  }

}
