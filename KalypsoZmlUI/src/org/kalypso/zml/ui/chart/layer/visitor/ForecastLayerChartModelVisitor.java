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
package org.kalypso.zml.ui.chart.layer.visitor;

import org.kalypso.commons.exception.CancelVisitorException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.ogc.sensor.provider.PlainObsProvider;
import org.kalypso.zml.core.diagram.base.IZmlLayer;
import org.kalypso.zml.core.diagram.data.IZmlLayerDataHandler;
import org.kalypso.zml.ui.chart.layer.themes.ZmlForecastLayer;

import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.manager.IChartLayerVisitor;

/**
 * @author Dirk Kuch
 */
public class ForecastLayerChartModelVisitor implements IChartLayerVisitor
{

  private ZmlForecastLayer m_foreCastLayer;

  private IZmlLayerDataHandler m_handler;

  @Override
  public void visit( final IChartLayer layer ) throws CancelVisitorException
  {
    if( !isValid( layer ) )
    {
      return;
    }

    if( layer instanceof ZmlForecastLayer )
    {
      m_foreCastLayer = (ZmlForecastLayer) layer;
    }
    else if( layer instanceof IZmlLayer && layer.isVisible() )
    {
      final IZmlLayer zml = (IZmlLayer) layer;
      final IZmlLayerDataHandler handler = zml.getDataHandler();

      if( Objects.isNotNull( handler ) && Objects.isNotNull( handler.getObservation() ) )
        m_handler = handler;
    }

    if( m_foreCastLayer != null && m_handler != null )
      throw new CancelVisitorException();

    layer.getLayerManager().accept( this );
  }

  private boolean isValid( final IChartLayer layer )
  {
    /** section variantenvergleich - don't use layers of "other" calc case as forecast date! */
    final String identifier = layer.getIdentifier();
    if( identifier.toLowerCase().contains( "other" ) ) //$NON-NLS-1$
      return false;

    return true;
  }

  @Override
  public void doFinialize( )
  {
    if( Objects.isNull( m_foreCastLayer ) )
      return;

    if( Objects.isNull( m_handler ) )
    {
      m_foreCastLayer.setVisible( false );
      m_foreCastLayer.setObsProvider( null );
    }
    else
    {
      m_foreCastLayer.setObsProvider( new PlainObsProvider( m_handler.getObservation(), m_handler.getRequest() ) );
      m_foreCastLayer.setVisible( true );
    }
  }
}
