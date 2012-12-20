/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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

import org.apache.commons.collections.ExtendedProperties;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.kalypso.metadoc.IExportableObject;
import org.kalypso.metadoc.IExportableObjectFactory;
import org.kalypso.metadoc.configuration.PublishingConfiguration;
import org.kalypso.ogc.gml.map.IMapPanel;
import org.kalypso.ui.controls.images.ImagePropertiesWizardPage;
import org.kalypso.ui.internal.i18n.Messages;

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
  private final IMapPanel m_mapPanel;

  /**
   * The constructor.
   * 
   * @param mapPanel
   *          The map panel.
   */
  public MapExportableObjectFactory( final IMapPanel mapPanel )
  {
    m_mapPanel = mapPanel;
  }

  @Override
  public IExportableObject[] createExportableObjects( final ExtendedProperties conf )
  {
    final String preferredDocumentName = m_mapPanel.getMapModell().getName().getValue();
    final int width = conf.getInt( ImagePropertiesWizardPage.CONFIG_IMAGE_WIDTH, m_mapPanel.getWidth() );
    final int height = conf.getInt( ImagePropertiesWizardPage.CONFIG_IMAGE_HEIGHT, m_mapPanel.getHeight() );
    final Insets insets = (Insets)conf.getProperty( ImagePropertiesWizardPage.CONFIG_INSETS );
    final boolean border = conf.getBoolean( ImagePropertiesWizardPage.CONFIG_HAS_BORDER, false );
    final int borderWidth = border ? 1 : 0;
    final String format = conf.getString( ImagePropertiesWizardPage.CONFIG_IMAGE_FORMAT, "PNG" ); //$NON-NLS-1$

    return new IExportableObject[] { new MapExportableObject( m_mapPanel, preferredDocumentName, width, height, insets, borderWidth, format ) };
  }

  @Override
  public IWizardPage[] createWizardPages( final PublishingConfiguration configuration, final ImageDescriptor defaultImage )
  {
    return new IWizardPage[] { new ImagePropertiesWizardPage( "ImagePropertiesWizardPage", Messages.getString( "MapExportableObjectFactory_2" ), defaultImage, configuration, -1, -1, false, null, false, "PNG" ) }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
  }
}