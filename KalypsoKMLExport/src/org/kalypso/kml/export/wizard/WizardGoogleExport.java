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
package org.kalypso.kml.export.wizard;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.wizard.Wizard;
import org.kalypso.contribs.eclipse.jface.operation.RunnableContextHelper;
import org.kalypso.kml.export.constants.IKMLExportSettings;
import org.kalypso.kml.export.i18n.Messages;
import org.kalypso.ogc.gml.map.IMapPanel;

/**
 * @author Dirk Kuch
 */
public class WizardGoogleExport extends Wizard
{
  private PageGoogleExport m_page;

  private final File m_targetFile;

  private final IMapPanel m_mapPanel;

  public WizardGoogleExport( final IMapPanel mapPanel, final File targetFile )
  {
    m_mapPanel = mapPanel;
    m_targetFile = targetFile;
  }

  @Override
  public void addPages( )
  {
    setWindowTitle( Messages.WizardGoogleExport_0 );

    m_page = new PageGoogleExport( m_targetFile );
    addPage( m_page );
  }

  public IKMLExportSettings getExportedSettings( )
  {
    return m_page;
  }

  @Override
  public boolean performFinish( )
  {
    final KMLExporter googleEarthExporter = new KMLExporter( m_mapPanel, m_page );

    final IStatus status = RunnableContextHelper.execute( getContainer(), true, false, googleEarthExporter );
    ErrorDialog.openError( getShell(), getWindowTitle(), Messages.WizardGoogleExport_1, status );

    if( status.isOK() )
      return true;

    return false;
  }
}