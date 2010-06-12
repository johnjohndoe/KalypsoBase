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
package org.kalypso.model.wspm.ui.wizard;

import javax.xml.namespace.QName;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.kalypso.contribs.eclipse.jface.wizard.IUpdateable;
import org.kalypso.contribs.eclipse.jface.wizard.ResourceChooserGroup;
import org.kalypso.contribs.eclipse.ui.dialogs.KalypsoResourceSelectionDialog;
import org.kalypso.contribs.eclipse.ui.dialogs.ResourceSelectionValidator;
import org.kalypso.contribs.eclipse.ui.forms.MessageProvider;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.IPropertyTypeFilter;
import org.kalypso.gmlschema.property.IValuePropertyType;
import org.kalypso.gmlschema.property.PropertyUtils;
import org.kalypso.model.wspm.core.profil.filter.IProfilePointFilter;
import org.kalypso.model.wspm.core.profil.filter.ProfilePointFilterComposite;
import org.kalypso.model.wspm.ui.i18n.Messages;
import org.kalypso.model.wspm.ui.wizard.ThemeAndPropertyChooserGroup.PropertyDescriptor;
import org.kalypso.ogc.gml.IKalypsoFeatureTheme;
import org.kalypso.ogc.gml.IKalypsoTheme;
import org.kalypso.ogc.gml.IKalypsoThemeFilter;
import org.kalypso.ogc.gml.mapmodel.IMapModell;
import org.kalypsodeegree.model.feature.FeatureList;
import org.kalypsodeegree_impl.tools.GMLConstants;

/**
 * @author Gernot Belger
 */
public class IntersectRoughnessPage extends WizardPage implements IUpdateable, IKalypsoThemeFilter
{
  private final ResourceChooserGroup m_assignmentGroup = new ResourceChooserGroup( this, org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.wizard.IntersectRoughnessPage.0" ), org.kalypso.model.wspm.ui.i18n.Messages.getString( "org.kalypso.model.wspm.ui.wizard.IntersectRoughnessPage.1" ) ); //$NON-NLS-1$ //$NON-NLS-2$

  private final ThemeAndPropertyChooserGroup m_themeGroup;

  private final IMapModell m_modell;

  private final PropertyDescriptor m_geoPd;

  private final PropertyDescriptor m_valuePd;

  private final ProfilePointFilterComposite m_filterChooser = new ProfilePointFilterComposite();

  public IntersectRoughnessPage( final IMapModell modell )
  {
    super( "intersectRoughnessPage", Messages.getString( "org.kalypso.model.wspm.ui.wizard.IntersectRoughnessPage.2" ), null ); //$NON-NLS-1$ //$NON-NLS-2$

    setDescription( Messages.getString( "org.kalypso.model.wspm.ui.wizard.IntersectRoughnessPage.3" ) ); //$NON-NLS-1$

    m_modell = modell;

    final IPropertyTypeFilter geoFilter = new IPropertyTypeFilter()
    {
      @Override
      public boolean accept( final IPropertyType pt )
      {
        if( pt instanceof IValuePropertyType )
        {
          final QName valueQName = ((IValuePropertyType) pt).getValueQName();
          if( valueQName.equals( GMLConstants.QN_POLYGON ) || valueQName.equals( GMLConstants.QN_MULTI_POLYGON ) )
            return true;
        }

        return false;
      }
    };

    final IPropertyTypeFilter valueFilter = new IPropertyTypeFilter()
    {
      @Override
      public boolean accept( final IPropertyType pt )
      {
        return pt instanceof IValuePropertyType && !((IValuePropertyType) pt).isGeometry();
      }
    };

    m_geoPd = new PropertyDescriptor( Messages.getString( "org.kalypso.model.wspm.ui.wizard.IntersectRoughnessPage.4" ), geoFilter, true ); //$NON-NLS-1$
    m_valuePd = new PropertyDescriptor( Messages.getString( "org.kalypso.model.wspm.ui.wizard.IntersectRoughnessPage.5" ), valueFilter, false ); //$NON-NLS-1$

    final PropertyDescriptor[] pds = new PropertyDescriptor[] { m_geoPd, m_valuePd };
    m_themeGroup = new ThemeAndPropertyChooserGroup( this, m_modell, this, pds );
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
    final Group polygoneGroup = m_themeGroup.createControl( composite );
    polygoneGroup.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );
    polygoneGroup.setText( Messages.getString( "org.kalypso.model.wspm.ui.wizard.IntersectRoughnessPage.6" ) ); //$NON-NLS-1$

    /* Assignment Group */
    final IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

    m_assignmentGroup.setDialogSettings( getDialogSettings() );
    final IResource initialSelection = getAssignmentPath() == null ? null : root.findMember( getAssignmentPath() );
    final KalypsoResourceSelectionDialog dialog = new KalypsoResourceSelectionDialog( getShell(), initialSelection, Messages.getString( "org.kalypso.model.wspm.ui.wizard.IntersectRoughnessPage.14" ), new String[] { "gml" }, root, new ResourceSelectionValidator() ); //$NON-NLS-1$//$NON-NLS-2$
    m_assignmentGroup.setSelectionDialog( dialog );

    final Control assignmentGroup = m_assignmentGroup.createControl( composite );
    assignmentGroup.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );

    final Control filterControl = createFilterGroup( composite );
    filterControl.setLayoutData( new GridData( SWT.FILL, SWT.BEGINNING, true, false ) );

    setControl( composite );
  }

  private Control createFilterGroup( final Composite composite )
  {
    final Group group = new Group( composite, SWT.NONE );
    group.setLayout( new FillLayout() );
    group.setText( ProfilePointFilterComposite.STR_GROUP_TEXT );

    m_filterChooser.createControl( group, SWT.BORDER );

    m_filterChooser.setDialogSettings( getDialogSettings() );

    m_filterChooser.addCheckStateListener( new ICheckStateListener()
    {
      @Override
      public void checkStateChanged( final CheckStateChangedEvent event )
      {
        update();
      }
    } );

    return group;
  }

  private IKalypsoFeatureTheme getPolygoneTheme( )
  {
    return (IKalypsoFeatureTheme) m_themeGroup.getTheme();
  }

  public FeatureList getPolygoneFeatures( )
  {
    final IKalypsoFeatureTheme polygoneTheme = getPolygoneTheme();
    if( polygoneTheme == null )
      return null;

    return polygoneTheme.getFeatureList();
  }

  public IPropertyType getPolygoneGeomProperty( )
  {
    return m_themeGroup.getProperty( m_geoPd );
  }

  public IPropertyType getPolygoneValueProperty( )
  {
    return m_themeGroup.getProperty( m_valuePd );
  }

  public IPath getAssignmentPath( )
  {
    return m_assignmentGroup.getPath();
  }

  /**
   * @see org.kalypso.contribs.eclipse.jface.wizard.IUpdateable#update()
   */
  @Override
  public void update( )
  {
    final IMessageProvider message = validatePage();
    if( message == null )
      setMessage( null );
    else
      setMessage( message.getMessage(), message.getMessageType() );
    setPageComplete( message == null );
  }

  private IMessageProvider validatePage( )
  {
    final IPath assignmentPath = m_assignmentGroup.getPath();
    final IKalypsoTheme polygoneTheme = getPolygoneTheme();
    final IPropertyType polygoneGeomProperty = getPolygoneGeomProperty();
    final IPropertyType polygoneValueProperty = getPolygoneValueProperty();

    final boolean pageComplete = polygoneTheme != null && polygoneGeomProperty != null && polygoneValueProperty != null && assignmentPath != null;

    setPageComplete( pageComplete );

    if( polygoneTheme == null )
      return new MessageProvider( Messages.getString( "org.kalypso.model.wspm.ui.wizard.IntersectRoughnessPage.10" ), ERROR ); //$NON-NLS-1$

    if( polygoneValueProperty == null )
      return new MessageProvider( Messages.getString( "org.kalypso.model.wspm.ui.wizard.IntersectRoughnessPage.11" ), ERROR ); //$NON-NLS-1$

    if( assignmentPath == null )
      return new MessageProvider( Messages.getString( "org.kalypso.model.wspm.ui.wizard.IntersectRoughnessPage.12" ), ERROR ); //$NON-NLS-1$

    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.IKalypsoThemeFilter#accept(org.kalypso.ogc.gml.IKalypsoTheme)
   */
  @Override
  public boolean accept( final IKalypsoTheme theme )
  {
    if( theme instanceof IKalypsoFeatureTheme )
    {
      final IKalypsoFeatureTheme featureTheme = (IKalypsoFeatureTheme) theme;
      final IFeatureType featureType = featureTheme.getFeatureType();
      if( featureType != null )
      {
        final IPropertyType[] polygoneProperties = PropertyUtils.filterProperties( featureType, m_geoPd.filter );
        if( polygoneProperties.length > 0 )
          return true;
      }
    }

    return false;
  }

  public IProfilePointFilter getSelectedPointFilter( )
  {
    return m_filterChooser;
  }

}
