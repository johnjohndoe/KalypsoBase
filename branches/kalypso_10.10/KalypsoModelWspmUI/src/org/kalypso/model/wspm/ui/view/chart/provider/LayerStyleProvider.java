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
package org.kalypso.model.wspm.ui.view.chart.provider;

import org.eclipse.swt.graphics.RGB;
import org.kalypso.model.wspm.core.IWspmConstants;
import org.kalypso.model.wspm.ui.view.AbstractLayerStyleProvider;

import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.impl.LineStyle;

/**
 * @author kimwerner
 */
public class LayerStyleProvider extends AbstractLayerStyleProvider
{
  private static final String LINE = "_LINE"; //$NON-NLS-1$

  @Override
  protected void createStyles( )
  {
    createCrossSectionSytles();
    createWasserspiegel2dStyle();
  }

  private void createWasserspiegel2dStyle( )
  {
    final ILineStyle ls = getStyleFor( IWspmConstants.PHENOMENON_WATERLEVEL_2D + LINE, LineStyle.class ); //$NON-NLS-1$
    ls.setColor( new RGB( 0, 0, 255 ) );
  }

  private void createCrossSectionSytles( )
  {
    final ILineStyle ls = getStyleFor( IWspmConstants.POINT_PROPERTY_HOEHE + LINE, LineStyle.class ); //$NON-NLS-1$
    ls.setColor( new RGB( 255, 150, 0 ) );
  }
}
