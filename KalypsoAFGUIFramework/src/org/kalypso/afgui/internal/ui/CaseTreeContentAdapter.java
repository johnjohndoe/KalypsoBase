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
package org.kalypso.afgui.internal.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.model.IWorkbenchAdapter;
import org.eclipse.ui.model.IWorkbenchAdapter2;
import org.eclipse.ui.model.WorkbenchAdapter;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.kalypso.afgui.KalypsoAFGUIFrameworkPlugin;
import org.kalypso.afgui.scenarios.ScenarioHelper;

import de.renew.workflow.connector.cases.IScenario;
import de.renew.workflow.connector.cases.IScenarioManager;
import de.renew.workflow.connector.cases.ScenarioHandlingProjectNature;

/**
 * TODO: is this really the right place? Shouldn't it better be moved to the AfgUi plug-in which already does all
 * ui-stuff for the workflow?
 *
 * @author Stefan Kurzbach
 */
public class CaseTreeContentAdapter extends WorkbenchAdapter
{
  private final ImageDescriptor m_caseImage;

  private final FontData m_activeFont;

  public CaseTreeContentAdapter( )
  {
    m_caseImage = AbstractUIPlugin.imageDescriptorFromPlugin( KalypsoAFGUIFrameworkPlugin.PLUGIN_ID, "icons/scenario.png" ); //$NON-NLS-1$
    final FontData[] fontData = JFaceResources.getFontRegistry().getFontData( JFaceResources.DIALOG_FONT );
    final String dialogFontName = fontData[0].getName();
    final int dialogFontHeight = fontData[0].getHeight();
    m_activeFont = new FontData( dialogFontName, dialogFontHeight, SWT.BOLD );
  }

  @Override
  public Object[] getChildren( final Object o )
  {
    if( o instanceof ScenarioHandlingProjectNature )
    {
      final ScenarioHandlingProjectNature nature = (ScenarioHandlingProjectNature) o;
      final IScenarioManager caseManager = nature.getCaseManager();
      return caseManager.getCases().toArray();
    }
    return NO_CHILDREN;
  }

  @Override
  public ImageDescriptor getImageDescriptor( final Object o )
  {
    if( o instanceof ScenarioHandlingProjectNature )
    {
      final ScenarioHandlingProjectNature nature = (ScenarioHandlingProjectNature) o;
      final IProject project = nature.getProject();
      final IWorkbenchAdapter adapter = (IWorkbenchAdapter) project.getAdapter( IWorkbenchAdapter.class );
      return adapter.getImageDescriptor( project );
    }
    else if( o instanceof IScenario )
      return m_caseImage;
    else
      return null;
  }

  @Override
  public String getLabel( final Object o )
  {
    if( o instanceof ScenarioHandlingProjectNature )
    {
      final ScenarioHandlingProjectNature nature = (ScenarioHandlingProjectNature) o;
      final IProject project = nature.getProject();
      final IWorkbenchAdapter adapter = (IWorkbenchAdapter) project.getAdapter( IWorkbenchAdapter.class );
      return adapter.getLabel( project );
    }
    else if( o instanceof IScenario )
      return ((IScenario) o).getName();
    return null;
  }

  @Override
  public FontData getFont( final Object o )
  {
    if( o instanceof ScenarioHandlingProjectNature )
    {
      final ScenarioHandlingProjectNature nature = (ScenarioHandlingProjectNature) o;
      final IProject project = nature.getProject();
      final IWorkbenchAdapter2 adapter = (IWorkbenchAdapter2) project.getAdapter( IWorkbenchAdapter2.class );
      return adapter.getFont( project );
    }
    else if( o instanceof IScenario )
    {
      final IScenario caze = (IScenario) o;
      final IWorkbench workbench = PlatformUI.getWorkbench();
      if( !workbench.isClosing() )
      {
        final IScenario activeScenario = ScenarioHelper.getActiveScenario();
        if( caze.equals( activeScenario ) )
          return m_activeFont;
      }
    }
    return null;
  }
}