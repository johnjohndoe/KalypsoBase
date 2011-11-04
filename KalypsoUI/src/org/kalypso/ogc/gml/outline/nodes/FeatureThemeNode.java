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
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypsodeegree.xml.Marshallable;

/**
 * @author Gernot Belger
 */
public class FeatureThemeNode extends KalypsoThemeNode<IKalypsoFeatureTheme> implements IFontProvider
{
  FeatureThemeNode( final IThemeNode parent, final IKalypsoFeatureTheme theme )
  {
    super( parent, theme );

    Assert.isNotNull( theme );
  }

  /**
   * @see org.kalypso.ogc.gml.outline.IThemeNode#hasChildren()
   */
  @Override
  public boolean hasChildren( )
  {
    final IKalypsoFeatureTheme theme = getElement();
    if( theme.shouldShowLegendChildren() == false )
      return false;

    return super.hasChildren();
  }

  /**
   * @see org.kalypso.ogc.gml.outline.IThemeNode#hasChildren()
   */
  @Override
  public boolean hasChildrenCompact( )
  {
    final IKalypsoFeatureTheme theme = getElement();
    if( theme.shouldShowLegendChildren() == false )
      return false;

    return super.hasChildrenCompact();
  }

  /**
   * @see org.kalypso.ogc.gml.outline.nodes.AbstractThemeNode#getChildren()
   */
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

  /**
   * @see org.kalypso.ogc.gml.outline.KalypsoThemeNode#getElementChildren()
   */
  @Override
  protected Object[] getElementChildren( )
  {
    final IKalypsoStyle[] styles = getElement().getStyles();

    final Predicate noSelectionStyles = new Predicate()
    {
      @Override
      public boolean evaluate( final Object object )
      {
        final IKalypsoStyle style = (IKalypsoStyle) object;
        return !style.isUsedForSelection();
      }
    };

    final List<IKalypsoStyle> filteredList = new ArrayList<IKalypsoStyle>( Arrays.asList( styles ) );
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
        final UserStyleNode usto = (UserStyleNode) object;
        if( usto.getStyle().getName().equals( styleName ) )
          return usto;
      }
      else if( object instanceof FeatureTypeStyleNode )
      {
        final FeatureTypeStyleNode fts = (FeatureTypeStyleNode) object;
        final String ftsName = fts.getStyle().getName();
        if( ftsName != null && ftsName.equals( styleName ) )
          return fts;
      }
    }

    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.outline.nodes.AbstractThemeNode#getLegendGraphic(java.lang.String[], boolean,
   *      org.eclipse.swt.graphics.Font)
   */
  @Override
  public Image getLegendGraphic( final String[] whiteList, final boolean onlyVisible, final Font font )
  {
    /* Check, if this theme is allowed. */
    if( !checkWhiteList( whiteList ) )
      return null;

    /* All elements in this theme. */
    final List<LegendElement> elements = collectElements( font );
    if( elements.size() == 0 )
      return null;

    /* Compute the size for the image. */
    final Rectangle computeSize = computeSize( elements );

    /* Create the image. */
    // HM: quite complicated to create a transparent image; any other ideas?
    final Device device = font.getDevice();
    final ImageData id = new ImageData( computeSize.width, computeSize.height, 32, new PaletteData( 0xFF, 0xFF00, 0xFF0000 ) );
    id.transparentPixel = 0xfffffe;
    final Image image = new Image( device, id );

    /* Need a graphical context. */
    final GC gc = new GC( image );

    final Color transparentColor = new Color( device, new RGB( 0xfe, 0xff, 0xff ) );
    gc.setBackground( transparentColor );
    gc.fillRectangle( image.getBounds() );
    transparentColor.dispose();

    /* Set the font. */
    gc.setFont( font );

    /* Change the color. */
    gc.setForeground( gc.getDevice().getSystemColor( SWT.COLOR_BLACK ) );

    int heightSoFar = BORDER;
    for( int i = 0; i < elements.size(); i++ )
    {
      /* Get the legend element. */
      final LegendElement legendElement = elements.get( i );

      /* Get the icon. */
      final ImageDescriptor icon = legendElement.getImage();
      if( icon != null )
      {
        final Image imageIcon = icon.createImage();
        gc.drawImage( imageIcon, BORDER + legendElement.getLevel() * (ICON_SIZE + GAP), heightSoFar );
        imageIcon.dispose();
      }

      /* Draw the text. */
      final String legendText = legendElement.getText();
      if( legendText != null )
        gc.drawString( legendText, BORDER + ICON_SIZE + GAP + legendElement.getLevel() * (ICON_SIZE + GAP), heightSoFar, true );

      // TODO:
      // Images should be disposed here.
      // But getLegendGraphic returns sometimes images that can be disposed, sometimes not.

      /* Add the height of the element and increase by gap. */
      heightSoFar = heightSoFar + legendElement.getSize().height + GAP;
    }

    gc.dispose();

    return image;
  }

  /**
   * This function collects all elements, contained in the theme.
   * 
   * @param font
   *          The font, to use.
   * @return A list, containing all elements.
   */
  private ArrayList<LegendElement> collectElements( final Font font )
  {
    /* Memory for the elements. */
    final ArrayList<LegendElement> elements = new ArrayList<LegendElement>();
    elements.add( new LegendElement( font, 0, this ) );

    /* Collect all elements. */
    collect( this, font, elements, 1 );

    return elements;
  }

  /**
   * This function collects all elements in an one level array.
   * 
   * @param contentProvider
   *          The content provider, with which the elements can be retrieved.
   * @param font
   *          The font, to use.
   * @param startElement
   *          The element, where the search begins.
   * @param elements
   *          Every element will be added to this list.
   * @param level
   *          The level of recursion.
   */
  private void collect( final IThemeNode node, final Font font, final ArrayList<LegendElement> elements, final int level )
  {
    final IThemeNode[] children = node.getChildrenCompact();
    for( final IThemeNode childNode : children )
    {
      /* Add the element. */
      elements.add( new LegendElement( font, level, childNode ) );

      /* If it has children, call recursive and add them, too. */
      if( childNode.hasChildrenCompact() )
        collect( childNode, font, elements, level + 1 );
    }
  }

  /**
   * This function computes the size for an image with the given elements and the given font.
   * 
   * @param elements
   *          The list of elements.
   */
  private Rectangle computeSize( final List<LegendElement> elements )
  {
    /* Start width. */
    int width = 2 * BORDER;

    /* Start height. */
    int height = 2 * BORDER;

    /* Memory for storing the longest width so far temporarly. */
    int temp = 0;

    /* Memory for the highest level. */
    int amount = 0;

    /* Loop for finding the longest width and for summarizing the height. */
    for( final LegendElement legendElement : elements )
    {
      /* Get the size of the element. */
      final Rectangle size = legendElement.getSize();

      /* For the longest width. */
      if( size.width > temp )
        temp = size.width;

      if( legendElement.getLevel() > amount )
        amount = legendElement.getLevel();

      /* Add the height an a small space. */
      height = height + size.height + GAP;
    }

    /* Store the longest width. */
    width = width + temp + amount * (ICON_SIZE + GAP);

    /* After the last image, there is no need for a gap. */
    height = height - GAP;

    return new Rectangle( 0, 0, width, height );
  }

  /**
   * @see org.kalypso.ogc.gml.outline.nodes.AbstractThemeNode#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
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
