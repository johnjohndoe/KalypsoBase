package de.openali.odysseus.chart.framework.impl.model.style;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.style.IMarker;
import de.openali.odysseus.chart.framework.model.style.ISWTable;

public interface StyleConstants
{

	public RGB DEFAULT_RGB_FOREGROUND = new RGB(0, 0, 0);

	public RGB DEFAULT_RGB_BACKGROUND = new RGB(122, 122, 255);

	public RGB DEFAULT_RGB_TEXT = new RGB(0, 0, 0);

	public RGB DEFAULT_RGB_TEXT_BACKGROUND = new RGB(255, 255, 255);

	public int DEFAULT_STROKE_WIDTH = 2;

	public IMarker DEFAULT_MARKER = new OvalMarker();

	public int DEFAULT_ALPHA = 255;

	public int DEFAULT_WIDTH = 10;

	public int DEFAULT_HEIGHT = 10;

	public int DEFAULT_FONT_HEIGHT = 10;

	public FONTSTYLE DEFAULT_FONT_STYLE = FONTSTYLE.NORMAL;

	public FONTWEIGHT DEFAULT_FONT_WEIGHT = FONTWEIGHT.NORMAL;

	public String DEFAULT_FONT_FAMILY = "Arial";

	public boolean DEFAULT_VISIBILITY = true;

	public int DEFAULT_DASHOFFSET = 0;

	public int[] DEFAULT_DASHARRAY = new int[0];

	public LINECAP DEFAULT_LINECAP = LINECAP.ROUND;

	public LINEJOIN DEFAULT_LINEJOIN = LINEJOIN.MITER;

	public int DEFAULT_MITERLIMIT = 1;

	public enum LINECAP implements ISWTable
	{
		FLAT(SWT.CAP_FLAT), ROUND(SWT.CAP_ROUND), SQUARE(SWT.CAP_SQUARE);
		private final int m_swtValue;

		private LINECAP(final int swtValue)
		{
			m_swtValue = swtValue;
		}

		public int toSWT()
		{
			return m_swtValue;
		}
	};

	public enum LINEJOIN implements ISWTable
	{
		MITER(SWT.JOIN_MITER), ROUND(SWT.JOIN_ROUND), BEVEL(SWT.JOIN_BEVEL);

		private final int m_swtValue;

		private LINEJOIN(final int swtValue)
		{
			m_swtValue = swtValue;
		}

		public int toSWT()
		{
			return m_swtValue;
		}
	}

	public enum GEOMETRY
	{
		POINT, LINE, AREA
	}

	public enum FONTSTYLE implements ISWTable
	{
		NORMAL(SWT.NORMAL), ITALIC(SWT.ITALIC);

		private final int m_swtValue;

		private FONTSTYLE(final int swtValue)
		{
			m_swtValue = swtValue;
		}

		public int toSWT()
		{
			return m_swtValue;
		}
	}

	public enum FONTWEIGHT implements ISWTable
	{
		NORMAL(SWT.NORMAL), BOLD(SWT.BOLD);

		private final int m_swtValue;

		private FONTWEIGHT(final int swtValue)
		{
			m_swtValue = swtValue;
		}

		public int toSWT()
		{
			return m_swtValue;
		}
	}

}
