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
package org.kalypso.ui.editor.styleeditor;

/**
 * Gives hints to the style editor components, how to render their contents.
 * 
 * @author Gernot Belger
 */
public interface IStyleEditorConfig
{
  /**
   * If this is <code>false</code>, the properties section of the {@link org.kalypso.ui.editor.styleeditor.style.FeatureTypeStyleComposite} will be hidden.
   */
  boolean isFeatureTypeStyleCompositeShowProperties( );

  /**
   * If this is <code>true</code>, rules can be added/(re)moved by the tab viewer.
   */
  boolean isRuleTabViewerAllowChange( );

  /**
   * If this is <code>false</code>, the graphic section of the stroke is hidden for the {@link org.kalypso.ui.editor.styleeditor.symbolizer.LineSymbolizerComposite}.
   */
  boolean isLineSymbolizerShowGraphic( );

  /**
   * If this is <code>false</code>, the graphic sections of the stroke and fill are hidden for the {@link org.kalypso.ui.editor.styleeditor.symbolizer.PolygonSymbolizerComposite}.
   */
  boolean isPolygonSymbolizerShowGraphic( );

  /**
   * If this is <code>true</code>, symbolizers can be added/(re)moved by the tab viewer.
   */
  boolean isSymbolizerTabViewerAllowChange( );

  /**
   * If this is <code>false</code>, the geometry chooser of the symbolizer composite is hidden.
   */
  boolean isSymbolizerEditGeometry( );

  /**
   * If this is <code>false</code>, the line details of the {@link org.kalypsodeegree.graphics.sld.LineSymbolizer} are
   * hidden.
   */
  boolean isLineSymbolizerStrokeLineDetails( );

  /**
   * If this is <code>false</code>, the line details of the {@link org.kalypsodeegree.graphics.sld.LineSymbolizer} are
   * hidden.
   */
  boolean isPolygonSymbolizerStrokeLineDetails( );

  /**
   * If <code>false</code>, editing the 'name' property of a rule is not allowed.
   */
  boolean isRuleEditName( );
}
