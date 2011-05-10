/*--------------- Kalypso-Header --------------------------------------------------------------------

 This file is part of kalypso.
 Copyright (C) 2004, 2005 by:

 Technical University Hamburg-Harburg (TUHH)
 Institute of River and coastal engineering
 Denickestr. 22
 21073 Hamburg, Germany
 http://www.tuhh.de/wb

 and

 Bjoernsen Consulting Engineers (BCE)
 Maria Trost 3
 56070 Koblenz, Germany
 http://www.bjoernsen.de

 This library is free software; you can redistribute it and/or
 modify it under the terms of the GNU Lesser General Public
 License as published by the Free Software Foundation; either
 version 2.1 of the License, or (at your option) any later version.

 This library is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 Lesser General Public License for more details.

 You should have received a copy of the GNU Lesser General Public
 License along with this library; if not, write to the Free Software
 Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA

 Contact:

 E-Mail:
 belger@bjoernsen.de
 schlienger@bjoernsen.de
 v.doemming@tuhh.de

 ---------------------------------------------------------------------------------------------------*/

package org.kalypso.ui.editor.styleeditor.style;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.eclipse.jface.viewers.ITabItem;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.rule.RuleComposite;
import org.kalypsodeegree.graphics.sld.Rule;

/**
 * @author F.Lindemann
 */
public class RuleTabItem implements ITabItem
{
  private RuleComposite m_ruleComposite;

  private final IStyleInput<Rule> m_input;


  public RuleTabItem( final IStyleInput<Rule> input )
  {
    m_input = input;
  }

  public Rule getRule( )
  {
    return m_input.getData();
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.IRuleTabItem#getItemLabel()
   */
  @Override
  public String getItemLabel( )
  {
    final String title = getRule().getTitle();
    if( title != null )
      return title;

    final String name = getRule().getName();
    if( name != null )
      return name;

    return "<No Name>";
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.IRuleTabItem#getItemImage()
   */
  @Override
  public Image getItemImage( )
  {
    return null;
  }

  @Override
  public Control createItemControl( final FormToolkit toolkit, final Composite parent )
  {
    m_ruleComposite = new RuleComposite( toolkit, parent, m_input );
    return m_ruleComposite;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabItem#updateItemControl()
   */
  @Override
  public void updateItemControl( )
  {
    m_ruleComposite.updateControl();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return getItemLabel();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    final HashCodeBuilder builder = new HashCodeBuilder();
    builder.append( getRule() );
    return builder.toHashCode();
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals( final Object obj )
  {
    if( obj == null )
      return false;

    if( obj == this )
      return true;

    if( obj.getClass() != getClass() )
      return false;

    final RuleTabItem rhs = (RuleTabItem) obj;

    final EqualsBuilder builder = new EqualsBuilder();
    builder.append( getRule(), rhs.getRule() );
    return builder.isEquals();
  }
}