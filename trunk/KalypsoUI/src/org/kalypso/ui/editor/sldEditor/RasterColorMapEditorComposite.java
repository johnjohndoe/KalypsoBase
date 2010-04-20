/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestraße 22
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.kalypso.i18n.Messages;
import org.kalypsodeegree.graphics.sld.ColorMapEntry;
import org.kalypsodeegree.graphics.sld.SldHelper;
import org.kalypsodeegree_impl.graphics.sld.ColorMapEntry_Impl;

/**
 * In this composite the user can specify the colors, values and opacities for two ColorMapEntries. The classes
 * inbetween get interpolated by defining the step width, min and max value.
 *
 * @author Thomas Jung
 */

public abstract class RasterColorMapEditorComposite extends Composite
{
  private static final Color DEFAULT_COLOR_MIN = new Color( Integer.parseInt( "00ff00", 16 ) ); //$NON-NLS-1$

  private static final Color DEFAULT_COLOR_MAX = new Color( Integer.parseInt( "330033", 16 ) ); //$NON-NLS-1$

  private ColorMapEntry m_toEntry;

  private ColorMapEntry m_fromEntry;

  private final Pattern m_patternDouble = Pattern.compile( "[\\+\\-]?[0-9]+[\\.\\,]?[0-9]*?" ); //$NON-NLS-1$

  private BigDecimal m_stepWidth;

  private BigDecimal m_minValue;

  private BigDecimal m_maxValue;

  private final BigDecimal m_globalMin;

  private final BigDecimal m_globalMax;

  public RasterColorMapEditorComposite( final Composite parent, final int style, final ColorMapEntry[] colorMap, final BigDecimal minGlobalValue, final BigDecimal maxGlobalValue )
  {
    super( parent, style );

    if( colorMap.length > 0 )
    {
      m_fromEntry = colorMap[0];
      m_toEntry = colorMap[colorMap.length - 1];
    }

    m_globalMin = minGlobalValue;
    m_globalMax = maxGlobalValue;

    if( m_fromEntry == null || m_toEntry == null )
    {
      // set default entries
      m_fromEntry = new ColorMapEntry_Impl( DEFAULT_COLOR_MIN, 0.8, m_globalMin.doubleValue(), m_globalMin.toString() );
      m_toEntry = new ColorMapEntry_Impl( DEFAULT_COLOR_MAX, 0.8, m_globalMax.doubleValue(), m_globalMax.toString() );
    }

    // calculate step width
    if( colorMap.length > 1 )
      m_stepWidth = new BigDecimal( colorMap[1].getQuantity() - colorMap[0].getQuantity() ).setScale( 2, BigDecimal.ROUND_HALF_UP );
    else
      m_stepWidth = m_globalMax.subtract( m_globalMin );

    m_minValue = new BigDecimal( m_fromEntry.getQuantity() ).setScale( 2, BigDecimal.ROUND_HALF_UP );
    m_maxValue = new BigDecimal( m_toEntry.getQuantity() ).setScale( 2, BigDecimal.ROUND_HALF_UP );

    createControl();
  }

  private void createControl( )
  {
    setLayout( new GridLayout( 2, true ) );

    createMinMaxGroup( this );

    final Group fromColorMapGroup = new Group( this, SWT.NONE );
    fromColorMapGroup.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    fromColorMapGroup.setLayout( new GridLayout( 1, true ) );
    fromColorMapGroup.setText( Messages.getString("org.kalypso.ui.editor.sldEditor.RasterColorMapEditorComposite.3") ); //$NON-NLS-1$

    final Group toColorMapGroup = new Group( this, SWT.NONE );
    toColorMapGroup.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    toColorMapGroup.setLayout( new GridLayout( 1, true ) );
    toColorMapGroup.setText( Messages.getString("org.kalypso.ui.editor.sldEditor.RasterColorMapEditorComposite.4") ); //$NON-NLS-1$

    final ColorMapEntryEditorComposite fromEntryComposite = new ColorMapEntryEditorComposite( fromColorMapGroup, SWT.NONE, m_fromEntry );

    fromEntryComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    fromEntryComposite.addModifyListener( new IColorMapEntryModifyListener()
    {
      public void onEntryChanged( final Object source, final ColorMapEntry entry )
      {
        colorMapChanged();
      }
    } );

    final ColorMapEntryEditorComposite toEntryComposite = new ColorMapEntryEditorComposite( toColorMapGroup, SWT.NONE, m_toEntry );
    toEntryComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    toEntryComposite.addModifyListener( new IColorMapEntryModifyListener()
    {
      public void onEntryChanged( final Object source, final ColorMapEntry entry )
      {
        colorMapChanged();
      }
    } );

    /*
     * final ColorMapEntryEditorComposite nodataEntryComposite = new ColorMapEntryEditorComposite( nodataColorMapGroup,
     * SWT.NONE, m_nodataEntry ); nodataEntryComposite.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
     * nodataEntryComposite.addModifyListener( new IColorMapEntryModifyListener() { public void onEntryChanged( Object
     * source, ColorMapEntry entry ) { colorMapChanged(); } } );
     */
  }

  private void createMinMaxGroup( final Composite commonComposite )
  {
    /* properties (global min / max, displayed min / max */
    final Group propertyGroup = new Group( commonComposite, SWT.NONE );
    final GridData gridDataProperty = new GridData( SWT.FILL, SWT.FILL, true, true );
    gridDataProperty.horizontalSpan = 2;
    propertyGroup.setLayoutData( gridDataProperty );
    propertyGroup.setLayout( new GridLayout( 2, true ) );
    propertyGroup.setText( Messages.getString("org.kalypso.ui.editor.sldEditor.RasterColorMapEditorComposite.5") ); //$NON-NLS-1$

    final Composite globalComposite = new Composite( propertyGroup, SWT.NONE );
    final GridData gridDataGlobalComp = new GridData( SWT.FILL, SWT.FILL, true, false );
    globalComposite.setLayoutData( gridDataGlobalComp );
    globalComposite.setLayout( new GridLayout( 2, false ) );

    final Composite displayComposite = new Composite( propertyGroup, SWT.NONE );
    final GridData gridDataDisplayComp = new GridData( SWT.FILL, SWT.FILL, true, false );
    displayComposite.setLayoutData( gridDataDisplayComp );
    displayComposite.setLayout( new GridLayout( 2, false ) );

    final Label globalMaxLabel = new Label( globalComposite, SWT.NONE );
    final GridData gridDataGlobalMax = new GridData( SWT.BEGINNING, SWT.CENTER, false, false );
    gridDataGlobalMax.heightHint = 15;
    globalMaxLabel.setLayoutData( gridDataGlobalMax );
    globalMaxLabel.setText( Messages.getString("org.kalypso.ui.editor.sldEditor.RasterColorMapEditorComposite.6") ); //$NON-NLS-1$

    final Label globalMaxValueLabel = new Label( globalComposite, SWT.NONE );
    final GridData gridDataMaxValueLabel = new GridData( SWT.END, SWT.CENTER, false, false );
    gridDataMaxValueLabel.widthHint = 40;
    gridDataMaxValueLabel.heightHint = 15;

    globalMaxValueLabel.setLayoutData( gridDataMaxValueLabel );
    String max;
    if( m_globalMax.toString() != null )
      max = m_globalMax.toString();
    else
      max = Messages.getString("org.kalypso.ui.editor.sldEditor.RasterColorMapEditorComposite.7"); //$NON-NLS-1$
    globalMaxValueLabel.setText( max );
    globalMaxValueLabel.setAlignment( SWT.RIGHT );

    final Label globalMinLabel = new Label( globalComposite, SWT.NONE );
    globalMinLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );
    globalMinLabel.setText( Messages.getString("org.kalypso.ui.editor.sldEditor.org.kalypso.ui.editor.sldEditor.RasterColorMapEditorComposite.8") ); //$NON-NLS-1$

    final Label globalMinValueLabel = new Label( globalComposite, SWT.NONE );
    final GridData gridDataMinValueLabel = new GridData( SWT.END, SWT.CENTER, false, false );
    gridDataMinValueLabel.widthHint = 40;

    globalMinValueLabel.setLayoutData( gridDataMinValueLabel );
    String min;
    if( m_globalMin.toString() != null )
      min = m_globalMin.toString();
    else
      min = Messages.getString("org.kalypso.ui.editor.sldEditor.RasterColorMapEditorComposite.9"); //$NON-NLS-1$

    globalMinValueLabel.setText( min );
    globalMinValueLabel.setAlignment( SWT.RIGHT );

    /* max value to display */
    final Label displayMaxLabel = new Label( displayComposite, SWT.NONE );
    displayMaxLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, true, false ) );
    displayMaxLabel.setText( Messages.getString("org.kalypso.ui.editor.sldEditor.RasterColorMapEditorComposite.10") ); //$NON-NLS-1$

    final Text maxValueText = new Text( displayComposite, SWT.BORDER | SWT.TRAIL );
    final GridData gridDataMaxText = new GridData( SWT.FILL, SWT.CENTER, true, false );
    gridDataMaxText.widthHint = 30;
    gridDataMaxText.heightHint = 10;
    maxValueText.setLayoutData( gridDataMaxText );

    final String stringMax = String.valueOf( m_maxValue );
    maxValueText.setText( stringMax );

    /* min value to display */
    final Label displayMinLabel = new Label( displayComposite, SWT.NONE );
    displayMinLabel.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, true, false ) );
    displayMinLabel.setText( Messages.getString("org.kalypso.ui.editor.sldEditor.RasterColorMapEditorComposite.11") ); //$NON-NLS-1$

    final Text minValueText = new Text( displayComposite, SWT.BORDER | SWT.TRAIL );
    final GridData gridDataMinText = new GridData( SWT.FILL, SWT.CENTER, true, false );
    gridDataMinText.widthHint = 30;
    gridDataMinText.heightHint = 10;
    minValueText.setLayoutData( gridDataMinText );

    final String stringMin = String.valueOf( m_minValue );
    minValueText.setText( stringMin );

    minValueText.addKeyListener( new KeyAdapter()
    {
      @SuppressWarnings("synthetic-access")
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
      @SuppressWarnings("synthetic-access")
      public void focusGained( final FocusEvent e )
      {
        final BigDecimal value = SldHelper.checkDoubleTextValue( propertyGroup, minValueText, m_patternDouble );
        if( value != null )
          m_minValue = value;
      }

      @SuppressWarnings("synthetic-access")
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
      @SuppressWarnings("synthetic-access")
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
      @SuppressWarnings("synthetic-access")
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
      @SuppressWarnings("synthetic-access")
      public void focusGained( final FocusEvent e )
      {
        final BigDecimal value = SldHelper.checkDoubleTextValue( propertyGroup, maxValueText, m_patternDouble );
        if( value != null )
          m_maxValue = value;
      }

      @SuppressWarnings("synthetic-access")
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
      @SuppressWarnings("synthetic-access")
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

    // step width spinner
    final Label labelWithSpinner = new Label( displayComposite, SWT.NONE );
    labelWithSpinner.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, true, false ) );
    labelWithSpinner.setText( Messages.getString("org.kalypso.ui.editor.sldEditor.RasterColorMapEditorComposite.24") ); //$NON-NLS-1$

    final Text stepWidthText = new Text( displayComposite, SWT.BORDER | SWT.TRAIL );
    final GridData gridDataStepWidthText = new GridData( SWT.FILL, SWT.CENTER, true, false );
    gridDataStepWidthText.widthHint = 30;
    gridDataStepWidthText.heightHint = 10;
    stepWidthText.setLayoutData( gridDataStepWidthText );

    final String stringStepWidth = String.valueOf( m_stepWidth );
    stepWidthText.setText( stringStepWidth );

    stepWidthText.addKeyListener( new KeyAdapter()
    {
      @SuppressWarnings("synthetic-access")
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
      @SuppressWarnings("synthetic-access")
      public void focusGained( final FocusEvent e )
      {
        final BigDecimal value = SldHelper.checkPositiveDoubleTextValue( propertyGroup, stepWidthText, m_patternDouble );
        if( value != null )
          m_stepWidth = value;
      }

      @SuppressWarnings("synthetic-access")
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
      @SuppressWarnings("synthetic-access")
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

  public ColorMapEntry[] getColorMap( )
  {
    return createColorMap( m_fromEntry, m_toEntry, m_stepWidth, m_minValue, m_maxValue );
  }

  protected abstract void colorMapChanged( );

  protected static ColorMapEntry[] createColorMap( final ColorMapEntry fromEntry, final ColorMapEntry toEntry, final BigDecimal stepWidth, final BigDecimal minValue, final BigDecimal maxValue )
  {
    final Color fromColor = fromEntry.getColor();
    final Color toColor = toEntry.getColor();
    double opacityFrom = fromEntry.getOpacity();
    if( !checkValue( opacityFrom ) == true )
      opacityFrom = 1.0;

    double opacityTo = toEntry.getOpacity();
    if( !checkValue( opacityTo ) == true )
      opacityTo = 1.0;

    final int fromAlpha = (int) (255.0 * opacityFrom);
    final int toAlpha = (int) (255.0 * opacityTo);
    final Color fromColorAlpha = new Color( fromColor.getRed(), fromColor.getGreen(), fromColor.getBlue(), fromAlpha );
    final Color toColorAlpha = new Color( toColor.getRed(), toColor.getGreen(), toColor.getBlue(), toAlpha );

    return SldHelper.createColorMap( fromColorAlpha, toColorAlpha, stepWidth, minValue, maxValue );
  }

  private static boolean checkValue( final double value )
  {
    if( value > 1 || value < 0 )
      return false;
    return true;
  }

}
