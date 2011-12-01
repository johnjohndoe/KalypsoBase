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
package org.kalypso.model.wspm.ui.profil.wizard.propertyEdit;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.kalypso.contribs.eclipse.swt.events.DoubleModifyListener;
import org.kalypso.contribs.eclipse.ui.forms.MessageProvider;
import org.kalypso.contribs.java.lang.NumberUtils;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter;
import org.kalypso.model.wspm.core.profil.filter.ProfilePointFilterComposite;
import org.kalypso.model.wspm.core.util.pointpropertycalculator.IPointPropertyCalculator;
import org.kalypso.model.wspm.ui.KalypsoModelWspmUIPlugin;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.wizard.utils.IWspmWizardConstants;
import org.kalypso.observation.result.IComponent;
import org.kalypso.observation.result.IRecord;

/**
 * @author kimwerner
 */
public class OperationChooserPage extends WizardPage
{
  private class PropertyCalculator
  {
    public final String m_id;

    public final String m_tooltip;

    public final IPointPropertyCalculator m_calculator;

    public PropertyCalculator( final String id, final String tooltip, final IPointPropertyCalculator calculator )
    {
      m_tooltip = tooltip;
      m_id = id;
      m_calculator = calculator;
    }
  }

  private final String SETTINGS_CALCULATOR_ID = "operationChooserPage.selectedcalculator"; //$NON-NLS-1$

  private final String SETTINGS_CALCULATOR_VALUE = "operationChooserPage.calculatorvalue"; //$NON-NLS-1$

  private final List<PropertyCalculator> m_calculators = new ArrayList<PropertyCalculator>();

  private Double m_value = Double.NaN;

  private final ProfilePointFilterComposite m_filterChooser = new ProfilePointFilterComposite( IWspmWizardConstants.FILTER_USAGE_SECTION );

  public OperationChooserPage( final String title )
  {
    super( "operationChooserPage", title, null ); //$NON-NLS-1$
  }

  /**
   * Add additional filters to this page.
   */
  public void addFilter( final IProfilePointFilter filter )
  {
    m_filterChooser.addFilter( filter );
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    final Composite panel = new Composite( parent, SWT.NONE );
    panel.setLayout( new GridLayout() );

    String selectedCalculator = ""; //$NON-NLS-1$
    String doubleValue = ""; //$NON-NLS-1$
    final IDialogSettings dialogSettings = getDialogSettings();
    if( dialogSettings != null )
    {
      // get selected filters

      // get selected calculator
      selectedCalculator = dialogSettings.get( SETTINGS_CALCULATOR_ID );

      // get doubleValue
      doubleValue = dialogSettings.get( SETTINGS_CALCULATOR_VALUE );
      if( doubleValue != null && doubleValue != "" ) //$NON-NLS-1$
      {
        m_value = Double.valueOf( doubleValue );
      }
      else
        m_value = Double.NaN;
    }

    createFilterGroup( panel );
    createOperationGroup( panel, selectedCalculator, doubleValue );
    setControl( panel );
  }

  private void createFilterGroup( final Composite composite )
  {
    final Group group = new Group( composite, SWT.NONE );
    group.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
    final GridLayout layout = new GridLayout();
    layout.marginHeight = 0;
    layout.marginWidth = 0;
    group.setLayout( layout );
    group.setText( ProfilePointFilterComposite.STR_GROUP_TEXT );

    final Control filterControl = m_filterChooser.createControl( group, SWT.BORDER );
    filterControl.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );

    m_filterChooser.setDialogSettings( getDialogSettings() );
    m_filterChooser.addCheckStateListener( new ICheckStateListener()
    {
      @Override
      public void checkStateChanged( final CheckStateChangedEvent event )
      {
        updateMessage();
      }
    } );
  }

  private void createOperationGroup( final Composite composite, final String calculatorId, final String value )
  {
    final Group group = new Group( composite, SWT.NONE );
    group.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
    group.setLayout( new GridLayout( 3, false ) );
    group.setText( Messages.getString( "org.kalypso.model.wspm.ui.profil.wizard.propertyEdit.OperationChooserPage.0" ) ); //$NON-NLS-1$

    final Label lbl = new Label( group, SWT.NONE );
    lbl.setText( Messages.getString( "org.kalypso.model.wspm.ui.profil.wizard.propertyEdit.OperationChooserPage.1" ) ); //$NON-NLS-1$
    lbl.setLayoutData( new GridData( SWT.BEGINNING, SWT.CENTER, false, false ) );

    final Combo combo = new Combo( group, SWT.DROP_DOWN | SWT.READ_ONLY );
    combo.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    final Display display = group.getDisplay();
    final Color goodColor = display.getSystemColor( SWT.COLOR_BLACK );
    final Color badColor = display.getSystemColor( SWT.COLOR_RED );
    final DoubleModifyListener doubleModifyListener = new DoubleModifyListener( goodColor, badColor );

    final Text bldText = new Text( group, SWT.TRAIL | SWT.SINGLE | SWT.BORDER );
    bldText.setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    if( value != null )
      bldText.setText( value );

    // TODO: move this code into a separate class
    final IExtensionRegistry registry = Platform.getExtensionRegistry();
    final IConfigurationElement[] elements = registry.getConfigurationElementsFor( "org.kalypso.model.wspm.ui.pointPropertyCalculator" ); //$NON-NLS-1$
    for( final IConfigurationElement element : elements )
    {
      final String id = element.getAttribute( "id" ); //$NON-NLS-1$
      final String label = element.getAttribute( "label" ); //$NON-NLS-1$
      final String tooltip = element.getAttribute( "tooltip" ); //$NON-NLS-1$
      try
      {
        final IPointPropertyCalculator calculator = (IPointPropertyCalculator) element.createExecutableExtension( "class" ); //$NON-NLS-1$
        final PropertyCalculator propCalc = new PropertyCalculator( id, tooltip, calculator );
        m_calculators.add( propCalc );
        combo.add( label );
        if( id.equals( calculatorId ) )
        {
          combo.select( combo.getItemCount() - 1 );
          bldText.setToolTipText( tooltip );
        }
      }
      catch( final CoreException e )
      {
        KalypsoModelWspmUIPlugin.getDefault().getLog().log( e.getStatus() );
      }

    }

    bldText.addModifyListener( doubleModifyListener );

    bldText.addFocusListener( new FocusAdapter()
    {
      @Override
      public void focusGained( final FocusEvent e )
      {
        bldText.selectAll();
      }

      @Override
      public void focusLost( final FocusEvent e )
      {
        handleFocusLost( bldText.getText() );
      }
    } );

    combo.addSelectionListener( new SelectionAdapter()
    {
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleCalculationSelected( combo, bldText );
      }

    } );
  }

  protected void handleCalculationSelected( final Combo combo, final Text bldText )
  {
    final IDialogSettings dialogSettings = getDialogSettings();
    if( dialogSettings != null )
    {
      final PropertyCalculator propertyCalculator = m_calculators.get( combo.getSelectionIndex() );
      dialogSettings.put( SETTINGS_CALCULATOR_ID, propertyCalculator.m_id );
      bldText.setToolTipText( propertyCalculator.m_tooltip );
    }

    updateMessage();
  }

  public void changeProfile( final IProfil profil, final Object[] properties )
  {
    final IComponent[] propertyIds = new IComponent[properties.length];
    for( int i = 0; i < properties.length; i++ )
    {
      propertyIds[i] = (IComponent) properties[i];
    }
    final IDialogSettings dialogSettings = getDialogSettings();
    IPointPropertyCalculator calculator = null;
    if( dialogSettings != null )
    {
      final String calculatorId = dialogSettings.get( SETTINGS_CALCULATOR_ID );
      if( calculatorId == null )
        return;

      for( final PropertyCalculator pc : m_calculators )
      {
        if( pc.m_id.equals( calculatorId ) )
        {
          calculator = pc.m_calculator;
          break;
        }
      }
    }

    final Set<IRecord> selectedPoints = new HashSet<IRecord>();
    for( final IRecord point : profil.getResult() )
    {
      if( m_filterChooser.accept( profil, point ) )
        selectedPoints.add( point );
    }

    calculator.calculate( m_value, propertyIds, selectedPoints );
  }

  protected void handleFocusLost( final String text )
  {
    final IDialogSettings dialogSettings = getDialogSettings();
    m_value = NumberUtils.parseQuietDouble( text );
    if( dialogSettings != null )
      dialogSettings.put( SETTINGS_CALCULATOR_VALUE, m_value.isNaN() ? "" : m_value.toString() ); //$NON-NLS-1$

    updateMessage();
  }

  protected void updateMessage( )
  {
    final IMessageProvider validatePage = validatePage();
    if( validatePage == null )
    {
      setMessage( null );
      setPageComplete( true );
    }
    else
    {
      setMessage( validatePage.getMessage(), validatePage.getMessageType() );
      setPageComplete( false );
    }
  }

  private IMessageProvider validatePage( )
  {
    final Object[] checkedElements = m_filterChooser.getCheckedElements();
    if( checkedElements.length == 0 )
      return new MessageProvider( Messages.getString("OperationChooserPage.0"), IMessageProvider.WARNING ); //$NON-NLS-1$

    if( m_value.isNaN() )
      return new MessageProvider( Messages.getString("OperationChooserPage.1"), IMessageProvider.WARNING ); //$NON-NLS-1$

    return null;
  }

}
