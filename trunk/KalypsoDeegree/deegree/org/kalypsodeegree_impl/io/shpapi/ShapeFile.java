/** This file is part of kalypso/deegree.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * history:
 *
 * Files in this package are originally taken from deegree and modified here
 * to fit in kalypso. As goals of kalypso differ from that one in deegree
<<<<<<< .working
 * interface-compatibility to deegree is wanted but not retained always.
 * 
 * If you intend to use this software in other ways than in kalypso
=======
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
>>>>>>> .merge-right.r3720
 * (e.g. OGC-web services), you should consider the latest version of deegree,
 * see http://www.deegree.org .
 *
 * all modifications are licensed as deegree,
 * original copyright:
 *
 * Copyright (C) 2001 by:
 * EXSE, Department of Geography, University of Bonn
 * http://www.giub.uni-bonn.de/exse/
 * lat/lon GmbH
 * http://www.lat-lon.de
 */
package org.kalypsodeegree_impl.io.shpapi;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.namespace.QName;

import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.ByteUtils;
import org.kalypsodeegree.model.geometry.GM_Curve;
import org.kalypsodeegree.model.geometry.GM_Envelope;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Point;
import org.kalypsodeegree.model.geometry.GM_SurfacePatch;
import org.kalypsodeegree_impl.io.rtree.RTree;
import org.kalypsodeegree_impl.io.rtree.RTreeException;
import org.kalypsodeegree_impl.io.shpapi.dataprovider.IShapeDataProvider;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * Class representing an ESRI Shape File.
 * <p>
 * This is a modification of the <tt>ShapeFile</tt> class within the shpapi package of sfcorba2java project performed by
 * the EXSE-Working group of of the geogr. institute of the university of Bonn
 * (http://www.giub.uni-bonn.de/exse/results/welcome.html).
 * <p>
 * ------------------------------------------------------------------------
 * </p>
 * 
 * @version 17.10.2001
 * @author Andreas Poth
 */
public class ShapeFile
{
  public static final QName PROPERTY_FEATURE_MEMBER = new QName( DBaseFile.SHP_NAMESPACE_URI, "featureMember" ); //$NON-NLS-1$

  public static final String GEOM = "GEOM";//$NON-NLS-1$

  private final String m_filePath;

  private DBaseFile m_dbf = null;

  /**
   * aggregated Instance-variables
   */
  private MainFile m_shp = null;

  private RTree m_rti = null;

  /*
   * indicates if a dBase-file is associated to the shape-file
   */
  private boolean m_hasDBaseFile = true;

  /*
   * indicates if an R-tree index is associated to the shape-file
   */
  private boolean m_hasRTreeIndex = true;

  /**
   * constructor: <BR>
   * Construct a ShapeFile from a file name. <BR>
   */
  public ShapeFile( final String filePath ) throws IOException
  {
    m_filePath = filePath;

    /*
     * initialize the MainFile
     */
    m_shp = new MainFile( filePath );

    /*
     * initialize the DBaseFile
     */
    try
    {
      m_dbf = new DBaseFile( filePath, m_shp.getFileShapeType() );
    }
    catch( final IOException e )
    {
      m_hasDBaseFile = false;

      e.printStackTrace();
    }

    /*
     * initialize the RTreeIndex
     */
    try
    {
      m_rti = new RTree( filePath + ".rti" );
    }
    catch( final RTreeException e )
    {
      m_hasRTreeIndex = false;
    }
  }

  /**
   * Creates a new shape file. If a file with the same name already exists, it will be overwritten.
   */
  public ShapeFile( final String filePath, final String rwflag ) throws IOException
  {
    m_filePath = filePath;

    m_shp = new MainFile( filePath, rwflag );

    // TODO: initialize dbf, rti (at the moment they are created during the call to writeShape)
    m_hasDBaseFile = false;
    m_hasRTreeIndex = false;
  }

  public void close( ) throws IOException
  {
    m_shp.close();

    if( m_dbf != null )
      m_dbf.close();

    if( m_rti != null )
    {
      try
      {
        m_rti.close();
      }
      catch( final Exception e )
      {
        e.printStackTrace();
      }
    }
  }

  /**
   * returns true if a dBase-file is associated to the shape-file <BR>
   */
  public boolean hasDBaseFile( )
  {
    return m_hasDBaseFile;
  }

  /**
   * returns true if an R-tree index is associated to the shape-file <BR>
   */
  public boolean hasRTreeIndex( )
  {
    return m_hasRTreeIndex;
  }

  /**
   * returns the number of records within a shape-file <BR>
   */
  public int getRecordNum( )
  {
    return m_shp.getRecordNum();
  }

  /**
   * returns the minimum bounding rectangle of all geometries <BR>
   * within the shape-file
   */
  public GM_Envelope getFileMBR( )
  {
    final double xmin = m_shp.getFileMBR().west;
    final double xmax = m_shp.getFileMBR().east;
    final double ymin = m_shp.getFileMBR().south;
    final double ymax = m_shp.getFileMBR().north;

    return GeometryFactory.createGM_Envelope( xmin, ymin, xmax, ymax, null );
  }

  /**
   * returns the minimum bound rectangle of RecNo'th Geometrie <BR>
   */
  public GM_Envelope getMBRByRecNo( final int recNo ) throws IOException
  {
    final SHPEnvelope shpenv = m_shp.getRecordMBR( recNo );
    final double xmin = shpenv.west;
    final double xmax = shpenv.east;
    final double ymin = shpenv.south;
    final double ymax = shpenv.north;

    return GeometryFactory.createGM_Envelope( xmin, ymin, xmax, ymax, null );
  }

  /**
   * returns the RecNo'th entry of the shape file as Feature. This contains the geometry as well as the attributes
   * stored into the dbase file.
   * 
   * @param allowNull
   *          if true, everything wich cannot parsed gets 'null' instaed of ""
   */
  public Feature getFeatureByRecNo( final Feature parent, final IRelationType parentRelation, final int RecNo, final String crs ) throws IOException, HasNoDBaseFileException, DBaseException
  {
    if( !m_hasDBaseFile )
      throw new HasNoDBaseFileException( "Exception: there is no dBase-file " + "associated to this shape-file" );

    final Feature feature = m_dbf.getFRow( parent, parentRelation, RecNo );
    final GM_Object geo = getGM_ObjectByRecNo( RecNo, crs );
    final IFeatureType featureType = feature.getFeatureType();
    final IPropertyType pt = featureType.getProperty( new QName( featureType.getQName().getNamespaceURI(), GEOM ) );
    feature.setProperty( pt, geo );

    return feature;
  }

  /**
   * returns RecNo'th Geometrie <BR>
   */
  public GM_Object getGM_ObjectByRecNo( final int RecNo, final String crs ) throws IOException
  {
    final ISHPGeometry shpGeom = m_shp.getByRecNo( RecNo );
    if( shpGeom == null )
      return null;

    return SHP2WKS.transform( crs, shpGeom );
    
  }

  /**
   * returns the properties (column headers) of the dBase-file <BR>
   * associated to the shape-file <BR>
   */
  public String[] getProperties( ) throws HasNoDBaseFileException, DBaseException
  {
    if( !m_hasDBaseFile )
      throw new HasNoDBaseFileException( "Exception: there is no dBase-file " + "associated to this shape-file" );

    return m_dbf.getProperties();
  }

  /**
   * returns the datatype of each column of the database file <BR>
   * associated to the shape-file <BR>
   */
  public String[] getDataTypes( ) throws HasNoDBaseFileException, DBaseException
  {
    if( !m_hasDBaseFile )
      throw new HasNoDBaseFileException( "Exception: there is no dBase-file " + "associated to this shape-file" );

    return m_dbf.getDataTypes();
  }

  /**
   * @throws HasNoDBaseFileException
   * @throws DBaseException
   */
  public int[] getDataLengths( ) throws HasNoDBaseFileException, DBaseException
  {
    final String[] properties = getProperties();
    final int[] retval = new int[properties.length];

    for( int i = 0; i < properties.length; i++ )
    {
      retval[i] = m_dbf.getDataLength( properties[i] );
    }

    return retval;
  }

  /**
   * returns the datatype of each column of the dBase associated <BR>
   * to the shape-file specified by fields <BR>
   */
  public String[] getDataTypes( final String[] fields ) throws HasNoDBaseFileException, DBaseException
  {
    if( !m_hasDBaseFile )
      throw new HasNoDBaseFileException( "Exception: there is no dBase-file " + "associated to this shape-file" );

    return m_dbf.getDataTypes( fields );
  }

  /**
   * returns a row of the dBase-file <BR>
   * associated to the shape-file <BR>
   */
  public Object[] getRow( final int rowNo ) throws HasNoDBaseFileException, DBaseException
  {
    if( !m_hasDBaseFile )
      throw new HasNoDBaseFileException( "Exception: there is no dBase-file " + "associated to this shape-file" );

    return m_dbf.getRow( rowNo );
  }

  private void createDBaseFile( final String filePath, final IFeatureType featT ) throws DBaseException
  {
    // count regular fields
    final IPropertyType[] ftp = featT.getProperties();

    // get properties names and types and create a FieldDescriptor
    // for each properties except the geometry-property
    final List<FieldDescriptor> fieldList = new ArrayList<FieldDescriptor>();
    for( int i = 0; i < ftp.length; i++ )
    {
      final String ftpName = ftp[i].getQName().getLocalPart();
      int pos = ftpName.lastIndexOf( '.' );
      if( pos < 0 )
      {
        pos = -1;
      }
      final String s = ftpName.substring( pos + 1 );
      if( !(ftp[i] instanceof IValuePropertyType) )
      {// TODO: this seems to be a bug;
      }
      final IValuePropertyType vpt = (IValuePropertyType) ftp[i];
      final Class< ? > clazz = vpt.getValueClass();
      if( clazz == Integer.class )
      {
        fieldList.add( new FieldDescriptor( s, "N", (byte) 20, (byte) 0 ) );
      }
      else if( clazz == Byte.class )
      {
        fieldList.add( new FieldDescriptor( s, "N", (byte) 4, (byte) 0 ) );
      }
      else if( clazz == Character.class )
      {
        fieldList.add( new FieldDescriptor( s, "C", (byte) 1, (byte) 0 ) );
      }
      else if( clazz == Float.class )
      {
        // TODO: Problem: reading/writing a shape will change the precision/size of the column!
        fieldList.add( new FieldDescriptor( s, "N", (byte) 30, (byte) 10 ) );
      }
      else if( (clazz == Double.class) || (clazz == Number.class) )
      {
        fieldList.add( new FieldDescriptor( s, "N", (byte) 30, (byte) 10 ) );
      }
      else if( clazz == BigDecimal.class )
      {
        fieldList.add( new FieldDescriptor( s, "N", (byte) 30, (byte) 10 ) );
      }
      else if( clazz == String.class )
      {
        fieldList.add( new FieldDescriptor( s, "C", (byte) 127, (byte) 0 ) );
      }
      else if( clazz == Date.class )
      {
        fieldList.add( new FieldDescriptor( s, "D", (byte) 12, (byte) 0 ) );
      }
      else if( clazz == Long.class || clazz == BigInteger.class )
      {
        fieldList.add( new FieldDescriptor( s, "N", (byte) 30, (byte) 0 ) );
      }
      else if( clazz == Boolean.class )
      {
        fieldList.add( new FieldDescriptor( s, "L", (byte) 1, (byte) 0 ) );
      }
      else
      {
        // System.out.println("no db-type:" + ftp[i].getType());
      }
    }

    // allocate memory for fielddescriptors
    final FieldDescriptor[] fieldDesc = fieldList.toArray( new FieldDescriptor[fieldList.size()] );
    m_dbf = new DBaseFile( filePath, fieldDesc );
  }

  // TODO: rework this. Especially, we should get all information from the shape data provider. The shape data provider
  // interface
  // should be refactored not to have references to features and such, but just to give the needed information from the
  // point of view of the shape file.
  public void writeShape( final IShapeDataProvider dataProvider ) throws Exception
  {
    final int featuresLength = dataProvider.getFeaturesLength();

    if( featuresLength == 0 )
      throw new Exception( "Can't write an empty shape." );

    // mbr of the whole shape file
    SHPEnvelope shpmbr = new SHPEnvelope();

    // Set the Offset to the end of the fileHeader
    int offset = ShapeConst.SHAPE_FILE_HEADER_LENGTH;

    // ====================DBASE TABLE =====================

    /* initialize the dbasefile associated with the shape file */
    final IFeatureType featureType = dataProvider.getFeatureType();
    createDBaseFile( m_filePath, featureType );

    /* loop through the Geometries of the feature collection and write them to a bytearray */
    final IPropertyType[] ftp = featureType.getProperties();

    /* loop over all features */
    for( int i = 0; i < featuresLength; i++ )
    {
      // write i'th feature properties to a ArrayList
      final ArrayList<Object> vec = new ArrayList<Object>();
      for( int j = 0; j < ftp.length; j++ )
      {
        /* get the property of the current feature */
        // final Object value = feature.getProperty( ftp[j] );
        final Object value = dataProvider.getFeatureProperty( i, ftp[j] );
        if( !(ftp[j] instanceof IValuePropertyType) )
        {
          continue;
        }
        final IValuePropertyType ivp = (IValuePropertyType) ftp[j];

        final Class< ? > clazz = ivp.getValueClass();
        if( (clazz == Integer.class) || (clazz == Byte.class) || (clazz == Character.class) || (clazz == Float.class) || (clazz == Double.class) || (clazz == Number.class) || (clazz == Date.class)
            || (clazz == Long.class) || (clazz == String.class) || (clazz == Boolean.class) )
        {
          vec.add( value );
        }
        else if( clazz == BigDecimal.class )
        {
          if( value != null )
          {
            vec.add( new Double( ((java.math.BigDecimal) value).doubleValue() ) );
          }
          else
          {
            vec.add( null );
          }
        }
        else if( clazz == BigInteger.class )
        {
          if( value != null )
          {
            vec.add( new Long( ((BigInteger) value).longValue() ) );
          }
          else
          {
            vec.add( null );
          }
        }
      }

      // write the ArrayList (properties) to the dbase file
      try
      {
        m_dbf.setRecord( vec );
      }
      catch( final DBaseException db )
      {
        db.printStackTrace();
        throw new Exception( db.toString(), db );
      }

      // ==================== SHAPE ENTRIES =====================

      /* create a new SHP type entry in the specified shape type */
      /* convert feature geometry into output geometry */
      final ISHPGeometry shpGeom = getShapeGeometry( dataProvider.getGeometry( i ), dataProvider.getOutputShapeConstant() );
      if( shpGeom == null )
        throw new IllegalStateException();

      final byte[] byteArray = shpGeom.writeShape();
      final int nbyte = shpGeom.size();
      final SHPEnvelope mbr = shpGeom.getEnvelope();

      // write bytearray to the shape file
      final IndexRecord record = new IndexRecord( offset / 2, nbyte / 2 );

      // write recordheader to the bytearray
      ByteUtils.writeBEInt( byteArray, 0, i );
      ByteUtils.writeBEInt( byteArray, 4, nbyte / 2 );

      // write record (bytearray) including recordheader to the shape file
      m_shp.write( byteArray, record, mbr );

      // icrement offset for pointing at the end of the file
      offset += (nbyte + ShapeConst.SHAPE_FILE_RECORD_HEADER_LENGTH);

      // actualize shape file minimum boundary rectangle
      if( i == 0 || (i > 0 && shpmbr == null) )
        shpmbr = mbr;
      if( mbr != null )
      {
        if( mbr.west < shpmbr.west )
          shpmbr.west = mbr.west;

        if( mbr.east > shpmbr.east )
          shpmbr.east = mbr.east;

        if( mbr.south < shpmbr.south )
          shpmbr.south = mbr.south;

        if( mbr.north > shpmbr.north )
          shpmbr.north = mbr.north;
      }
    }

    m_dbf.writeAllToFile();

    // Header schreiben
    m_shp.writeHeader( offset, dataProvider.getOutputShapeConstant(), shpmbr );
  }

  private ISHPGeometry getShapeGeometry( final GM_Object geom, final byte outputShapeConstant )
  {
    if( geom == null )
      return new SHPNullShape();

    switch( outputShapeConstant )
    {
      case ShapeConst.SHAPE_TYPE_NULL:
        return new SHPNullShape();
      case ShapeConst.SHAPE_TYPE_POINT:
      {
        final GM_Point point = (GM_Point) geom.getAdapter( GM_Point.class );
        if( point == null )
          return null;
        else
          return new SHPPoint( point );
      }
      case ShapeConst.SHAPE_TYPE_POLYLINE:
      {
        final GM_Curve[] curves = (GM_Curve[]) geom.getAdapter( GM_Curve[].class );
        if( curves == null )
          return null;
        else
          return new SHPPolyLine( curves );
      }
      case ShapeConst.SHAPE_TYPE_POLYGON:
      {
        final GM_SurfacePatch[] surfacePatches = (GM_SurfacePatch[]) geom.getAdapter( GM_SurfacePatch[].class );
        if( surfacePatches == null )
          return null;
        else
          return new SHPPolygon( surfacePatches );
      }
      case ShapeConst.SHAPE_TYPE_POINTZ:
      {
        final GM_Point point = (GM_Point) geom.getAdapter( GM_Point.class );
        if( point == null )
          return null;
        else
          return new SHPPointz( point );
      }
      case ShapeConst.SHAPE_TYPE_POLYLINEZ:
      {
        final GM_Curve[] curves = (GM_Curve[]) geom.getAdapter( GM_Curve[].class );
        if( curves == null )
          return null;
        else
          return new SHPPolyLinez( curves );
      }
      case ShapeConst.SHAPE_TYPE_POLYGONZ:
      {
        final GM_SurfacePatch[] surfacePatches = (GM_SurfacePatch[]) geom.getAdapter( GM_SurfacePatch[].class );
        if( surfacePatches == null )
          return null;
        else
          return new SHPPolygonz( surfacePatches );
      }
    }

    return null;
  }

  public IFeatureType getFeatureType( )
  {
    return m_dbf.getFeatureType();
  }

  public int getFileShapeType( )
  {
    return m_shp.getFileShapeType();
  }

}