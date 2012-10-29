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
package org.kalypso.core.internal.layoutwizard.part;

import org.eclipse.core.runtime.Assert;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.core.layoutwizard.ILayoutPart;

/**
 * @author Gernot Belger
 */
public class TabFolderContainer extends AbstractLayoutContainer
{
  public TabFolderContainer( final String id )
  {
    super( id );
  }

  @Override
  public Control createControl( final Composite parent, final FormToolkit toolkit )
  {
    final TabFolder tabFolder = new TabFolder( parent, getStyle() );
    toolkit.adapt( tabFolder );

    final ILayoutPart[] children = getChildren();
    for( final ILayoutPart child : children )
    {
      Assert.isTrue( child instanceof TabItemContainer );
      final TabItemContainer tic = (TabItemContainer) child;
      tic.createControl( tabFolder, toolkit );
    }

    return tabFolder;
  }
}