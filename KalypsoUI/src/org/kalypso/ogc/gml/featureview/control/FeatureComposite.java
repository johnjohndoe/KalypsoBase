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
package org.kalypso.ogc.gml.featureview.control;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.kalypso.commons.command.ICommand;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.swt.ColorUtilities;
import org.kalypso.gmlschema.annotation.AnnotationUtilities;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.maker.IFeatureviewFactory;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.template.featureview.Button;
import org.kalypso.template.featureview.Checkbox;
import org.kalypso.template.featureview.ColorLabelType;
import org.kalypso.template.featureview.Combo;
import org.kalypso.template.featureview.CompositeType;
import org.kalypso.template.featureview.ControlType;
import org.kalypso.template.featureview.DynamicTabFolder;
import org.kalypso.template.featureview.Extensioncontrol;
import org.kalypso.template.featureview.FeatureviewType;
import org.kalypso.template.featureview.GeometryLabelType;
import org.kalypso.template.featureview.GridDataType;
import org.kalypso.template.featureview.Image;
import org.kalypso.template.featureview.LabelType;
import org.kalypso.template.featureview.LayoutDataType;
import org.kalypso.template.featureview.LayoutType;
import org.kalypso.template.featureview.PropertyControlType;
import org.kalypso.template.featureview.Radiobutton;
import org.kalypso.template.featureview.Spinner;
import org.kalypso.template.featureview.SubcompositeType;
import org.kalypso.template.featureview.TabFolder;
import org.kalypso.template.featureview.Table;
import org.kalypso.template.featureview.Text;
import org.kalypso.template.featureview.TupleResult;
import org.kalypso.template.featureview.ValidatorLabelType;
import org.kalypso.ui.KalypsoGisPlugin;
import org.kalypso.ui.KalypsoUIDebug;
import org.kalypso.util.swt.SWTUtilities;
import org.kalypsodeegree.filterencoding.FilterConstructionException;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.filterencoding.Operation;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.filterencoding.AbstractOperation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * @author Gernot Belger
 */
public class FeatureComposite extends AbstractFeatureControl implements IFeatureChangeListener, ModifyListener, IFeatureComposite
{
  private static final String DATA_LAYOUTDATA = "layoutData"; //$NON-NLS-1$

  private static final String DATA_CONTROL_TYPE = "controlType"; //$NON-NLS-1$

  private static final LayoutDataType NULL_LAYOUT_DATA_TYPE = new LayoutDataType();

  /**
   * The flag, indicating, if the green hook should be displayed.
   */
  private boolean m_showOk = false;

  /** Used for the compability-hack. Is it possible to get this from the binding classes? */
  private static String FEATUREVIEW_NAMESPACE = "featureview.template.kalypso.org"; //$NON-NLS-1$

  private final Collection<IFeatureControl> m_featureControls = new ArrayList<IFeatureControl>();

  private final Collection<Control> m_swtControls = new ArrayList<Control>();

  private final Collection<ModifyListener> m_modifyListeners = new ArrayList<ModifyListener>( 5 );

  private Control m_control = null;

  private final IFeatureSelectionManager m_selectionManager;

  private FormToolkit m_formToolkit = null;

  private final IFeatureviewFactory m_featureviewFactory;

  /**
   * Constructs the FeatureComposite.
   * 
   * @param feature
   *          If you want to add a feature directly at instantiation time, provide it here, otherwise leave it null.
   * @param selectionManager
   *          A selection manager, which provides functionality for adding and removing a feature from an selection and
   *          it handels the registration of listerners and so on. It has to implement IFeatureSelectionManager. You can
   *          get a default one for the features here
   *          <strong>KalypsoCorePlugin.getDefault().getSelectionManager()</strong>.
   * @param featureviewFactory
   *          A factory which delivers feature-view-templates (e.g. FeatureviewHelper).
   */
  public FeatureComposite( final Feature feature, final IFeatureSelectionManager selectionManager, final IFeatureviewFactory featureviewFactory )
  {
    super( feature, null );

    m_selectionManager = selectionManager;
    m_featureviewFactory = featureviewFactory;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#updateControl()
   */
  public void updateControl( )
  {
    for( final IFeatureControl fc : m_featureControls )
      fc.updateControl();

    for( final Control control : m_swtControls )
      updateLayoutData( control );

    if( m_control != null && !m_control.isDisposed() && m_control instanceof Composite )
      ((Composite) m_control).layout();
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#dispose()
   */
  @Override
  public void dispose( )
  {
    disposeControl();

    m_modifyListeners.clear();
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#isValid()
   */
  public boolean isValid( )
  {
    for( final Object element : m_featureControls )
    {
      final IFeatureControl fc = (IFeatureControl) element;

      if( !fc.isValid() )
        return false;
    }

    return true;
  }

  public Control createControl( final Composite parent, final int defaultStyle, final IFeatureType ft )
  {
    final FeatureviewType view = m_featureviewFactory.get( ft, getFeature() );

    if( m_formToolkit != null )
      m_formToolkit.adapt( parent );

    m_control = createControl( parent, defaultStyle, view );

    /* If a toolkit is set, use it. */
    if( m_formToolkit != null )
      m_formToolkit.adapt( m_control, true, true );

    return m_control;
  }

  public final Control createControl( final Composite parent, final int defaultStyle )
  {
    try
    {
      return createControl( parent, defaultStyle, getFeature().getFeatureType() );
    }
    catch( final Throwable t )
    {
      final org.eclipse.swt.widgets.Text text = new org.eclipse.swt.widgets.Text( parent, SWT.MULTI );
      text.setLayoutData( new GridData( SWT.FILL, SWT.FILL, true, true ) );
      text.setEditable( false );
      final String trace = ExceptionUtils.getStackTrace( t );
      text.setText( trace );
      return text;
    }
  }

  private Control createControl( final Composite parent, final int defaultStyle, final ControlType controlType )
  {
    final Feature feature = getFeature();

    final IFeatureType featureType = feature == null ? null : feature.getFeatureType();
    final IPropertyType propertyType = getProperty( featureType, controlType );

    final IAnnotation annotation = propertyType == null ? null : propertyType.getAnnotation();

    final String controlStyle = controlType.getStyle();
    final int styleToUse = controlStyle == null ? defaultStyle : SWTUtilities.createStyleFromString( controlStyle );

    final Control control = createControlFromControlType( parent, styleToUse, controlType, propertyType, annotation );

    // Set tooltip: an explicitly set tooltip always wins
    final String tooltipControlText = controlType.getTooltip();

    final String tooltipText = AnnotationUtilities.getAnnotation( annotation, tooltipControlText, IAnnotation.ANNO_TOOLTIP );
    control.setToolTipText( tooltipText );

    /* If a toolkit is set, use it. */
    if( m_formToolkit != null )
      m_formToolkit.adapt( control, true, true );

    control.setData( DATA_CONTROL_TYPE, controlType );

    m_swtControls.add( control );

    /* Set the background-color. */
    final Object backgroundColor = controlType.getBackgroundColor();
    if( backgroundColor != null )
    {
      RGB rgb = null;

      if( backgroundColor instanceof String )
        rgb = ColorUtilities.toRGBFromHTML( (String) backgroundColor );

      if( rgb != null )
        control.setBackground( new Color( control.getDisplay(), rgb ) );
    }

    final JAXBElement< ? extends LayoutDataType> jaxLayoutData = controlType.getLayoutData();
    final LayoutDataType layoutDataType;
    if( jaxLayoutData == null )
      layoutDataType = NULL_LAYOUT_DATA_TYPE;
    else
      layoutDataType = jaxLayoutData.getValue();

    control.setData( DATA_LAYOUTDATA, layoutDataType );
    updateLayoutData( control );

    return control;
  }

  private void updateLayoutData( final Control control )
  {
    if( control.isDisposed() )
      return;

    final Feature feature = getFeature();

    /* Update the layout data */
    final LayoutDataType layoutDataType = (LayoutDataType) control.getData( DATA_LAYOUTDATA );
    if( layoutDataType instanceof GridDataType )
    {
      final GridDataType gridDataType = (GridDataType) layoutDataType;
      final GridData gridData = new GridData();

      gridData.grabExcessHorizontalSpace = gridDataType.isGrabExcessHorizontalSpace();
      gridData.grabExcessVerticalSpace = gridDataType.isGrabExcessVerticalSpace();

      gridData.heightHint = gridDataType.getHeightHint();
      gridData.widthHint = gridDataType.getWidthHint();
      gridData.horizontalAlignment = SWTUtilities.getGridData( gridDataType.getHorizontalAlignment() );
      gridData.verticalAlignment = SWTUtilities.getGridData( gridDataType.getVerticalAlignment() );
      gridData.horizontalIndent = gridDataType.getHorizontalIndent();

      gridData.horizontalSpan = gridDataType.getHorizontalSpan();
      gridData.verticalSpan = gridDataType.getVerticalSpan();

      final Object excludeType = gridDataType.getExcludeOperation();
      gridData.exclude = evaluateOperation( feature, excludeType, false );

      control.setLayoutData( gridData );
    }
    else if( layoutDataType == NULL_LAYOUT_DATA_TYPE )
      control.setLayoutData( new GridData() );

    /* Update visibility, enablement, ... */
    final ControlType controlType = (ControlType) control.getData( DATA_CONTROL_TYPE );

    // REMARK: Special case for direct children of Tab-Folders. Setting the visibility here
    // breaks the tab folder behaviour. We assume, that the visibility of a
    // tab folder item is never changed depending on a value of a feature.
    if( !(control.getParent() instanceof org.eclipse.swt.widgets.TabFolder) )
    {
      final Object visibleOperation = controlType.getVisibleOperation();
      final boolean visible = evaluateOperation( getFeature(), visibleOperation, controlType.isVisible() );
      if( control.getVisible() != visible )
        control.setVisible( visible );
    }

    final Object enabledOperation = controlType.getEnabledOperation();
    final boolean enabled = evaluateOperation( getFeature(), enabledOperation, controlType.isEnabled() );
    if( control.getEnabled() != enabled )
      control.setEnabled( enabled );
  }

  private boolean evaluateOperation( final Feature feature, final Object operationElement, final boolean defaultValue )
  {
    try
    {
      if( operationElement instanceof String )
        return Boolean.parseBoolean( (String) operationElement );
      else if( operationElement instanceof Element )
      {
        KalypsoUIDebug.FEATUREVIEW_OPERATIONS.printf( String.format( "Found operation: %s%nfor feature: %s%n", operationElement, feature ) ); //$NON-NLS-1$

        final Element element = (Element) operationElement;
        final NodeList childNodes = element.getChildNodes();
        for( int i = 0; i < childNodes.getLength(); i++ )
        {
          final Node item = childNodes.item( i );
          if( item instanceof Element )
          {
            final Operation operation = AbstractOperation.buildFromDOM( (Element) item );
            final Boolean value = operation.evaluate( feature );
            final boolean result = value == null ? false : value.booleanValue();

            KalypsoUIDebug.FEATUREVIEW_OPERATIONS.printf( String.format( "Operation result: %s%n%n", result ) ); //$NON-NLS-1$

            return result;
          }
        }
      }
    }
    catch( final FilterConstructionException e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      KalypsoGisPlugin.getDefault().getLog().log( status );
    }
    catch( final FilterEvaluationException e )
    {
      final IStatus status = StatusUtilities.statusFromThrowable( e );
      KalypsoGisPlugin.getDefault().getLog().log( status );
    }

    return defaultValue;
  }

  private Control createControlFromControlType( final Composite parent, final int style, final ControlType controlType, final IPropertyType ftp, final IAnnotation annotation )
  {
    final Feature feature = getFeature();
    if( controlType instanceof CompositeType )
    {
      final CompositeType compositeType = (CompositeType) controlType;
      final Composite composite = createCompositeFromCompositeType( parent, style, compositeType, annotation );

      // Layout setzen
      final LayoutType layoutType = compositeType.getLayout().getValue();
      if( layoutType != null )
        composite.setLayout( createLayout( layoutType ) );

      for( final JAXBElement< ? extends ControlType> element : compositeType.getControl() )
      {
        final ControlType value = element.getValue();
        final int elementStyle = SWTUtilities.createStyleFromString( value.getStyle() );
        createControl( composite, elementStyle, value );
      }

      return composite;
    }

    // FIXME: create TabFolderFeatureControl
    if( controlType instanceof TabFolder )
    {
      final TabFolder tabFolderType = (TabFolder) controlType;

      final org.eclipse.swt.widgets.TabFolder tabFolder = new org.eclipse.swt.widgets.TabFolder( parent, style );

      final List<org.kalypso.template.featureview.TabFolder.TabItem> tabItem = tabFolderType.getTabItem();
      for( final org.kalypso.template.featureview.TabFolder.TabItem tabItemType : tabItem )
      {
        final String label = tabItemType.getTabLabel();
        final String itemLabel = AnnotationUtilities.getAnnotation( annotation, label, IAnnotation.ANNO_LABEL );

        final ControlType control = tabItemType.getControl().getValue();

        final TabItem item = new TabItem( tabFolder, SWT.NONE );
        item.setText( itemLabel );

        final Control tabControl = createControl( tabFolder, SWT.NONE, control );

        // ?? This seems to be breaking FeatureView's with observations. in this case control of parent will be used
        // FIXME: The parent if a TabItem MUST be the TabFolder! Everything else is just nonsense
        try
        {
          item.setControl( tabControl );
        }
        catch( final Exception e )
        {
          item.setControl( tabControl.getParent() );
        }
      }

      return tabFolder;
    }

    /* TODO: move all from above into the factory method */
    final IFeatureControlFactory controlFactory = createControlFactory( controlType );
    final IFeatureControl featureControl = createFeatureControl( controlFactory, feature, ftp, controlType, annotation );
    final Control control = featureControl.createControl( parent, style );
    addFeatureControl( featureControl );
    return control;
  }

  private IFeatureControl createFeatureControl( final IFeatureControlFactory controlFactory, final Feature feature, final IPropertyType ftp, final ControlType controlType, final IAnnotation annotation )
  {
    if( controlFactory == null )
    {
      final String msg = Messages.getString( "org.kalypso.ogc.gml.featureview.control.FeatureComposite.create" ); //$NON-NLS-1$
      return new LabelFeatureControl( feature, ftp, msg );
    }

    return controlFactory.createFeatureControl( this, feature, ftp, controlType, annotation );
  }

  // TODO: use extension point instead?
  private IFeatureControlFactory createControlFactory( final ControlType controlType )
  {
    if( controlType instanceof LabelType )
      return new LabelFeatureControlFactory();

    if( controlType instanceof Extensioncontrol )
      return new ExtensionFeatureControlFactory();

    if( controlType instanceof Text )
      return new TextFeatureControlFactory();

    if( controlType instanceof DynamicTabFolder )
      return new DynamicTabFolderFeatureControlFactory();

    if( controlType instanceof Button )
      return new ButtonFeatureControlFactory();

    if( controlType instanceof Image )
      return new ImageFeatureControlFactory();

    if( controlType instanceof TupleResult )
      return new TupleResultFeatureControlFactory();

    if( controlType instanceof SubcompositeType )
      return new SubFeatureControlFactory();

    if( controlType instanceof Table )
      return new TableFeatureContolFactory();

    if( controlType instanceof ValidatorLabelType )
      return new ValidatorLabelTypeFactory();

    if( controlType instanceof Spinner )
      return new SpinnerFeatureControlFactory();

    if( controlType instanceof ColorLabelType )
      return new ColorFeatureControlFactory();

    if( controlType instanceof Radiobutton )
      return new RadioFeatureControlFactory();

    if( controlType instanceof Checkbox )
      return new CheckboxFeatureControlFactory();

    if( controlType instanceof Combo )
      return new ComboFeatureControlFactory();

    if( controlType instanceof GeometryLabelType )
      return new GeometryFeatureControlFactory();

    return null;
  }

  private Composite createCompositeFromCompositeType( final Composite parent, final int style, final CompositeType compositeType, final IAnnotation annotation )
  {
    if( compositeType instanceof org.kalypso.template.featureview.Group )
    {
      final Group group = new org.eclipse.swt.widgets.Group( parent, style );

      final String groupControlText = ((org.kalypso.template.featureview.Group) compositeType).getText();

      final String groupText = AnnotationUtilities.getAnnotation( annotation, groupControlText, IAnnotation.ANNO_LABEL );
      group.setText( groupText );

      return group;
    }

    return new Composite( parent, style );
  }

  private Layout createLayout( final LayoutType layoutType )
  {
    if( layoutType instanceof org.kalypso.template.featureview.GridLayout )
    {
      final org.kalypso.template.featureview.GridLayout gridLayoutType = (org.kalypso.template.featureview.GridLayout) layoutType;
      final GridLayout layout = new GridLayout();
      layout.horizontalSpacing = gridLayoutType.getHorizontalSpacing();
      layout.verticalSpacing = gridLayoutType.getVerticalSpacing();
      layout.makeColumnsEqualWidth = gridLayoutType.isMakeColumnsEqualWidth();
      layout.marginHeight = gridLayoutType.getMarginHeight();
      layout.marginWidth = gridLayoutType.getMarginWidth();
      layout.numColumns = gridLayoutType.getNumColumns();

      return layout;
    }

    return null;
  }

  private void addFeatureControl( final IFeatureControl fc )
  {
    m_featureControls.add( fc );
    fc.addChangeListener( this );
    fc.addModifyListener( this );
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#addModifyListener(org.eclipse.swt.events.ModifyListener)
   */
  public void addModifyListener( final ModifyListener l )
  {
    m_modifyListeners.add( l );
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#removeModifyListener(org.eclipse.swt.events.ModifyListener)
   */
  public void removeModifyListener( final ModifyListener l )
  {
    m_modifyListeners.remove( this );
  }

  @Override
  public void setFeature( final Feature feature )
  {
    super.setFeature( feature );
    for( final Object element : m_featureControls )
    {
      final IFeatureControl fc = (IFeatureControl) element;
      fc.setFeature( feature );
    }
  }

  public void disposeControl( )
  {
    for( final Object element : m_featureControls )
    {
      final IFeatureControl fc = (IFeatureControl) element;
      fc.dispose();
    }
    m_featureControls.clear();

    for( final Object element : m_swtControls )
    {
      final Control c = (Control) element;
      c.dispose();
    }
    m_swtControls.clear();

    if( m_control != null )
    {
      m_control.dispose();
      m_control = null;
    }
  }

  public Control getControl( )
  {
    return m_control;
  }

  private IPropertyType getProperty( final IFeatureType featureType, final ControlType controlType )
  {
    if( featureType == null )
      return null;

    if( controlType instanceof PropertyControlType )
      return getPropertyTypeForQName( featureType, ((PropertyControlType) controlType).getProperty() );

    if( controlType instanceof CompositeType )
      return getPropertyTypeForQName( featureType, ((CompositeType) controlType).getProperty() );

    return null;
  }

  /**
   * Special method to retrieve a property from a feature for a special qname. Neeeded to have backward compability for
   * the feature-template. Before, the propertyName was given as xs:string (only the local part), now it is a xs:QName.
   * So old entries are interpreted against the namespace of the featuretemplate.
   */
  @SuppressWarnings("deprecation")
  private IPropertyType getPropertyTypeForQName( final IFeatureType featureType, final QName property )
  {
    if( property == null )
      return null;

    final IPropertyType propertyType = featureType.getProperty( property );
    if( propertyType != null )
      return propertyType;

    if( property.getNamespaceURI().equals( FeatureComposite.FEATUREVIEW_NAMESPACE ) )
    {
      final String localPart = property.getLocalPart();
      PluginUtilities.logToPlugin( KalypsoGisPlugin.getDefault(), IStatus.WARNING, "Still using localPart for property-name '" + localPart + "'. Use QName instead.", null ); //$NON-NLS-1$ //$NON-NLS-2$
      return featureType.getProperty( localPart );
    }

    return null;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureChangeListener#featureChanged(org.kalypso.commons.command.ICommand)
   */
  public void featureChanged( final ICommand changeCommand )
  {
    fireFeatureChange( changeCommand );
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureChangeListener#openFeatureRequested(org.kalypsodeegree.model.feature.Feature,
   *      org.kalypsodeegree.model.feature.IPropertyType)
   */
  public void openFeatureRequested( final Feature feature, final IPropertyType ftp )
  {
    fireOpenFeatureRequested( feature, ftp );
  }

  /**
   * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
   */
  public void modifyText( final ModifyEvent e )
  {
    final ModifyListener[] listeners = m_modifyListeners.toArray( new ModifyListener[m_modifyListeners.size()] );
    for( final ModifyListener listener : listeners )
      SafeRunnable.run( new SafeRunnable()
      {
        public void run( ) throws Exception
        {
          listener.modifyText( e );
        }
      } );
  }

  /** Traverse the tree feature controls adds all found feature view types to the given collection */
  public void collectViewTypes( final Collection<FeatureviewType> types )
  {
    final Feature feature = getFeature();
    if( feature == null )
      return;

    final FeatureviewType type = m_featureviewFactory.get( feature.getFeatureType(), feature );
    types.add( type );

    for( final IFeatureControl control : m_featureControls )
      if( control instanceof FeatureComposite )
        ((FeatureComposite) control).collectViewTypes( types );
      else if( control instanceof SubFeatureControl )
      {
        final IFeatureControl fc = ((SubFeatureControl) control).getFeatureControl();
        if( fc instanceof FeatureComposite )
          ((FeatureComposite) fc).collectViewTypes( types );
      }
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureComposite#getFeatureviewFactory()
   */
  @Override
  public IFeatureviewFactory getFeatureviewFactory( )
  {
    return m_featureviewFactory;
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.control.IFeatureComposite#getSelectionManager()
   */
  @Override
  public IFeatureSelectionManager getSelectionManager( )
  {
    return m_selectionManager;
  }

  public FormToolkit getFormToolkit( )
  {
    return m_formToolkit;
  }

  public void setFormToolkit( final FormToolkit formToolkit )
  {
    m_formToolkit = formToolkit;
  }

  /**
   * This function sets, if the green hook on a ok validated feature should be displayed. The default is false. This
   * flag has only an effect, if the validator label is activated.
   * 
   * @param showOk
   *          The flag, indicating, if the green hook should be displayed.
   */
  public void setShowOk( final boolean showOk )
  {
    m_showOk = showOk;
  }

  /**
   * This function returns the flag for displaying the green hook on a ok validated feature.
   * 
   * @return The flag, indicating, if the green hook should be displayed.
   */
  public boolean isShowOk( )
  {
    return m_showOk;
  }
}