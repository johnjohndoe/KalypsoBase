package org.kalypso.chart.framework.model.styles;

import org.kalypso.chart.framework.model.styles.IStyleConstants.SE_TYPE;

/**
 * @author burtscher Container for IStyledElements
 */
public interface ILayerStyle
{
  /**
   * @return IStyledElement with given type on given position pos; if there's no element at the designated position, the
   *         last element by the favoured type is returned. If there's no element by the designated type is available,
   *         null is returned;
   *         <p>
   *         TODO: is position by in really necessary? how is is known which pos'es are valid?
   */
  public IStyledElement getElement( SE_TYPE type, int pos );

  /**
   * returns all StyledElements the Style contains
   */
  public IStyledElement[] getElements( );

}
