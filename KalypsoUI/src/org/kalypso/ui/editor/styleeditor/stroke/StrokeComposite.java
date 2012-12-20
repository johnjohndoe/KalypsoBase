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
package org.kalypso.ui.editor.styleeditor.stroke;

import java.awt.Color;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.kalypso.commons.databinding.IDataBinding;
import org.kalypso.commons.databinding.conversion.AwtToSwtColorConverter;
import org.kalypso.commons.databinding.conversion.FloatArrayToStringConverter;
import org.kalypso.commons.databinding.conversion.StringToFloatArrayConverter;
import org.kalypso.commons.databinding.conversion.SwrToAwtColorConverter;
import org.kalypso.commons.databinding.forms.DatabindingForm;
import org.kalypso.commons.databinding.validation.NumberNotNegativeValidator;
import org.kalypso.contribs.eclipse.swt.ColorUtilities;
import org.kalypso.contribs.eclipse.swt.layout.Layouts;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.contribs.eclipse.swt.widgets.MenuButton;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.SLDBinding;
import org.kalypso.ui.editor.styleeditor.preview.StrokePreview;
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.graphics.sld.DrawingUtils;
import org.kalypsodeegree.graphics.sld.Stroke;

/**
 * TODO: implement editing of GraphicFill<br/>
 * Composite, which gives the most important editing tools for a given stroke.
 * 
 * @author Thomas Jung
 */
public class StrokeComposite extends Composite
{
  /**
   * If this style is used, a preview will be shown below the panel.
   */
  public static final int PREVIEW = 1 << 1;

  /**
   * If this style is used, the graphic section will be hidden.
   */
  public static final int HIDE_GRAPHIC = 1 << 2;

  private StrokePreview m_previewComp;

  private Group m_previewGroup;

  private final Section m_lineDetailsSection;

  private final IStyleInput<Stroke> m_input;

  private final IDataBinding m_binding;

  private GraphicStrokeSection m_strokeSection;

  public StrokeComposite( final FormToolkit toolkit, final Composite parent, final IStyleInput<Stroke> input, final int sldStyle )
  {
    super( parent, SWT.NONE );

    m_input = input;

    toolkit.adapt( this );
    setLayout( new FillLayout() );

    final ScrolledForm form = toolkit.createScrolledForm( this );

    final Composite body = form.getBody();
    body.setLayout( new GridLayout( 3, false ) );

    m_binding = new DatabindingForm( form, toolkit );

    ControlUtils.addDisposeListener( this );

    createColorControl( toolkit, body );
    createOpacityControl( toolkit, body );
    createWidthControl( toolkit, body );

    m_lineDetailsSection = toolkit.createSection( body, ExpandableComposite.TITLE_BAR | ExpandableComposite.TWISTIE );
    m_lineDetailsSection.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false, 3, 1 ) );

    final Composite detailsPanel = toolkit.createComposite( m_lineDetailsSection );
    detailsPanel.setLayout( Layouts.createGridLayout( 3 ) );
    m_lineDetailsSection.setClient( detailsPanel );
    m_lineDetailsSection.setText( Messages.getString( "StrokeComposite.0" ) ); //$NON-NLS-1$
    m_lineDetailsSection.setExpanded( shouldExpand() );

    createLineJoinControl( toolkit, detailsPanel );
    createLineCapControl( toolkit, detailsPanel );
    createDashArrayControl( toolkit, detailsPanel );
    createDashOffsetControl( toolkit, detailsPanel );

    if( (sldStyle & HIDE_GRAPHIC) == 0 )
      createGraphicControl( toolkit, body );

    if( (sldStyle & PREVIEW) != 0 )
      createPreviewControl( toolkit, body );
  }

  private void createColorControl( final FormToolkit toolkit, final Composite parent )
  {
    toolkit.createLabel( parent, Messages.getString( "org.kalypso.ui.editor.sldEditor.StrokeEditorComposite.16" ) ); //$NON-NLS-1$

    final Label colorLabel = toolkit.createLabel( parent, StringUtils.EMPTY, SWT.BORDER );
    final GridData gridData = new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 );
    gridData.widthHint = 16;
    colorLabel.setLayoutData( gridData );

    final IObservableValue target = SWTObservables.observeBackground( colorLabel );
    final IObservableValue model = new StrokeColorValue( m_input );

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

    colorLabel.setCursor( getDisplay().getSystemCursor( SWT.CURSOR_HAND ) );
  }

  private void createOpacityControl( final FormToolkit toolkit, final Composite parent )
  {
    toolkit.createLabel( parent, Messages.getString( "org.kalypso.ui.editor.sldEditor.StrokeEditorComposite.14" ) ); //$NON-NLS-1$

    final Spinner spinner = new Spinner( parent, SWT.BORDER );
    toolkit.adapt( spinner, true, true );
    spinner.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

    final StrokeOpacityValue model = new StrokeOpacityValue( m_input );
    model.configureSpinner( spinner );

    m_binding.bindValue( SWTObservables.observeSelection( spinner ), model );
  }

  private void createWidthControl( final FormToolkit toolkit, final Composite parent )
  {
    toolkit.createLabel( parent, Messages.getString( "org.kalypso.ui.editor.sldEditor.StrokeEditorComposite.12" ) ); //$NON-NLS-1$

    final Spinner spinner = new Spinner( parent, SWT.BORDER | SWT.TRAIL );
    toolkit.adapt( spinner, true, true );
    spinner.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

    final StrokeWidthValue model = new StrokeWidthValue( m_input );
    model.configureSpinner( spinner );

    m_binding.bindValue( SWTObservables.observeSelection( spinner ), model );
  }

  private void createLineJoinControl( final FormToolkit toolkit, final Composite parent )
  {
    toolkit.createLabel( parent, Messages.getString( "StrokeComposite.1" ) ); //$NON-NLS-1$

    final ComboViewer lineJoinChooser = new ComboViewer( parent, SWT.DROP_DOWN | SWT.READ_ONLY );
    toolkit.adapt( lineJoinChooser.getControl(), true, true );
    lineJoinChooser.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

    final StrokeLineJoinValue model = new StrokeLineJoinValue( m_input );
    model.configureViewer( lineJoinChooser );

    final IObservableValue target = ViewerProperties.singleSelection().observe( lineJoinChooser );
    m_binding.bindValue( target, model );
  }

  private void createLineCapControl( final FormToolkit toolkit, final Composite parent )
  {
    toolkit.createLabel( parent, Messages.getString( "StrokeComposite.2" ) ); //$NON-NLS-1$

    final ComboViewer lineCapChooser = new ComboViewer( parent, SWT.DROP_DOWN | SWT.READ_ONLY );
    toolkit.adapt( lineCapChooser.getControl(), true, true );
    lineCapChooser.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );

    final StrokeLineCapValue model = new StrokeLineCapValue( m_input );
    model.configureViewer( lineCapChooser );

    final IObservableValue target = ViewerProperties.singleSelection().observe( lineCapChooser );
    m_binding.bindValue( target, model );
  }

  private void createDashArrayControl( final FormToolkit toolkit, final Composite parent )
  {
    final String tooltip = Messages.getString( "StrokeComposite.3" ) //$NON-NLS-1$
        + Messages.getString( "StrokeComposite.4" ); //$NON-NLS-1$

    final Label label = toolkit.createLabel( parent, Messages.getString( "org.kalypso.ui.editor.sldEditor.StrokeEditorComposite.2" ) ); //$NON-NLS-1$
    label.setToolTipText( tooltip );

    final Text strokeDashField = toolkit.createText( parent, StringUtils.EMPTY );
    strokeDashField.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    strokeDashField.setMessage( Messages.getString( "StrokeComposite.5" ) ); //$NON-NLS-1$
    strokeDashField.setToolTipText( tooltip );

    final IObservableValue target = SWTObservables.observeText( strokeDashField, SLDBinding.TEXT_DEFAULT_EVENTS );
    final StrokeDashArrayValue model = new StrokeDashArrayValue( m_input );
    m_binding.bindValue( target, model, new StringToFloatArrayConverter(), new FloatArrayToStringConverter( "%.0f" ) ); //$NON-NLS-1$

    final MenuManager menuManager = new MenuManager( StringUtils.EMPTY );
    for( final DashArrayType dash : DashArrayType.values() )
    {
      menuManager.add( new Action( dash.toString() )
      {
        @Override
        public void runWithEvent( final Event event )
        {
          model.setValue( dash.getDashes() );
        }
      } );
    }

    final Button menuButton = new Button( parent, SWT.ARROW | SWT.LEFT );
    new MenuButton( menuButton, menuManager );
  }

  private void createDashOffsetControl( final FormToolkit toolkit, final Composite parent )
  {
    final String tooltip = Messages.getString( "StrokeComposite.7" ); //$NON-NLS-1$

    toolkit.createLabel( parent, Messages.getString( "StrokeComposite.8" ) ).setToolTipText( tooltip ); //$NON-NLS-1$

    final Text strokeDashControl = toolkit.createText( parent, StringUtils.EMPTY );
    strokeDashControl.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false, 2, 1 ) );
    strokeDashControl.setMessage( Messages.getString( "StrokeComposite.9" ) ); //$NON-NLS-1$
    strokeDashControl.setToolTipText( tooltip );

    final IObservableValue target = SWTObservables.observeText( strokeDashControl, SLDBinding.TEXT_DEFAULT_EVENTS );
    m_binding.bindValue( target, new StrokeDashOffsetValue( m_input ), new NumberNotNegativeValidator( IStatus.WARNING ) );
  }

  private void createPreviewControl( final FormToolkit toolkit, final Composite parent )
  {
    m_previewGroup = new Group( parent, SWT.NONE );
    m_previewGroup.setLayout( new GridLayout() );
    toolkit.adapt( m_previewGroup );

    final GridData previewGridData = new GridData( SWT.FILL, SWT.CENTER, true, false, 3, 1 );
    m_previewGroup.setLayoutData( previewGridData );
    m_previewGroup.setText( Messages.getString( "org.kalypso.ui.editor.sldEditor.StrokeEditorComposite.0" ) ); //$NON-NLS-1$

    m_previewComp = new StrokePreview( m_previewGroup, new Point( SWT.DEFAULT, 32 ), m_input );
    m_previewComp.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
  }

  private void createGraphicControl( final FormToolkit toolkit, final Composite parent )
  {
    m_strokeSection = new GraphicStrokeSection( toolkit, parent, m_input );
    m_strokeSection.getSection().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true, 3, 1 ) );
  }

  private boolean shouldExpand( )
  {
    final Stroke stroke = m_input.getData();

    final boolean hasDashArray = DrawingUtils.isCssParameterSet( stroke, Stroke.CSS_DASHARRAY );
    final boolean hasDashOffset = DrawingUtils.isCssParameterSet( stroke, Stroke.CSS_DASHOFFSET );
    final boolean hasLineCap = DrawingUtils.isCssParameterSet( stroke, Stroke.CSS_LINECAP );
    final boolean hasLineJoin = DrawingUtils.isCssParameterSet( stroke, Stroke.CSS_LINEJOIN );
    return hasDashArray | hasDashOffset | hasLineCap | hasLineJoin;
  }

  public void updateControl( )
  {
    m_binding.getBindingContext().updateTargets();

    if( m_strokeSection != null )
      m_strokeSection.updateControl();

    if( m_previewComp != null )
      m_previewComp.updateControl();
  }
}