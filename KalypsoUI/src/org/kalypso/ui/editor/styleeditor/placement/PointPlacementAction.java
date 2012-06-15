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
package org.kalypso.ui.editor.styleeditor.placement;

import org.kalypso.i18n.Messages;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.util.StyleElementAction;
import org.kalypsodeegree.graphics.sld.LabelPlacement;
import org.kalypsodeegree.graphics.sld.PointPlacement;
import org.kalypsodeegree.graphics.sld.TextSymbolizer;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author Gernot Belger
 */
public class PointPlacementAction extends StyleElementAction<TextSymbolizer>
{
  public PointPlacementAction( final IStyleInput<TextSymbolizer> input )
  {
    super( input );

    setText( Messages.getString("PointPlacementAction_0") ); //$NON-NLS-1$

    setToolTipText( Messages.getString("PointPlacementAction_1") ); //$NON-NLS-1$

    update();
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.util.StyleElementAction#checkEnabled(java.lang.Object)
   */
  @Override
  protected boolean checkEnabled( final TextSymbolizer data )
  {
    final LabelPlacement placement = data.getLabelPlacement();
    if( placement == null )
      return true;

    return placement.getPointPlacement() == null;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.util.StyleElementAction#changeElement(java.lang.Object)
   */
  @Override
  protected void changeElement( final TextSymbolizer data )
  {
    final LabelPlacement placement = getOrCreatePlacement( data );
    if( placement == null )
      return;

    if( placement.getPointPlacement() == null )
      placement.setPointPlacement( StyleFactory.createPointPlacement() );
  }

  static LabelPlacement getOrCreatePlacement( final TextSymbolizer symbolizer )
  {
    final LabelPlacement placement = symbolizer.getLabelPlacement();
    if( placement != null )
      return placement;

    final LabelPlacement newPlacement = StyleFactory.createLabelPlacement( (PointPlacement) null );
    symbolizer.setLabelPlacement( newPlacement );
    return newPlacement;
  }
}