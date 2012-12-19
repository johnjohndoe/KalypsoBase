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
package org.kalypso.ogc.gml.outline.handler;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Device;
import org.eclipse.swt.graphics.Point;
import org.kalypso.commons.java.io.FileUtilities;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypso.ogc.gml.outline.nodes.LegendExporter;

/**
 * @author Gernot Belger
 */
public class ExportLegendOperation implements ICoreRunnableWithProgress, Runnable
{
  private final ExportLegendData m_data;

  private final Device m_display;

  private IStatus m_result;

  public ExportLegendOperation( final ExportLegendData data, final Device display )
  {
    m_data = data;
    m_display = display;
  }

  @Override
  public void run( )
  {
    m_result = execute( new NullProgressMonitor() );
  }

  public IStatus getResult( )
  {
    return m_result;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    /* Now save it to a file. */
    final File legendFile = m_data.getExportFile();
    final IThemeNode[] nodes = m_data.getNodes();
    final String suffix = FileUtilities.getSuffix( legendFile );

    int format = SWT.IMAGE_PNG;
    if( "PNG".equals( suffix ) ) //$NON-NLS-1$
      format = SWT.IMAGE_PNG;
    else if( "JPG".equals( suffix ) ) //$NON-NLS-1$
      format = SWT.IMAGE_JPEG;
    else if( "GIF".equals( suffix ) ) //$NON-NLS-1$
      format = SWT.IMAGE_GIF;

    /* Export the legends. */
    final LegendExporter legendExporter = new LegendExporter();
    final Point autoSize = new Point( -1, -1 );
    return legendExporter.exportLegends( nodes, legendFile, format, m_display, null, autoSize, true, monitor );
  }
}