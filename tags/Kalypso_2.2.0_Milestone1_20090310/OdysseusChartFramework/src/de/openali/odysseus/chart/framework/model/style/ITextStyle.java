package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTSTYLE;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTWEIGHT;

public interface ITextStyle extends IStyle
{

  public void setWeight( FONTWEIGHT weight );

  public void setFillColor( RGB rgbFill );

  public void setFontStyle( FONTSTYLE style );

  public void setTextColor( RGB rgbText );

  public void setFamily( String family );

  public void setHeight( int size );

  public ITextStyle copy( );

  public FONTWEIGHT getWeight( );

  public FONTSTYLE getFontStyle( );

  public RGB getFillColor( );

  public RGB getTextColor( );

  public String getFamily( );

  public int getHeight( );

}