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

package org.kalypso.ui.editor.styleeditor.rule;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.kalypso.ogc.gml.filterdialog.dialog.FilterDialog;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.graphics.sld.Rule;

/**
 * TODO:
 * <ul>
 * <li>Tooltips + Messages for all fields</li>
 * <li>Implement LegendGraphic</li>
 * </ul>
 * 
 * @author F.Lindemann
 */
public class RuleComposite extends Composite
{
  private final IStyleInput<Rule> m_input;

  private RulePropertiesComposite m_rulePropertiesComposite;

  private SymbolizerTabViewer m_symbolizerTabViewer;

  public RuleComposite( final FormToolkit toolkit, final Composite parent, final IStyleInput<Rule> input )
  {
    super( parent, SWT.NONE );

    m_input = input;

    toolkit.adapt( this );

    GridLayoutFactory.fillDefaults().applyTo( this );

    createControl( toolkit, this );
  }

  private Rule getRule( )
  {
    return m_input.getData();
  }

  private void createControl( final FormToolkit toolkit, final Composite parent )
  {
    final Control rulePropertiesControl = createPropertiesControl( toolkit, parent );
    rulePropertiesControl.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );

    // TODO: the legend graphic is nowhere supported in Kalypso, so we ommit it here
    // if we show it, we should show it inside a ExpandableSection or in a popup dialog, its just too big
    // createLegendGraphicControl( toolkit, parent );

    final Control symbolizerTabsControl = createSymbolizerTabs( toolkit, parent );
    symbolizerTabsControl.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
  }

  private Control createPropertiesControl( final FormToolkit toolkit, final Composite parent )
  {
    final Section section = toolkit.createSection( parent, Section.TITLE_BAR | Section.TWISTIE | Section.DESCRIPTION );

    section.setText( "Rule Properties" );
    section.setDescription( "This section allows to edit general properties of the rule." );

    m_rulePropertiesComposite = new RulePropertiesComposite( toolkit, section, m_input );
    section.setClient( m_rulePropertiesComposite );

    return section;
  }

  private Control createSymbolizerTabs( final FormToolkit toolkit, final Composite parent )
  {
    final Section tabsSection = toolkit.createSection( parent, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION );

    tabsSection.setText( "Symbolizers" );
    tabsSection.setDescription( "Add, remove or edit the symbolizers of the rule." );

    m_symbolizerTabViewer = new SymbolizerTabViewer( toolkit, tabsSection, m_input );

    final CTabFolder ruleTabFolder = m_symbolizerTabViewer.getControl();
    tabsSection.setClient( ruleTabFolder );

    return tabsSection;
  }

  // private void createLegendGraphicControl( final FormToolkit toolkit, final Composite parent )
  // {
  // final IValueReceiver<Graphic> graphicReceiver = new IValueReceiver<Graphic>()
  // {
  // @Override
  // public void updateValue( final Graphic newValue )
  // {
  // handleLegendGraphicChanged( newValue );
  // }
  // };
  // m_graphicComposite = new GraphicComposite( toolkit, parent, graphicReceiver );
  // }

  // protected void handleLegendGraphicChanged( final Graphic newValue )
  // {
  // final Rule rule = getRule();
  // if( rule == null )
  // return;
  //
  // final LegendGraphic legendGraphic = rule.getLegendGraphic();
  // if( legendGraphic == null )
  // SLDFactory.createLegendGraphic();
  //
  // legendGraphic.setGraphic( newValue );
  // fireStyleChanged();
  // }

  protected void handleFilterButtonSelected( final Shell shell )
  {
    final Filter oldFilter = getRule().getFilter();
    final Filter clone = cloneFilter( oldFilter );

    final FilterDialog dialog = new FilterDialog( shell, m_input, getRule().getFilter(), null, null, false );
    final int open = dialog.open();
    if( open == Window.OK )
    {
      final Filter filter = dialog.getFilter();
      getRule().setFilter( filter );
      fireStyleChanged();
    }

    if( open == Window.CANCEL )
    {
      getRule().setFilter( clone );
      fireStyleChanged();
    }
  }

  private Filter cloneFilter( final Filter oldFilter )
  {
    try
    {
      if( oldFilter == null )
        return null;

      return oldFilter.clone();
    }
    catch( final CloneNotSupportedException e )
    {
      e.printStackTrace();
      return null;
    }
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.IStyleChanger#fireStyleChanged()
   */
  void fireStyleChanged( )
  {
    m_input.fireStyleChanged();
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

    final RuleComposite rhs = (RuleComposite) obj;

    final EqualsBuilder builder = new EqualsBuilder();
    builder.append( getRule(), rhs.getRule() );
    return builder.isEquals();
  }

  /**
   * Call, if style has changed.
   */
  public void updateControl( )
  {

    m_rulePropertiesComposite.updateControl();

    m_symbolizerTabViewer.refresh();
  }
}