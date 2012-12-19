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
package org.kalypso.gml.processes.internal.gml;

import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.expressions.PropertyTester;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaCatalog;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.KalypsoGMLSchemaPlugin;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ui.catalogs.FeatureTypePropertiesCatalog;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IFeatureRelation;

/**
 * Tests gml elements for their qname. Can be anything form map.themes to
 * {@link org.kalypsodeegree.model.feature.Feature}s and
 * {@link org.kalypso.ui.editor.gmleditor.part.FeatureAssociationTypeElement}s.
 *
 * @author Gernot Belger
 */
public class GmlPropertyTester extends PropertyTester
{
  public static String NAMESPACE = "org.kalypso.gml"; //$NON-NLS-1$

  private static final String PROPERTY_QNAME = "qname"; //$NON-NLS-1$

  private static final String PROPERTY_TARGET_QNAME = "targetQName"; //$NON-NLS-1$

  private static final String PROPERTY_ROOT_QNAME = "rootQName"; //$NON-NLS-1$

  /**
   * Works on IFeatureProperty only. True if it represents a list of features.
   */
  private static final String PROPERTY_IS_LIST_PROPERTY = "isListProperty"; //$NON-NLS-1$

  /**
   * Works on {@link Feature}s. Is <code>true</code>, if this feature is part of a list.
   */
  private static final String PROPERTY_IS_LIST_FEATURE = "isListFeature"; //$NON-NLS-1$

  private static final String PROPERTY_IS_LIST_FEATURE_FIRST = "isListFeatureFirst"; //$NON-NLS-1$

  private static final String PROPERTY_IS_LIST_FEATURE_LAST = "isListFeatureLast"; //$NON-NLS-1$

  /**
   * Test for a property that is registered in a catalog with the feature type.
   *
   * @see FeatureTypePropertiesCatalog.
   */
  private static final String PROPERTY_CATALOG_PROPERTY = "catalogProperty"; //$NON-NLS-1$

  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    if( PROPERTY_IS_LIST_PROPERTY.equals( property ) )
      return testIsListProperty( receiver );
    if( PROPERTY_IS_LIST_FEATURE.equals( property ) )
      return testIsListFeature( receiver );
    if( PROPERTY_IS_LIST_FEATURE_FIRST.equals( property ) )
      return testIsListFeatureFirst( receiver );
    if( PROPERTY_IS_LIST_FEATURE_LAST.equals( property ) )
      return testIsListFeatureLast( receiver );

    if( PROPERTY_CATALOG_PROPERTY.equals( property ) )
      return testCatalogProperty( receiver, args, expectedValue );

    /* Properties that expect a qname */
    final QName expectedQName = parseQName( expectedValue );
    if( expectedQName == null )
      return false;

    if( PROPERTY_QNAME.equals( property ) )
      return testQName( expectedQName, receiver );

    if( PROPERTY_TARGET_QNAME.equals( property ) )
      return testTargetQName( expectedQName, receiver );

    if( PROPERTY_ROOT_QNAME.equals( property ) )
      return testRootQName( expectedQName, receiver );

    // TODO: add tests for parent, etc.

    throw new IllegalArgumentException( String.format( "Unknown property '%s'", property ) ); //$NON-NLS-1$
  }

  private boolean testIsListFeatureFirst( final Object receiver )
  {
    final List< ? > list = getList( receiver );
    if( list == null )
      return false;

    final int index = list.indexOf( receiver );
    return index == 0;
  }

  private boolean testIsListFeatureLast( final Object receiver )
  {
    final List< ? > list = getList( receiver );
    if( list == null )
      return false;

    final int index = list.indexOf( receiver );
    if( index == -1 )
      return false;

    final int size = list.size();
    return index == size - 1;
  }

  private List< ? > getList( final Object receiver )
  {
    if( receiver instanceof Feature )
    {
      final Feature feature = (Feature) receiver;
      final IRelationType parentRelation = feature.getParentRelation();
      final Feature parent = feature.getOwner();
      if( parent != null && parentRelation != null && parentRelation.isList() )
        return (List< ? >) parent.getProperty( parentRelation );
    }

    return null;
  }

  private boolean testIsListFeature( final Object receiver )
  {
    if( receiver instanceof Feature )
    {
      final Feature feature = (Feature) receiver;
      final IRelationType parentRelation = feature.getParentRelation();
      if( parentRelation != null )
        return parentRelation.isList();
    }

    return false;
  }

  private boolean testQName( final QName expectedQName, final Object receiver )
  {
    /* REMARK: special handling for feature types, because we need the schema */
    if( receiver instanceof Feature )
      return checkEquals( expectedQName, ((Feature) receiver).getFeatureType() );

    final QName qname = findQName( receiver );
    return checkEquals( expectedQName, qname );
  }

  private boolean testTargetQName( final QName expectedQName, final Object receiver )
  {
    if( receiver instanceof IFeatureRelation )
    {
      final IRelationType type = ((IFeatureRelation) receiver).getPropertyType();
      if( type == null )
        return false;

      final IFeatureType targetFeatureType = type.getTargetFeatureType();
      if( targetFeatureType == null )
        return false;

      return checkEquals( expectedQName, targetFeatureType );
    }

    /* Only works for containers of features */
    return false;
  }

  private boolean testRootQName( final QName expectedQName, final Object receiver )
  {
    final GMLWorkspace workspace = findWorkspace( receiver );
    if( workspace == null )
      return false;

    return testQName( expectedQName, workspace.getRootFeature() );
  }

  private boolean testIsListProperty( final Object receiver )
  {
    if( receiver instanceof IFeatureRelation )
    {
      final IRelationType type = ((IFeatureRelation) receiver).getPropertyType();
      if( type == null )
        return false;

      return type.isList();
    }

    return false;
  }

  private GMLSchema findSchema( final QName qname )
  {
    try
    {
      final GMLSchemaCatalog schemaCatalog = KalypsoGMLSchemaPlugin.getDefault().getSchemaCatalog();
      final String namespaceURI = qname.getNamespaceURI();
      return schemaCatalog.getSchema( namespaceURI, (String) null );
    }
    catch( final GMLSchemaException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  private static QName parseQName( final Object expectedValue )
  {
    final String excpectedStr = expectedValue.toString().replaceAll( "\"", "" ); // strip " //$NON-NLS-1$ //$NON-NLS-2$
    return QName.valueOf( excpectedStr );
  }

  private boolean checkEquals( final QName expectedQName, final IFeatureType featureType )
  {
    if( expectedQName == null || featureType == null )
      return false;

    return GMLSchemaUtilities.substitutes( featureType, expectedQName );
  }

  private boolean checkEquals( final QName expectedQName, final QName qname )
  {
    if( expectedQName == null || qname == null )
      return false;

    /* Shortcut if equal */
    if( qname.equals( expectedQName ) )
      return true;

    /* If we have a feature type, additionally test for substitution */
    final GMLSchema schema = findSchema( qname );
    if( schema == null )
      return false;

    final IFeatureType featureType = schema.getFeatureType( qname );
    if( featureType == null )
      return false;

    return GMLSchemaUtilities.substitutes( featureType, expectedQName );
  }

  private QName findQName( final Object receiver )
  {
    if( receiver instanceof Feature )
      return ((Feature) receiver).getQualifiedName();

    if( receiver instanceof IFeatureRelation )
    {
      final IFeatureRelation property = (IFeatureRelation) receiver;
      final IRelationType type = property.getPropertyType();
      if( type != null )
        return type.getQName();
    }

    return null;
  }

  private GMLWorkspace findWorkspace( final Object receiver )
  {
    if( receiver instanceof Feature )
    {
      final Feature feature = (Feature) receiver;
      return feature.getWorkspace();
    }

    if( receiver instanceof IFeatureRelation )
    {
      final Feature parentFeature = ((IFeatureRelation) receiver).getOwner();
      return findWorkspace( parentFeature );
    }

    return null;
  }

  private boolean testCatalogProperty( final Object receiver, final Object[] args, final Object expectedValue )
  {
    if( args.length != 1 )
      return false;

    if( !(receiver instanceof Feature) )
      return false;

    final Feature feature = (Feature) receiver;

    final String catalogProperty = ObjectUtils.toString( args[0] );
    final Boolean expected = Boolean.valueOf( ObjectUtils.toString( expectedValue ) );

    final boolean isOn = FeatureTypePropertiesCatalog.isPropertyOn( feature, catalogProperty );
    return isOn == expected;
  }

}