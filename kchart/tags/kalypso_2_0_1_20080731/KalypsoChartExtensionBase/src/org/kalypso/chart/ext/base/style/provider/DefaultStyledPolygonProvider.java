package org.kalypso.chart.ext.base.style.provider;

import org.eclipse.swt.graphics.RGB;
import org.kalypso.chart.ext.base.style.StyledPolygon;
import org.kalypso.chart.factory.configuration.exception.StyledElementProviderException;
import org.kalypso.chart.factory.configuration.parameters.impl.DoubleParser;
import org.kalypso.chart.factory.configuration.parameters.impl.RGBParser;
import org.kalypso.chart.factory.provider.AbstractStyledElementProvider;
import org.kalypso.chart.framework.model.styles.IStyledElement;

public class DefaultStyledPolygonProvider extends AbstractStyledElementProvider
{

  public IStyledElement getStyledElement( ) throws StyledElementProviderException
  {

    final int borderWidth = getParameterContainer().getParsedParameterValue( "borderWidth", "0", new DoubleParser() ).intValue();
    final RGB borderColor = getParameterContainer().getParsedParameterValue( "borderColor", "#000000", new RGBParser() );
    final RGB fillColor = getParameterContainer().getParsedParameterValue( "fillColor", "#ffffff", new RGBParser() );
    final int alpha = getParameterContainer().getParsedParameterValue( "alpha", "255", new DoubleParser() ).intValue();
    final StyledPolygon sp = new StyledPolygon( getStyleType().getId(), fillColor, borderWidth, borderColor, alpha );

    return sp;
  }

}
