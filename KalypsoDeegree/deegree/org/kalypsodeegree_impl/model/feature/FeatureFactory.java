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
 * interface-compatibility to deegree is wanted but not retained always.
 *
 * If you intend to use this software in other ways than in kalypso
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
package org.kalypsodeegree_impl.model.feature;

import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;

import org.kalypso.gmlschema.GMLSchemaCatalog;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.IGMLSchema;
import org.kalypso.gmlschema.KalypsoGMLSchemaPlugin;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.xml.Mapper;
import org.kalypsodeegree.model.feature.ArrayFeatureList;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IXLinkedFeature;
import org.kalypsodeegree_impl.model.sort.IEnvelopeProvider;
import org.kalypsodeegree_impl.model.sort.SplitSort;

/**
 * This factory offers methods for creating Features, FeatureCollection and all direct related classes/interfaces that
 * are part of the org.kalypsodeegree.model.feature package.
 * <p>
 * -----------------------------------------------------------------------
 * </p>
 * 
 * @author <a href="mailto:poth@lat-lon.de">Andreas Poth </a>
 * @version $Revision$ $Date$
 */
public final class FeatureFactory
{
  private FeatureFactory( )
  {
    // helper class, do not instantiate
  }

  /**
   * creates an instance of a Feature from its IFeatureType and an array of Objects that represents it properties. It is
   * assumed that the order of the properties is identical to the order of the FeatureTypeProperties of the the
   * IFeatureType.
   * 
   * @param id
   *          unique id of the <CODE>Feature</CODE>
   * @param featureType
   *          <CODE>IFeatureType</CODE> of the <CODE>Feature</CODE>
   * @param properties
   *          properties (content) of the <CODE>Feature</CODE>
   * @return instance of a <CODE>Feature</CODE>
   */
  public static Feature createFeature( final Feature parent, final IRelationType parentRelation, final String id, final IFeatureType featureType, final Object[] properties )
  {
    // / TODO: put the feature into the workspace!
    return ExtendedFeatureFactory.getFeature( parent, parentRelation, featureType, id, properties );
  }

  /**
   * Same as {@link #createFeature(Feature, String, IFeatureType, boolean, false)}.
   */
  public static Feature createFeature( final Feature parent, final IRelationType parentRelation, final String id, final IFeatureType featureType, final boolean initializeWithDefaults )
  {
    return createFeature( parent, parentRelation, id, featureType, initializeWithDefaults, 0 );
  }

  /**
   * Erzeugt ein Feature mit gesetzter ID und f�llt das Feature mit Standardwerten.
   * 
   * @param initializeWithDefaults
   *          set <code>true</code> to generate default properties (e.g. when generating from UserInterface) <br>
   *          set <code>false</code> to not generate default properties (e.g. when reading from GML or so.)
   */
  public static Feature createFeature( final Feature parent, final IRelationType parentRelation, final String id, final IFeatureType featureType, final boolean initializeWithDefaults, final int depth )
  {
    final IPropertyType[] ftp = featureType.getProperties();

    final Feature feature = ExtendedFeatureFactory.getFeature( parent, parentRelation, featureType, id, new Object[ftp.length] );

    // TODO: shouldn't we move this to the Feature_Impl constructor?
    for( final IPropertyType pt : ftp )
    {
      if( pt.isList() )
      {
        if( pt instanceof IRelationType )
          feature.setProperty( pt, FeatureFactory.createFeatureList( feature, (IRelationType)pt ) );
        else
          feature.setProperty( pt, new ArrayList<>() );
      }
      else
      {
        // leave it null
      }
    }

    if( initializeWithDefaults )
    {
      final Map<IPropertyType, Object> properties = FeatureFactory.createDefaultFeatureProperty( feature, depth );
      FeatureHelper.setProperties( feature, properties );
    }

    return feature;
  }

  /** Creates default FeatureProperties, used by LegendView */
  public static Map<IPropertyType, Object> createDefaultFeatureProperty( final Feature feature, final int depth )
  {
    final IPropertyType[] propTypes = feature.getFeatureType().getProperties();

    final Map<IPropertyType, Object> results = new LinkedHashMap<>( propTypes.length );
    for( final IPropertyType ftp : propTypes )
    {
      final Object value = createDefaultFeatureProperty( feature, ftp, depth );
      if( value != null )
        results.put( ftp, value );
    }
    return results;
  }

  private static Object createDefaultFeatureProperty( final Feature feature, final IPropertyType ftp, final int depth )
  {
    if( ftp instanceof IValuePropertyType )
      return createDefaultValueProperty( (IValuePropertyType)ftp );

    if( ftp instanceof IRelationType )
      return createDefaultRelationProperty( feature, (IRelationType)ftp, depth );

    return null;
  }

  private static Object createDefaultValueProperty( final IValuePropertyType vpt )
  {
    final boolean isOptional = vpt.getMinOccurs() == 0;

    // get default value from schema if possible
    final String defaultValue;
    if( vpt.hasDefault() )
      defaultValue = vpt.getDefault();
    else if( vpt.isFixed() )
      defaultValue = vpt.getFixed();
    else
      defaultValue = null;

    // Only fill non optional values with default value set
    if( isOptional || defaultValue == null )
      return null;

    final IMarshallingTypeHandler typeHandler = vpt.getTypeHandler();
    if( typeHandler != null )
    {
      try
      {
        return typeHandler.parseType( defaultValue );
      }
      catch( final ParseException e )
      {
        e.printStackTrace();
        return null;
      }
    }

    return Mapper.defaultValueforJavaType( vpt );
  }

  private static Object createDefaultRelationProperty( final Feature feature, final IRelationType rt, final int depth )
  {
    if( rt.isList() )
      return FeatureFactory.createFeatureList( feature, rt );

    final int minOccurs = rt.getMinOccurs();
    if( depth == 0 || minOccurs == 0 || !rt.isInlineAble() || rt.isLinkAble() )
      return null;

    final int subDepth = depth == -1 ? -1 : depth - 1;

    final IFeatureType targetFeatureType = rt.getTargetFeatureType();

    // we have a single, non-optional, inlinable, not-linkable feature here: create inner feature
    final GMLWorkspace workspace = feature.getWorkspace();
    if( workspace == null )
      return FeatureFactory.createFeature( feature, rt, null, targetFeatureType, true, depth );

    return workspace.createFeature( feature, rt, targetFeatureType, subDepth );
  }

  @SuppressWarnings( "unchecked" )
  public static FeatureList createFeatureList( final Feature parentFeature, final IRelationType parentFTP, final List< ? > list )
  {
    final FeatureList result = new SplitSort( parentFeature, parentFTP );
    result.addAll( list );
    return result;
  }

  public static FeatureList createFeatureList( final Feature parentFeature, final IRelationType parentFTP, final Feature[] features )
  {
    return createFeatureList( parentFeature, parentFTP, Arrays.asList( features ) );
  }

  public static FeatureList createFeatureList( final Feature parentFeature, final IRelationType parentFTP )
  {
    return createFeatureList( parentFeature, parentFTP, (IEnvelopeProvider)null );
  }

  public static FeatureList createFeatureList( final Feature parentFeature, final IRelationType parentFTP, final IEnvelopeProvider envelopeProvider )
  {
    if( parentFTP != null && parentFTP.getMaxOccurs() > IPropertyType.UNBOUND_OCCURENCY && parentFTP.getMaxOccurs() <= ArrayFeatureList.INITIAL_CAPACITY )
      return new ArrayFeatureList( parentFeature, parentFTP, envelopeProvider );
    else
      return new SplitSort( parentFeature, parentFTP, envelopeProvider );
  }

  public static GMLWorkspace createGMLWorkspace( final IGMLSchema schema, final Feature rootFeature, final URL context, final String schemaLocation, final IFeatureProviderFactory factory, final NamespaceContext namespaceContext )
  {
    return new GMLWorkspace_Impl( schema, rootFeature, context, namespaceContext, schemaLocation, factory );
  }

  /**
   * create a new GMLWorkspace with a root feature for the given feature type
   */
  public static GMLWorkspace createGMLWorkspace( final QName rootFeatureQName, final URL context, final IFeatureProviderFactory factory ) throws GMLSchemaException
  {
    return createGMLWorkspace( rootFeatureQName, null, context, factory );
  }

  /**
   * create a new GMLWorkspace with a root feature for the given feature type
   */
  public static GMLWorkspace createGMLWorkspace( final QName rootFeatureQName, final URL context, final IFeatureProviderFactory factory, final int depth ) throws GMLSchemaException
  {
    return createGMLWorkspace( rootFeatureQName, null, context, factory, depth );
  }

  /**
   * create a new GMLWorkspace with a root feature for the given feature type
   */
  public static GMLWorkspace createGMLWorkspace( final QName rootFeatureQName, final String gmlVersion, final URL context, final IFeatureProviderFactory factory ) throws GMLSchemaException
  {
    final IFeatureType rootFeatureType = getFeatureType( rootFeatureQName, gmlVersion );
    return createGMLWorkspace( rootFeatureType, context, factory );
  }

  /**
   * create a new GMLWorkspace with a root feature for the given feature type
   */
  public static GMLWorkspace createGMLWorkspace( final QName rootFeatureQName, final String gmlVersion, final URL context, final IFeatureProviderFactory factory, final int depth ) throws GMLSchemaException
  {
    final IFeatureType rootFeatureType = getFeatureType( rootFeatureQName, gmlVersion );
    return createGMLWorkspace( rootFeatureType, context, factory, depth );
  }

  private static IFeatureType getFeatureType( final QName rootFeatureQName, final String gmlVersion ) throws GMLSchemaException
  {
    final GMLSchemaCatalog schemaCatalog = KalypsoGMLSchemaPlugin.getDefault().getSchemaCatalog();
    final IGMLSchema schema = schemaCatalog.getSchema( rootFeatureQName.getNamespaceURI(), gmlVersion );
    return schema.getFeatureType( rootFeatureQName );
  }

  public static Feature createFeature( final String id, final QName qname ) throws GMLSchemaException
  {
    final IFeatureType featureType = getFeatureType( qname, null );
    return createFeature( null, null, id, featureType, true );
  }

  /**
   * create a new GMLWorkspace with a root feature for the given feature type
   */
  public static GMLWorkspace createGMLWorkspace( final IFeatureType rootFeatureType, final URL context, final IFeatureProviderFactory factory )
  {
    final IGMLSchema schema = rootFeatureType.getGMLSchema();
    final String schemaLocation = null;
    final Feature rootFeature = FeatureFactory.createFeature( null, null, "root", rootFeatureType, true );
    return FeatureFactory.createGMLWorkspace( schema, rootFeature, context, schemaLocation, factory, null );
  }

  /**
   * create a new GMLWorkspace with a root feature for the given feature type
   */
  public static GMLWorkspace createGMLWorkspace( final IFeatureType rootFeatureType, final URL context, final IFeatureProviderFactory factory, final int depth )
  {
    final IGMLSchema schema = rootFeatureType.getGMLSchema();
    final String schemaLocation = null;
    final Feature rootFeature = FeatureFactory.createFeature( null, null, "root", rootFeatureType, true, depth );
    return FeatureFactory.createGMLWorkspace( schema, rootFeature, context, schemaLocation, factory, null );
  }

  /**
   * Only for backwards compatibility. Create a raw xlink without inserting it into the linking workspace.
   */
  @Deprecated
  public static IXLinkedFeature createXLink( final Feature parentFeature, final IRelationType relation, final IFeatureType linkedType, final String href )
  {
    return new XLinkedFeature_Impl( parentFeature, relation, linkedType, href );
  }
}