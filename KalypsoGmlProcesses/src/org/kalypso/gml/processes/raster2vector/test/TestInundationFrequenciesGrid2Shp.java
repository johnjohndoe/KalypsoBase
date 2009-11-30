package org.kalypso.gml.processes.raster2vector.test;

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

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.namespace.QName;

import junit.framework.TestCase;
import ogc31.www.opengis.net.gml.FileType;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.kalypso.commons.java.util.zip.ZipUtilities;
import org.kalypso.commons.xml.NS;
import org.kalypso.commons.xml.XmlTypes;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.ogc31.KalypsoOGC31JAXBcontext;
import org.kalypso.gml.processes.raster2vector.Raster2Lines;
import org.kalypso.gml.processes.raster2vector.Raster2LinesWalkingStrategy;
import org.kalypso.gml.processes.raster2vector.collector.CollectorDataProvider;
import org.kalypso.gml.processes.raster2vector.collector.LineStringCollector;
import org.kalypso.gml.processes.raster2vector.collector.PolygonCollector;
import org.kalypso.gml.processes.raster2vector.collector.SegmentCollector;
import org.kalypso.gmlschema.GMLSchemaFactory;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypso.grid.AscGridExporter;
import org.kalypso.grid.ConvertAscii2Binary;
import org.kalypso.grid.FlattenToCategoryGrid;
import org.kalypso.grid.GeoGridException;
import org.kalypso.grid.GeoGridUtilities;
import org.kalypso.grid.GridCategoryWrapper;
import org.kalypso.grid.IGeoGrid;
import org.kalypso.ogc.gml.serialize.GmlSerializeException;
import org.kalypso.ogc.gml.serialize.ShapeSerializer;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Surface;
import org.kalypsodeegree_impl.gml.binding.commons.CoverageCollection;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverage;
import org.kalypsodeegree_impl.gml.binding.commons.ICoverageCollection;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridCoverage;
import org.kalypsodeegree_impl.gml.binding.commons.RectifiedGridDomain;
import org.kalypsodeegree_impl.io.shpapi.ShapeConst;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.geometry.JTSAdapter;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

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
public class TestInundationFrequenciesGrid2Shp extends TestCase
{

  private static final GeometryFactory GF = new GeometryFactory();

  public void testRiskModel( ) throws Exception
  {
    // unzip test data into workspace
    final IWorkspace workspace = ResourcesPlugin.getWorkspace();
    final IProject project = workspace.getRoot().getProject( "InundationFrequenciesTest" );
    project.create( new NullProgressMonitor() );

    final URL zipLocation = getClass().getResource( "resources/inundationDepthRaster.zip" );
// final URL zipLocation = getClass().getResource( "resources/flattentestgrids.zip" );
    ZipUtilities.unzip( zipLocation, project, new NullProgressMonitor() );

    // run test model
// final IFolder importDataFolder = project.getFolder( "flattentestgrids" );
    final IFolder importDataFolder = project.getFolder( "inundationDepthRaster" );

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

      for( int j = 0; j < collection.size(); j++ )
      {
        final ICoverage coverage = collection.get( j );
        final IGeoGrid grid = GeoGridUtilities.toGrid( coverage );

        gridCategories[i].addGeoGrid( grid );
      }
    }

    /* write the envelope as shape ====================================================================== */
    final boolean intersection = true;
    final Geometry globalEnv = GeoGridUtilities.getCommonGridEnvelopeForCollections( collections, intersection );
    final GM_Object gmObject = JTSAdapter.wrap( globalEnv );

    final String shapeBase1 = importDataFolder.getLocation() + "grid_envelope";
    writeEnvelopeShape( gmObject.getEnvelope(), shapeBase1 );

    collections = null;

    /* create the grid ====================================================================== */
    final FlattenToCategoryGrid grid = GeoGridUtilities.getFlattedGrid( gridCategories, intersection );

    /* export it ====================================================================== */
    final AscGridExporter gridExporter = new AscGridExporter( -9999, 3 );
    gridExporter.export( grid, new File( importDataFolder.getLocation() + "_flatten.asc" ), new NullProgressMonitor() );

    /* vectorize it ====================================================================== */
    vectorize( importDataFolder, grid );

  }

  private void vectorize( final IFolder importDataFolder, final IGeoGrid raster ) throws GeoGridException, Exception, GmlSerializeException
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
      final String shapeBase = importDataFolder.getLocation() + "grid_polygons";
      writePolygonShape( data, shapeBase );
    }
    else
    {
      final String shapeBase = importDataFolder.getLocation() + "grid_lines";
      writeLineShape( data, shapeBase );
    }
  }

  private void writeLineShape( final CollectorDataProvider[] data, final String shapeBase ) throws Exception, GmlSerializeException
  {
    /* Create feature type which describes what data the shape file contains */
    final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();

    final IMarshallingTypeHandler doubleTypeHandler = typeRegistry.getTypeHandlerForTypeName( XmlTypes.XS_DOUBLE );
    final IMarshallingTypeHandler stringTypeHandler = typeRegistry.getTypeHandlerForTypeName( XmlTypes.XS_STRING );
    final IMarshallingTypeHandler lineTypeHandler = typeRegistry.getTypeHandlerForTypeName( GeometryUtilities.QN_LINE_STRING );

    final QName shapeTypeQName = new QName( "anyNS", "shapeType" );

    final IValuePropertyType doubleType = GMLSchemaFactory.createValuePropertyType( new QName( "anyNS", "id" ), doubleTypeHandler, 1, 1, false );
    final IValuePropertyType stringType = GMLSchemaFactory.createValuePropertyType( new QName( "anyNS", "name" ), stringTypeHandler, 1, 1, false );
    final IValuePropertyType lineType = GMLSchemaFactory.createValuePropertyType( new QName( "anyNS", "aGeometry" ), lineTypeHandler, 1, 1, false );

    final IPropertyType[] properties = new IPropertyType[] { lineType, doubleType, stringType };
    final IFeatureType shapeFT = GMLSchemaFactory.createFeatureType( shapeTypeQName, properties );

    /* Create the shape root feature, we need it to create the children. */
    final Feature shapeRootFeature = ShapeSerializer.createWorkspaceRootFeature( shapeFT, ShapeConst.SHAPE_TYPE_POLYLINEZ );
    final GMLWorkspace shapeWorkspace = shapeRootFeature.getWorkspace();
    final IRelationType shapeParentRelation = (IRelationType) shapeRootFeature.getFeatureType().getProperty( ShapeSerializer.PROPERTY_FEATURE_MEMBER );

    for( int i = 0; i < data.length; i++ )
    {
      final GM_Curve line = (GM_Curve) data[i].getGeometry();
      final Double id = data[i].getId();
      final String[] name = data[i].getName();

      final Object[] shapeData = new Object[] { line, id, name[0] };
      final Feature feature = FeatureFactory.createFeature( shapeRootFeature, shapeParentRelation, "FeatureID" + i, shapeFT, shapeData );
      shapeWorkspace.addFeatureAsComposition( shapeRootFeature, shapeParentRelation, -1, feature );
    }

    ShapeSerializer.serialize( shapeWorkspace, shapeBase, null );
  }

  private void writeEnvelopeShape( final GM_Envelope env, final String shapeBase ) throws Exception, GmlSerializeException
  {
    /* Create feature type which describes what data the shape file contains */
    final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();

    final IMarshallingTypeHandler doubleTypeHandler = typeRegistry.getTypeHandlerForTypeName( new QName( NS.XSD_SCHEMA, "double" ) );
    final IMarshallingTypeHandler polygonTypeHandler = typeRegistry.getTypeHandlerForTypeName( GeometryUtilities.QN_POLYGON );

    final QName shapeTypeQName = new QName( "anyNS", "shapeType" );

    final IValuePropertyType doubleTypeId = GMLSchemaFactory.createValuePropertyType( new QName( "anyNS", "id" ), doubleTypeHandler, 1, 1, false );
    final IValuePropertyType polygonType = GMLSchemaFactory.createValuePropertyType( new QName( "anyNS", "geometry" ), polygonTypeHandler, 1, 1, false );

    final IPropertyType[] properties = new IPropertyType[] { polygonType, doubleTypeId };
    final IFeatureType shapeFT = GMLSchemaFactory.createFeatureType( shapeTypeQName, properties );

    /* Create the shape root feature, we need it to create the children. */
    final Feature shapeRootFeature = ShapeSerializer.createWorkspaceRootFeature( shapeFT, ShapeConst.SHAPE_TYPE_POLYGONZ );
    final GMLWorkspace shapeWorkspace = shapeRootFeature.getWorkspace();
    final IRelationType shapeParentRelation = (IRelationType) shapeRootFeature.getFeatureType().getProperty( ShapeSerializer.PROPERTY_FEATURE_MEMBER );

    final String crs = KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
    final GM_Surface< ? > surface = org.kalypsodeegree_impl.model.geometry.GeometryFactory.createGM_Surface( env, crs );
    final Double id = 1.0;

    final Object[] shapeData = new Object[] { surface, id };
    final Feature feature = FeatureFactory.createFeature( shapeRootFeature, shapeParentRelation, "FeatureID", shapeFT, shapeData );
    shapeWorkspace.addFeatureAsComposition( shapeRootFeature, shapeParentRelation, -1, feature );

    ShapeSerializer.serialize( shapeWorkspace, shapeBase, null );
  }

  private void writePolygonShape( final CollectorDataProvider[] data, final String shapeBase ) throws Exception, GmlSerializeException
  {
    /* Create feature type which describes what data the shape file contains */
    final ITypeRegistry<IMarshallingTypeHandler> typeRegistry = MarshallingTypeRegistrySingleton.getTypeRegistry();

    final IMarshallingTypeHandler doubleTypeHandler = typeRegistry.getTypeHandlerForTypeName( XmlTypes.XS_DOUBLE );
    final IMarshallingTypeHandler stringTypeHandler = typeRegistry.getTypeHandlerForTypeName( XmlTypes.XS_STRING );
    final IMarshallingTypeHandler polygonTypeHandler = typeRegistry.getTypeHandlerForTypeName( GeometryUtilities.QN_POLYGON );

    final QName shapeTypeQName = new QName( "anyNS", "shapeType" );

    final IValuePropertyType doubleTypeId = GMLSchemaFactory.createValuePropertyType( new QName( "anyNS", "id" ), doubleTypeHandler, 1, 1, false );
    final IValuePropertyType doubleTypeFrom = GMLSchemaFactory.createValuePropertyType( new QName( "anyNS", "from" ), doubleTypeHandler, 1, 1, false );
    final IValuePropertyType doubleTypeTo = GMLSchemaFactory.createValuePropertyType( new QName( "anyNS", "to" ), doubleTypeHandler, 1, 1, false );
    final IValuePropertyType stringTypeRange = GMLSchemaFactory.createValuePropertyType( new QName( "anyNS", "range" ), stringTypeHandler, 1, 1, false );
    final IValuePropertyType polygonType = GMLSchemaFactory.createValuePropertyType( new QName( "anyNS", "geometry" ), polygonTypeHandler, 1, 1, false );

    final IPropertyType[] properties = new IPropertyType[] { polygonType, doubleTypeId, doubleTypeFrom, doubleTypeTo, stringTypeRange };
    final IFeatureType shapeFT = GMLSchemaFactory.createFeatureType( shapeTypeQName, properties );

    /* Create the shape root feature, we need it to create the children. */
    final Feature shapeRootFeature = ShapeSerializer.createWorkspaceRootFeature( shapeFT, ShapeConst.SHAPE_TYPE_POLYGONZ );
    final GMLWorkspace shapeWorkspace = shapeRootFeature.getWorkspace();
    final IRelationType shapeParentRelation = (IRelationType) shapeRootFeature.getFeatureType().getProperty( ShapeSerializer.PROPERTY_FEATURE_MEMBER );

    for( int i = 0; i < data.length; i++ )
    {
      final GM_Surface< ? > line = (GM_Surface< ? >) data[i].getGeometry();
      final Double id = data[i].getId();
      final Double[] borders = data[i].getBorders();
      final Double from = borders[0];
      final Double to = borders[1];
      final String[] name = data[i].getName();

      final Object[] shapeData = new Object[] { line, id, from, to, name[0] };
      final Feature feature = FeatureFactory.createFeature( shapeRootFeature, shapeParentRelation, "FeatureID" + i, shapeFT, shapeData );
      shapeWorkspace.addFeatureAsComposition( shapeRootFeature, shapeParentRelation, -1, feature );
    }

    ShapeSerializer.serialize( shapeWorkspace, shapeBase, null );
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
    files[0] = importDataFolder.getFile( "wt_hq100.asc" );
    files[1] = importDataFolder.getFile( "wt_hq50.asc" );
    files[2] = importDataFolder.getFile( "wt_hq18.asc" );
    files[3] = importDataFolder.getFile( "wt_hq5_18m15.asc" );
    files[4] = importDataFolder.getFile( "wt_hq2_18m25.asc" );
    files[5] = importDataFolder.getFile( "wt_hq1_18m30.asc" );

    for( int i = 0; i < collections.length; i++ )
    {
      final GMLWorkspace covCollWorkspace = FeatureFactory.createGMLWorkspace( new QName( "org.kalypso.gml.common.coverage", "CoverageCollection" ), gmlContext, null );
      final Feature rootFeature = covCollWorkspace.getRootFeature();
      final ICoverageCollection covColl = (ICoverageCollection) rootFeature.getAdapter( ICoverageCollection.class );

      final IFile file = files[i];
      final String binFileName = file.getName() + ".bin";
      final String dstFileName = binFileName;
      final IFile dstRasterIFile = importDataFolder.getFile( dstFileName );
      final File dstRasterFile = dstRasterIFile.getRawLocation().toFile();
      final RectifiedGridDomain gridDomain = importAsBinaryRaster( file.getLocation().toFile(), dstRasterFile, "EPSG:31467", new NullProgressMonitor() );

      final IFeatureType ft = covCollWorkspace.getGMLSchema().getFeatureType( RectifiedGridCoverage.QNAME );
      final IRelationType parentRelation = (IRelationType) covColl.getFeature().getFeatureType().getProperty( CoverageCollection.QNAME_PROP_COVERAGE_MEMBER );
      final Feature coverageFeature = covCollWorkspace.createFeature( covColl.getFeature(), parentRelation, ft );
      final RectifiedGridCoverage coverage = new RectifiedGridCoverage( coverageFeature );

      final FileType rangeSetFile = KalypsoOGC31JAXBcontext.GML3_FAC.createFileType();
      rangeSetFile.setFileName( binFileName );
      rangeSetFile.setMimeType( "image/bin" ); //$NON-NLS-1$

      covColl.add( coverage );
      coverage.setRangeSet( rangeSetFile );
      coverage.setGridDomain( gridDomain );
      coverage.setName( binFileName );
      coverage.setDescription( "ASCII-Import" );

      collections[i] = covColl;
    }
    return collections;
  }

  private static RectifiedGridDomain importAsBinaryRaster( final File srcFile, final File dstFile, final String sourceCRS, final IProgressMonitor monitor ) throws IOException, CoreException
  {
    final ConvertAscii2Binary ascii2Binary = new ConvertAscii2Binary( srcFile.toURL(), dstFile, 2, sourceCRS );
    ascii2Binary.doConvert( monitor );
    final RectifiedGridDomain gridDomain = ascii2Binary.getGridDomain();
    return gridDomain;
  }

}
