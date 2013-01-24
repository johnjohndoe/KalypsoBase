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
/*
 * Created on 12.09.2004
 *
 */
package org.kalypso.ui.editor.styleeditor.style;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.kalypso.commons.eclipse.jface.viewers.ITabItem;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ui.editor.styleeditor.binding.StyleInput;
import org.kalypso.ui.editor.styleeditor.tabs.AbstractTabList;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.Rule;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree_impl.filterencoding.BoundaryExpression;
import org.kalypsodeegree_impl.filterencoding.ComplexFilter;
import org.kalypsodeegree_impl.filterencoding.PropertyIsBetweenOperation;
import org.kalypsodeegree_impl.filterencoding.PropertyName;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * This class is fed with rules. It identifies whether it is a normal rule or the rule belongs to a pattern. It collects
 * the rules and returns the number of rule items (-> number of tabitems to be displayed) as a list of Rule and
 * RuleCollection Objects.
 * 
 * @author F.Lindemann
 */
public class RuleOrPatternCollection extends AbstractTabList<FeatureTypeStyle>
{
  private final Map<String, RuleCollection> m_patterns = new HashMap<String, RuleCollection>();

  public RuleOrPatternCollection( final IFeatureTypeStyleInput input )
  {
    super( input );

    init( input.getStyleToSelect() );
  }

  @Override
  public void refresh( )
  {
    final Map<Rule, RuleTabItem> ruleItems = new IdentityHashMap<Rule, RuleTabItem>();
    final Map<RuleCollection, RulePatternTabItem> rulePatternItems = new IdentityHashMap<RuleCollection, RulePatternTabItem>();

    final ITabItem[] items = getItems();
    for( final ITabItem item : items )
    {
      if( item instanceof RuleTabItem )
        ruleItems.put( ((RuleTabItem) item).getRule(), (RuleTabItem) item );
      else if( item instanceof RulePatternTabItem )
        rulePatternItems.put( ((RulePatternTabItem) item).getRuleCollection(), (RulePatternTabItem) item );
    }

    m_patterns.clear();
    internalClear();

    final FeatureTypeStyle fts = getData();
    if( fts != null )
    {
      for( final Rule element : fts.getRules() )
      {
        final ITabItem newItem = createItem( element );
        if( newItem instanceof RuleTabItem )
        {
          final Rule rule = ((RuleTabItem) newItem).getRule();
          if( ruleItems.containsKey( rule ) )
            internalAddItem( ruleItems.get( rule ) );
          else
            internalAddItem( newItem );
        }
        else if( newItem instanceof RulePatternTabItem )
        {
          final RuleCollection rule = ((RulePatternTabItem) newItem).getRuleCollection();
          if( rulePatternItems.containsKey( rule ) )
            internalAddItem( rulePatternItems.get( rule ) );
          else
            internalAddItem( newItem );
        }
      }
    }

    fireChanged();
  }

  private ITabItem createItem( final Rule rule )
  {
    // the name of a rule serves as key for the hashMap
    final String key = rule.getName();
    // it it is a pattern, add to ruleCollection
    if( key == null || !key.startsWith( "-name-" ) )
      return new RuleTabItem( new StyleInput<Rule>( rule, getInput() ) );

    // 1. check whether there is already a rule collection with this rule
    if( m_patterns.containsKey( key ) )
    {
      // if yes - add rule to collection
      m_patterns.get( key ).addRule( rule );
      return null;
    }
    else
    {
      final RuleCollection ruleCollection = new RuleCollection( rule );
      m_patterns.put( key, ruleCollection );
      return new RulePatternTabItem( new StyleInput<RuleCollection>( ruleCollection, getInput() ) );
    }
  }

  public ITabItem addNewItem( )
  {
    final FeatureTypeStyle style = getData();
    if( style == null )
      return null;

    final Rule rule = StyleFactory.createRule( (Symbolizer[]) null );
    style.addRule( rule );

    getInput().fireStyleChanged();

    final ITabItem[] items = getItems();
    return items[items.length - 1];
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabList#removeItem()
   */
  @Override
  public void removeItem( final ITabItem item )
  {
    if( item instanceof RuleTabItem )
      removeRule( (RuleTabItem) item );
    else if( item instanceof RulePatternTabItem )
      removePatternRule( (RulePatternTabItem) item );
    else
      throw new IllegalArgumentException( String.format( "Unknown tab item '%s'", item ) );
  }

  private void removeRule( final RuleTabItem item )
  {
    final FeatureTypeStyle style = getData();
    if( style == null )
      return;

    final Rule rule = item.getRule();
    style.removeRule( rule );

    getInput().fireStyleChanged();

    // TODO
    // the title of a rule serves as key for the hashMap
// final String key = rule.getName();
    // // it it is a pattern, add to ruleCollection
//    if( key != null && key.startsWith( "-name-" ) ) //$NON-NLS-1$
// {
// // 1. check whether there is already a rule collection with this rule
// if( m_patterns.containsKey( key ) )
// {
// // if yes - add rule to collection
// m_patterns.get( key ).removeRule( rule );
// }
// }
// else
// {
//
// for( ITabItem tabItem : items )
// {
// if( tabItem instanceof RuleTabItem )
// {
// if( rule == ((RuleTabItem) tabItem).getRule() )
// iterator.remove();
// }
// }
// }
  }

  private void removePatternRule( final RulePatternTabItem item )
  {
    final RuleCollection ruleCollection = item.getRuleCollection();

    m_patterns.remove( ruleCollection.getId() );

    // TODO: remove all rules of this collection from the style
    // m_fts.removeRule( rule );
    getInput().fireStyleChanged();
  }

  @Override
  public void moveBackward( final int index )
  {
    Assert.isTrue( index > 0 );
    Assert.isTrue( index < size() );

    final FeatureTypeStyle style = getData();
    if( style == null )
      return;

    final Rule[] rules = style.getRules();
    final Rule[] newRules = rules.clone();
    newRules[index] = rules[index - 1];
    newRules[index - 1] = rules[index];
    style.setRules( newRules );

    getInput().fireStyleChanged();
  }

  @Override
  public void moveForward( final int index )
  {
    Assert.isTrue( index >= 0 );
    Assert.isTrue( index < size() - 1 );

    moveBackward( index + 1 );
  }

  public void addNewPatternCollection( )
  {
    final FeatureTypeStyle style = getData();
    if( style == null )
      return;

    final IFeatureType featureType = getFeatureType();
    final IPropertyType[] numericProperties = RuleTabUtils.getNumericProperties( featureType );
    if( numericProperties.length == 0 )
      return;

    // set by default first featuretypeproperty
    final IPropertyType prop = numericProperties[0];
    final PropertyName propertyName = new PropertyName( prop.getQName() );

    final IPropertyType[] geometryProperties = featureType.getAllGeomteryProperties();

    if( geometryProperties.length > 0 )
    {
      final String patternName = "-name-" + new Date().getTime(); //$NON-NLS-1$
      final BoundaryExpression upperBoundary = new BoundaryExpression( "1" ); //$NON-NLS-1$
      final BoundaryExpression lowerBoundary = new BoundaryExpression( "0" ); //$NON-NLS-1$
      final PropertyIsBetweenOperation operation = new PropertyIsBetweenOperation( propertyName, lowerBoundary, upperBoundary );
      final ArrayList<Rule> ruleList = new ArrayList<Rule>();
      ruleList.add( StyleFactory.createRule( null, patternName, "", "abstract", null, new ComplexFilter( operation ), false, 0, Double.MAX_VALUE ) ); //$NON-NLS-1$ //$NON-NLS-2$
      style.addRule( StyleFactory.createRule( null, patternName, "", "abstract", null, new ComplexFilter( operation ), false, 0, Double.MAX_VALUE ) ); //$NON-NLS-1$ //$NON-NLS-2$

      fireChanged();
    }
  }

  public boolean canAddPatternRule( )
  {
    final IFeatureType featureType = getFeatureType();
    final IPropertyType[] numericProperties = RuleTabUtils.getNumericProperties( featureType );
    return numericProperties.length > 0;
  }
}