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
package org.kalypsodeegree_impl.graphics.sld;

import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.graphics.sld.LegendGraphic;
import org.kalypsodeegree.graphics.sld.Rule;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree_impl.filterencoding.RoughnessFilter;

/**
 * @author barbarins
 */
public class RoughnessRule implements Rule
{
  private final RoughnessFilter m_roughFilter = new RoughnessFilter();

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#addSymbolizer(org.kalypsodeegree.graphics.sld.Symbolizer)
   */
  @Override
  public void addSymbolizer( final Symbolizer symbolizer )
  {
// System.out.println(" **** RoughnessRule, non implemented method: addSymbolizer ");
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#getAbstract()
   */
  @Override
  public String getAbstract( )
  {
// System.out.println(" **** RoughnessRule, non implemented method: getAbstract");
    return null;
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#getFilter()
   */
  @Override
  public Filter getFilter( )
  {
    return m_roughFilter;
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#getLegendGraphic()
   */
  @Override
  public LegendGraphic getLegendGraphic( )
  {
// System.out.println(" **** RoughnessRule, non implemented method: getLegendGraphic");
    return null;
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#getMaxScaleDenominator()
   */
  @Override
  public double getMaxScaleDenominator( )
  {
// System.out.println(" **** RoughnessRule, non implemented method: getMaxScaleDenominator");
    return 0;
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#getMinScaleDenominator()
   */
  @Override
  public double getMinScaleDenominator( )
  {
// System.out.println(" **** RoughnessRule, non implemented method: getMinScaleDenominator");
    return 0;
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#getName()
   */
  @Override
  public String getName( )
  {
// System.out.println(" **** RoughnessRule, non implemented method: getName");
    return null;
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#getSymbolizers()
   */
  @Override
  public Symbolizer[] getSymbolizers( )
  {
    return m_roughFilter.getLastRule().getSymbolizers();
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#getTitle()
   */
  @Override
  public String getTitle( )
  {
// System.out.println(" **** RoughnessRule, non implemented method: getTitle");
    return null;
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#hasElseFilter()
   */
  @Override
  public boolean hasElseFilter( )
  {
// System.out.println(" **** RoughnessRule, non implemented method: hasElseFilter");
    return false;
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#removeSymbolizer(org.kalypsodeegree.graphics.sld.Symbolizer)
   */
  @Override
  public void removeSymbolizer( final Symbolizer symbolizer )
  {
// System.out.println(" **** RoughnessRule, non implemented method: removeSymbolizer");
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#setAbstract(java.lang.String)
   */
  @Override
  public void setAbstract( final String abstract1 )
  {
// System.out.println(" **** RoughnessRule, non implemented method: setAbstract");
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#setElseFilter(boolean)
   */
  @Override
  public void setElseFilter( final boolean elseFilter )
  {
// System.out.println(" **** RoughnessRule, non implemented method: setElseFilter");
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#setFilter(org.kalypsodeegree.filterencoding.Filter)
   */
  @Override
  public void setFilter( final Filter filter )
  {
// System.out.println(" **** RoughnessRule, non implemented method: setFilter");
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#setLegendGraphic(org.kalypsodeegree.graphics.sld.LegendGraphic)
   */
  @Override
  public void setLegendGraphic( final LegendGraphic legendGraphic )
  {
// System.out.println(" **** RoughnessRule, non implemented method: setLegendGraphic");
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#setMaxScaleDenominator(double)
   */
  @Override
  public void setMaxScaleDenominator( final double maxScaleDenominator )
  {
// System.out.println(" **** RoughnessRule, non implemented method: setMaxScaleDenominator");
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#setMinScaleDenominator(double)
   */
  @Override
  public void setMinScaleDenominator( final double minScaleDenominator )
  {
// System.out.println(" **** RoughnessRule, non implemented method: setMinScaleDenominator");
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#setName(java.lang.String)
   */
  @Override
  public void setName( final String name )
  {
// System.out.println(" **** RoughnessRule, non implemented method: setName");
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#setSymbolizers(org.kalypsodeegree.graphics.sld.Symbolizer[])
   */
  @Override
  public void setSymbolizers( final Symbolizer[] symbolizers )
  {
// System.out.println(" **** RoughnessRule, non implemented method: setSymbolizers");
  }

  /**
   * @see org.kalypsodeegree.graphics.sld.Rule#setTitle(java.lang.String)
   */
  @Override
  public void setTitle( final String title )
  {
// System.out.println(" **** RoughnessRule, non implemented method: setTitle");
  }

  public void put( final String title, final Rule rule )
  {
    // TODO Auto-generated method stub
    m_roughFilter.put( title, rule );
  }

}
