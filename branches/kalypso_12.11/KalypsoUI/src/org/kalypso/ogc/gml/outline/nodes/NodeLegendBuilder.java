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

import java.awt.Insets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Transform;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.themes.KalypsoLegendTheme;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * Builds a legend from a {@link org.kalypso.ogc.gml.outline.nodes.IThemeNode}.
 * 
 * @author Gernot Belger
 */
public class NodeLegendBuilder
{
  private static final Insets DEFAULT_INSETS = new Insets( 5, 5, 5, 5 );

  private static final RGB TRANSPARENT_BG = new RGB( 0xfe, 0xff, 0xff );

  private static final int DEFAULT_FONT_SIZE = 10;

  private final Set<String> m_whiteList = new HashSet<>();

  private Point m_fixedSize = new Point( -1, -1 );

  private RGB m_background = TRANSPARENT_BG;

  private Insets m_insets = DEFAULT_INSETS;

  private final boolean m_onlyVisible;

  private int m_fontSize = DEFAULT_FONT_SIZE;

  public NodeLegendBuilder( final String[] themeWhiteList, final boolean onlyVisible )
  {
    if( themeWhiteList != null )
      m_whiteList.addAll( Arrays.asList( themeWhiteList ) );

    m_onlyVisible = onlyVisible;
  }

  /**
   * Creates a legend image for the given node.<br/>
   * The caller is responsible to dispose the returned image.
   */
  public Image createLegend( final IThemeNode[] nodes, final Device device, final IProgressMonitor monitor )
  {
    /* All elements in this theme. */
    final LegendElement[] elements = collectElements( nodes );
    if( elements.length == 0 )
      return null;

    /* Monitor. */
    final SubMonitor progress = SubMonitor.convert( monitor, Messages.getString( "org.kalypso.ogc.gml.map.utilities.MapUtilities.0" ), elements.length ); //$NON-NLS-1$

    GC gc = null;
    Font font = null;
    Color bgColor = null;
    Transform shift = null;
    Image image = null;

    try
    {
      font = new Font( device, JFaceResources.DIALOG_FONT, m_fontSize, SWT.NORMAL );

      /* Compute the size for the image. */
      final Point computeSize = computeSize( elements, font );

      /* Create the image. */
      // HM: quite complicated to create a transparent image; any other ideas?
      final ImageData id = new ImageData( computeSize.x, computeSize.y, 32, new PaletteData( 0xFF, 0xFF00, 0xFF0000 ) );
      id.transparentPixel = 0xfffffe;
      image = new Image( device, id );

      /* Need a graphical context. */
      gc = new GC( image );

      /* Set the background color. */
      bgColor = new Color( device, m_background );
      gc.setBackground( bgColor );
      gc.fillRectangle( image.getBounds() );

      /* Set the font. */
      gc.setFont( font );

      /* Change the color. */
      gc.setForeground( gc.getDevice().getSystemColor( SWT.COLOR_BLACK ) );

      shift = new Transform( device );
      shift.translate( m_insets.left, m_insets.top );
      gc.setTransform( shift );

      for( final LegendElement legendElement : elements )
      {
        /* Monitor. */
        progress.subTask( Messages.getString( "org.kalypso.ogc.gml.map.utilities.MapUtilities.2", legendElement.getText() ) ); //$NON-NLS-1$

        final Point size = legendElement.getSize( font );

        legendElement.paintLegend( gc );

        shift.translate( 0, size.y + LegendElement.GAP );
        gc.setTransform( shift );

        // FIXME: resources not disposed on exception!
        ProgressUtilities.worked( progress, 1 );
      }

      return image;
    }
    catch( final Throwable e )
    {
      if( image != null )
        image.dispose();

      throw e;
    }
    finally
    {
      for( final LegendElement legendElement : elements )
        legendElement.dispose();

      if( bgColor != null )
        bgColor.dispose();

      if( shift != null )
        shift.dispose();

      if( gc != null )
        gc.dispose();
      if( font != null )
        font.dispose();
    }
  }

  /**
   * This function returns true, if the current theme is allowed by the white list. Otherwise it will return false.
   * 
   * @param whiteList
   *          The list of ids of allowed themes.
   * @return True, if the current theme is allowed. False otherwise.
   */
  private boolean checkWhiteList( final IThemeNode node )
  {
    if( m_whiteList.isEmpty() )
      return true;

    final Object element = node.getElement();
    if( !(element instanceof IKalypsoTheme) )
      return true;

    final IKalypsoTheme theme = (IKalypsoTheme)element;

    /* Only show themes in the white list. */
    final String id = theme.getId();
    if( StringUtils.isBlank( id ) )
      return true;

    if( !m_whiteList.contains( id ) )
      return false;

    return true;
  }

  /**
   * This function collects all elements, contained in the theme.
   * 
   * @param font
   *          The font, to use.
   * @return A list, containing all elements.
   */
  private LegendElement[] collectElements( final IThemeNode[] nodes )
  {
    /* Memory for the elements. */
    final List<LegendElement> elements = new ArrayList<>();

    for( final IThemeNode node : nodes )
      collect( node, elements, 0 );

    return elements.toArray( new LegendElement[elements.size()] );
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
  private void collect( final IThemeNode node, final List<LegendElement> elements, final int level )
  {
    if( m_onlyVisible && !node.isChecked( node ) )
      return;

    /* Legend themes should not provide itself for a legend. */
    if( node.getElement() instanceof KalypsoLegendTheme )
      return;

// // FIXME: bad check here
// if( node instanceof WMSLayerNode )
// return;

    /* Check, if this theme is allowed. */
    if( !checkWhiteList( node ) )
      return;

    /* Add tis element */
    elements.add( new LegendElement( level, node ) );

    /* recurse into compact children */
    final IThemeNode[] children = node.getChildrenCompact();
    for( final IThemeNode childNode : children )
      collect( childNode, elements, level + 1 );
  }

  /**
   * This function computes the size for an image with the given elements and the given font.
   * 
   * @param elements
   *          The list of elements.
   */
  private Point computeSize( final LegendElement[] elements, final Font font )
  {
    final Point size = new Point( 0, 0 );

    /* Memory for storing the longest width so far temporarily. */
    int maxWidth = 0;

    /* Memory for the highest level. */
    int maxLevel = 0;

    /* Loop for finding the longest width and for summarizing the height. */
    for( final LegendElement legendElement : elements )
    {
      /* Get the size of the element. */
      final Point elementSize = legendElement.getSize( font );

      maxWidth = Math.max( maxWidth, elementSize.x );
      maxLevel = Math.max( maxLevel, legendElement.getLevel() );

      /* Add the height and a small space. */
      size.y += elementSize.y + LegendElement.GAP;
    }

    /* Store the longest width. */
    size.x += maxWidth;

    /* After the last image, there is no need for a gap. */
    size.y -= LegendElement.GAP;

    /* apply insets */
    size.x += m_insets.left + m_insets.right;
    size.y += m_insets.top + m_insets.bottom;

    /* Apply fixed sizes */
    size.x = m_fixedSize.x > 0 ? m_fixedSize.x : size.x;
    size.y = m_fixedSize.y > 0 ? m_fixedSize.y : size.y;

    return size;
  }

  public void setFixedSize( final Point fixedWidth )
  {
    Assert.isNotNull( fixedWidth );

    m_fixedSize = fixedWidth;
  }

  public void setBackground( final RGB background )
  {
    if( background == null )
      m_background = TRANSPARENT_BG;
    else
      m_background = background;
  }

  public void setInsets( final Insets insets )
  {
    if( insets == null )
      m_insets = DEFAULT_INSETS;
    else
      m_insets = insets;
  }

  public void setFontSize( final int fontSize )
  {
    if( fontSize <= 0 )
      m_fontSize = DEFAULT_FONT_SIZE;
    else
      m_fontSize = fontSize;
  }
}