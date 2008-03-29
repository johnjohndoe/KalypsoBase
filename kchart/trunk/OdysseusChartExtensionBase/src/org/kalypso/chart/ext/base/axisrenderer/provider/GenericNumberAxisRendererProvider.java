package org.kalypso.chart.ext.base.axisrenderer.provider;

import java.awt.Insets;

import org.kalypso.chart.ext.base.axisrenderer.GenericAxisRenderer;
import org.kalypso.chart.ext.base.axisrenderer.GenericNumberTickCalculator;
import org.kalypso.chart.ext.base.axisrenderer.NumberLabelCreator;

import de.openali.odysseus.chart.factory.config.parameters.IParameterContainer;
import de.openali.odysseus.chart.factory.config.parameters.impl.BooleanParser;
import de.openali.odysseus.chart.framework.model.mapper.renderer.IAxisRenderer;

public class GenericNumberAxisRendererProvider extends AbstractGenericAxisRendererProvider
{

  @Override
  public IAxisRenderer getAxisRenderer( )
  {
    final IParameterContainer pc = getParameterContainer();

    final String tick_label_formater = pc.getParameterValue( "tick_label_formater", "%s" );

    final IAxisRenderer axisRenderer = new GenericAxisRenderer( getId(), getTickLength(), getTickInsets(), getLabelInsets(), getGap(), new NumberLabelCreator( tick_label_formater ), new GenericNumberTickCalculator(), getMinTickInteval(), getHideCut(), getFixedWidth() );
    return axisRenderer;
  }

  private int getFixedWidth( )
  {
    return Integer.parseInt( getParameterContainer().getParameterValue( "fixed_width", "0" ) );
  }

  private boolean getHideCut( )
  {
    final BooleanParser bp = new BooleanParser();
    return getParameterContainer().getParsedParameterValue( "hide_cut", "false", bp );
  }

  private Number getMinTickInteval( )
  {
    return Integer.parseInt( getParameterContainer().getParameterValue( "min_tick_interval", "0" ) );
  }

  private Insets getLabelInsets( )
  {
    IParameterContainer pc = getParameterContainer();
    final int insetLabel = Integer.parseInt( pc.getParameterValue( "inset_label", "1" ) );
    final String insetLabelString = (new Integer( insetLabel )).toString();
    final int insetLabel_left = Integer.parseInt( pc.getParameterValue( "inset_label_left", insetLabelString ) );
    final int insetLabel_right = Integer.parseInt( pc.getParameterValue( "inset_label_right", insetLabelString ) );
    final int insetLabel_bottom = Integer.parseInt( pc.getParameterValue( "inset_label_bottom", insetLabelString ) );
    final int insetLabel_top = Integer.parseInt( pc.getParameterValue( "inset_label_top", insetLabelString ) );
    return new Insets( insetLabel_top, insetLabel_left, insetLabel_bottom, insetLabel_right );
  }

  private int getGap( )
  {
    return Integer.parseInt( getParameterContainer().getParameterValue( "gap", "0" ) );
  }

  private Insets getTickInsets( )
  {
    // Ticks setzen; inset_tick definiert Abstände für alle Seiten; die können dann für jede Seite einzeln überschrieben
    // werden
    IParameterContainer pc = getParameterContainer();
    final int insetTick = Integer.parseInt( pc.getParameterValue( "inset_tick", "1" ) );
    final String insetTickString = (new Integer( insetTick )).toString();
    final int insetTick_left = Integer.parseInt( pc.getParameterValue( "inset_tick_left", insetTickString ) );
    final int insetTick_right = Integer.parseInt( pc.getParameterValue( "inset_tick_right", insetTickString ) );
    final int insetTick_bottom = Integer.parseInt( pc.getParameterValue( "inset_tick_bottom", insetTickString ) );
    final int insetTick_top = Integer.parseInt( pc.getParameterValue( "inset_tick_top", insetTickString ) );
    return new Insets( insetTick_top, insetTick_left, insetTick_bottom, insetTick_right );
  }

  private int getTickLength( )
  {
    return Integer.parseInt( getParameterContainer().getParameterValue( "tick_length", "5" ) );
  }

}
