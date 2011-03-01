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
package org.kalypso.ui.editor.mapeditor;

import java.awt.Insets;

import org.apache.commons.configuration.Configuration;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.metadoc.IExportableObjectFactory;
import org.kalypso.metadoc.configuration.IPublishingConfiguration;
import org.kalypso.metadoc.ui.ImageExportPage;
import org.kalypso.ogc.gml.map.IMapPanel;

/**
 * The map exportable object factory.
 * 
 * @author Holger Albert
 */
public class MapExportableObjectFactory implements IExportableObjectFactory
{
  /**
   * The map panel.
   */
  private IMapPanel m_mapPanel;

  /**
   * The constructor.
   * 
   * @param mapPanel
   *          The map panel.
   */
  public MapExportableObjectFactory( IMapPanel mapPanel )
  {
    m_mapPanel = mapPanel;
  }

  /**
   * @see org.kalypso.metadoc.IExportableObjectFactory#createExportableObjects(org.apache.commons.configuration.Configuration)
   */
  @Override
  public IExportableObject[] createExportableObjects( Configuration conf )
  {
    // TODO
    int width = conf.getInt( ImageExportPage.CONF_IMAGE_WIDTH, 640 );
    int height = conf.getInt( ImageExportPage.CONF_IMAGE_HEIGHT, 480 );
    Insets insets = (Insets) conf.getProperty( "insets" );
    int borderWidth = conf.getInt( "borderWidth" );
    String format = conf.getString( ImageExportPage.CONF_IMAGE_FORMAT, "png" );

    return new IExportableObject[] { new MapExportableObject( m_mapPanel, width, height, insets, borderWidth, format ) };
  }

  /**
   * @see org.kalypso.metadoc.IExportableObjectFactory#createWizardPages(org.kalypso.metadoc.configuration.IPublishingConfiguration,
   *      ImageDescriptor)
   */
  @Override
  public IWizardPage[] createWizardPages( IPublishingConfiguration configuration, ImageDescriptor defaultImage )
  {
    // TODO

    return null;
  }
}