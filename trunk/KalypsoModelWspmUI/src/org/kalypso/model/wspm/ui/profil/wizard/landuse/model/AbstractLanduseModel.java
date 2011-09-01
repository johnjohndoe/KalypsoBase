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

import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.util.AbstractModelObject;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.core.gml.IWspmProject;
import org.kalypso.model.wspm.core.gml.classifications.IClassificationClass;
import org.kalypso.model.wspm.core.gml.classifications.IWspmClassification;
import org.kalypso.ogc.gml.serialize.GmlSerializer;
import org.kalypso.shape.dbf.IDBFField;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractLanduseModel extends AbstractModelObject implements ILanduseModel
{
  private Properties m_mapping = new Properties();

  private IDBFField m_shapeColumn = null;

  private final IProject m_project;

  private GMLWorkspace m_workspace;

  public AbstractLanduseModel( final IProject project )
  {
    m_project = project;
  }

  @Override
  public final Properties getMapping( )
  {
    return m_mapping;
  }

  public final void setMapping( final Properties mapping )
  {
    final Object oldValue = m_mapping;
    m_mapping = mapping;

    firePropertyChange( PROPERTY_MAPPING, oldValue, mapping );
  }

  @Override
  public final IClassificationClass[] getClasses( )
  {
    try
    {
      final IWspmProject project = getWspmModel();
      final IWspmClassification classification = project.getClassificationMember();

      if( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS.equals( getType() ) )
        return classification.getVegetationClasses();
      else if( IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS.equals( getType() ) )
        return classification.getRoughnessClasses();
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
}
