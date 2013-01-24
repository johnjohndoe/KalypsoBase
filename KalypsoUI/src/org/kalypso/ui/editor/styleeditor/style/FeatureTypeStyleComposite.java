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
package org.kalypso.ui.editor.styleeditor.style;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.kalypso.contribs.eclipse.swt.layout.LayoutHelper;

/**
 * @author Gernot Belger
 */
public class FeatureTypeStyleComposite extends Composite
{
  private final IFeatureTypeStyleInput m_input;

  private RuleTabViewer m_ruleTabViewer;

  private FeatureTypeStylePropertiesComposite m_propertiesComposite;

  public FeatureTypeStyleComposite( final FormToolkit toolkit, final Composite parent, final IFeatureTypeStyleInput input )
  {
    super( parent, SWT.NONE );

    m_input = input;

    setLayout( LayoutHelper.createGridLayout() );
    toolkit.adapt( this );

    if( input.getConfig().isFeatureTypeStyleCompositeShowProperties() )
    {
      final Control propertiesControl = createPropertiesComposite( toolkit );
      propertiesControl.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );
    }

    final Control ruleTabsControl = createRuleTabViewer( toolkit );
    ruleTabsControl.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
  }

  protected Section createPropertiesComposite( final FormToolkit toolkit )
  {
    final Section section = toolkit.createSection( this, Section.TITLE_BAR | Section.TWISTIE | Section.DESCRIPTION );
    section.setText( "Style Properties" );
    section.setDescription( "This section allows to edit general properties of the style." );

    m_propertiesComposite = new FeatureTypeStylePropertiesComposite( toolkit, section, m_input );
    section.setClient( m_propertiesComposite );

    return section;
  }

  protected Control createRuleTabViewer( final FormToolkit toolkit )
  {
    final SectionPart tabSectionPart = new SectionPart( this, toolkit, Section.TITLE_BAR | Section.DESCRIPTION );
    final Section section = tabSectionPart.getSection();
    section.setText( "Rules" );
    section.setDescription( "Add, remove or edit the rules of the style." );

    m_ruleTabViewer = new RuleTabViewer( toolkit, section, m_input );
    section.setClient( m_ruleTabViewer.getControl() );

    return section;
  }

  public int getSelectedStyle( )
  {
    return m_ruleTabViewer.getControl().getSelectionIndex();
  }

  /**
   * Call, if style has changed.
   */
  public void updateControl( )
  {
    if( m_propertiesComposite != null )
      m_propertiesComposite.updateControl();

    m_ruleTabViewer.refresh();
  }
}