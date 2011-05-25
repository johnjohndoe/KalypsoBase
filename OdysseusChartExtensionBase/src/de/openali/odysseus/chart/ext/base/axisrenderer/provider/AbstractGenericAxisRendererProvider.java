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
package de.openali.odysseus.chart.ext.base.axisrenderer.provider;

import java.awt.Insets;

import de.openali.odysseus.chart.ext.base.axisrenderer.AxisRendererConfig;
import de.openali.odysseus.chart.ext.base.axisrenderer.ExtendedAxisRenderer;
import de.openali.odysseus.chart.ext.base.axisrenderer.ILabelCreator;
import de.openali.odysseus.chart.ext.base.axisrenderer.ITickCalculator;
import de.openali.odysseus.chart.factory.config.parameters.impl.BooleanParser;
import de.openali.odysseus.chart.factory.provider.AbstractAxisRendererProvider;
import de.openali.odysseus.chart.framework.model.layer.IParameterContainer;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.POSITION;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;
import de.openali.odysseus.chart.framework.model.style.ILineStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleSet;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.impl.StyleSetVisitor;

/**
 * @author burtscher1
 */
public abstract class AbstractGenericAxisRendererProvider extends AbstractAxisRendererProvider
{
  private static final String ROLE_AXIS_LINE_STYLE = "axisLine"; //$NON-NLS-1$

  private static final String ROLE_AXIS_TICK_LINE_STYLE = "tickLine"; //$NON-NLS-1$ 

  private static final String ROLE_AXIS_LABEL_STYLE = "axisLabel"; //$NON-NLS-1$

  private static final String ROLE_AXIS_TICK_LABEL_STYLE = "tickLabel"; //$NON-NLS-1$

  /**
   * @see de.openali.odysseus.chart.factory.provider.IAxisRendererProvider#getAxisRenderer()
   */
  @Override
  public IAxisRenderer getAxisRenderer( final POSITION position )
  {
    return getAxisRenderer( position, false );
  }

  protected final IAxisRenderer getAxisRenderer( final POSITION position, final boolean intervallLabeledTick )
  {
    final ITickCalculator tickCalculator = getTickCalculator();
    final ILabelCreator labelCreator = getLabelCreator();
    final AxisRendererConfig rendererConfig = getRendererConfig();
    // TODO remove intervallstuff from axisrenderer, move this to ticklabelrenderer
    rendererConfig.intervallLabeledTick = intervallLabeledTick;
    final IAxisRenderer calendarAxisRenderer = new ExtendedAxisRenderer( getId(), position, labelCreator, tickCalculator, rendererConfig );
    return calendarAxisRenderer;
  }

  private final AxisRendererConfig getRendererConfig( )
  {
    final AxisRendererConfig config = new AxisRendererConfig();
    final StyleSetVisitor visitor = new StyleSetVisitor( true );
    final IStyleSet styleSet = getStyleSet();

    final ILineStyle axisLine = visitor.visit( styleSet, ILineStyle.class, ROLE_AXIS_LINE_STYLE );

    final ITextStyle labelText = visitor.visit( styleSet, ITextStyle.class, ROLE_AXIS_LABEL_STYLE );
    final ILineStyle tickLine = visitor.visit( styleSet, ILineStyle.class, ROLE_AXIS_TICK_LINE_STYLE );
    final ITextStyle tickLabelText = visitor.visit( styleSet, ITextStyle.class, ROLE_AXIS_TICK_LABEL_STYLE );

    config.tickLength = getTickLength();
    config.tickLabelInsets = getTickInsets();
    config.labelInsets = getLabelInsets();
    config.minTickInterval = getMinTickInteval();
    config.hideCut = getHideCut();
    config.fixedWidth = getFixedWidth();
    config.axisLineStyle = axisLine;
    config.tickLineStyle = tickLine;
    config.axisInsets = new Insets( getGap(), 0, getBorderSize(), 0 );
    config.tickLabelStyle = tickLabelText;
    config.labelStyle = labelText;
    config.labelPosition = ALIGNMENT.CENTER;
    return config;
  }

  private int getFixedWidth( )
  {
    return Integer.parseInt( getParameterContainer().getParameterValue( "fixed_width", "0" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private int getBorderSize( )

  {
    return Integer.parseInt( getParameterContainer().getParameterValue( "border_size", "0" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private int getGap( )
  {
    return Integer.parseInt( getParameterContainer().getParameterValue( "gap", "0" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  private boolean getHideCut( )
  {
    final BooleanParser bp = new BooleanParser();
    return getParameterContainer().getParsedParameterValue( "hide_cut", "false", bp ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public abstract ILabelCreator getLabelCreator( );

  private Insets getLabelInsets( )
  {
    final IParameterContainer pc = getParameterContainer();
    final int insetLabel = Integer.parseInt( pc.getParameterValue( "inset_label", "1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    final String insetLabelString = new Integer( insetLabel ).toString();
    final int insetLabelLeft = Integer.parseInt( pc.getParameterValue( "inset_label_left", insetLabelString ) ); //$NON-NLS-1$
    final int insetLabelRight = Integer.parseInt( pc.getParameterValue( "inset_label_right", insetLabelString ) ); //$NON-NLS-1$
    final int insetLabelBottom = Integer.parseInt( pc.getParameterValue( "inset_label_bottom", insetLabelString ) ); //$NON-NLS-1$
    final int insetLabelTop = Integer.parseInt( pc.getParameterValue( "inset_label_top", insetLabelString ) ); //$NON-NLS-1$

    return new Insets( insetLabelTop, insetLabelLeft, insetLabelBottom, insetLabelRight );
  }

  private Number getMinTickInteval( )
  {
    return Integer.parseInt( getParameterContainer().getParameterValue( "min_tick_interval", "0" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  public abstract ITickCalculator getTickCalculator( );

  private Insets getTickInsets( )
  {
    // Ticks setzen; inset_tick definiert Abst�nde f�r alle Seiten; die k�nnen dann f�r jede Seite einzeln �berschrieben
    // werden
    final IParameterContainer pc = getParameterContainer();
    final int insetTick = Integer.parseInt( pc.getParameterValue( "inset_tick", "1" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    final String insetTickString = new Integer( insetTick ).toString();
    final int insetTickLeft = Integer.parseInt( pc.getParameterValue( "inset_tick_left", insetTickString ) ); //$NON-NLS-1$
    final int insetTickRight = Integer.parseInt( pc.getParameterValue( "inset_tick_right", insetTickString ) ); //$NON-NLS-1$
    final int insetTickBottom = Integer.parseInt( pc.getParameterValue( "inset_tick_bottom", insetTickString ) ); //$NON-NLS-1$
    final int insetTickTop = Integer.parseInt( pc.getParameterValue( "inset_tick_top", insetTickString ) ); //$NON-NLS-1$

    return new Insets( insetTickTop, insetTickLeft, insetTickBottom, insetTickRight );
  }

  private int getTickLength( )
  {
    return Integer.parseInt( getParameterContainer().getParameterValue( "tick_length", "5" ) ); //$NON-NLS-1$ //$NON-NLS-2$
  }

}
