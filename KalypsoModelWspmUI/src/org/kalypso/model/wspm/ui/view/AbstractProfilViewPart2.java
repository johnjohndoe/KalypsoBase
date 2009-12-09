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
package org.kalypso.model.wspm.ui.view;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.contribs.eclipse.ui.partlistener.AdapterPartListener;
import org.kalypso.contribs.eclipse.ui.partlistener.EditorFirstAdapterFinder;
import org.kalypso.contribs.eclipse.ui.partlistener.IAdapterEater;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilListener;
import org.kalypso.model.wspm.ui.Messages;
import org.kalypso.model.wspm.ui.profil.IProfilProvider2;
import org.kalypso.model.wspm.ui.profil.IProfilProviderListener;

/**
 * @author Gernot Belger
 */
public abstract class AbstractProfilViewPart2 extends ViewPart implements IProfilViewPart2, IProfilViewDataListener, IProfilListener, IProfilProviderListener, IAdapterEater
{
  //private ProfilChartViewActionBarContributor m_actionContributor = new ProfilChartViewActionBarContributor();

  private final AdapterPartListener m_adapterPartListener = new AdapterPartListener( IProfilProvider2.class, this, new EditorFirstAdapterFinder(), new EditorFirstAdapterFinder() );

  private IProfil m_profile = null;

  private Composite m_control;

  private ProfilViewData m_viewData = null;

  private IProfilProvider2 m_provider;

  /** The part where the profile provider came from. */
  private IWorkbenchPart m_profilProviderPart = null;

  private final UIJob m_updateProfilJob = new UIJob( Messages.AbstractProfilViewPart2_4 )
  {
    @Override
    public IStatus runInUIThread( IProgressMonitor monitor )
    {
      handleProfilChanged();
      return Status.OK_STATUS;
    }
  };

 
  @Override
  public void init( final IViewSite site ) throws PartInitException
  {
    super.init( site );

    final IWorkbenchPage page = site.getPage();

    m_adapterPartListener.init( page );
   
  }

  public IProfil getProfil( )
  {
    return m_profile;
  }

  public void setAdapter( final IWorkbenchPart part, final Object adapter )
  {
    final IProfilProvider2 provider = (IProfilProvider2) adapter;
    if( provider == m_provider )
    {
      // for first initialization, provider empty profile
      if( provider == null )
        onProfilProviderChanged( null, null, null, null, null );

      return;
    }

    if( m_provider != null )
    {
      m_provider.removeProfilProviderListener( this );
      m_provider = null;
    }

    final IProfil oldProfile = m_profile;
    final ProfilViewData oldViewData = getProfilViewData();

    m_provider = provider;
    m_profilProviderPart = part;

    if( m_provider != null )
      m_provider.addProfilProviderListener( this );

    final IProfil newProfile = m_provider == null ? null : m_provider.getProfil();
    final ProfilViewData newViewData = m_provider == null ? null : m_provider.getViewData();

    onProfilProviderChanged( m_provider, oldProfile, newProfile, oldViewData, newViewData );
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#dispose()
   */
  @Override
  public void dispose( )
  {
    m_adapterPartListener.dispose();
 
    m_profilProviderPart = null;

    unhookProvider();

    super.dispose();
  }

  /**
   * @see org.eclipse.ui.IWorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public final void createPartControl( final Composite parent )
  {
    m_control = new Composite( parent, SWT.NONE );
    final GridLayout gridLayout = new GridLayout();
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    m_control.setLayout( gridLayout );

    onProfilProviderChanged( m_provider, null, m_profile, null, getProfilViewData() );
  }

  /**
   * @see com.bce.profil.ui.view.IProfilProviderListener#onProfilProviderChanged(com.bce.eind.core.profil.IProfilEventManager,
   *      com.bce.eind.core.profil.IProfilEventManager, com.bce.profil.ui.view.ProfilViewData,
   *      com.bce.profil.ui.view.ProfilViewData)
   */
  public void onProfilProviderChanged( final IProfilProvider2 provider, final IProfil oldProfile, final IProfil newProfile, final ProfilViewData oldViewData, final ProfilViewData newViewData )
  {
    unhookProvider();

    setPartNames( Messages.AbstractProfilViewPart2_0, Messages.AbstractProfilViewPart2_1 );

    m_profile = newProfile;
    m_viewData = newViewData;

    if( m_profile != null )
      m_profile.addProfilListener( this );

    if( m_viewData != null )
      m_viewData.addProfilViewDataListener( this );

    onProfilChanged();
  }

  /** Returns the part which provides the current profile. */
  public IWorkbenchPart getProfileProviderPart( )
  {
    return m_profilProviderPart;
  }

  private void setPartNames( final String partName, final String tooltip )
  {
    final Composite control = getControl();
    if( control != null && !control.isDisposed() )
    {
      final Runnable object = new Runnable()
      {
        public void run( )
        {
          if( !control.isDisposed() )
            setPartNamesInternal( partName, tooltip );
        }
      };
      control.getDisplay().asyncExec( object );
    }
  }

  protected void setPartNamesInternal( final String partName, final String tooltip )
  {
    setTitleToolTip( tooltip );
    setPartName( partName );
  }

  private void unhookProvider( )
  {
    saveState();

    if( m_profile != null )
    {
      m_profile.removeProfilListener( this );
      m_profile = null;
    }

    if( m_viewData != null )
    {
      m_viewData.removeProfilViewDataListener( this );
      m_viewData = null;
    }
  }

  @Override
  public void setFocus( )
  {
    m_control.setFocus();
  }

  /** Recreates the control */
  private final void onProfilChanged( )
  {
    // REMARK: this makes this code a bit more robust, if too many profil changes go in...
    // we quickly cancel still pending jobs and reschedule again (with a small schedule)
    m_updateProfilJob.cancel();
    m_updateProfilJob.schedule( 100 );
  }

  protected Composite getControl( )
  {
    return m_control;
  }

  public ProfilViewData getProfilViewData( )
  {
    return m_viewData;
  }

  /** Used internally. Must be called in the SWT-Thread. */
  protected void handleProfilChanged( )
  {
    if( m_control == null || m_control.isDisposed() )
      return;

    final String partName = m_profile == null ? Messages.AbstractProfilViewPart2_2 : Messages.AbstractProfilViewPart2_3 + " " + m_profile.getStation(); //$NON-NLS-1$
    final String tooltip = null;

    setPartNames( partName, tooltip );

    final Composite parent = getControl();
    if( parent == null || parent.isDisposed() )
      return;

    for( final Control c : parent.getChildren() )
      c.dispose();

    final Control control = createContent( parent );
    if( control != null )
      control.setLayoutData( new GridData( GridData.FILL_BOTH ) );
    parent.layout();
  }

  protected abstract Control createContent( final Composite parent );

  protected abstract void saveState( );

}