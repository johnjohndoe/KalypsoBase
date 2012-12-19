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
package org.kalypso.model.wspm.ui.profil.wizard.landuse.model;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.util.AbstractModelObject;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.gml.IWspmProject;
import org.kalypso.model.wspm.core.gml.classifications.IClassificationClass;
import org.kalypso.model.wspm.core.gml.classifications.IRoughnessClass;
import org.kalypso.model.wspm.core.gml.classifications.IVegetationClass;
import org.kalypso.model.wspm.core.gml.classifications.IWspmClassification;
import org.kalypso.model.wspm.ui.view.table.handler.RoughnessClassComparator;
import org.kalypso.model.wspm.ui.view.table.handler.VegetationClassComparator;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.shape.FileMode;
import org.kalypso.shape.ShapeFile;
import org.kalypso.shape.dbf.IDBFField;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Dirk Kuch
 */
public class LanduseModel extends AbstractModelObject implements ILanduseModel
{
  private LanduseProperties m_mapping = new LanduseProperties( this );

  private IDBFField m_shapeColumn = null;

  private ShapeFile m_shapeFile;

  private IFile m_landuseShape;

  private final IProject m_project;

  private GMLWorkspace m_workspace;

  private String m_type;

  private String m_shapeFileBase;

  private String m_shapeSRS;

  public LanduseModel( final IProject project, final String type )
  {
    m_project = project;
    m_type = type;
  }

  @Override
  public final LanduseProperties getMapping( )
  {
    return m_mapping;
  }

  protected IProject getProject( )
  {
    return m_project;
  }

  public final void setMapping( final LanduseProperties mapping )
  {
    final Object oldValue = m_mapping;
    m_mapping = mapping;

    firePropertyChange( PROPERTY_MAPPING, oldValue, mapping );
  }

  /**
   * @return classes defined in the underlying wspm modell.gml
   */
  @Override
  public final IClassificationClass[] getClasses( )
  {
    try
    {
      final IWspmProject project = getWspmModel();
      final IWspmClassification classification = project.getClassificationMember();

      if( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS.equals( getType() ) )
      {
        final IVegetationClass[] vegetationClasses = classification.getVegetationClasses();

        /* Sort the vegetation classes. */
        Arrays.sort( vegetationClasses, new VegetationClassComparator() );

        return vegetationClasses;
      }
      else if( IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS.equals( getType() ) )
      {
        final IRoughnessClass[] roughnessClasses = classification.getRoughnessClasses();

        /* Sort the vegetation classes. */
        Arrays.sort( roughnessClasses, new RoughnessClassComparator() );

        return roughnessClasses;
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    throw new UnsupportedOperationException();
  }

  public IWspmProject getWspmModel( ) throws Exception
  {
    if( Objects.isNotNull( m_workspace ) )
      return (IWspmProject) m_workspace.getRootFeature();

    final IFile file = m_project.getFile( "modell.gml" ); //$NON-NLS-1$
    m_workspace = GmlSerializer.createGMLWorkspace( file );

    return (IWspmProject) m_workspace.getRootFeature();
  }

  @Override
  public final IDBFField getShapeColumn( )
  {
    return m_shapeColumn;
  }

  public final void setShapeColumn( final IDBFField shapeColumn )
  {
    final Object oldValue = m_shapeColumn;
    m_shapeColumn = shapeColumn;

    firePropertyChange( PROPERTY_SHAPE_COLUMN, oldValue, shapeColumn );
  }

  @Override
  public ShapeFile getShapeFile( )
  {
    return m_shapeFile;
  }

  public void updateShapeFile( final String location )
  {
    try
    {
      final String base = FilenameUtils.removeExtension( location );
      if( StringUtils.equalsIgnoreCase( m_shapeFileBase, base ) )
        return;

      final ShapeFile shapeFile = new ShapeFile( base, Charset.defaultCharset(), FileMode.READ );
      if( Objects.isNotNull( m_shapeFile ) )
        m_shapeFile.close();

      m_shapeSRS = readShapeSRS( base );

      m_shapeFile = shapeFile;
      m_shapeFileBase = base;
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();
    }
  }

  private String readShapeSRS( final String base )
  {
    try
    {
      final File prjFile = new File( base + ".prj" ); //$NON-NLS-1$

      return FileUtils.readFileToString( prjFile );
    }
    catch( final IOException e )
    {
      return KalypsoDeegreePlugin.getDefault().getCoordinateSystem();
    }
  }

  @Override
  public IFile getLanduseShape( )
  {
    return m_landuseShape;
  }

  public void setLanduseShape( final IFile shapeFile )
  {
    final Object oldValue = m_landuseShape;
    m_landuseShape = shapeFile;

    updateShapeFile( shapeFile.getLocation().toOSString() );

    firePropertyChange( PROPERTY_LANDUSE_SHAPE, oldValue, shapeFile );
  }

  @Override
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

  @Override
  public String getShapeSRS( )
  {
    return m_shapeSRS;
  }
}