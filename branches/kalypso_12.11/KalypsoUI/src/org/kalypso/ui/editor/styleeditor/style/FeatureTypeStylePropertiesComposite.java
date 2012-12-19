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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.validation.IValidator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.databinding.swt.ISWTObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.kalypso.commons.databinding.IDataBinding;
import org.kalypso.commons.databinding.conversion.StringToQNameConverter;
import org.kalypso.commons.databinding.forms.DatabindingForm;
import org.kalypso.commons.databinding.validation.StringBlankValidator;
import org.kalypso.contribs.eclipse.ui.forms.ToolkitUtils;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ui.editor.styleeditor.MessageBundle;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.SLDBinding;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;

/**
 * @author Gernot Belger
 */
public class FeatureTypeStylePropertiesComposite extends Composite
{
  private final IStyleInput<FeatureTypeStyle> m_input;

  private final IDataBinding m_binding;

  private final ScrolledForm m_form;

  public FeatureTypeStylePropertiesComposite( final FormToolkit toolkit, final Composite parent, final IStyleInput<FeatureTypeStyle> input )
  {
    super( parent, SWT.NONE );

    m_input = input;

    toolkit.adapt( this );
    setLayout( new FillLayout() );

    m_form = ToolkitUtils.createScrolledForm( toolkit, this, SWT.H_SCROLL );
    final Composite body = m_form.getBody();
    toolkit.adapt( body );
    body.setLayout( new GridLayout( 2, false ) );

    m_binding = new DatabindingForm( m_form, toolkit );

    createNameField( toolkit, body );
    createTitleField( toolkit, body );
    createAbstractField( toolkit, body );
    createFeatureTypeField( toolkit, body );
    // createSemanticTypeIdentifierTypeField( toolkit, body );
  }

  private void createNameField( final FormToolkit toolkit, final Composite parent )
  {
    final String tooltip = Messages.getString( "FeatureTypeStylePropertiesComposite_0" ); //$NON-NLS-1$
    toolkit.createLabel( parent, Messages.getString( "FeatureTypeStylePropertiesComposite_1" ) ).setToolTipText( tooltip ); //$NON-NLS-1$

    final Text control = toolkit.createText( parent, StringUtils.EMPTY );
    control.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    control.setMessage( MessageBundle.STYLE_EDITOR_FIELD_EMPTY );
    control.setToolTipText( tooltip );

    final IValidator blankValidator = new StringBlankValidator( IStatus.WARNING, StringBlankValidator.DEFAULT_WARNING_MESSAGE );
    final ISWTObservableValue targetValue = SWTObservables.observeText( control, SLDBinding.TEXT_DEFAULT_EVENTS );
    m_binding.bindValue( targetValue, new FeatureTypeStyleNameValue( m_input ), blankValidator );
  }

  private void createTitleField( final FormToolkit toolkit, final Composite parent )
  {
    final String tooltip = Messages.getString( "FeatureTypeStylePropertiesComposite_2" ); //$NON-NLS-1$
    toolkit.createLabel( parent, Messages.getString( "FeatureTypeStylePropertiesComposite_3" ) ).setToolTipText( tooltip ); //$NON-NLS-1$

    final Text control = toolkit.createText( parent, StringUtils.EMPTY );
    control.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, false, false ) );
    control.setMessage( MessageBundle.STYLE_EDITOR_FIELD_EMPTY );
    control.setToolTipText( tooltip );

    final ISWTObservableValue targetValue = SWTObservables.observeText( control, SLDBinding.TEXT_DEFAULT_EVENTS );
    m_binding.bindValue( targetValue, new FeatureTypeStyleTitleValue( m_input ) );
  }

  private void createAbstractField( final FormToolkit toolkit, final Composite parent )
  {
    final String tooltip = Messages.getString( "FeatureTypeStylePropertiesComposite_4" ); //$NON-NLS-1$
    toolkit.createLabel( parent, Messages.getString( "FeatureTypeStylePropertiesComposite_5" ) ).setToolTipText( tooltip ); //$NON-NLS-1$

    final Text control = toolkit.createText( parent, StringUtils.EMPTY );
    control.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    control.setMessage( MessageBundle.STYLE_EDITOR_FIELD_EMPTY );
    control.setToolTipText( tooltip );

    final ISWTObservableValue targetValue = SWTObservables.observeText( control, SLDBinding.TEXT_DEFAULT_EVENTS );
    m_binding.bindValue( targetValue, new FeatureTypeStyleAbstractValue( m_input ) );
  }

  private void createFeatureTypeField( final FormToolkit toolkit, final Composite parent )
  {
    final String tooltip = Messages.getString( "FeatureTypeStylePropertiesComposite_6" ); //$NON-NLS-1$
    toolkit.createLabel( parent, Messages.getString( "FeatureTypeStylePropertiesComposite_7" ) ).setToolTipText( tooltip ); //$NON-NLS-1$

    final ComboViewer viewer = new ComboViewer( parent, SWT.NONE );
    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.setLabelProvider( new LabelProvider() );
    viewer.setSorter( new ViewerSorter() );
    viewer.setInput( findTypes() );

    final Combo control = viewer.getCombo();
    control.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    control.setToolTipText( tooltip );
    toolkit.adapt( control, true, true );

    final ISWTObservableValue controlObserver = SWTObservables.observeText( control );
    final IConverter converter = new StringToQNameConverter();
    m_binding.bindValue( controlObserver, new FeatureTypeStyleFeatureTypeValue( m_input ), converter );
  }

  private String[] findTypes( )
  {
    final IFeatureType featureType = m_input.getFeatureType();
    if( featureType == null )
      return new String[] {};

    final IFeatureType[] possibleTypes = GMLSchemaUtilities.getSubstituts( featureType, null, true, true );
    final String[] qnames = new String[possibleTypes.length];
    for( int i = 0; i < possibleTypes.length; i++ )
      qnames[i] = possibleTypes[i].getQName().toString();

    return qnames;
  }

  public void updateControl( )
  {
    if( isDisposed() )
      return;

    m_binding.getBindingContext().updateTargets();

    m_form.reflow( true );
  }
}