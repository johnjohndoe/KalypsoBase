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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.part.ViewPart;
import org.kalypso.chart.ui.IChartPart;
import org.kalypso.contribs.eclipse.ui.partlistener.AdapterPartListener;
import org.kalypso.contribs.eclipse.ui.partlistener.EditorFirstAdapterFinder;
import org.kalypso.contribs.eclipse.ui.partlistener.IAdapterEater;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.view.chart.ProfilChartModel;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.IChartModelEventListener;
import de.openali.odysseus.chart.framework.view.impl.ChartComposite;

/**
 * @author kimwerner
 */
public abstract class AbstractChartModelView extends ViewPart implements IAdapterEater<IChartPart>, IChartModelEventListener
{
  private final AdapterPartListener<IChartPart> m_chartProviderListener = new AdapterPartListener<IChartPart>( IChartPart.class, this, EditorFirstAdapterFinder.<IChartPart> instance(), EditorFirstAdapterFinder.<IChartPart> instance() );

  private Form m_form;

  private FormToolkit m_toolkit;

  private IChartPart m_chartPart;

  private IChartModel m_chartModel;

  protected abstract void createControl( final Composite parent );

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#createPartControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createPartControl( final Composite parent )
  {
    m_toolkit = new FormToolkit( parent.getDisplay() );
    m_form = m_toolkit.createForm( parent );
    m_toolkit.decorateFormHeading( m_form );

    final GridLayout bodyLayout = new GridLayout();
    bodyLayout.marginHeight = 0;
    bodyLayout.marginWidth = 0;

    m_form.getBody().setLayout( bodyLayout );

    createControl( m_form.getBody() );

    updateControl( m_form );
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#dispose()
   */
  @Override
  public void dispose( )
  {
    getSite().setSelectionProvider( null );
    if( m_chartPart != null )
      m_chartPart.removeListener( this );
    m_toolkit.dispose();
    m_form.dispose();

    super.dispose();
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#getAdapter(java.lang.Class)
   */

  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    if( adapter == IChartModel.class )
      return getChartModel();
    if( adapter == IChartPart.class )
      return getChartPart();
    return super.getAdapter( adapter );
  }

  public IChartModel getChartModel( )
  {
    return m_chartModel;
  }

  public IChartPart getChartPart( )
  {
    return m_chartPart;
  }

  private String getStationName( final IChartModel model )
  {
    final IProfil profil = model instanceof ProfilChartModel ? ((ProfilChartModel) model).getProfil() : null;
    return profil == null ? null : String.format( "Station km %10.4f", profil.getStation() );
  }

  public FormToolkit getToolkit( )
  {
    return m_toolkit;
  }

  @Override
  public void init( final IViewSite site ) throws PartInitException
  {
    super.init( site );
    m_chartProviderListener.init( site.getPage() );

    updateControl( m_form );
  }

  protected abstract void modelChanged( final IChartModel oldModel );

  /**
   * @see de.openali.odysseus.chart.framework.model.event.IChartModelEventListener#onModelChanged(de.openali.odysseus.chart.framework.model.IChartModel,
   *      de.openali.odysseus.chart.framework.model.IChartModel)
   */
  @Override
  public void onModelChanged( final IChartModel oldModel, final IChartModel newModel )
  {
    m_chartModel = newModel;

    // FIXME: throws InvalidThreadAccess if not invoked from SWT-Thread, this is not good!

    modelChanged( oldModel );
  }

  /**
   * @see org.kalypso.contribs.eclipse.ui.partlistener.IAdapterEater#setAdapter(java.lang.Object)
   */

  @Override
  public void setAdapter( final IWorkbenchPart part, final IChartPart adapter )
  {
    if( adapter == m_chartPart )
      return;
    if( m_chartPart != null )
      m_chartPart.removeListener( this );
    m_chartPart = adapter;
    if( adapter != null )
    {
      adapter.addListener( this );
      final ChartComposite chart = adapter.getChartComposite();
      m_chartModel = chart == null ? null : chart.getChartModel();
    }
    updateControl( m_form );
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#setFocus()
   */
  @Override
  public void setFocus( )
  {
    if( m_form != null )
    {
      final Control control = m_form.getBody();
      if( control != null && !control.isDisposed() )
        control.setFocus();
    }
  }

  public void setForm( final Form form )
  {
    m_form = form;
  }

  /** Must be called in SWT thread */
  protected final void updateControl( )
  {
    updateControl( m_form );
  }

  /** Must be called in SWT thread */
  protected abstract void updateControl( final Form form );
  protected final void updatePartName( final Form form, final IChartModel model)
  {
    updatePartName(form,model,null);
  }

  protected final void updatePartName( final Form form, final IChartModel model,final String message  )
  {
    if( model == null )
    {
      form.setMessage( Messages.getString( "org.kalypso.model.wspm.ui.view.legend.LegendView.2" ), IMessageProvider.INFORMATION ); //$NON-NLS-1$
      setPartName( getPartName() );
    }
    else
    {
      form.setMessage( message );
      setPartName( getStationName( model ) );
    }
  }

}
