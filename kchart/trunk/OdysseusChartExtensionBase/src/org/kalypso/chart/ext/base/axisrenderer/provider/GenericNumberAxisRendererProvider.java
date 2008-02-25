package org.kalypso.chart.ext.base.axisrenderer.provider;

import java.awt.Insets;

import org.kalypso.chart.ext.base.axisrenderer.GenericAxisRenderer;
import org.kalypso.chart.ext.base.axisrenderer.GenericNumberTickCalculator;
import org.kalypso.chart.ext.base.axisrenderer.NumberLabelCreator;
import org.kalypso.chart.factory.configuration.exception.AxisRendererProviderException;
import org.kalypso.chart.factory.configuration.parameters.IParameterContainer;
import org.kalypso.chart.factory.configuration.parameters.impl.BooleanParser;

import de.openali.odysseus.chart.factory.provider.AbstractAxisRendererProvider;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

public class GenericNumberAxisRendererProvider extends AbstractAxisRendererProvider
{

  public IAxisRenderer< ? > getAxisRenderer( ) throws AxisRendererProviderException
  {
    final IParameterContainer pc = getParameterContainer();

    final String tick_label_formater = pc.getParameterValue( "tick_label_formater", "%s" );

    // Ticks setzen; inset_tick definiert Abstände für alle Seiten; die können dann für jede Seite einzeln überschrieben
    // werden
    final int insetTick = Integer.parseInt( pc.getParameterValue( "inset_tick", "1" ) );
    final String insetTickString = (new Integer( insetTick )).toString();
    final int insetTick_left = Integer.parseInt( pc.getParameterValue( "inset_tick_left", insetTickString ) );
    final int insetTick_right = Integer.parseInt( pc.getParameterValue( "inset_tick_right", insetTickString ) );
    final int insetTick_bottom = Integer.parseInt( pc.getParameterValue( "inset_tick_bottom", insetTickString ) );
    final int insetTick_top = Integer.parseInt( pc.getParameterValue( "inset_tick_top", insetTickString ) );
    final Insets insetsTick = new Insets( insetTick_top, insetTick_left, insetTick_bottom, insetTick_right );

    final int insetLabel = Integer.parseInt( pc.getParameterValue( "inset_label", "1" ) );
    final String insetLabelString = (new Integer( insetLabel )).toString();
    final int insetLabel_left = Integer.parseInt( pc.getParameterValue( "inset_label_left", insetLabelString ) );
    final int insetLabel_right = Integer.parseInt( pc.getParameterValue( "inset_label_right", insetLabelString ) );
    final int insetLabel_bottom = Integer.parseInt( pc.getParameterValue( "inset_label_bottom", insetLabelString ) );
    final int insetLabel_top = Integer.parseInt( pc.getParameterValue( "inset_label_top", insetLabelString ) );
    final Insets insetsLabel = new Insets( insetLabel_top, insetLabel_left, insetLabel_bottom, insetLabel_right );

    final int tickLength = Integer.parseInt( pc.getParameterValue( "tick_length", "5" ) );
    final int gap = Integer.parseInt( pc.getParameterValue( "gap", "0" ) );

    final int minTickInteval = Integer.parseInt( pc.getParameterValue( "min_tick_interval", "0" ) );
    final int fixedWidth = Integer.parseInt( pc.getParameterValue( "fixed_width", "0" ) );
    final BooleanParser bp = new BooleanParser();
    final boolean hideCut = pc.getParsedParameterValue( "hide_cut", "false", bp );

    final IAxisRenderer<Number> axisRenderer = new GenericAxisRenderer( getAxisRendererType().getId(), tickLength, insetsTick, insetsLabel, 0, new NumberLabelCreator( tick_label_formater ), new GenericNumberTickCalculator(), minTickInteval, hideCut );
    return axisRenderer;
  }

  public Class< ? > getDataClass( )
  {
    return Number.class;
  }

}
