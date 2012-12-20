/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
package org.kalypso.ui.internal.layoutwizard.featureview;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Properties;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.arguments.Arguments;
import org.kalypso.commons.java.util.PropertiesHelper;
import org.kalypso.contribs.eclipse.swt.SWTUtilities;
import org.kalypso.core.layoutwizard.ILayoutPageContext;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.ogc.gml.GisTemplateHelper;
import org.kalypso.ogc.gml.IFeaturesProvider;
import org.kalypso.ogc.gml.IFeaturesProviderListener;
import org.kalypso.ogc.gml.PlainFeaturesProvider;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.EasyFeatureWrapper;
import org.kalypso.ogc.gml.selection.IFeatureSelection;
import org.kalypso.template.featureview.Featuretemplate;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.editor.featureeditor.FeatureTemplateviewer;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypso.ui.layoutwizard.AbstractWizardLayoutPart;
import org.kalypso.util.command.JobExclusiveCommandTarget;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.IFeatureProvider;
import org.kalypsodeegree.model.feature.event.ModellEvent;

/**
 * @author Gernot Belger
 */
public class FeatureViewLayoutPart extends AbstractWizardLayoutPart implements IFeatureProvider
{
  /** Argument: Pfad auf Vorlage für die Feature-View (.gft Datei) */
  private static final String PROP_FEATURETEMPLATE = "featureTemplate"; //$NON-NLS-1$

  /**
   * Argument: SWT-Style für die Composite des Features. Default ist {@link SWT#BORDER} | {@link SWT#V_SCROLL}<br>
   * 
   * @deprecated Use the style element from the .gft file instead (swtflags)
   */
  @Deprecated
  private static final String PROP_FEATURE_VIEW_STYLE = "featureControlStyle"; //$NON-NLS-1$

  private final Runnable m_modificationRunnable = new Runnable()
  {
    @Override
    public void run( )
    {
      handlePostFeatureEdit();
    }
  };

  private final JobExclusiveCommandTarget m_commandTarget = new JobExclusiveCommandTarget( null, m_modificationRunnable );

  private final FeatureTemplateviewer m_templateviewer = new FeatureTemplateviewer( m_commandTarget );

  private final ISelectionChangedListener m_selectionChangedListener = new ISelectionChangedListener()
  {
    @Override
    public void selectionChanged( final SelectionChangedEvent event )
    {
      handleSelectionChanged( event.getSelection() );
    }
  };

  private int m_viewStyle;

  private Composite m_composite;

  public FeatureViewLayoutPart( final String id, final ILayoutPageContext context )
  {
    super( id, context );

    getSelectionProvider().addSelectionChangedListener( m_selectionChangedListener );
    m_templateviewer.addFeaturesProviderListener( new IFeaturesProviderListener()
    {
      @Override
      public void featuresChanged( final IFeaturesProvider source, final ModellEvent modellEvent )
      {
        handleFeaturesChanged( modellEvent );
      }
    } );
  }

  @Override
  public void dispose( )
  {
    m_templateviewer.dispose();
  }

  @Override
  public void init( ) throws CoreException
  {
    final ILayoutPageContext context = getContext();

    final Arguments arguments = context.getArguments();

    final String featureTemplateArgument = arguments.getProperty( PROP_FEATURETEMPLATE, null );
    final String templateFileName = featureTemplateArgument == null ? null : featureTemplateArgument.replaceAll( "#.*", "" ); //$NON-NLS-1$ //$NON-NLS-2$

    final String featureViewStyle = arguments.getProperty( PROP_FEATURE_VIEW_STYLE, "SWT.BORDER | SWT.V_SCROLL" ); //$NON-NLS-1$
    m_viewStyle = SWTUtilities.createStyleFromString( featureViewStyle );

    if( templateFileName == null )
      return;

    final Properties featureTemplateProps = PropertiesHelper.parseFromString( featureTemplateArgument, '#' );
    final String featurePath = featureTemplateProps.getProperty( "featurepath", "" ); //$NON-NLS-1$ //$NON-NLS-2$
    final String href = featureTemplateProps.getProperty( "href", null ); //$NON-NLS-1$
    final String linktype = featureTemplateProps.getProperty( "linktype", "gml" ); //$NON-NLS-1$ //$NON-NLS-2$

    try
    {
      final URL templateURL = context.resolveURI( templateFileName );
      if( templateURL == null )
      {
        final String message = String.format( Messages.getString( "FeatureViewLayoutPart_10" ), templateFileName ); //$NON-NLS-1$
        final Status status = new Status( IStatus.ERROR, KalypsoGisPlugin.PLUGIN_ID, message );
        throw new CoreException( status );
      }

      final Featuretemplate template = GisTemplateHelper.loadGisFeatureTemplate( templateURL, new NullProgressMonitor() );

      final URL dataContext = context.getContext();
      final IPoolableObjectType key = FeatureTemplateviewer.createKey( template, href, linktype, dataContext );

      m_templateviewer.setTemplate( template, key, featurePath, templateURL );
    }
    catch( final CoreException e )
    {
      throw e;
    }
    catch( final Exception e )
    {
      throw createLoadFailedException( templateFileName, e );
    }
  }

  private CoreException createLoadFailedException( final String templateFileName, final Exception e )
  {
    final String message = String.format( Messages.getString( "FeatureViewLayoutPart_11" ), templateFileName ); //$NON-NLS-1$
    final Status status = new Status( IStatus.ERROR, KalypsoGisPlugin.PLUGIN_ID, message, e );
    return new CoreException( status );
  }

  @Override
  public Control createControl( final Composite parent, final FormToolkit toolkit )
  {
    m_templateviewer.setToolkit( toolkit );
    m_composite = m_templateviewer.createControls( parent, m_viewStyle );
    return m_composite;
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutPart#saveData(boolean)
   */
  @Override
  public final void saveData( final boolean doSaveGml ) throws CoreException
  {
    if( doSaveGml )
    {
      final IStatus result = m_templateviewer.saveGML( new NullProgressMonitor() );
      if( !result.isOK() )
        throw new CoreException( result );
    }
  }

  protected final void handleSelectionChanged( final ISelection selection )
  {
    final IFeaturesProvider provider = findProvider( selection );
    setFeaturesProvider( provider );
  }

  private IFeaturesProvider findProvider( final ISelection selection )
  {
    final EasyFeatureWrapper[] features = findFeatures( selection );
    if( features.length == 0 )
      return null;

    final EasyFeatureWrapper featureWrapper = features[0];
    final CommandableWorkspace workspace = featureWrapper.getWorkspace();
    final Feature feature = featureWrapper.getFeature();
    if( workspace == null )
      return null;

    if( feature == null )
      return new PlainFeaturesProvider( workspace );

    return new PlainFeaturesProvider( workspace, feature );
  }

  private EasyFeatureWrapper[] findFeatures( final ISelection selection )
  {
    if( selection instanceof IFeatureSelection )
      return ((IFeatureSelection)selection).getAllFeatures();

    if( selection instanceof IStructuredSelection )
    {
      final Collection<EasyFeatureWrapper> result = new ArrayList<>();
      final IStructuredSelection structSel = (IStructuredSelection)selection;
      for( final Iterator< ? > iterator = structSel.iterator(); iterator.hasNext(); )
      {
        final Object element = iterator.next();
        if( element instanceof EasyFeatureWrapper )
          result.add( (EasyFeatureWrapper)element );
        else if( element instanceof EasyFeatureWrapper[] )
          result.addAll( Arrays.asList( (EasyFeatureWrapper[])element ) );
      }

      return result.toArray( new EasyFeatureWrapper[result.size()] );
    }

    return new EasyFeatureWrapper[] {};
  }

  private void setFeaturesProvider( final IFeaturesProvider provider )
  {
    m_templateviewer.setFeaturesProvider( provider );

    getContext().reflow();
  }

  protected void handlePostFeatureEdit( )
  {
    getModificationProvider().fireModified();
  }

  protected void handleFeaturesChanged( final ModellEvent modellEvent )
  {
    if( modellEvent == null )
      getContext().reflow();
  }

  @Override
  public Feature getFeature( )
  {
    return m_templateviewer.getFeature();
  }
}
