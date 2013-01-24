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

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.model.wspm.ui.view.chart.IProfilChartLayer;
import org.kalypso.model.wspm.ui.view.chart.ProfilChartModel;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.ILayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.event.impl.AbstractLayerManagerEventListener;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;

/**
 * @author kimwerner
 */
public class LayerViewPart extends AbstractChartModelViewPart
{
  private Composite m_parent = null;

  private final ILayerManagerEventListener m_layerManagerEventListener = new AbstractLayerManagerEventListener()
  {
    @Override
    public void onActivLayerChanged( final IChartLayer layer )
    {
      final Runnable runnable = new Runnable()
      {
        @Override
        public void run( )
        {
          updateControl();
        }
      };

      ControlUtils.syncExec( getParent(), runnable );
    }
  };

  private ScrolledForm m_form;


  /**
   * @see org.kalypso.model.wspm.ui.view.AbstractChartModelViewPart#doCreateControl(org.eclipse.swt.widgets.Composite,
   *      org.eclipse.ui.forms.widgets.FormToolkit)
   */
  @Override
  protected Control doCreateControl( final Composite parent, final FormToolkit toolkit )
  {
    m_form = toolkit.createScrolledForm( parent );
    m_form.setExpandHorizontal( false );
    m_form.setExpandHorizontal( true );
    toolkit.decorateFormHeading( m_form.getForm() );

    m_parent = m_form.getBody();
    m_parent.setLayout( new FillLayout() );

    modelChanged( null );

    updateControl();

    return m_parent;
  }

  /**
   * @see org.eclipse.ui.part.WorkbenchPart#dispose()
   */
  @Override
  public void dispose( )
  {
    final ILayerManager lm = getChartModel() == null ? null : getChartModel().getLayerManager();
    if( lm != null )
      lm.removeListener( m_layerManagerEventListener );

    super.dispose();
  }

  private final IChartLayer getActiveLayer( )
  {
    final IChartModel model = getChartModel();
    final ILayerManager mngr = model == null ? null : model.getLayerManager();
    if( mngr != null )
    {
      for( final IChartLayer layer : mngr.getLayers() )
      {
        if( layer.isActive() )
          return layer;
      }
    }
    return null;
  }

  protected Composite getParent( )
  {
    return m_parent;
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.AbstractChartModelView#updateControl()
   */
  @Override
  protected void updateControl( )// final Form form )
  {
    if( m_parent == null || m_parent.isDisposed() )
      return;

    for( final Control ctrl : m_parent.getChildren() )
      ctrl.dispose();

    final IChartLayer activeLayer = getActiveLayer();
    IChartModel model = getChartModel();
    if( model != null && model instanceof ProfilChartModel && ((ProfilChartModel) model).getProfil() == null )
      model = null;

    updatePartName( model, activeLayer == null ? null : activeLayer.getTitle(), m_form.getForm() );

    if( activeLayer == null )
      return;

    final IProfilView panel = activeLayer instanceof IProfilChartLayer ? ((IProfilChartLayer) activeLayer).createLayerPanel() : null;

    if( panel != null )
    {
      panel.createControl( m_parent, getToolkit() );
      m_parent.layout();
    }
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.AbstractChartModelView#modelChanged(de.openali.odysseus.chart.framework.model.IChartModel)
   */
  @Override
  protected void modelChanged( final IChartModel oldModel )
  {
    final ILayerManager oldLm = oldModel == null ? null : oldModel.getLayerManager();
    final ILayerManager lm = getChartModel() == null ? null : getChartModel().getLayerManager();
    if( oldLm != null )
      oldLm.removeListener( m_layerManagerEventListener );
    if( lm != null )
      lm.addListener( m_layerManagerEventListener );

    final Runnable runnable = new Runnable()
    {
      @Override
      public void run( )
      {
        updateControl();
      }
    };

    ControlUtils.asyncExec( m_parent, runnable );
  }

}
