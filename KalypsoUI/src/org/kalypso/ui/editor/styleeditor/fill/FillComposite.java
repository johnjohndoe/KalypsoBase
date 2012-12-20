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
package org.kalypso.ui.editor.styleeditor.fill;

import java.awt.Color;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.kalypso.commons.databinding.IDataBinding;
import org.kalypso.commons.databinding.conversion.AwtToSwtColorConverter;
import org.kalypso.commons.databinding.conversion.SwrToAwtColorConverter;
import org.kalypso.commons.databinding.forms.DatabindingForm;
import org.kalypso.contribs.eclipse.swt.ColorUtilities;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.preview.FillPreview;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.graphics.sld.Fill;

/**
 * @author Thomas Jung
 */
public class FillComposite extends Composite
{
  /**
   * If this style is used, a preview will be shown below the panel.
   */
  public static final int PREVIEW = 1 << 1;

  /**
   * If this style is used, the graphic section will be hidden.
   */
  public static final int HIDE_GRAPHIC = 1 << 2;

  private FillPreview m_previewComp;

  private Group m_previewGroup;

  private final IStyleInput<Fill> m_input;

  private final IDataBinding m_binding;

  private GraphicFillSection m_fillSection;

  public FillComposite( final FormToolkit toolkit, final Composite parent, final IStyleInput<Fill> input, final int sldStyle )
  {
    super( parent, SWT.NONE );

    m_input = input;

    ControlUtils.addDisposeListener( this );

    setLayout( new FillLayout() );
    toolkit.adapt( this );

    final ScrolledForm form = toolkit.createScrolledForm( this );
    final Composite body = form.getBody();
    GridLayoutFactory.fillDefaults().numColumns( 2 ).applyTo( body );

    m_binding = new DatabindingForm( form, toolkit );

    createColorControl( toolkit, body );
    createOpacityControl( toolkit, body );

    if( (sldStyle & HIDE_GRAPHIC) == 0 )
      createGraphicControl( toolkit, body );

    if( (sldStyle & PREVIEW) != 0 )
      createPreviewControl( toolkit, body );
  }

  private void createColorControl( final FormToolkit toolkit, final Composite parent )
  {
    toolkit.createLabel( parent, Messages.getString( "org.kalypso.ui.editor.sldEditor.FillEditorComposite.0" ) ); //$NON-NLS-1$

    final Label colorLabel = toolkit.createLabel( parent, StringUtils.EMPTY, SWT.BORDER );
    final GridData gridData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    gridData.widthHint = 16;
    colorLabel.setLayoutData( gridData );
    colorLabel.setCursor( getDisplay().getSystemCursor( SWT.CURSOR_HAND ) );

    final IObservableValue target = SWTObservables.observeBackground( colorLabel );
    final IObservableValue model = new FillColorValue( m_input );

    m_binding.bindValue( target, model, new SwrToAwtColorConverter(), new AwtToSwtColorConverter( JFaceResources.getColorRegistry(), toString() ) );

    colorLabel.addMouseListener( new MouseAdapter()
    {
      @Override
      public void mouseDown( final MouseEvent e )
      {
        final ColorDialog colorDialog = new ColorDialog( getShell() );
        colorDialog.setRGB( ColorUtilities.toRGB( (Color)model.getValue() ) );
        final RGB chosenColor = colorDialog.open();
        if( chosenColor != null )
          model.setValue( ColorUtilities.toAwtColor( chosenColor ) );
      }
    } );
  }

  private void createOpacityControl( final FormToolkit toolkit, final Composite parent )
  {
    toolkit.createLabel( parent, Messages.getString( "org.kalypso.ui.editor.sldEditor.FillEditorComposite.14" ) ); //$NON-NLS-1$

    final Spinner spinner = new Spinner( parent, SWT.BORDER );
    toolkit.adapt( spinner, true, true );
    spinner.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    final IObservableValue target = SWTObservables.observeSelection( spinner );
    final FillOpacityValue model = new FillOpacityValue( m_input );

    model.configureSpinner( spinner );

    m_binding.bindValue( target, model );
  }

  private void createGraphicControl( final FormToolkit toolkit, final Composite parent )
  {
    m_fillSection = new GraphicFillSection( toolkit, parent, m_input );
    m_fillSection.getSection().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 2, 1 ) );
  }

  private void createPreviewControl( final FormToolkit toolkit, final Composite parent )
  {
    m_previewGroup = new Group( parent, SWT.NONE );
    m_previewGroup.setLayout( new GridLayout() );
    toolkit.adapt( m_previewGroup );

    final GridData previewGridData = new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 );
    m_previewGroup.setLayoutData( previewGridData );
    m_previewGroup.setText( Messages.getString( "org.kalypso.ui.editor.sldEditor.FillEditorComposite.16" ) ); //$NON-NLS-1$

    /* preview */
    final Point size = new Point( SWT.DEFAULT, 32 );
    m_previewComp = new FillPreview( m_previewGroup, size, m_input );
    m_previewComp.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
  }

  public void updateControl( )
  {
    m_binding.getBindingContext().updateTargets();

    if( m_fillSection != null )
      m_fillSection.updateControl();

    if( m_previewComp != null )
      m_previewComp.updateControl();
  }
}