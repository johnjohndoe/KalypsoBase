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
 * Created on 12.07.2004
 */
package org.kalypso.ui.editor.styleeditor.style;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.eclipse.jface.viewers.ITabItem;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ui.editor.styleeditor.MessageBundle;
import org.kalypso.ui.editor.styleeditor.binding.IStyleInput;
import org.kalypso.ui.editor.styleeditor.binding.StyleInput;
import org.kalypso.ui.editor.styleeditor.dialogs.StyleEditorErrorDialog;
import org.kalypso.ui.editor.styleeditor.panels.AddFilterPropertyPanel;
import org.kalypso.ui.editor.styleeditor.panels.EditSymbolizerPanel;
import org.kalypso.ui.editor.styleeditor.panels.PanelEvent;
import org.kalypso.ui.editor.styleeditor.panels.PanelListener;
import org.kalypso.ui.editor.styleeditor.panels.RulePatternInputPanel;
import org.kalypso.ui.editor.styleeditor.panels.TextInputPanel;
import org.kalypso.ui.editor.styleeditor.panels.TextInputPanel.ModifyListener;
import org.kalypso.ui.editor.styleeditor.rule.AddSymbolizerComposite;
import org.kalypso.ui.editor.styleeditor.rule.SymbolizerType;
import org.kalypso.ui.editor.styleeditor.symbolizer.FilterPatternSymbolizerTabItemBuilder;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.filterencoding.Operation;
import org.kalypsodeegree.graphics.sld.LineSymbolizer;
import org.kalypsodeegree.graphics.sld.PointSymbolizer;
import org.kalypsodeegree.graphics.sld.PolygonSymbolizer;
import org.kalypsodeegree.graphics.sld.Rule;
import org.kalypsodeegree.graphics.sld.Symbolizer;
import org.kalypsodeegree.graphics.sld.TextSymbolizer;
import org.kalypsodeegree_impl.filterencoding.BoundaryExpression;
import org.kalypsodeegree_impl.filterencoding.ComplexFilter;
import org.kalypsodeegree_impl.filterencoding.OperationDefines;
import org.kalypsodeegree_impl.filterencoding.PropertyIsBetweenOperation;
import org.kalypsodeegree_impl.filterencoding.PropertyName;
import org.kalypsodeegree_impl.graphics.sld.StyleFactory;

/**
 * @author F.Lindemann
 */
public class RulePatternTabItem implements ITabItem
{
  private int m_focusedRuleItem = -1;

  private int m_focusedSymbolizerItem = -1;

  double minValue = -1;

  double maxValue = -1;

  double step = -1;

  private final IStyleInput<RuleCollection> m_input;

  public RulePatternTabItem( final IStyleInput<RuleCollection> input )
  {
    m_input = input;
  }

  public RuleCollection getRuleCollection( )
  {
    return m_input.getData();
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.IRuleTabItem#getItemLabel()
   */
  @Override
  public String getItemLabel( )
  {
    if( getRuleCollection().size() == 0 )
      return null;

    final Rule tmpRule = getRuleCollection().get( 0 );
    // PatternRule only possible for PropertyIsBetweenOperation
    if( tmpRule.getFilter() == null || !((((ComplexFilter) tmpRule.getFilter()).getOperation()) instanceof PropertyIsBetweenOperation) )
      return null;

    // 1. get global values for name, minDen, maxDen,
    String rulePatternName = tmpRule.getTitle();
    if( rulePatternName == null || rulePatternName.trim().length() == 0 )
      rulePatternName = MessageBundle.STYLE_EDITOR_SET_VALUE;

    return MessageBundle.STYLE_EDITOR_PATTERN + rulePatternName;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.IRuleTabItem#getItemImage()
   */
  @Override
  public Image getItemImage( )
  {
    return null;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.IRuleTabItem#createItemControl(org.eclipse.ui.forms.widgets.FormToolkit,
   *      org.eclipse.swt.widgets.Composite)
   */
  @Override
  public Control createItemControl( final FormToolkit toolkit, final Composite parent )
  {
    if( getRuleCollection().size() == 0 )
      return null;

    final Rule tmpRule = getRuleCollection().get( 0 );
    // PatternRule only possible for PropertyIsBetweenOperation
    if( tmpRule.getFilter() == null || !((((ComplexFilter) tmpRule.getFilter()).getOperation()) instanceof PropertyIsBetweenOperation) )
      return null;

    // 1. get global values for name, minDen, maxDen,
    String rulePatternName = tmpRule.getTitle();
    if( rulePatternName == null || rulePatternName.trim().length() == 0 )
    {
      rulePatternName = MessageBundle.STYLE_EDITOR_SET_VALUE;
      tmpRule.setTitle( rulePatternName );
    }
    final double rulePatternMinDenom = tmpRule.getMinScaleDenominator();
    final double rulePatternMaxDenom = tmpRule.getMaxScaleDenominator();

    // 2. Begin to draw the first lines
    final Composite composite = new Composite( parent, SWT.NULL );
    final GridLayout compositeLayout = new GridLayout();
    composite.setSize( 270, 230 );
    composite.setLayout( compositeLayout );
    compositeLayout.marginWidth = 5;
    compositeLayout.marginHeight = 5;
    composite.layout();

    final TabFolder symbolizerTabFolder;
    RulePatternInputPanel rulePatternInputPanel = null;

    final TextInputPanel rowBuilder = new TextInputPanel( toolkit, composite );

    rowBuilder.createTextRow( MessageBundle.STYLE_EDITOR_TITLE, rulePatternName, new ModifyListener()
    {
      /**
       * @see org.kalypso.ui.editor.styleeditor.panels.TextInputPanel.ModifyListener#textModified(java.lang.String)
       */
      @Override
      public String textModified( final String newValue )
      {
        if( newValue == null || newValue.trim().length() == 0 )
        {
          final StyleEditorErrorDialog errorDialog = new StyleEditorErrorDialog( composite.getShell(), MessageBundle.STYLE_EDITOR_ERROR_INVALID_INPUT, MessageBundle.STYLE_EDITOR_ERROR_NO_TITLE );
          errorDialog.showError();
        }
        else
        {
          for( int counter6 = 0; counter6 < getRuleCollection().size(); counter6++ )
          {
            getRuleCollection().get( counter6 ).setTitle( newValue );
          }
          fireStyleChanged();
        }
//        tabItem.setText( MessageBundle.STYLE_EDITOR_PATTERN + " " + newValue ); //$NON-NLS-1$
// setFocusedRuleItem( getRuleTabFolder().getSelectionIndex() );

        return null;
      }
    } );

    rowBuilder.createDenominatorRow( MessageBundle.STYLE_EDITOR_MIN_DENOM, rulePatternMinDenom, new ModifyListener()
    {
      @Override
      public String textModified( final String newValue )
      {
        final double min = new Double( newValue );
        final double max = tmpRule.getMaxScaleDenominator();
        // verify that min<=max
        if( min > max )
        {
          final StyleEditorErrorDialog errorDialog = new StyleEditorErrorDialog( composite.getShell(), MessageBundle.STYLE_EDITOR_ERROR_INVALID_INPUT, MessageBundle.STYLE_EDITOR_ERROR_MIN_DENOM_BIG );
          errorDialog.showError();
          return "" + tmpRule.getMinScaleDenominator(); //$NON-NLS-1$
        }

        for( int i = 0; i < getRuleCollection().size(); i++ )
          getRuleCollection().get( i ).setMinScaleDenominator( min );
        fireStyleChanged();
        return null;
      }
    } );

    // max denominator cannot be 0.0 as this would imply that the min
    // denominator needs to be smaller than 0.0 -> does not make sense
    // hence, if no max denomiator specified, get the denominator of the
    // individiual symbolizer
    if( tmpRule.getMaxScaleDenominator() == 0.0 )
      tmpRule.setMaxScaleDenominator( Double.MAX_VALUE );

    rowBuilder.createDenominatorRow( MessageBundle.STYLE_EDITOR_MAX_DENOM, rulePatternMaxDenom, new ModifyListener()
    {
      @Override
      public String textModified( final String newValue )
      {
        double max = new Double( newValue );
        final double min = tmpRule.getMinScaleDenominator();
        // verify that min<=max
        if( min > max )
        {
          final StyleEditorErrorDialog errorDialog = new StyleEditorErrorDialog( composite.getShell(), MessageBundle.STYLE_EDITOR_ERROR_INVALID_INPUT, MessageBundle.STYLE_EDITOR_ERROR_MAX_DENOM_SMALL );
          errorDialog.showError();
          return "" + tmpRule.getMaxScaleDenominator(); //$NON-NLS-1$
        }

        // add a minimum to max in order to be a little bit larger than the
        // current scale and
        // to keep the current view -> otherwise the rule would automatically
        // exculde this configuration
        max += 0.01;
        for( int counter8 = 0; counter8 < getRuleCollection().size(); counter8++ )
          getRuleCollection().get( counter8 ).setMaxScaleDenominator( max );
        fireStyleChanged();
        return null;
      }
    } );

    final AddFilterPropertyPanel addFilterPropertyPanel = new AddFilterPropertyPanel( composite, MessageBundle.STYLE_EDITOR_FILTER_PROPERTY, getNumericFeatureTypePropertylist() );
    // necessary if focus had been changed and rule-pattern is redrawn
    addFilterPropertyPanel.setSelection( ((PropertyIsBetweenOperation) ((ComplexFilter) tmpRule.getFilter()).getOperation()).getPropertyName().getValue() );
    // if numeric Property selection for Filter has changed -> need to change it
    // for every rule of the pattern
    addFilterPropertyPanel.addPanelListener( new PanelListener()
    {
      @Override
      public void valueChanged( final PanelEvent event )
      {
        final String filterPropertyName = addFilterPropertyPanel.getSelection();
        for( int i = 0; i < getRuleCollection().size(); i++ )
        {
          final ComplexFilter filter = (ComplexFilter) getRuleCollection().get( i ).getFilter();
          final PropertyIsBetweenOperation oldOperation = (PropertyIsBetweenOperation) filter.getOperation();
          final PropertyIsBetweenOperation operation = new PropertyIsBetweenOperation( new PropertyName( filterPropertyName ), oldOperation.getLowerBoundary(), oldOperation.getUpperBoundary() );
          getRuleCollection().get( i ).setFilter( new ComplexFilter( operation ) );
        }
        fireStyleChanged();
      }
    } );

    final IFeatureType featureType = m_input.getFeatureType();
// final AddSymbolizerPanel addSymbolizerPanel = new AddSymbolizerPanel( toolkit, composite,
// MessageBundle.STYLE_EDITOR_SYMBOLIZER, featureType, false );

    // 3. getFilterType -> at the moment we assume only a pattern of
    // PropertyIsBetween
    // draw the pattern line
    final Filter filter = tmpRule.getFilter();
    // must be a complex filter -> then we can find out what operation-id is has
    if( filter instanceof ComplexFilter )
    {
      // if PropertyIsBetween
      if( ((ComplexFilter) filter).getOperation().getOperatorId() == OperationDefines.PROPERTYISBETWEEN )
      {
        // find out the settings of the filter - min, max and step values

        for( int j = 0; j < getRuleCollection().size(); j++ )
        {
          // verify again that it is a complexFilter and of type PropertyIs
          // Between for every rule
          if( getRuleCollection().get( j ).getFilter() instanceof ComplexFilter )
          {
            final Operation ruleOperation = ((ComplexFilter) getRuleCollection().get( j ).getFilter()).getOperation();
            if( ruleOperation.getOperatorId() == OperationDefines.PROPERTYISBETWEEN )
            {
              final PropertyIsBetweenOperation isBetweenOperation = (PropertyIsBetweenOperation) ruleOperation;
              if( j == 0 )
              {
                minValue = Double.parseDouble( ((BoundaryExpression) isBetweenOperation.getLowerBoundary()).getValue() );
                maxValue = Double.parseDouble( ((BoundaryExpression) isBetweenOperation.getUpperBoundary()).getValue() );
                step = maxValue - minValue;
              }
              else
              {
                final double tmpMinValue = Double.parseDouble( ((BoundaryExpression) isBetweenOperation.getLowerBoundary()).getValue() );
                final double tmpMaxValue = Double.parseDouble( ((BoundaryExpression) isBetweenOperation.getUpperBoundary()).getValue() );
                if( tmpMinValue < minValue )
                  minValue = tmpMinValue;
                if( tmpMaxValue > maxValue )
                  maxValue = tmpMaxValue;
              }
            }
          }
        }
        rulePatternInputPanel = new RulePatternInputPanel( composite, MessageBundle.STYLE_EDITOR_PATTERN, minValue, maxValue, step );
      }
    }
    else
      return null;

    final EditSymbolizerPanel editSymbolizerPanel = new EditSymbolizerPanel( composite, tmpRule.getSymbolizers().length );

    symbolizerTabFolder = new TabFolder( composite, SWT.NULL );

// addSymbolizerPanel.addPanelListener( new PanelListener()
// {
// @Override
// public void valueChanged( final PanelEvent event )
// {
// final Symbolizer symbolizer = ((AddSymbolizerPanel) event.getSource()).createSymbolizer();
// if( symbolizer != null )
// {
// for( int i = 0; i < m_ruleCollection.size(); i++ )
// {
// final Symbolizer[] symb = { symbolizer };
// m_ruleCollection.get( i ).addSymbolizer( cloneSymbolizer( symb )[0] );
// }
// fireStyleChanged();
// // setFocusedRuleItem( getRuleTabFolder().getSelectionIndex() );
// editSymbolizerPanel.update( m_ruleCollection.get( 0 ).getSymbolizers().length );
// drawSymbolizerTabItems( toolkit, m_ruleCollection.get( 0 ), symbolizerTabFolder, m_ruleCollection );
// symbolizerTabFolder.setSelection( m_ruleCollection.get( 0 ).getSymbolizers().length - 1 );
// }
// }
// } );

    if( rulePatternInputPanel != null )
    {
      rulePatternInputPanel.addPanelListener( new PanelListener()
      {
        @Override
        public void valueChanged( final PanelEvent event )
        {
          final RulePatternInputPanel panel = (RulePatternInputPanel) event.getSource();
          // reset the values for all rules in this pattern if step did not
          // change !!!!
          minValue = panel.getMin();
          maxValue = panel.getMax();
          step = panel.getStep();

          // first create new rules
          BoundaryExpression upperBoundary = null;
          BoundaryExpression lowerBoundary = null;
          final ArrayList<Rule> ruleList = new ArrayList<Rule>();
          final PropertyName propertyName = new PropertyName( addFilterPropertyPanel.getSelection() );
          PropertyIsBetweenOperation operation = null;

          // only need to take first rule and duplicate it
          // plus apply the pattern
          // there needs to be at least one rule, otherwise no pattern rule
          // visible !!!!
          final int patternRuleNumber = (int) Math.ceil( (maxValue - minValue) / step );
          final Symbolizer[] symbolizer = tmpRule.getSymbolizers();

          // first add those that are existing and are to be kept
          final int currentSize = getRuleCollection().size();

          if( patternRuleNumber <= currentSize )
          {
            for( int i = 0; i < patternRuleNumber; i++ )
            {
              lowerBoundary = new BoundaryExpression( "" + (minValue + (i * step)) ); //$NON-NLS-1$
              if( (minValue + ((i + 1) * step)) > maxValue )
                upperBoundary = new BoundaryExpression( "" + maxValue ); //$NON-NLS-1$
              else
                upperBoundary = new BoundaryExpression( "" + (minValue + ((i + 1) * step)) ); //$NON-NLS-1$
              operation = new PropertyIsBetweenOperation( propertyName, lowerBoundary, upperBoundary );
              getRuleCollection().get( i ).setFilter( new ComplexFilter( operation ) );
              ruleList.add( getRuleCollection().get( i ) );
            }
          }
          else if( patternRuleNumber > currentSize )
          {
            for( int i = 0; i < currentSize; i++ )
            {
              lowerBoundary = new BoundaryExpression( "" + (minValue + (i * step)) ); //$NON-NLS-1$
              if( (minValue + ((i + 1) * step)) > maxValue )
                upperBoundary = new BoundaryExpression( "" + maxValue ); //$NON-NLS-1$
              else
                upperBoundary = new BoundaryExpression( "" + (minValue + ((i + 1) * step)) ); //$NON-NLS-1$
              operation = new PropertyIsBetweenOperation( propertyName, lowerBoundary, upperBoundary );
              getRuleCollection().get( i ).setFilter( new ComplexFilter( operation ) );
              ruleList.add( getRuleCollection().get( i ) );
            }
            for( int i = currentSize; i < patternRuleNumber; i++ )
            {
              lowerBoundary = new BoundaryExpression( "" + (minValue + (i * step)) ); //$NON-NLS-1$
              if( (minValue + ((i + 1) * step)) > maxValue )
                upperBoundary = new BoundaryExpression( "" + maxValue ); //$NON-NLS-1$
              else
                upperBoundary = new BoundaryExpression( "" + (minValue + ((i + 1) * step)) ); //$NON-NLS-1$
              operation = new PropertyIsBetweenOperation( propertyName, lowerBoundary, upperBoundary );
              ruleList.add( StyleFactory.createRule( cloneSymbolizer( symbolizer ), tmpRule.getName(), "-name-" + i, "abstract", null, new ComplexFilter( operation ), false, tmpRule.getMinScaleDenominator(), tmpRule.getMaxScaleDenominator() ) ); //$NON-NLS-1$ //$NON-NLS-2$
            }
          }

          // then remove old ones
          final int collSize = getRuleCollection().size() - 1;
          for( int i = collSize; i >= 0; i-- )
          {
            removeRule( getRuleCollection().get( i ) );
            // FIXME
// getRulePatternCollection().removeRule( m_ruleCollection.get( i ) );
          }

          // add new ones
          for( int j = 0; j < ruleList.size(); j++ )
          {
            // FIXME
// getRulePatternCollection().addRule( ruleList.get( j ) );
// final FeatureTypeStyle style = m_input.getStyle();
// style.addRule( ruleList.get( j ) );
          }
          // update
          drawSymbolizerTabItems( toolkit, tmpRule, symbolizerTabFolder, getRuleCollection() );
          fireStyleChanged();
        }
      } );
    }
    editSymbolizerPanel.addPanelListener( new PanelListener()
    {
      @Override
      public void valueChanged( final PanelEvent event )
      {
        final int action = ((EditSymbolizerPanel) event.getSource()).getAction();

        if( action == EditSymbolizerPanel.REM_SYMB )
        {
          final int index1 = symbolizerTabFolder.getSelectionIndex();
          if( index1 >= 0 )
          {
            for( int i = 0; i < getRuleCollection().size(); i++ )
            {
              final Symbolizer s[] = getRuleCollection().get( i ).getSymbolizers();
              getRuleCollection().get( i ).removeSymbolizer( s[index1] );
            }
            symbolizerTabFolder.getItem( index1 ).dispose();
            setFocusedSymbolizerItem( index1 );
// setFocusedRuleItem( getRuleTabFolder().getSelectionIndex() );
            fireStyleChanged();
          }
          drawSymbolizerTabItems( toolkit, getRuleCollection().get( 0 ), symbolizerTabFolder, getRuleCollection() );
          symbolizerTabFolder.setSelection( index1 - 1 );
        }
        else if( action == EditSymbolizerPanel.FOR_SYMB )
        {
          final int index1 = symbolizerTabFolder.getSelectionIndex();
          if( index1 == (getRuleCollection().get( 0 ).getSymbolizers().length - 1) || index1 < 0 )
          {
            // nothing
          }
          else
          {
            for( int i = 0; i < getRuleCollection().size(); i++ )
            {
              final Symbolizer newOrderedObjects[] = new Symbolizer[getRuleCollection().get( i ).getSymbolizers().length];
              for( int counter4 = 0; counter4 < getRuleCollection().get( i ).getSymbolizers().length; counter4++ )
              {
                if( counter4 == index1 )
                  newOrderedObjects[counter4] = getRuleCollection().get( i ).getSymbolizers()[counter4 + 1];
                else if( counter4 == (index1 + 1) )
                  newOrderedObjects[counter4] = getRuleCollection().get( i ).getSymbolizers()[counter4 - 1];
                else
                  newOrderedObjects[counter4] = getRuleCollection().get( i ).getSymbolizers()[counter4];
              }
              getRuleCollection().get( i ).setSymbolizers( newOrderedObjects );
            }
            setFocusedSymbolizerItem( index1 + 1 );
// setFocusedRuleItem( getRuleTabFolder().getSelectionIndex() );
            fireStyleChanged();
            drawSymbolizerTabItems( toolkit, getRuleCollection().get( 0 ), symbolizerTabFolder, getRuleCollection() );
            symbolizerTabFolder.setSelection( index1 + 1 );
          }
        }
        else if( action == EditSymbolizerPanel.BAK_SYMB )
        {
          final int index1 = symbolizerTabFolder.getSelectionIndex();
          if( index1 > 0 )
          {
            for( int i = 0; i < getRuleCollection().size(); i++ )
            {
              final Symbolizer newOrderedObjects[] = new Symbolizer[getRuleCollection().get( i ).getSymbolizers().length];
              for( int counter5 = 0; counter5 < getRuleCollection().get( i ).getSymbolizers().length; counter5++ )
              {
                if( counter5 == index1 )
                  newOrderedObjects[counter5] = getRuleCollection().get( i ).getSymbolizers()[counter5 - 1];
                else if( counter5 == (index1 - 1) )
                  newOrderedObjects[counter5] = getRuleCollection().get( i ).getSymbolizers()[counter5 + 1];
                else
                  newOrderedObjects[counter5] = getRuleCollection().get( i ).getSymbolizers()[counter5];
              }
              getRuleCollection().get( i ).setSymbolizers( newOrderedObjects );
            }
            setFocusedSymbolizerItem( index1 - 1 );
// setFocusedRuleItem( getRuleTabFolder().getSelectionIndex() );
            fireStyleChanged();
            drawSymbolizerTabItems( toolkit, getRuleCollection().get( 0 ), symbolizerTabFolder, getRuleCollection() );
            symbolizerTabFolder.setSelection( index1 - 1 );
          }
        }
      }
    } );

    // ******* DISPLAY ALL symbolizers
    drawSymbolizerTabItems( toolkit, tmpRule, symbolizerTabFolder, getRuleCollection() );

// m_focusedRuleItem = index;
// composite.pack( true );
    return composite;
  }

  /**
   * @see org.kalypso.ui.editor.styleeditor.tab.ITabItem#updateItemControl()
   */
  @Override
  public void updateItemControl( )
  {
  }

  private String[] getNumericFeatureTypePropertylist( )
  {
    final IFeatureType featureType = m_input.getFeatureType();
    final IPropertyType[] numericFeatureTypePropertylist = RuleTabUtils.getNumericProperties( featureType );
    final String[] tmpList = new String[numericFeatureTypePropertylist.length];
    for( int i = 0; i < numericFeatureTypePropertylist.length; i++ )
      tmpList[i] = numericFeatureTypePropertylist[i].getName();
    return tmpList;
  }

  void drawSymbolizerTabItems( final FormToolkit toolkit, final Rule rule, final TabFolder symbolizerTabFolder, final RuleCollection ruleCollection )
  {
    // remove all existing items from tab folder
    final TabItem[] items = symbolizerTabFolder.getItems();
    for( int i = 0; i < items.length; i++ )
    {
      items[i].dispose();
      items[i] = null;
    }

    if( rule.getSymbolizers().length == 0 )
    {
      // add dummy invisilbe placeholder
      new FilterPatternSymbolizerTabItemBuilder( toolkit, symbolizerTabFolder, null, ruleCollection, -1 );
      symbolizerTabFolder.setVisible( false );
    }
    else
    {
      for( int j = 0; j < rule.getSymbolizers().length; j++ )
      {
        final IStyleInput<Symbolizer> input = new StyleInput<Symbolizer>( rule.getSymbolizers()[j], m_input );
        new FilterPatternSymbolizerTabItemBuilder( toolkit, symbolizerTabFolder, input, ruleCollection, j );
      }
      symbolizerTabFolder.pack();
      symbolizerTabFolder.setSize( 224, 259 );
      symbolizerTabFolder.setVisible( true );
    }
  }

  Symbolizer[] cloneSymbolizer( final Symbolizer[] symbolizers )
  {
    final IFeatureType featureType = m_input.getFeatureType();
    final Symbolizer[] returnArray = new Symbolizer[symbolizers.length];
    for( int i = 0; i < symbolizers.length; i++ )
    {
      returnArray[i] = cloneSymbolizer( symbolizers[i], featureType );
      if( returnArray[i] == null )
        return null;
    }
    return returnArray;
  }

  private Symbolizer cloneSymbolizer( final Symbolizer symbolizer, final IFeatureType featureType )
  {
    final PropertyName geomPropertyName = symbolizer.getGeometry().getPropertyName();
    if( symbolizer instanceof PointSymbolizer )
      return AddSymbolizerComposite.createSymbolizer( geomPropertyName, SymbolizerType.POINT, featureType );

    if( symbolizer instanceof LineSymbolizer )
      return AddSymbolizerComposite.createSymbolizer( geomPropertyName, SymbolizerType.LINE, featureType );

    if( symbolizer instanceof TextSymbolizer )
      return AddSymbolizerComposite.createSymbolizer( geomPropertyName, SymbolizerType.TEXT, featureType );

    if( symbolizer instanceof PolygonSymbolizer )
      return AddSymbolizerComposite.createSymbolizer( geomPropertyName, SymbolizerType.POLYGON, featureType );

    return null;
  }

  void removeRule( final Rule rule )
  {
    // TODO
// final FeatureTypeStyle style = .getStyle();
// style.removeRule( rule );
  }

  public int getFocusedRuleItem( )
  {
    return m_focusedRuleItem;
  }

  public void setFocusedRuleItem( final int focusedRuleItem )
  {
    m_focusedRuleItem = focusedRuleItem;
  }

  public int getFocusedSymbolizerItem( )
  {
    return m_focusedSymbolizerItem;
  }

  public void setFocusedSymbolizerItem( final int focusedSymbolizerItem )
  {
    m_focusedSymbolizerItem = focusedSymbolizerItem;
  }

  private void fireStyleChanged( )
  {
    m_input.fireStyleChanged();
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString( )
  {
    return getItemLabel();
  }

}