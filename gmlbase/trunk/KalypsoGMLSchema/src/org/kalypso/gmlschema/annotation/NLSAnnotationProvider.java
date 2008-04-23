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
import java.util.Properties;

import javax.xml.namespace.QName;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.java.util.PropertiesUtilities;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.ReferencedRelationType;
import org.kalypso.gmlschema.xml.QualifiedElement;

/**
 * Abstract implementation of {@link IAnnotationProvider}, which gets the strings from message classes, as used by the
 * 'normal' eclipse i18n mechanisms.
 * 
 * @author Gernot Belger
 */
public class NLSAnnotationProvider implements IAnnotationProvider, IExecutableExtension
{
  private Properties m_properties;

  private URL m_baseUrl;

  private String m_path;

  private static final String SEPARATOR = "_";

  private static final String FEATURE_SEPARATOR = "_";

  /**
   * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
   *      java.lang.String, java.lang.Object)
   */
  public void setInitializationData( final IConfigurationElement config, final String propertyName, final Object data ) throws CoreException
  {
    m_baseUrl = getClass().getResource( "" );
    m_path = (String) data;

    if( m_path == null || m_path.length() == 0 )
      throw new CoreException( StatusUtilities.createErrorStatus( "Path not specified for extension: " + config.getAttribute( "id" ) ) );
  }

  /**
   * @see org.kalypso.gmlschema.annotation.IAnnotationProvider#getAnnotation(java.lang.String, java.lang.Object)
   */
  public IAnnotation getAnnotation( final String lang, final Object annotableObject )
  {
    // REMARK: if it is a referenced element, first try to get the annotation from the
    // referencing element. Only if we get nothing there, return the annotation of the referenced element.
    if( annotableObject instanceof ReferencedRelationType )
    {
      final ReferencedRelationType relType = (ReferencedRelationType) annotableObject;

      final QName qname = relType.getQName();
      final String key = createKey( relType.getFeatureQName(), qname );

      final IAnnotation annotation = getAnnotation( lang, key, qname.getLocalPart() );
      if( annotation != null )
        return annotation;

      // else fall through, try to get the annotation from the referenced element
    }

    if( annotableObject instanceof QualifiedElement )
    {
      final QName qname = ((QualifiedElement) annotableObject).getQName();

      final QName featureQName = annotableObject instanceof IPropertyType ? ((IPropertyType) annotableObject).getFeatureQName() : null;

      final String key = createKey( featureQName, qname );

      return getAnnotation( lang, key );
    }

    return null;
  }

  private String createKey( final QName featureQName, final QName qname )
  {
    if( featureQName == null )
      return qname.getNamespaceURI() + SEPARATOR + qname.getLocalPart();

    return featureQName.getNamespaceURI() + SEPARATOR + featureQName.getLocalPart() + FEATURE_SEPARATOR + qname.getNamespaceURI() + SEPARATOR + qname.getLocalPart();
  }

  private IAnnotation getAnnotation( final String lang, final String key, final String defaultName )
  {
    final Properties props = getProperties();
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
  private synchronized Properties getProperties( )
  {
    if( m_properties == null )
    {
      m_properties = new Properties();
      PropertiesUtilities.loadI18nProperties( m_properties, m_baseUrl, m_path );
    }

    return m_properties;
  }
}
