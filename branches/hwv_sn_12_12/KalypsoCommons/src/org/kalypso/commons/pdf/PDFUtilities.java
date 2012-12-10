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
package org.kalypso.commons.pdf;

import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import javax.swing.SwingUtilities;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.KalypsoCommonsPlugin;

import com.sun.pdfview.PDFFile;
import com.sun.pdfview.PDFPrintPage;

/**
 * This class provides functions for dealing with pdf files.
 * 
 * @author Holger Albert
 */
public class PDFUtilities
{
  private static final int DEFAULT_USER_SPACE_UNIT_DPI = 72;

  private static final float MM_TO_UNITS = 1 / (10 * 2.54f) * DEFAULT_USER_SPACE_UNIT_DPI;

  public static final float PAGE_SIZE_A4_WIDTH = 210 * MM_TO_UNITS;

  public static final float PAGE_SIZE_A4_HEIGHT = 297 * MM_TO_UNITS;

  public static final float MAX_PDF_PAGE_WITDH = 5080 * MM_TO_UNITS;

  /**
   * The constructor.
   */
  private PDFUtilities( )
  {
  }

  /**
   * This function prints the a pdf document.
   * 
   * @param file
   *          The file of the pdf document.
   * @param pageFormat
   *          The page format. See {@link PageFormat#LANDSCAPE} or {@link PageFormat#PORTRAIT};
   */
  public static void print( final File file, final int pageFormat )
  {
    SwingUtilities.invokeLater( new Runnable()
    {
      /**
       * @see java.lang.Runnable#run()
       */
      @Override
      public void run( )
      {
        try
        {
          /* Create a random access file. */
          RandomAccessFile raf = new RandomAccessFile( file, "r" );
          FileChannel channel = raf.getChannel();
          ByteBuffer buf = channel.map( FileChannel.MapMode.READ_ONLY, 0, channel.size() );

          /* Create the pdf file. */
          PDFFile pdfFile = new PDFFile( buf );

          /* This is used for printing the pdf file. */
          PDFPrintPage pages = new PDFPrintPage( pdfFile );

          /* Create print job. */
          PrinterJob pjob = PrinterJob.getPrinterJob();
          pjob.setJobName( file.getName() );

          /* Get the page format. */
          PageFormat pf = pjob.defaultPage();
          pf.setPaper( createPaper( PAGE_SIZE_A4_WIDTH, PAGE_SIZE_A4_HEIGHT ) );
          pf.setOrientation( pageFormat );

          /* Create a new book. */
          Book book = new Book();
          book.append( pages, pf, pdfFile.getNumPages() );
          pjob.setPageable( book );

          /* Send print job to default printer. */
          if( pjob.printDialog() )
            pjob.print();

          /* Close the random access file. */
          raf.close();
        }
        catch( Exception ex )
        {
          /* Log the error message. */
          KalypsoCommonsPlugin.getDefault().getLog().log( new Status( IStatus.ERROR, KalypsoCommonsPlugin.getID(), ex.getLocalizedMessage(), ex ) );
        }
      }
    } );
  }

  /**
   * This function creates a paper.
   * 
   * @param width
   *          The witdh of the paper. See {@link PDFUtilities#PAGE_SIZE_A4_WIDTH}
   * @param height
   *          The height of the paper. See {@link PDFUtilities#PAGE_SIZE_A4_HEIGHT}
   * @return The paper.
   */
  protected static Paper createPaper( float width, float height )
  {
    Paper paper = new Paper();
    paper.setSize( width, height );
    paper.setImageableArea( 0, 0, paper.getWidth(), paper.getHeight() );

    return paper;
  }
}