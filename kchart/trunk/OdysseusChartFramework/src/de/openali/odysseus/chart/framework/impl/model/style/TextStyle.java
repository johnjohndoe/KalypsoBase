package de.openali.odysseus.chart.framework.impl.model.style;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.impl.model.style.StyleConstants.FONTSTYLE;
import de.openali.odysseus.chart.framework.impl.model.style.StyleConstants.FONTWEIGHT;

public class TextStyle extends AbstractStyle
{

	private Font m_font;
	private FONTSTYLE m_style;
	private RGB m_textRGB;
	private String m_family;
	private int m_size;
	private RGB m_fillRGB;
	private Color m_fillColor;
	private Color m_textColor;
	private FONTWEIGHT m_weight;

	public TextStyle(int height, String family, RGB rgbText, RGB rgbFill, FONTSTYLE style, FONTWEIGHT weight, int alpha, boolean isVisible)
	{
		setFamily(family);
		setHeight(height);
		setTextColor(rgbText);
		setFillColor(rgbFill);
		setStyle(style);
		setWeight(weight);
		setAlpha(alpha);
		setVisible(isVisible);
	}

	public void setWeight(FONTWEIGHT weight)
	{
		m_weight = weight;
	}

	public void setFillColor(RGB rgbFill)
	{
		m_fillRGB = rgbFill;
	}

	public void setStyle(FONTSTYLE style)
	{
		m_style = style;
	}

	public void setTextColor(RGB rgbText)
	{
		m_textRGB = rgbText;
	}

	public void setFamily(String family)
	{
		m_family = family;
	}

	public void setHeight(int size)
	{
		m_size = size;
	}

	public void apply(GC gc)
	{
		FontData fd = new FontData(m_family, m_size, m_style.toSWT() | m_weight.toSWT());

		if (m_font != null && !m_font.isDisposed())
		{
			m_font.dispose();
		}
		m_font = new Font(gc.getDevice(), fd);
		gc.setFont(m_font);

		if (m_fillColor != null && !m_fillColor.isDisposed())
		{
			m_fillColor.dispose();
		}
		m_fillColor = new Color(gc.getDevice(), m_fillRGB);
		gc.setBackground(m_fillColor);

		if (m_textColor != null && !m_textColor.isDisposed())
		{
			m_textColor.dispose();
		}
		m_textColor = new Color(gc.getDevice(), m_textRGB);
		gc.setForeground(m_textColor);

	}

	public void dispose()
	{
		if (m_font != null && !m_font.isDisposed())
		{
			m_font.dispose();
		}
		if (m_fillColor != null && !m_fillColor.isDisposed())
		{
			m_fillColor.dispose();
		}
		if (m_textColor != null && !m_textColor.isDisposed())
		{
			m_textColor.dispose();
		}

	}

}
