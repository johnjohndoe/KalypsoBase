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
package org.kalypso.core.internal.layoutwizard.controller;

import org.kalypso.core.layoutwizard.ILayoutController;
import org.kalypso.core.layoutwizard.ILayoutPart;
import org.kalypso.core.layoutwizard.IModificationListener;
import org.kalypso.core.layoutwizard.IModificationProvider;

/**
 * Updates the target part if the source part is modified.
 * 
 * @author Gernot Belger
 */
public class ModificationLayoutController implements ILayoutController
{
  private final IModificationListener m_modificationListener = new IModificationListener()
  {
    @Override
    public void onModified( )
    {
      handleOnModified();
    }
  };

  private final ILayoutPart m_provider;

  private final ILayoutPart m_consumer;

  public ModificationLayoutController( final ILayoutPart provider, final ILayoutPart consumer )
  {
    m_provider = provider;
    m_consumer = consumer;
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutController#init()
   */
  @Override
  public void init( )
  {
    final IModificationProvider modificationProvider = m_provider.getModificationProvider();
    modificationProvider.addModificationListener( m_modificationListener );

    handleOnModified();
  }

  protected void handleOnModified( )
  {
    m_consumer.refresh();
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutController#dispose()
   */
  @Override
  public void dispose( )
  {
    final IModificationProvider modificationProvider = m_provider.getModificationProvider();
    modificationProvider.removeModificationListener( m_modificationListener );
  }

}
