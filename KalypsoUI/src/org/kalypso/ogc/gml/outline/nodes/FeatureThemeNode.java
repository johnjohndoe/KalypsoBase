/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.ogc.gml.outline.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.graphics.Image;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypsodeegree.xml.Marshallable;

/**
 * @author Gernot Belger
 */
public class FeatureThemeNode extends KalypsoThemeNode<IKalypsoFeatureTheme>
{
  FeatureThemeNode( final IThemeNode parent, final IKalypsoFeatureTheme theme )
  {
    super( parent, theme );

    Assert.isNotNull( theme );
  }

  @Override
  public boolean hasChildren( )
  {
    final IKalypsoFeatureTheme theme = getElement();
    if( theme.shouldShowLegendChildren() == false )
      return false;

    return super.hasChildren();
  }

  @Override
  public boolean hasChildrenCompact( )
  {
    final IKalypsoFeatureTheme theme = getElement();
    if( theme.shouldShowLegendChildren() == false )
      return false;

    return super.hasChildrenCompact();
  }

  @Override
  public IThemeNode[] getChildren( )
  {
    final IThemeNode[] children = super.getChildren();

    final IKalypsoFeatureTheme theme = getElement();
    if( theme.shouldShowLegendChildren() == false )
      return new IThemeNode[] {};

    return children;
  }

  private IThemeNode findImageChild( final IThemeNode[] children )
  {
    final String externIconUrn = getElement().getLegendIcon();
    if( externIconUrn == null )
    {
      if( children.length == 0 )
        return null;

      return children[0];
    }

    /* Check, if it is a special URN. */
    final Pattern p = Pattern.compile( "^urn:kalypso:map:theme:swtimage:style:(.*):rule:(.*)$", Pattern.MULTILINE ); //$NON-NLS-1$
    final Matcher m = p.matcher( externIconUrn.trim() );

    if( !m.matches() || m.groupCount() != 2 )
      return null;

    /* A special URN was defined. Evaluate it. */
    final String styleName = m.group( 1 );
    final String ruleName = m.group( 2 );

    final IThemeNode themeNode = findObject( children, styleName );
    if( themeNode == null )
      return null;

    final Object[] ftsChildren = themeNode.getChildren();
    return RuleNode.findObject( ftsChildren, ruleName );
  }

  @Override
  protected Object[] getElementChildren( )
  {
    final IKalypsoStyle[] styles = getElement().getStyles();

    final Predicate noSelectionStyles = new Predicate()
    {
      @Override
      public boolean evaluate( final Object object )
      {
        final IKalypsoStyle style = (IKalypsoStyle)object;
        return !style.isUsedForSelection();
      }
    };

    final List<IKalypsoStyle> filteredList = new ArrayList<>( Arrays.asList( styles ) );
    CollectionUtils.filter( filteredList, noSelectionStyles );
    return filteredList.toArray( new IKalypsoStyle[filteredList.size()] );
  }

  @Override
  protected Image createExternalIcon( final String externIconUrn )
  {
    final IThemeNode imageChild = findImageChild( super.getChildren() );
    if( imageChild == null )
      return super.createExternalIcon( externIconUrn );

    /* Found the right one, need this image icon. */
    final ImageDescriptor descriptor = imageChild.getImageDescriptor();
    if( descriptor == null )
      return null;

    return descriptor.createImage();
  }

  public static IThemeNode findObject( final Object[] objects, final String styleName )
  {
    for( final Object object : objects )
    {
      if( object instanceof UserStyleNode )
      {
        final UserStyleNode usto = (UserStyleNode)object;
        if( usto.getStyle().getName().equals( styleName ) )
          return usto;
      }
      else if( object instanceof FeatureTypeStyleNode )
      {
        final FeatureTypeStyleNode fts = (FeatureTypeStyleNode)object;
        final String ftsName = fts.getStyle().getName();
        if( ftsName != null && ftsName.equals( styleName ) )
          return fts;
      }
    }

    return null;
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter == Marshallable.class )
    {
      final IKalypsoStyle[] styles = getElement().getStyles();
      final StyleSldExporter styleSldExporter = new StyleSldExporter( styles, getLabel() );
      return styleSldExporter.createMarshallable();
    }

    return super.getAdapter( adapter );
  }
}
