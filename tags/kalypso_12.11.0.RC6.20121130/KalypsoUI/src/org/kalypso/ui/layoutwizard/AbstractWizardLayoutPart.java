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
package org.kalypso.ui.layoutwizard;

import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.ISourceProvider;
import org.eclipse.ui.services.IServiceLocator;
import org.kalypso.core.layoutwizard.ILayoutPageContext;
import org.kalypso.core.layoutwizard.part.AbstractLayoutPart;

/**
 * @author Dirk Kuch
 */
public abstract class AbstractWizardLayoutPart extends AbstractLayoutPart
{
  private ISourceProvider m_sourceProvider;

  public AbstractWizardLayoutPart( final String id, final ILayoutPageContext context )
  {
    super( id, context );
  }

  public AbstractWizardLayoutPart( final String id, final ILayoutPageContext context, final ISelectionProvider selectionProvider )
  {
    super( id, context, selectionProvider );
  }

  @Override
  public void dispose( )
  {
    destroySourceProvider();
  }

  @Override
  public ILayoutPageContext getContext( )
  {
    return super.getContext();
  }

  /**
   * Used for parts that need to activate a {@link ISourceProvider} on activation.<br/>
   * Default implementation returns <code>null</code>.<br/>
   * Implementors should overwrite and create their source provider here. This abstract implementation will handle the
   * source life-cycle.
   */
  protected ISourceProvider createSourceProvider( @SuppressWarnings( "unused" ) final IServiceLocator context )
  {
    return null;
  }

  private void destroySourceProvider( )
  {
    if( m_sourceProvider != null )
    {
      m_sourceProvider.dispose();
      m_sourceProvider = null;
    }
  }

  @Override
  public void activate( )
  {
    final IServiceLocator context = getContext();
    destroySourceProvider();

    m_sourceProvider = createSourceProvider( context );
  }

  public boolean isActivated( )
  {
    return m_sourceProvider != null;
  }

  @Override
  public void deactivate( )
  {
    destroySourceProvider();
  }
}