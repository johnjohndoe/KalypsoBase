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
package org.kalypso.ui.editor.styleeditor.symbolizer;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.kalypso.contribs.eclipse.swt.widgets.ControlUtils;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ui.editor.styleeditor.IStyleEditorConfig;
import org.kalypso.ui.editor.styleeditor.binding.DatabindingForm;
import org.kalypso.ui.editor.styleeditor.binding.IDataBinding;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.preview.SymbolizerPreview;
import org.kalypsodeegree.graphics.sld.Symbolizer;

/**
 * @author F.Lindemann
 */
public abstract class AbstractSymbolizerComposite<S extends Symbolizer> extends Composite implements ISymbolizerComposite<S>
{
  private final IStyleInput<S> m_input;

  private SymbolizerPreview<S> m_preview;

  private final IDataBinding m_binding;

  private final Form m_form;

  private final FormToolkit m_toolkit;

  protected AbstractSymbolizerComposite( final FormToolkit toolkit, final Composite parent, final IStyleInput<S> input )
  {
    super( parent, SWT.NONE );

    m_toolkit = toolkit;
    m_input = input;

    setLayout( new FillLayout() );

    ControlUtils.addDisposeListener( this );

    m_form = toolkit.createForm( this );

    final Composite body = m_form.getBody();
    GridLayoutFactory.fillDefaults().applyTo( body );

    m_binding = new DatabindingForm( m_form, toolkit );

    final IStyleEditorConfig config = input.getConfig();

    if( config.isSymbolizerEditGeometry() )
      createGeometryControl( toolkit, body ).setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, false ) );

    createContent( toolkit, body ).setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    createPreviewControl( toolkit, body ).setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
  }

  protected IDataBinding getBinding( )
  {
    return m_binding;
  }

  protected FormToolkit getToolkit( )
  {
    return m_toolkit;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.symbolizer.ISymbolizerComposite#getControl()
   */
  @Override
  public Control getControl( )
  {
    return this;
  }

  protected IStyleInput<S> getInput( )
  {
    return m_input;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.symbolizerLayouts.ISymbolizerLayout#getSymbolizer()
   */
  @Override
  public S getSymbolizer( )
  {
    return m_input.getData();
  }

  protected void fireStyleChanged( )
  {
    m_input.fireStyleChanged();
  }

  private Control createGeometryControl( final FormToolkit toolkit, final Composite parent )
  {
    final SectionPart sectionPart = new SectionPart( parent, toolkit, Section.TITLE_BAR | Section.EXPANDED | Section.DESCRIPTION );
    final Section section = sectionPart.getSection();
    section.setText( "Geometry" );
    section.setDescription( "Choose the geometry for the symbolizer" );

    final ComboViewer geometryChooser = new ComboViewer( section, SWT.DROP_DOWN | SWT.READ_ONLY );
    section.setClient( geometryChooser.getControl() );

    final GeometryValue<S> geometryHandler = new GeometryValue<S>( m_input );
    geometryHandler.configureViewer( geometryChooser );

    final IViewerObservableValue target = ViewerProperties.singleSelection().observe( geometryChooser );
    m_binding.bindValue( target, geometryHandler );

    return section;
  }

  protected abstract Control createContent( FormToolkit toolkit, Composite parent );

  private Control createPreviewControl( final FormToolkit toolkit, final Composite parent )
  {
    final SectionPart sectionPart = new SectionPart( parent, toolkit, Section.TITLE_BAR | Section.EXPANDED | Section.SHORT_TITLE_BAR );
    final Section section = sectionPart.getSection();
    section.setText( "Preview" );

    final Point size = new Point( SWT.DEFAULT, 32 );
    m_preview = createPreview( section, size, m_input );
    toolkit.adapt( m_preview );

    section.setClient( m_preview );
    return section;
  }

  protected abstract SymbolizerPreview<S> createPreview( Composite parent, Point size, IStyleInput<S> input );

  @Override
  public final void updateControl( )
  {
    m_binding.getBindingContext().updateTargets();

    doUpdateControl();

    m_preview.updateControl();
  }

  protected abstract void doUpdateControl( );

  protected IFeatureType getFeatureType( )
  {
    return getInput().getFeatureType();
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode( )
  {
    final S input = m_input.getData();
    return new HashCodeBuilder().append( input ).toHashCode();
  }

  @Override
  public boolean equals( final Object obj )
  {
    if( obj == null )
      return false;

    if( obj == this )
      return true;

    if( obj.getClass() != getClass() )
      return false;

    final ISymbolizerComposite< ? > rhs = (ISymbolizerComposite< ? >) obj;

    return new EqualsBuilder().appendSuper( super.equals( obj ) ).append( getSymbolizer(), rhs.getSymbolizer() ).isEquals();
  }
}