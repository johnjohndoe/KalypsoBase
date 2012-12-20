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
package org.kalypso.model.wspm.ui.view.chart.layer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.wrappers.IProfileRecord;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.ILayerStyleProvider;
import org.kalypso.model.wspm.ui.view.chart.AbstractProfilePointsLayer;

import de.openali.odysseus.chart.framework.model.figure.impl.PolylineFigure;
import de.openali.odysseus.chart.framework.model.layer.EditInfo;
import de.openali.odysseus.chart.framework.model.layer.ILegendEntry;
import de.openali.odysseus.chart.framework.model.layer.impl.LegendEntry;
import de.openali.odysseus.chart.framework.model.mapper.IAxis;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.util.img.ChartImageInfo;

/**
 * @author kimwerner
 */
public class StationLineLayer extends AbstractProfilePointsLayer
{
  private final ILineStyle m_style;

  public StationLineLayer( final String id, final IProfile profil, final String targetRangeProperty, final ILayerStyleProvider styleProvider )
  {
    super( id, profil, targetRangeProperty, styleProvider );

    m_style = styleProvider.getStyleFor( id + ILayerStyleProvider.LINE, ILineStyle.class );
  }

  @Override
  public synchronized ILegendEntry[] getLegendEntries( )
  {
    final LegendEntry le = new LegendEntry( this, toString() )
    {
      @Override
      public void paintSymbol( final GC gc, final Point size )
      {
        drawLine( gc, gc.getClipping() );
      }
    };

    return new ILegendEntry[] { le };
  }

  @Override
  public String getTitle( )
  {
    return Messages.getString( "org.kalypso.model.wspm.ui.view.chart.layer.StationLineLayer.1" ); //$NON-NLS-1$
  }

  @Override
  public void paint( final GC gc, final ChartImageInfo chartImageInfo, final IProgressMonitor monitor )
  {
    final IProfile profil = getProfil();

    if( profil == null )
      return;

    final IProfileRecord[] profilPoints = profil.getPoints();

    final IAxis< ? > targetAxis = getCoordinateMapper().getTargetAxis();
    final int baseLine = targetAxis.numericToScreen( targetAxis.getNumericRange().getMin() );
    for( final IProfileRecord profilPoint : profilPoints )
    {
      final Point point = toScreen( profilPoint );
      if( point == null )
      {
        continue;
      }
      drawLine( gc, new Rectangle( point.x, point.y, 0, baseLine ) );
    }
  }

  protected void drawLine( final GC gc, final Rectangle clipping )
  {
    final PolylineFigure pf = new PolylineFigure( m_style );

    final int lineX = clipping.x + clipping.width / 2;
    pf.setPoints( new Point[] { new Point( lineX, clipping.height ), new Point( lineX, clipping.y ) } );
    pf.paint( gc );
  }

  @Override
  public EditInfo getHover( final Point pos )
  {
    return null;
  }
}