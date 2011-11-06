/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and
 
 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de
 
 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ogc.sensor.diagview.jfreechart;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.jfree.chart.encoders.EncoderUtil;
import org.jfree.chart.encoders.ImageEncoderFactory;
import org.jfree.chart.title.TextTitle;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.i18n.Messages;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.ogc.sensor.ExportUtilities;

/**
 * ExportableChart based on an existing chart
 * 
 * @author schlienger
 */
public class ExportableChart implements IExportableObject
{
  public final static String DEFAULT_FORMAT = "png"; //$NON-NLS-1$

  public final static int DEFAULT_WIDTH = 400;

  public final static int DEFAULT_HEIGHT = 300;

  private final ObservationChart m_chart;

  private final String m_format;

  private final int m_width;

  private final int m_height;

  private final String m_identifierPrefix;

  private final String m_category;

  private final String m_stationIDs;

  static
  {
    // WORKAROUND: we use another PNG-encoder, there seem to be a memory leak in the JVM when using
    // the default encoder which is shipped with it.
    // 
    // From a Bugreport-Topic of JFreeChart:
    // -------------------------------------
    // After analysing the source code of javax.imageio.ImageIO and some googeling I found
    // the following bug report on SUN web site. The bug with the id 4513817 was open 11-OCT-2001 and still in status
    // "In progress, bug". One of the authors suggest to use the static method ImageIO.setUseCache(false).
    // Adding ImageIO.setUseCache(false) to my reproducer and executing the test shows good results. I run the test with
    // more the 30000 iteration and couldn't detect any memory grow. Furthermore I did a test run with
    // ImageEncoderFactory.setImageEncoder("png","org.jfree.chart.encoders.KeypointPNGEncoderAdapter"); Also here the
    // bug disappear.
    ImageEncoderFactory.setImageEncoder( "png", "org.jfree.chart.encoders.KeypointPNGEncoderAdapter" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * @param kennzifferIndex
   *          If non- <code>null</code>, use only the observation-item with that index to generate the kennziffer.
   */
  public ExportableChart( final ObservationChart chart, final String format, final int width, final int height, final String identifierPrefix, final String category, final Integer kennzifferIndex )
  {
    m_chart = chart;
    m_format = format;
    m_width = width;
    m_height = height;
    m_identifierPrefix = identifierPrefix;
    m_category = category;
    m_stationIDs = ExportUtilities.extractStationIDs( m_chart.getTemplate().getItems(), kennzifferIndex );
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#getPreferredDocumentName()
   */
  @Override
  public String getPreferredDocumentName( )
  {
    final TextTitle title = m_chart.getTitle();

    String name = Messages.getString( "org.kalypso.ogc.sensor.diagview.jfreechart.ExportableChart.1" ); //$NON-NLS-1$
    if( title != null && title.getText().length() > 0 )
      name = title.getText();

    return FileUtilities.validateName( name + "." + m_format, "_" ); //$NON-NLS-1$ //$NON-NLS-2$
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#exportObject(java.io.OutputStream,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus exportObject( final OutputStream outs, final IProgressMonitor monitor )
  {
    monitor.beginTask( Messages.getString( "org.kalypso.ogc.sensor.diagview.jfreechart.ExportableChart.3" ), IProgressMonitor.UNKNOWN ); //$NON-NLS-1$

    try
    {
      final BufferedImage image = m_chart.createBufferedImage( m_width, m_height, null );
      EncoderUtil.writeBufferedImage( image, m_format, outs );
    }
    catch( final IOException e )
    {
      return StatusUtilities.statusFromThrowable( e, Messages.getString( "org.kalypso.ogc.sensor.diagview.jfreechart.ExportableChart.4" ) ); //$NON-NLS-1$
    }
    finally
    {
      monitor.done();
    }

    return Status.OK_STATUS;
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#getIdentifier()
   */
  @Override
  public String getIdentifier( )
  {
    return m_identifierPrefix + getPreferredDocumentName();
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#getCategory()
   */
  @Override
  public String getCategory( )
  {
    return m_category;
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#getStationIDs()
   */
  @Override
  public String getStationIDs( )
  {
    return m_stationIDs;
  }
}