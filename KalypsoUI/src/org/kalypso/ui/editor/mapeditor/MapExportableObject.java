/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wbprivate ExportMapOptionsPage

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
package org.kalypso.ui.editor.mapeditor;

import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ogc.gml.mapmodel.MapModellHelper;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * The map exportable object.
 * 
 * @author Gernot Belger
 * @author Holger Albert
 */
public class MapExportableObject implements IExportableObject
{
  /**
   * The map panel.
   */
  private final IMapPanel m_panel;

  /**
   * The preferred document name.
   */
  private final String m_preferredDocumentName;

  /**
   * The width of the new image.
   */
  private final int m_width;

  /**
   * The height of the new image.
   */
  private final int m_height;

  /**
   * The insets of the image define a print border, which is kept empty.
   */
  private final Insets m_insets;

  /**
   * If >0 and <=25 a border will be drawn around the map.
   */
  private final int m_borderWidth;

  /**
   * The format of the image.
   */
  private final String m_format;

  /**
   * The constructor.
   * 
   * @param panel
   *          The map panel.
   * @param preferredDocumentName
   *          The preferred document name.
   * @param width
   *          The width of the new image.
   * @param height
   *          The height of the new image.
   * @param insets
   *          The insets of the image define a print border, which is kept empty.
   * @param borderWidth
   *          If >0 and <=25 a border will be drawn around the map.
   * @param format
   *          The format of the image.
   */
  public MapExportableObject( final IMapPanel panel, final String preferredDocumentName, final int width, final int height, final Insets insets, final int borderWidth, final String format )
  {
    m_panel = panel;
    m_preferredDocumentName = preferredDocumentName;
    m_width = width;
    m_height = height;
    m_insets = insets;
    m_borderWidth = borderWidth;
    m_format = format;
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#getPreferredDocumentName()
   */
  @Override
  public String getPreferredDocumentName( )
  {
    final String baseName = FilenameUtils.removeExtension( m_preferredDocumentName );
    final String fileName = String.format( "%s.%s", baseName, m_format ); //$NON-NLS-1$

    return FileUtilities.validateName( fileName, "_" ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#exportObject(java.io.OutputStream, org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public IStatus exportObject( final OutputStream output, final IProgressMonitor monitor )
  {
    try
    {
      monitor.beginTask( Messages.getString( "org.kalypso.ui.editor.mapeditor.ExportableMap.1" ), 1000 ); //$NON-NLS-1$

      final BufferedImage image = MapModellHelper.createWellFormedImageFromModel( m_panel, m_width, m_height, m_insets, m_borderWidth );

      final boolean result = ImageIO.write( image, m_format, output );
      if( !result )
        return new Status( IStatus.WARNING, KalypsoGisPlugin.getId(), 0, Messages.getString( "org.kalypso.ui.editor.mapeditor.ExportableMap.2" ) + m_format, null ); //$NON-NLS-1$
    }
    catch( final IOException e )
    {
      e.printStackTrace();
      return new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), 0, Messages.getString( "org.kalypso.ui.editor.mapeditor.ExportableMap.3" ), e ); //$NON-NLS-1$
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
    // TODO bessere Id?
    return getPreferredDocumentName();
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#getCategory()
   */
  @Override
  public String getCategory( )
  {
    // TODO bessere category
    return Messages.getString( "org.kalypso.ui.editor.mapeditor.ExportableMap.4" ); //$NON-NLS-1$
  }

  /**
   * @see org.kalypso.metadoc.IExportableObject#getStationIDs()
   */
  @Override
  public String getStationIDs( )
  {
    return ""; //$NON-NLS-1$
  }
}