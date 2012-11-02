/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraﬂe 22
 *  21073 Hamburg, Germany
 *  http://www.tuhh.de/wb
 * 
 *  and
 *  
 *  Bjoernsen Consulting Engineers (BCE)
 *  Maria Trost 3
 *  56070 Koblenz, Germany
 *  http://www.bjoernsen.de
 * 
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 * 
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 * 
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 *  Contact:
 * 
 *  E-Mail:
 *  belger@bjoernsen.de
 *  schlienger@bjoernsen.de
 *  v.doemming@tuhh.de
 *   
 *  ---------------------------------------------------------------------------*/
package org.kalypso.ogc.gml.map.handlers.utils;

import java.awt.Insets;
import java.awt.image.BufferedImage;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.ImageData;
import org.kalypso.contribs.eclipse.swt.udig.AWTSWTImageUtils;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.mapmodel.MapModellHelper;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * Exports a map to the clipboard.
 * 
 * @author Holger Albert
 */
public class ImageExporter
{
  /**
   * The map panel.
   */
  private final IMapPanel m_mapPanel;

  /**
   * The image data.
   */
  private ImageData m_imageData;

  /**
   * The constructor.
   * 
   * @param mapPanel
   *          The map panel.
   */
  public ImageExporter( final IMapPanel mapPanel )
  {
    m_mapPanel = mapPanel;
    m_imageData = null;
  }

  public IStatus doExport( IProgressMonitor monitor )
  {
    /* If no monitor is given, take a null progress monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    try
    {
      /* Monitor. */
      monitor.beginTask( Messages.getString( "ImageExporter_0" ), 1000 ); //$NON-NLS-1$
      monitor.subTask( Messages.getString( "ImageExporter_1" ) ); //$NON-NLS-1$

      /* Create the image. */
      final BufferedImage image = MapModellHelper.createWellFormedImageFromModel( m_mapPanel, m_mapPanel.getWidth(), m_mapPanel.getHeight(), new Insets( 10, 10, 10, 10 ), 1 );

      /* Monitor. */
      monitor.worked( 500 );
      monitor.subTask( Messages.getString( "ImageExporter_2" ) ); //$NON-NLS-1$

      /* Convert to a SWT image data. */
      final ImageData imageData = AWTSWTImageUtils.createImageData( image );

      /* Store the result. */
      m_imageData = imageData;

      /* Monitor. */
      monitor.worked( 500 );

      return new Status( IStatus.OK, KalypsoGisPlugin.getId(), Messages.getString( "ImageExporter_3" ) ); //$NON-NLS-1$
    }
    catch( final Exception ex )
    {
      return new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), ex.getLocalizedMessage(), ex );
    }
    finally
    {
      /* Monitor. */
      monitor.done();
    }
  }

  /**
   * This function returns the image data.
   * 
   * @return The image data or null.
   */
  public ImageData getImageData( )
  {
    return m_imageData;
  }
}