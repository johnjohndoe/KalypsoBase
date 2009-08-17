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
package org.kalypso.contribs.eclipse.ui.controls;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.events.IExpansionListener;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;

/**
 * @author Dirk Kuch
 */
public class ScrolledSection
{
  protected final FormToolkit m_toolkit;

  public Section m_section;

  protected Composite m_parent;

  protected final boolean m_scrolledBody;

  protected ScrolledForm m_scrolledForm = null;

  protected final int m_sectionStyle;

  private Composite m_composite;

  public ScrolledSection( final Composite parent, final FormToolkit toolkit, final int sectionStyle, final boolean scrolledBody )
  {
    m_parent = parent;
    m_sectionStyle = sectionStyle;
    m_scrolledBody = scrolledBody;
    m_toolkit = toolkit;
  }

  public ScrolledForm getScrolledForm( )
  {
    return m_scrolledForm;
  }

  public Composite getBody( )
  {
    if( m_scrolledBody )
      return m_scrolledForm;
    else
      return m_composite;
  }

  public Section getSection( )
  {
    return m_section;
  }

  public void reflow( )
  {
    if( m_scrolledBody )
    {
      m_scrolledForm.reflow( true );
    }

  }

  public void setDescription( final String description )
  {
    if( description != null )
    {
      m_section.setDescription( description );
    }
  }

  protected void setLayout( final GridData secLayoutExpanded, final GridData secLayoutCollapsed )
  {
    if( m_section.isExpanded() )
    {
      m_section.setLayoutData( secLayoutExpanded );
    }
    else
    {
      m_section.setLayoutData( secLayoutCollapsed );
    }
  }

  public Composite setup( final String title, final GridData secLayoutExpanded, final GridData secLayoutCollapsed )
  {
    m_section = m_toolkit.createSection( m_parent, m_sectionStyle );
    m_section.setText( title );

    setLayout( secLayoutExpanded, secLayoutCollapsed );

    final IExpansionListener lnerExp = new IExpansionListener()
    {
      public void expansionStateChanged( final ExpansionEvent e )
      {
        setLayout( secLayoutExpanded, secLayoutCollapsed );
        m_parent.layout();
      }

      public void expansionStateChanging( final ExpansionEvent e )
      {
      }
    };

    m_section.addExpansionListener( lnerExp );

    if( m_scrolledBody )
    {
      m_scrolledForm = m_toolkit.createScrolledForm( m_section );
      m_section.setClient( m_scrolledForm );

      return m_scrolledForm.getBody();
    }
    else
    {
      m_composite = m_toolkit.createComposite( m_section );
      m_section.setClient( m_composite );

      return m_composite;
    }
  }
}
