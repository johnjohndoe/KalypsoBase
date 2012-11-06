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
package org.kalypso.model.wspm.ui.featureview;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.progress.IProgressConstants;
import org.kalypso.contribs.eclipse.jface.action.CommandWithStyle;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ogc.gml.featureview.control.AbstractFeatureControl;
import org.kalypsodeegree.model.feature.Feature;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.impl.ChartModel;

/**
 * @author Gernot Belger
 */
class ChartFeatureControl extends AbstractFeatureControl
{
  private final IChartModel m_chartModel = new ChartModel();

  private final String m_featureKeyName;

  private final String m_configurationUrl;

  private final String m_chartName;

  private final CommandWithStyle[] m_commands;

  private ChartFeatureControlComposite m_chartControl;

  public ChartFeatureControl( final String featureKeyName, final Feature feature, final IPropertyType pt, final String configurationUrl, final String chartName, final CommandWithStyle[] commands )
  {
    super( feature, pt );

    m_featureKeyName = featureKeyName;
    m_configurationUrl = configurationUrl;
    m_chartName = chartName;
    m_commands = commands;
  }

  @Override
  public Control createControl( final Composite parent, final int style )
  {
    m_chartControl = new ChartFeatureControlComposite( m_chartModel, m_commands, parent, style );

    /* Update the controls. */
    updateControl();

    loadChart( parent );

    return m_chartControl;
  }

  @Override
  public boolean isValid( )
  {
    return true;
  }

  @Override
  public void updateControl( )
  {
  }

  @Override
  public void dispose( )
  {
    if( m_chartControl != null )
      m_chartControl.dispose();

    m_chartModel.dispose();

    super.dispose();
  }

  private void loadChart( final Composite parent )
  {
    // remember the feature from the feature control
    final Feature chartFeature = ChartFeatureControlComposite.getChartFeature( getFeature(), getFeatureTypeProperty() );
    m_chartModel.setData( m_featureKeyName, chartFeature );

    final ChartLoaderJob loader = new ChartLoaderJob( m_chartModel, m_configurationUrl, m_chartName );
    loader.addJobChangeListener( new JobChangeAdapter()
    {
      @Override
      public void done( final IJobChangeEvent event )
      {
        handleChartLoaded( event.getResult(), parent );
      }
    } );

    loader.setUser( false );
    loader.setProperty( IProgressConstants.KEEP_PROPERTY, Boolean.FALSE );
    loader.setProperty( IProgressConstants.NO_IMMEDIATE_ERROR_PROMPT_PROPERTY, Boolean.TRUE );
    loader.schedule( 100 );
  }

  protected void handleChartLoaded( final IStatus result, final Composite parent )
  {
    if( parent.isDisposed() )
      return;

    final Display display = parent.getDisplay();
    display.asyncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        if( !parent.isDisposed() )
          onChartLoaded( parent, result );
      }
    } );
  }

  protected void onChartLoaded( final Composite parent, final IStatus result )
  {
    if( m_chartControl == null )
      return;

    if( !result.isOK() )
      m_chartControl.addStatus( result );
    else
    {
      handleChartLoaded( m_chartModel );

      hideUnusedTabs( parent );
    }
  }

  /**
   * Allows client to tweak chart after it was loaded
   */
  protected void handleChartLoaded( @SuppressWarnings( "unused" ) final IChartModel chartModel )
  {
  }

  private void hideUnusedTabs( final Composite parent )
  {
    /* if we have data, nothing to do */
    if( m_chartControl.hasData() )
      return;

    /* hindnig only works for tab folders */
    if( !(parent instanceof TabFolder) )
      return;

    final TabFolder folder = (TabFolder)parent;
    final TabItem[] items = folder.getItems();
    for( final TabItem tabItem : items )
    {
      final Control control = tabItem.getControl();
      if( control == m_chartControl )
      {
        control.dispose();
        tabItem.dispose();
      }
    }
  }
}