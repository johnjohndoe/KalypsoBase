package org.kalypso.zml.core.table.binding;

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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.JAXBException;

import jregex.Pattern;
import jregex.RETokenizer;

import org.eclipse.core.runtime.CoreException;
import org.kalypso.commons.java.util.StringUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.catalog.ICatalog;
import org.kalypso.zml.core.table.ZmlTableConfigurationLoader;
import org.kalypso.zml.core.table.schema.RuleRefernceType;
import org.kalypso.zml.core.table.schema.RuleSetType;
import org.kalypso.zml.core.table.schema.RuleType;
import org.kalypso.zml.core.table.schema.ZmlTableType;

import com.google.common.collect.MapMaker;

/**
 * @author Dirk Kuch
 */
public final class ZmlRuleResolver
{
  private final Map<String, List<RuleSetType>> m_ruleSetCache;

  private final Map<String, ZmlRule> m_ruleCache;

  private static ZmlRuleResolver INSTANCE;

  private ZmlRuleResolver( )
  {
    final MapMaker marker = new MapMaker().expiration( 30, TimeUnit.MINUTES );
    m_ruleSetCache = marker.makeMap();
    m_ruleCache = marker.makeMap();
  }

  public static ZmlRuleResolver getInstance( )
  {
    if( INSTANCE == null )
      INSTANCE = new ZmlRuleResolver();

    return INSTANCE;
  }

  public ZmlRule findRule( final URL context, final RuleRefernceType reference ) throws CoreException
  {
    try
    {
      final Object ref = reference.getReference();
      if( ref instanceof RuleType )
        return new ZmlRule( (RuleType) ref );

      final String url = reference.getUrl();
      if( url != null )
      {
        final ZmlRule cached = getCachedRule( url );
        if( cached != null )
          return cached;

        final String plainUrl = getUrl( url );
        final String identifier = getAnchor( url );

        ZmlRule rule;
        if( plainUrl.startsWith( "urn:" ) )
          rule = findUrnRule( context, plainUrl, identifier );
        else
          rule = findUrlRule( context, plainUrl, identifier );

        // FIXME: what to do if rule null?
        if( rule != null )
          m_ruleCache.put( url, rule );

        return rule;
      }
    }
    catch( final Throwable t )
    {
      throw new CoreException( StatusUtilities.createExceptionalErrorStatus( "Resolving style failed", t ) );
    }
    throw new IllegalStateException();
  }

  private ZmlRule getCachedRule( final String url )
  {
    // FIXME: we should consider a timeout based on the modification timestamp of the underlying resource here
    // Else, the referenced resource will never be loaded again, even if it has changed meanwhile
    return m_ruleCache.get( url );
  }

  private ZmlRule findUrlRule( final URL context, final String uri, final String identifier ) throws MalformedURLException, JAXBException, CoreException
  {
    final URL absoluteUri = new URL( context, uri );

    List<RuleSetType> ruleSets = m_ruleSetCache.get( uri );
    if( ruleSets == null )
    {
      final ZmlTableConfigurationLoader loader = new ZmlTableConfigurationLoader( absoluteUri );
      final ZmlTableType tableType = loader.getTableType();

      ruleSets = tableType.getRuleSet();

      m_ruleSetCache.put( uri, ruleSets );
    }

    if( ruleSets == null )
      return null;

    for( final RuleSetType ruleSet : ruleSets )
    {

      for( final Object ruleObject : ruleSet.getRuleOrRule() )
      {
        if( ruleObject instanceof RuleType )
        {
          final RuleType ruleType = (RuleType) ruleObject;
          if( ruleType.getId().equals( identifier ) )
            return new ZmlRule( ruleType );
        }
        else if( ruleObject instanceof RuleRefernceType )
        {
          final RuleRefernceType reference = (RuleRefernceType) ruleObject;
          final ZmlRule rule = findRule( context, reference );
          if( rule.getIdentifier().equals( identifier ) )
            return rule;
        }

      }

    }

    return null;
  }

  private ZmlRule findUrnRule( final URL context, final String urn, final String identifier ) throws MalformedURLException, JAXBException, CoreException
  {
    final ICatalog baseCatalog = KalypsoCorePlugin.getDefault().getCatalogManager().getBaseCatalog();
    final String uri = baseCatalog.resolve( urn, urn );

    return findUrlRule( context, uri, identifier );
  }

  private String getUrl( final String url )
  {
    final RETokenizer tokenizer = new RETokenizer( new Pattern( "#.*" ), url ); //$NON-NLS-1$

    return StringUtilities.chop( tokenizer.nextToken() );
  }

  private String getAnchor( final String url )
  {
    final RETokenizer tokenizer = new RETokenizer( new Pattern( ".*#" ), url ); //$NON-NLS-1$

    return StringUtilities.chop( tokenizer.nextToken() );
  }

}
