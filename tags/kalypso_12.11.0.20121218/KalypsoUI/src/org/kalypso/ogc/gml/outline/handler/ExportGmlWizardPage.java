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
package org.kalypso.ogc.gml.outline.handler;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.kalypso.commons.databinding.jface.wizard.DatabindingWizardPage;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * @author Gernot Belger
 */
public class ExportGmlWizardPage extends WizardPage
{
  private final ExportGMLData m_data;

  private DatabindingWizardPage m_binding;

  public ExportGmlWizardPage( final String pageName, final ExportGMLData data )
  {
    super( pageName );
    m_data = data;

    setTitle( ExportFileWizardConstants.STR_EXPORT_FILE_PAGE_TITLE );
    setDescription( ExportFileWizardConstants.STR_EXPORT_FILE_PAGE_DESCRIPTION );
  }

  @Override
  public void createControl( final Composite parent )
  {
    final Composite panel = new Composite( parent, SWT.NONE );
    setControl( panel );
    GridLayoutFactory.swtDefaults().numColumns( 2 ).applyTo( panel );

    m_binding = new DatabindingWizardPage( this, null );

    createFileControls( panel );
  }

  private void createFileControls( final Composite parent )
  {
    final String title = getWizard().getWindowTitle();
    final ExportFileControls exportFileControls = new ExportFileControls( m_data, m_binding, title );

    final String gmlFilterName = Messages.getString( "org.kalypso.ogc.gml.outline.handler.ExportGMLThemeHandler.6" ); //$NON-NLS-1$
    exportFileControls.addFilter( gmlFilterName, "*.gml" ); //$NON-NLS-1$
    exportFileControls.createControls( parent, 1 );
  }
}