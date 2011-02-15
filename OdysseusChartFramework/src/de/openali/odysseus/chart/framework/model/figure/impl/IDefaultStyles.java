package de.openali.odysseus.chart.framework.model.figure.impl;

import org.eclipse.swt.graphics.RGB;

import de.openali.odysseus.chart.framework.model.mapper.IAxisConstants.ALIGNMENT;
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
  RGB DEFAULT_RGB_FOREGROUND = new RGB( 0, 0, 0 );

  RGB DEFAULT_RGB_BACKGROUND = new RGB( 122, 122, 255 );

  RGB DEFAULT_RGB_TEXT = new RGB( 0, 0, 0 );

  RGB DEFAULT_RGB_TEXT_BACKGROUND = new RGB( 255, 255, 255 );

  int DEFAULT_STROKE_WIDTH = 2;

  IMarker DEFAULT_MARKER = new OvalMarker();

  IFill DEFAULT_FILL = new ColorFill( DEFAULT_RGB_BACKGROUND );

  int DEFAULT_ALPHA = 255;

  int DEFAULT_WIDTH = 10;

  int DEFAULT_HEIGHT = 10;

  int DEFAULT_FONT_HEIGHT = 10;

  FONTSTYLE DEFAULT_FONT_STYLE = FONTSTYLE.NORMAL;

  FONTWEIGHT DEFAULT_FONT_WEIGHT = FONTWEIGHT.NORMAL;

  String DEFAULT_FONT_FAMILY = "Arial";

  boolean DEFAULT_VISIBILITY = true;

  int DEFAULT_DASHOFFSET = 0;

  float[] DEFAULT_DASHARRAY = new float[0];

  LINECAP DEFAULT_LINECAP = LINECAP.ROUND;

  LINEJOIN DEFAULT_LINEJOIN = LINEJOIN.MITER;

  int DEFAULT_MITERLIMIT = 1;

  boolean DEFAULT_FILL_VISIBILITY = true;

  ALIGNMENT DEFAULT_TEXT_ALIGNMENT = ALIGNMENT.LEFT;
}
