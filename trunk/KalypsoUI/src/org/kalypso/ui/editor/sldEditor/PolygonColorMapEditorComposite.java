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
package org.kalypso.ui.editor.sldEditor;

import java.awt.Color;
import java.math.BigDecimal;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.kalypso.i18n.Messages;
import org.kalypsodeegree.graphics.sld.Fill;
import org.kalypsodeegree.graphics.sld.ParameterValueType;
import org.kalypsodeegree.graphics.sld.PolygonColorMapEntry;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizerUtils;
import org.kalypsodeegree.graphics.sld.SldHelper;
import org.kalypsodeegree.graphics.sld.Stroke;
import org.kalypsodeegree_impl.graphics.sld.PolygonColorMapEntry_Impl;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author Thomas Jung
 */
public abstract class PolygonColorMapEditorComposite extends Composite
{
  private static final Color DEFAULT_COLOR_MIN = new Color( Integer.parseInt( "ff0000", 16 ) ); //$NON-NLS-1$

  private static final Color DEFAULT_COLOR_MAX = new Color( Integer.parseInt( "0000ff", 16 ) ); //$NON-NLS-1$

  private PolygonColorMapEntry m_toEntry;

  private PolygonColorMapEntry m_fromEntry;

  private final Pattern m_patternDouble = Pattern.compile( "[\\+\\-]?[0-9]+[\\.\\,]?[0-9]*?" ); //$NON-NLS-1$

  private boolean m_strokeChecked;

  private BigDecimal m_stepWidth;

  private BigDecimal m_minValue;

  private BigDecimal m_maxValue;

  private final String m_globalMin;

  private final String m_globalMax;

  public PolygonColorMapEditorComposite( final Composite parent, final int style, final PolygonColorMapEntry from, final PolygonColorMapEntry to, final BigDecimal minGlobalValue, final BigDecimal maxGlobalValue )
  {
    super( parent, style );

    m_fromEntry = from;
    m_toEntry = to;

    final BigDecimal globalMin = minGlobalValue == null ? new BigDecimal( m_fromEntry.getFrom( null ) ).setScale( 2, BigDecimal.ROUND_HALF_UP ) : minGlobalValue.setScale( 2, BigDecimal.ROUND_HALF_UP );
    final BigDecimal globalMax = maxGlobalValue == null ? new BigDecimal( m_toEntry.getFrom( null ) ).setScale( 2, BigDecimal.ROUND_HALF_UP ) : maxGlobalValue.setScale( 2, BigDecimal.ROUND_HALF_UP );

    m_globalMin = minGlobalValue == null ? "<Unknown>" : globalMin.toPlainString(); //$NON-NLS-1$
    m_globalMax = minGlobalValue == null ? "<Unknown>" : globalMax.toPlainString(); //$NON-NLS-1$

    // check if an entry is null. If that is the case, create both entries.
    if( m_fromEntry == null || m_toEntry == null )
    {
      final BigDecimal width = globalMax.subtract( globalMin ).divide( new BigDecimal( 4 ), BigDecimal.ROUND_HALF_UP ).setScale( 3, BigDecimal.ROUND_HALF_UP );
      m_fromEntry = StyleFactory.createPolygonColorMapEntry( DEFAULT_COLOR_MIN, DEFAULT_COLOR_MIN, globalMin, globalMin.add( width ) );
      m_toEntry = StyleFactory.createPolygonColorMapEntry( DEFAULT_COLOR_MAX, DEFAULT_COLOR_MAX, globalMax.subtract( width ), globalMax );
    }

    m_minValue = new BigDecimal( m_fromEntry.getFrom( null ) ).setScale( 2, BigDecimal.ROUND_HALF_UP );
    m_maxValue = new BigDecimal( m_toEntry.getTo( null ) ).setScale( 2, BigDecimal.ROUND_HALF_UP );

    m_stepWidth = new BigDecimal( m_fromEntry.getTo( null ) - m_fromEntry.getFrom( null ) ).setScale( 2, BigDecimal.ROUND_HALF_UP );

    createControl();
  }

  /**
   * creates an default PolygonColorMapEntry TODO: move to style helper classes
   */
  protected static PolygonColorMapEntry_Impl createDefaultColorMapEntry( final Color color, final BigDecimal fromValue, final BigDecimal toValue )
  {
    // fill
    final Fill defaultFillFrom = StyleFactory.createFill( color );

    // stroke
    final Stroke defaultStrokeFrom = StyleFactory.createStroke( color );

    // parameters
    final String label = String.format( "%s - %s", fromValue.toString(), toValue.toString() ); //$NON-NLS-1$

    final ParameterValueType defaultLabel = StyleFactory.createParameterValueType( label );
    final ParameterValueType defaultFrom = StyleFactory.createParameterValueType( fromValue.doubleValue() );
    final ParameterValueType defaultTo = StyleFactory.createParameterValueType( toValue.doubleValue() );

    return new PolygonColorMapEntry_Impl( defaultFillFrom, defaultStrokeFrom, defaultLabel, defaultFrom, defaultTo );
  }

  private void createControl( )
  {
    setLayout( new GridLayout( 2, true ) );

    createMinMaxGroup( this );

    final Group fromColorMapGroup = new Group( this, SWT.NONE );
    fromColorMapGroup.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    fromColorMapGroup.setLayout( new GridLayout( 1, true ) );
    fromColorMapGroup.setText( Messages.getString( "org.kalypso.ui.editor.sldEditor.PolygonColorMapEditorComposite.4" ) ); //$NON-NLS-1$

    final Group toColorMapGroup = new Group( this, SWT.NONE );
    toColorMapGroup.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    toColorMapGroup.setLayout( new GridLayout( 1, true ) );
    toColorMapGroup.setText( Messages.getString( "org.kalypso.ui.editor.sldEditor.PolygonColorMapEditorComposite.5" ) ); //$NON-NLS-1$

    final PolygonColorMapEntryEditorComposite fromEntryComposite = new PolygonColorMapEntryEditorComposite( fromColorMapGroup, SWT.NONE, m_fromEntry );
    fromEntryComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    fromEntryComposite.addModifyListener( new IPolygonColorMapEntryModifyListener()
    {
      public void onEntryChanged( final Object source, final PolygonColorMapEntry entry )
      {
        colorMapChanged();
      }
    } );

    final PolygonColorMapEntryEditorComposite toEntryComposite = new PolygonColorMapEntryEditorComposite( toColorMapGroup, SWT.NONE, m_toEntry );
    toEntryComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    toEntryComposite.addModifyListener( new IPolygonColorMapEntryModifyListener()
    {
      public void onEntryChanged( final Object source, final PolygonColorMapEntry entry )
      {
        colorMapChanged();
      }
    } );

    final Button checkStrokeFrom = new Button( this, SWT.CHECK );
    checkStrokeFrom.setLayoutData( new GridData( SWT.BEGINNING, SWT.UP, true, false, 2, 1 ) );
    checkStrokeFrom.setText( Messages.getString( "org.kalypso.ui.editor.sldEditor.PolygonColorMapEditorComposite.9" ) ); //$NON-NLS-1$

    if( m_fromEntry.getStroke() != null )
    {
      checkStrokeFrom.setSelection( true );
    }
    else
    {
      checkStrokeFrom.setSelection( false );
    }
    m_strokeChecked = checkStrokeFrom.getSelection();

    checkStrokeFrom.addSelectionListener( new SelectionAdapter()
    {

      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        m_strokeChecked = checkStrokeFrom.getSelection();
        colorMapChanged();
      }
    } );

  }

  private void createMinMaxGroup( final Composite commonComposite )
  {
    /* properties (global min / max, displayed min / max */
    final Group propertyGroup = new Group( commonComposite, SWT.NONE );
    final GridData gridDataProperty = new GridData( SWT.FILL, SWT.BEGINNING, true, false );
    gridDataProperty.horizontalSpan = 2;
    propertyGroup.setLayoutData( gridDataProperty );
    propertyGroup.setLayout( new GridLayout( 3, false ) );
    propertyGroup.setText( Messages.getString( "org.kalypso.ui.editor.sldEditor.PolygonColorMapEditorComposite.6" ) ); //$NON-NLS-1$

    final Label globalMinLabel = new Label( propertyGroup, SWT.BEGINNING );
    globalMinLabel.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, false ) );
    globalMinLabel.setText( Messages.getString( "org.kalypso.ui.editor.sldEditor.PolygonColorMapEditorComposite.8", m_globalMin ) ); //$NON-NLS-1$
    globalMinLabel.setToolTipText( Messages.getString( "org.kalypso.ui.editor.sldEditor.PolygonColorMapEditorComposite.12", m_globalMin ) ); //$NON-NLS-1$

    final Label globalMaxLabel = new Label( propertyGroup, SWT.BEGINNING );
    globalMaxLabel.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, false ) );
    globalMaxLabel.setText( Messages.getString( "org.kalypso.ui.editor.sldEditor.PolygonColorMapEditorComposite.7", m_globalMax ) ); //$NON-NLS-1$
    globalMaxLabel.setToolTipText( Messages.getString( "org.kalypso.ui.editor.sldEditor.PolygonColorMapEditorComposite.11", m_globalMax ) ); //$NON-NLS-1$

    final Label labelWithSpinner = new Label( propertyGroup, SWT.NONE );
    labelWithSpinner.setLayoutData( new GridData( SWT.CENTER, SWT.CENTER, true, false ) );
    labelWithSpinner.setText( Messages.getString( "org.kalypso.ui.editor.sldEditor.PolygonColorMapEditorComposite.25" ) ); //$NON-NLS-1$

    final Text minValueText = new Text( propertyGroup, SWT.BORDER | SWT.TRAIL );
    final GridData minValueTextData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    minValueTextData.widthHint = 100;
    minValueText.setLayoutData( minValueTextData );
    minValueText.setText( String.valueOf( m_minValue ) );

    final Text maxValueText = new Text( propertyGroup, SWT.BORDER | SWT.TRAIL );
    final GridData maxValueTextData = new GridData( SWT.FILL, SWT.CENTER, true, false );
    maxValueTextData.widthHint = 100;
    maxValueText.setLayoutData( maxValueTextData );
    maxValueText.setText( String.valueOf( m_maxValue ) );

    final Text stepWidthText = new Text( propertyGroup, SWT.BORDER | SWT.TRAIL );
    final GridData gridDataStepWidthText = new GridData( SWT.FILL, SWT.CENTER, true, false );
    gridDataStepWidthText.widthHint = 100;
    stepWidthText.setLayoutData( gridDataStepWidthText );
    stepWidthText.setText( String.valueOf( m_stepWidth ) );

    minValueText.addKeyListener( new KeyAdapter()
    {
      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      @Override
      public void keyPressed( final KeyEvent event )
      {
        switch( event.keyCode )
        {
          case SWT.CR:
            final BigDecimal value = SldHelper.checkDoubleTextValue( propertyGroup, minValueText, m_patternDouble );
            if( value != null )
              m_minValue = value;
        }
      }
    } );

    minValueText.addFocusListener( new FocusListener()
    {
      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      public void focusGained( final FocusEvent e )
      {
        final BigDecimal value = SldHelper.checkDoubleTextValue( propertyGroup, minValueText, m_patternDouble );
        if( value != null )
          m_minValue = value;
      }

      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      public void focusLost( final FocusEvent e )
      {
        final BigDecimal value = SldHelper.checkDoubleTextValue( propertyGroup, minValueText, m_patternDouble );
        if( value != null )
        {
          m_minValue = value;
          colorMapChanged();
        }
      }
    } );

    minValueText.addModifyListener( new ModifyListener()
    {
      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      public void modifyText( final ModifyEvent e )
      {
        final String tempText = minValueText.getText();

        final Matcher m = m_patternDouble.matcher( tempText );

        if( !m.matches() )
        {
          minValueText.setBackground( propertyGroup.getDisplay().getSystemColor( SWT.COLOR_RED ) );
        }
        else
        {
          minValueText.setBackground( propertyGroup.getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
          tempText.replaceAll( ",", "." ); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    } );

    maxValueText.addKeyListener( new KeyAdapter()
    {
      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      @Override
      public void keyPressed( final KeyEvent event )
      {
        switch( event.keyCode )
        {
          case SWT.CR:
            final BigDecimal value = SldHelper.checkDoubleTextValue( propertyGroup, maxValueText, m_patternDouble );
            if( value != null )
              m_maxValue = value;
        }
      }
    } );

    maxValueText.addFocusListener( new FocusListener()
    {
      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      public void focusGained( final FocusEvent e )
      {
        final BigDecimal value = SldHelper.checkDoubleTextValue( propertyGroup, maxValueText, m_patternDouble );
        if( value != null )
          m_maxValue = value;
      }

      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      public void focusLost( final FocusEvent e )
      {
        final BigDecimal value = SldHelper.checkDoubleTextValue( propertyGroup, maxValueText, m_patternDouble );
        if( value != null )
        {
          m_maxValue = value;
          colorMapChanged();
        }
      }
    } );

    maxValueText.addModifyListener( new ModifyListener()
    {
      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      public void modifyText( final ModifyEvent e )
      {
        final String tempText = maxValueText.getText();

        final Matcher m = m_patternDouble.matcher( tempText );

        if( !m.matches() )
        {
          maxValueText.setBackground( propertyGroup.getDisplay().getSystemColor( SWT.COLOR_RED ) );
        }
        else
        {
          maxValueText.setBackground( propertyGroup.getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
          tempText.replaceAll( ",", "." ); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    } );

    stepWidthText.addKeyListener( new KeyAdapter()
    {
      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      @Override
      public void keyPressed( final KeyEvent event )
      {
        switch( event.keyCode )
        {
          case SWT.CR:
            final BigDecimal value = SldHelper.checkPositiveDoubleTextValue( propertyGroup, stepWidthText, m_patternDouble );
            if( value != null )
              m_stepWidth = value;
        }
      }
    } );

    stepWidthText.addFocusListener( new FocusListener()
    {
      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      public void focusGained( final FocusEvent e )
      {
        final BigDecimal value = SldHelper.checkPositiveDoubleTextValue( propertyGroup, stepWidthText, m_patternDouble );
        if( value != null )
          m_stepWidth = value;
      }

      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      public void focusLost( final FocusEvent e )
      {
        final BigDecimal value = SldHelper.checkPositiveDoubleTextValue( propertyGroup, stepWidthText, m_patternDouble );
        if( value != null )
        {
          m_stepWidth = value;
          colorMapChanged();
        }
      }
    } );

    stepWidthText.addModifyListener( new ModifyListener()
    {
      @SuppressWarnings("synthetic-access")//$NON-NLS-1$
      public void modifyText( final ModifyEvent e )
      {
        final String tempText = stepWidthText.getText();

        final Matcher m = m_patternDouble.matcher( tempText );

        if( !m.matches() )
        {
          stepWidthText.setBackground( propertyGroup.getDisplay().getSystemColor( SWT.COLOR_RED ) );
        }
        else
        {
          stepWidthText.setBackground( propertyGroup.getDisplay().getSystemColor( SWT.COLOR_WHITE ) );
          tempText.replaceAll( ",", "." ); //$NON-NLS-1$ //$NON-NLS-2$
        }
      }
    } );

  }

  public List<PolygonColorMapEntry> getColorMap( )
  {
    return PolygonSymbolizerUtils.createColorMap( m_fromEntry, m_toEntry, m_stepWidth, m_minValue, m_maxValue, m_strokeChecked );
  }

  protected abstract void colorMapChanged( );
}
