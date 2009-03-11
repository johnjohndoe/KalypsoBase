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
 *  
 */
package org.kalypso.ui.editor.styleeditor;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.ogc.gml.KalypsoUserStyle;
import org.kalypso.ogc.gml.filterdialog.dialog.FilterDialog;
import org.kalypso.ui.editor.styleeditor.dialogs.StyleEditorErrorDialog;
import org.kalypso.ui.editor.styleeditor.panels.AddSymbolizerPanel;
import org.kalypso.ui.editor.styleeditor.panels.DenominatorInputPanel;
import org.kalypso.ui.editor.styleeditor.panels.EditSymbolizerPanel;
import org.kalypso.ui.editor.styleeditor.panels.LegendLabel;
import org.kalypso.ui.editor.styleeditor.panels.PanelEvent;
import org.kalypso.ui.editor.styleeditor.panels.PanelListener;
import org.kalypso.ui.editor.styleeditor.panels.TextInputPanel;
import org.kalypsodeegree.filterencoding.Filter;
import org.kalypsodeegree.graphics.sld.Rule;
import org.kalypsodeegree.graphics.sld.Symbolizer;

/**
 * @author F.Lindemann
 */
public class RuleTabItem
{
  private int counter = 0;

  private TabFolder m_ruleTabFolder = null;

  private int m_focusedRuleItem = -1;

  private int focusedSymbolizerItem = -1;

  private final KalypsoUserStyle m_userStyle;

  final IFeatureType m_featureType;

  public RuleTabItem( final TabFolder ruleTabFolder, final KalypsoUserStyle userStyle, final IFeatureType featureType )
  {
    m_ruleTabFolder = ruleTabFolder;
    m_userStyle = userStyle;
    m_featureType = featureType;
  }

  public void drawRule( final Rule rule, final int i )
  {
    final TabItem tabItem = new TabItem( getRuleTabFolder(), SWT.NULL );
    final Composite composite = new Composite( getRuleTabFolder(), SWT.NULL );
    final GridLayout compositeLayout = new GridLayout();
    composite.setSize( 270, 400 );
    composite.setLayout( compositeLayout );
    compositeLayout.marginWidth = 5;
    compositeLayout.marginHeight = 5;
    composite.layout();
    tabItem.setControl( composite );
    String ruleName;
    if( rule.getTitle() != null )
      ruleName = rule.getTitle();
    else if( rule.getName() != null )
      ruleName = rule.getName();
    else
    {
      ruleName = MessageBundle.STYLE_EDITOR_RULE + (++counter);
      rule.setTitle( ruleName );
    }
    tabItem.setText( ruleName );

    final TabFolder symbolizerTabFolder;

    final TextInputPanel titleInputPanel = new TextInputPanel( composite, MessageBundle.STYLE_EDITOR_TITLE, rule.getTitle() );
    titleInputPanel.addPanelListener( new PanelListener()
    {
      public void valueChanged( final PanelEvent event )
      {
        final String title = ((TextInputPanel) event.getSource()).getLabelText();
        if( title == null || title.trim().length() == 0 )
        {
          final StyleEditorErrorDialog errorDialog = new StyleEditorErrorDialog( composite.getShell(), MessageBundle.STYLE_EDITOR_ERROR_INVALID_INPUT, MessageBundle.STYLE_EDITOR_ERROR_NO_TITLE );
          errorDialog.showError();
          titleInputPanel.setInputText( rule.getTitle() );
        }
        else
        {
          rule.setTitle( title );
          tabItem.setText( title );
          getUserStyle().fireStyleChanged();
        }
        setFocusedRuleItem( getRuleTabFolder().getSelectionIndex() );
      }
    } );

    final DenominatorInputPanel minDenominatorPanel = new DenominatorInputPanel( composite, MessageBundle.STYLE_EDITOR_MIN_DENOM, rule.getMinScaleDenominator() );
    minDenominatorPanel.addPanelListener( new PanelListener()
    {
      public void valueChanged( final PanelEvent event )
      {
        final double min = ((DenominatorInputPanel) event.getSource()).getDenominator();
        final double max = rule.getMaxScaleDenominator();
        // verify that min<=max
        if( min > max )
        {
          final StyleEditorErrorDialog errorDialog = new StyleEditorErrorDialog( composite.getShell(), MessageBundle.STYLE_EDITOR_ERROR_INVALID_INPUT, MessageBundle.STYLE_EDITOR_ERROR_MIN_DENOM_BIG );
          errorDialog.showError();
          minDenominatorPanel.setDenominator( rule.getMinScaleDenominator() );
        }
        else
        {
          rule.setMinScaleDenominator( min );
          final Symbolizer symbolizers[] = rule.getSymbolizers();
          for( final Symbolizer element : symbolizers )
          {
            element.setMinScaleDenominator( min );
          }
          getUserStyle().fireStyleChanged();
        }
        setFocusedRuleItem( getRuleTabFolder().getSelectionIndex() );
      }
    } );

    // max denominator cannot be 0.0 as this would imply that the min
    // denominator needs to be smaller than 0.0 -> does not make sense
    // hence, if no max denomiator specified, get the denominator of the
    // individiual symbolizer
    if( rule.getMaxScaleDenominator() == 0.0 )
    {
      if( rule.getSymbolizers().length > 0 )
        rule.setMaxScaleDenominator( rule.getSymbolizers()[0].getMaxScaleDenominator() );
      else
        rule.setMaxScaleDenominator( Double.MAX_VALUE );
    }
    final DenominatorInputPanel maxDenominatorPanel = new DenominatorInputPanel( composite, MessageBundle.STYLE_EDITOR_MAX_DENOM, rule.getMaxScaleDenominator() );
    maxDenominatorPanel.addPanelListener( new PanelListener()
    {
      public void valueChanged( final PanelEvent event )
      {
        double max = ((DenominatorInputPanel) event.getSource()).getDenominator();
        final double min = rule.getMinScaleDenominator();
        // verify that min<=max
        if( min > max )
        {
          final StyleEditorErrorDialog errorDialog = new StyleEditorErrorDialog( composite.getShell(), MessageBundle.STYLE_EDITOR_ERROR_INVALID_INPUT, MessageBundle.STYLE_EDITOR_ERROR_MAX_DENOM_SMALL );
          errorDialog.showError();
          maxDenominatorPanel.setDenominator( rule.getMaxScaleDenominator() );
        }
        else
        {
          // add a minimum to max in order to be a little bit larger than the
          // current scale and
          // to keep the current view -> otherwise the rule would automatically
          // exculde this configuration
          max += 0.01;
          rule.setMaxScaleDenominator( max );
          final Symbolizer symbolizers[] = rule.getSymbolizers();
          for( final Symbolizer element : symbolizers )
          {
            element.setMaxScaleDenominator( max );
          }
          getUserStyle().fireStyleChanged();
        }
        setFocusedRuleItem( getRuleTabFolder().getSelectionIndex() );
      }
    } );

    final AddSymbolizerPanel addSymbolizerPanel = new AddSymbolizerPanel( composite, MessageBundle.STYLE_EDITOR_SYMBOLIZER, m_featureType );

    final EditSymbolizerPanel editSymbolizerPanel = new EditSymbolizerPanel( composite, rule.getSymbolizers().length );

    new LegendLabel( composite, m_userStyle, i );

    symbolizerTabFolder = new TabFolder( composite, SWT.NULL );

    editSymbolizerPanel.addPanelListener( new PanelListener()
    {
      public void valueChanged( final PanelEvent event )
      {
        final int action = ((EditSymbolizerPanel) event.getSource()).getAction();

        if( action == EditSymbolizerPanel.REM_SYMB )
        {
          final int index = symbolizerTabFolder.getSelectionIndex();
          if( index >= 0 )
          {
            final Symbolizer s[] = rule.getSymbolizers();
            rule.removeSymbolizer( s[index] );
            symbolizerTabFolder.getItem( index ).dispose();
            setFocusedSymbolizerItem( index );
            setFocusedRuleItem( getRuleTabFolder().getSelectionIndex() );
            getUserStyle().fireStyleChanged();
          }
          drawSymbolizerTabItems( rule, symbolizerTabFolder );
          symbolizerTabFolder.setSelection( index - 1 );
        }
        else if( action == EditSymbolizerPanel.FOR_SYMB )
        {
          final int index = symbolizerTabFolder.getSelectionIndex();
          if( index == (rule.getSymbolizers().length - 1) || index < 0 )
          {
            // nothing
          }
          else
          {
            final Symbolizer newOrderedObjects[] = new Symbolizer[rule.getSymbolizers().length];
            for( int counter4 = 0; counter4 < rule.getSymbolizers().length; counter4++ )
            {
              if( counter4 == index )
                newOrderedObjects[counter4] = rule.getSymbolizers()[counter4 + 1];
              else if( counter4 == (index + 1) )
                newOrderedObjects[counter4] = rule.getSymbolizers()[counter4 - 1];
              else
                newOrderedObjects[counter4] = rule.getSymbolizers()[counter4];
            }
            rule.setSymbolizers( newOrderedObjects );
            setFocusedSymbolizerItem( index + 1 );
            setFocusedRuleItem( getRuleTabFolder().getSelectionIndex() );
            getUserStyle().fireStyleChanged();
            drawSymbolizerTabItems( rule, symbolizerTabFolder );
            symbolizerTabFolder.setSelection( index + 1 );
          }
        }

        else if( action == EditSymbolizerPanel.BAK_SYMB )
        {
          final int index = symbolizerTabFolder.getSelectionIndex();
          if( index > 0 )
          {
            final Symbolizer newOrderedObjects[] = new Symbolizer[rule.getSymbolizers().length];
            for( int counter5 = 0; counter5 < rule.getSymbolizers().length; counter5++ )
            {
              if( counter5 == index )
                newOrderedObjects[counter5] = rule.getSymbolizers()[counter5 - 1];
              else if( counter5 == (index - 1) )
                newOrderedObjects[counter5] = rule.getSymbolizers()[counter5 + 1];
              else
                newOrderedObjects[counter5] = rule.getSymbolizers()[counter5];
            }
            rule.setSymbolizers( newOrderedObjects );
            setFocusedSymbolizerItem( index - 1 );
            setFocusedRuleItem( getRuleTabFolder().getSelectionIndex() );
            getUserStyle().fireStyleChanged();
            drawSymbolizerTabItems( rule, symbolizerTabFolder );
            symbolizerTabFolder.setSelection( index - 1 );
          }
        }
        editSymbolizerPanel.update( rule.getSymbolizers().length );
      }
    } );

    addSymbolizerPanel.addPanelListener( new PanelListener()
    {
      public void valueChanged( final PanelEvent event )
      {
        final Symbolizer symbolizer = ((AddSymbolizerPanel) event.getSource()).getSelection();
        if( symbolizer != null )
        {
          rule.addSymbolizer( symbolizer );
          getUserStyle().fireStyleChanged();
          setFocusedRuleItem( getRuleTabFolder().getSelectionIndex() );
          setFocusedSymbolizerItem( rule.getSymbolizers().length - 1 );
          editSymbolizerPanel.update( rule.getSymbolizers().length );
          drawSymbolizerTabItems( rule, symbolizerTabFolder );
          symbolizerTabFolder.setSelection( rule.getSymbolizers().length - 1 );
        }
      }
    } );

    // ***** Button Composite
    final Composite buttonComposite = new Composite( composite, SWT.NULL );
    buttonComposite.setLayout( new GridLayout( 1, true ) );
    final Button button = new Button( buttonComposite, SWT.NULL );
    button.setText( MessageBundle.STYLE_EDITOR_EDIT_FILTER );
    button.addSelectionListener( new SelectionListener()
    {
      public void widgetSelected( final SelectionEvent e )
      {
        final Filter oldFilter = rule.getFilter();
        Filter clone = null;
        if( oldFilter != null )
          try
          {
            clone = oldFilter.clone();
          }
          catch( final CloneNotSupportedException ex )
          {
            ex.printStackTrace();
          }
        final FilterDialog dialog = new FilterDialog( composite.getShell(), m_featureType, getUserStyle(), rule.getFilter(), null, null, false );
        final int open = dialog.open();
        if( open == Window.OK )
        {
          final Filter filter = dialog.getFilter();
          rule.setFilter( filter );
          getUserStyle().fireStyleChanged();
        }
        if( open == Window.CANCEL )
        {
          rule.setFilter( clone );
          getUserStyle().fireStyleChanged();
        }
      }

      public void widgetDefaultSelected( final SelectionEvent e )
      {
        widgetSelected( e );
      }
    } );

    // ******* DISPLAY ALL symbolizers
    drawSymbolizerTabItems( rule, symbolizerTabFolder );

    if( rule.getSymbolizers().length == 0 )
      symbolizerTabFolder.setVisible( false );
    if( m_focusedRuleItem == i && focusedSymbolizerItem != -1 )
      symbolizerTabFolder.setSelection( focusedSymbolizerItem );

    composite.pack( true );
  }

  void drawSymbolizerTabItems( final Rule rule, final TabFolder symbolizerTabFolder )
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
      new SymbolizerTabItemBuilder( symbolizerTabFolder, null, m_userStyle, m_featureType );
      symbolizerTabFolder.setVisible( false );
    }
    else
    {
      for( int j = 0; j < rule.getSymbolizers().length; j++ )
      {
        new SymbolizerTabItemBuilder( symbolizerTabFolder, rule.getSymbolizers()[j], m_userStyle, m_featureType );
      }
      symbolizerTabFolder.pack();
      symbolizerTabFolder.setSize( 224, 287 );
      symbolizerTabFolder.setVisible( true );
    }
  }

  public KalypsoUserStyle getUserStyle( )
  {
    return m_userStyle;
  }

  public TabFolder getRuleTabFolder( )
  {
    return m_ruleTabFolder;
  }

  public void setFocusedRuleItem( final int focusedRuleItem )
  {
    m_focusedRuleItem = focusedRuleItem;
  }

  public void setFocusedSymbolizerItem( final int m_focusedSymbolizerItem )
  {
    this.focusedSymbolizerItem = m_focusedSymbolizerItem;
  }
}