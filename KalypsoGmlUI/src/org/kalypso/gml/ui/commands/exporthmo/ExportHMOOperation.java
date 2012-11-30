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
package org.kalypso.gml.ui.commands.exporthmo;

import java.io.File;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.ogc.gml.serialize.Gml2HmoConverter;
import org.kalypso.ogc.gml.serialize.GmlTriSurface2HmoConverter;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.geometry.GM_TriangulatedSurface;

/**
 * @author Gernot Belger
 */
public class ExportHMOOperation implements ICoreRunnableWithProgress
{
  private final ExportHMOData m_data;

  public ExportHMOOperation( final ExportHMOData data )
  {
    m_data = data;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    try
    {
      final File exportFile = m_data.getExportFile();

      final Feature[] features = m_data.getFeatures();
      // FIXME: why only the first?
      final Feature feature = features[0];
      // FIXME: is the tin always the default property?
      final GM_TriangulatedSurface geometryProperty = (GM_TriangulatedSurface)feature.getDefaultGeometryPropertyValue();
      final Gml2HmoConverter converter = new GmlTriSurface2HmoConverter( geometryProperty );

      converter.writeHmo( exportFile, monitor );
      return Status.OK_STATUS;
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), Messages.getString( "ExportHMOOperation_0" ), e ); //$NON-NLS-1$
    }
  }
}