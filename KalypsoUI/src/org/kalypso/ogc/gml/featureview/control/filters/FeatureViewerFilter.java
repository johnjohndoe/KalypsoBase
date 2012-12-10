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
package org.kalypso.ogc.gml.featureview.control.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.filterencoding.FilterConstructionException;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.filterencoding.AbstractFilter;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This viewer filter has an init()-function, which will allow the client to send a expression (out of a .gft, for
 * example) to the filter.
 * 
 * @author Holger Albert
 */
public class FeatureViewerFilter extends ViewerFilter implements IViewerFilter
{
  /**
   * The parent workspace.
   */
  private GMLWorkspace m_workspace;

  /**
   * The filter from the expression.
   */
  private Filter m_filter;

  /**
   * The constructor.
   */
  public FeatureViewerFilter( )
  {
    m_workspace = null;
    m_filter = null;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.filters.IViewerFilter#init(org.kalypsodeegree.model.feature.Feature,
   *      java.lang.Object)
   */
  @Override
  public void init( Feature parent, Object expression )
  {
    try
    {
      /* The workspace of the parent. */
      GMLWorkspace workspace = null;
      if( parent != null )
        workspace = parent.getWorkspace();

      /* Try to get the filter from the expression. */
      Filter filter = getFilter( expression );

      if( workspace != null && filter != null )
      {
        m_workspace = workspace;
        m_filter = filter;
      }
    }
    catch( FilterConstructionException ex )
    {
      /* It is safe to ignore this exception, because in this case this filter will allow everything. */
      ex.printStackTrace();
    }
  }

  /**
   * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object,
   *      java.lang.Object)
   */
  @Override
  public boolean select( Viewer viewer, Object parentElement, Object element )
  {
    if( m_filter == null || m_workspace == null )
      return true;

    try
    {
      Feature feature = FeatureHelper.resolveLinkedFeature( m_workspace, element );
      if( feature == null )
        return true;

      return m_filter.evaluate( feature );
    }
    catch( Exception ex )
    {
      /* It is safe to ignore this exception, because in this case this filter will allow the element. */
      ex.printStackTrace();
    }

    return true;
  }

  /**
   * This function builds the filter from an expression.
   */
  private Filter getFilter( Object expression ) throws FilterConstructionException
  {
    /* Check the type. */
    if( expression == null || !(expression instanceof Element) )
      return null;

    /* Cast. */
    Element filterElement = (Element) expression;

    /* Get the child nodes. */
    NodeList childNodes = filterElement.getChildNodes();
    for( int i = 0; i < childNodes.getLength(); i++ )
    {
      Node item = childNodes.item( i );
      if( item instanceof Element )
        return AbstractFilter.buildFromDOM( (Element) item );
    }

    return null;
  }
}