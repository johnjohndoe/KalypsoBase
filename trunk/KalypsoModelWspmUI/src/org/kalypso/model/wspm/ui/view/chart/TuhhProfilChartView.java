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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.RGB;
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
import org.kalypso.chart.ui.editor.mousehandler.AxisDragHandlerDelegate;
import org.kalypso.chart.ui.editor.mousehandler.PlotDragHandlerDelegate;
import org.kalypso.contribs.eclipse.ui.partlistener.AdapterPartListener;
import org.kalypso.contribs.eclipse.ui.partlistener.EditorFirstAdapterFinder;
import org.kalypso.contribs.eclipse.ui.partlistener.IAdapterEater;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.IProfilProvider;
import org.kalypso.model.wspm.ui.profil.IProfilProviderListener;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.IChartModelEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.ChartModelEventHandler;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.IExpandableChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;
import de.openali.odysseus.chart.framework.view.IChartView;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author kimwerner
 */
public class TuhhProfilChartView extends ViewPart implements IChartPart, IProfilProviderListener, IAdapterEater<IProfilProvider>
{
  public static final String ID = "org.kalypso.model.wspm.ui.view.chart.ChartView"; //$NON-NLS-1$

  private final AdapterPartListener<IProfilProvider> m_adapterPartListener = new AdapterPartListener<IProfilProvider>( IProfilProvider.class, this, new EditorFirstAdapterFinder<IProfilProvider>(), new EditorFirstAdapterFinder<IProfilProvider>() );

  private Composite m_control;

  private ChartComposite m_chartComposite;

  private IProfilProvider m_provider;

  private final ChartModelEventHandler m_chartModelEventHandler = new ChartModelEventHandler();

  private AxisDragHandlerDelegate m_axisDragHandler = null;

  private PlotDragHandlerDelegate m_plotDragHandler = null;

  private FormToolkit m_toolkit;

  private Form m_form;

  @Override
  public void init( final IViewSite site ) throws PartInitException
  {
    super.init( site );

    final IWorkbenchPage page = site.getPage();
    m_adapterPartListener.init( page );
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
   * @see com.bce.profil.ui.view.IProfilProviderListener#onProfilProviderChanged(com.bce.eind.core.profil.IProfilEventManager,
   *      com.bce.eind.core.profil.IProfilEventManager, com.bce.profil.ui.view.ProfilViewData,
   *      com.bce.profil.ui.view.ProfilViewData)
   */
  @Override
  public void onProfilProviderChanged( final IProfilProvider provider, final IProfil oldProfile, final IProfil newProfile )
  {
    setPartNames( Messages.getString( "org.kalypso.model.wspm.ui.view.AbstractProfilViewPart_1" ), Messages.getString( "org.kalypso.model.wspm.ui.view.AbstractProfilViewPart_2" ) ); //$NON-NLS-1$ //$NON-NLS-2$
    if( m_chartComposite == null )
      return;

    final IChartModel oldModel = m_chartComposite.getChartModel();
    if( oldModel instanceof ProfilChartModel )
      ((ProfilChartModel) oldModel).dispose();

    final ProfilChartModel newModel = newProfile == null ? null : new ProfilChartModel( newProfile, provider.getResult() );

    String activeLayerId = null;
    List<Object> positions = null;
    final Map<String, Boolean> visibility = new HashMap<String, Boolean>();

    if( oldModel != null )
    {
      activeLayerId = saveStateActive( oldModel.getLayerManager() );
      saveStateVisible( oldModel.getLayerManager(), visibility );
      positions = saveStatePosition( oldModel.getLayerManager() );
    }

    if( newModel == null )
    {
      setPartNames( Messages.getString( "org.kalypso.model.wspm.ui.view.AbstractProfilViewPart_1" ), Messages.getString( "org.kalypso.model.wspm.ui.view.AbstractProfilViewPart_2" ) ); //$NON-NLS-1$ //$NON-NLS-2$
      setFormMessage( Messages.getString( "org.kalypso.model.wspm.ui.view.chart.ChartView.0" ), IMessageProvider.INFORMATION ); //$NON-NLS-1$
    }
    else
    {
      restoreStateActive( newModel.getLayerManager(), activeLayerId );
      restoreStatePosition( newModel.getLayerManager(), positions );
      restoreStateVisible( newModel.getLayerManager(), visibility );
      setFormMessage( null, IMessageProvider.NONE );
      setPartNames( String.format( "Station km %10.4f", newProfile.getStation() ), Messages.getString( "org.kalypso.model.wspm.ui.view.AbstractProfilViewPart_2" ) ); //$NON-NLS-1$
      newModel.maximize();
    }

    setChartModel( oldModel, newModel );
  }

  private void setChartModel( final IChartModel oldModel, final ProfilChartModel newModel )
  {
    final ChartComposite chartComposite = m_chartComposite;
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
              chartComposite.setChartModel( newModel );
            chartModelEventHandler.fireModelChanged( oldModel, newModel );
          }
        };
        display.syncExec( runnable );
      }
    }

  }

  private void setFormMessage( final String message, final int type )
  {
    if( m_form.isDisposed() )
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

  @Override
  public void setFocus( )
  {
    m_control.setFocus();
  }

  private final void saveStateVisible( final ILayerManager mngr, final Map<String, Boolean> map )
  {
    for( final IChartLayer layer : mngr.getLayers() )
    {
      map.put( layer.getId(), layer.isVisible() );
      if( layer instanceof IExpandableChartLayer )
      {
        saveStateVisible( ((IExpandableChartLayer) layer).getLayerManager(), map );
      }
    }
  }

  private final List<Object> saveStatePosition( final ILayerManager mngr )
  {
    final List<Object> list = new ArrayList<Object>();

    for( final IChartLayer layer : mngr.getLayers() )
    {
      list.add( layer.getId() );
      if( layer instanceof IExpandableChartLayer )
      {
        final List<Object> subList = saveStatePosition( ((IExpandableChartLayer) layer).getLayerManager() );
        list.add( subList );
      }
    }

    return list;
  }

  private final String saveStateActive( final ILayerManager mngr )
  {
    for( final IChartLayer layer : mngr.getLayers() )
    {
      if( layer.isActive() )
        return layer.getId();
    }
    return ""; //$NON-NLS-1$
  }

  private final void restoreStateVisible( final ILayerManager mngr, final Map<String, Boolean> map )
  {
    for( final IChartLayer layer : mngr.getLayers() )
    {
      final Boolean visible = map.get( layer.getId() );
      if( visible != null )
        layer.setVisible( visible );
      if( layer instanceof IExpandableChartLayer )
      {
        restoreStateVisible( ((IExpandableChartLayer) layer).getLayerManager(), map );
      }
    }
  }

  @SuppressWarnings("unchecked")
  private final void restoreStatePosition( final ILayerManager mngr, final List<Object> list )
  {
    if( mngr == null || list == null )
      return;

    int pos = 0;
    for( final Object o : list )
    {
      if( o instanceof List )
      {
        final List<Object> l = (List<Object>) o;
        if( !l.isEmpty() )
        {
          final Object id = l.get( 0 );
          final IChartLayer layer = id == null ? null : mngr.getLayerById( id.toString() );
          if( layer != null )
          {
            mngr.moveLayerToPosition( layer, pos++ );
            if( layer instanceof IExpandableChartLayer )
            {
              restoreStatePosition( ((IExpandableChartLayer) layer).getLayerManager(), l );
            }
          }
        }
      }
      else
      {
        final IChartLayer layer = mngr.getLayerById( o.toString() );
        if( layer != null )
        {
          mngr.moveLayerToPosition( layer, pos++ );
        }
      }
    }
  }

  private final void restoreStateActive( final ILayerManager mngr, final String id )
  {
    final IChartLayer layer = mngr.getLayerById( id );
    if( layer != null )
    {
      layer.setActive( true );
      return;
    }

// old active Layer removed
    if( mngr.getLayers().length > 0 )
    {
      mngr.getLayers()[0].setActive( true );
    }
  }

  protected Composite getControl( )
  {
    return m_control;
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
      m_chartComposite = new ChartComposite( m_form.getBody(), parent.getStyle(), null, new RGB( 255, 255, 255 ) );
      m_axisDragHandler = new AxisDragHandlerDelegate( m_chartComposite );
      m_plotDragHandler = new PlotDragHandlerDelegate( m_chartComposite );

    }
    return m_chartComposite;
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.AbstractProfilViewPart#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_provider != null )
    {
      m_provider.removeProfilProviderListener( this );
      m_provider = null;
    }

    if( m_adapterPartListener != null )
      m_adapterPartListener.dispose();

    if( m_chartComposite != null )
    {
      final IChartModel chartModel = m_chartComposite.getChartModel();
      if( chartModel instanceof ProfilChartModel )
        ((ProfilChartModel) chartModel).dispose();
    }

    if( m_form != null )
      m_form.dispose();

    m_form = null;
    m_chartComposite = null;
    m_toolkit = null;

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
  public ChartComposite getChartComposite( )
  {

    return m_chartComposite;
  }

  /**
   * @see org.kalypso.chart.ui.IChartPart#getAxisDragHandler()
   */
  @Override
  public AxisDragHandlerDelegate getAxisDragHandler( )
  {
    return m_axisDragHandler;
  }

  /**
   * @see org.kalypso.chart.ui.IChartPart#getPlotDragHandler()
   */
  @Override
  public PlotDragHandlerDelegate getPlotDragHandler( )
  {
    return m_plotDragHandler;
  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.IEventProvider#addListener(java.lang.Object)
   */
  @Override
  public void addListener( final IChartModelEventListener listener )
  {
    m_chartModelEventHandler.addListener( listener );

  }

  /**
   * @see de.openali.odysseus.chart.framework.model.event.IEventProvider#removeListener(java.lang.Object)
   */
  @Override
  public void removeListener( final IChartModelEventListener listener )
  {
    m_chartModelEventHandler.removeListener( listener );

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

}