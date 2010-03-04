/*******************************************************************************
 * Copyright (c) 2006, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.kalypso.contribs.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.CommandEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.ICommandListener;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.action.ContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.bindings.TriggerSequence;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.commands.ICommandImageService;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementReference;
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.WorkbenchPlugin;
import org.eclipse.ui.keys.IBindingService;
import org.eclipse.ui.menus.IMenuService;
import org.eclipse.ui.menus.UIElement;
import org.eclipse.ui.services.IServiceLocator;

/**
 * TODO: check what has changed from {@link org.eclipse.ui.menus.CommandContributionItem} and doucment it.<br>
 * A contribution item which delegates to a command. It can be used in
 * {@link AbstractContributionFactory#createContributionItems(IServiceLocator, IContributionRoot)}.
 * <p>
 * It currently supports placement in menus and toolbars.
 * </p>
 * <p>
 * This class may be instantiated; it is not intended to be subclassed.
 * </p>
 * 
 * @since 3.3
 */
// We suppress everything at class level as this is a copied class and we do not want to change too much
@SuppressWarnings( { "unchecked", "restriction", "hiding", "synthetic-access" })
public final class CommandContributionItem extends ContributionItem
{
  /**
   * A push button tool item or menu item.
   */
  public static final int STYLE_PUSH = SWT.PUSH;

  /**
   * A checked tool item or menu item.
   */
  public static final int STYLE_CHECK = SWT.CHECK;

  /**
   * A radio-button style menu item.
   */
  public static final int STYLE_RADIO = SWT.RADIO;

  /**
   * A ToolBar pulldown item.
   */
  public static final int STYLE_PULLDOWN = SWT.DROP_DOWN;

  private LocalResourceManager localResourceManager;

  private Listener menuItemListener;

  private Widget widget;

  private final IMenuService menuService;

  private ICommandService commandService;

  private final IHandlerService handlerService;

  private final IBindingService bindingService;

  private ParameterizedCommand command;

  private ImageDescriptor icon;

  private String label;

  private String tooltip;

  private ImageDescriptor disabledIcon;

  private ImageDescriptor hoverIcon;

  private final String mnemonic;

  private IElementReference elementRef;

  private boolean checkedState;

  private final int style;

  private ICommandListener commandListener;

  private String dropDownMenuOverride;

  /**
   * Create a CommandContributionItem to place in a ContributionManager.
   * 
   * @param serviceLocator
   *          a service locator that is most appropriate for this contribution. Typically the local
   *          {@link IWorkbenchWindow} or {@link IWorkbenchPartSite} will be sufficient.
   * @param id
   *          The id for this item. May be <code>null</code>. Items without an id cannot be referenced later.
   * @param commandId
   *          A command id for a defined command. Must not be <code>null</code>.
   * @param parameters
   *          A map of strings to strings which represent parameter names to values. The parameter names must match
   *          those in the command definition.
   * @param icon
   *          An icon for this item. May be <code>null</code>.
   * @param disabledIcon
   *          A disabled icon for this item. May be <code>null</code>.
   * @param hoverIcon
   *          A hover icon for this item. May be <code>null</code>.
   * @param label
   *          A label for this item. May be <code>null</code>.
   * @param mnemonic
   *          A mnemonic for this item to be applied to the label. May be <code>null</code>.
   * @param tooltip
   *          A tooltip for this item. May be <code>null</code>. Tooltips are currently only valid for toolbar
   *          contributions.
   * @param style
   *          The style of this menu contribution. See the STYLE_* contants.
   */
  public CommandContributionItem( final IServiceLocator serviceLocator, final String id, final String commandId, final Map parameters, final ImageDescriptor icon, final ImageDescriptor disabledIcon, final ImageDescriptor hoverIcon, final String label, final String mnemonic, final String tooltip, final int style )
  {
    super( id );
    this.icon = icon;
    this.disabledIcon = disabledIcon;
    this.hoverIcon = hoverIcon;
    this.label = label;
    this.mnemonic = mnemonic;
    this.tooltip = tooltip;
    this.style = style;
    menuService = (IMenuService) serviceLocator.getService( IMenuService.class );
    commandService = (ICommandService) serviceLocator.getService( ICommandService.class );
    handlerService = (IHandlerService) serviceLocator.getService( IHandlerService.class );
    bindingService = (IBindingService) serviceLocator.getService( IBindingService.class );
    createCommand( commandId, parameters );

    if( command != null )
    {
      try
      {
        final UIElement callback = new UIElement( serviceLocator )
        {
          @Override
          public void setChecked( final boolean checked )
          {
            CommandContributionItem.this.setChecked( checked );
          }

          @Override
          public void setDisabledIcon( final ImageDescriptor desc )
          {
            CommandContributionItem.this.setDisabledIcon( desc );
          }

          @Override
          public void setHoverIcon( final ImageDescriptor desc )
          {
            CommandContributionItem.this.setHoverIcon( desc );
          }

          @Override
          public void setIcon( final ImageDescriptor desc )
          {
            CommandContributionItem.this.setIcon( desc );
          }

          @Override
          public void setText( final String text )
          {
            CommandContributionItem.this.setText( text );
          }

          @Override
          public void setTooltip( final String text )
          {
            CommandContributionItem.this.setTooltip( text );
          }

          @Override
          public void setDropDownId( final String id )
          {
            dropDownMenuOverride = id;
          }
        };
        elementRef = commandService.registerElementForCommand( command, callback );
        command.getCommand().addCommandListener( getCommandListener() );
        setImages( serviceLocator );
      }
      catch( final NotDefinedException e )
      {
        WorkbenchPlugin.log( "Unable to register menu item \"" + getId() //$NON-NLS-1$
            + "\", command \"" + commandId + "\" not defined" ); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }
  }

  private void setImages( final IServiceLocator locator )
  {
    if( icon == null )
    {
      final ICommandImageService service = (ICommandImageService) locator.getService( ICommandImageService.class );
      icon = service.getImageDescriptor( command.getId(), ICommandImageService.TYPE_DEFAULT );
      disabledIcon = service.getImageDescriptor( command.getId(), ICommandImageService.TYPE_DISABLED );
      hoverIcon = service.getImageDescriptor( command.getId(), ICommandImageService.TYPE_HOVER );
    }
  }

  /**
   * @return
   */
  private ICommandListener getCommandListener( )
  {
    if( commandListener == null )
    {
      commandListener = new ICommandListener()
      {
        public void commandChanged( final CommandEvent commandEvent )
        {
          if( commandEvent.isHandledChanged() || commandEvent.isEnabledChanged() || commandEvent.isDefinedChanged() )
          {
            if( commandEvent.isHandledChanged() )
            {
              dropDownMenuOverride = null;
            }
            if( commandEvent.getCommand().isDefined() )
            {
              update( null );
            }
          }
        }
      };
    }
    return commandListener;
  }

  public ParameterizedCommand getCommand( )
  {
    return command;
  }

  void createCommand( final String commandId, final Map parameters )
  {
    if( commandId == null )
    {
      WorkbenchPlugin.log( "Unable to create menu item \"" + getId() //$NON-NLS-1$
          + "\", no command id" ); //$NON-NLS-1$
      return;
    }
    final Command cmd = commandService.getCommand( commandId );
    if( !cmd.isDefined() )
    {
      WorkbenchPlugin.log( "Unable to create menu item \"" + getId() //$NON-NLS-1$
          + "\", command \"" + commandId + "\" not defined" ); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }

    if( parameters == null || parameters.size() == 0 )
    {
      command = new ParameterizedCommand( cmd, null );
      return;
    }

    try
    {
      final ArrayList parmList = new ArrayList();
      final Iterator i = parameters.entrySet().iterator();
      while( i.hasNext() )
      {
        final Map.Entry entry = (Map.Entry) i.next();
        final String parmName = (String) entry.getKey();
        IParameter parm;
        parm = cmd.getParameter( parmName );
        if( parm == null )
        {
          WorkbenchPlugin.log( "Unable to create menu item \"" + getId() //$NON-NLS-1$
              + "\", parameter \"" + parmName + "\" for command \"" //$NON-NLS-1$ //$NON-NLS-2$
              + commandId + "\" is not defined" ); //$NON-NLS-1$
          return;
        }
        parmList.add( new Parameterization( parm, (String) entry.getValue() ) );
      }
      command = new ParameterizedCommand( cmd, (Parameterization[]) parmList.toArray( new Parameterization[parmList.size()] ) );
    }
    catch( final NotDefinedException e )
    {
      // this shouldn't happen as we checked for !defined, but we
      // won't take the chance
      WorkbenchPlugin.log( "Failed to create menu item " //$NON-NLS-1$
          + getId(), e );
    }
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.Menu, int)
   */
  @Override
  public void fill( final Menu parent, final int index )
  {
    if( command == null )
    {
      return;
    }
    if( widget != null || parent == null )
    {
      return;
    }

    // Menus don't support the pulldown style
    int tmpStyle = style;
    if( tmpStyle == STYLE_PULLDOWN )
      tmpStyle = STYLE_PUSH;

    MenuItem item = null;
    if( index >= 0 )
    {
      item = new MenuItem( parent, tmpStyle, index );
    }
    else
    {
      item = new MenuItem( parent, tmpStyle );
    }
    item.setData( this );

    item.addListener( SWT.Dispose, getItemListener() );
    item.addListener( SWT.Selection, getItemListener() );
    widget = item;

    update( null );
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.ToolBar, int)
   */
  @Override
  public void fill( final ToolBar parent, final int index )
  {
    if( command == null )
    {
      return;
    }
    if( widget != null || parent == null )
    {
      return;
    }

    ToolItem item = null;
    if( index >= 0 )
    {
      item = new ToolItem( parent, style, index );
    }
    else
    {
      item = new ToolItem( parent, style );
    }

    item.setData( this );

    item.addListener( SWT.Selection, getItemListener() );
    item.addListener( SWT.Dispose, getItemListener() );
    widget = item;

    update( null );
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.action.ContributionItem#update()
   */
  @Override
  public void update( )
  {
    update( null );
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.action.ContributionItem#update(java.lang.String)
   */
  @Override
  public void update( final String id )
  {
    if( widget != null )
    {
      if( widget instanceof MenuItem )
      {
        final MenuItem item = (MenuItem) widget;

        String text = label;
        if( text == null )
        {
          if( command != null )
          {
            try
            {
              text = command.getCommand().getName();
            }
            catch( final NotDefinedException e )
            {
              WorkbenchPlugin.log( "Update item failed " //$NON-NLS-1$
                  + getId(), e );
            }
          }
        }
        text = updateMnemonic( text );

        String keyBindingText = null;
        if( command != null )
        {
          final TriggerSequence[] bindings = bindingService.getActiveBindingsFor( command );
          if( bindings.length > 0 )
          {
            keyBindingText = bindings[0].format();
          }
        }
        if( text != null )
        {
          if( keyBindingText == null )
          {
            item.setText( text );
          }
          else
          {
            item.setText( text + '\t' + keyBindingText );
          }
        }

        updateIcons();
        if( item.getSelection() != checkedState )
        {
          item.setSelection( checkedState );
        }

        final boolean shouldBeEnabled = isEnabled();
        if( item.getEnabled() != shouldBeEnabled )
        {
          item.setEnabled( shouldBeEnabled );
        }
      }
      else if( widget instanceof ToolItem )
      {
        final ToolItem item = (ToolItem) widget;

        if( icon != null )
        {
          updateIcons();
        }
        else if( label != null )
        {
          item.setText( label );
        }

        if( tooltip != null )
          item.setToolTipText( tooltip );
        else
        {
          String text = label;
          if( text == null )
          {
            if( command != null )
            {
              try
              {
                text = command.getCommand().getName();
              }
              catch( final NotDefinedException e )
              {
                WorkbenchPlugin.log( "Update item failed " //$NON-NLS-1$
                    + getId(), e );
              }
            }
          }
          if( text != null )
          {
            item.setToolTipText( text );
          }
        }

        if( item.getSelection() != checkedState )
        {
          item.setSelection( checkedState );
        }

        final boolean shouldBeEnabled = isEnabled();
        if( item.getEnabled() != shouldBeEnabled )
        {
          item.setEnabled( shouldBeEnabled );
        }
      }
    }
  }

  private String updateMnemonic( final String s )
  {
    if( mnemonic == null || s == null )
    {
      return s;
    }
    final int idx = s.indexOf( mnemonic );
    if( idx == -1 )
    {
      return s;
    }

    return s.substring( 0, idx ) + '&' + s.substring( idx );
  }

  private void handleWidgetDispose( final Event event )
  {
    if( event.widget == widget )
    {
      widget.removeListener( SWT.Selection, getItemListener() );
      widget.removeListener( SWT.Dispose, getItemListener() );
      widget = null;
      disposeOldImages();
    }
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.action.ContributionItem#dispose()
   */
  @Override
  public void dispose( )
  {
    if( elementRef != null )
    {
      commandService.unregisterElement( elementRef );
      elementRef = null;
    }
    if( commandListener != null )
    {
      command.getCommand().removeCommandListener( commandListener );
      commandListener = null;
    }
    command = null;
    commandService = null;
    disposeOldImages();
    super.dispose();
  }

  private void disposeOldImages( )
  {
    if( localResourceManager != null )
    {
      localResourceManager.dispose();
      localResourceManager = null;
    }
  }

  private Listener getItemListener( )
  {
    if( menuItemListener == null )
    {
      menuItemListener = new Listener()
      {
        public void handleEvent( final Event event )
        {
          switch( event.type )
          {
            case SWT.Dispose:
              handleWidgetDispose( event );
              break;
            case SWT.Selection:
              if( event.widget != null )
              {
                handleWidgetSelection( event );
              }
              break;
          }
        }
      };
    }
    return menuItemListener;
  }

  private void handleWidgetSelection( final Event event )
  {
    // Special check for ToolBar dropdowns...
    if( openDropDownMenu( event ) )
      return;

    if( (style & (SWT.TOGGLE | SWT.CHECK)) != 0 )
    {
      if( event.widget instanceof ToolItem )
      {
        checkedState = ((ToolItem) event.widget).getSelection();
      }
      else if( event.widget instanceof MenuItem )
      {
        checkedState = ((MenuItem) event.widget).getSelection();
      }
    }

    try
    {
      handlerService.executeCommand( command, event );
    }
    catch( final ExecutionException e )
    {
      WorkbenchPlugin.log( "Failed to execute item " //$NON-NLS-1$
          + getId(), e );
    }
    catch( final NotDefinedException e )
    {
      WorkbenchPlugin.log( "Failed to execute item " //$NON-NLS-1$
          + getId(), e );
    }
    catch( final NotEnabledException e )
    {
      WorkbenchPlugin.log( "Failed to execute item " //$NON-NLS-1$
          + getId(), e );
    }
    catch( final NotHandledException e )
    {
      WorkbenchPlugin.log( "Failed to execute item " //$NON-NLS-1$
          + getId(), e );
    }
  }

  /**
   * Determines if the selection was on the dropdown affordance and, if so, opens the drop down menu (populated using
   * the same id as this item...
   * 
   * @param event
   *          The <code>SWT.Selection</code> event to be tested
   * @return <code>true</code> iff a drop down menu was opened
   */
  private boolean openDropDownMenu( final Event event )
  {
    final Widget item = event.widget;
    if( item != null )
    {
      final int style = item.getStyle();
      if( (style & SWT.DROP_DOWN) != 0 )
      {
        if( event.detail == 4 )
        { // on drop-down button
          final ToolItem ti = (ToolItem) item;

          final MenuManager menuManager = new MenuManager();
          final Menu menu = menuManager.createContextMenu( ti.getParent() );
          menuManager.addMenuListener( new IMenuListener()
          {
            public void menuAboutToShow( final IMenuManager manager )
            {
              String id = getId();
              if( dropDownMenuOverride != null )
              {
                id = dropDownMenuOverride;
              }
              menuService.populateContributionManager( menuManager, "menu:" + id ); //$NON-NLS-1$
            }
          } );

          // position the menu below the drop down item
          final Rectangle b = ti.getBounds();
          final Point p = ti.getParent().toDisplay( new Point( b.x, b.y + b.height ) );
          menu.setLocation( p.x, p.y ); // waiting for SWT
          // 0.42
          menu.setVisible( true );
          return true; // we don't fire the action
        }
      }
    }

    return false;
  }

  private void setIcon( final ImageDescriptor desc )
  {
    icon = desc;
    updateIcons();
  }

  private void updateIcons( )
  {
    if( widget instanceof MenuItem )
    {
      final MenuItem item = (MenuItem) widget;
      final LocalResourceManager m = new LocalResourceManager( JFaceResources.getResources() );
      item.setImage( icon == null ? null : m.createImage( icon ) );
      disposeOldImages();
      localResourceManager = m;
    }
    else if( widget instanceof ToolItem )
    {
      final ToolItem item = (ToolItem) widget;
      final LocalResourceManager m = new LocalResourceManager( JFaceResources.getResources() );
      item.setDisabledImage( disabledIcon == null ? null : m.createImage( disabledIcon ) );
      item.setHotImage( hoverIcon == null ? null : m.createImage( hoverIcon ) );
      item.setImage( icon == null ? null : m.createImage( icon ) );
      disposeOldImages();
      localResourceManager = m;
    }
  }

  private void setText( final String text )
  {
    label = text;
    update( null );
  }

  private void setChecked( final boolean checked )
  {
    if( checkedState == checked )
    {
      return;
    }
    checkedState = checked;
    if( widget instanceof MenuItem )
    {
      ((MenuItem) widget).setSelection( checkedState );
    }
    else if( widget instanceof ToolItem )
    {
      ((ToolItem) widget).setSelection( checkedState );
    }
  }

  private void setTooltip( final String text )
  {
    tooltip = text;
    if( widget instanceof ToolItem )
    {
      ((ToolItem) widget).setToolTipText( text );
    }
  }

  private void setDisabledIcon( final ImageDescriptor desc )
  {
    disabledIcon = desc;
    updateIcons();
  }

  private void setHoverIcon( final ImageDescriptor desc )
  {
    hoverIcon = desc;
    updateIcons();
  }

  /*
   * (non-Javadoc)
   * @see org.eclipse.jface.action.ContributionItem#isEnabled()
   */
  @Override
  public boolean isEnabled( )
  {
    if( command != null )
    {
      return command.getCommand().isEnabled();
    }
    return false;
  }

  public ImageDescriptor getIcon( )
  {
    return icon;
  }

  public String getLabel( )
  {
    return label;
  }

  public String getTooltip( )
  {
    return tooltip;
  }

  public ImageDescriptor getDisabledIcon( )
  {
    return disabledIcon;
  }

  public ImageDescriptor getHoverIcon( )
  {
    return hoverIcon;
  }
}
