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
package org.kalypso.gmlschema.annotation;

import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.commons.xml.NSPrefixProvider;
import org.kalypso.commons.xml.NSUtilities;
import org.kalypso.contribs.java.net.URNUtilities;
import org.kalypso.contribs.java.util.PropertiesUtilities;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.ReferencedRelationType;
import org.kalypso.gmlschema.property.value.ReferencedPropertyType;
import org.kalypso.gmlschema.xml.QualifiedElement;

/**
 * Abstract implementation of {@link IAnnotationProvider}, which gets the strings from message classes, as used by the
 * 'normal' eclipse i18n mechanisms.
 * 
 * @author Gernot Belger
 */
public class NLSAnnotationProvider implements IAnnotationProvider, IExecutableExtension
{
  private final Map<URL, Properties> m_propertyHash = new HashMap<URL, Properties>();

  private static final char SEPARATOR = '_';

  private static final char FEATURE_SEPARATOR = '_';

  /**
   * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
   *      java.lang.String, java.lang.Object)
   */
  public void setInitializationData( final IConfigurationElement config, final String propertyName, final Object data )
  {
  }

  /**
   * @see org.kalypso.gmlschema.annotation.IAnnotationProvider#getAnnotation(java.lang.String, java.lang.Object)
   */
  public IAnnotation getAnnotation( final String lang, final Object annotableObject )
  {
    if( annotableObject instanceof QualifiedElement )
    {
      final QualifiedElement element = (QualifiedElement) annotableObject;
      final URL context = element.getGMLSchema().getContext();
      final QName qname = element.getQName();

      final QName featureQName = annotableObject instanceof IPropertyType ? ((IPropertyType) annotableObject).getFeatureQName() : null;
      final IAnnotation annotation = findAnnotation( context, lang, featureQName, qname );
      if( annotation != null )
        return annotation;

      // fall through, see below
    }

    // REMARK: if it is a referencing element, first try to get the annotation from the
    // referencing element. Only if we get nothing there, return the annotation of the referenced element.
    // TODO: both cannot work
    if( annotableObject instanceof ReferencedRelationType )
    {
      final ReferencedRelationType relType = (ReferencedRelationType) annotableObject;

      final QName qname = relType.getQName();
      final URL context = relType.getGMLSchema().getContext();

      // TODO: find qname etc. from referenced element

      return findAnnotation( context, lang, relType.getFeatureQName(), qname );
    }

    if( annotableObject instanceof ReferencedPropertyType )
    {
      final ReferencedPropertyType refType = (ReferencedPropertyType) annotableObject;

      final QName qname = refType.getQName();
      final URL context = refType.getGMLSchema().getContext();

      return findAnnotation( context, lang, refType.getFeatureQName(), qname );
    }

    return null;
  }

  private IAnnotation findAnnotation( final URL context, final String lang, final QName featureQName, final QName qname )
  {
    final Properties props = getProperties( context );

    final String[] keys = createKeys( props, featureQName, qname );
    for( final String key : keys )
    {
      final IAnnotation annotation = getAnnotation( props, lang, key, qname.getLocalPart() );
      if( annotation != null )
        return annotation;
    }

    return null;
  }

  private String[] createKeys( final Properties props, final QName featureQName, final QName qname )
  {
    /* Produce all parts */
    final String featureNamespace = featureQName == null ? null : featureQName.getNamespaceURI();
    final String featureLocalPart = featureQName == null ? null : featureQName.getLocalPart();
    final String namespace = qname.getNamespaceURI();
    final String localPart = qname.getLocalPart();

    final String featureShortnameFromProperties = featureNamespace == null ? null : props.getProperty( featureNamespace, null );
    final String shortnameFromProperties = props.getProperty( namespace, null );

    final NSPrefixProvider nsMapper = NSUtilities.getNSProvider();

    final String featureShortnameFromCatalog = featureNamespace == null ? null : nsMapper.getPreferredPrefix( featureNamespace, null );
    final String shortnameFromCatalog = nsMapper.getPreferredPrefix( namespace, null );

    /* Produce the different types of keys */
    final Set<String> keys = new HashSet<String>( 5 );

    // full
    keys.add( buildKey( featureNamespace, featureLocalPart, namespace, localPart ) );
    // full xmlns from properties
    keys.add( buildKey( featureShortnameFromProperties, featureLocalPart, shortnameFromProperties, localPart ) );
    // full xmlns from catalog
    keys.add( buildKey( featureShortnameFromCatalog, featureLocalPart, shortnameFromCatalog, localPart ) );
    // only names
    keys.add( buildKey( null, featureLocalPart, null, localPart ) );
    // only namespace for feature, property has same namespace: full
    keys.add( buildKey( featureNamespace, featureLocalPart, null, localPart ) );
    // only namespace for feature, property has same namespace: from properties
    keys.add( buildKey( featureShortnameFromProperties, featureLocalPart, null, localPart ) );
    // only namespace for feature, property has same namespace: from catalog
    keys.add( buildKey( featureShortnameFromCatalog, featureLocalPart, null, localPart ) );
    // no feature qname
    keys.add( buildKey( null, null, null, localPart ) );

    return keys.toArray( new String[keys.size()] );
  }

  private String buildKey( final String featureNamespace, final String featureLocalPart, final String namespace, final String localPart )
  {
    final String featureNs = featureNamespace == null ? null : URNUtilities.convertURN( featureNamespace );
    final String ns = namespace == null ? null : URNUtilities.convertURN( namespace );

    final StringBuffer sb = new StringBuffer();

    if( featureNs != null )
    {
      sb.append( featureNs );
      sb.append( SEPARATOR );
    }

    if( featureLocalPart != null )
    {
      sb.append( featureLocalPart );
      sb.append( FEATURE_SEPARATOR );
    }

    if( ns != null )
    {
      sb.append( ns );
      sb.append( SEPARATOR );
    }

    sb.append( localPart );

    return sb.toString();
  }

  private IAnnotation getAnnotation( final Properties props, final String lang, final String key, final String defaultName )
  {
    final String nameValue = props.getProperty( key + SEPARATOR + "name", null );
    final String labelValue = props.getProperty( key + SEPARATOR + "label" );
    final String tooltipValue = props.getProperty( key + SEPARATOR + "tooltip" );
    final String descriptionValue = props.getProperty( key + SEPARATOR + "description" );

    if( nameValue == null && labelValue == null && tooltipValue == null && descriptionValue == null )
      return null;

    final DefaultAnnotation annotation = new DefaultAnnotation( lang, defaultName );
    if( nameValue != null )
      annotation.putValue( IAnnotation.ANNO_NAME, nameValue );
    if( labelValue != null )
      annotation.putValue( IAnnotation.ANNO_LABEL, labelValue );
    if( tooltipValue != null )
      annotation.putValue( IAnnotation.ANNO_TOOLTIP, tooltipValue );
    if( descriptionValue != null )
      annotation.putValue( IAnnotation.ANNO_DESCRIPTION, descriptionValue );

    return annotation;
  }

  /**
   * @see org.kalypso.gmlschema.annotation.NLSAnnotationProvider#getProperties()
   */
  private synchronized Properties getProperties( final URL context )
  {
    if( !m_propertyHash.containsKey( context ) )
    {
      // REMARK: we assume here, the last part of the context url is the filename
      // of the .xsd file. So we try to load a file with the same name but extension .properties
      final String filename = FileUtilities.nameFromPath( context.toExternalForm() );
      final String path = FileUtilities.nameWithoutExtension( filename );

      final Properties properties = new Properties();
      PropertiesUtilities.loadI18nProperties( properties, context, path );
      m_propertyHash.put( context, properties );
    }

    return m_propertyHash.get( context );

  }
}
