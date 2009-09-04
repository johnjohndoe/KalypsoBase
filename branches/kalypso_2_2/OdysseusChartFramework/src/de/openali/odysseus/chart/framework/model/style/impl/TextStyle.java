package de.openali.odysseus.chart.framework.model.style.impl;

import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.OdysseusChartFrameworkPlugin;
import de.openali.odysseus.chart.framework.model.style.ITextStyle;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTSTYLE;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTWEIGHT;

public class TextStyle extends AbstractStyle implements ITextStyle
{

  private FONTSTYLE m_style;

  private RGB m_textRGB;

  private String m_family;

  private int m_size;

  private RGB m_fillRGB;

  private FONTWEIGHT m_weight;

  public TextStyle( int height, String family, RGB rgbText, RGB rgbFill, FONTSTYLE style, FONTWEIGHT weight, int alpha, boolean isVisible )
  {
    setFamily( family );
    setHeight( height );
    setTextColor( rgbText );
    setFillColor( rgbFill );
    setFontStyle( style );
    setWeight( weight );
    setAlpha( alpha );
    setVisible( isVisible );
  }

  /*
   * (non-Javadoc)
   * @see
   * de.openali.odysseus.chart.framework.impl.model.style.ITextStyle#setWeight(de.openali.odysseus.chart.framework.model
   * .style.IStyleConstants.FONTWEIGHT)
   */
  public void setWeight( FONTWEIGHT weight )
  {
    m_weight = weight;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.ITextStyle#setFillColor(org.eclipse.swt.graphics.RGB)
   */
  public void setFillColor( RGB rgbFill )
  {
    m_fillRGB = rgbFill;
  }

  /*
   * (non-Javadoc)
   * @see
   * de.openali.odysseus.chart.framework.impl.model.style.ITextStyle#setStyle(de.openali.odysseus.chart.framework.model
   * .style.IStyleConstants.FONTSTYLE)
   */
  public void setFontStyle( FONTSTYLE style )
  {
    m_style = style;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.ITextStyle#setTextColor(org.eclipse.swt.graphics.RGB)
   */
  public void setTextColor( RGB rgbText )
  {
    m_textRGB = rgbText;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.ITextStyle#setFamily(java.lang.String)
   */
  public void setFamily( String family )
  {
    m_family = family;
  }

  /*
   * (non-Javadoc)
   * @see de.openali.odysseus.chart.framework.impl.model.style.ITextStyle#setHeight(int)
   */
  public void setHeight( int size )
  {
    m_size = size;
  }

  public void apply( GC gc )
  {
    FontData fd = new FontData( m_family, m_size, m_style.toSWT() | m_weight.toSWT() );

    Font font = OdysseusChartFrameworkPlugin.getDefault().getFontRegistry().getResource( gc.getDevice(), fd );

    gc.setFont( font );

    gc.setBackground( OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( gc.getDevice(), m_fillRGB ) );

    gc.setForeground( OdysseusChartFrameworkPlugin.getDefault().getColorRegistry().getResource( gc.getDevice(), m_textRGB ) );

  }

  @Deprecated
  public void dispose( )
  {
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.style.ITextStyle#copy()
   */
  public ITextStyle copy( )
  {
    return new TextStyle( getHeight(), getFamily(), getTextColor(), getFillColor(), getFontStyle(), getWeight(), getAlpha(), isVisible() );
  }

  public FONTWEIGHT getWeight( )
  {
    return m_weight;
  }

  public FONTSTYLE getFontStyle( )
  {
    return m_style;
  }

  public RGB getFillColor( )
  {
    return m_fillRGB;
  }

  public RGB getTextColor( )
  {
    return m_textRGB;
  }

  public String getFamily( )
  {
    return m_family;
  }

  public int getHeight( )
  {
    return m_size;
  }

}
