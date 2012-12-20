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

import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.kalypso.core.layoutwizard.ILayoutController;
import org.kalypso.core.layoutwizard.ILayoutPart;

/**
 * Moves the selection from the observed {@link ILayoutPart} to another {@link ILayoutPart} (the consumer).
 * 
 * @author Gernot Belger
 */
public class SelectionLayoutController implements ILayoutController
{
  private final ISelectionChangedListener m_selectionChangedListener = new ISelectionChangedListener()
  {
    @Override
    public void selectionChanged( final SelectionChangedEvent event )
    {
      handleSelectionChanged( event.getSelection() );
    }
  };

  private final ILayoutPart m_provider;

  private final ILayoutPart m_consumer;

  public SelectionLayoutController( final ILayoutPart provider, final ILayoutPart consumer )
  {
    m_provider = provider;
    m_consumer = consumer;
  }

  @Override
  public void init( )
  {
    final ISelectionProvider providerProvider = m_provider.getSelectionProvider();
    if( providerProvider != null )
    {
      providerProvider.addSelectionChangedListener( m_selectionChangedListener );

      final ISelection selection = providerProvider.getSelection();
      handleSelectionChanged( selection );
    }
  }

  protected void handleSelectionChanged( final ISelection selection )
  {
    final ISelectionProvider consumerProvider = m_consumer.getSelectionProvider();
    if( consumerProvider != null )
    {
      final ISelection consumerSelection = consumerProvider.getSelection();
      if( selection instanceof IStructuredSelection && consumerSelection instanceof IStructuredSelection )
      {
        final Set< ? > selectionSet = new HashSet<>( ((IStructuredSelection) selection).toList() );
        final Set< ? > consumerSet = new HashSet<>( ((IStructuredSelection) consumerSelection).toList() );
        if( !selectionSet.equals( consumerSet ) )
          consumerProvider.setSelection( selection );
      }
      else
        consumerProvider.setSelection( selection );
    }
  }

  @Override
  public void dispose( )
  {
    final ISelectionProvider providerProvider = m_provider.getSelectionProvider();
    if( providerProvider != null )
      providerProvider.removeSelectionChangedListener( m_selectionChangedListener );
  }

}
