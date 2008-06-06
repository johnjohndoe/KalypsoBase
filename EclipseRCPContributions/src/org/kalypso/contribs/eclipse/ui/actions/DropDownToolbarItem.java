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
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IExecutionListener;
import org.eclipse.core.commands.IParameter;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.Parameterization;
import org.eclipse.core.commands.ParameterizedCommand;
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
import org.eclipse.ui.handlers.IHandlerService;
import org.eclipse.ui.internal.commands.ICommandImageService;
import org.eclipse.ui.progress.UIJob;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.contribs.eclipse.core.commands.ExecutionAdapter;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/**
 * @author kuch, kurzbach
 */
public class DropDownToolbarItem extends CompoundContributionItem implements IExecutableExtension
{
  protected Command[] m_commands = new Command[] {};

  private final Map<Command, IExecutionListener> m_commandListeners = new HashMap<Command, IExecutionListener>();

  protected CommandContributionItem m_currentCommand;

  protected final IHandlerService m_handlerService;

  public DropDownToolbarItem( )
  {
    // REMARK: we are using the most global service locator here, that is not
    // the very best choice... however, how do we find a better one?
    final IWorkbench serviceLocator = PlatformUI.getWorkbench();
    m_commandService = (ICommandService) serviceLocator.getService( ICommandService.class );
    m_handlerService = (IHandlerService) serviceLocator.getService( IHandlerService.class );
  }

  /**
   * @see org.eclipse.ui.actions.CompoundContributionItem#getContributionItems()
   */
  @SuppressWarnings("unchecked")
  @Override
  protected CommandContributionItem[] getContributionItems( )
  {
    final IServiceLocator locator = PlatformUI.getWorkbench();

    final CommandContributionItem[] items = new CommandContributionItem[m_commands.length];
    for( int i = 0; i < m_commands.length; i++ )
      items[i] = new CommandContributionItem( locator, m_commands[i].getId(), m_commands[i].getId(), new HashMap(), null, null, null, null, null, null, SWT.PUSH );

    return items;
  }

  /**
   * @see org.eclipse.core.runtime.IExecutableExtension#setInitializationData(org.eclipse.core.runtime.IConfigurationElement,
   *      java.lang.String, java.lang.Object)
   */
  @SuppressWarnings("unchecked")
  public void setInitializationData( final IConfigurationElement config, final String propertyName, final Object data )
  {
    final Set<Command> commands = new LinkedHashSet<Command>();
    if( data instanceof Map< ? , ? > )
    {

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
          commands.add( m_commandService.getCommand( value ) );
        }
      }
      m_commands = commands.toArray( new Command[commands.size()] );
    }

    if( m_commands.length > 0 )
    {
      m_currentCommand = getContributionItems()[0];
    }

    registerCommandListeners( commands.toArray( new Command[commands.size()] ) );
  }

  // COPIED FRMO COMMAND CONTRIBUTION ITEM

  protected void registerCommandListeners( Command[] commands )
  {
    for( final Command command : commands )
    {
      final IExecutionListener listener = new ExecutionAdapter()
      {
        /**
         * @see org.kalypso.contribs.eclipse.core.commands.ExecutionAdapter#preExecute(java.lang.String,
         *      org.eclipse.core.commands.ExecutionEvent)
         */
        @SuppressWarnings("unchecked")
        @Override
        public void preExecute( String commandId, ExecutionEvent event )
        {
          final Command cmd = m_commandService.getCommand( commandId );
          final ArrayList<Parameterization> parmList = new ArrayList<Parameterization>();
          final Iterator<Map.Entry<String, String>> i = event.getParameters().entrySet().iterator();
          while( i.hasNext() )
          {
            final Map.Entry<String, String> entry = i.next();
            final String parmName = entry.getKey();
            IParameter parm;
            try
            {
              parm = cmd.getParameter( parmName );
            }
            catch( final NotDefinedException e )
            {
              e.printStackTrace();
              return;
            }
            parmList.add( new Parameterization( parm, entry.getValue() ) );
          }
          final ParameterizedCommand thisCommand = new ParameterizedCommand( cmd, parmList.toArray( new Parameterization[parmList.size()] ) );

          for( final CommandContributionItem item : getContributionItems() )
          {
            if( item.getCommand().equals( thisCommand ) )
            {
              m_currentCommand = item;
              update();
            }
          }
        }
      };

      command.addExecutionListener( listener );
      m_commandListeners.put( command, listener );
    }
  }

  private LocalResourceManager localResourceManager;

  private Listener menuItemListener;

  private Widget widget;

  protected ICommandService m_commandService;

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

        ImageDescriptor icon = service.getImageDescriptor( m_currentCommand.getId(), ICommandImageService.TYPE_DEFAULT );
        ImageDescriptor disabledIcon = service.getImageDescriptor( m_currentCommand.getId(), ICommandImageService.TYPE_DISABLED );
        ImageDescriptor hoverIcon = service.getImageDescriptor( m_currentCommand.getId(), ICommandImageService.TYPE_HOVER );

        if( icon == null )
        {
          icon = m_currentCommand.getIcon();
        }
        if( disabledIcon == null )
        {
          disabledIcon = m_currentCommand.getDisabledIcon();
        }
        if( hoverIcon == null )
        {
          hoverIcon = m_currentCommand.getHoverIcon();
        }

        updateIcons( icon, disabledIcon, hoverIcon );

        final String label = m_currentCommand.getLabel();
        final String tooltip = m_currentCommand.getTooltip();

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

  protected void handleWidgetDispose( final Event event )
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
      m_commandService.unregisterElement( elementRef );
      elementRef = null;
    }
    m_commandService = null;
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

  protected void handleWidgetSelection( final Event event )
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
              m_handlerService.executeCommand( m_currentCommand.getCommand(), event );
            }
            catch( final ExecutionException e )
            {
              return Status.CANCEL_STATUS;
            }
            catch( final NotHandledException e )
            {
              return Status.CANCEL_STATUS;
            }
            catch( NotDefinedException e )
            {
              // TODO Auto-generated catch block
              e.printStackTrace();
            }
            catch( NotEnabledException e )
            {
              // TODO Auto-generated catch block
              e.printStackTrace();
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
          menu.setLocation( p ); // waiting for SWT
          // 0.42
          menu.setVisible( true );
          return true; // we don't fire the action
        }
      }
    }

    return false;
  }

  /**
   * @see org.eclipse.jface.action.ContributionItem#isEnabled()
   */
  @Override
  public boolean isEnabled( )
  {
    return getContributionItems().length > 0;
  }

}
