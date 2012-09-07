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
package org.kalypso.zml.core;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.zml.core.base.request.IRequestStrategy;
import org.kalypso.zml.core.table.rules.IZmlCellRuleImplementation;
import org.kalypso.zml.core.table.rules.IZmlColumnRuleImplementation;
import org.kalypso.zml.core.table.rules.impl.grenzwert.IZmlGrenzwertValue;
import org.osgi.framework.Bundle;

/**
 * @author Dirk Kuch
 */
public final class KalypsoZmlCoreExtensions
{
  private static Map<String, IZmlCellRuleImplementation> ZML_CELL_RULES = null;

  private static Map<String, IZmlColumnRuleImplementation> ZML_COLUMN_RULES = null;

  private static Map<String, IZmlGrenzwertValue> ZML_GRENZWERT_DELEGATES = null;

  private static Map<String, IRequestStrategy> REQUEST_STRATEGIES = null;

  private static KalypsoZmlCoreExtensions INSTANCE;

  private KalypsoZmlCoreExtensions( )
  {
  }

  public static synchronized KalypsoZmlCoreExtensions getInstance( )
  {
    if( Objects.isNull( INSTANCE ) )
      INSTANCE = new KalypsoZmlCoreExtensions();

    return INSTANCE;
  }

  public synchronized Map<String, IZmlColumnRuleImplementation> getColumnRules( )
  {
    // fill binding map
    if( Objects.isNull( ZML_COLUMN_RULES ) )
    {
      ZML_COLUMN_RULES = new HashMap<>();

      /* get extension points */
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IConfigurationElement[] elements = registry.getConfigurationElementsFor( IZmlColumnRuleImplementation.EXTENSION_POINT_ID );

      for( final IConfigurationElement element : elements )
      {
        try
        {
          final String pluginid = element.getContributor().getName();
          final Bundle bundle = Platform.getBundle( pluginid );
          final Class< ? > featureClass = bundle.loadClass( element.getAttribute( "rule" ) ); //$NON-NLS-1$
          final Constructor< ? > constructor = featureClass.getConstructor();

          final IZmlColumnRuleImplementation instance = (IZmlColumnRuleImplementation) constructor.newInstance();
          ZML_COLUMN_RULES.put( instance.getIdentifier(), instance );
        }
        catch( final Throwable e )
        {
          e.printStackTrace();
        }
      }
    }

    return ZML_COLUMN_RULES;
  }

  public synchronized IZmlColumnRuleImplementation findColumnRule( final String identifier )
  {
    final Map<String, IZmlColumnRuleImplementation> rules = getColumnRules();

    return rules.get( identifier );
  }

  /**
   * @return list of feature binding handlers, handling a special featureType qname
   */
  public synchronized Map<String, IZmlCellRuleImplementation> getCellRules( )
  {
    // fill binding map
    if( Objects.isNull( ZML_CELL_RULES ) )
    {
      ZML_CELL_RULES = new HashMap<>();

      /* get extension points */
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IConfigurationElement[] elements = registry.getConfigurationElementsFor( IZmlCellRuleImplementation.EXTENSION_POINT_ID );

      for( final IConfigurationElement element : elements )
      {
        try
        {
          final String pluginid = element.getContributor().getName();
          final Bundle bundle = Platform.getBundle( pluginid );
          final Class< ? > featureClass = bundle.loadClass( element.getAttribute( "rule" ) ); //$NON-NLS-1$
          final Constructor< ? > constructor = featureClass.getConstructor();

          final IZmlCellRuleImplementation instance = (IZmlCellRuleImplementation) constructor.newInstance();
          ZML_CELL_RULES.put( instance.getIdentifier(), instance );
        }
        catch( final Throwable e )
        {
          e.printStackTrace();
        }
      }
    }

    return ZML_CELL_RULES;
  }

  /**
   * @return list of feature binding handlers, handling a special featureType qname
   */
  public synchronized IZmlCellRuleImplementation findCellRule( final String identifier )
  {
    final Map<String, IZmlCellRuleImplementation> rules = getCellRules();

    return rules.get( identifier );
  }

  public synchronized Map<String, IZmlGrenzwertValue> getGrenzwertDelegates( )
  {
    // fill binding map
    if( Objects.isNull( ZML_GRENZWERT_DELEGATES ) )
    {
      ZML_GRENZWERT_DELEGATES = new HashMap<>();

      /* get extension points */
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IConfigurationElement[] elements = registry.getConfigurationElementsFor( IZmlGrenzwertValue.EXTENSION_POINT_ID );

      for( final IConfigurationElement element : elements )
      {
        try
        {
          final String pluginid = element.getContributor().getName();
          final Bundle bundle = Platform.getBundle( pluginid );
          final Class< ? > featureClass = bundle.loadClass( element.getAttribute( "implementation" ) ); //$NON-NLS-1$
          final Constructor< ? > constructor = featureClass.getConstructor();

          final IZmlGrenzwertValue instance = (IZmlGrenzwertValue) constructor.newInstance();
          ZML_GRENZWERT_DELEGATES.put( instance.getIdentifier(), instance );
        }
        catch( final Throwable e )
        {
          e.printStackTrace();
        }
      }
    }

    return ZML_GRENZWERT_DELEGATES;
  }

  /**
   * @return list of feature binding handlers, handling a special featureType qname
   */
  public synchronized IZmlGrenzwertValue findGrenzwertDelegate( final String identifier )
  {
    final Map<String, IZmlGrenzwertValue> rules = getGrenzwertDelegates();

    return rules.get( identifier );
  }

  public synchronized Map<String, IRequestStrategy> getRequestStrategies( )
  {
    // fill binding map
    if( Objects.isNull( REQUEST_STRATEGIES ) )
    {
      REQUEST_STRATEGIES = new HashMap<>();

      /* get extension points */
      final IExtensionRegistry registry = Platform.getExtensionRegistry();
      final IConfigurationElement[] elements = registry.getConfigurationElementsFor( "org.kalypso.zml.core.requestStrategy" ); //$NON-NLS-1$

      for( final IConfigurationElement element : elements )
      {
        try
        {

          final String identifier = element.getAttribute( "id" ); //$NON-NLS-1$
          final String pluginid = element.getContributor().getName();
          final Bundle bundle = Platform.getBundle( pluginid );
          final Class< ? > featureClass = bundle.loadClass( element.getAttribute( "strategy" ) ); //$NON-NLS-1$
          final Constructor< ? > constructor = featureClass.getConstructor();

          final IRequestStrategy instance = (IRequestStrategy) constructor.newInstance();
          REQUEST_STRATEGIES.put( identifier, instance );
        }
        catch( final Throwable e )
        {
          e.printStackTrace();
        }
      }
    }

    return REQUEST_STRATEGIES;
  }

  public synchronized IRequestStrategy findStrategy( final String identifier )
  {
    final Map<String, IRequestStrategy> strategies = getRequestStrategies();

    return strategies.get( identifier );
  }

}
