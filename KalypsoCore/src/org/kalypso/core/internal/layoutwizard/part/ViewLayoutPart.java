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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.views.IViewDescriptor;
import org.eclipse.ui.views.IViewRegistry;
import org.kalypso.core.KalypsoCorePlugin;
import org.kalypso.core.i18n.Messages;
import org.kalypso.core.layoutwizard.ILayoutPageContext;
import org.kalypso.core.layoutwizard.part.AbstractLayoutPart;

/**
 * A layout part which checks for view ids.
 * 
 * @author Holger Albert, Gernot Belger
 */
public class ViewLayoutPart extends AbstractLayoutPart
{
  private String m_type;

  private IViewPart m_view;

  public ViewLayoutPart( final String id, final ILayoutPageContext context, final String type )
  {
    super( id, context );

    m_type = type;
    m_view = null;
  }

  @Override
  public void init( ) throws CoreException
  {
    final IViewRegistry viewRegistry = PlatformUI.getWorkbench().getViewRegistry();
    final IViewDescriptor viewDescriptor = viewRegistry.find( m_type );
    if( viewDescriptor == null )
    {
      final String message = String.format( Messages.getString("ViewLayoutPart_0"), m_type ); //$NON-NLS-1$
      throw new CoreException( new Status( IStatus.ERROR, KalypsoCorePlugin.getID(), message, null ) );
    }
    m_view = viewDescriptor.createView();

    // TODO Call init with a view site in future...
    // TODO Perhaps give the Wizard context in a site (wrapper)...
    // m_view.init( null );
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutPart#createControl(org.eclipse.ui.forms.widgets.FormToolkit,
   *      org.eclipse.swt.widgets.Composite)
   */
  @Override
  public Control createControl( final Composite parent, final FormToolkit toolkit )
  {
    final Composite main = toolkit.createComposite( parent, SWT.NONE );
    main.setLayout( new FillLayout() );
    m_view.createPartControl( main );

    return main;
  }

  /**
   * @see org.kalypso.hwv.ui.wizards.calculation.modelpages.layout.ILayoutPart#dispose()
   */
  @Override
  public void dispose( )
  {
    if( m_view != null )
      m_view.dispose();

    m_type = null;
    m_view = null;
  }
}