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
package org.kalypso.chart.ui.editor.commandhandler;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.contexts.IContextActivation;
import org.eclipse.ui.contexts.IContextService;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.services.IEvaluationService;
import org.eclipse.ui.services.IServiceLocator;
import org.eclipse.ui.services.IServiceWithSources;
import org.kalypso.contribs.eclipse.core.runtime.jobs.MutexRule;
import org.kalypso.contribs.eclipse.ui.commands.CommandUtilities;

import de.openali.odysseus.chart.framework.model.IChartModel;
import de.openali.odysseus.chart.framework.model.event.IChartModelEventListener;
import de.openali.odysseus.chart.framework.view.IChartComposite;

/**
 * Manages context and sources corresponding to the chart.<br>
 * As soon as the manager is created on a chart, the chart-context is activated and registered with the given
 * serviceLocator. Also, the chart is provides as source via the evaluation context.<br>
 * 
 * @author Gernot Belger
 */
public class ChartSourceProvider extends AbstractSourceProvider
{
  // FIXME We need more events of state changes (link, zoom, handler state changed)...
  private final IChartModelEventListener m_listener = new IChartModelEventListener()
  {
    @Override
    public void onModelChanged( final IChartModel oldModel, final IChartModel newModel )
    {
      fireSourceChanged();
    }
  };

  /**
   * ID of the registered context of the chart (see extension-point <code>org.eclipse.ui.contexts</code>).
   */
  public static final String CHART_CONTEXT = "de.openali.odysseus.chart.ui.context"; //$NON-NLS-1$

  public static final String CHART_COMMAND_CATEGORY = "org.kalypso.chart.ui.commands.category"; //$NON-NLS-1$

  public static final String ACTIVE_CHART_NAME = "activeChart"; //$NON-NLS-1$

  private static final String[] PROVIDED_SOURCE_NAMES = new String[] { ACTIVE_CHART_NAME };

  /**
   * This collection contains all services, with which this provider has been registered. Used in order to correctly
   * unregister.
   */
  private final Collection<IServiceWithSources> m_registeredServices = new HashSet<>();

  /** Ensures, that the context are activated in the same order as the themes are activated. */
  private final ISchedulingRule m_mutexRule = new MutexRule();

  private IChartComposite m_chart;

  private final IContextActivation m_chartContext;

  private final IServiceLocator m_serviceLocator;

  /**
   * Creates a new {@link ChartSourceProvider} on the given chart.<br>
   * Initializes it state with the given parameters.
   */
  public ChartSourceProvider( final IServiceLocator serviceLocator, final IChartComposite chart )
  {
    m_serviceLocator = serviceLocator;
    m_chart = chart;
    if( m_chart != null )
      m_chart.addListener( m_listener );

    // FIXME: refaktor into helper class
    final IContextService contextService = (IContextService)registerServiceWithSources( serviceLocator, IContextService.class );
    registerServiceWithSources( serviceLocator, IEvaluationService.class );
    registerServiceWithSources( serviceLocator, IHandlerService.class );
    registerServiceWithSources( serviceLocator, IMenuService.class );

    m_chartContext = contextService.activateContext( CHART_CONTEXT );

    // FIXME: check other source providers; they should do the same -> refaktor into helper class
    refreshUIelements();
  }

  private IServiceWithSources registerServiceWithSources( final IServiceLocator serviceLocator, final Class< ? extends IServiceWithSources> serviceClass )
  {
    final IServiceWithSources service = (IServiceWithSources)serviceLocator.getService( serviceClass );
    if( service == null )
      return null;

    service.addSourceProvider( this );
    m_registeredServices.add( service );

    return service;
  }

  @Override
  public void dispose( )
  {
    if( m_chart != null )
      m_chart.removeListener( m_listener );

    m_chart = null;

    fireSourceChanged();

    // unregister the registered source provider
    for( final IServiceWithSources service : m_registeredServices )
      service.removeSourceProvider( this );

    if( m_chartContext != null )
      m_chartContext.getContextService().deactivateContext( m_chartContext );
  }

  @Override
  public Map< ? , ? > getCurrentState( )
  {
    final Map<String, Object> currentState = new TreeMap<>();
    currentState.put( ACTIVE_CHART_NAME, m_chart );
    return currentState;
  }

  @Override
  public String[] getProvidedSourceNames( )
  {
    return PROVIDED_SOURCE_NAMES;
  }

  public void fireSourceChanged( )
  {
    final UIJob job = new UIJob( "Activate theme context job" ) //$NON-NLS-1$
    {
      @SuppressWarnings( "synthetic-access" )
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        // REMARK: priority has been chosen more or less by random... set a correct priority if
        // clear how that stuff works.
        fireSourceChanged( ISources.ACTIVE_WORKBENCH_WINDOW, ACTIVE_CHART_NAME, m_chart );

        refreshElements();

        return Status.OK_STATUS;
      }
    };
    job.setRule( m_mutexRule );
    job.setSystem( true );
    job.schedule();
  }

  public void refreshUIelements( )
  {
    final UIJob job = new UIJob( "Refreshing chart ui-elements" ) //$NON-NLS-1$
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        refreshElements();
        return Status.OK_STATUS;
      }
    };
    job.setRule( m_mutexRule );
    job.setSystem( true );
    job.schedule();
  }

  protected void refreshElements( )
  {
    try
    {
      final IEvaluationService evalService = (IEvaluationService)m_serviceLocator.getService( IEvaluationService.class );
      if( evalService != null )
        evalService.requestEvaluation( ACTIVE_CHART_NAME );

      // Refresh the ui elements (i.e. toolbar), but is this the best place...?
      final ICommandService commandService = (ICommandService)m_serviceLocator.getService( ICommandService.class );
      if( commandService != null )
        CommandUtilities.refreshElements( commandService, CHART_COMMAND_CATEGORY, null );
    }
    catch( final CommandException e )
    {
      e.printStackTrace();
    }
  }

  public void setChart( final IChartComposite chart )
  {
    if( m_chart != null )
      m_chart.removeListener( m_listener );

    m_chart = chart;

    if( m_chart != null )
      m_chart.addListener( m_listener );

    fireSourceChanged();
  }
}