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
package org.kalypso.model.wspm.ui.profil.wizard.classification.landuse.model;

import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.util.AbstractModelObject;
import org.kalypso.contribs.eclipse.core.resources.CollectFilesWithExtensionVisitor;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.shape.ShapeFile;
import org.kalypso.shape.dbf.IDBFField;

/**
 * @author Dirk Kuch
 */
public class ApplyLanduseShapeModel extends AbstractModelObject
{

  public static final String PROPERTY_LANDUSE_SHAPE = "landuseShape";

  public static final String PROPERTY_SHAPE_FILE = "shapeFile";

  public static final String PROPERTY_SHAPE_FILE_PROPERTY = "shapeFileProperty";

  public static final String PROPERTY_MAPPING = "mapping";

  public static final String PROPERTY_TYPE = "type";

  private String m_type = IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS;

  private IFile m_landuseShape;

  private IFile[] m_landuseShapeFiles;

  private ShapeFile m_shapeFile;

  private IDBFField m_shapeFileProperty;

  Properties m_mapping = new Properties();

  public ApplyLanduseShapeModel( final IProject project )
  {

    try
    {
      init( project );
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
    }

  }

  private void init( final IProject project ) throws CoreException
  {
    final IFolder landuses = project.getFolder( "data/landuse" );
    if( !landuses.exists() )
      return;

    final CollectFilesWithExtensionVisitor visitor = new CollectFilesWithExtensionVisitor();
    visitor.setExtension( "shp" );
    landuses.accept( visitor );

    m_landuseShapeFiles = visitor.getFiles();
  }

  public IFile[] getLanduseShapeFiles( )
  {
    return m_landuseShapeFiles;
  }

  public IFile getLanduseShape( )
  {
    return m_landuseShape;
  }

  public void setLanduseShape( final IFile shapeFile )
  {
    final Object oldValue = m_landuseShape;
    m_landuseShape = shapeFile;

    firePropertyChange( PROPERTY_LANDUSE_SHAPE, oldValue, shapeFile );
  }

  public ShapeFile getShapeFile( )
  {
    return m_shapeFile;
  }

  public void setShapeFile( final ShapeFile file )
  {
    final Object oldValue = m_shapeFile;
    if( Objects.isNotNull( m_shapeFile ) )
      try
      {
        m_shapeFile.close();
      }
      catch( final IOException e )
      {
        e.printStackTrace();
      }

    m_shapeFile = file;

    firePropertyChange( PROPERTY_SHAPE_FILE, oldValue, file );
  }

  public IDBFField getShapeFileProperty( )
  {
    return m_shapeFileProperty;
  }

  public void setShapeFileProperty( final IDBFField property )
  {
    final Object oldValue = m_shapeFileProperty;
    m_shapeFileProperty = property;

    firePropertyChange( PROPERTY_SHAPE_FILE_PROPERTY, oldValue, property );
  }

  public Properties getMapping( )
  {
    return m_mapping;
  }

  public void setMapping( final Properties mapping )
  {
    final Object oldValue = m_mapping;
    m_mapping = mapping;

    firePropertyChange( PROPERTY_MAPPING, oldValue, mapping );
  }

  public void fireMappingChanged( )
  {
    firePropertyChange( PROPERTY_MAPPING, m_mapping, m_mapping );
  }

  public String getType( )
  {
    return m_type;
  }

  public void setType( final String type )
  {
    final Object oldValue = m_type;
    m_type = type;

    firePropertyChange( PROPERTY_TYPE, oldValue, type );
  }

}
