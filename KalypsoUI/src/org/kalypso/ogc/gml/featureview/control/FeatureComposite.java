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

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.kalypso.commons.command.ICommand;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.commons.i18n.ITranslator;
import org.kalypso.contribs.eclipse.core.runtime.PluginUtilities;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.swt.ColorUtilities;
import org.kalypso.contribs.eclipse.swt.SWTUtilities;
import org.kalypso.contribs.eclipse.ui.forms.ToolkitUtils;
import org.kalypso.gmlschema.annotation.AnnotationUtilities;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.i18n.Messages;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.control.composite.CompositeFeatureControlFactory;
import org.kalypso.ogc.gml.featureview.control.composite.IFeatureCompositionControl;
import org.kalypso.ogc.gml.featureview.control.composite.IFeatureCompositionControlFactory;
import org.kalypso.ogc.gml.featureview.control.composite.SectionCompositionFactory;
import org.kalypso.ogc.gml.featureview.control.composite.TablFolderCompositionFactory;
import org.kalypso.ogc.gml.featureview.maker.FeatureviewTypeWithContext;
import org.kalypso.ogc.gml.featureview.maker.IFeatureviewFactory;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypso.template.featureview.Button;
import org.kalypso.template.featureview.Checkbox;
import org.kalypso.template.featureview.ColorLabelType;
import org.kalypso.template.featureview.Combo;
import org.kalypso.template.featureview.CommandHyperlink;
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

  private FeatureViewTranslator m_translator;

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
  @Override
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

  @Override
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
    final FeatureviewTypeWithContext view = m_featureviewFactory.get( ft, getFeature() );

    m_translator = new FeatureViewTranslator( m_featureviewFactory.getTranslator( view, null ) );

    // TODO: dubious we shoudn't need to adapt the parent, that should already have been done by the calling code
    if( m_formToolkit != null )
      m_formToolkit.adapt( parent );

    m_control = createControl( parent, defaultStyle, view.getView(), m_translator );

    /* If a toolkit is set, use it. */
    if( m_formToolkit != null )
      m_formToolkit.adapt( m_control, true, true );

    return m_control;
  }

  @Override
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

  public Control createControl( final Composite parent, final int defaultStyle, final ControlType controlType, final ITranslator translator )
  {
    final Feature feature = getFeature();

    final IFeatureType featureType = feature == null ? null : feature.getFeatureType();
    final IPropertyType propertyType = getProperty( featureType, controlType );

    final IAnnotation annotation = propertyType == null ? null : propertyType.getAnnotation();

    final String controlStyle = controlType.getStyle();
    final int styleToUse = controlStyle == null ? defaultStyle : SWTUtilities.createStyleFromString( controlStyle );

    final Control control = createControlFromControlType( parent, styleToUse, controlType, propertyType, annotation, translator );

    // Set tooltip: an explicitly set tooltip always wins
    final String tooltipControlText = controlType.getTooltip();

    final String tooltipText = AnnotationUtilities.getAnnotation( annotation, tooltipControlText, IAnnotation.ANNO_TOOLTIP );
    final String translatedTooltipText = new I10nString( tooltipText, translator ).getValue();
    control.setToolTipText( translatedTooltipText );

    /* If a toolkit is set, use it. */
    applyToolkit( control );

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

  private void applyToolkit( final Control control )
  {
    if( m_formToolkit == null )
      return;

    if( control instanceof Composite )
    {
      final Composite panel = (Composite) control;

      if( panel instanceof Section )
        return;

      m_formToolkit.adapt( panel );

      final Control[] children = panel.getChildren();
      for( final Control child : children )
        applyToolkit( child );
    }
    else
      m_formToolkit.adapt( control, true, true );
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
    // breaks the tab folder behavior. We assume, that the visibility of a
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

  private Control createControlFromControlType( final Composite parent, final int style, final ControlType controlType, final IPropertyType ftp, final IAnnotation annotation, final ITranslator translator )
  {

    // NEUUU
    final Feature feature = getFeature();

    final IFeatureCompositionControlFactory compositionFactory = createCompositionFactory( controlType );
    if( compositionFactory != null )
    {
      final IFeatureCompositionControl composite = compositionFactory.createControl( this, annotation, translator );
      return composite.createControl( m_formToolkit, parent, style );
    }

    /* TODO: move all from above into the factory method */
    final IFeatureControlFactory controlFactory = createControlFactory( parent, controlType );
    final IFeatureControl featureControl = createFeatureControl( controlFactory, feature, ftp, controlType, annotation );
    final Control control = featureControl.createControl(m_formToolkit, parent, style );
    addFeatureControl( featureControl );
    return control;
  }

  private IFeatureCompositionControlFactory createCompositionFactory( final ControlType controlType )
  {
    if( controlType instanceof CompositeType )
      return new CompositeFeatureControlFactory( (CompositeType) controlType );

    if( controlType instanceof TabFolder )
      return new TablFolderCompositionFactory( (TabFolder) controlType );

    if( controlType instanceof org.kalypso.template.featureview.Section )
      return new SectionCompositionFactory( (org.kalypso.template.featureview.Section) controlType );

    return null;
  }

  private IFeatureControl createFeatureControl( final IFeatureControlFactory controlFactory, final Feature feature, final IPropertyType ftp, final ControlType controlType, final IAnnotation annotation )
  {
    if( controlFactory == null )
    {
      final String msg = Messages.getString( "org.kalypso.ogc.gml.featureview.control.FeatureComposite.create" ); //$NON-NLS-1$
      final IStatus status = new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), msg );
      return new StatusFeatureControl( status );
    }

    return controlFactory.createFeatureControl( this, feature, ftp, controlType, annotation );
  }

  // TODO: use extension point instead?
  private IFeatureControlFactory createControlFactory( final Composite parent, final ControlType controlType )
  {
    final FormToolkit toolkit = createOrGetToolkit( parent );

    if( controlType instanceof LabelType )
      return new LabelFeatureControlFactory();

    if( controlType instanceof Extensioncontrol )
      return new ExtensionFeatureControlFactory( toolkit );

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
      return new TableFeatureControlFactory();

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

    if( controlType instanceof CommandHyperlink )
      return new CommandHyperlinkFeatureControlFactory();

    return null;
  }

  /**
   * Returns a toolkit. Either the one we got already, or a new one that will be disposed if the given control is
   * disposed
   */
  private FormToolkit createOrGetToolkit( final Control control )
  {
    if( m_formToolkit == null )
    {
      final FormToolkit toolkit = ToolkitUtils.createToolkit( control );
      toolkit.setBackground( control.getBackground() );

      return toolkit;
    }
    else
      return m_formToolkit;
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
  @Override
  public void addModifyListener( final ModifyListener l )
  {
    m_modifyListeners.add( l );
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureControl#removeModifyListener(org.eclipse.swt.events.ModifyListener)
   */
  @Override
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

    final QName propertyName = findPropertyName( controlType );
    if( propertyName == null )
      return null;

    return getPropertyTypeForQName( featureType, propertyName );
  }

  private QName findPropertyName( final ControlType controlType )
  {
    if( controlType instanceof PropertyControlType )
      return ((PropertyControlType) controlType).getProperty();

    if( controlType instanceof CompositeType )
      return ((CompositeType) controlType).getProperty();

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
  @Override
  public void featureChanged( final ICommand changeCommand )
  {
    fireFeatureChange( changeCommand );
  }

  /**
   * @see org.kalypso.ogc.gml.featureview.IFeatureChangeListener#openFeatureRequested(org.kalypsodeegree.model.feature.Feature,
   *      org.kalypsodeegree.model.feature.IPropertyType)
   */
  @Override
  public void openFeatureRequested( final Feature feature, final IPropertyType ftp )
  {
    fireOpenFeatureRequested( feature, ftp );
  }

  /**
   * @see org.eclipse.swt.events.ModifyListener#modifyText(org.eclipse.swt.events.ModifyEvent)
   */
  @Override
  public void modifyText( final ModifyEvent e )
  {
    final ModifyListener[] listeners = m_modifyListeners.toArray( new ModifyListener[m_modifyListeners.size()] );
    for( final ModifyListener listener : listeners )
      SafeRunnable.run( new SafeRunnable()
      {
        @Override
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

    final FeatureviewTypeWithContext type = m_featureviewFactory.get( feature.getFeatureType(), feature );
    types.add( type.getView() );

    for( final IFeatureControl control : m_featureControls )
    {
      if( control instanceof FeatureComposite )
        ((FeatureComposite) control).collectViewTypes( types );
      else if( control instanceof SubFeatureControl )
      {
        final IFeatureControl fc = ((SubFeatureControl) control).getFeatureControl();
        if( fc instanceof FeatureComposite )
          ((FeatureComposite) fc).collectViewTypes( types );
      }
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

  @Override
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
  @Override
  public boolean isShowOk( )
  {
    return m_showOk;
  }

  @Override
  public URL getFeatureviewContext( )
  {
    final Feature feature = getFeature();
    if( feature == null )
      return null;

    final IFeatureType featureType = feature.getFeatureType();

    final FeatureviewTypeWithContext featureviewWithContext = m_featureviewFactory.get( featureType, feature );
    if( featureviewWithContext == null )
      return null;

    return featureviewWithContext.getContext();
  }

  @Override
  public ITranslator getTranslator( )
  {
    return m_translator;
  }
}