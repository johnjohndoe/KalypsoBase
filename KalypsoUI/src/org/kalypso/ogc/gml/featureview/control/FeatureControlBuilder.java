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
package org.kalypso.ogc.gml.featureview.control;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ColorRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.kalypso.commons.i18n.I10nString;
import org.kalypso.commons.i18n.ITranslator;
import org.kalypso.contribs.eclipse.core.runtime.StatusUtilities;
import org.kalypso.contribs.eclipse.swt.ColorUtilities;
import org.kalypso.contribs.eclipse.swt.SWTUtilities;
import org.kalypso.contribs.eclipse.ui.forms.ToolkitUtils;
import org.kalypso.gmlschema.annotation.AnnotationUtilities;
import org.kalypso.gmlschema.annotation.IAnnotation;
import org.kalypso.gmlschema.feature.IFeatureType;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.ogc.gml.featureview.control.composite.CompositeFeatureControlFactory;
import org.kalypso.ogc.gml.featureview.control.composite.IFeatureCompositionControl;
import org.kalypso.ogc.gml.featureview.control.composite.IFeatureCompositionControlFactory;
import org.kalypso.ogc.gml.featureview.control.composite.SectionCompositionFactory;
import org.kalypso.ogc.gml.featureview.control.composite.TablFolderCompositionFactory;
import org.kalypso.template.featureview.Button;
import org.kalypso.template.featureview.Checkbox;
import org.kalypso.template.featureview.ColorLabelType;
import org.kalypso.template.featureview.Combo;
import org.kalypso.template.featureview.CommandHyperlink;
import org.kalypso.template.featureview.CompositeType;
import org.kalypso.template.featureview.ControlType;
import org.kalypso.template.featureview.DynamicTabFolder;
import org.kalypso.template.featureview.Extensioncontrol;
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
import org.kalypso.ui.internal.i18n.Messages;
import org.kalypsodeegree.filterencoding.FilterConstructionException;
import org.kalypsodeegree.filterencoding.FilterEvaluationException;
import org.kalypsodeegree.filterencoding.Operation;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree.xml.XMLTools;
import org.kalypsodeegree_impl.filterencoding.AbstractOperation;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Wrapper around the ControlType that extracts all basic stuff from the {@link org.kalypso.template.featureview.ControlType} definition.
 * 
 * @author Gernot Belger
 */
public class FeatureControlBuilder
{
  private static final LayoutDataType NULL_LAYOUT_DATA_TYPE = new LayoutDataType();

  /** Used for the compatibility-hack. Is it possible to get this from the binding classes? */
  private static String FEATUREVIEW_NAMESPACE = "featureview.template.kalypso.org"; //$NON-NLS-1$

  private final ControlType m_controlType;

  private Feature m_feature;

  private final ITranslator m_translator;

  private final FeatureComposite m_fc;

  private Control m_control;

  private IFeatureControl m_featureControl;

  public FeatureControlBuilder( final FeatureComposite fc, final ControlType controlType, final Feature feature, final ITranslator translator )
  {
    m_fc = fc;
    m_controlType = controlType;
    m_feature = feature;
    m_translator = translator;
  }

  public Control create( final FormToolkit toolkit, final Composite parent, final int defaultStyle )
  {
    final IAnnotation annotation = getAnnotation();

    m_control = createControl( toolkit, parent, defaultStyle, annotation );

    /* If a toolkit is set, use it. */
    applyToolkit( toolkit, m_control );

    initControl( annotation );

    updateLayoutData();

    return m_control;
  }

  private Control createControl( final FormToolkit toolkit, final Composite parent, final int defaultStyle, final IAnnotation annotation )
  {
    final int style = getStyle( defaultStyle );

    // FIXME: think the composites as IFeaturecontrols, should not be a special case...
    final IFeatureCompositionControlFactory compositionFactory = createCompositionFactory();
    if( compositionFactory != null )
    {
      final IFeatureCompositionControl composite = compositionFactory.createControl( m_fc, annotation, m_translator );
      return composite.createControl( toolkit, parent, style );
    }

    m_featureControl = createFeatureControl( toolkit, parent, annotation );
    final Control control = m_featureControl.createControl( toolkit, parent, style );

    m_featureControl.addChangeListener( m_fc );
    m_featureControl.addModifyListener( m_fc );

    return control;
  }

  private LayoutDataType getLayoutData( )
  {
    final JAXBElement< ? extends LayoutDataType> jaxLayoutData = m_controlType.getLayoutData();
    if( jaxLayoutData == null )
      return NULL_LAYOUT_DATA_TYPE;

    return jaxLayoutData.getValue();
  }

  private void initControl( final IAnnotation annotation )
  {
    final String tooltip = getTooltip( annotation );
    m_control.setToolTipText( tooltip );

    final Color background = getBackgroundColor();
    if( background != null )
      m_control.setBackground( background );
  }

  private void applyToolkit( final FormToolkit toolkit, final Control control )
  {
    if( !m_fc.hasToolkit() )
      return;

    if( toolkit == null )
      return;

    if( control instanceof Composite )
    {
      final Composite panel = (Composite)control;

      if( panel instanceof Section )
        return;

      toolkit.adapt( panel );

      final Control[] children = panel.getChildren();
      for( final Control child : children )
        applyToolkit( toolkit, child );
    }
    else
      toolkit.adapt( control, true, true );
  }

  private Color getBackgroundColor( )
  {
    final String htmlColor = getBackgroundHtml();
    if( StringUtils.isBlank( htmlColor ) )
      return null;

    final ColorRegistry colorRegistry = JFaceResources.getColorRegistry();
    final Color cachedColor = colorRegistry.get( htmlColor );
    if( cachedColor != null )
      return cachedColor;

    final RGB rgb = ColorUtilities.toRGBFromHTML( htmlColor );
    if( rgb == null )
      return null;

    colorRegistry.put( htmlColor, rgb );
    return colorRegistry.get( htmlColor );
  }

  private String getBackgroundHtml( )
  {
    final Object backgroundColor = m_controlType.getBackgroundColor();

    if( backgroundColor instanceof String )
      return ((String)backgroundColor).trim();

    if( backgroundColor instanceof Node )
      return XMLTools.getStringValue( (Node)backgroundColor );

    return null;
  }

  private String getTooltip( final IAnnotation annotation )
  {
    // REMARK: an explicitly set tooltip always wins
    final String tooltipControlText = m_controlType.getTooltip();

    final String tooltipText = AnnotationUtilities.getAnnotation( annotation, tooltipControlText, IAnnotation.ANNO_TOOLTIP );
    return new I10nString( tooltipText, m_translator ).getValue();
  }

  private int getStyle( final int defaultStyle )
  {
    final String controlStyle = m_controlType.getStyle();
    if( controlStyle == null )
      return defaultStyle;

    return SWTUtilities.createStyleFromString( controlStyle );
  }

  private IAnnotation getAnnotation( )
  {
    final IPropertyType propertyType = getProperty();

    if( propertyType == null )
      return null;

    return propertyType.getAnnotation();
  }

  private IPropertyType getProperty( )
  {
    final IFeatureType featureType = getFeatureType();
    if( featureType == null )
      return null;

    final QName propertyName = findPropertyName();
    if( propertyName == null )
      return null;

    return getPropertyTypeForQName( featureType, propertyName );
  }

  private IFeatureType getFeatureType( )
  {
    if( m_feature == null )
      return null;

    return m_feature.getFeatureType();
  }

  private QName findPropertyName( )
  {
    if( m_controlType instanceof PropertyControlType )
      return ((PropertyControlType)m_controlType).getProperty();

    if( m_controlType instanceof CompositeType )
      return ((CompositeType)m_controlType).getProperty();

    return null;
  }

  /**
   * Special method to retrieve a property from a feature for a special qname. Neeeded to have backward compability for
   * the feature-template. Before, the propertyName was given as xs:string (only the local part), now it is a xs:QName.
   * So old entries are interpreted against the namespace of the featuretemplate.
   */
  private IPropertyType getPropertyTypeForQName( final IFeatureType featureType, final QName property )
  {
    if( property == null )
      return null;

    final IPropertyType propertyType = featureType.getProperty( property );
    if( propertyType != null )
      return propertyType;

    if( property.getNamespaceURI().equals( FEATUREVIEW_NAMESPACE ) )
    {
      final String localPart = property.getLocalPart();
      return featureType.getProperty( localPart );
    }

    return null;
  }

  private IFeatureCompositionControlFactory createCompositionFactory( )
  {
    if( m_controlType instanceof CompositeType )
      return new CompositeFeatureControlFactory( (CompositeType)m_controlType );

    if( m_controlType instanceof TabFolder )
      return new TablFolderCompositionFactory( (TabFolder)m_controlType );

    if( m_controlType instanceof org.kalypso.template.featureview.Section )
      return new SectionCompositionFactory( (org.kalypso.template.featureview.Section)m_controlType );

    return null;
  }

  // TODO: use extension point instead?
  private IFeatureControlFactory createControlFactory( final FormToolkit defaultToolkit, final Composite parent )
  {
    final FormToolkit toolkit = createOrGetToolkit( defaultToolkit, parent );

    if( m_controlType instanceof LabelType )
      return new LabelFeatureControlFactory();

    if( m_controlType instanceof Extensioncontrol )
      return new ExtensionFeatureControlFactory( toolkit );

    if( m_controlType instanceof Text )
      return new TextFeatureControlFactory();

    if( m_controlType instanceof DynamicTabFolder )
      return new DynamicTabFolderFeatureControlFactory();

    if( m_controlType instanceof Button )
      return new ButtonFeatureControlFactory();

    if( m_controlType instanceof Image )
      return new ImageFeatureControlFactory();

    if( m_controlType instanceof TupleResult )
      return new TupleResultFeatureControlFactory();

    if( m_controlType instanceof SubcompositeType )
      return new SubFeatureControlFactory();

    if( m_controlType instanceof Table )
      return new TableFeatureControlFactory();

    if( m_controlType instanceof ValidatorLabelType )
      return new ValidatorLabelTypeFactory();

    if( m_controlType instanceof Spinner )
      return new SpinnerFeatureControlFactory();

    if( m_controlType instanceof ColorLabelType )
      return new ColorFeatureControlFactory();

    if( m_controlType instanceof Radiobutton )
      return new RadioFeatureControlFactory();

    if( m_controlType instanceof Checkbox )
      return new CheckboxFeatureControlFactory();

    if( m_controlType instanceof Combo )
      return new ComboFeatureControlFactory();

    if( m_controlType instanceof GeometryLabelType )
      return new GeometryFeatureControlFactory();

    if( m_controlType instanceof CommandHyperlink )
      return new CommandHyperlinkFeatureControlFactory();

    return null;
  }

  /**
   * Returns a toolkit. Either the one we got already, or a new one that will be disposed if the given control is
   * disposed
   */
  private FormToolkit createOrGetToolkit( final FormToolkit defaultToolkit, final Control control )
  {
    if( defaultToolkit == null )
    {
      final FormToolkit toolkit = ToolkitUtils.createToolkit( control );
      toolkit.setBackground( control.getBackground() );

      return toolkit;
    }
    else
      return defaultToolkit;
  }

  private IFeatureControl createFeatureControl( final FormToolkit toolkit, final Composite parent, final IAnnotation annotation )
  {
    final IFeatureControlFactory controlFactory = createControlFactory( toolkit, parent );
    if( controlFactory == null )
    {
      final String msg = Messages.getString( "org.kalypso.ogc.gml.featureview.control.FeatureComposite.create" ); //$NON-NLS-1$
      final IStatus status = new Status( IStatus.ERROR, KalypsoGisPlugin.getId(), msg );
      return new StatusFeatureControl( status );
    }

    final IPropertyType propertyType = getProperty();

    return controlFactory.createFeatureControl( m_fc, m_feature, propertyType, m_controlType, annotation );
  }

  public void updateControl( )
  {
    if( m_featureControl != null )
      m_featureControl.updateControl();

    updateLayoutData();
  }

  public IFeatureControl getFeatureControl( )
  {
    return m_featureControl;
  }

  public void setFeature( final Feature feature )
  {
    m_feature = feature;

    if( m_featureControl != null )
      m_featureControl.setFeature( feature );
  }

  public void dispose( )
  {
    if( m_featureControl != null )
      m_featureControl.dispose();
    // TODO: really necesary? parent is always disposed
    // m_control.dispose();
  }

  private void updateLayoutData( )
  {
    if( m_control.isDisposed() )
      return;

    /* Update the layout data */
    final LayoutDataType layoutDataType = getLayoutData();

    if( layoutDataType instanceof GridDataType )
    {
      final GridDataType gridDataType = (GridDataType)layoutDataType;
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
      gridData.exclude = evaluateOperation( m_feature, excludeType, false );

      m_control.setLayoutData( gridData );
    }
    else if( layoutDataType == NULL_LAYOUT_DATA_TYPE )
      m_control.setLayoutData( new GridData() );

    // REMARK: Special case for direct children of Tab-Folders. Setting the visibility here
    // breaks the tab folder behavior. We assume, that the visibility of a
    // tab folder item is never changed depending on a value of a feature.
    if( !(m_control.getParent() instanceof org.eclipse.swt.widgets.TabFolder) )
    {
      final Object visibleOperation = m_controlType.getVisibleOperation();
      final boolean visible = evaluateOperation( m_feature, visibleOperation, m_controlType.isVisible() );
      if( m_control.getVisible() != visible )
        m_control.setVisible( visible );
    }

    final Object enabledOperation = m_controlType.getEnabledOperation();
    final boolean enabled = evaluateOperation( m_feature, enabledOperation, m_controlType.isEnabled() );
    if( m_control.getEnabled() != enabled )
      m_control.setEnabled( enabled );
  }

  private boolean evaluateOperation( final Feature feature, final Object operationElement, final boolean defaultValue )
  {
    try
    {
      if( operationElement instanceof String )
        return Boolean.parseBoolean( (String)operationElement );
      else if( operationElement instanceof Element )
      {
        KalypsoUIDebug.FEATUREVIEW_OPERATIONS.printf( String.format( "Found operation: %s%nfor feature: %s%n", operationElement, feature ) ); //$NON-NLS-1$

        final Element element = (Element)operationElement;
        final NodeList childNodes = element.getChildNodes();
        for( int i = 0; i < childNodes.getLength(); i++ )
        {
          final Node item = childNodes.item( i );
          if( item instanceof Element )
          {
            final Operation operation = AbstractOperation.buildFromDOM( (Element)item );
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

}