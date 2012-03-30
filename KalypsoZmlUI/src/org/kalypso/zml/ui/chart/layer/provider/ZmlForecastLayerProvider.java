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
package org.kalypso.zml.ui.chart.layer.provider;

import java.net.URL;

import org.kalypso.zml.core.base.request.IRequestHandler;
import org.kalypso.zml.core.base.request.MetadataRequestHandler;
import org.kalypso.zml.ui.chart.layer.themes.ZmlForecastLayer;
import org.kalypso.zml.ui.i18n.Messages;

import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;

/**
 * @author Dirk Kuch
 */
public class ZmlForecastLayerProvider extends AbstractLayerProvider
{
  public static final String ID = "org.kalypso.zml.ui.chart.layer.provider.ZmlForecastLayerProvider"; //$NON-NLS-1$

  @Override
  public IChartLayer getLayer( final URL context ) throws ConfigurationException
  {
    try
    {
      final StyleSetVisitor visitor = new StyleSetVisitor( false );
      final ILineStyle style = visitor.visit( getStyleSet(), ILineStyle.class, 0 );

      return new ZmlForecastLayer( this, style );
    }
    catch( final Throwable t )
    {
      throw new ConfigurationException( Messages.ZmlForecastLayerProvider_1, t );
    }
  }

  protected IRequestHandler getRequestHandler( )
  {
    return new MetadataRequestHandler( getParameterContainer() );
  }

}
