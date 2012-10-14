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
package org.kalypso.model.wspm.ui.view;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.kalypso.commons.java.lang.Objects;
import org.kalypso.contribs.eclipse.ui.partlistener.AdapterPartListener;
import org.kalypso.contribs.eclipse.ui.partlistener.EditorFirstAdapterFinder;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.gml.IProfileProvider;
import org.kalypso.model.wspm.core.gml.IProfileProviderListener;
import org.kalypso.model.wspm.core.gml.IProfileSelection;
import org.kalypso.model.wspm.core.gml.ProfileSelection;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.event.FeaturesChangedModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEvent;

/**
 * @author Dirk Kuch
 */
public class ProfileFeatureSeletionHandler
{
  private final AdapterPartListener<IFeatureSelection> m_adapterPartListener;

  private final ISelectionChangedListener m_selectionListener = new ISelectionChangedListener()
  {
    @Override
    public void selectionChanged( final SelectionChangedEvent event )
    {
      handleSelectionChanged( (IStructuredSelection) event.getSelection() );
    }
  };

  private final IProfileProviderListener m_providerListener = new IProfileProviderListener()
  {
    @Override
    public void onProfilProviderChanged( final IProfileProvider provider )
    {
      handleProfileChanged( provider );
    }
  };

  private final IProfileSelectionListener m_part;

  private IProfileSelection m_selectedElement = ProfileSelection.fromSelection( StructuredSelection.EMPTY );

  private IWorkbenchPart m_profileSourcePart;

  public ProfileFeatureSeletionHandler( final IProfileSelectionListener part )
  {
    m_part = part;

    m_adapterPartListener = new AdapterPartListener<IFeatureSelection>( IFeatureSelection.class, part, new EditorFirstAdapterFinder<IFeatureSelection>(), new EditorFirstAdapterFinder<IFeatureSelection>() )
    {
      @Override
      protected IFeatureSelection doAdaptPart( final IWorkbenchPart p )
      {
        final ISelectionProvider selectionProvider = p.getSite().getSelectionProvider();
        if( selectionProvider == null )
          return null;

        final ISelection selection = selectionProvider.getSelection();
        if( selection instanceof IFeatureSelection )
          return (IFeatureSelection) selection;

        return null;
      }
    };
  }

  public void dispose( )
  {
    if( Objects.isNotNull( m_profileSourcePart ) )
    {
      final ISelectionProvider provider = m_profileSourcePart.getSite().getSelectionProvider();
      if( Objects.isNotNull( provider ) )
        provider.removeSelectionChangedListener( m_selectionListener );
    }

    final IProfileProvider profileFeature = getProfileFeature();
    if( profileFeature != null )
      profileFeature.removeProfilProviderListener( m_providerListener );

    if( m_adapterPartListener != null )
      m_adapterPartListener.dispose();
  }

  public void setAdapter( final IWorkbenchPart part, final ISelection selection )
  {
    if( Objects.equal( m_profileSourcePart, part ) )
      return;

    if( m_profileSourcePart != null )
      m_profileSourcePart.getSite().getSelectionProvider().removeSelectionChangedListener( m_selectionListener );

    m_profileSourcePart = part;

    if( m_profileSourcePart != null )
      m_profileSourcePart.getSite().getSelectionProvider().addSelectionChangedListener( m_selectionListener );

    final ISelection initialSelection = Objects.firstNonNull( selection, StructuredSelection.EMPTY );
    handleSelectionChanged( (IStructuredSelection)initialSelection );
  }

  public void doInit( final IViewSite site )
  {
    final IWorkbenchPage page = site.getPage();
    m_adapterPartListener.init( page );
  }

  protected void handleSelectionChanged( final IStructuredSelection selection )
  {
    final IProfileSelection selectedElement = ProfileSelection.fromSelection( selection );

    /* If during sleection the source does not change, we do nothing */
    if( Objects.equal( selectedElement.getSource(), m_selectedElement.getSource() ) )
      return;

    final IProfileProvider oldProfileFeature = getProfileFeature();
    if( oldProfileFeature != null )
      oldProfileFeature.removeProfilProviderListener( m_providerListener );

    m_selectedElement = selectedElement;

    final IProfileProvider profileFeature = getProfileFeature();
    if( profileFeature != null )
      profileFeature.addProfilProviderListener( m_providerListener );

    m_part.handleProfilSourceChanged( selectedElement );
  }

  // TODO: consider moving this into the table/chart
  protected void handleProfileChanged( final IProfileProvider provider )
  {
    /* check if the event comes from the current selected element */
    final IProfileFeature selectedProvider = m_selectedElement == null ? null : m_selectedElement.getProfileFeature();
    if( provider != selectedProvider )
      return;

    /* reinit selection */
    final ProfileSelection newSelectedElement = new ProfileSelection( m_selectedElement.getProfileFeature(), m_selectedElement.getSource() );

    // REMARK: no need to unregister/register the profile provider listeners, we know that we still have the same profile feature as before
    m_selectedElement = newSelectedElement;

    m_part.handleProfilSourceChanged( newSelectedElement );

    // HACK/REMARK: sometimes, the selected object is not directly the profile but a referencing feasture (e.g. TuhhReachSegment)
    // In order to let this element to refresh it's state (e.g. the decorator on the state tree), we fire an extra event on this element

    // FIXME: does not belong here, the decorator itself should be listener to the object and react accordingly

    final Object selection = newSelectedElement.getSource();
    if( selection == newSelectedElement.getProfileFeature() )
      return;

    if( !(selection instanceof Feature) )
      return;

    final Feature selectedFeature = (Feature)selection;
    final GMLWorkspace workspace = selectedFeature.getWorkspace();
    if( workspace == null )
      return;

    final ModellEvent changeEvent = new FeaturesChangedModellEvent( workspace, new Feature[] { selectedFeature } );
    workspace.fireModellEvent( changeEvent );
  }

  private IProfileFeature getProfileFeature( )
  {
    if( m_selectedElement == null )
      return null;

    return m_selectedElement.getProfileFeature();
  }

  public IProfileSelection getProfileSelection( )
  {
    return m_selectedElement;
  }
}