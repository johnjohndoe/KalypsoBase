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
package org.kalypso.gml.ui.tools;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.wizards.IWizardRegistry;
import org.kalypso.contribs.eclipse.jface.dialog.DialogSettingsUtils;
import org.kalypso.contribs.eclipse.ui.dialogs.GenericWizardsWizard;
import org.kalypso.gml.ui.KalypsoGmlUIPlugin;
import org.kalypso.gml.ui.KalypsoGmlUiImages;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.ui.KalypsoGisPlugin;

/**
 * @author Gernot Belger
 */
public class ToolWizardsWizard extends GenericWizardsWizard
{
  public ToolWizardsWizard( final IStructuredSelection selection, final IWizardRegistry registry )
  {
    super( selection, registry );

    setWindowTitle( Messages.getString( "ToolWizardsWizard_0" ) ); //$NON-NLS-1$

    final ImageDescriptor toolImage = KalypsoGmlUIPlugin.getImageProvider().getImageDescriptor( KalypsoGmlUiImages.DESCRIPTORS.TOOLS_WIZ );
    setDefaultPageImageDescriptor( toolImage );

    setDialogSettings( DialogSettingsUtils.getDialogSettings( KalypsoGisPlugin.getDefault(), getClass().getName() ) );
  }

  @Override
  protected String getWizardsPageMessage( )
  {
    return Messages.getString( "ToolWizardsWizard_1" ); //$NON-NLS-1$
  }

  @Override
  protected String getWizardsPageDescription( )
  {
    return Messages.getString( "ToolWizardsWizard_2" ); //$NON-NLS-1$
  }
}