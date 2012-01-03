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
package org.kalypso.model.wspm.ui.profil.wizard.classification.landuse;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.resources.IProject;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.kalypso.commons.command.ICommand;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.model.wspm.ui.action.ProfileSelection;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.wizard.ProfileHandlerUtils;
import org.kalypso.model.wspm.ui.profil.wizard.ProfilesChooserPage;
import org.kalypso.model.wspm.ui.profil.wizard.classification.landuse.pages.ApplyLanduseShapePage;
import org.kalypso.model.wspm.ui.profil.wizard.classification.landuse.worker.ApplyLanduseWorker;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.model.ILanduseModel;
import org.kalypso.model.wspm.ui.profil.wizard.utils.FeatureThemeWizardUtilitites;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.command.ChangeFeaturesCommand;
import org.kalypso.ogc.gml.command.FeatureChange;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypsodeegree.model.feature.GMLWorkspace;

/**
 * @author Dirk Kuch
 */
public class ApplyLanduseShapeWizard extends Wizard implements IWorkbenchWizard
{
  protected IProject m_project;

  private ApplyLanduseShapePage m_page;

  private ProfilesChooserPage m_profileChooserPage;

  private ProfileSelection m_profileSelection;

  private IKalypsoFeatureTheme m_theme;

  public ApplyLanduseShapeWizard( )
  {
    setWindowTitle( Messages.getString( "ApplyLanduseShapeWizard_0" ) ); //$NON-NLS-1$

    setNeedsProgressMonitor( true );
  }

  @Override
  public void addPages( )
  {
    m_page = new ApplyLanduseShapePage( m_project );

    final String msg = Messages.getString( "ApplyLanduseShapeWizard_1" ); //$NON-NLS-1$
    final String title = Messages.getString( "ApplyLanduseShapeWizard_2" ); //$NON-NLS-1$

    m_profileChooserPage = new ProfilesChooserPage( msg, m_profileSelection, false );
    m_profileChooserPage.setTitle( title );

    addPage( m_page );
    addPage( m_profileChooserPage );
  }

  @Override
  public boolean performFinish( )
  {
    final ILanduseModel model = m_page.getModel();

    try
    {
      final ApplyLanduseDelegate delegate = new ApplyLanduseDelegate( model, m_profileChooserPage );
      try
      {
        final ApplyLanduseWorker worker = new ApplyLanduseWorker( delegate );
        getContainer().run( false, true, worker );

        final FeatureChange[] changes = worker.getChanges();
        if( !ArrayUtils.isEmpty( changes ) )
        {
          final GMLWorkspace gmlworkspace = changes[0].getFeature().getWorkspace();
          final ICommand command = new ChangeFeaturesCommand( gmlworkspace, changes );
          m_theme.postCommand( command, null );
        }

        return true;
      }
      finally
      {
        delegate.dispose();
      }
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return false;
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    final Object element = selection.getFirstElement();

    if( element instanceof IThemeNode )
    {
      final IThemeNode node = (IThemeNode) element;
      final Object nodeElement = node.getElement();
      if( !(nodeElement instanceof IKalypsoTheme) )
        throw new UnsupportedOperationException();

      final IKalypsoTheme theme = (IKalypsoTheme) nodeElement;
      final IMapModell model = theme.getMapModell();

      m_project = ResourceUtilities.findProjectFromURL( model.getContext() );
    }
    else
      throw new UnsupportedOperationException();

    m_theme = FeatureThemeWizardUtilitites.findTheme( selection );

    final ProfileSelection profileSelection = ProfileHandlerUtils.getSelectionChecked( selection );
    m_profileSelection = profileSelection;
  }

}
