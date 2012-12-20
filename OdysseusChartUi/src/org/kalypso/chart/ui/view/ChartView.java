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
package org.kalypso.chart.ui.view;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;
import org.kalypso.chart.ui.workbench.ChartPartComposite;
import org.kalypso.contribs.eclipse.ui.IContentOutlineProvider;

/**
 * @author Thomas Jung
 */
public class ChartView extends ViewPart implements IContentOutlineProvider
{
  public static final String ID = "org.kalypso.chart.ui.view.ChartView"; //$NON-NLS-1$

  private final ChartPartComposite m_chartPartComposite = new ChartPartComposite( this );

  @Override
  public void dispose( )
  {
    // REMARK: no, the composite is automatically disposed if the parent is disposed.
    // Disposing it here might lead to an "Widget disposed too early for part" exception.
    // m_chartPartComposite.dispose();

    super.dispose();
  }

  @Override
  public void init( final IViewSite site ) throws PartInitException
  {
    super.init( site );

    m_chartPartComposite.init( site );
  }

  /**
   * Only way to set some input into this view. The content of the input should be a .kod - xml.
   */
  public void setInput( final IEditorInput input )
  {
    m_chartPartComposite.loadInput( input );

    updatePartName();
  }

  @Override
  public void createPartControl( final Composite parent )
  {
    final ChartPartComposite partComposite = m_chartPartComposite;
    final Composite control = partComposite.createControl( parent );
    // REMARK: dispose chart when control is disposed, see above.
    control.addDisposeListener( new DisposeListener()
    {
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        partComposite.dispose();
      }
    } );

    updatePartName();
  }

  private void updatePartName( )
  {
    setPartName( m_chartPartComposite.getPartName() );
  }

  @Override
  public void setFocus( )
  {
    m_chartPartComposite.setFocus();
  }

  @Override
  public Object getAdapter( final Class adapter )
  {
    final Object adapted = m_chartPartComposite.adapt( adapter );
    if( adapted != null )
      return adapted;

    return super.getAdapter( adapter );
  }
}