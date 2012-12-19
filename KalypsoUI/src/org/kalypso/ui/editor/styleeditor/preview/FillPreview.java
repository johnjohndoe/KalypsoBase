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
package org.kalypso.ui.editor.styleeditor.preview;

import java.awt.Graphics2D;

import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.Fill;
import org.kalypsodeegree.model.geometry.GM_Exception;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree.model.geometry.GM_Position;
import org.kalypsodeegree_impl.graphics.sld.awt.FillPainter;
import org.kalypsodeegree_impl.model.geometry.GeometryFactory;

/**
 * @author Gernot Belger
 */
public class FillPreview extends Preview<Fill>
{
  public FillPreview( final Composite parent, final Point size, final IStyleInput<Fill> input )
  {
    super( parent, size, input );
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.preview.Preview#doPaintData(java.awt.Graphics2D, int, int, org.kalypsodeegree.model.geometry.GM_Object)
   */
  @Override
  protected void doPaintData( final Graphics2D g2d, final int width, final int height, final GM_Object geom )
  {
    try
    {
      final FillPainter painter = new FillPainter( getInputData(), null, null, null );
      painter.prepareGraphics( g2d );
      g2d.fillRect( 0, 0, width, height );
    }
    catch( final FilterEvaluationException e )
    {
      e.printStackTrace();
    }
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.preview.Preview#doCreateGeometry()
   */
  @Override
  protected GM_Object doCreateGeometry( ) throws GM_Exception
  {
    final GM_Position pos1 = GeometryFactory.createGM_Position( 0.1, 0.1 );
    final GM_Position pos2 = GeometryFactory.createGM_Position( 0.9, 0.1 );
    final GM_Position pos3 = GeometryFactory.createGM_Position( 0.9, 0.9 );
    final GM_Position pos4 = GeometryFactory.createGM_Position( 0.1, 0.9 );

    final GM_Position[] positions = new GM_Position[] { pos1, pos2, pos3, pos4, pos1 };

    return GeometryFactory.createGM_Surface( positions, null, null );
  }
}