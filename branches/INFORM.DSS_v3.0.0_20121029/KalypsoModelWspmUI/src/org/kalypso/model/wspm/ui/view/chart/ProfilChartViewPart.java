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
package org.kalypso.model.wspm.ui.view.chart;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.ChartPartListener;
import org.kalypso.model.wspm.core.gml.IProfileSelection;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIExtensions;
import org.kalypso.model.wspm.ui.dialog.compare.ProfileChartComposite;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.IProfileSelectionListener;
import org.kalypso.model.wspm.ui.view.ProfileFeatureSeletionHandler;
import org.kalypso.ogc.gml.selection.IFeatureSelection;

import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IChartView;

/**
 * @author kimwerner
 */
public class ProfilChartViewPart extends ViewPart implements IChartPart, IProfileSelectionListener
{
  public static final String ID = "org.kalypso.model.wspm.ui.view.chart.ChartView"; //$NON-NLS-1$

  private Composite m_control;

  private ProfileChartComposite m_profilChartComposite;

  private FormToolkit m_toolkit;

  private Form m_form;

  private ChartPartListener m_partListener = null;

  private final ProfileFeatureSeletionHandler m_handler = new ProfileFeatureSeletionHandler( this );

  protected Control createContent( final Composite parent )
  {
    if( parent == null )
      return null;
    if( m_toolkit == null )
    {
      m_toolkit = new FormToolkit( parent.getDisplay() );
    }

    m_partListener.setChart( null );

    // FIXME: refaktor out a ProfileChartForm
    if( m_form == null )
    {
      m_form = m_toolkit.createForm( parent );
      m_form.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
      m_form.getBody().setLayout( new FillLayout() );
      m_toolkit.decorateFormHeading( m_form );

      // TODO: we have now access to the profile source and hence to the reach -> we could show prev/next buttons or do other things with the profile container
      final IProfileSelection profileSelection = m_handler.getProfileSelection();
      m_profilChartComposite = new ProfileChartComposite( m_form.getBody(), parent.getStyle(), getProfilLayerProvider(), profileSelection );
      m_partListener.setChart( m_profilChartComposite );
    }

    return m_profilChartComposite;
  }

  @Override
  public final void createPartControl( final Composite parent )
  {
    m_control = new Composite( parent, SWT.NONE );
    m_control.setLayout( GridLayoutFactory.fillDefaults().create() );

    createContent( m_control );

    final IProfileSelection selection = m_handler.getProfileSelection();

    handleProfilSourceChanged( selection );
  }

  @Override
  public void dispose( )
  {
    m_handler.dispose();

    if( m_partListener != null )
    {
      m_partListener.dispose();
      getSite().getPage().removePartListener( m_partListener );
      m_partListener = null;
    }

    if( m_profilChartComposite != null )
    {
      m_profilChartComposite.dispose();
      m_profilChartComposite = null;
    }

    if( m_form != null )
    {
      m_form.dispose();
    }

    m_form = null;
    m_profilChartComposite = null;

    if( m_toolkit != null )
    {
      m_toolkit.dispose();
      m_toolkit = null;
    }

    super.dispose();
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    if( IChartPart.class.equals( adapter ) )
      return this;
    if( IChartView.class.equals( adapter ) )
      return this;

    return super.getAdapter( adapter );
  }

  @Override
  public IChartComposite getChartComposite( )
  {
    return m_profilChartComposite;
  }

  protected Composite getControl( )
  {
    return m_control;
  }

  protected IProfilLayerProvider getProfilLayerProvider( )
  {
    if( m_profilChartComposite == null )
      return null;

    final IProfileSelection profilSelection = m_profilChartComposite.getProfileSelection();
    if( profilSelection == null )
      return null;

    final IProfile profile = profilSelection.getProfile();
    if( profile == null )
      return null;

    return KalypsoModelWspmUIExtensions.createProfilLayerProvider( profile.getType() );
  }

  @Override
  public void init( final IViewSite site ) throws PartInitException
  {
    super.init( site );

    m_handler.doInit( site );

    final IWorkbenchPage page = site.getPage();

    m_partListener = new ChartPartListener( this, site );
    page.addPartListener( m_partListener );
  }

  @Override
  public final void handleProfilSourceChanged( final IProfileSelection selection )
  {
    setChartModel( selection );
  }

  private void setChartModel( final IProfileSelection newProfileSelection )
  {
    updateMessages( newProfileSelection.getProfile() );

    final ProfileChartComposite chartComposite = m_profilChartComposite;
    if( chartComposite == null )
      return;

    /* If no reference changed, do nothing. The chart itself reacts to inner profile changes */
    final IProfile newProfile = newProfileSelection.getProfile();

    final IProfileSelection profileSelection = chartComposite.getProfileSelection();
    final IProfile profile = profileSelection != null ? profileSelection.getProfile() : null;
    if( ObjectUtils.equals( newProfile, profile ) )
      return;

    if( !chartComposite.isDisposed() )
      chartComposite.setProfileSelection( newProfileSelection );
  }

  private void updateMessages( final IProfile newProfile )
  {
    if( newProfile == null )
    {
      setFormMessage( Messages.getString( "org.kalypso.model.wspm.ui.view.chart.ChartView.0" ), IMessageProvider.INFORMATION ); //$NON-NLS-1$
      setPartNames( Messages.getString( "org.kalypso.model.wspm.ui.view.AbstractProfilViewPart_1" ), Messages.getString( "org.kalypso.model.wspm.ui.view.AbstractProfilViewPart_2" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }
    else
    {
      setFormMessage( null, IMessageProvider.NONE );
      setPartNames( Messages.getString( "ProfilChartViewPart.1", newProfile.getStation() ), newProfile.getDescription() ); //$NON-NLS-1$
    }
  }

  @Override
  public void setFocus( )
  {
    m_control.setFocus();
  }

  private void setFormMessage( final String message, final int type )
  {
    if( m_form == null || m_form.isDisposed() )
      return;

    final Display display = m_form.getDisplay();
    if( display.isDisposed() )
      return;

    final Form form = m_form;
    final Runnable runnable = new Runnable()
    {
      @Override
      public void run( )
      {
        if( !form.isDisposed() )
        {
          form.setMessage( message, type );
        }
      }
    };

    display.asyncExec( runnable );
  }

  private void setPartNames( final String partName, final String tooltip )
  {
    final Composite control = getControl();
    if( control != null && !control.isDisposed() )
    {
      final Runnable object = new Runnable()
      {
        @Override
        public void run( )
        {
          if( !control.isDisposed() )
          {
            setPartNamesInternal( partName, tooltip );
          }
        }
      };
      control.getDisplay().asyncExec( object );
    }
  }

  @Override
  public void setAdapter( final IWorkbenchPart part, final IFeatureSelection selection )
  {
    m_handler.setAdapter( part, selection );
  }

  protected void setPartNamesInternal( final String partName, final String tooltip )
  {
    setTitleToolTip( tooltip );
    setPartName( partName );
  }
}