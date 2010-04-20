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
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.kalypso.contribs.eclipse.swt.events.DoubleModifyListener;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.i18n.Messages;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.graphics.sld.LineColorMapEntry;
import org.kalypsodeegree.graphics.sld.ParameterValueType;
import org.kalypsodeegree.graphics.sld.SldHelper;
import org.kalypsodeegree.graphics.sld.Stroke;
import org.kalypsodeegree_impl.graphics.sld.LineColorMap;
import org.kalypsodeegree_impl.graphics.sld.LineColorMapEntry_Impl;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author Thomas Jung
 * 
 */
public class LineColorMapEditorComposite extends Composite
{
  private final LineColorMap m_colorMap;

  private LineColorMapEntry m_entry;

  private final Pattern m_patternDouble = Pattern.compile( "[\\+\\-]?[0-9]+[\\.\\,]?[0-9]*?" ); //$NON-NLS-1$

  private BigDecimal m_stepWidth;

  private int m_fatValue;

  private int m_fatWidth;

  private BigDecimal m_minValue;

  private BigDecimal m_maxValue;

  private final String m_globalMin;

  private final String m_globalMax;

  private final org.eclipse.swt.graphics.Color m_goodColor;

  private final org.eclipse.swt.graphics.Color m_badColor;
  
  public LineColorMapEditorComposite( final Composite parent, final int style, final LineColorMap colorMap, final BigDecimal minGlobalValue, final BigDecimal maxGlobalValue )
  { 
    super( parent, style );

    m_goodColor = parent.getDisplay().getSystemColor( SWT.COLOR_BLACK );
    m_badColor = parent.getDisplay().getSystemColor( SWT.COLOR_RED );
    
    m_colorMap = colorMap;

    final BigDecimal globalMin = minGlobalValue == null ? null : minGlobalValue.setScale( 2, BigDecimal.ROUND_HALF_UP );
    final BigDecimal globalMax = maxGlobalValue == null ? null : maxGlobalValue.setScale( 2, BigDecimal.ROUND_HALF_UP );
    
    final LineColorMapEntry[] colorMapEntries = m_colorMap.getColorMap();

    final BigDecimal firstValue = new BigDecimal( colorMapEntries[0].getQuantity( null ) ).setScale( 2, BigDecimal.ROUND_HALF_UP );
    if( colorMapEntries.length < 2 )
    {
      m_minValue = globalMin == null ? firstValue : globalMin;
      m_maxValue = globalMax == null ? firstValue : globalMax;
    }
    else
    {
      m_minValue = firstValue;
      m_maxValue = new BigDecimal( colorMapEntries[colorMapEntries.length - 1].getQuantity( null ) ).setScale( 2, BigDecimal.ROUND_HALF_UP );
    }

    m_globalMin = globalMin == null ? "<Unknown>" : globalMin.toPlainString(); //$NON-NLS-1$
    m_globalMax = globalMax == null ? "<Unknown>" : globalMax.toPlainString(); //$NON-NLS-1$

    
    /* default parameter */
    m_stepWidth = new BigDecimal( 0.1 ).setScale( 2, BigDecimal.ROUND_HALF_UP );
    m_fatValue = 4;
    m_fatWidth = 4;
    
    if( colorMapEntries.length > 1 )
    {
      // determine step by first two entries
      final double value0 = colorMapEntries[0].getQuantity( null );
      final double value1 = colorMapEntries[1].getQuantity( null );
      m_stepWidth = new BigDecimal( Math.abs( value0 - value1 ) );
      
      try
      {
        // use frst width bigger than the width of first entry as fatWidth
        final double basicWidth = colorMapEntries[0].getStroke().getWidth( null );

        for( int i = 0; i < colorMapEntries.length; i++ )
        {
          final LineColorMapEntry colorMapEntry = colorMapEntries[i];

          final int width = (int) colorMapEntry.getStroke().getWidth( null );
          if( width > basicWidth )
          {
            m_fatWidth = width;
            
            // only assign fatValue if we have more than 2 entries. Else it's most probably an sld template
            if( colorMapEntries.length > 2 )
              m_fatValue = i;
            
            break;
          }
        }
      }
      catch( final FilterEvaluationException e )
      {
        e.printStackTrace();
      }
    }

    createControl();
  }

  private void createControl( )
  {
    setLayout( new GridLayout( 2, true ) );

    createPropertyGroup();

    createEntryGroup();
  }

  private void createPropertyGroup( )
  {
    final Group propertyGroup = new Group( this, SWT.NONE );
    propertyGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    propertyGroup.setLayout( new GridLayout( 2, false ) );
    propertyGroup.setText( Messages.getString( "org.kalypso.ui.editor.sldEditor.LineColorMapEditorComposite.1" ) ); //$NON-NLS-1$

    final Label fromLabel = new Label( propertyGroup, SWT.NONE );
    fromLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, true, false ) );
    fromLabel.setText( Messages.getString( "org.kalypso.ui.editor.sldEditor.LineColorMapEditorComposite.9", m_globalMin ) ); //$NON-NLS-1$
    fromLabel.setToolTipText( Messages.getString( "org.kalypso.ui.editor.sldEditor.LineColorMapEditorComposite.11", m_globalMin  ) ); //$NON-NLS-1$

    final Text minValueText = new Text( propertyGroup, SWT.BORDER | SWT.TRAIL );
    final GridData gridDataMinText = new GridData( SWT.FILL, SWT.CENTER, true, false );
    minValueText.setLayoutData( gridDataMinText );
    final String stringMin = String.valueOf( m_minValue );
    minValueText.setText( stringMin );

    final Label toLabel = new Label( propertyGroup, SWT.NONE );
    toLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, true, false ) );
    toLabel.setText( Messages.getString( "org.kalypso.ui.editor.sldEditor.LineColorMapEditorComposite.10", m_globalMax ) ); //$NON-NLS-1$
    toLabel.setToolTipText( Messages.getString( "org.kalypso.ui.editor.sldEditor.LineColorMapEditorComposite.12",m_globalMax ) ); //$NON-NLS-1$

    final Text maxValueText = new Text( propertyGroup, SWT.BORDER | SWT.TRAIL );
    final GridData gridDataMaxText = new GridData( SWT.FILL, SWT.CENTER, true, false );
    maxValueText.setLayoutData( gridDataMaxText );
    final String stringMax = String.valueOf( m_maxValue );
    maxValueText.setText( stringMax );

    minValueText.addFocusListener( new FocusAdapter()
    {
      @Override
      @SuppressWarnings("synthetic-access")
      public void focusLost( final FocusEvent e )
      {
        final BigDecimal value = NumberUtils.parseQuietDecimal( minValueText.getText() );
        if( value != null )
        {
          m_minValue = value.setScale( 2, BigDecimal.ROUND_HALF_UP );
          minValueText.setText( m_minValue.toString() );
          updateColorMap();
        }
      }
    } );

    minValueText.addModifyListener( new DoubleModifyListener( m_goodColor, m_badColor ) );

    maxValueText.addFocusListener( new FocusAdapter()
    {
      @Override
      @SuppressWarnings("synthetic-access")
      public void focusLost( final FocusEvent e )
      {
        final BigDecimal value = NumberUtils.parseQuietDecimal( maxValueText.getText() );
        if( value != null )
        {
          m_maxValue = value.setScale( 2, BigDecimal.ROUND_HALF_UP );
          maxValueText.setText( m_maxValue.toString() );
          updateColorMap();
        }
      }
    } );

    maxValueText.addModifyListener( new DoubleModifyListener( m_goodColor, m_badColor ) );
    
    // step width spinner
    final Label labelWithSpinner = new Label( propertyGroup, SWT.NONE );
    labelWithSpinner.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, true, false ) );
    labelWithSpinner.setText( Messages.getString( "org.kalypso.ui.editor.sldEditor.LineColorMapEditorComposite.2" ) ); //$NON-NLS-1$

    final Text stepWidthText = new Text( propertyGroup, SWT.BORDER | SWT.TRAIL );
    stepWidthText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );

    final String stringStepWidth = String.valueOf( m_stepWidth );
    stepWidthText.setText( stringStepWidth );

    stepWidthText.addFocusListener( new FocusAdapter()
    {
      @Override
      @SuppressWarnings("synthetic-access")
      public void focusLost( final FocusEvent e )
      {
        final BigDecimal value = SldHelper.checkPositiveDoubleTextValue( propertyGroup, stepWidthText, m_patternDouble );
        if( value != null )
        {
          m_stepWidth = value;
          updateColorMap();
        }
      }
    } );

    stepWidthText.addModifyListener( new DoubleModifyListener( m_goodColor, m_badColor ) );

    // fat step spinner
    final Label labelFatStepSpinner = new Label( propertyGroup, SWT.NONE );
    labelFatStepSpinner.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, true, false ) );
    labelFatStepSpinner.setText( Messages.getString( "org.kalypso.ui.editor.sldEditor.LineColorMapEditorComposite.5" ) ); //$NON-NLS-1$

    final Spinner fatStepSpinner = new Spinner( propertyGroup, SWT.NONE );
    fatStepSpinner.setLayoutData( new GridData( SWT.TRAIL, SWT.CENTER, true, false ) );
    fatStepSpinner.setBackground( this.getBackground() );
    fatStepSpinner.setSelection( m_fatValue );

    fatStepSpinner.addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( final ModifyEvent e )
      {
        m_fatValue = fatStepSpinner.getSelection();
        updateColorMap();
      }
    } );
    
    // bold width spinner
    final Label labelboldWidthSpinner = new Label( propertyGroup, SWT.NONE );
    labelboldWidthSpinner.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, true, false ) );
    labelboldWidthSpinner.setText( Messages.getString( "org.kalypso.ui.editor.sldEditor.LineColorMapEditorComposite.6" ) ); //$NON-NLS-1$

    final Spinner boldWidthSpinner = new Spinner( propertyGroup, SWT.NONE );
    boldWidthSpinner.setLayoutData( new GridData( SWT.TRAIL, SWT.CENTER, true, false ) );
    boldWidthSpinner.setBackground( this.getBackground() );
    boldWidthSpinner.setSelection( m_fatWidth );
    
    boldWidthSpinner.addModifyListener( new ModifyListener()
    {
      @Override
      public void modifyText( final ModifyEvent e )
      {
        m_fatWidth = boldWidthSpinner.getSelection();
        updateColorMap();
      }
    } );
  }

  private void createEntryGroup( )
  {
    final Group normalColorMapGroup = new Group( this, SWT.NONE );
    normalColorMapGroup.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
    normalColorMapGroup.setLayout( new FillLayout() );
    normalColorMapGroup.setText( Messages.getString( "org.kalypso.ui.editor.sldEditor.LineColorMapEditorComposite.7" ) ); //$NON-NLS-1$

    m_entry = m_colorMap.getColorMap()[0];
    final LineColorMapEntryEditorComposite fromEntryComposite = new LineColorMapEntryEditorComposite( normalColorMapGroup, SWT.NONE, m_entry );

    fromEntryComposite.addModifyListener( new ILineColorMapEntryModifyListener()
    {
      public void onEntryChanged( final Object source, final LineColorMapEntry entry )
      {
        updateColorMap();
      }
    } );
  }

  /**
   * sets the parameters for the colormap of an isoline
   */
  protected void updateColorMap( )
  {
    final Stroke stroke = m_entry.getStroke();

    try
    {
      final Color fromColor = stroke.getStroke( null );
      final Color toColor = stroke.getStroke( null );

      final double opacity = stroke.getOpacity( null );
      final double normalWidth = stroke.getWidth( null );
      final float[] dashArray = stroke.getDashArray( null );

      final BigDecimal minDecimal = m_minValue.setScale( 2, BigDecimal.ROUND_FLOOR );
      final BigDecimal maxDecimal = m_maxValue.setScale( 2, BigDecimal.ROUND_CEILING );

      final BigDecimal stepWidth = m_stepWidth.setScale( 2, BigDecimal.ROUND_HALF_UP );
      final int numOfClasses = (maxDecimal.subtract( minDecimal ).divide( stepWidth )).intValue() + 1;

      final List<LineColorMapEntry> colorMapList = new LinkedList<LineColorMapEntry>();

      for( int currentClass = 0; currentClass < numOfClasses; currentClass++ )
      {
        final double currentValue = minDecimal.doubleValue() + currentClass * stepWidth.doubleValue();

        Color lineColor;
        if( fromColor == toColor )
          lineColor = fromColor;
        else
          lineColor = SldHelper.interpolateColor( fromColor, toColor, currentClass, numOfClasses );

        final double strokeWidth;
        if( currentClass != 0 && currentClass % m_fatValue == 0 )
          strokeWidth = m_fatWidth;
        else
          strokeWidth = normalWidth;

        final Stroke newStroke = StyleFactory.createStroke( lineColor, strokeWidth );
        newStroke.setOpacity( opacity );
        newStroke.setDashArray( dashArray );

        final ParameterValueType label = StyleFactory.createParameterValueType( String.format( "%.2f", currentValue ) ); //$NON-NLS-1$
        final ParameterValueType quantity = StyleFactory.createParameterValueType( currentValue );

        final LineColorMapEntry colorMapEntry = new LineColorMapEntry_Impl( newStroke, label, quantity );
        colorMapList.add( colorMapEntry );
      }
      if( colorMapList.size() > 0 )
        m_colorMap.replaceColorMap( colorMapList );
    }
    catch( final FilterEvaluationException e )
    {
      e.printStackTrace();
    }

  }
}
