/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.gmlschema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.gmlschema.annotation.IAnnotationProvider;
import org.kalypso.gmlschema.annotation.NLSAnnotationProvider;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.virtual.IVirtualFunctionValuePropertyType;
import org.kalypso.gmlschema.property.virtual.VirtualFunctionValuePropertyType;
import org.kalypso.gmlschema.types.IMarshallingTypeHandler;
import org.kalypso.gmlschema.types.MarshallingTypeRegistrySingleton;

/**
 * @author Gernot Belger
 */
public class KalypsoGmlSchemaExtensions
{
  private static final String EXT_ANNOTATIONPROVIDER = "org.kalypso.gmlschema.annotationProvider";

  private static final String EXT_VIRTUALPROPERTY = "org.kalypso.gmlschema.virtualProperty";

  private static final String ATTR_VIRTUALPROPERTY_ALLOWSUBST = "allowSubstitution";

  private static final String ATTR_VIRTUALPROPERTY_FEATURE = "feature";

  private static final String ATTR_VIRTUALPROPERTY_QNAME = "qname";

  private static final String ATTR_VIRTUALPROPERTY_VALUE = "value";

  private static final String ATTR_VIRTUALPROPERTY_ISLIST = "isList";

  private static final String ATTR_VIRTUALPROPERTY_FUNCTION = "function";

  private static final String ATTR_VIRTUALPROPERTY_PROPERTY = "property";

  private static final String ATTR_VIRTUALPROPERTY_NAME = "name";

  private static IAnnotationProvider[] THE_ANNOTATION_PROVIDERS = null;

  private static Map<QName, Collection<IConfigurationElement>> REGISTERED_VIRTUAL_PROPERTIES = null;

  public static IAnnotationProvider[] getAnnotationProviders( )
  {
    if( THE_ANNOTATION_PROVIDERS != null )
      return THE_ANNOTATION_PROVIDERS;

    Debug.ANNOTATION_PROVIDERS.printf( "Reading annotation providers fomr registry .%n" );

    final IExtensionRegistry registry = Platform.getExtensionRegistry();

    final IExtensionPoint extensionPoint = registry.getExtensionPoint( EXT_ANNOTATIONPROVIDER );

    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();

    final List<IAnnotationProvider> providers = new ArrayList<IAnnotationProvider>( configurationElements.length );

    for( final IConfigurationElement element : configurationElements )
    {
      Debug.ANNOTATION_PROVIDERS.printf( "Found provider element: %s", element );

      try
      {
        final Object createExecutableExtension = element.createExecutableExtension( "class" );
        final IAnnotationProvider provider = (IAnnotationProvider) createExecutableExtension;

        Debug.ANNOTATION_PROVIDERS.printf( "Annotation provider successfully created: %s", provider );

        providers.add( provider );
      }
      catch( final Throwable t )
      {
        // In order to prevent bad code from other plug-ins (see Eclipse-PDE-Rules)
        // catch exception here and just log it
        final IStatus status = StatusUtilities.statusFromThrowable( t );
        KalypsoGMLSchemaPlugin.getDefault().getLog().log( status );

        Debug.ANNOTATION_PROVIDERS.printf( "Failed to create annotation provider:" );
        Debug.ANNOTATION_PROVIDERS.printStackTrace( t );
      }
    }

    // We always add the NLSAnnotation as a default annotation provider
    providers.add( new NLSAnnotationProvider() );

    final IAnnotationProvider[] result = providers.toArray( new IAnnotationProvider[providers.size()] );
    THE_ANNOTATION_PROVIDERS = result;
    return result;
  }

  public static IVirtualFunctionValuePropertyType[] createVirtualPropertyTypes( final IFeatureType featureType )
  {
    final Map<QName, Collection<IConfigurationElement>> vptMap = getRegisteredVirtualProperties();

    final Collection<IVirtualFunctionValuePropertyType> foundTypes = new ArrayList<IVirtualFunctionValuePropertyType>();

    IFeatureType ft = featureType;
    while( ft != null )
    {
      final QName qname = ft.getQName();
      final Collection<IConfigurationElement> elements = vptMap.get( qname );
      if( elements != null )
      {
        for( final IConfigurationElement element : elements )
        {
          final boolean allowSubst = Boolean.valueOf( element.getAttribute( ATTR_VIRTUALPROPERTY_ALLOWSUBST ) ).booleanValue();

          if( ft == featureType || allowSubst )
          {
            final IVirtualFunctionValuePropertyType vpt = createVirtualPropertType( element );
            foundTypes.add( vpt );
          }
        }
      }

      ft = ft.getSubstitutionGroupFT();
    }

    return foundTypes.toArray( new IVirtualFunctionValuePropertyType[foundTypes.size()] );
  }

  private static IVirtualFunctionValuePropertyType createVirtualPropertType( final IConfigurationElement element )
  {
    final String qnameString = element.getAttribute( ATTR_VIRTUALPROPERTY_QNAME );
    final String featureQNameString = element.getAttribute( ATTR_VIRTUALPROPERTY_VALUE );
    final String valueQNameString = element.getAttribute( ATTR_VIRTUALPROPERTY_VALUE );
    final String isListString = element.getAttribute( ATTR_VIRTUALPROPERTY_ISLIST );
    final String functionId = element.getAttribute( ATTR_VIRTUALPROPERTY_FUNCTION );

    final QName featureQName = QName.valueOf( featureQNameString );
    final QName valueQName = QName.valueOf( valueQNameString );
    final QName propertyQName = QName.valueOf( qnameString );
    final IMarshallingTypeHandler handler = MarshallingTypeRegistrySingleton.getTypeRegistry().getTypeHandlerForTypeName( valueQName );
    final boolean isList = Boolean.valueOf( isListString );
    if( isList == true )
      throw new UnsupportedOperationException( "isList == true not yet supported in virtualProperty extension-point" );

    // Read properties from child elements
    final Map<String, String> properties = new HashMap<String, String>();

    final IConfigurationElement[] children = element.getChildren( ATTR_VIRTUALPROPERTY_PROPERTY );
    for( final IConfigurationElement child : children )
    {
      final String propName = child.getAttribute( ATTR_VIRTUALPROPERTY_NAME );
      final String propValue = child.getAttribute( ATTR_VIRTUALPROPERTY_VALUE );
      properties.put( propName, propValue );
    }

    return new VirtualFunctionValuePropertyType( handler, propertyQName, featureQName, functionId, properties );
  }

  private synchronized static Map<QName, Collection<IConfigurationElement>> getRegisteredVirtualProperties( )
  {
    if( REGISTERED_VIRTUAL_PROPERTIES == null )
      return REGISTERED_VIRTUAL_PROPERTIES = createRegisteredVirtualProperties();

    return REGISTERED_VIRTUAL_PROPERTIES;
  }

  private static Map<QName, Collection<IConfigurationElement>> createRegisteredVirtualProperties( )
  {
    final Map<QName, Collection<IConfigurationElement>> result = new HashMap<QName, Collection<IConfigurationElement>>();

    final IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint( EXT_VIRTUALPROPERTY );

    final IConfigurationElement[] configurationElements = extensionPoint.getConfigurationElements();

    for( final IConfigurationElement element : configurationElements )
    {
      try
      {
        final String featureQNameString = element.getAttribute( ATTR_VIRTUALPROPERTY_FEATURE );
        final QName featureQName = QName.valueOf( featureQNameString );

        if( !result.containsKey( featureQName ) )
          result.put( featureQName, new ArrayList<IConfigurationElement>() );

        final Collection<IConfigurationElement> elements = result.get( featureQName );
        elements.add( element );
      }
      catch( final Throwable t )
      {
        // In order to prevent bad code from other plug-ins (see Eclipse-PDE-Rules)
        // catch exception here and just log it
        final IStatus status = StatusUtilities.statusFromThrowable( t );
        KalypsoGMLSchemaPlugin.getDefault().getLog().log( status );
      }
    }

    return result;
  }

}
