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
/*
 * Created on 26.07.2004
 *
 */
package org.kalypso.ui.editor.styleeditor.symbolizer;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.kalypso.commons.databinding.conversion.ITypedConverter;
import org.kalypso.ui.editor.styleeditor.MessageBundle;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.preview.SymbolizerPreview;
import org.kalypso.ui.editor.styleeditor.preview.TextPreview;
import org.kalypso.ui.editor.styleeditor.util.IValueReceiver;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.graphics.sld.Font;
import org.kalypsodeegree.graphics.sld.ParameterValueType;
import org.kalypsodeegree.graphics.sld.TextSymbolizer;

/**
 * @author F.Lindemann
 */
public class TextSymbolizerComposite extends AbstractSymbolizerComposite<TextSymbolizer>
{
  private FontComposite m_fontChooserPanel;

  private ScrolledForm m_commonForm;

  private HaloSection m_haloSection;

  private LabelPlacementSection m_placementSection;

  public TextSymbolizerComposite( final FormToolkit toolkit, final Composite parent, final IStyleInput<TextSymbolizer> input )
  {
    super( toolkit, parent, input );
  }

  @Override
  protected SymbolizerPreview<TextSymbolizer> createPreview( final Composite parent, final Point size, final IStyleInput<TextSymbolizer> input )
  {
    return new TextPreview( parent, size, input );
  }

  @Override
  protected Control createContent( final FormToolkit toolkit, final Composite parent )
  {
    final Composite panel = toolkit.createComposite( parent );
    GridLayoutFactory.fillDefaults().applyTo( panel );

    final Control upperPanel = createCommonAndPlacementSections( toolkit, panel );
    upperPanel.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );

    m_haloSection = new HaloSection( toolkit, panel, getInput() );
    m_haloSection.getSection().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    // TODO: there is another fill-element at any TextSymbolizer; it is not used by Kalypso however; what is it
    // supposed to do?

    return panel;
  }

  private Control createCommonAndPlacementSections( final FormToolkit toolkit, final Composite parent )
  {
    final Composite panel = toolkit.createComposite( parent );
    GridLayoutFactory.fillDefaults().numColumns( 2 ).equalWidth( true ).spacing( 0, 0 ).applyTo( panel );

    createFontAndLabelSection( toolkit, panel ).setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    createPlacementSection( toolkit, panel ).setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    return panel;
  }

  private Control createFontAndLabelSection( final FormToolkit toolkit, final Composite parent )
  {
    final Section section = toolkit.createSection( parent, ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED );
    section.setText( Messages.getString( "TextSymbolizerComposite_0" ) ); //$NON-NLS-1$

    m_commonForm = toolkit.createScrolledForm( section );
    m_commonForm.setExpandHorizontal( true );
    m_commonForm.setExpandVertical( true );
    section.setClient( m_commonForm );

    final Composite body = m_commonForm.getBody();
    GridLayoutFactory.fillDefaults().numColumns( 3 ).applyTo( body );

    createLabelControl( toolkit, body );
    createFontControl( toolkit, body );

    return section;
  }

  private Control createPlacementSection( final FormToolkit toolkit, final Composite parent )
  {
    m_placementSection = new LabelPlacementSection( toolkit, parent, getInput() );
    return m_placementSection.getSection();
  }

  private void createLabelControl( final FormToolkit toolkit, final Composite parent )
  {
    toolkit.createLabel( parent, MessageBundle.STYLE_EDITOR_LABEL );

    final ComboViewer labelViewer = new ComboViewer( parent, SWT.DROP_DOWN );

    final Control combo = labelViewer.getControl();

    toolkit.adapt( combo, true, true );
    combo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    final ITypedConverter<ParameterValueType, String> converter = new ParameterValueTypeToString();

    final TextSymbolizerLabelField model = new TextSymbolizerLabelField( getInput(), converter );
    model.configureViewer( labelViewer );

    final IObservableValue target = SWTObservables.observeText( combo );

    getBinding().bindValue( target, model, new StringToParameterValueTypeConverter(), converter );

    getToolkit().createLabel( parent, StringUtils.EMPTY );
  }

  private void createFontControl( final FormToolkit toolkit, final Composite panel )
  {
    final IValueReceiver<Font> fontReceiver = new IValueReceiver<Font>()
    {
      @Override
      public void updateValue( final Font newFont )
      {
        updateFont( newFont );
      }
    };

    // TODO:
    // - as font css-parameters may contain property-references, we need a different font control
    // - we should allow for font removal/addition similar to other elements (halo/placement)
    // - font editor should allow to edit single properties (not via FontDialog)
    m_fontChooserPanel = new FontComposite( toolkit, panel, fontReceiver );
    m_fontChooserPanel.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1 ) );
  }

  protected void updateFont( final Font newFont )
  {
    final TextSymbolizer symbolizer = getSymbolizer();
    symbolizer.setFont( newFont );

    fireStyleChanged();
  }

  @Override
  public void doUpdateControl( )
  {
    // Binding already updated in super

    if( m_fontChooserPanel != null )
    {
      final TextSymbolizer symbolizer = getSymbolizer();
      final Font font = symbolizer.getFont();
      m_fontChooserPanel.setFont( font );
    }

    m_placementSection.updateControl();

    m_haloSection.updateControl();

    m_commonForm.reflow( true );
  }
}