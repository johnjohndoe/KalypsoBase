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

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.IOUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.mapmodel.MapModellHelper;
import org.kalypso.ui.KalypsoGisPlugin;

import com.itextpdf.text.Document;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;

/**
 * Exports a map to a PDF.
 * 
 * @author Holger Albert
 */
public class PDFExporter
{
  /**
   * The map panel.
   */
  private IMapPanel m_mapPanel;

  /**
   * The constructor.
   * 
   * @param mapPanel
   *          The map panel.
   */
  public PDFExporter( IMapPanel mapPanel )
  {
    m_mapPanel = mapPanel;
  }

  public IStatus doExport( File targetFile, IProgressMonitor monitor )
  {
    /* If no monitor is given, take a null progress monitor. */
    if( monitor == null )
      monitor = new NullProgressMonitor();

    /* The output streams. */
    BufferedOutputStream os = null;

    try
    {
      /* Monitor. */
      monitor.beginTask( "Exportiere PDF...", 1000 );
      monitor.subTask( "Erzeuge Inhalt..." );

      /* Create the image. */
      BufferedImage image = MapModellHelper.createWellFormedImageFromModel( m_mapPanel, (int) PageSize.A4.getHeight(), (int) PageSize.A4.getWidth() );

      /* Convert to an itext image. */
      Image img = Image.getInstance( image, null );

      /* Monitor. */
      monitor.subTask( "Erzeuge PDF..." );

      /* Create the output stream. */
      os = new BufferedOutputStream( new FileOutputStream( targetFile ) );

      /* Create a new document. */
      Document document = new Document( new com.itextpdf.text.Rectangle( PageSize.A4.getHeight(), PageSize.A4.getWidth() ), 30, 30, 30, 30 );

      /* Create the pdf writter. */
      PdfWriter writer = PdfWriter.getInstance( document, os );
      writer.setCompressionLevel( 0 );

      /* Open the document. */
      document.open();

      /* Set the position. */
      img.setAbsolutePosition( 0, 0 );

      /* Set to the pdf. */
      writer.getDirectContent().addImage( img, true );

      /* Close the document. */
      document.close();

      /* Monitor. */
      monitor.worked( 500 );

      return new Status( IStatus.OK, KalypsoGisPlugin.getId(), "OK" );
    }
    catch( Exception ex )
    {
      return new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), ex.getLocalizedMessage(), ex );
    }
    finally
    {
      /* Close the output streams. */
      IOUtils.closeQuietly( os );

      /* Monitor. */
      monitor.done();
    }
  }
}