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
 * Created on 09.07.2004
 *
 */
package org.kalypso.ui.editor.styleeditor;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.contribs.eclipse.ui.progress.ProgressUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoFeatureTypeStyle;
import org.kalypso.ogc.gml.IKalypsoStyle;
import org.kalypso.ogc.gml.IKalypsoStyleListener;
import org.kalypso.ogc.gml.IKalypsoUserStyle;
import org.kalypso.ui.ImageProvider;
import org.kalypso.ui.editor.styleeditor.RuleTabItemBuilder.EventType;
import org.kalypso.ui.editor.styleeditor.panels.PanelEvent;
import org.kalypso.ui.editor.styleeditor.panels.PanelListener;
import org.kalypso.ui.editor.styleeditor.rulePattern.RuleFilterCollection;
import org.kalypsodeegree.graphics.sld.FeatureTypeStyle;
import org.kalypsodeegree.graphics.sld.Rule;

/**
 * @author F.Lindemann
 */
public class SLDEditorGuiBuilder
{
  private final Action m_saveAction = new Action( "Save", ImageProvider.IMAGE_STYLEEDITOR_SAVE )
  {
    /**
     * @see org.eclipse.jface.action.Action#runWithEvent(org.eclipse.swt.widgets.Event)
     */
    @Override
    public void runWithEvent( final Event event )
    {
      final Shell shell = event.display.getActiveShell();
      handleSave( shell );
    }
  };

  private final PanelListener m_panelListener = new PanelListener()
  {
    @Override
    public void valueChanged( final PanelEvent event )
    {
      handleValueChanged( event.eventType, event.param );
    }
  };

  private IFeatureType m_featureType = null;

  private Composite m_parent = null;

  private int m_focusedRuleItem = -1;

  private RuleFilterCollection m_rulePatternCollection = null;

  private final ScrolledForm m_form;

  private IKalypsoStyle m_style;

  private IKalypsoFeatureTheme m_theme;

  private final FormToolkit m_toolkit;

  private final IKalypsoStyleListener m_styleListener = new IKalypsoStyleListener()
  {
    @Override
    public void styleChanged( )
    {
      handleStyleChanged();
    }
  };

  public SLDEditorGuiBuilder( final FormToolkit toolkit, final Composite parent )
  {
    m_toolkit = toolkit;
    m_parent = parent;

    m_form = toolkit.createScrolledForm( m_parent );
    m_form.setText( Messages.getString( "org.kalypso.ui.editor.styleeditor.SLDEditorGuiBuilder.1" ) ); //$NON-NLS-1$
    m_form.getBody().setLayout( new GridLayout() );

    final IToolBarManager toolBarManager = m_form.getForm().getToolBarManager();
    createActions( toolBarManager );
    toolBarManager.update( true );

    setStyle( null, null );
  }

  private void createActions( final IToolBarManager toolBarManager )
  {
    m_saveAction.setToolTipText( MessageBundle.STYLE_EDITOR_SAVE_STYLE );

    toolBarManager.add( m_saveAction );
  }

  public Composite getControl( )
  {
    return m_form;
  }

  public void setStyle( final IKalypsoStyle style, final IKalypsoFeatureTheme theme )
  {
    setStyle( style, theme, -1 );
  }

  public void setStyle( final IKalypsoStyle style, final IKalypsoFeatureTheme theme, final int index )
  {
    // TODO: missing level: FeatureTypeStyle between UserStyle and Rule!

    if( m_style != null )
      m_style.removeStyleListener( m_styleListener );

    m_theme = theme;
    m_style = style;

    if( m_style != null )
      m_style.addStyleListener( m_styleListener );

    if( index != -1 )
      m_focusedRuleItem = index;

    // get IFeatureType from layer
    if( theme != null )
      m_featureType = theme.getFeatureType();

    /* Rebuild the ui */
    final Composite mainComposite = m_form.getBody();
    final Control[] bodyChildren = mainComposite.getChildren();
    for( final Control control : bodyChildren )
      control.dispose();

    /* Configure actions */
    m_saveAction.setEnabled( m_style != null && m_style.isDirty() );

    /* User-Style */
    final String formTitle = style == null ? MessageBundle.STYLE_EDITOR_NO_STYLE_FOR_EDITOR : style.getTitle();
    m_form.setText( formTitle );

    updateRuleTabs( mainComposite );

    m_form.reflow( true );
  }

  private void updateRuleTabs( final Composite mainComposite )
  {
    if( m_style == null )
      return;

    final FeatureTypeStyle fts = findFeatureTypeStyle();
    if( fts == null )
      return;

    final Rule[] rules = fts.getRules();

    m_rulePatternCollection = new RuleFilterCollection();
    // filter patterns from rules and draw them afterwards
    for( final Rule element : rules )
      m_rulePatternCollection.addRule( element );

    // check whether there are featureTypes that have numeric properties to be
    // used by a pattern-filter
    final List<IPropertyType> numericFeatureTypePropertylist = new ArrayList<IPropertyType>();
    if( m_featureType != null )
    {
      final IPropertyType[] ftp = m_featureType.getProperties();
      for( final IPropertyType propertyType : ftp )
      {
        if( propertyType instanceof IValuePropertyType )
        {
          final IValuePropertyType vpt = (IValuePropertyType) propertyType;
          final Class< ? > valueClass = vpt.getValueClass();
          if( Number.class.isAssignableFrom( valueClass ) )
            numericFeatureTypePropertylist.add( propertyType );
        }
      }
    }

    final RuleTabItemBuilder ruleTabItemBuilder = new RuleTabItemBuilder( m_toolkit, mainComposite, m_rulePatternCollection, m_style, fts, m_featureType, numericFeatureTypePropertylist );
    ruleTabItemBuilder.addPanelListener( m_panelListener );
    ruleTabItemBuilder.getControl().setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    ruleTabItemBuilder.draw();
    if( m_focusedRuleItem > -1 )
      ruleTabItemBuilder.setSelectedRule( m_focusedRuleItem );
  }

  private FeatureTypeStyle findFeatureTypeStyle( )
  {
    if( m_style instanceof IKalypsoFeatureTypeStyle )
      return (FeatureTypeStyle) m_style;

    if( m_style instanceof IKalypsoUserStyle )
    {
      final FeatureTypeStyle[] featureTypeStyles = ((IKalypsoUserStyle) m_style).getFeatureTypeStyles();
      if( featureTypeStyles.length > 0 )
        return featureTypeStyles[0];
    }

    return null;
  }

  public void setFocus( )
  {
    m_form.setFocus();
  }

  protected void handleValueChanged( final EventType eventType, final Object param )
  {
    switch( eventType )
    {
      case RULE_ADDED:
        break;

      case RULE_REMOVED:
        break;

      case RULE_BACKWARD:
        break;

      case RULE_FORWARD:
        break;

      case PATTERN_ADDED:
        break;
    }

    if( param instanceof Integer )
      m_focusedRuleItem = ((Integer) param);

    m_style.fireStyleChanged();
    setStyle( m_style, m_theme );
  }

  protected void handleSave( final Shell shell )
  {
    final String label = m_style.getLabel();
    final String titel = String.format( "Save - UserStyle '%s'", label );
    final String msg = String.format( "Save UserStyle '%s'?", label );
    MessageDialog.openQuestion( shell, titel, msg );

    final IKalypsoStyle style = m_style;

    final ICoreRunnableWithProgress operation = new ICoreRunnableWithProgress()
    {
      @Override
      public IStatus execute( final IProgressMonitor monitor ) throws CoreException
      {
        style.save( monitor );
        return Status.OK_STATUS;
      }
    };

    final IStatus result = ProgressUtilities.busyCursorWhile( operation );
    final String errorMsg = String.format( "Failed to save style." );
    ErrorDialog.openError( shell, titel, errorMsg, result );
  }

  protected void handleStyleChanged( )
  {
    m_saveAction.setEnabled( m_style != null && m_style.isDirty() );
  }

}