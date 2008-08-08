package de.openali.odysseus.chart.framework.model.figure.impl;

import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.style.IFill;
import de.openali.odysseus.chart.framework.model.style.IMarker;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTSTYLE;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.FONTWEIGHT;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINECAP;
import de.openali.odysseus.chart.framework.model.style.IStyleConstants.LINEJOIN;
import de.openali.odysseus.chart.framework.model.style.impl.ColorFill;
import de.openali.odysseus.chart.framework.model.style.impl.OvalMarker;

public interface IDefaultStyles
{
  public RGB DEFAULT_RGB_FOREGROUND = new RGB( 0, 0, 0 );

  public RGB DEFAULT_RGB_BACKGROUND = new RGB( 122, 122, 255 );

  public RGB DEFAULT_RGB_TEXT = new RGB( 0, 0, 0 );

  public RGB DEFAULT_RGB_TEXT_BACKGROUND = new RGB( 255, 255, 255 );

  public int DEFAULT_STROKE_WIDTH = 2;

  public IMarker DEFAULT_MARKER = new OvalMarker();

  public IFill DEFAULT_FILL = new ColorFill( DEFAULT_RGB_BACKGROUND );

  public int DEFAULT_ALPHA = 255;

  public int DEFAULT_WIDTH = 10;

  public int DEFAULT_HEIGHT = 10;

  public int DEFAULT_FONT_HEIGHT = 10;

  public FONTSTYLE DEFAULT_FONT_STYLE = FONTSTYLE.NORMAL;

  public FONTWEIGHT DEFAULT_FONT_WEIGHT = FONTWEIGHT.NORMAL;

  public String DEFAULT_FONT_FAMILY = "Arial";

  public boolean DEFAULT_VISIBILITY = true;

  public int DEFAULT_DASHOFFSET = 0;

  public float[] DEFAULT_DASHARRAY = new float[0];

  public LINECAP DEFAULT_LINECAP = LINECAP.ROUND;

  public LINEJOIN DEFAULT_LINEJOIN = LINEJOIN.MITER;

  public int DEFAULT_MITERLIMIT = 1;

  public boolean DEFAULT_FILL_VISIBILITY = true;
}
