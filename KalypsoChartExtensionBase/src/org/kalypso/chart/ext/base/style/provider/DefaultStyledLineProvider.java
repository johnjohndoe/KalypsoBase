package org.kalypso.chart.ext.base.style.provider;

import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.ext.base.style.StyledLine;
import org.kalypso.chart.factory.configuration.parameters.impl.DoubleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.LineStyleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.RGBParser;
import org.kalypso.chart.factory.provider.AbstractStyledElementProvider;
import org.kalypso.chart.framework.model.styles.IStyledElement;

public class DefaultStyledLineProvider extends AbstractStyledElementProvider
{

  public IStyledElement getStyledElement( )
  {
    final int alpha = getParameterContainer().getParsedParameterValue( "alpha", "255", new DoubleParser() ).intValue();
    final int lineWidth = getParameterContainer().getParsedParameterValue( "lineWidth", "1", new DoubleParser() ).intValue();
    final RGB lineColor = getParameterContainer().getParsedParameterValue( "lineColor", "#000000", new RGBParser() );
    final int lineStyle = getParameterContainer().getParsedParameterValue( "lineStyle", "SOLID", new LineStyleParser() );
    final StyledLine sl = new StyledLine( getStyleType().getId(), lineWidth, lineColor, lineStyle, alpha );
    return sl;
  }
}
