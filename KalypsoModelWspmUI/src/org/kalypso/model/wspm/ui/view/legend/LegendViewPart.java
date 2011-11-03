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
package org.kalypso.model.wspm.ui.view.legend;

import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.chart.ui.editor.ChartEditorTreeOutlinePage;
import org.kalypso.chart.ui.editor.ChartTreeLabelProvider;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.model.wspm.ui.view.AbstractChartModelViewPart;
import org.kalypso.model.wspm.ui.view.chart.ProfilChartModel;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.layer.IChartLayer;
import de.openali.odysseus.chart.framework.model.layer.ILayerManager;

/**
 * This view shows the profile legend. It always shows the legend of the last active part which adapts to
 * {@link org.kalypso.model.wspm.ui.profil.view.chart}.
 * <p>
 * It is also a selection provider of its selected layers.
 * </p>
 * 
 * @author Gernot Belger
 * @author kimwerner
 */
public class LegendViewPart extends AbstractChartModelViewPart
{
  private final ChartEditorTreeOutlinePage m_chartlegend = new ChartEditorTreeOutlinePage( new ProfilChartEditorTreeContentProvider(), new ChartTreeLabelProvider() );

  private Form m_form;

  /**
   * @see org.kalypso.model.wspm.ui.view.AbstractChartModelViewPart#doCreateControl(org.eclipse.swt.widgets.Composite,
   *      org.eclipse.ui.forms.widgets.FormToolkit)
   */
  @Override
  protected Control doCreateControl( final Composite parent, final FormToolkit toolkit )
  {
    m_form = toolkit.createForm( parent );
    toolkit.decorateFormHeading( m_form );

    final Composite body = m_form.getBody();
    body.setLayout( new FillLayout() );

    m_chartlegend.createControl( body );

    updateControl();

    return body;
  }

  @Override
  public Object getAdapter( @SuppressWarnings("rawtypes") final Class adapter )
  {
    if( adapter == ChartEditorTreeOutlinePage.class )
    {
      return m_chartlegend;
    }

    return super.getAdapter( adapter );
  }

  private final void setSelectedLayer( final IChartModel model )
  {
    final ILayerManager lm = model == null ? null : model.getLayerManager();
    if( lm == null )
      return;

    for( final IChartLayer layer : model.getLayerManager().getLayers() )
    {
      if( layer.isActive() )
      {
        m_chartlegend.selectLayer( layer );
        break;
      }
    }
  }

  @Override
  public void updateControl( )
  {
    if( m_chartlegend == null )
      return;
    IChartModel model = getChartModel();
    m_chartlegend.setModel( model );
    setSelectedLayer( model );
    if( model != null && model instanceof ProfilChartModel && ((ProfilChartModel) model).getProfil() == null )
      model = null;
    updatePartName( model, null, m_form );
  }

  /**
   * @see org.kalypso.model.wspm.ui.view.AbstractChartModelView#modelChanged(de.openali.odysseus.chart.framework.model.IChartModel)
   */
  @Override
  protected void modelChanged( final IChartModel oldModel )
  {
    final Runnable runnable = new Runnable()
    {
      @Override
      public void run( )
      {
        updateControl();
      }
    };

    final Control control = m_chartlegend.getControl();
    ControlUtils.asyncExec( control, runnable );
  }

}