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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.core.resources.IFile;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.commons.java.util.PropertiesUtilities;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.ILanduseModel;

/**
 * @author Dirk Kuch
 */
public class ALSSelectedShapeFileChangedListener implements PropertyChangeListener
{

  private IFile m_file;

  public ALSSelectedShapeFileChangedListener( )
  {
  }

  @Override
  public void propertyChange( final PropertyChangeEvent evt )
  {
    final ApplyLanduseShapeModel model = (ApplyLanduseShapeModel) evt.getSource();
    final IFile file = (IFile) evt.getNewValue();
    if( Objects.equal( m_file, file ) )
      return;

    try
    {
      final String base = FilenameUtils.removeExtension( model.getLanduseShape().getLocation().toOSString() );

      final Properties shapeFileProperties = getProperties( model, base );
      PropertiesUtilities.merge( model.getMapping(), shapeFileProperties );

      model.firePropertyChange( ILanduseModel.PROPERTY_MAPPING, model.getMapping(), model.getMapping() );
      m_file = file;
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();
    }
  }

  private Properties getProperties( final ApplyLanduseShapeModel model, final String base ) throws IOException
  {
    File file;
    if( IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS.equals( model.getType() ) )
    {
      file = new File( String.format( "%s.vegetation.properties", base ) );
    }
    else if( IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS.equals( model.getType() ) )
    {
      file = new File( String.format( "%s.roughness.properties", base ) );
    }
    else
      throw new IllegalStateException();

    final FileInputStream inputStream = new FileInputStream( file );

    try
    {
      final Properties properties = new Properties();
      properties.load( inputStream );

      return properties;
    }
    finally
    {
      inputStream.close();
    }
  }
}
