/*----------------    FILE HEADER KALYPSO ------------------------------------------
 *
 *  This file is part of kalypso.
 *  Copyright (C) 2004 by:
 *
 *  Technical University Hamburg-Harburg (TUHH)
 *  Institute of River and coastal engineering
 *  Denickestra�e 22
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
package org.kalypso.model.wspm.ui.profil.wizard.createDivider;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.namespace.QName;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.gmlschema.GMLSchemaUtilities;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IPropertyTypeFilter;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.PropertyUtils;
import org.kalypso.model.wspm.core.KalypsoModelWspmCoreExtensions;
import org.kalypso.model.wspm.core.gml.IProfileFeature;
import org.kalypso.model.wspm.core.profil.IProfilPointPropertyProvider;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.profil.wizard.utils.ThemeAndPropertyChooserGroup;
import org.kalypso.model.wspm.ui.profil.wizard.utils.ThemeAndPropertyChooserGroup.PropertyDescriptor;
import org.kalypso.observation.result.IComponent;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoThemeFilter;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree_impl.tools.GMLConstants;

/**
 * @author Gernot Belger
 */
public class CreateProfileDeviderPage extends WizardPage implements IUpdateable, IKalypsoThemeFilter
{
  private static final String SETTINGS_DEVIDER = "settings.devider.type"; //$NON-NLS-1$

  private static final String SETTINGS_USE_EXISTING = "settings.use.existing"; //$NON-NLS-1$

  private final ThemeAndPropertyChooserGroup m_themeGroup;

  private final PropertyDescriptor m_geoPd;

  private IComponent m_deviderType = null;

  private boolean m_useExisting = false;

  private final IKalypsoFeatureTheme m_profileTheme;

  private final String m_profileType;

  public CreateProfileDeviderPage( final IKalypsoFeatureTheme profileTheme, final String profileType )
  {
    super( "createProfileDeviderPage", Messages.getString( "org.kalypso.model.wspm.ui.wizard.CreateProfileDeviderPage.3" ), null ); //$NON-NLS-1$ //$NON-NLS-2$

    m_profileTheme = profileTheme;
    m_profileType = profileType;

    setMessage( Messages.getString( "org.kalypso.model.wspm.ui.wizard.CreateProfileDeviderPage.4" ) ); //$NON-NLS-1$

    final Set<QName> validGeomProperties = new HashSet<QName>();
    validGeomProperties.add( GMLConstants.QN_LINE_STRING );
    validGeomProperties.add( GMLConstants.QN_MULTI_LINE_STRING );
    validGeomProperties.add( GMLConstants.QN_CURVE );
    validGeomProperties.add( GMLConstants.QN_MULTI_CURVE );
    validGeomProperties.add( GMLConstants.QN_POLYGON );
    validGeomProperties.add( GMLConstants.QN_MULTI_POLYGON );

    final IPropertyTypeFilter geoFilter = new IPropertyTypeFilter()
    {
      @Override
      public boolean accept( final IPropertyType pt )
      {
        if( !(pt instanceof IValuePropertyType) )
          return false;

        final IValuePropertyType pt2 = (IValuePropertyType) pt;
        if( !pt2.isGeometry() )
          return false;

        final QName valueQName = pt2.getValueQName();
        return validGeomProperties.contains( valueQName );
      }
    };

    m_geoPd = new PropertyDescriptor( "&Geometry", geoFilter, true ); //$NON-NLS-1$

    final PropertyDescriptor[] pds = new PropertyDescriptor[] { m_geoPd };
    m_themeGroup = new ThemeAndPropertyChooserGroup( this, profileTheme.getMapModell(), this, pds );
  }

  /**
   * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
   */
  @Override
  public void createControl( final Composite parent )
  {
    final Composite composite = new Composite( parent, SWT.NONE );
    composite.setLayout( new GridLayout() );

    /* Polygone Group */
    m_themeGroup.setDialogSettings( getDialogSettings() );
    final Group themeGroup = m_themeGroup.createControl( composite );
    themeGroup.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
    themeGroup.setText( Messages.getString( "org.kalypso.model.wspm.ui.wizard.CreateProfileDeviderPage.6" ) ); //$NON-NLS-1$

    /* Devider Group */
    final Group deviderGroup = createDeviderGroup( composite );
    deviderGroup.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );

    /* Options */
    createOptions( composite );

    setControl( composite );
  }

  private void createOptions( final Composite composite )
  {
    final Button useExistingCheckbox = new Button( composite, SWT.CHECK );
    useExistingCheckbox.setText( Messages.getString( "org.kalypso.model.wspm.ui.wizard.CreateProfileDeviderPage.8" ) ); //$NON-NLS-1$
    useExistingCheckbox.setToolTipText( Messages.getString( "org.kalypso.model.wspm.ui.wizard.CreateProfileDeviderPage.9" ) ); //$NON-NLS-1$
    useExistingCheckbox.addSelectionListener( new SelectionAdapter()
    {
      /**
       * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
       */
      @Override
      public void widgetSelected( final SelectionEvent e )
      {
        handleKeepExistingChanged( useExistingCheckbox.getSelection() );
      }
    } );

    final IDialogSettings dialogSettings = getDialogSettings();
    if( dialogSettings != null )
    {
      final boolean useExisting = dialogSettings.getBoolean( SETTINGS_USE_EXISTING );
      useExistingCheckbox.setSelection( useExisting );
      m_useExisting = useExisting;
    }
  }

  protected void handleKeepExistingChanged( final boolean useExisting )
  {
    if( m_useExisting == useExisting )
      return;

    m_useExisting = useExisting;

    final IDialogSettings dialogSettings = getDialogSettings();
    if( dialogSettings != null )
      dialogSettings.put( SETTINGS_USE_EXISTING, useExisting );
  }

  private Group createDeviderGroup( final Composite composite )
  {
    final Group group = new Group( composite, SWT.NONE );
    group.setLayout( new GridLayout( 1, false ) );
    group.setText( Messages.getString( "org.kalypso.model.wspm.ui.wizard.CreateProfileDeviderPage.7" ) ); //$NON-NLS-1$

    final ComboViewer viewer = new ComboViewer( group, SWT.READ_ONLY | SWT.DROP_DOWN );
    viewer.getControl().setLayoutData( new GridData( SWT.FILL, SWT.CENTER, true, false ) );
    viewer.setContentProvider( new ArrayContentProvider() );
    viewer.setLabelProvider( new LabelProvider()
    {
      /**
       * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
       */
      @Override
      public String getText( final Object element )
      {
        final IComponent comp = (IComponent) element;
        return comp.getName();
      }
    } );
    viewer.setSorter( new ViewerSorter() );

    final IProfilPointPropertyProvider provider = KalypsoModelWspmCoreExtensions.getPointPropertyProviders( m_profileType );

    final String[] markerTypes = provider.getPointProperties();
    final Collection<IComponent> markerComponents = new ArrayList<IComponent>( markerTypes.length );
    for( final String markerType : markerTypes )
    {
      if( provider.isMarker( markerType ) )
        markerComponents.add( provider.getPointProperty( markerType ) );
    }
    viewer.setInput( markerComponents );

    viewer.addSelectionChangedListener( new ISelectionChangedListener()
    {
      @Override
      public void selectionChanged( final SelectionChangedEvent event )
      {
        final IStructuredSelection selection = (IStructuredSelection) event.getSelection();
        final Object firstElement = (selection).getFirstElement();
        handleDeviderChanged( (IComponent) firstElement );
      }
    } );

    final IStructuredSelection initialSelection = getInitialSelection( markerComponents );
    viewer.setSelection( initialSelection );

    return group;
  }

  protected IStructuredSelection getInitialSelection( final Collection<IComponent> markerComponents )
  {
    final IDialogSettings dialogSettings = getDialogSettings();
    if( dialogSettings == null )
    {
      if( markerComponents.size() > 0 )
        return new StructuredSelection( markerComponents.iterator().next() );
    }
    else
    {
      final String typeName = dialogSettings.get( SETTINGS_DEVIDER );
      for( final IComponent component : markerComponents )
      {
        if( component.getId().equals( typeName ) )
          return new StructuredSelection( component );
      }
    }

    return StructuredSelection.EMPTY;
  }

  protected void handleDeviderChanged( final IComponent type )
  {
    if( m_deviderType == type )
      return;

    m_deviderType = type;

    final IDialogSettings dialogSettings = getDialogSettings();
    if( dialogSettings != null )
      dialogSettings.put( SETTINGS_DEVIDER, type.getId() );
  }

  private IKalypsoFeatureTheme getTheme( )
  {
    return (IKalypsoFeatureTheme) m_themeGroup.getTheme();
  }

  public FeatureList getFeatures( )
  {
    final IKalypsoFeatureTheme polygoneTheme = getTheme();
    if( polygoneTheme == null )
      return null;

    return polygoneTheme.getFeatureList();
  }

  public IPropertyType getGeomProperty( )
  {
    return m_themeGroup.getProperty( m_geoPd );
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.IUpdateable#update()
   */
  @Override
  public void update( )
  {
    final IKalypsoTheme polygoneTheme = getTheme();
    final IPropertyType polygoneGeomProperty = getGeomProperty();

    final boolean pageComplete = polygoneTheme != null && polygoneGeomProperty != null;

    setPageComplete( pageComplete );

    if( polygoneTheme == null )
      setErrorMessage( Messages.getString( "org.kalypso.model.wspm.ui.wizard.CreateProfileDeviderPage.11" ) ); //$NON-NLS-1$
    else
    {
      setErrorMessage( null );
      setMessage( Messages.getString( "org.kalypso.model.wspm.ui.wizard.CreateProfileDeviderPage.12" ) ); //$NON-NLS-1$
    }
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoThemeFilter#accept(org.kalypso.ogc.gml.IKalypsoTheme)
   */
  @Override
  public boolean accept( final IKalypsoTheme theme )
  {
    if( theme instanceof IKalypsoFeatureTheme && theme != m_profileTheme )
    {
      final IKalypsoFeatureTheme featureTheme = (IKalypsoFeatureTheme) theme;
      final IFeatureType featureType = featureTheme.getFeatureType();
      if( featureType != null )
      {
        if( GMLSchemaUtilities.substitutes( featureType, IProfileFeature.QN_PROFILE ) )
          return false;

        final IPropertyType[] polygoneProperties = PropertyUtils.filterProperties( featureType, m_geoPd.filter );
        if( polygoneProperties.length > 0 )
          return true;
      }
    }

    return false;
  }

  public IComponent getDeviderType( )
  {
    return m_deviderType;
  }

  public boolean isUseExisting( )
  {
    return m_useExisting;
  }
}
