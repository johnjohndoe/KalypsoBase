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
package org.kalypso.ogc.gml.outline.nodes;

import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.resource.ImageDescriptor;
import org.kalypsodeegree.graphics.sld.Rule;
import org.kalypsodeegree.graphics.sld.Symbolizer;

public class RuleNode extends AbstractThemeNode<Rule>
{
  private final RuleImagePainter m_rulePainter;

  RuleNode( final IThemeNode parent, final Rule rule )
  {
    super( parent, rule );

    m_rulePainter = new RuleImagePainter( rule );
  }

  public Rule getRule( )
  {
    return getElement();
  }

  @Override
  public String toString( )
  {
    final Rule element = getElement();
    if( element == null )
      return "<no rules set>"; //$NON-NLS-1$

    if( element.getTitle() != null )
      return element.getTitle();

    if( element.getName() != null )
      return element.getName();

    return "rule"; //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.ogc.gml.outline.AbstractThemeNode#getElementChildren()
   */
  @Override
  protected Object[] getElementChildren( )
  {
    final Rule element = getElement();

    // We collect all children of the symbolisers,
    // as we do not want to show the symbolisers (they got painted
    // as a combined symbol for this tree object)
    // Instead we return the collection of all their children
    final List<Object> result = new ArrayList<Object>();
    final Symbolizer[] symbolizers = element.getSymbolizers();
    for( final Symbolizer symbolizer : symbolizers )
    {
      final IThemeNode symbTreeObject = NodeFactory.createNode( this, symbolizer );
      final IThemeNode[] children = symbTreeObject.getChildren();
      for( final IThemeNode child : children )
      {
        final AbstractThemeNode< ? > childElement = (AbstractThemeNode< ? >) child;
        result.add( childElement.getElement() );
      }
    }

    return result.toArray( new Object[result.size()] );
  }

  @Override
  public ImageDescriptor getImageDescriptor( )
  {
    final Rule element = getElement();

    if( element == null )
      return null;

    /* Get the size of the symbol. It may be 0 / 0. */
    final Rectangle size = m_rulePainter.getImageSize();

    /* The default size. */
    int width = 16;
    int height = 16;

    /* Adjust if neccessary. */
    if( size != null && size.width > 0 )
      width = size.width;

    /* Adjust if neccessary. */
    if( size != null && size.height > 0 )
      height = size.height;

    /*
     * Draw the image on the fly to avoid the need to dispose it later. This is probably ok, because we wont have too
     * many RuleTreeObjects.
     */

    TreeObjectImage treeImage = null;
    try
    {
      treeImage = new TreeObjectImage( width, height );
      m_rulePainter.paintImage( treeImage.getGC() );

      return treeImage.getImageDescriptor();
    }
    catch( final Throwable t )
    {
      t.printStackTrace();

      return null;
    }
    finally
    {
      if( treeImage != null )
        treeImage.dispose();
    }
  }

  public String getLabel( )
  {
    final Rule rule = getElement();
    if( rule == null )
      return "<no styles set>"; //$NON-NLS-1$

    final String title = rule.getTitle();
    if( title != null )
      return resolveI18nString( title );

    final String name = rule.getName();
    if( name != null )
      return name;

    return "Rule: neither 'title' nor 'name' defined.";
  }

  /**
   * @see org.kalypso.ogc.gml.outline.nodes.AbstractThemeNode#getDescription()
   */
  @Override
  public String getDescription( )
  {
    final Rule rule = getElement();

    final String tooltip = rule.getAbstract();
    return resolveI18nString( tooltip );
  }

  public static RuleNode findObject( final Object[] objects, final String ruleName )
  {
    for( final Object object : objects )
    {
      if( object instanceof RuleNode )
      {
        final RuleNode rto = (RuleNode) object;
        if( rto.getRule().getName().equals( ruleName ) )
          return rto;
      }
    }

    return null;
  }
}