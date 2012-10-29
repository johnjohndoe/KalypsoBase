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
package org.kalypso.ui.wizard.shape;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.kalypso.commons.databinding.swt.FileAndHistoryData;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.addlayer.dnd.MapDropData;
import org.kalypso.ui.i18n.Messages;
import org.kalypso.ui.wizard.AbstractDataImportWizard;

/**
 * @author Kuepferle
 */
public class ImportShapeSourceWizard extends AbstractDataImportWizard
{
  private final ImportShapeFileData m_data = new ImportShapeFileData();

  public ImportShapeSourceWizard( )
  {
    setWindowTitle( Messages.getString( "org.kalypso.ui.wizard.shape.ImportShapeSourceWizard.0" ) ); //$NON-NLS-1$
  }

  @Override
  public void addPages( )
  {
    m_data.init( getDialogSettings() );

    final IProject project = ResourceUtilities.findProjectFromURL( getMapModel().getContext() );

    final String title = getWindowTitle();
    final ImportShapeFileImportPage page = new ImportShapeFileImportPage( "shapefileimport", title, ImageProvider.IMAGE_KALYPSO_ICON_BIG, m_data ); //$NON-NLS-1$

    page.setProjectSelection( project );

    addPage( page );
  }

  @Override
  public void initFromDrop( final MapDropData data )
  {
    final String path = data.getPath();

    final FileAndHistoryData shapeFile = m_data.getShapeFile();
    if( StringUtils.isEmpty( path ) )
      shapeFile.setPath( null );
    else
      shapeFile.setPath( new Path( path ) );
  }

  @Override
  public boolean performFinish( )
  {
    m_data.storeSettings( getDialogSettings() );

    final int insertionIndex = getInsertionIndex();
    m_data.setInsertionIndex( insertionIndex );

    final ImportShapeOperation operation = new ImportShapeOperation( m_data );

    return operation.executeOnWizard( this );
  }
}