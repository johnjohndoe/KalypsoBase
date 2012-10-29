/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.ogc.gml.map.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ui.views.contentoutline.ContentOutline;
import org.kalypso.commons.command.ICommandTarget;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.map.IMapPanel;

/**
 * @author Gernot Belger
 */
public class ThemeInfoWidget extends AbstractThemeInfoWidget
{
  private final ISelectionChangedListener m_selectionListener = new ISelectionChangedListener()
  {
    @Override
    public void selectionChanged( final SelectionChangedEvent event )
    {
      handleSelectionChanged( event.getSelection() );
    }
  };

  private ISelectionProvider m_selectionProvider = null;

  public ThemeInfoWidget( )
  {
    super( Messages.getString( "org.kalypso.ogc.gml.map.widgets.ThemeInfoWidget.0" ), Messages.getString( "org.kalypso.ogc.gml.map.widgets.ThemeInfoWidget.1" ) ); //$NON-NLS-1$ //$NON-NLS-2$

    setNoThemesTooltip( Messages.getString( "org.kalypso.ogc.gml.map.widgets.ThemeInfoWidget.2" ) ); //$NON-NLS-1$
  }

  public ThemeInfoWidget( final String name, final String toolTip )
  {
    super( name, toolTip );
  }

  /**
   * @see org.kalypso.ogc.gml.map.widgets.AbstractWidget#activate(org.kalypso.commons.command.ICommandTarget,
   *      org.kalypso.ogc.gml.map.MapPanel)
   */
  @Override
  public void activate( final ICommandTarget commandPoster, final IMapPanel mapPanel )
  {
    super.activate( commandPoster, mapPanel );

    final ContentOutline outlineView = FindElementMapWidget.findOutlineView();
    if( outlineView == null )
    {
      mapPanel.setMessage( Messages.getString( "org.kalypso.ogc.gml.map.widgets.ThemeInfoWidget.3" ) ); //$NON-NLS-1$
      return;
    }

    // REMARK: we get selection from outline, so outlines mapPanel should be the same as the widgets ones
    final IMapPanel outlineMapPanel = FindElementMapWidget.findOutlineMapPanel( outlineView );
    if( outlineMapPanel != mapPanel )
    {
      mapPanel.setMessage( Messages.getString( "org.kalypso.ogc.gml.map.widgets.ThemeInfoWidget.4" ) ); //$NON-NLS-1$
      return;
    }

    m_selectionProvider = outlineView.getSite().getSelectionProvider();
    m_selectionProvider.addSelectionChangedListener( m_selectionListener );

    handleSelectionChanged( m_selectionProvider.getSelection() );
  }

  @Override
  public void finish( )
  {
    super.finish();

    if( m_selectionProvider != null )
    {
      m_selectionProvider.removeSelectionChangedListener( m_selectionListener );
      m_selectionProvider = null;
    }
  }

  protected void handleSelectionChanged( final ISelection selection )
  {
    final List<IKalypsoTheme> themes = new ArrayList<>();

    final IStructuredSelection sel = (IStructuredSelection) selection;
    final Object[] selectedElements = sel.toArray();
    for( final Object object : selectedElements )
    {
      final IKalypsoTheme theme = findTheme( object );
      if( theme != null )
        themes.add( theme );
    }

    setThemes( themes.toArray( new IKalypsoTheme[themes.size()] ) );
  }

  private IKalypsoTheme findTheme( final Object object )
  {
    if( object instanceof IKalypsoTheme )
      return (IKalypsoTheme) object;

    if( object instanceof IAdaptable )
    {
      final IAdaptable adapable = (IAdaptable) object;
      final IKalypsoTheme theme = (IKalypsoTheme) adapable.getAdapter( IKalypsoTheme.class );
      if( theme != null )
        return theme;
    }

    // other checks needed?
    return null;
  }
}