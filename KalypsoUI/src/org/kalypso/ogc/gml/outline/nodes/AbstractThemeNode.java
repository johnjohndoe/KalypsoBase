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
package org.kalypso.ogc.gml.outline.nodes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.commons.command.ICommand;
import org.kalypso.contribs.eclipse.jface.viewers.ViewerUtilities;
import org.kalypso.ogc.gml.AbstractKalypsoTheme;
import org.kalypso.ogc.gml.map.themes.KalypsoLegendTheme;

/**
 * @author Gernot Belger
 */
abstract class AbstractThemeNode<T> implements IThemeNode
{
  /**
   * The border, left free in the image.
   */
  protected static int BORDER = 0;

  /**
   * The size of the icon.
   */
  protected static int ICON_SIZE = 16;

  /**
   * The gap between the single rows.
   */
  protected static int GAP = LegendElement.GAP;

  /**
   * The gap between two images.
   */
  protected static int IMAGE_GAP = 5;

  protected static Object[] EMPTY_CHILDREN = new Object[] {};

  private final IThemeNode m_parent;

  private final T m_element;

  private IThemeNode[] m_childNodes;

  AbstractThemeNode( final IThemeNode parent, final T element )
  {
    m_parent = parent;

    Assert.isNotNull( element );

    m_element = element;
  }

  @Override
  public void dispose( )
  {
    disposeChildren();
  }

  @Override
  public void clear( )
  {
    disposeChildren();
  }

  private void disposeChildren( )
  {
    if( m_childNodes == null )
      return;

    for( final IThemeNode child : m_childNodes )
      child.dispose();
    m_childNodes = null;
  }

  @Override
  public T getElement( )
  {
    return m_element;
  }

  /**
   * Overwritten, else mapping items to object does not work for the tree, as these item get recreated for every call to
   * getChildren.
   * 
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    if( obj instanceof AbstractThemeNode< ? > )
      return getElement() == ((AbstractThemeNode< ? >) obj).getElement();

    return false;
  }

  /**
   * @see #equals(Object)
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    return getElement().hashCode();
  }

  /**
   * @see org.kalypso.ogc.gml.outline.IThemeNode#getParent()
   */
  @Override
  public IThemeNode getParent( )
  {
    return m_parent;
  }

  /**
   * @see org.kalypso.ogc.gml.outline.nodes.IThemeNode#getDescription()
   */
  @Override
  public String getDescription( )
  {
    return null;
  }

  @Override
  public ImageDescriptor getImageDescriptor( )
  {
    final IThemeNode[] children = getChildren();
    if( children.length > 0 )
      return children[children.length - 1].getImageDescriptor();

    return null;
  }

  /**
   * @see org.eclipse.jface.viewers.IFontProvider#getFont(java.lang.Object)
   */
  @Override
  public Font getFont( final Object element )
  {
    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.outline.IThemeNode#hasChildren()
   */
  @Override
  public boolean hasChildren( )
  {
    return getElementChildren().length > 0;
  }

  /**
   * @see org.kalypso.ogc.gml.outline.IThemeNode#hasChildren()
   */
  @Override
  public boolean hasChildrenCompact( )
  {
    final Object[] children = getElementChildren();
    if( children.length == 0 )
      return false;

    if( children.length > 1 )
      return true;

    final IThemeNode childNode = NodeFactory.createNode( this, children[0] );
    final boolean hasChildren = childNode.hasChildrenCompact();
    childNode.dispose();
    return hasChildren;
  }

  @Override
  public IThemeNode[] getChildren( )
  {
    if( m_childNodes != null )
      return m_childNodes;

    final Object[] children = getElementChildren();
    m_childNodes = NodeFactory.createNodes( this, children );
    if( doReverseChildren() )
      ArrayUtils.reverse( m_childNodes );
    return m_childNodes;
  }

  /**
   * Returns <code>true</code> by default, overwrite to change.
   */
  protected boolean doReverseChildren( )
  {
    return true;
  }

  /**
   * @see org.kalypso.ogc.gml.outline.IThemeNode#getChildrenCompact()
   */
  @Override
  public IThemeNode[] getChildrenCompact( )
  {
    /* Get all children. */
    final IThemeNode[] children = getChildren();
    if( children.length == 0 )
      return new IThemeNode[] {};

    /* If there are more than one children or if this node belongs to a cascading theme, return all children. */
    if( children.length > 1 || this instanceof CascadingThemeNode )
      return children;

    /* Else ask the single children for its own children. */
    return children[0].getChildrenCompact();
  }

  /**
   * Return the children of the real element here, they will be wrapped into {@link IThemeNode}'s.
   */
  protected abstract Object[] getElementChildren( );

  /**
   * @see org.eclipse.jface.viewers.ICheckStateProvider#isChecked(java.lang.Object)
   */
  @Override
  public boolean isChecked( final Object element )
  {
    Assert.isTrue( element == this );

    return true;
  }

  /**
   * @see org.eclipse.jface.viewers.ICheckStateProvider#isGrayed(java.lang.Object)
   */
  @Override
  public boolean isGrayed( final Object element )
  {
    Assert.isTrue( element == this );

    return true;
  }

  /**
   * @see org.kalypso.ogc.gml.outline.IThemeNode#resolveI18nString(java.lang.String)
   */
  @Override
  public String resolveI18nString( final String text )
  {
    return m_parent.resolveI18nString( text );
  }

  /**
   * @see org.kalypso.ogc.gml.outline.IThemeNode#setVisible(boolean)
   */
  @Override
  public ICommand setVisible( final boolean checked )
  {
    return null;
  }

  protected TreeViewer getViewer( )
  {
    if( m_parent == null )
      return null;

    return ((AbstractThemeNode< ? >) m_parent).getViewer();
  }

  protected final void refreshViewer( final IThemeNode elementToRefresh )
  {
    elementToRefresh.clear();

    ViewerUtilities.refresh( getViewer(), elementToRefresh, true );
  }

  protected final void updateViewer( final IThemeNode[] elementsToUpdate )
  {
    ViewerUtilities.update( getViewer(), elementsToUpdate, null, true );
  }

  /**
   * @see org.kalypso.ogc.gml.outline.nodes.ILegendProvider#getLegendGraphic(java.lang.String[],
   *      org.eclipse.swt.graphics.Font)
   */
  @Override
  public Image getLegendGraphic( final String[] whiteList, final Font font ) throws CoreException
  {
    /* Legend themes should not provide itself for a legend. */
    if( m_element instanceof KalypsoLegendTheme )
      return null;

    /* Check, if this theme is allowed. */
    if( !checkWhiteList( whiteList ) )
      return null;

    /* Memory for all legends. */
    final List<Image> legends = new ArrayList<Image>();

    /* Get the legend of each child. */
    final IThemeNode[] children = getChildrenCompact();
    for( final IThemeNode childNode : children )
    {
      /* Get the legend. */
      final Image legendGraphic = childNode.getLegendGraphic( whiteList, font );
      if( legendGraphic != null )
        legends.add( legendGraphic );
    }

    /* Create a legend element for this theme. */
    final LegendElement legendElement = new LegendElement( font, 0, this );

    /* Compute the size for the image. */
    final Rectangle computeSize = computeSize( legends, legendElement );

    /* Create the image. */
    final Image image = new Image( font.getDevice(), computeSize.width, computeSize.height );

    /* Need a graphical context. */
    final GC gc = new GC( image );

    /* Set the font. */
    gc.setFont( font );

    /* Change the color. */
    gc.setForeground( gc.getDevice().getSystemColor( SWT.COLOR_BLACK ) );

    /* Get the icon. */
    final ImageDescriptor icon = legendElement.getImage();

    /* Draw for the theme itself. */
    if( icon != null )
    {
      final Image imageIcon = icon.createImage();
      gc.drawImage( imageIcon, BORDER, BORDER );
      imageIcon.dispose();
    }

    /* Draw the text. */
    gc.drawString( legendElement.getText(), BORDER + ICON_SIZE + GAP, BORDER, true );

    int heightSoFar = BORDER + legendElement.getSize().height + GAP;
    for( final Image legend : legends )
    {
      /* Draw the legend. */
      gc.drawImage( legend, BORDER + ICON_SIZE + GAP, heightSoFar );

      // TODO: normally, the images should be disposed here, but
      // this also disposed reused images... (the getLegenGraphics() image should
      // always be disposable (or not)

      /* Increase the height. */
      heightSoFar = heightSoFar + legend.getBounds().height + IMAGE_GAP;
    }

    gc.dispose();

    return image;
  }

  /**
   * This function returns true, if the current theme is allowed by the white list. Otherwise it will return false.
   * 
   * @param whiteList
   *          The list of ids of allowed themes.
   * @return True, if the current theme is allowed. False otherwise.
   */
  protected boolean checkWhiteList( String[] whiteList )
  {
    /* The theme ids, which are allowed. */
    List<String> themeIds = null;
    if( whiteList != null && whiteList.length > 0 )
      themeIds = Arrays.asList( whiteList );

    /* Only show themes in the white list. */
    if( themeIds != null && themeIds.size() > 0 )
    {
      final String id = ((AbstractKalypsoTheme) getElement()).getId();
      if( id != null && id.length() > 0 && !themeIds.contains( id ) )
        return false;
    }

    return true;
  }

  /**
   * This function computes the size for an image with the given elements and the given font.
   * 
   * @param legends
   *          The list of legends.
   * @param legendElement
   *          The legend element for this theme.
   */
  private Rectangle computeSize( final List<Image> legends, final LegendElement legendElement )
  {
    /* The size for this theme. */
    final Rectangle size = legendElement.getSize();

    /* If there are no other legends, the width and height will only represent the size for this theme. */
    if( legends.size() == 0 )
      return new Rectangle( 0, 0, size.width, size.height );

    /* Start width. */
    int width = 2 * BORDER;

    /* Start height. */
    int height = 2 * BORDER;

    /* The height needs to be increased by the height for the theme and a gap. */
    height = height + size.height + GAP;

    /* Memory for storing the longest width so far temporarly (the first value will be the width of the theme). */
    int temp = size.width;

    /* No collect the legends. */
    for( final Image image : legends )
    {
      if( image == null )
        continue;

      /* The width will be largest one. */
      if( image.getBounds().width > temp )
        temp = image.getBounds().width;

      /* The height will be summarized. */
      height = height + image.getBounds().height + IMAGE_GAP;
    }

    /* Store the longest width. */
    width = width + temp + ICON_SIZE + GAP;

    /* After the last image, there is no need for a gap. */
    height = height - IMAGE_GAP;

    return new Rectangle( 0, 0, width, height );
  }

  /**
   * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    final T element = getElement();

    if( adapter.isInstance( element ) )
      return element;

    if( element instanceof IAdaptable )
    {
      final IAdaptable adaptable = (IAdaptable) element;
      final Object adapted = adaptable.getAdapter( adapter );
      if( adapted != null )
        return adapted;
    }

    return null;
  }

  /**
   * Return <code>true</code> by default, overwrite to change.
   * 
   * @see org.kalypso.ogc.gml.outline.nodes.IThemeNode#isCompactable()
   */
  @Override
  public boolean isCompactable( )
  {
    return true;
  }

}
