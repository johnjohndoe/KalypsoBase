/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.ui.wizard.gml;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.ui.KalypsoAddLayerPlugin;
import org.kalypso.ui.i18n.Messages;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Gernot Belger
 */
public class GmlFileImportLoadOperation implements ICoreRunnableWithProgress
{
  private final GmlFileImportData m_data;

  public GmlFileImportLoadOperation( final GmlFileImportData data )
  {
    m_data = data;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor )
  {
    final IPath path = m_data.getGmlFile().getPath();
    if( path == null || path.isEmpty() )
      return Status.OK_STATUS;

    final IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile( path );
    if( !file.exists() )
      return Status.OK_STATUS;

    try
    {
      final GMLWorkspace workspace = GmlSerializer.createGMLWorkspace( file, monitor );
      m_data.setWorkspace( workspace );
      return Status.OK_STATUS;
    }
    catch( final Exception e )
    {
      e.printStackTrace();
      return new Status( IStatus.WARNING, KalypsoAddLayerPlugin.getId(), Messages.getString("GmlFileImportLoadOperation_0"), e ); //$NON-NLS-1$
    }
  }
}