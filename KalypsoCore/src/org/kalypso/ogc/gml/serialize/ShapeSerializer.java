/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.serialize;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.kalypso.commons.java.net.UrlUtilities;
import org.kalypso.commons.xml.XmlTypes;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.core.i18n.Messages;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaFactory;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.ITypeRegistry;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.io.shpapi.DBaseFile;
import org.kalypsodeegree_impl.io.shpapi.ShapeFile;
import org.kalypsodeegree_impl.io.shpapi.dataprovider.IShapeDataProvider;
import org.kalypsodeegree_impl.io.shpapi.dataprovider.StandardShapeDataProvider;
import org.kalypsodeegree_impl.model.feature.FeatureFactory;
import org.kalypsodeegree_impl.model.feature.GMLWorkspace_Impl;

/**
 * Helper-Klasse zum lesen und schreiben von GML <br>
 * TODO: Problem: reading/writing a shape will change the precision/size of the columns!
 * 
 * @author Gernot Belger
 */
public final class ShapeSerializer
{
  /** The default charset of a shape (really the .dbf) is IBM850. */
  private static final String SHAPE_DEFAULT_CHARSET_IBM850 = "IBM850";

  public static final String SHP_NAMESPACE_URI = DBaseFile.SHP_NAMESPACE_URI;

  private static final QName ROOT_FEATURETYPE = new QName( SHP_NAMESPACE_URI, "ShapeCollection" ); //$NON-NLS-1$

  public static final QName PROPERTY_FEATURE_MEMBER = new QName( DBaseFile.SHP_NAMESPACE_URI, "featureMember" ); //$NON-NLS-1$

  private static final QName PROPERTY_NAME = new QName( SHP_NAMESPACE_URI, "name" ); //$NON-NLS-1$

  private static final QName PROPERTY_TYPE = new QName( SHP_NAMESPACE_URI, "type" ); //$NON-NLS-1$

  public static final String PROPERTY_GEOM = "GEOM";//$NON-NLS-1$

  /**
   * Pseudo QNAME, placeholder for the gml-id to be written.
   */
  public static final QName QNAME_GMLID = new QName( ShapeSerializer.class.getName() + "gmlid" ); //$NON-NLS-1$

  private ShapeSerializer( )
  {
    // wird nicht instantiiert
  }

  /**
   * @deprecated Use {@link org.kalypso.shape.ShapeWriter} and {@link org.kalypso.shape.deegree.GenericShapeDataFactory}
   *             instead.
   */
  @Deprecated
  public static void serialize( final GMLWorkspace workspace, final String filenameBase, IShapeDataProvider shapeDataProvider ) throws GmlSerializeException
  {
    final Feature rootFeature = workspace.getRootFeature();
    final List<Feature> features = (List<Feature>) rootFeature.getProperty( PROPERTY_FEATURE_MEMBER );

    try
    {
      final ShapeFile shapeFile = new ShapeFile( filenameBase, "rw" ); //$NON-NLS-1$

      // if no dataProvider is set take the StandardProvider
      if( shapeDataProvider == null )
      {
        shapeDataProvider = new StandardShapeDataProvider( features.toArray( new Feature[features.size()] ) );
      }

      shapeFile.writeShape( shapeDataProvider );
      shapeFile.close();
    }
    catch( final Exception e )
    {
      e.printStackTrace();

      throw new GmlSerializeException( Messages.getString( "org.kalypso.ogc.gml.serialize.ShapeSerializer.7" ), e ); //$NON-NLS-1$
    }
  }

  public final static Feature createWorkspaceRootFeature( final IFeatureType featureType, final int shapeFileType )
  {
    final IGMLSchema schema = featureType.getGMLSchema();
    final IFeatureType[] featureTypes = schema.getAllFeatureTypes();
    final Feature rootFeature = ShapeSerializer.createShapeRootFeature( featureType );
    final String schemaLocation = schema instanceof GMLSchema ? ((GMLSchema) schema).getContext().toExternalForm() : null;
    new GMLWorkspace_Impl( schema, featureTypes, rootFeature, null, null, schemaLocation, null );
    rootFeature.setProperty( ShapeSerializer.PROPERTY_TYPE, new Integer( shapeFileType ) );
    return rootFeature;
  }

  /**
   * Creates to feature type for the root feature of a shape-file-based workspace.
   * 
   * @param childFeatureType
   *          The feature type for the children (i.e. the shape-objects) of the root.
   * @return A newly created feature suitable for the root of a workspace. It has the following properties:
   *         <ul>
   *         <li>name : String [1] - some meaningful name</li>
   *         <li>type : int [1] - the shape-geometry-type</li>
   *         <li>featureMember : inline-feature with type childFeatureType [0,n]</li>
   *         </ul>
   */
  public static Feature createShapeRootFeature( final IFeatureType childFeatureType )
  {
    final ITypeRegistry<IMarshallingTypeHandler> registry = MarshallingTypeRegistrySingleton.getTypeRegistry();
    final IMarshallingTypeHandler stringTH = registry.getTypeHandlerForTypeName( XmlTypes.XS_STRING );
    final IMarshallingTypeHandler intTH = registry.getTypeHandlerForTypeName( XmlTypes.XS_INT );

    final IPropertyType nameProp = GMLSchemaFactory.createValuePropertyType( ShapeSerializer.PROPERTY_NAME, stringTH, 1, 1, false );
    final IPropertyType typeProp = GMLSchemaFactory.createValuePropertyType( ShapeSerializer.PROPERTY_TYPE, intTH, 1, 1, false );
    final IRelationType memberProp = GMLSchemaFactory.createRelationType( PROPERTY_FEATURE_MEMBER, childFeatureType, 0, IPropertyType.UNBOUND_OCCURENCY, false );
    final IPropertyType[] ftps = new IPropertyType[] { nameProp, typeProp, memberProp };
    final QName fcQName = new QName( "http://www.opengis.net/gml", "_FeatureCollection" ); //$NON-NLS-1$ //$NON-NLS-2$
    final IFeatureType collectionFT = GMLSchemaFactory.createFeatureType( ShapeSerializer.ROOT_FEATURETYPE, ftps, childFeatureType.getGMLSchema(), fcQName );
    return FeatureFactory.createFeature( null, null, "root", collectionFT, true ); //$NON-NLS-1$
  }

  /**
   * Same as {@link #deserialize(String, String, new NullProgressMonitor())}
   */
  public final static GMLWorkspace deserialize( final String fileBase, final String sourceCrs ) throws GmlSerializeException
  {
    return deserialize( fileBase, sourceCrs, new NullProgressMonitor() );
  }

  public final static GMLWorkspace deserialize( final String fileBase, final String sourceCrs, final IProgressMonitor monitor ) throws GmlSerializeException
  {
    final Charset charset = getShapeDefaultCharset();
    return deserialize( fileBase, sourceCrs, charset, monitor );
  }

  // FIXME:...
  // The shape default charset if IBM850. We use this if it exists on this platform.
  public static Charset getShapeDefaultCharset( )
  {
    try
    {
      return Charset.forName( SHAPE_DEFAULT_CHARSET_IBM850 );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    /* If the shape default charset is not available on this platform, we use the platforms default. */
    return Charset.defaultCharset();
  }

  public final static GMLWorkspace deserialize( final String fileBase, final String sourceCrs, final Charset charset, final IProgressMonitor monitor ) throws GmlSerializeException
  {
    final String taskName = Messages.getString( "org.kalypso.ogc.gml.serialize.ShapeSerializer.2", fileBase ); //$NON-NLS-1$
    final SubMonitor moni = SubMonitor.convert( monitor, taskName, 100 );

    ShapeFile sf = null;

    try
    {
      sf = new ShapeFile( fileBase, charset );
      final IFeatureType featureType = sf.getFeatureType();
      final int fileShapeType = sf.getFileShapeType();

      final Feature rootFeature = ShapeSerializer.createWorkspaceRootFeature( featureType, fileShapeType );
      final GMLWorkspace workspace = rootFeature.getWorkspace();

      final IRelationType listRelation = (IRelationType) rootFeature.getFeatureType().getProperty( PROPERTY_FEATURE_MEMBER );

      final int count = sf.getRecordNum();

      moni.setWorkRemaining( count );
      for( int i = 0; i < count; i++ )
      {
        if( i % 100 == 0 )
          moni.subTask( String.format( "%d / %d", i, count ) ); //$NON-NLS-1$
        final Feature fe = sf.getFeatureByRecNo( rootFeature, listRelation, i + 1, sourceCrs );
        workspace.addFeatureAsComposition( rootFeature, listRelation, -1, fe );

        if( i % 100 == 0 )
          ProgressUtilities.worked( moni, 100 );
      }

      return workspace;
    }
    catch( final CoreException e )
    {
      throw new GmlSerializeException( "Abbruch durch Benutzer" );
    }
    catch( final Exception e )
    {
      throw new GmlSerializeException( Messages.getString( "org.kalypso.ogc.gml.serialize.ShapeSerializer.19" ), e ); //$NON-NLS-1$
    }
    finally
    {
      if( sf != null )
      {
        try
        {
          sf.close();
        }
        catch( final IOException e )
        {
          throw new GmlSerializeException( Messages.getString( "org.kalypso.ogc.gml.serialize.ShapeSerializer.20" ), e ); //$NON-NLS-1$
        }
      }

      moni.done();
    }
  }

  /**
   * This function tries to load a prj file, which contains the coordinate system. If it exists and is a valid one, this
   * coordinate system is returned. If it is not found, the source coordinate system is returned (this should be the one
   * in the gmt). If it does also not exist, null will be returned.
   * 
   * @param prjLocation
   *          Location of the .prj file.
   * @param defaultSrs
   *          Will be returned, if the .prj file could not be read.
   * @return The coordinate system, which should be used to load the shape.
   */
  public static String loadCrs( final URL prjLocation, final String defaultSrs )
  {
    try
    {
      // TODO: Should in the first instance interpret the prj content ...
      // Does not work now because we must create a coordinate system instance then, but we use string codes right now
      final String prjString = UrlUtilities.toString( prjLocation, "UTF-8" ); //$NON-NLS-1$
      if( prjString.startsWith( "EPSG:" ) ) //$NON-NLS-1$
        return prjString;

      return defaultSrs;
    }
    catch( final IOException ex )
    {
      System.out.println( "No prj file found for: " + prjLocation.toString() ); //$NON-NLS-1$
      return defaultSrs;
    }
  }

}