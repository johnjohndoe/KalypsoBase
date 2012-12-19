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
package org.kalypso.core.internal.layoutwizard.part;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.core.layoutwizard.ILayoutContainer;
import org.kalypso.core.layoutwizard.ILayoutPart;
import org.kalypso.core.layoutwizard.ILayoutPartVisitor;
import org.kalypso.core.layoutwizard.part.AbstractLayoutPart;

/**
 * @author Gernot Belger
 */
public abstract class AbstractLayoutContainer extends AbstractLayoutPart implements ILayoutContainer
{
  private final List<ILayoutPart> m_children = new ArrayList<>();

  public AbstractLayoutContainer( final String id )
  {
    super( id, null );
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutPart#dispose()
   */
  @Override
  public void dispose( )
  {
    for( final ILayoutPart part : m_children )
      part.dispose();
  }

  @Override
  public void init( )
  {
    for( int i = 0; i < m_children.size(); i++ )
    {
      final ILayoutPart child = m_children.get( i );
      try
      {
        child.init();
      }
      catch( final CoreException e )
      {
        // On failure, replace child by a status part that shows the error
        m_children.set( i, new StatusLayoutPart( child.getId(), child.getContext(), e.getStatus() ) );
        child.dispose();
      }
      catch( final Exception e )
      {
        // On failure, replace child by a status part that shows the error
        final IStatus status = new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), Messages.getString("AbstractLayoutContainer_0"), e ); //$NON-NLS-1$
        m_children.set( i, new StatusLayoutPart( child.getId(), child.getContext(), status ) );
        child.dispose();
      }
    }
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutContainer#findPart(java.lang.String)
   */
  @Override
  public ILayoutPart findPart( final String id )
  {
    if( getId().equals( id ) )
      return this;

    for( final ILayoutPart part : m_children )
    {
      if( part instanceof ILayoutContainer )
      {
        final ILayoutPart foundPart = ((ILayoutContainer) part).findPart( id );
        if( foundPart != null )
          return foundPart;
      }
      else if( part.getId().equals( id ) )
        return part;
    }

    return null;
  }

  @Override
  public void addChild( final ILayoutPart part )
  {
    m_children.add( part );
  }

  protected ILayoutPart[] getChildren( )
  {
    return m_children.toArray( new ILayoutPart[m_children.size()] );
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutPart#activate()
   */
  @Override
  public void activate( )
  {
    for( final ILayoutPart part : m_children )
      part.activate();
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutPart#deactivate()
   */
  @Override
  public void deactivate( )
  {
    for( final ILayoutPart part : m_children )
      part.deactivate();
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutPart#saveSelection()
   */
  @Override
  public void saveSelection( ) throws CoreException
  {
    for( final ILayoutPart part : m_children )
      part.saveSelection();
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutPart#restoreSelection(boolean)
   */
  @Override
  public void restoreSelection( final boolean clearState ) throws CoreException
  {
    for( final ILayoutPart part : m_children )
      part.restoreSelection( clearState );
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutPart#saveData(boolean)
   */
  @Override
  public void saveData( final boolean doSaveGml ) throws CoreException
  {
    for( final ILayoutPart part : m_children )
      part.saveData( doSaveGml );
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.AbstractLayoutPart#accept(org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutPartVisitor)
   */
  @Override
  public void accept( final ILayoutPartVisitor visitor ) throws CoreException
  {
    visitor.visit( this );

    for( final ILayoutPart part : m_children )
      part.accept( visitor );
  }

}