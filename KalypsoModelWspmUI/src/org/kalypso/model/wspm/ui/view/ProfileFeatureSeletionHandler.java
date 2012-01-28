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
import org.kalypso.model.wspm.core.gml.ProfileFeatureSelections;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.ogc.gml.selection.IFeatureSelection;

/**
 * @author kuch
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
      m_part.handleProfilProviderChanged( provider );
    }
  };

  protected final IProfileFeatureSelectionListener m_part;

  private IProfileFeature m_profileFeature;

  private IWorkbenchPart m_profileSourcePart;

  public ProfileFeatureSeletionHandler( final IProfileFeatureSelectionListener part )
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

  protected void handleSelectionChanged( final IStructuredSelection selection )
  {
    final IProfileFeature profileFeature = ProfileFeatureSelections.findFeature( selection );
    setProfileFeature( profileFeature );
  }

  private void setProfileFeature( final IProfileFeature profileFeature )
  {
    if( Objects.equal( profileFeature, m_profileFeature ) )
      return;

    if( m_profileFeature != null )
      m_profileFeature.removeProfilProviderListener( m_providerListener );

    m_profileFeature = profileFeature;

    if( m_profileFeature != null )
      m_profileFeature.addProfilProviderListener( m_providerListener );

    m_part.handleProfilProviderChanged( m_profileFeature );
  }

  public void dispose( )
  {
    if( m_profileSourcePart != null )
      m_profileSourcePart.getSite().getSelectionProvider().removeSelectionChangedListener( m_selectionListener );

    if( m_profileFeature != null )
      m_profileFeature.removeProfilProviderListener( m_providerListener );

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
    handleSelectionChanged( (IStructuredSelection) initialSelection );
  }

  public void doInit( final IViewSite site )
  {
    final IWorkbenchPage page = site.getPage();
    m_adapterPartListener.init( page );
  }

  public IProfil getProfile( )
  {
    if( m_profileFeature != null )
      return m_profileFeature.getProfil();

    return null;
  }

  public IProfileProvider getProfileFeature( )
  {
    return m_profileFeature;
  }
}