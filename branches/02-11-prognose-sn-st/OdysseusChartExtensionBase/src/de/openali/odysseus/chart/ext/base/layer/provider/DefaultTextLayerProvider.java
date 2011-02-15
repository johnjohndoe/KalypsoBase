package de.openali.odysseus.chart.ext.base.layer.provider;

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

import java.net.URL;

import de.openali.odysseus.chart.ext.base.layer.DefaultTextLayer;
import de.openali.odysseus.chart.factory.provider.AbstractLayerProvider;
import de.openali.odysseus.chart.framework.model.exception.ConfigurationException;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerProvider;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;
import de.openali.odysseus.chart.framework.util.img.TitleTypeBean;

/**
 * @author Dirk Kuch
 */
public class DefaultTextLayerProvider extends AbstractLayerProvider implements ILayerProvider
{
  public static final String ID = "de.openali.odysseus.chart.ext.base.layer.provider.DefaultTextLayerProvider";

  public static final String PROPERTY_TEXT = "text";

  public static final String PROPERTY_ALIGNMENT_HORIZONTAL = "alignment.horizontal";

  public static final String PROPERTY_ALIGNMENT_VERTICAL = "alignment.vertical";

  public static final String PROPERTY_TEXT_ANCHOR_X = "text.anchor.x";

  public static final String PROPERTY_TEXT_ANCHOR_Y = "text.anchor.y";

  /**
   * @see de.openali.odysseus.chart.factory.provider.ILayerProvider#getLayer(java.net.URL)
   */
  @Override
  public IChartLayer getLayer( final URL context ) throws ConfigurationException
  {
    try
    {
      final TitleTypeBean bean = getTitleBean();
      final DefaultTextLayer layer = new DefaultTextLayer( this, getId(), bean );

      return layer;
    }
    catch( final Throwable t )
    {
      throw new ConfigurationException( "Configuring of .kod line layer theme failed.", t );
    }
  }

  private TitleTypeBean getTitleBean( )
  {
    final IParameterContainer container = getParameterContainer();

    final String text = container.getParameterValue( PROPERTY_TEXT, "" );

    final String propertyAlignmentHorizontal = container.getParameterValue( PROPERTY_ALIGNMENT_HORIZONTAL, "CENTERED_HORIZONTAL" );
    final String propertyAlignmentVertical = container.getParameterValue( PROPERTY_ALIGNMENT_VERTICAL, "CENTERED_VERTICAL" );
    final String propertyTextAnchorX = container.getParameterValue( PROPERTY_TEXT_ANCHOR_X, "CENTERED_HORIZONTAL" );
    final String propertyTextAnchorY = container.getParameterValue( PROPERTY_TEXT_ANCHOR_Y, "CENTERED_VERTICAL" );

    final ALIGNMENT alignmentHorizontal = ALIGNMENT.valueOf( propertyAlignmentHorizontal );
    final ALIGNMENT alignmentVertical = ALIGNMENT.valueOf( propertyAlignmentVertical );
    final ALIGNMENT textAnchorX = ALIGNMENT.valueOf( propertyTextAnchorX );
    final ALIGNMENT textAnchorY = ALIGNMENT.valueOf( propertyTextAnchorY );

    final StyleSetVisitor visitor = new StyleSetVisitor();

    final TitleTypeBean bean = new TitleTypeBean( text );

    bean.setTextStyle( visitor.visit( getStyleSet(), ITextStyle.class, 0 ) );
    bean.setPositionHorizontal( alignmentHorizontal );
    bean.setPositionVertical( alignmentVertical );
    bean.setTextAnchorX( textAnchorX );
    bean.setTextAnchorY( textAnchorY );

    return bean;
  }

}
