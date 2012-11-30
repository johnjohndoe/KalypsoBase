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
package org.kalypso.ui.editor.gistableeditor;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.core.commands.common.CommandException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
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
import org.kalypso.contribs.eclipse.ui.commands.CommandUtilities;
import org.kalypso.ogc.gml.table.LayerTableViewer;
import org.kalypso.ui.editor.ISourceProvider2;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEventListener;

/**
 * Manages context and sources corresponding to the gtt table.<br>
 * As soon as the manager is created on a gtt part, the context is activated and registered with the given
 * serviceLocator. Also, the gtt table is provides as source via the evaluation context.<br>
 * 
 * @author Gernot Belger
 */
public class GmlTableSourceProvider extends AbstractSourceProvider implements ISourceProvider2
{
  public static final String GMLTABLE_COMMAND_CATEGORY = "org.kalypso.ogc.gml.table.category"; //$NON-NLS-1$

  public static final String GMLTABLE_CONTEXT = "org.kalypso.ogc.gml.table.context"; //$NON-NLS-1$

  public static final String ACTIVE_GMLTABLE_NAME = "activeGmlTable"; //$NON-NLS-1$

  private static final String[] PROVIDED_SOURCE_NAMES = new String[] { ACTIVE_GMLTABLE_NAME };

  // FIXME
  private final ModellEventListener m_modelListener = new ModellEventListener()
  {
    @Override
    public void onModellChange( final ModellEvent modellEvent )
    {
      refreshUIelements();
    }
  };

  private final ISelectionChangedListener m_selectionChangedListener = new ISelectionChangedListener()
  {
    @Override
    public void selectionChanged( final SelectionChangedEvent event )
    {
      refreshUIelements();
    }
  };

  /**
   * This collection contains all services, with which this provider has been registered. Used in order to correctly
   * unregister.
   */
  private final Collection<IServiceWithSources> m_registeredServices = new HashSet<>();

  private LayerTableViewer m_tableViewer;

  private final IContextActivation m_activationContext;

  private final IServiceLocator m_serviceLocator;

  private final UIJob m_refreshElementsJob = new UIJob( "refresh ui elements" ) //$NON-NLS-1$
  {
    @Override
    public IStatus runInUIThread( final IProgressMonitor monitor )
    {
      refreshElements();
      return Status.OK_STATUS;
    }
  };

  private final Job m_activateContextJob = new UIJob( "Activate gml table context job" ) //$NON-NLS-1$
  {
    @Override
    @SuppressWarnings( "synthetic-access" )
    public IStatus runInUIThread( final IProgressMonitor monitor )
    {
      // REMARK: priority has been chosen more or less by random... set a correct priority if
      // clear how that stuff works.
      fireSourceChanged( ISources.ACTIVE_WORKBENCH_WINDOW, ACTIVE_GMLTABLE_NAME, m_tableViewer );

      refreshElements();

      return Status.OK_STATUS;
    }
  };

  /**
   * Creates a new {@link GmlTableSourceProvider}r on the given {@link LayerTableViewer}.<br>
   * Initializes it state with the given parameters.
   */
  public GmlTableSourceProvider( final IServiceLocator serviceLocator, final LayerTableViewer tableViewer )
  {
    m_activateContextJob.setSystem( true );

    m_refreshElementsJob.setSystem( true );

    m_serviceLocator = serviceLocator;
    m_tableViewer = tableViewer;

    final IContextService contextService = (IContextService)registerServiceWithSources( serviceLocator, IContextService.class );
    registerServiceWithSources( serviceLocator, IEvaluationService.class );
    registerServiceWithSources( serviceLocator, IHandlerService.class );
    registerServiceWithSources( serviceLocator, IMenuService.class );

    m_activationContext = contextService.activateContext( GMLTABLE_CONTEXT );

    // FIXME
    // m_tableViewer.addModellListener( m_modelListener );

    m_tableViewer.addSelectionChangedListener( m_selectionChangedListener );
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
    // unregister the registered source provider
    for( final IServiceWithSources service : m_registeredServices )
      service.removeSourceProvider( this );

    // FIXME
    // m_tableViewer.removeModellListener( m_modelListener );

    m_tableViewer.removeSelectionChangedListener( m_selectionChangedListener );
    m_tableViewer = null;

    if( m_activationContext != null )
      m_activationContext.getContextService().deactivateContext( m_activationContext );
  }

  @Override
  public Map< ? , ? > getCurrentState( )
  {
    final Map<String, Object> currentState = new TreeMap<>();
    currentState.put( ACTIVE_GMLTABLE_NAME, m_tableViewer );
    return currentState;
  }

  @Override
  public String[] getProvidedSourceNames( )
  {
    return PROVIDED_SOURCE_NAMES;
  }

  @Override
  public void fireSourceChanged( )
  {
    m_activateContextJob.cancel();
    m_activateContextJob.schedule( 50 );
  }

  void refreshUIelements( )
  {
    m_refreshElementsJob.cancel();
    m_refreshElementsJob.schedule( 50 );
  }

  void refreshElements( )
  {
    try
    {
      final IEvaluationService evalService = (IEvaluationService)m_serviceLocator.getService( IEvaluationService.class );
      if( evalService != null )
        evalService.requestEvaluation( ACTIVE_GMLTABLE_NAME );

      // Refresh the ui elements (i.e. toolbar), but is this the best place...?
      final ICommandService commandService = (ICommandService)m_serviceLocator.getService( ICommandService.class );
      if( commandService != null )
        CommandUtilities.refreshElements( commandService, GMLTABLE_COMMAND_CATEGORY, null );
    }
    catch( final CommandException e )
    {
      e.printStackTrace();
    }
  }
}
