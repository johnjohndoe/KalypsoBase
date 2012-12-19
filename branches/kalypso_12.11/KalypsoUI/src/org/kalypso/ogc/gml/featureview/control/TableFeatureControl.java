package org.kalypso.ogc.gml.featureview.control;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.ArrayUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Table;
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
import org.kalypso.contribs.eclipse.swt.SWTUtilities;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.util.pool.KeyInfo;
import org.kalypso.core.util.pool.ResourcePool;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.toolbar.AddFeatureHandler;
import org.kalypso.ogc.gml.featureview.toolbar.DeleteFeatureHandler;
import org.kalypso.ogc.gml.featureview.toolbar.ToolbarHelper;
import org.kalypso.ogc.gml.mapmodel.CommandableWorkspace;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.ogc.gml.table.ColumnDescriptor;
import org.kalypso.ogc.gml.table.LayerTableViewer;
import org.kalypso.ogc.gml.table.celleditors.IFeatureModifierFactory;
import org.kalypso.template.featureview.Toolbar;
import org.kalypso.template.featureview.Toolbar.Command;
import org.kalypso.template.featureview.Toolbar.MenuContribution;
import org.kalypso.template.gistableview.Gistableview;
import org.kalypso.util.command.JobExclusiveCommandTarget;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.model.feature.GMLWorkspace;
import org.kalypsodeegree.model.feature.event.IGMLWorkspaceModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEvent;
import org.kalypsodeegree.model.feature.event.ModellEventListener;
import org.kalypsodeegree_impl.model.feature.FeaturePath;
import org.kalypsodeegree_impl.model.feature.gmlxpath.GMLXPath;

/**
 * @author Gernot Belger
 */
public class TableFeatureControl extends AbstractToolbarFeatureControl implements ModellEventListener
{
  private static final QName[] DEFAULT_INVISIBLE_PROPERTIES = new QName[] { Feature.QN_BOUNDED_BY, Feature.QN_LOCATION };

  private final IFeatureModifierFactory m_factory;

  private LayerTableViewer m_viewer;

  private final ICommandTarget m_templateTarget = new JobExclusiveCommandTarget( new DefaultCommandManager(), null );

  protected final Collection<ModifyListener> m_listeners = new ArrayList<>();

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

  private CommandableWorkspace m_workspace;

  private final URL m_templateContext;

  public TableFeatureControl( final IPropertyType ftp, final IFeatureModifierFactory factory, final IFeatureSelectionManager selectionManager, final Toolbar toolbar, final boolean showToolbar, final boolean showContextMenu, final URL templateContext )
  {
    super( ftp, showToolbar, SWT.VERTICAL | SWT.FLAT );
    m_showToolbar = showToolbar;
    m_templateContext = templateContext;

    Assert.isNotNull( ftp );

    m_factory = factory;
    m_selectionManager = selectionManager;

    m_showContextMenu = showContextMenu;
    m_toolbar = toolbar;
  }

  @Override
  public Control createControl( final FormToolkit toolkit, final Composite parent, final int style )
  {
    /* Create a new Composite for the toolbar. */
    final Composite client = new Composite( parent, SWT.NONE );
    final GridLayout gridLayout = GridLayoutFactory.fillDefaults().create();
    if( ToolbarHelper.hasActions( m_toolbar ) || m_showToolbar )
      gridLayout.numColumns++;
    client.setLayout( gridLayout );

    /* Create the layer table viewer. */
    m_viewer = new LayerTableViewer( client, style, m_templateTarget, m_factory, m_selectionManager, m_fcl );
    final Table table = m_viewer.getTable();
    applyToolkit( toolkit, client );

    table.setLayoutData( new GridData( GridData.FILL, GridData.FILL, true, true ) );

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

    final List<Toolbar.Command> commands;
    if( m_toolbar == null )
      commands = new ArrayList<>();
    else
      commands = m_toolbar.getCommand();

    /**
     * support old definition of toolbar (add and delete feature actions)
     */
    if( m_showToolbar && m_toolbar == null )
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

    if( m_toolbar != null )
    {
      final List<MenuContribution> contributionUris = m_toolbar.getMenuContribution();
      for( final MenuContribution contribution : contributionUris )
        addToolbarItems( contribution.getUri() );
    }

    if( getToolbarManager() != null )
    {
      final ToolBar toolbar = getToolbarManager().createControl( client );
      toolbar.setLayoutData( new GridData( GridData.FILL, GridData.FILL, false, true ) );

      applyToolkit( toolkit, toolbar );

      hookExecutionListener( m_viewer, getToolbarManager() );
    }

    return client;
  }

  @Override
  public void dispose( )
  {
    if( m_workspace != null )
    {
      m_workspace.removeModellListener( this );
      m_workspace = null;
    }

    if( m_templateTarget instanceof JobExclusiveCommandTarget )
      ((JobExclusiveCommandTarget)m_templateTarget).dispose();

    super.dispose();
  }

  @Override
  public void setFeature( final Feature feature )
  {
    super.setFeature( feature );

    if( m_workspace != null )
    {
      m_workspace.removeModellListener( this );
      m_workspace = null;
    }

    final GMLWorkspace workspace = feature == null ? null : feature.getWorkspace();
    if( m_viewer != null && workspace != null && feature != null )
    {
      final FeaturePath parentFeaturePath = new FeaturePath( feature );
      final String ftpName = getFeatureTypeProperty().getQName().getLocalPart();
      final FeaturePath featurePath = new FeaturePath( parentFeaturePath, ftpName );

      m_workspace = findCommandableWorkspace( workspace );

      final ICommandTarget commandTarget = new ICommandTarget()
      {
        @Override
        public void postCommand( final ICommand command, final Runnable runnable )
        {
          fireFeatureChange( command );
        }
      };

      m_workspace.addModellListener( this );

      m_viewer.setInput( m_workspace, featurePath.toString(), commandTarget );

      if( m_tableView != null )
      {
        m_viewer.applyLayer( m_tableView.getLayer(), m_templateContext );
      }
      else
      {
        // FIXME: use special descriptor implementation instead; use to NOT tab-traverse (still a bug)
        final ColumnDescriptor emptyColumn = new ColumnDescriptor( null );
        emptyColumn.setWidth( 0 );
        emptyColumn.setResizeable( false );
        m_viewer.addColumn( emptyColumn, true );

        final IFeatureType featureType = m_viewer.getInput().getFeatureType();
        addDefaultColumns( featureType );
      }
    }
  }

  private void addDefaultColumns( final IFeatureType featureType )
  {
    final IPropertyType[] properties = featureType == null ? new IPropertyType[0] : featureType.getProperties();
    for( int i = 0; i < properties.length; i++ )
    {
      final IPropertyType ftp = properties[i];
      final QName qName = ftp.getQName();
      if( !ArrayUtils.contains( DEFAULT_INVISIBLE_PROPERTIES, qName ) )
      {
        final int columnAlignment = findDefaultColumnAlignment( ftp );

        final GMLXPath columnPath = new GMLXPath( ftp.getQName() );

        final ColumnDescriptor column = new ColumnDescriptor( columnPath );
        column.setAlignment( columnAlignment );

        m_viewer.addColumn( column, i == properties.length - 1 );
      }
    }
  }

  private int findDefaultColumnAlignment( final IPropertyType ftp )
  {
    if( ftp instanceof IValuePropertyType )
    {
      final IValuePropertyType vpt = (IValuePropertyType)ftp;
      final Class< ? > valueClass = vpt.getValueClass();
      if( Number.class.isAssignableFrom( valueClass ) )
        return SWT.RIGHT;
    }

    return SWT.LEFT;
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
      if( object instanceof CommandableWorkspace && ((CommandableWorkspace)object).getWorkspace() == workspace )
        return (CommandableWorkspace)object;
    }

    final CommandableWorkspace commandable;
    if( workspace instanceof CommandableWorkspace )
      commandable = (CommandableWorkspace)workspace;
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
    if( m_workspace == null )
      return;

    if( modellEvent instanceof IGMLWorkspaceModellEvent )
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
              ((ModifyListener)element).modifyText( me );

          }
        } );
      }
    }
  }

  public CommandableWorkspace getWorkspace( )
  {
    return m_workspace;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.AbstractFeatureControl#getFeatureTypeProperty()
   */
  @Override
  public IRelationType getFeatureTypeProperty( )
  {
    return (IRelationType)super.getFeatureTypeProperty();
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