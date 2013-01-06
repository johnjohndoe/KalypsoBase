/** This file is part of Kalypso
 *
 *  Copyright (c) 2012 by
 *
 *  Björnsen Beratende Ingenieure GmbH, Koblenz, Germany (Bjoernsen Consulting Engineers), http://www.bjoernsen.de
 *  Technische Universität Hamburg-Harburg, Institut für Wasserbau, Hamburg, Germany
 *  (Technical University Hamburg-Harburg, Institute of River and Coastal Engineering), http://www.tu-harburg.de/wb/
 *
 *  Kalypso is free software: you can redistribute it and/or modify it under the terms  
 *  of the GNU Lesser General Public License (LGPL) as published by the Free Software 
 *  Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  Kalypso is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied 
 *  warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Kalypso.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.kalypso.afgui.internal.handlers;

import java.net.URL;
import java.util.Properties;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.expressions.IEvaluationContext;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.ISources;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.progress.UIJob;
import org.kalypso.afgui.internal.i18n.Messages;
import org.kalypso.afgui.scenarios.ScenarioHelper;
import org.kalypso.contribs.eclipse.core.resources.ResourceUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.template.featureview.Featuretemplate;
import org.kalypso.template.featureview.Featuretemplate.Layer;
import org.kalypso.ui.editor.featureeditor.FeatureTemplateView;

/**
 * Loads a template file in the current feature view. Requires that the current context contains the feature view. Use a {@link ViewContextHandler} for this purpose.<br>
 * Supported parameters:
 * <ul>
 * <li>input (optional): (scenario relative) path to a .gft file; if set this file will be shown in the feature view</li>
 * <li>gml (optional): (scenario relative) path to a .gml file; if set, the root feature of this file will be shown in the feature view</li>
 * <li>viewTitle (optional): If set, the title of the view will be set to this value</li>
 * </ul>
 * One of 'input' or 'gml' must be set.
 * 
 * @author Stefan Kurzbach
 */
public class FeatureViewInputContextHandler extends AbstractHandler
{
  private static final String PARAM_GML = "gml"; //$NON-NLS-1$

  private static final String PARAM_VIEW_TITLE = "viewTitle"; //$NON-NLS-1$

  private final String m_featureViewInput;

  private final String m_gmlPath;

  private final String m_viewTitle;

  /**
   * Creates a new {@link FeatureViewInputContextHandler} that loads the given input file
   */
  public FeatureViewInputContextHandler( final Properties properties )
  {
    m_featureViewInput = properties.getProperty( KalypsoContextHandlerFactory.PARAM_INPUT, null );
    m_gmlPath = properties.getProperty( PARAM_GML, null );
    m_viewTitle = properties.getProperty( PARAM_VIEW_TITLE, null );

    Assert.isTrue( m_featureViewInput != null || m_gmlPath != null, Messages.getString( "org.kalypso.afgui.handlers.FeatureViewInputContextHandler.2" ) ); //$NON-NLS-1$
  }

  @Override
  public Object execute( final ExecutionEvent event ) throws ExecutionException
  {
    final IEvaluationContext context = (IEvaluationContext)event.getApplicationContext();

    final IContainer szenarioFolder = ScenarioHelper.getScenarioFolder();
    // TODO: that is strange and probably bug-prone. Why not just use scenario-relative paths for the .gft file?
    final IContainer folder = m_featureViewInput == null ? null : ScenarioHelper.findModelContext( szenarioFolder, m_featureViewInput );
    // TODO: directly throw exceptions if something is missing
    final IFile file;
    if( folder != null && m_featureViewInput != null )
      file = folder.getFile( Path.fromPortableString( m_featureViewInput ) );
    else
      file = null;

    final IWorkbenchWindow window = (IWorkbenchWindow)context.getVariable( ISources.ACTIVE_WORKBENCH_WINDOW_NAME );
    final IWorkbenchPage page = window == null ? null : window.getActivePage();
    final IViewPart view = page == null ? null : page.findView( FeatureTemplateView.ID );

    if( !(view instanceof FeatureTemplateView) )
      throw new ExecutionException( Messages.getString( "org.kalypso.afgui.handlers.FeatureViewInputContextHandler.3" ) ); //$NON-NLS-1$

    final String gmlPath = m_gmlPath;
    if( file == null && gmlPath == null )
      throw new ExecutionException( Messages.getString( "org.kalypso.afgui.handlers.FeatureViewInputContextHandler.4" ) + m_featureViewInput ); //$NON-NLS-1$

    final FeatureTemplateView featureView = (FeatureTemplateView)view;
    final String viewTitle = m_viewTitle;

    final UIJob job = new UIJob( Messages.getString( "org.kalypso.afgui.handlers.FeatureViewInputContextHandler.5" ) ) //$NON-NLS-1$
    {
      @Override
      public IStatus runInUIThread( final IProgressMonitor monitor )
      {
        try
        {
          final Featuretemplate template;
          final URL urlContext;
          // either the file is set OR we have a gmlPath (was checked above)
          if( file == null )
          {
            // if we have a gmlPath we create a pseudo template here
            template = GisTemplateHelper.OF_FEATUREVIEW.createFeaturetemplate();
            template.setName( Messages.getString( "org.kalypso.afgui.handlers.FeatureViewInputContextHandler.6" ) ); //$NON-NLS-1$
            final Layer layer = GisTemplateHelper.OF_FEATUREVIEW.createFeaturetemplateLayer();
            layer.setHref( gmlPath );
            layer.setLinktype( "gml" ); //$NON-NLS-1$
            layer.setFeaturePath( "#fid#root" ); // always use root feature; maybe get from parameter some day //$NON-NLS-1$
            template.setLayer( layer );
            urlContext = ResourceUtilities.createURL( szenarioFolder );
          }
          else
          {
            template = GisTemplateHelper.loadGisFeatureTemplate( file );
            urlContext = ResourceUtilities.createURL( file );
          }

          if( viewTitle != null )
            template.setName( viewTitle );

          featureView.setTemplate( template, urlContext, null, null, null );

          return Status.OK_STATUS;
        }
        catch( final Throwable e )
        {
          return StatusUtilities.statusFromThrowable( e );
        }
      }
    };
    job.schedule();

    return Status.OK_STATUS;
  }
}
