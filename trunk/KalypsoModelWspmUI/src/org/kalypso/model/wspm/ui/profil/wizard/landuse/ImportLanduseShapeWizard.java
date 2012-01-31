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
package org.kalypso.model.wspm.ui.profil.wizard.landuse;

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IPageChangeProvider;
import org.eclipse.jface.dialogs.IPageChangedListener;
import org.eclipse.jface.dialogs.PageChangedEvent;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.jface.viewers.IRefreshable;
import org.kalypso.model.wspm.core.IWspmPointProperties;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.pages.LanduseMappingPage;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.runnables.ImportLanduseShapeRunnable;
import org.kalypso.model.wspm.ui.profil.wizard.landuse.utils.LanduseShapeHandler;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.outline.nodes.IThemeNode;
import org.kalypso.ui.wizard.shape.SelectShapeFilePage;

/**
 * @author Dirk Kuch
 */
public class ImportLanduseShapeWizard extends Wizard implements IWorkbenchWizard
{
  private final IPageChangedListener m_pageListener = new IPageChangedListener()
  {
    @Override
    public void pageChanged( final PageChangedEvent event )
    {
      handlePageChanged( event.getSelectedPage() );
    }
  };

  protected SelectShapeFilePage m_pageShapeImport;

  protected IProject m_project;

  private LanduseShapeHandler m_handler;

  private LanduseMappingPage m_roughnessPage;

  private LanduseMappingPage m_vegetationPage;

  public ImportLanduseShapeWizard( )
  {
    setWindowTitle( Messages.getString( "ImportLanduseShapeWizard.0" ) ); //$NON-NLS-1$

    setNeedsProgressMonitor( true );
  }

  @Override
  public void addPages( )
  {
    m_pageShapeImport = new SelectShapeFilePage( "shapePage" ); //$NON-NLS-1$
    addPage( m_pageShapeImport );

    m_pageShapeImport.setTitle( Messages.getString( "ImportLanduseShapeWizard.1" ) ); //$NON-NLS-1$
    m_pageShapeImport.setDescription( Messages.getString( "ImportLanduseShapeWizard.2" ) ); //$NON-NLS-1$

    m_handler = new LanduseShapeHandler( m_pageShapeImport, m_project );

    m_roughnessPage = new LanduseMappingPage( m_handler, IWspmPointProperties.POINT_PROPERTY_ROUGHNESS_CLASS );
    m_vegetationPage = new LanduseMappingPage( m_handler, IWspmPointProperties.POINT_PROPERTY_BEWUCHS_CLASS );

    addPage( m_roughnessPage );
    addPage( m_vegetationPage );
  }

  @Override
  public boolean performFinish( )
  {
    try
    {
      final ImportLanduseShapeRunnable runnable = new ImportLanduseShapeRunnable( m_handler, m_roughnessPage.getModel(), m_vegetationPage.getModel() );
      getContainer().run( false, false, runnable );

      return true;
    }
    catch( final Exception ex )
    {
      ex.printStackTrace();
    }

    return false;
  }

  @Override
  public void init( final IWorkbench workbench, final IStructuredSelection selection )
  {
    final Iterator< ? > itr = selection.iterator();

    while( itr.hasNext() )
    {
      final Object next = itr.next();
      if( next instanceof IThemeNode )
      {
        final IThemeNode node = (IThemeNode) next;
        final Object nodeElement = node.getElement();
        if( !(nodeElement instanceof IKalypsoTheme) )
          throw new UnsupportedOperationException();

        final IKalypsoTheme theme = (IKalypsoTheme) nodeElement;
        m_project = ResourceUtilities.findProjectFromURL( theme.getContext() );

        break;
      }
      else if( next instanceof IResource )
      {
        final IResource resource = (IResource) next;
        m_project = resource.getProject();

        break;
      }
    }

    Assert.isNotNull( m_project );
  }

  @Override
  public void setContainer( final IWizardContainer container )
  {
    final IWizardContainer oldContainer = getContainer();
    if( oldContainer instanceof IPageChangeProvider )
      ((IPageChangeProvider) oldContainer).removePageChangedListener( m_pageListener );

    super.setContainer( container );

    if( container instanceof IPageChangeProvider )
      ((IPageChangeProvider) container).addPageChangedListener( m_pageListener );
  }

  protected void handlePageChanged( final Object selectedPage )
  {
    if( selectedPage instanceof IRefreshable )
    {
      final IRefreshable page = (IRefreshable) selectedPage;
      page.refresh();
    }
  }
}