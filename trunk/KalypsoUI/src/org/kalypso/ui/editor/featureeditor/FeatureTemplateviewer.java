/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/
package org.kalypso.ui.editor.featureeditor;

import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;
import org.kalypso.commons.command.ICommand;
import org.kalypso.contribs.eclipse.ui.forms.ToolkitUtils;
import org.kalypso.core.util.pool.IPoolableObjectType;
import org.kalypso.core.util.pool.PoolableObjectType;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IFeaturesProvider;
import org.kalypso.ogc.gml.IFeaturesProviderListener;
import org.kalypso.ogc.gml.ILoadStartable;
import org.kalypso.ogc.gml.ISaveableFeaturesProvider;
import org.kalypso.ogc.gml.PoolFeaturesProvider;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.control.FeatureComposite;
import org.kalypso.ogc.gml.featureview.maker.CachedFeatureviewFactory;
import org.kalypso.ogc.gml.featureview.maker.FeatureviewHelper;
import org.kalypso.ogc.gml.selection.FeatureSelectionManager2;
import org.kalypso.template.featureview.Featuretemplate;
import org.kalypso.template.featureview.Featuretemplate.Layer;
import org.kalypso.template.featureview.FeatureviewType;
import org.kalypso.util.command.JobExclusiveCommandTarget;
import org.kalypso.util.swt.SWTUtilities;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.event.ModellEvent;

/**
 * @author Gernot Belger
 */
public class FeatureTemplateviewer
{
  private final FeatureviewHelper m_featureViewHelper = new FeatureviewHelper();

  private final CachedFeatureviewFactory m_fvFactory = new CachedFeatureviewFactory( m_featureViewHelper );

  private final FeatureComposite m_featureComposite = new FeatureComposite( null, new FeatureSelectionManager2(), m_fvFactory );

  private final IFeatureChangeListener m_changeListener = new IFeatureChangeListener()
  {
    @Override
    public void featureChanged( final ICommand changeCommand )
    {
      onFeatureChanged( changeCommand );
    }

    @Override
    public void openFeatureRequested( final Feature feature, final IPropertyType ftp )
    {
      // feature view öffnen
      final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      final IWorkbenchPage page = window.getActivePage();
      try
      {
        page.showView( "org.kalypso.featureview.views.FeatureView", null, IWorkbenchPage.VIEW_VISIBLE ); //$NON-NLS-1$
      }
      catch( final PartInitException e )
      {
        e.printStackTrace();
        final Shell shell = window.getShell();
        ErrorDialog.openError( shell, Messages.getString( "org.kalypso.ui.editor.featureeditor.FeatureTemplateviewer.1" ), Messages.getString( "org.kalypso.ui.editor.featureeditor.FeatureTemplateviewer.2" ), e.getStatus() ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
  };

  private final IFeaturesProviderListener m_providerListener = new IFeaturesProviderListener()
  {
    @Override
    public void featuresChanged( final IFeaturesProvider source, final ModellEvent modellEvent )
    {
      handleModellChange( source, modellEvent );
    }
  };

  private final Set<IFeaturesProviderListener> m_listener = new HashSet<IFeaturesProviderListener>();

  private Composite m_contentPanel;

  private Label m_label;

  private final JobExclusiveCommandTarget m_commandtarget;

  private Featuretemplate m_template;

  private Composite m_topLevelComposite;

  private FormToolkit m_toolkit;

  private IFeaturesProvider m_featuresProvider;


  public FeatureTemplateviewer( final JobExclusiveCommandTarget commandtarget )
  {
    m_commandtarget = commandtarget;
    m_featureComposite.addChangeListener( m_changeListener );
  }

  /**
   * @see org.kalypso.ui.editor.AbstractEditorPart#dispose()
   */
  public void dispose( )
  {
    setFeaturesProvider( null );

    m_featureComposite.dispose();
  }

  public IStatus saveGML( final IProgressMonitor monitor )
  {
    try
    {
      if( m_featuresProvider instanceof ISaveableFeaturesProvider )
        ((ISaveableFeaturesProvider) m_featuresProvider).save( monitor );

      return Status.OK_STATUS;
    }
    catch( final CoreException e )
    {
      e.printStackTrace();
      return e.getStatus();
    }
  }

  public void setTemplate( final Featuretemplate template, final URL context, final String defaultFeaturePath, final String href, final String linkType )
  {
    m_template = template;

    final List<FeatureviewType> view = template.getView();
    for( final FeatureviewType featureviewType : view )
      m_fvFactory.addView( featureviewType );

    final Layer layer = template.getLayer();

    final IPoolableObjectType key = getKey( layer, href, linkType, context );
    final String featurePath = layer == null ? defaultFeaturePath : layer.getFeaturePath();

    // only load, if href non null; in this case, the feature must be set via setFeature()
    if( key == null )
      setFeaturesProvider( null );
    else
      setFeaturesProvider( new PoolFeaturesProvider( key, featurePath ) );
  }

  private IPoolableObjectType getKey( final Layer layer, final String defaultHref, final String defaultLinkType, final URL context )
  {
    final String source = layer == null ? defaultHref : layer.getHref();
    final String linktype = layer == null ? defaultLinkType : layer.getLinktype();

    if( source == null )
      return null;

    return new PoolableObjectType( linktype, source, context );
  }

  protected void handleModellChange( final IFeaturesProvider source, final ModellEvent event )
  {
    m_commandtarget.setCommandManager( source.getWorkspace() );

    if( m_contentPanel == null || m_contentPanel.isDisposed() )
      return;

    final FeatureComposite featureComposite = m_featureComposite;
    m_contentPanel.getDisplay().asyncExec( new Runnable()
    {
      @Override
      public void run( )
      {
        if( event == null )
          updateControls();
        else
          featureComposite.updateControl();

        fireChanged( event );
      }
    } );
  }

  /**
   * @defaultStyle SWT-style for the top-level component. Will only be used, if the template defines no style.
   */
  public Composite createControls( final Composite parent, final int defaultStyle )
  {
    final int formStyle = getTemplateStyle( defaultStyle );

    /* If we explicitely need an toolkit but we don't have one, create it now */
    if( m_template != null && m_template.isToolkit() && m_toolkit == null )
      m_toolkit = ToolkitUtils.createToolkit( parent );

    m_featureComposite.setFormToolkit( m_toolkit );

    m_contentPanel = createTopLevelComposite( parent, formStyle );

    final GridLayout gridLayout = new GridLayout();
    gridLayout.marginHeight = 0;
    gridLayout.marginWidth = 0;
    m_contentPanel.setLayout( gridLayout );

    try
    {
      updateControls();
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }

    return m_topLevelComposite;
  }

  private Composite createTopLevelComposite( final Composite parent, final int formStyle )
  {
    final boolean scrollVertical = (formStyle & SWT.V_SCROLL) != 0;
    final boolean scrollHorzizontal = (formStyle & SWT.H_SCROLL) != 0;

    final boolean useScrolledForm = scrollVertical || scrollHorzizontal;
    if( useScrolledForm )
    {
      final ScrolledForm scrolledForm = new ScrolledForm( parent, formStyle );
      scrolledForm.setExpandHorizontal( true );
      scrolledForm.setExpandVertical( true );

      m_topLevelComposite = scrolledForm;
      return scrolledForm.getBody();
    }

    final Form form = new Form( parent, formStyle );
    m_topLevelComposite = form;
    return form.getBody();
  }

  private int getTemplateStyle( final int style )
  {
    if( m_template == null )
      return style;

    final String swtflags = m_template.getSwtflags();
    if( swtflags == null )
      return style;

    return SWTUtilities.createStyleFromString( swtflags );
  }

  /**
   * This function updates the controls.
   */
  protected final void updateControls( )
  {
    try
    {
      /* Need a panel to do something. */
      if( m_contentPanel == null || m_contentPanel.isDisposed() )
        return;

      /* Reset the label. */
      if( m_label != null && !m_label.isDisposed() )
        m_label.dispose();

      /* Reset the feature composite. */
      m_featureComposite.setFeature( null );
      m_featureComposite.disposeControl();

      /* If a workspace is missing, it is probably still loading. */
      if( m_featuresProvider == null )
      {
        /* Create a label, to inform the user about the status. */
        m_label = new Label( m_contentPanel, SWT.CENTER );
        m_label.setText( Messages.getString( "org.kalypso.ui.editor.featureeditor.FeatureTemplateviewer.5" ) ); //$NON-NLS-1$
        m_label.setLayoutData( new GridData( GridData.FILL_BOTH ) );
        return;
      }

      final List<Feature> features = m_featuresProvider == null ? null : m_featuresProvider.getFeatures();
      if( features.size() != 1 )
        throw new IllegalArgumentException();
      final Feature feature = features.get( 0 );

      /* Try to obtain the feature to display. */
      /* The result may be null, if the feature path is null, too. */
      /* A empty feature path will lead to the root feature. */

      /* Set the new feature. May be null. */
      m_featureComposite.setFeature( feature );

      /* Create the control. */
      final IFeatureType featureType = feature != null ? feature.getFeatureType() : null;
      final Control control = m_featureComposite.createControl( m_contentPanel, SWT.NONE, featureType );

      /* Update the control of the feature composite. */
      m_featureComposite.updateControl();

      control.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    }
    catch( final Exception e )
    {
      e.printStackTrace();
    }
    finally
    {
      if( !m_topLevelComposite.isDisposed() )
      {
        if( m_topLevelComposite instanceof ScrolledForm )
          ((SharedScrolledComposite) m_topLevelComposite).reflow( true );
        else
          m_topLevelComposite.layout( true, true );
      }
    }
  }

  protected void onFeatureChanged( final ICommand changeCommand )
  {
    m_commandtarget.postCommand( changeCommand, null );
  }

  public Feature getFeature( )
  {
    return m_featureComposite.getFeature();
  }

  public Control getControl( )
  {
    return m_contentPanel;
  }

  public FeatureComposite getFeatureComposite( )
  {
    return m_featureComposite;
  }

  public void setFeaturesProvider( final IFeaturesProvider featuresProvider )
  {
    if( m_featuresProvider != null )
    {
      m_featuresProvider.dispose();
      m_commandtarget.setCommandManager( null );
    }

    m_featuresProvider = featuresProvider;

    if( m_featuresProvider != null )
    {
      m_commandtarget.setCommandManager( m_featuresProvider.getWorkspace() );
      m_featuresProvider.addFeaturesProviderListener( m_providerListener );
    }

    if( m_topLevelComposite != null && !m_topLevelComposite.isDisposed() )
      updateControls();

    if( m_featuresProvider instanceof ILoadStartable )
      ((ILoadStartable) m_featuresProvider).startLoading();
  }

  public void setToolkit( final FormToolkit toolkit )
  {
    m_toolkit = toolkit;
  }

  public void addFeaturesProviderListener( final IFeaturesProviderListener listener )
  {
    m_listener.add( listener );
  }

  public void removeFeaturesProviderListener( final IFeaturesProviderListener listener )
  {
    m_listener.remove( listener );
  }

  protected void fireChanged( final ModellEvent modellEvent )
  {
    final IFeaturesProviderListener[] listeners = m_listener.toArray( new IFeaturesProviderListener[m_listener.size()] );
    for( final IFeaturesProviderListener listener : listeners )
      listener.featuresChanged( m_featuresProvider, modellEvent );
  }

}