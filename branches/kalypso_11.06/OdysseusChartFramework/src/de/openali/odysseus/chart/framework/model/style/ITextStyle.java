package de.openali.odysseus.chart.framework.model.style;

import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTSTYLE;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTWEIGHT;

public interface ITextStyle extends IStyle
{
  @Override
  ITextStyle clone( );

  String getFamily( );

  RGB getFillColor( );

  // FIXME: bad: not in xml, used for special situations, does not belong here
  ALIGNMENT getAlignment( );

  // FIXME: bad: not in xml, used for special situations, does not belong here
  void setAlignment( final ALIGNMENT position );

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

  FontData toFontData( );
}