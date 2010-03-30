package org.kalypso.ogc.gml.featureview.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.command.DefaultCommandManager;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.util.pool.KeyInfo;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.KalypsoTableFeatureTheme;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.toolbar.AddFeatureHandler;
import org.kalypso.ogc.gml.featureview.toolbar.DeleteFeatureHandler;
import org.kalypso.ogc.gml.featureview.toolbar.ToolbarHelper;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.table.LayerTableViewer;
import org.kalypso.ogc.gml.table.celleditors.IFeatureModifierFactory;
import org.kalypso.template.featureview.Toolbar;
import org.kalypso.template.featureview.Toolbar.Command;
import org.kalypso.template.featureview.Toolbar.MenuContribution;
import org.kalypso.template.gistableview.Gistableview;
import org.kalypso.util.command.JobExclusiveCommandTarget;
import org.kalypso.util.swt.SWTUtilities;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.event.IGMLWorkspaceModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEventListener;
import org.kalypsodeegree_impl.model.feature.FeaturePath;

/**
 * @author Gernot Belger
 */
public class TableFeatureContol extends AbstractToolbarFeatureControl implements ModellEventListener
{
  private final IFeatureModifierFactory m_factory;

  private LayerTableViewer m_viewer;

  private KalypsoTableFeatureTheme m_kft;

  private final ICommandTarget m_templateTarget = new JobExclusiveCommandTarget( new DefaultCommandManager(), null );

  protected final Collection<ModifyListener> m_listeners = new ArrayList<ModifyListener>();

  protected final IFeatureSelectionManager m_selectionManager;

  private final IFeatureChangeListener m_fcl = new IFeatureChangeListener()
  {
    @Override
    public void openFeatureRequested( final Feature feature, final IPropertyType pt )
    {
      fireOpenFeatureRequested( feature, pt );
    }

    @Override
    public void featureChanged( final ICommand changeCommand )
    {
      fireFeatureChange( changeCommand );
    }
  };

  private Gistableview m_tableView;

  private final boolean m_showContextMenu;

  private final Toolbar m_toolbar;

  private final boolean m_showToolbar;

  public TableFeatureContol( final IPropertyType ftp, final IFeatureModifierFactory factory, final IFeatureSelectionManager selectionManager, final Toolbar toolbar, final boolean showToolbar, final boolean showContextMenu )
  {
    super( ftp, ToolbarHelper.hasActions( toolbar ), SWT.VERTICAL | SWT.FLAT );
    m_showToolbar = showToolbar;

    Assert.isNotNull( ftp );

    m_factory = factory;
    m_selectionManager = selectionManager;

    m_showContextMenu = showContextMenu;
    m_toolbar = toolbar;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#createControl(org.eclipse.swt.widgets.Composite, int)
   */
  @Override
  public Control createControl( final Composite parent, final int style )
  {
    /* Create a new Composite for the toolbar. */
    final Composite client = new Composite( parent, SWT.NONE );
    final GridLayout gridLayout = new GridLayout( 1, false );
    gridLayout.marginWidth = 0;
    gridLayout.marginHeight = 0;
    if( ToolbarHelper.hasActions( m_toolbar ) )
      gridLayout.numColumns++;
    client.setLayout( gridLayout );

    /* Create the layer table viewer. */
    m_viewer = new LayerTableViewer( client, style, m_templateTarget, m_factory, m_selectionManager, m_fcl );
    m_viewer.setFeatureCommandTarget( new ICommandTarget()
    {
      @Override
      public void postCommand( final ICommand command, final Runnable runnable )
      {
        fireFeatureChange( command );
      }
    } );
    m_viewer.getTable().setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

    /* Set the feature. */
    final Feature feature = getFeature();
    setFeature( feature );

    /* Only show the context menu, if it is wanted to be shown. */
    if( m_showContextMenu )
    {
      /* Need a menu manager for the context menu. */
      final MenuManager menuManager = new MenuManager();
      menuManager.setRemoveAllWhenShown( true );
      menuManager.addMenuListener( new IMenuListener()
      {
        @Override
        public void menuAboutToShow( final IMenuManager manager )
        {
          manager.add( new GroupMarker( IWorkbenchActionConstants.MB_ADDITIONS ) );
          manager.add( new Separator() );
        }
      } );

      final IWorkbenchWindow activeWorkbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
      final IWorkbenchPage activePage = activeWorkbenchWindow.getActivePage();
      final IWorkbenchPart activeEditor = activePage.getActivePart();
      if( activeEditor != null )
      {
        /* Set the context menu. */
        m_viewer.setMenu( menuManager );

        /* TODO Check if we can register the menu more global, even when we have no active editor. */
        activeEditor.getSite().registerContextMenu( menuManager, m_viewer );
      }
    }

    if( m_toolbar == null )
      return client;

    final List<Toolbar.Command> commands = m_toolbar.getCommand();

    /**
     * support old definition of toolbar (add and delete feature actions)
     */
    if( m_showToolbar )
    {
      final Command addFeatureCommand = new Toolbar.Command();
      addFeatureCommand.setCommandId( AddFeatureHandler.ID );
      addFeatureCommand.setStyle( Integer.valueOf( SWT.PUSH ).toString() );
      commands.add( addFeatureCommand );

      final Command deleteFeatureCommand = new Toolbar.Command();
      deleteFeatureCommand.setCommandId( DeleteFeatureHandler.ID );
      deleteFeatureCommand.setStyle( Integer.valueOf( SWT.PUSH ).toString() );
      commands.add( deleteFeatureCommand );
    }

    for( final Toolbar.Command command : commands )
    {
      final String commandId = command.getCommandId();
      final int itemStyle = SWTUtilities.createStyleFromString( command.getStyle() );
      addToolbarItem( commandId, itemStyle );
    }

    final List<MenuContribution> contributionUris = m_toolbar.getMenuContribution();
    for( final MenuContribution contribution : contributionUris )
      addToolbarItems( contribution.getUri() );

    final ToolBar toolbar = getToolbarManager().createControl( client );
    toolbar.setLayoutData( new GridData( GridData.FILL, GridData.FILL, false, true ) );

    final FormToolkit toolkit = new FormToolkit( toolbar.getDisplay() );
    toolkit.adapt( client );
    toolkit.adapt( toolbar );

    hookExecutionListener( m_viewer, getToolbarManager() );

    return client;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_viewer != null )
      m_viewer.dispose();

    if( m_kft != null )
    {
      final CommandableWorkspace workspace = m_kft.getWorkspace();
      if( workspace != null )
        workspace.removeModellListener( this );
      m_kft.dispose();
      m_kft = null;
    }

    if( m_templateTarget instanceof JobExclusiveCommandTarget )
      ((JobExclusiveCommandTarget) m_templateTarget).dispose();

    super.dispose();
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#setFeature(org.kalypsodeegree.model.feature.GMLWorkspace,
   *      org.kalypsodeegree.model.feature.Feature)
   */
  @Override
  public void setFeature( final Feature feature )
  {
    super.setFeature( feature );

    if( m_kft != null )
    {
      final CommandableWorkspace workspace = m_kft.getWorkspace();
      if( workspace != null )
        workspace.removeModellListener( this );
      m_kft.dispose();
      m_kft = null;
    }

    final GMLWorkspace workspace = feature == null ? null : feature.getWorkspace();
    if( m_viewer != null && workspace != null && feature != null )
    {
      final FeaturePath parentFeaturePath = workspace.getFeaturepathForFeature( feature );
      final String ftpName = getFeatureTypeProperty().getQName().getLocalPart();
      final FeaturePath featurePath = new FeaturePath( parentFeaturePath, ftpName );

      final CommandableWorkspace commandable = findCommandableWorkspace( workspace );

      m_kft = new KalypsoTableFeatureTheme( commandable, featurePath.toString(), new I10nString( ftpName ), m_selectionManager );

      commandable.addModellListener( this );
      m_viewer.setInput( m_kft );

      // create columns
      // add all columns
      if( m_tableView != null )
      {
        m_viewer.applyTableTemplate( m_tableView, workspace.getContext(), false );
      }
      else
      {
        final IFeatureType featureType = m_kft.getFeatureType();
        final IPropertyType[] properties = featureType == null ? new IPropertyType[0] : featureType.getProperties();
        for( int i = 0; i < properties.length; i++ )
        {
          final IPropertyType ftp = properties[i];
          m_viewer.addColumn( ftp.getQName().getLocalPart(), null, null, true, 100, "SWT.CENTER", null, null, i == properties.length - 1 ); //$NON-NLS-1$
        }
      }
    }
  }

  /**
   * Helps to find the right commandable workspace for the given feature.
   */
  private CommandableWorkspace findCommandableWorkspace( final GMLWorkspace workspace )
  {
    final ResourcePool pool = KalypsoCorePlugin.getDefault().getPool();
    final KeyInfo[] infos = pool.getInfos();
    for( final KeyInfo keyInfo : infos )
    {
      final Object object = keyInfo.getObject();
      if( object instanceof CommandableWorkspace && ((CommandableWorkspace) object).getWorkspace() == workspace )
        return (CommandableWorkspace) object;
    }

    final CommandableWorkspace commandable;
    if( workspace instanceof CommandableWorkspace )
      commandable = (CommandableWorkspace) workspace;
    else
      commandable = new CommandableWorkspace( workspace );
    return commandable;
  }

  public void setTableTemplate( final Gistableview tableView )
  {
    m_tableView = tableView;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#updateControl()
   */
  @Override
  public void updateControl( )
  {
    m_viewer.refresh();
  }

  /**
   * createFeatureControl
   * 
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#isValid()
   */
  @Override
  public boolean isValid( )
  {
    return true;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#addModifyListener(org.eclipse.swt.events.ModifyListener)
   */
  @Override
  public void addModifyListener( final ModifyListener l )
  {
    m_listeners.add( l );
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#removeModifyListener(org.eclipse.swt.events.ModifyListener)
   */
  @Override
  public void removeModifyListener( final ModifyListener l )
  {
    m_listeners.remove( l );
  }

  /**
   * @see org.kalypsodeegree.model.feature.event.ModellEventListener#onModellChange(org.kalypsodeegree.model.feature.event.ModellEvent)
   */
  @Override
  public void onModellChange( final ModellEvent modellEvent )
  {
    if( m_kft == null )
      return;

    if( modellEvent instanceof IGMLWorkspaceModellEvent && ((IGMLWorkspaceModellEvent) modellEvent).getGMLWorkspace() == m_kft.getWorkspace() )
    {
      final Event event = new Event();
      final Control control = m_viewer.getControl();
      if( control != null && !control.isDisposed() )
      {
        control.getDisplay().asyncExec( new Runnable()
        {
          @Override
          public void run( )
          {
            event.widget = control;
            final ModifyEvent me = new ModifyEvent( event );
            for( final Object element : m_listeners )
              ((ModifyListener) element).modifyText( me );

          }
        } );

// if( modellEvent instanceof FeatureChangeModellEvent )
// {
// final FeatureChangeModellEvent featureEvent = (FeatureChangeModellEvent) modellEvent;
// fireFeatureChange( featureEvent.getChanges() );
// }
      }
    }
  }

  public CommandableWorkspace getWorkspace( )
  {
    return m_kft.getWorkspace();
  }

  public IRelationType getParentRealtion( )
  {
    return (IRelationType) getFeatureTypeProperty();
  }

  public void execute( final ICommand command )
  {
    fireFeatureChange( command );
  }

  public void setSelection( final ISelection selection )
  {
    m_viewer.setSelection( selection );
  }

}