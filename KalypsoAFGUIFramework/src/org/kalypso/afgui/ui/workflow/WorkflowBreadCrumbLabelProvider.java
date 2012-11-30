/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.afgui.ui.workflow;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.IFontProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;

import de.renew.workflow.connector.cases.IScenario;

/**
 * @author Gernot Belger
 */
public class WorkflowBreadCrumbLabelProvider extends LabelProvider implements IFontProvider
{
  private final WorkbenchLabelProvider m_labelProvider = new WorkbenchLabelProvider();

  @Override
  public void dispose( )
  {
    m_labelProvider.dispose();

    super.dispose();
  }

  @Override
  public Image getImage( final Object element )
  {
    return m_labelProvider.getImage( element );
  }

  @Override
  public String getText( final Object element )
  {
    if( element instanceof String )
      return (String) element;

    return m_labelProvider.getText( element );
  }

  @Override
  public Font getFont( final Object element )
  {
    final IScenario currentScenario = KalypsoAFGUIFrameworkPlugin.getActiveWorkContext().getCurrentCase();
    if( element.equals( currentScenario ) )
      return JFaceResources.getFontRegistry().getBold( JFaceResources.DEFAULT_FONT );

    return null;
  }
}