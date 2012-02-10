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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypsodeegree.graphics.displayelements.DisplayElement;
import org.kalypsodeegree.graphics.displayelements.IncompatibleGeometryTypeException;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.graphics.transformation.GeoTransform;
import org.kalypsodeegree.model.geometry.GM_Object;
import org.kalypsodeegree_impl.graphics.displayelements.DisplayElementFactory;
import org.kalypsodeegree_impl.graphics.transformation.WorldToScreenTransform;

/**
 * @author Gernot Belger
 */
public abstract class SymbolizerPreview<S extends Symbolizer> extends Preview<S>
{
  public SymbolizerPreview( final Composite parent, final Point size, final IStyleInput<S> input )
  {
    super( parent, size, input );
  }

  @Override
  protected void doPaintData( final Graphics2D g2d, final int width, final int height, final GM_Object geom )
  {
    try
    {
      final GeoTransform projection = new WorldToScreenTransform( 0, 0, 1, 1, null, 0, 0, width, height, null );

      final DisplayElement displayElement = createDisplayElement( geom );
      if( displayElement != null )
        displayElement.paint( g2d, projection, new NullProgressMonitor() );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }
    catch( final IncompatibleGeometryTypeException e )
    {
      e.printStackTrace();
    }
  }

  protected DisplayElement createDisplayElement( final GM_Object geom ) throws IncompatibleGeometryTypeException
  {
    return DisplayElementFactory.buildDisplayElement( null, getInputData(), geom, null );
  }
}