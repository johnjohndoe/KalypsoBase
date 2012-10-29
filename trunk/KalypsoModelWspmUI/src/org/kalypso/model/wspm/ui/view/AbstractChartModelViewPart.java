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

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.contribs.eclipse.ui.forms.ToolkitUtils;
import org.kalypso.contribs.eclipse.ui.partlistener.AdapterPartListener;
import org.kalypso.contribs.eclipse.ui.partlistener.EditorFirstAdapterFinder;
import org.kalypso.contribs.eclipse.ui.partlistener.IAdapterEater;
import org.kalypso.model.wspm.core.gml.IProfileSelection;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.chart.ProfilChartModel;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.IChartModelEventListener;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * @author kimwerner
 */
public abstract class AbstractChartModelViewPart extends ViewPart implements IAdapterEater<IChartPart>, IChartModelView
{
  private final IChartModelEventListener m_chartListener = new IChartModelEventListener()
  {
    @Override
    public void onModelChanged( final IChartModel oldModel, final IChartModel newModel )
    {
      handleModelChanged( oldModel, newModel );
    }
  };

  private final AdapterPartListener<IChartPart> m_chartProviderListener = new AdapterPartListener<>( IChartPart.class, this, EditorFirstAdapterFinder.<IChartPart> instance(), EditorFirstAdapterFinder.<IChartPart> instance() );

  private IChartPart m_chartPart;

  private IChartModel m_chartModel;

  private String m_registeredName;

  private Control m_control;

  private FormToolkit m_toolkit;

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public final void createPartControl( final Composite parent )
  {
    m_toolkit = ToolkitUtils.createToolkit( parent );

    m_control = doCreateControl( parent, m_toolkit );
  }

  protected abstract Control doCreateControl( Composite parent, FormToolkit toolkit );

  protected FormToolkit getToolkit( )
  {
    return m_toolkit;
  }

  @Override
  public void dispose( )
  {
    getSite().setSelectionProvider( null );

    if( m_chartPart != null )
    {
      final IChartComposite chart = m_chartPart.getChartComposite();
      if( chart != null )
        chart.removeListener( m_chartListener );
    }

    super.dispose();
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    if( adapter == IChartModel.class )
      return getChartModel();
    if( adapter == IChartPart.class )
      return getChartPart();
    return super.getAdapter( adapter );
  }

  @Override
  public IChartModel getChartModel( )
  {
    return m_chartModel;
  }

  public IChartPart getChartPart( )
  {
    return m_chartPart;
  }

  protected final void updatePartName( final IChartModel model, final String message, final Form form )
  {
    if( form == null || form.isDisposed() )
      return;

    final String stationName = getStationName( model );
    if( stationName == null )
    {
      setPartName( m_registeredName );
      form.setMessage( Messages.getString( "org.kalypso.model.wspm.ui.view.legend.LegendView.2" ), IMessageProvider.INFORMATION ); //$NON-NLS-1$
    }
    else
    {
      form.setMessage( message );
      setPartName( stationName );
    }
  }

  private String getStationName( final IChartModel model )
  {
    if( !(model instanceof ProfilChartModel) )
      return null;

    final IProfileSelection profileSelection = ((ProfilChartModel)model).getProfileSelection();
    if( profileSelection == null )
      return null;

    final IProfile profile = profileSelection.getProfile();
    if( profile == null )
      return null;

    return String.format( Messages.getString( "AbstractChartModelViewPart.0" ), profile.getStation() ); //$NON-NLS-1$
  }

  @Override
  public void init( final IViewSite site ) throws PartInitException
  {
    super.init( site );

    if( site == null )
      return;

    m_chartProviderListener.init( site.getPage() );
    m_registeredName = site.getRegisteredName();
    updateControl();
  }

  @Override
  public void setAdapter( final IWorkbenchPart part, final IChartPart adapter )
  {
    if( adapter == m_chartPart )
      return;

    final IChartModel oldModel = m_chartModel;

    if( m_chartPart != null )
    {
      final IChartComposite chartComposite = m_chartPart.getChartComposite();
      if( chartComposite != null )
        chartComposite.removeListener( m_chartListener );
    }

    m_chartPart = adapter;

    if( adapter != null )
    {
      final IChartComposite chart = adapter.getChartComposite();
      if( chart == null )
        m_chartModel = null;
      else
      {
        m_chartModel = chart.getChartModel();
        chart.addListener( m_chartListener );
      }
    }

    handleModelChanged( oldModel, m_chartModel );

    // updateControl();
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus( )
  {
    if( m_control != null && !m_control.isDisposed() )
    {
      m_control.setFocus();
    }
  }

  protected void handleModelChanged( final IChartModel oldModel, final IChartModel newModel )
  {
    m_chartModel = newModel;

    final Runnable runnable = new Runnable()
    {
      @Override
      public void run( )
      {
        modelChanged( oldModel );
      }
    };

    ControlUtils.asyncExec( m_control, runnable );
  }

  protected abstract void modelChanged( final IChartModel oldModel );

  /** Must be called in SWT thread */
  protected abstract void updateControl( );
}
