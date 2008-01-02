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
package org.kalypso.contribs.eclipse.ui.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
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
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.actions.CompoundContributionItem;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementReference;
import org.eclipse.ui.internal.commands.ICommandImageService;
import org.eclipse.ui.menus.CommandContributionItem;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.contribs.eclipse.core.commands.ExecutionAdapter;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @author kuch
 */
public class DropDownToolbarItem extends CompoundContributionItem implements IExecutableExtension
{
  private Command[] m_commands = new Command[] {};

  private final Map<Command, IExecutionListener> m_commandListeners = new HashMap<Command, IExecutionListener>();

  protected Command m_currentCommand;

  public DropDownToolbarItem( )
  {
    final IWorkbench serviceLocator = PlatformUI.getWorkbench();

// menuService = (IMenuService) serviceLocator.getService( IMenuService.class );
    commandService = (ICommandService) serviceLocator.getService( ICommandService.class );
// handlerService = (IHandlerService) serviceLocator.getService( IHandlerService.class );
// bindingService = (IBindingService) serviceLocator.getService( IBindingService.class );
  }

  /**
   * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
   */
  @Override
  protected IContributionItem[] getContributionItems( )
  {
    final IServiceLocator locator = PlatformUI.getWorkbench();

    final IContributionItem[] items = new IContributionItem[m_commands.length];
    for( int i = 0; i < m_commands.length; i++ )
      items[i] = new CommandContributionItem( locator, m_commands[i].getId(), m_commands[i].getId(), new HashMap(), null, null, null, null, null, null, SWT.PUSH );

    return items;
  }

  /**
   * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
   *      java.lang.String, java.lang.Object)
   */
  public void setInitializationData( final IConfigurationElement config, final String propertyName, final Object data )
  {
    if( data instanceof Map< ? , ? > )
    {
      final Set<Command> commands = new LinkedHashSet<Command>();

      /* REMARK: map ht is unsorted - sort commands by param id! */
      final Map<String, String> ht = (Map<String, String>) data;

      final List<String> keys = new ArrayList<String>();
      keys.addAll( ht.keySet() );
      Collections.sort( keys );

      for( final String key : keys )
      {
        final String value = ht.get( key );
        if( key.toLowerCase().startsWith( "command" ) )
        {
          commands.add( commandService.getCommand( value ) );
        }
      }

      m_commands = commands.toArray( new Command[] {} );
    }

    if( m_commands.length > 0 )
    {
      m_currentCommand = m_commands[0];
    }

    registerCommandListeners();
  }

  // COPIED FRMO COMMAND CONTRIBUTION ITEM

  private void registerCommandListeners( )
  {
    for( final Command command : m_commands )
    {

      final IExecutionListener listener = new ExecutionAdapter()
      {
        @Override
        public void postExecuteSuccess( String commandId, Object returnValue )
        {
          m_currentCommand = command;
          update();
        }
      };

      command.addExecutionListener( listener );
      m_commandListeners.put( command, listener );
    }
  }

  private LocalResourceManager localResourceManager;

  private Listener menuItemListener;

  private Widget widget;

  private ICommandService commandService;

// private final IHandlerService handlerService;
// private final IMenuService menuService;
// private final IBindingService bindingService;

  private IElementReference elementRef;

  private boolean checkedState;

  private final int style = CommandContributionItem.STYLE_PULLDOWN;

  /**
   * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.Menu, int)
   */
  @Override
  public void fill( final Menu parent, final int index )
  {
    if( widget != null || parent == null )
      return;

    // Menus don't support the pulldown style
    int tmpStyle = style;
    if( tmpStyle == CommandContributionItem.STYLE_PULLDOWN )
      tmpStyle = CommandContributionItem.STYLE_PUSH;

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

  /**
   * @see org.eclipse.jface.action.ContributionItem#fill(org.eclipse.swt.widgets.ToolBar, int)
   */
  @Override
  public void fill( final ToolBar parent, final int index )
  {
    if( widget != null || parent == null )
      return;

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

  /**
   * @see org.eclipse.jface.action.ContributionItem#update()
   */
  @Override
  public void update( )
  {
    update( null );
  }

  /**
   * @see org.eclipse.jface.action.ContributionItem#update(java.lang.String)
   */
  @SuppressWarnings("restriction")
  @Override
  public void update( final String id )
  {
    if( widget != null )
    {
      if( m_currentCommand != null )
      {
        final IServiceLocator locator = PlatformUI.getWorkbench();
        final ICommandImageService service = (ICommandImageService) locator.getService( ICommandImageService.class );

        final ImageDescriptor icon = service.getImageDescriptor( m_currentCommand.getId(), ICommandImageService.TYPE_DEFAULT );
        final ImageDescriptor disabledIcon = service.getImageDescriptor( m_currentCommand.getId(), ICommandImageService.TYPE_DISABLED );
        final ImageDescriptor hoverIcon = service.getImageDescriptor( m_currentCommand.getId(), ICommandImageService.TYPE_HOVER );

        updateIcons( icon, disabledIcon, hoverIcon );

        try
        {
          final String label = m_currentCommand.getName();
          final String tooltip = m_currentCommand.getDescription();

          updateText( label, tooltip );

          if( widget instanceof MenuItem )
          {
            final MenuItem item = (MenuItem) widget;

            final String text = label;
            final String keyBindingText = null;
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
        catch( final NotDefinedException e )
        {
          e.printStackTrace();
        }
      }
    }
  }

  private void updateIcons( final ImageDescriptor icon, final ImageDescriptor disabledIcon, final ImageDescriptor hoverIcon )
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

  private void updateText( final String label, final String tooltip )
  {
    if( widget instanceof MenuItem )
    {
      final MenuItem itm = (MenuItem) widget;
      itm.setText( label );
    }
    else if( widget instanceof ToolItem )
    {
      final ToolItem itm = (ToolItem) widget;
// itm.setText( label );
      itm.setToolTipText( tooltip );
    }
    else
      throw (new NotImplementedException());

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

  /**
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
    commandService = null;
    disposeOldImages();

    final Set<Entry<Command, IExecutionListener>> entrySet = m_commandListeners.entrySet();
    for( final Entry<Command, IExecutionListener> entry : entrySet )
    {
      entry.getKey().removeExecutionListener( entry.getValue() );
    }
    m_commandListeners.clear();

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

    if( ((event.type & SWT.MouseDown) != 0) )
    {
      if( m_currentCommand != null )
        new UIJob( "executing comannd..." )
        {

          @Override
          public IStatus runInUIThread( final IProgressMonitor monitor )
          {
            try
            {
              m_currentCommand.execute( new ExecutionEvent() );
            }
            catch( final ExecutionException e )
            {
              return Status.CANCEL_STATUS;
            }
            catch( final NotHandledException e )
            {
              return Status.CANCEL_STATUS;
            }
            return Status.OK_STATUS;
          }
        }.schedule();
    }

  }

  /**
   * Determines if the selection was on the dropdown affordance and, if so, opens the drop down menu (populated using
   * the same id as this item...
   * 
   * @param event
   *            The <code>SWT.Selection</code> event to be tested
   * @return <code>true</code> iff a drop down menu was opened
   */
  private boolean openDropDownMenu( final Event event )
  {
    final Widget item = event.widget;
    if( item != null )
    {
      final int myStyle = item.getStyle();
      if( (myStyle & SWT.DROP_DOWN) != 0 )
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
              final IContributionItem[] contributionItems = getContributionItems();
              for( final IContributionItem contributionItem : contributionItems )
                manager.add( contributionItem );
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

// private void setChecked( final boolean checked )
// {
// if( checkedState == checked )
// return;
// checkedState = checked;
// if( widget instanceof MenuItem )
// {
// ((MenuItem) widget).setSelection( checkedState );
// }
// else if( widget instanceof ToolItem )
// {
// ((ToolItem) widget).setSelection( checkedState );
// }
// }
//
// private void setTooltip( final String text )
// {
// tooltip = text;
// if( widget instanceof ToolItem )
// {
// ((ToolItem) widget).setToolTipText( text );
// }
// }
//
// private void setDisabledIcon( final ImageDescriptor desc )
// {
// disabledIcon = desc;
// updateIcons();
// }
//
// private void setHoverIcon( final ImageDescriptor desc )
// {
// hoverIcon = desc;
// updateIcons();
// }

  /*
   * (non-Javadoc)
   * 
   * @see org.eclipse.jface.action.ContributionItem#isEnabled()
   */
  @Override
  public boolean isEnabled( )
  {
    if( m_commands.length > 0 )
      return true;

    return false;
  }

}
