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

import javax.xml.namespace.QName;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.core.runtime.IAdaptable;
import org.kalypso.gmlschema.GMLSchema;
import org.kalypso.gmlschema.GMLSchemaCatalog;
import org.kalypso.gmlschema.GMLSchemaException;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.KalypsoGMLSchemaPlugin;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.IQNameProvider;

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

  @Override
  public boolean test( final Object receiver, final String property, final Object[] args, final Object expectedValue )
  {
    final IQNameProvider qnameProvider = findQNameProvider( receiver );
    if( qnameProvider == null )
      return false;

    final QName expectedQName = parseQName( expectedValue );
    if( expectedQName == null )
      return false;

    if( PROPERTY_QNAME.equals( property ) )
      return testQName( expectedQName, qnameProvider );

    if( PROPERTY_TARGET_QNAME.equals( property ) )
      return testTargetQName( expectedQName, qnameProvider );

    if( PROPERTY_ROOT_QNAME.equals( property ) )
      return testRootQName( expectedQName, qnameProvider );

    // TODO: add tests for parent, etc.

    throw new IllegalArgumentException( String.format( "Unknown property '%s'", property ) ); //$NON-NLS-1$
  }

  private IQNameProvider findQNameProvider( final Object receiver )
  {
    if( receiver instanceof IQNameProvider )
      return (IQNameProvider) receiver;

    if( receiver instanceof IAdaptable )
    {
      final IAdaptable adaptable = (IAdaptable) receiver;
      final Object adapted = adaptable.getAdapter( IQNameProvider.class );
      if( adapted instanceof IQNameProvider )
        return (IQNameProvider) adapted;
    }

    return null;
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

  private boolean testQName( final QName expectedQName, final IQNameProvider qnameProvider )
  {
    final QName qname = qnameProvider.getQualifiedName();
    return checkEquals( expectedQName, qname );
  }

  private boolean testTargetQName( final QName expectedQName, final IQNameProvider qnameProvider )
  {
    final QName qname = qnameProvider.getTargetQualifiedName();
    return checkEquals( expectedQName, qname );
  }

  private boolean testRootQName( final QName expectedQName, final IQNameProvider qnameProvider )
  {
    // REMARK: at the moment this works only for features; maybe put something like getParent() into IQNameProvider?
    if( qnameProvider instanceof Feature )
    {
      final Feature feature = (Feature) qnameProvider;
      final GMLWorkspace workspace = feature.getWorkspace();
      if( workspace != null )
        return testQName( expectedQName, workspace.getRootFeature() );
    }

    return false;
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
    final IFeatureType featureType = schema.getFeatureType( qname );
    if( featureType == null )
      return false;

    return GMLSchemaUtilities.substitutes( featureType, expectedQName );
  }
}