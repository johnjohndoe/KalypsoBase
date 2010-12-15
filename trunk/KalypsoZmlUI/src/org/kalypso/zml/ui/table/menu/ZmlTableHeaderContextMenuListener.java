/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 * 
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  DenickestraÃŸe 22
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
package org.kalypso.zml.ui.table.menu;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.PlatformUI;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.jface.action.ContributionUtils;
import org.kalypso.zml.core.table.binding.BaseColumn;
import org.kalypso.zml.core.table.binding.CellStyle;
import org.kalypso.zml.core.table.binding.ColumnHeader;
import org.kalypso.zml.core.table.binding.ZmlRule;
import org.kalypso.zml.ui.KalypsoZmlUI;
import org.kalypso.zml.ui.table.ZmlTableComposite;
import org.kalypso.zml.ui.table.model.IZmlTableColumn;
import org.kalypso.zml.ui.table.provider.strategy.ExtendedZmlTableColumn;

/**
 * @author Dirk Kuch
 */
public class ZmlTableHeaderContextMenuListener implements SelectionListener
{

  private final ZmlTableComposite m_table;

  private final ExtendedZmlTableColumn m_column;

  public ZmlTableHeaderContextMenuListener( final ZmlTableComposite table, final ExtendedZmlTableColumn column )
  {
    m_table = table;
    m_column = column;
  }

  private void setMenu( final String uri )
  {
    final Control control = m_table.getTableViewer().getControl();
    if( uri != null )
    {
      final MenuManager menuManager = new MenuManager();
      final Menu menu = menuManager.createContextMenu( control );

      // add basic menu entries which are defined in the plugin.xml
      ContributionUtils.populateContributionManager( PlatformUI.getWorkbench(), menuManager, uri );

      // add additional info items
      addAdditionalItems( menuManager );

      m_table.setContextMenu( menu );

      menu.setVisible( true );

    }
    else
      m_table.setContextMenu( new Menu( control ) );
  }

  private void addAdditionalItems( final MenuManager menuManager )
  {
    menuManager.add( new Separator() );

    menuManager.add( new Action()
    {
      @Override
      public String getText( )
      {
        return "Details und zusätzliche Informationen:";
      }

      @Override
      public boolean isEnabled( )
      {
        return false;
      }
    } );

    final ColumnHeader[] headers = m_column.getColumnType().getHeaders();
    for( final ColumnHeader header : headers )
    {
      addAditionalItem( header, menuManager );
    }

    final ZmlRule[] applied = m_column.getAppliedRules();
    for( final ZmlRule rule : applied )
    {
      addAditionalItem( rule, menuManager );
    }
  }

  private void addAditionalItem( final ColumnHeader header, final MenuManager menuManager )
  {
    menuManager.add( new Action()
    {
      @Override
      public org.eclipse.jface.resource.ImageDescriptor getImageDescriptor( )
      {
        try
        {
          final Image icon = header.getIcon();
          if( icon != null )
            return ImageDescriptor.createFromImage( icon );
        }
        catch( final Throwable t )
        {
          KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
        }

        return null;
      }

      @Override
      public String getText( )
      {
        return String.format( "   %s", header.getLabel() );
      }

      @Override
      public boolean isEnabled( )
      {
        return false;
      }

    } );

  }

  private void addAditionalItem( final ZmlRule rule, final MenuManager menuManager )
  {
    menuManager.add( new Action()
    {
      @Override
      public org.eclipse.jface.resource.ImageDescriptor getImageDescriptor( )
      {
        try
        {
          final CellStyle style = rule.getPlainStyle();
          return ImageDescriptor.createFromImage( style.getImage() );
        }
        catch( final Throwable t )
        {
          KalypsoZmlUI.getDefault().getLog().log( StatusUtilities.statusFromThrowable( t ) );
        }

        return null;
      }

      @Override
      public String getText( )
      {
        return String.format( "   enthält: %s", rule.getLabel() );
      }

      @Override
      public boolean isEnabled( )
      {
        return false;
      }
    } );
  }

  /**
   * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
   */
  @Override
  public void widgetSelected( final SelectionEvent event )
  {
    String uri = null;
    final IZmlTableColumn column = m_table.getActiveColumn();
    if( column != null )
    {
      final BaseColumn columnType = column.getColumnType();
      uri = columnType.getUriHeaderContextMenu();
    }

    setMenu( uri );
  }

  /**
   * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
   */
  @Override
  public void widgetDefaultSelected( final SelectionEvent e )
  {
  }
}
