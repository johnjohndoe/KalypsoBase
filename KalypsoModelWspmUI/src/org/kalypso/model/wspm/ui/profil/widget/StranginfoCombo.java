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
package org.kalypso.model.wspm.ui.profil.widget;

import org.eclipse.jface.action.IContributionItem;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.kalypso.contribs.eclipse.jface.action.DropdownContributionItem;
import org.kalypso.model.wspm.core.strang.IStranginfoListener;
import org.kalypso.model.wspm.core.strang.ProfileInfo;
import org.kalypso.model.wspm.core.strang.StrangInfo;

/**
 * @author gernot
 */
public class StranginfoCombo implements IStranginfoListener, SelectionListener
{
  private final DropdownContributionItem<ProfileInfo> m_comboitem = new DropdownContributionItem<>( null );

  private StrangInfo m_info = null;

  public StranginfoCombo( )
  {
    m_comboitem.addSelectionListener( this );
  }

  public void setInfo( final StrangInfo info )
  {
    if( m_info != null )
    {
      m_info.removeStranginfoListener( this );
    }

    m_info = info;

    if( m_info == null )
    {
      m_comboitem.setItems( null );
    }
    else
    {
      m_info.addStranginfoListener( this );

      m_comboitem.setItems( m_info.getInfos() );
      onIndexChanged( m_info );
    }
  }

  /**
   * @see org.kalypso.model.wspm.core.strang.IStranginfoListener#onIndexChanged(org.kalypso.model.wspm.core.strang.StrangInfo)
   */
  @Override
  public void onIndexChanged( final StrangInfo source )
  {
    if( source != null )
    {
      final ProfileInfo oldItem = m_comboitem.getSelectedItem();
      if( oldItem != m_info.getInfo() )
      {
        m_comboitem.setSelectedItem( m_info.getInfo() );
      }
    }
  }

  public IContributionItem getItem( )
  {
    return m_comboitem;
  }

  /**
   * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
   */
  @Override
  public void widgetSelected( final SelectionEvent e )
  {
    if( m_info == null )
      return;

    final ProfileInfo selectedItem = m_comboitem.getSelectedItem();

    m_info.setInfo( selectedItem );

    onIndexChanged( m_info );
  }

  /**
   * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
   */
  @Override
  public void widgetDefaultSelected( final SelectionEvent e )
  {
  }

  /**
   * @see org.kalypso.model.wspm.core.strang.IStranginfoListener#onTryChangeIndex(org.kalypso.model.wspm.core.strang.StrangInfo)
   */
  @Override
  public boolean onTryChangeIndex( final StrangInfo source )
  {
    return true;
  }
}
