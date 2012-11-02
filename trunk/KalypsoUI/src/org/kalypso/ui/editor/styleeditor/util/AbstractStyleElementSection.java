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
package org.kalypso.ui.editor.styleeditor.util;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.contribs.eclipse.swt.widgets.SectionUtils;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.internal.i18n.Messages;

/**
 * Helper that controls the presence of a sub-element of a style element.<br>
 * E.g. PointSymbolizer contains a Graphic element. This section helps to add/remove the graphic.<br/>
 * The sub element is shown inside a section with toolbar for add/remove actions.
 * 
 * @author Gernot Belger
 */
public abstract class AbstractStyleElementSection<ELEMENT, ITEM, ITEMCONTROL>
{
  private final IUpdateable[] m_actions;

  private final Section m_section;

  private final ToolBarManager m_toolbar;

  private final IStyleInput<ELEMENT> m_input;

  private final FormToolkit m_toolkit;

  private final Composite m_contentPanel;

  private ITEM m_item;

  private ITEMCONTROL m_itemControl;

  protected AbstractStyleElementSection( final FormToolkit toolkit, final Composite parent, final IStyleInput<ELEMENT> input )
  {
    final String description = getDescription();
    int style;
    if( description == null )
      style = ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED;
    else
      style = ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED | Section.DESCRIPTION;

    m_section = toolkit.createSection( parent, style );

    m_toolkit = toolkit;
    m_input = input;

    m_section.setText( getTitle() );
    if( description != null )
      m_section.setDescription( description );

    m_contentPanel = toolkit.createComposite( m_section );
    m_contentPanel.setLayout( new FillLayout() );
    m_section.setClient( m_contentPanel );

    m_section.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        dispose();
      }
    } );

    m_actions = createActions( input );
    m_toolbar = createToolbar();

    updateControl();
  }

  protected String getDescription( )
  {
    return null;
  }

  protected abstract String getTitle( );

  protected abstract StyleElementAction<ELEMENT>[] createActions( IStyleInput<ELEMENT> input );

  protected abstract ITEM getItem( ELEMENT data );

  private ToolBarManager createToolbar( )
  {
    final ToolBarManager toolbar = SectionUtils.createSectionToolbar( m_section );
    for( final IUpdateable action : m_actions )
      toolbar.add( (IAction)action );
    toolbar.update( true );
    return toolbar;
  }

  void dispose( )
  {
    m_toolbar.dispose();
  }

  public Section getSection( )
  {
    return m_section;
  }

  protected FormToolkit getToolkit( )
  {
    return m_toolkit;
  }

  protected IStyleInput<ELEMENT> getInput( )
  {
    return m_input;
  }

  public void updateControl( )
  {
    final ELEMENT data = m_input.getData();
    final ITEM item = getItem( data );

    if( ObjectUtils.equals( item, m_item ) )
    {
      updateToolbar();
      if( m_itemControl != null )
        updateItemControl( m_itemControl );
      return;
    }

    m_item = item;

    ControlUtils.disposeChildren( m_contentPanel );

    if( item == null )
    {
      m_itemControl = null;
      getToolkit().createLabel( m_contentPanel, String.format( Messages.getString( "AbstractStyleElementSection_0" ), getTitle() ) ); //$NON-NLS-1$
    }
    else
      m_itemControl = createItemControl( m_contentPanel, item );

    m_contentPanel.layout( true, true );

    updateToolbar();
  }

  protected abstract ITEMCONTROL createItemControl( Composite parent, ITEM item );

  protected abstract void updateItemControl( ITEMCONTROL itemControl );

  private void updateToolbar( )
  {
    for( final IUpdateable action : m_actions )
      action.update();
  }
}