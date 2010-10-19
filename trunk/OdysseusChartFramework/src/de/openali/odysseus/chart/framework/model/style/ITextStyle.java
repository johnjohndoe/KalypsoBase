package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTSTYLE;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTWEIGHT;

public interface ITextStyle extends IStyle
{
  void setWeight( FONTWEIGHT weight );

  void setFillColor( RGB rgbFill );

  void setFontStyle( FONTSTYLE style );

  void setTextColor( RGB rgbText );

  void setFamily( String family );

  void setHeight( int size );

  @Override
  ITextStyle copy( );

  FONTWEIGHT getWeight( );

  FONTSTYLE getFontStyle( );

  RGB getFillColor( );

  RGB getTextColor( );

  String getFamily( );

  int getHeight( );
}