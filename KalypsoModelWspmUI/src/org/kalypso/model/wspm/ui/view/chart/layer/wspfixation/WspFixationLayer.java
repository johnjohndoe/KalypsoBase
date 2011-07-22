/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.model.wspm.ui.view.chart.layer.wspfixation;

import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.ILayerStyleProvider;
import org.kalypso.model.wspm.ui.view.IProfilView;
import org.kalypso.model.wspm.ui.view.chart.layer.wsp.IWspLayerData;
import org.kalypso.model.wspm.ui.view.chart.layer.wsp.WspLayer;

import de.openali.odysseus.chart.framework.model.mapper.ICoordinateMapper;

/**
 * @author Dirk Kuch
 */
public class WspFixationLayer extends WspLayer
{
  public WspFixationLayer( final IProfil profile, final String layerId, final ILayerStyleProvider styleProvider, final IWspLayerData data, final boolean fill, final ICoordinateMapper mapper )
  {
    super( profile, layerId, styleProvider, data, fill, mapper );

    setTitle( Messages.getString("WspFixationLayer_0") ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.chart.layer.wsp.WspLayer#createLayerPanel()
   */
  @Override
  public IProfilView createLayerPanel( )
  {
    return new WspFixationPanel( this );
  }
}
