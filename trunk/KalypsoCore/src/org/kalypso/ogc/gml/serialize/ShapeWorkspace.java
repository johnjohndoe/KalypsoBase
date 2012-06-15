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
package org.kalypso.ogc.gml.serialize;

import java.io.File;

import org.kalypso.core.i18n.Messages;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypsodeegree.KalypsoDeegreePlugin;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree.model.feature.FeatureVisitor;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree_impl.model.feature.visitors.TransformVisitor;

/**
 * @author Dirk Kuch
 */
public class ShapeWorkspace implements IShapeWorkspace
{
  private GMLWorkspace m_workspace;

  private final File m_file;

  private final CommandableWorkspace m_commandable;

  public ShapeWorkspace( final File file ) throws Exception
  {
    this( file, KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
  }

  public ShapeWorkspace( final File file, final String targetCrs ) throws Exception
  {
    m_file = file;
    /* attention - eShape loading works only without fileextension */
    final String[] sShapes = file.toString().split( "\\." ); //$NON-NLS-1$
    if( sShapes == null || sShapes.length < 2 )
      throw new IllegalStateException( String.format( Messages.getString("ShapeWorkspace_0"), file.getName() ) ); //$NON-NLS-1$

    final StringBuffer buffer = new StringBuffer();
    for( int i = 0; i < sShapes.length - 1; i++ )
    {
      if( i > 0 )
        buffer.append( "." );//$NON-NLS-1$

      buffer.append( sShapes[i] );
    }

    m_workspace = ShapeSerializer.deserialize( buffer.toString(), KalypsoDeegreePlugin.getDefault().getCoordinateSystem() );
    m_commandable = new CommandableWorkspace( m_workspace );

    final TransformVisitor visitor = new TransformVisitor( targetCrs );
    final Feature rootFeature = m_commandable.getRootFeature();
    m_commandable.accept( visitor, rootFeature, FeatureVisitor.DEPTH_INFINITE );
  }

  /**
   * @see org.kalypso.nofdpidss.core.common.shape.IShapeWorkspaceDelegate#dispose()
   */
  @Override
  public void dispose( )
  {
    m_commandable.dispose();
    m_workspace.dispose();
    m_workspace = null;
  }

  public CommandableWorkspace getWorkspace( )
  {
    return m_commandable;
  }

  /**
   * @see org.kalypso.nofdpidss.core.common.shape.IShapeWorkspaceDelegate#getFeatureList()
   */
  @Override
  public FeatureList getFeatureList( )
  {
    final Feature root = m_commandable.getRootFeature();

    return (FeatureList) root.getProperty( ShapeSerializer.PROPERTY_FEATURE_MEMBER );
  }

  public IValuePropertyType getGeometryPropertyType( )
  {
    final Feature feature = (Feature) getFeatureList().get( 0 );

    return feature.getFeatureType().getDefaultGeometryProperty();
  }

  public String getName( )
  {
    return m_file.getName();
  }
}
