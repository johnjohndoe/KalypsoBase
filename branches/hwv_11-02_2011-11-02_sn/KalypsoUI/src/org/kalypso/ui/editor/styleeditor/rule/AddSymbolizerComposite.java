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
 * Created on 15.07.2004
 *
 */
package org.kalypso.ui.editor.styleeditor.rule;

import java.awt.Color;

import javax.xml.namespace.QName;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.kalypso.gmlschema.annotation.AnnotationUtilities;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.ui.editor.styleeditor.StyleEditorHelper;
import org.kalypsodeegree.graphics.sld.Graphic;
import org.kalypsodeegree.graphics.sld.LinePlacement.PlacementType;
import org.kalypsodeegree.graphics.sld.Mark;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.graphics.sld.TextSymbolizer;
import org.kalypsodeegree_impl.filterencoding.PropertyName;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;
import org.kalypsodeegree_impl.tools.GeometryType;
import org.kalypsodeegree_impl.tools.GeometryUtilities;

/**
 * @author F.Lindemann
 */
public class AddSymbolizerComposite extends Composite
{
  private final IFeatureType m_featureType;

  private ComboViewer m_symbolizerChooser = null;

  private IValuePropertyType m_geometry;

  private SymbolizerType m_symbolizerType;

  private final boolean m_choiceIsPossible;

  public AddSymbolizerComposite( final Composite parent, final IFeatureType featureType )
  {
    super( parent, SWT.NONE );

    setLayout( new FillLayout() );

    m_featureType = featureType;

    final Composite group = new Composite( this, SWT.NONE );
    group.setLayout( new GridLayout( 3, false ) );
// group.setText( "" );

    m_choiceIsPossible = init( group );

    addDisposeListener( new DisposeListener()
    {
      /**
       * @see org.eclipse.swt.events.DisposeListener#widgetDisposed(org.eclipse.swt.events.DisposeEvent)
       */
      @Override
      public void widgetDisposed( final DisposeEvent e )
      {
        dispose();
      }
    } );
  }

  public boolean isChoicePossible( )
  {
    return m_choiceIsPossible;
  }

  private boolean init( final Composite parent )
  {
    new Label( parent, SWT.NONE ).setText( "Geometry" );
    new Label( parent, SWT.NONE ).setText( "Symbolizer Type" );
    new Label( parent, SWT.NONE );

    // Geometry-Selection Combo
    final ComboViewer geometryChooser = new ComboViewer( parent, SWT.DROP_DOWN | SWT.READ_ONLY );
    final Combo geometryCombo = geometryChooser.getCombo();
    geometryCombo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    geometryChooser.setContentProvider( new ArrayContentProvider() );
    geometryChooser.setLabelProvider( new LabelProvider()
    {
      @Override
      public String getText( final Object element )
      {
        final IValuePropertyType pt = (IValuePropertyType) element;
        return AnnotationUtilities.getAnnotation( pt.getAnnotation(), null, IAnnotation.ANNO_LABEL );
      }
    } );

    final IValuePropertyType[] geomProperties = m_featureType.getAllGeomteryProperties();
    geometryChooser.setInput( geomProperties );

    geometryChooser.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        handleGeometrySelected( (IValuePropertyType) selection.getFirstElement() );
      }
    } );

    // Symbolizer Combo
    m_symbolizerChooser = new ComboViewer( parent, SWT.DROP_DOWN | SWT.READ_ONLY );
    final Combo symbolizerCombo = m_symbolizerChooser.getCombo();
    symbolizerCombo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    m_symbolizerChooser.setContentProvider( new ArrayContentProvider() );
    m_symbolizerChooser.setLabelProvider( new LabelProvider() );

    m_symbolizerChooser.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        handleSymbolizerChanged( (SymbolizerType) selection.getFirstElement() );
      }
    } );

    if( geomProperties.length > 0 )
      geometryChooser.setSelection( new StructuredSelection( geomProperties[0] ) );
    else
    {
      geometryChooser.setSelection( StructuredSelection.EMPTY );

      geometryCombo.setEnabled( false );
      symbolizerCombo.setEnabled( false );
    }

    return geomProperties.length > 0;
  }

  protected void handleGeometrySelected( final IValuePropertyType geometry )
  {
    m_geometry = geometry;

    updateSymbolizerCombo();
  }

  protected void handleSymbolizerChanged( final SymbolizerType symbolizerType )
  {
    m_symbolizerType = symbolizerType;
  }

  protected void updateSymbolizerCombo( )
  {
    final QName propName = m_geometry.getQName();
    final SymbolizerType items[] = getSymbolizerTypesByFeatureProperty( new PropertyName( propName ) );
    m_symbolizerChooser.setInput( items );

    if( items.length > 0 )
      m_symbolizerChooser.setSelection( new StructuredSelection( items[0] ) );
    else
      m_symbolizerChooser.setSelection( StructuredSelection.EMPTY );
  }

  public Symbolizer createSymbolizer( )
  {
    final QName geometryProperty = m_geometry.getQName();
    return createSymbolizer( new PropertyName( geometryProperty ), m_symbolizerType, m_featureType );
  }

  public static Symbolizer createSymbolizer( final PropertyName geometryPropertyName, final SymbolizerType symbolizerType, final IFeatureType featureType )
  {
    final IPropertyType ftp = StyleEditorHelper.getFeatureTypeProperty( featureType, geometryPropertyName );

    switch( symbolizerType )
    {
      case POINT:
        final Mark mark = StyleFactory.createMark( "square" ); //$NON-NLS-1$
        final Graphic graphic = StyleFactory.createGraphic( null, mark, 1.0, 2.0, 0.0 );
        return StyleFactory.createPointSymbolizer( graphic, geometryPropertyName );

      case LINE:
        return StyleFactory.createLineSymbolizer( StyleFactory.createStroke(), geometryPropertyName );

      case POLYGON:
        return StyleFactory.createPolygonSymbolizer( StyleFactory.createStroke(), StyleFactory.createFill(), geometryPropertyName );

      case TEXT:
        final TextSymbolizer textSymbolizer = StyleFactory.createTextSymbolizer( geometryPropertyName, null, null );
        // TODO: move into StyleFactory
        textSymbolizer.setFill( null );
        textSymbolizer.getHalo().getFill().setOpacity( 0.3 );
        textSymbolizer.setLabel( null );
        textSymbolizer.getFont().setColor( Color.BLACK );
        // check which geometry-type
        // if line than label_placement - line_placement
        final GeometryType geometryType = GeometryUtilities.classifyGeometry( ftp );
        if( geometryType == GeometryType.CURVE )
          StyleFactory.createLabelPlacement( StyleFactory.createLinePlacement( PlacementType.above ) );
        // else label_placement - point_placement
        else
          StyleFactory.createLabelPlacement( StyleFactory.createPointPlacement() );
        return textSymbolizer;

      case RASTER:
        return StyleFactory.createRasterSymbolizer();
    }

    return null;
  }

  private SymbolizerType[] getSymbolizerTypesByFeatureProperty( final PropertyName propName )
  {
    final IPropertyType ftp = StyleEditorHelper.getFeatureTypeProperty( m_featureType, propName );

    final GeometryType geometryType = GeometryUtilities.classifyGeometry( ftp );
    if( geometryType == null )
      return null;

    switch( geometryType )
    {
      case POINT:
        return new SymbolizerType[] { SymbolizerType.POINT, SymbolizerType.TEXT };

      case CURVE:
        return new SymbolizerType[] { SymbolizerType.LINE, SymbolizerType.POINT, SymbolizerType.TEXT };

      case SURFACE:
        return new SymbolizerType[] { SymbolizerType.POLYGON, SymbolizerType.LINE, SymbolizerType.POINT, SymbolizerType.TEXT };

      case UNKNOWN:
      default:
        return new SymbolizerType[] { SymbolizerType.POINT, SymbolizerType.LINE, SymbolizerType.POLYGON, SymbolizerType.TEXT };
    }

    // TODO: NOT SIMPLE CASE: for pattern; used anymore?
// if( featureTypeGeometryType == TextSymbolizerComposite.GM_POINT || featureTypeGeometryType ==
// TextSymbolizerComposite.GM_MULTIPOINT )
// return new SymbolizerType[] { SymbolizerType.POINT };
// else if( featureTypeGeometryType == TextSymbolizerComposite.GM_LINESTRING || featureTypeGeometryType ==
// TextSymbolizerComposite.GM_MULTILINESTRING )
// return new SymbolizerType[] { SymbolizerType.LINE, SymbolizerType.POINT };
// else if( featureTypeGeometryType == TextSymbolizerComposite.GM_POLYGON || featureTypeGeometryType ==
// TextSymbolizerComposite.GM_MULTIPOLYGON )
// return new SymbolizerType[] { SymbolizerType.POLYGON, SymbolizerType.POINT };
// else if( featureTypeGeometryType == TextSymbolizerComposite.GM_OBJECT )
// return new SymbolizerType[] { SymbolizerType.POINT, SymbolizerType.LINE, SymbolizerType.POLYGON };
//
// return new SymbolizerType[] {};
  }
}