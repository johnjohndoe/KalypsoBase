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
package org.kalypso.model.wspm.ui.view.chart;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.chart.ui.editor.ChartPartListener;
import org.kalypso.chart.ui.editor.mousehandler.PlotDragHandlerDelegate;
import org.kalypso.contribs.eclipse.ui.partlistener.AdapterPartListener;
import org.kalypso.contribs.eclipse.ui.partlistener.EditorFirstAdapterFinder;
import org.kalypso.contribs.eclipse.ui.partlistener.IAdapterEater;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIExtensions;
import org.kalypso.model.wspm.ui.dialog.compare.ProfileChartComposite;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.IProfilProvider;
import org.kalypso.model.wspm.ui.profil.IProfilProviderListener;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.IChartModelEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.ChartModelEventHandler;
import de.openali.odysseus.chart.framework.view.IChartComposite;
import de.openali.odysseus.chart.framework.view.IChartView;

/**
 * @author kimwerner
 */
public class ProfilChartViewPart extends ViewPart implements IChartPart, IProfilProviderListener, IAdapterEater<IProfilProvider>
{
  public static final String ID = "org.kalypso.model.wspm.ui.view.chart.ChartView"; //$NON-NLS-1$

  private final AdapterPartListener<IProfilProvider> m_adapterPartListener = new AdapterPartListener<IProfilProvider>( IProfilProvider.class, this, new EditorFirstAdapterFinder<IProfilProvider>(), new EditorFirstAdapterFinder<IProfilProvider>() );

  private Composite m_control;

  private ProfileChartComposite m_profilChartComposite;

  private IProfilProvider m_provider;

  private final ChartModelEventHandler m_chartModelEventHandler = new ChartModelEventHandler();

  private PlotDragHandlerDelegate m_plotDragHandler = null;

  private FormToolkit m_toolkit;

  private Form m_form;

  private ChartPartListener m_partListener = null;

  /**
   * @see de.openali.odysseus.chart.framework.model.event.IEventProvider#addListener(java.lang.Object)
   */
  @Override
  public void addListener( final IChartModelEventListener listener )
  {
    m_chartModelEventHandler.addListener( listener );
  }

  /**
   * @see com.bce.profil.eclipse.view.AbstractProfilViewPart2#createContent(org.eclipse.swt.widgets.Composite)
   */
  protected Control createContent( final Composite parent )
  {
    if( parent == null )
      return null;
    if( m_toolkit == null )
      m_toolkit = new FormToolkit( parent.getDisplay() );

    if( m_form == null )
    {
      m_form = m_toolkit.createForm( parent );
      m_form.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
      final GridLayout gridLayout = new GridLayout();
      gridLayout.marginWidth = 0;
      gridLayout.marginHeight = 0;
      m_form.setLayout( gridLayout );
      m_form.getBody().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
      m_form.getBody().setLayout( new GridLayout() );
      m_toolkit.decorateFormHeading( m_form );
      final IProfil profile = m_provider == null ? null : m_provider.getProfil();
      m_profilChartComposite = new ProfileChartComposite( m_form.getBody(), parent.getStyle(), getProfilLayerProvider(), profile );
      m_profilChartComposite.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
      m_plotDragHandler = new PlotDragHandlerDelegate( m_profilChartComposite.getChart() );

    }
    return m_profilChartComposite;
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
    createContent( m_control );
    if( m_provider != null )
      onProfilProviderChanged( m_provider, null, m_provider.getProfil() );
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.AbstractProfilViewPart#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_partListener != null )
    {
      m_partListener.dispose();
      getSite().getPage().removePartListener( m_partListener );
      m_partListener = null;
    }

    if( m_provider != null )
      m_provider.removeProfilProviderListener( this );

    if( m_adapterPartListener != null )
      m_adapterPartListener.dispose();

    if( m_profilChartComposite != null )
      m_profilChartComposite.dispose();

    if( m_form != null )
      m_form.dispose();

    m_form = null;
    m_profilChartComposite = null;
    m_toolkit = null;
    m_provider = null;

    super.dispose();
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
   */
  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    if( IChartPart.class.equals( adapter ) )
      return this;
    if( IChartView.class.equals( adapter ) )
      return this;

    return super.getAdapter( adapter );
  }

 
  /**
   * @see org.kalypso.chart.ui.IChartPart#getChartComposite()
   */
  @Override
  public IChartComposite getChartComposite( )
  {

    return m_profilChartComposite;
  }

  protected Composite getControl( )
  {
    return m_control;
  }

  /**
   * @see org.kalypso.chart.ui.IChartPart#getOutlinePage()
   */
  @Override
  public IContentOutlinePage getOutlinePage( )
  {
    // TODO Auto-generated method stub
    return null;
  }

  /**
   * @see org.kalypso.chart.ui.IChartPart#getPlotDragHandler()
   */
  @Override
  public PlotDragHandlerDelegate getPlotDragHandler( )
  {
    return m_plotDragHandler;
  }

  protected IProfilLayerProvider getProfilLayerProvider( )
  {
    if( m_profilChartComposite == null || m_profilChartComposite.getProfil() == null )
      return null;
    return KalypsoModelWspmUIExtensions.createProfilLayerProvider( m_profilChartComposite.getProfil().getType() );
  }

  @Override
  public void init( final IViewSite site ) throws PartInitException
  {
    super.init( site );

    final IWorkbenchPage page = site.getPage();
    m_adapterPartListener.init( page );

    m_partListener = new ChartPartListener( this, site );
    page.addPartListener( m_partListener );
  }

  /**
   * @see com.bce.profil.ui.view.IProfilProviderListener#onProfilProviderChanged(com.bce.eind.core.profil.IProfilEventManager,
   *      com.bce.eind.core.profil.IProfilEventManager, com.bce.profil.ui.view.ProfilViewData,
   *      com.bce.profil.ui.view.ProfilViewData)
   */
  @Override
  public void onProfilProviderChanged( final IProfilProvider provider, final IProfil oldProfile, final IProfil newProfile )
  {
    setPartNames( Messages.getString( "org.kalypso.model.wspm.ui.view.AbstractProfilViewPart_1" ), Messages.getString( "org.kalypso.model.wspm.ui.view.AbstractProfilViewPart_2" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    if( newProfile == null )
    {
      setPartNames( Messages.getString( "org.kalypso.model.wspm.ui.view.AbstractProfilViewPart_1" ), Messages.getString( "org.kalypso.model.wspm.ui.view.AbstractProfilViewPart_2" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      setFormMessage( Messages.getString( "org.kalypso.model.wspm.ui.view.chart.ChartView.0" ), IMessageProvider.INFORMATION ); //$NON-NLS-1$
    }
    else
    {
      setFormMessage( null, IMessageProvider.NONE );
      setPartNames( String.format( Messages.getString( "ProfilChartViewPart.1" ), newProfile.getStation() ), Messages.getString( "org.kalypso.model.wspm.ui.view.AbstractProfilViewPart_2" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    }

    setChartModel( newProfile, provider == null ? null : provider.getResult() );

  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.IEventProvider#removeListener(java.lang.Object)
   */
  @Override
  public void removeListener( final IChartModelEventListener listener )
  {
    m_chartModelEventHandler.removeListener( listener );

  }

  @Override
  public void setAdapter( final IWorkbenchPart part, final IProfilProvider adapter )
  {
    if( adapter == m_provider )
      return;

    if( m_provider != null )
      m_provider.removeProfilProviderListener( this );

    m_provider = adapter;
    if( m_provider != null )
      m_provider.addProfilProviderListener( this );

    onProfilProviderChanged( m_provider, null, m_provider == null ? null : m_provider.getProfil() );
  }

  private void setChartModel( final IProfil newProfile, final Object result )
  {
    final ProfileChartComposite chartComposite = m_profilChartComposite;
    final ChartModelEventHandler chartModelEventHandler = m_chartModelEventHandler;

    if( chartComposite != null && !chartComposite.isDisposed() )
    {
      final Display display = chartComposite.getDisplay();
      if( !display.isDisposed() )
      {
        final Runnable runnable = new Runnable()
        {
          @Override
          public void run( )
          {
            if( !chartComposite.isDisposed() )
            {
              final IChartModel oldModel = chartComposite.getChartModel();
              chartComposite.setProfil( newProfile, result );
              chartModelEventHandler.fireModelChanged( oldModel, chartComposite.getChartModel() );
            }

          }
        };
        display.syncExec( runnable );
      }
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
          form.setMessage( message, type );
      }
    };
    display.syncExec( runnable );
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

}