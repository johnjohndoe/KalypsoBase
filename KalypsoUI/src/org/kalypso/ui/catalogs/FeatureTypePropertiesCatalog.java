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
package org.kalypso.ui.catalogs;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.javax.xml.namespace.QNameUnique;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * Returns properties for qnames.
 * 
 * @author Gernot Belger
 */
public final class FeatureTypePropertiesCatalog
{
  private static final String BASETYPE = "uiproperties"; //$NON-NLS-1$

  private static final FeatureTypePropertiesCatalog INSTANCE = new FeatureTypePropertiesCatalog();

  private final Map<String, Properties> m_propertiesCache = new HashMap<>();

  private final Properties m_defaultValues = new Properties();

  /** cache for hidden children propertties */
  private final Map<QName, Set<QName>> m_hiddenChildren = new HashMap<>();

  public static FeatureTypePropertiesCatalog getInstance( )
  {
    return INSTANCE;
  }

  private FeatureTypePropertiesCatalog( )
  {
    m_defaultValues.setProperty( IFeatureTypePropertiesConstants.FEATURE_CREATION_DEPTH, "0" ); //$NON-NLS-1$
    m_defaultValues.setProperty( IFeatureTypePropertiesConstants.GMLTREE_SHOW_CHILDREN, "true" ); //$NON-NLS-1$
    m_defaultValues.setProperty( IFeatureTypePropertiesConstants.GMLTREE_NEW_MENU_ON_FEATURE, "true" ); //$NON-NLS-1$
    m_defaultValues.setProperty( IFeatureTypePropertiesConstants.GMLTREE_NEW_MENU_SHOW_SUB_FEATURES, "false" ); //$NON-NLS-1$
    m_defaultValues.setProperty( IFeatureTypePropertiesConstants.GMLTREE_SHOW_DUPLICATION_MENU, "true" ); //$NON-NLS-1$
    m_defaultValues.setProperty( IFeatureTypePropertiesConstants.THEME_INFO_ID, StringUtils.EMPTY );
  }

  public static Properties getProperties( final URL context, final QName qname )
  {
    return INSTANCE.getProperties2( context, qname );
  }

  public synchronized Properties getProperties2( final URL context, final QName qname )
  {
    /* Try to get cached properties */
    final String contextStr = context == null ? "null" : context.toExternalForm(); //$NON-NLS-1$
    final String qnameStr = qname == null ? "null" : qname.toString(); //$NON-NLS-1$
    final String cacheKey = contextStr + '#' + qnameStr;

    if( m_propertiesCache.containsKey( cacheKey ) )
      return m_propertiesCache.get( cacheKey );

    final Properties properties = new Properties();
    /* Allways add properties, so this lookup takes only place once (not finding anything is very expensive) */
    m_propertiesCache.put( cacheKey, properties );

    final URL url = FeatureTypeCatalog.getURL( BASETYPE, context, qname );
    if( url == null )
      return properties;

    try( InputStream is = url.openStream() )
    {
      properties.load( is );
      is.close();
    }
    catch( final IOException e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      KalypsoCorePlugin.getDefault().getLog().log( status );
    }

    return properties;
  }

  public static boolean isPropertyOn( final Feature feature, final String property )
  {
    if( feature == null )
      return false;

    final IFeatureType featureType = feature.getFeatureType();
    final QNameUnique qName = featureType.getQName();
    final GMLWorkspace workspace = feature.getWorkspace();
    final URL context = workspace == null ? null : workspace.getContext();
    return isPropertyOn( qName, context, property );
  }

  public static boolean isPropertyOn( final QName qname, final URL context, final String property )
  {
    return INSTANCE.isPropertyOn2( qname, context, property );
  }

  public boolean isPropertyOn2( final QName qname, final URL context, final String property )
  {
    final Properties properties = getProperties2( context, qname );

    final String defaultValue = m_defaultValues.getProperty( property );
    // Default values must always be defined
    Assert.isNotNull( defaultValue );

    final String value = properties.getProperty( property, defaultValue );
    return Boolean.parseBoolean( value );
  }

  public boolean isChildHiddenInTree( final QName parentName, final QName childName, final URL context )
  {
    final Set<QName> hiddenChildren = getHiddenChildren( parentName, context );
    return hiddenChildren.contains( childName );
  }

  private synchronized Set<QName> getHiddenChildren( final QName parentName, final URL context )
  {
    if( !m_hiddenChildren.containsKey( parentName ) )
    {
      final Properties properties = getProperties2( context, parentName );
      final String hiddenChildrenValue = properties.getProperty( IFeatureTypePropertiesConstants.GMLTREE_HIDDEN_CHILDREN );

      final Set<QName> hiddenChildren = parseHiddenChildren( hiddenChildrenValue );
      m_hiddenChildren.put( parentName, hiddenChildren );
    }

    return m_hiddenChildren.get( parentName );
  }

  private Set<QName> parseHiddenChildren( final String hiddenChildrenValue )
  {
    final Set<QName> hiddenChildren = new HashSet<>();

    if( StringUtils.isBlank( hiddenChildrenValue ) )
      return hiddenChildren;

    final String[] split = StringUtils.split( hiddenChildrenValue, ',' );
    for( final String element : split )
    {
      try
      {
        final QName qname = QName.valueOf( element );
        hiddenChildren.add( qname );
      }
      catch( final IllegalArgumentException e )
      {
        e.printStackTrace();
      }
    }

    return hiddenChildren;
  }
}