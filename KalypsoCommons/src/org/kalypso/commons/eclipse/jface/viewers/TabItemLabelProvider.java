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
package org.kalypso.commons.eclipse.jface.viewers;

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * @author Gernot Belger
 */
public class TabItemLabelProvider extends LabelProvider implements ITabControlProvider
{
  private final FormToolkit m_toolkit;

  public TabItemLabelProvider( final FormToolkit toolkit )
  {
    m_toolkit = toolkit;
  }

  /**
   * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
   */
  @Override
  public String getText( final Object element )
  {
    if( element instanceof ITabItem )
      return ((ITabItem) element).getItemLabel();

    return super.getText( element );
  }

  /**
   * @see org.eclipse.jface.viewers.LabelProvider#getImage(java.lang.Object)
   */
  @Override
  public Image getImage( final Object element )
  {
    if( element instanceof ITabItem )
      return ((ITabItem) element).getItemImage();

    return super.getImage( element );
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabControlProvider#getItemStyle(java.lang.Object)
   */
  @Override
  public int getItemStyle( final Object element )
  {
    return SWT.CLOSE;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabControlProvider#createControl(org.eclipse.swt.widgets.Composite,
   *      java.lang.Object)
   */
  @Override
  public Control createControl( final Composite parent, final Object element )
  {
    if( element instanceof ITabItem )
      return ((ITabItem) element).createItemControl( m_toolkit, parent );

    return null;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabControlProvider#updateControl(java.lang.Object)
   */
  @Override
  public void updateControl( final Object element )
  {
    if( element instanceof ITabItem )
      ((ITabItem) element).updateItemControl();
  }

}

