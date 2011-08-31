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
package org.kalypso.model.wspm.ui.profil.wizard.landuse.utils;

import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.model.wspm.core.gml.IWspmProject;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.shape.FileMode;
import org.kalypso.shape.ShapeFile;
import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.ui.wizard.shape.IShapeFileSelection;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Dirk Kuch
 */
public class LanduseShapeHandler implements ILanduseShapeDataProvider
{

  private final IShapeFileSelection m_selection;

  private ShapeFile m_shapeFile;

  private String m_lnk;

  private final IProject m_project;

  private GMLWorkspace m_workspace;

  public LanduseShapeHandler( final IShapeFileSelection selection, final IProject project )
  {
    m_selection = selection;
    m_project = project;
  }

  /**
   * @see org.kalypso.model.wspm.ui.profil.wizard.landuse.ILanduseShape#getShapeFile()
   */
  @Override
  public ShapeFile getShapeFile( ) throws IOException, DBaseException
  {
    final String lnk = FilenameUtils.removeExtension( m_selection.getShapeFile() );
    if( Objects.notEqual( m_lnk, lnk ) )
    {
      if( Objects.isNotNull( m_shapeFile ) )
        m_shapeFile.close();

      m_shapeFile = new ShapeFile( lnk, Charset.defaultCharset(), FileMode.READ );
      m_lnk = lnk;
    }

    return m_shapeFile;
  }

  /**
   * @see org.kalypso.model.wspm.ui.profil.wizard.landuse.utils.ILanduseShapeDataProvider#getWspmModel()
   */
  @Override
  public IWspmProject getWspmModel( ) throws Exception
  {
    if( Objects.isNotNull( m_workspace ) )
      return (IWspmProject) m_workspace.getRootFeature();

    final IFile file = m_project.getFile( "modell.gml" ); //$NON-NLS-1$
    m_workspace = GmlSerializer.createGMLWorkspace( file );

    return (IWspmProject) m_workspace.getRootFeature();
  }

  /**
   * @see org.kalypso.model.wspm.ui.profil.wizard.landuse.utils.ILanduseShapeDataProvider#dispose()
   */
  @Override
  public void dispose( )
  {
    if( Objects.isNotNull( m_shapeFile ) )
      try
      {
        m_shapeFile.close();
      }
      catch( final IOException e )
      {
        e.printStackTrace();
      }

    if( Objects.isNotNull( m_workspace ) )
      m_workspace.dispose();
  }

}
