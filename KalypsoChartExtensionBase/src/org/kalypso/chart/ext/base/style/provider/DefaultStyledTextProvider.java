package org.kalypso.chart.ext.base.style.provider;

import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.ext.base.style.StyledText;
import org.kalypso.chart.factory.configuration.exception.StyledElementProviderException;
import org.kalypso.chart.factory.configuration.parameters.impl.DoubleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.FontStyleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.RGBParser;
import org.kalypso.chart.factory.provider.AbstractStyledElementProvider;
import org.kalypso.chart.framework.model.styles.IStyledElement;

public class DefaultStyledTextProvider extends AbstractStyledElementProvider
{

  public IStyledElement getStyledElement( ) throws StyledElementProviderException
  {

    final String fontName = getParameterContainer().getParameterValue( "fontName", "arial" );
    final int fontStyle = getParameterContainer().getParsedParameterValue( "fontName", "NORMAL", new FontStyleParser() );
    final int fontSize = getParameterContainer().getParsedParameterValue( "fontSize", "10", new DoubleParser() ).intValue();
    final RGB foregroundColor = getParameterContainer().getParsedParameterValue( "textColor", "#ffffff", new RGBParser() );
    final RGB backgroundColor = getParameterContainer().getParsedParameterValue( "backgroundColor", "#000000", new RGBParser() );
    final int alpha = getParameterContainer().getParsedParameterValue( "alpha", "255", new DoubleParser() ).intValue();
    final StyledText st = new StyledText( getStyleType().getId(), foregroundColor, backgroundColor, fontName, fontStyle, fontSize, alpha );
    return st;
  }

}
