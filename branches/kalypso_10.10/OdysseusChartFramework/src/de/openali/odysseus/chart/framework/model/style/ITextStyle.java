package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTSTYLE;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTWEIGHT;

public interface ITextStyle extends IStyle
{
  @Override
  ITextStyle copy( );

  String getFamily( );

  RGB getFillColor( );

  FONTSTYLE getFontStyle( );

  int getHeight( );

  RGB getTextColor( );

  FONTWEIGHT getWeight( );

  void setFamily( String family );

  void setFillColor( RGB rgbFill );

  void setFontStyle( FONTSTYLE style );

  void setHeight( int size );

  void setTextColor( RGB rgbText );

  void setWeight( FONTWEIGHT weight );
}