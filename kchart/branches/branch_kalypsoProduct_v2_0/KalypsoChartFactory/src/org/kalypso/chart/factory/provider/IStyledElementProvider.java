package org.kalypso.chart.factory.provider;

import org.kalypso.chart.factory.configuration.exception.StyledElementProviderException;
import org.kalypso.chart.framework.model.styles.IStyledElement;
import org.ksp.chart.factory.StyleType;

/**
 * @author burtscher A LayerProvider is needed to create layers from configuration data. Theres no 1:1 mapping from data
 *         soureces to layers, as 1.) several data sources can be merged to generate layer data and 2.) one data source
 *         can be used to create several layers. The LayerProvider is used to fetch, filter and analyze data and to
 *         provide layers according to the datas needs.
 */
public interface IStyledElementProvider
{
  /**
   * @return axis created by the AxisProvider
   */
  public IStyledElement getStyledElement( ) throws StyledElementProviderException;

  public void init( final StyleType st );

  /**
   * returns XML configuration element for the given chart element
   */
  public StyleType getXMLType( IStyledElement styledElement );

}
