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
package org.kalypso.ogc.sensor.tableview.rules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Holds a list of rules.
 *
 * @author schlienger
 */
public class Rules implements ITableViewRules
{
  private final List<RenderingRule> m_rules = new ArrayList<>();

  private final Map<Number, RenderingRule[]> m_map = new HashMap<>();

  public Rules( )
  {
    // empty
  }

  /**
   * Constructor with given rules
   *
   * @param rules
   */
  public Rules( final RenderingRule[] rules )
  {
    m_rules.addAll( Arrays.asList( rules ) );
  }

  @Override
  public void addRule( final RenderingRule rule )
  {
    m_rules.add( rule );
  }

  @Override
  public void removeRule( final RenderingRule rule )
  {
    m_rules.remove( rule );
  }

  /**
   * @see org.kalypso.ogc.sensor.tableview.rules.ITableViewRules#findRules(int)
   */
  @Override
  public RenderingRule[] findRules( final int mask )
  {
    return findRules( new Integer( mask ) );
  }

  /**
   * @see org.kalypso.ogc.sensor.tableview.rules.ITableViewRules#findRules(java.lang.Integer)
   */
  @Override
  public RenderingRule[] findRules( final Number mask ) throws NoSuchElementException
  {
    RenderingRule[] r = m_map.get( mask );
    // TODO: the map is never reset, this smells buggy...
    if( r != null )
      return r;

    final int intMask = mask.intValue();

    final List<RenderingRule> lrules = new ArrayList<>();
    for( final RenderingRule rule : m_rules )
    {
      if( rule.contains( intMask ) )
        lrules.add( rule );
    }

    r = lrules.toArray( new RenderingRule[0] );
    m_map.put( mask, r );

    return r;
  }

  /**
   * @see org.kalypso.ogc.sensor.tableview.rules.ITableViewRules#isEmpty()
   */
  @Override
  public boolean isEmpty( )
  {
    return m_rules.size() == 0;
  }

  /**
   * @see org.kalypso.ogc.sensor.tableview.rules.ITableViewRules#getRules()
   */
  @Override
  public List<RenderingRule> getRules( )
  {
    return m_rules;
  }

  /**
   * @see org.kalypso.ogc.sensor.tableview.rules.ITableViewRules#removeAllRules()
   */
  @Override
  public void removeAllRules( )
  {
    m_rules.clear();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return "Rules (Amount= " + m_rules.size() + ")"; //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * @see org.kalypso.ogc.sensor.tableview.rules.ITableViewRules#cloneRules()
   */
  @Override
  public ITableViewRules cloneRules( )
  {
    final Rules rules = new Rules();

    for( final Object element : m_rules )
    {
      final RenderingRule rule = (RenderingRule) element;
      rules.addRule( rule.cloneRule() );
    }

    return rules;
  }
}