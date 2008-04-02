package org.kalypso.chart.framework.model.legend;

import org.eclipse.swt.graphics.ImageData;

public interface ILegendItem
{

  public abstract ILegendItem[] getChildren( );

  public abstract void addChild( ILegendItem l );

  public abstract ILegendItem getParent( );

  public abstract String getLabel( );

  public abstract ImageData getImage( );

  public void setParent( ILegendItem parent );

}