package de.openali.odysseus.chart.framework.model.style;

public interface IStylable
{

  /**
   * sets the layer styles
   */
  public void setStyles( final IStyleSet styles );

  /**
   * @return the layer styles
   */
  public IStyleSet getStyles( );

  public String getId( );

}
