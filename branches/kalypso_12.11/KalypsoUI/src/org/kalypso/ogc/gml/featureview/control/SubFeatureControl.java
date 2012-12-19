package org.kalypso.ogc.gml.featureview.control;

import javax.xml.namespace.QName;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ImageHyperlink;
import org.kalypso.commons.command.ICommand;
import org.kalypso.contribs.eclipse.jface.action.ActionHyperlink;
import org.kalypso.gmlschema.property.IPropertyType;
import org.kalypso.gmlschema.property.relation.IRelationType;
import org.kalypso.ogc.gml.command.DeleteFeatureCommand;
import org.kalypso.ogc.gml.featureview.IFeatureChangeListener;
import org.kalypso.ogc.gml.featureview.maker.IFeatureviewFactory;
import org.kalypso.ogc.gml.selection.IFeatureSelectionManager;
import org.kalypsodeegree.model.feature.Feature;
import org.kalypsodeegree_impl.model.feature.FeatureHelper;

/**
 * @author Gernot Belger
 */
public class SubFeatureControl extends AbstractFeatureControl
{
  private IFeatureControl m_fc;

  private QName m_selector;

  private Composite m_container;

  private final IFeatureComposite m_parentFeatureComposite;

  private ImageHyperlink m_deleteButton;

  private final boolean m_deleteEnabled;

  public SubFeatureControl( final IPropertyType ftp, final IFeatureComposite parentFeatureComposite, final String selector, final boolean deleteEnabled )
  {
    super( ftp );

    m_parentFeatureComposite = parentFeatureComposite;
    m_deleteEnabled = deleteEnabled;

    if( selector != null && ftp != null )
      // TODO: NO! Please ALWAYS use full qnames inside the .gft!! The namespace of the property is not always the
      // namespace of the feature!!
      // You can even define the selector attribute to be of type QNAME! You dont have to parse anything yourself!
      // BEST HERE: use gml-xpath like the enabledOperation attribute of the ControlType.
      // Search for 'evaluateOperation' in the FeatureComposite class for an example on how to use it.

      // TODO: check if the selector is needed at all
      // See the findFeatureToSet method: probably just setting the link-property to the property-attribute in the gft
      // should be
      // enough!

      m_selector = new QName( ftp.getQName().getNamespaceURI(), selector );
    else
      m_selector = null;
  }

  @Override
  public Control createControl( final FormToolkit toolkit, final Composite parent, final int style )
  {
    if( m_container == null )
    {
      // on first call to createControl the container is set up
      m_container = new Composite( parent, style );
      // FIXME: actually we'd like to use a FillLayout, but there are still buggy Feature-Controls out
      // there that set their own layoutData to grid-data....
      GridLayoutFactory.fillDefaults().applyTo( m_container );

      applyToolkit( toolkit, m_container );
    }

    final IPropertyType ftp = getFeatureTypeProperty();
    final Feature featureToSet = findFeatuereToSet();

    try
    {

      /* create the control */
      if( featureToSet == null )
      {
        // TODO: If selector is present, just create an empty control

        m_fc = new ButtonFeatureControl( getFeature(), ftp );
      }
      else
      {
        final IFeatureSelectionManager selectionManager = m_parentFeatureComposite.getSelectionManager();
        final IFeatureviewFactory featureviewFactory = m_parentFeatureComposite.getFeatureviewFactory();
        final FormToolkit formToolkit = m_parentFeatureComposite.getFormToolkit();
        final boolean showOk = m_parentFeatureComposite.isShowOk();

        final FeatureComposite fc = new FeatureComposite( featureToSet, selectionManager, featureviewFactory );
        fc.setFormToolkit( formToolkit );
        fc.setShowOk( showOk );

        m_fc = fc;
      }
    }
    catch( final Throwable t )
    {
      // TODO: Create text feature control with error message!
    }

    m_fc.addChangeListener( new IFeatureChangeListener()
    {
      @Override
      public void featureChanged( final ICommand changeCommand )
      {
        fireFeatureChange( changeCommand );
      }

      @Override
      public void openFeatureRequested( final Feature featureToOpen, final IPropertyType ftpToOpen )
      {
        fireOpenFeatureRequested( featureToOpen, ftpToOpen );
      }
    } );

    m_fc.createControl( toolkit, m_container, SWT.NONE );

    if( m_deleteEnabled && featureToSet != null && (ftp.isNillable() || ftp.getMinOccurs() == 0) )
    {
      final Action deleteFeatureAction = new DeleteSubFeatureAction( this, featureToSet );
      m_deleteButton = ActionHyperlink.createHyperlink( toolkit, m_container, SWT.None, deleteFeatureAction );
    }

    // FIXME we should set the layout here, but the FeatureComposite does it itself, which it shouldn't

    return m_container;
  }

  private Feature findFeatuereToSet( )
  {
    final Feature feature = getFeature();
    final IPropertyType ftp = getFeatureTypeProperty();
    final IRelationType rt = (IRelationType)ftp;

    // find feature to set to the sub-FeatureControl
    final Feature featureToSet;

    if( m_selector == null )
    {
      Assert.isTrue( !rt.isList() );
      final Object property = feature.getProperty( rt );
      featureToSet = FeatureHelper.resolveLinkedFeature( feature.getWorkspace(), property );
    }
    else
    {
      final Object link = feature.getProperty( m_selector );
      featureToSet = FeatureHelper.resolveLinkedFeature( feature.getWorkspace(), link );
    }
    return featureToSet;
  }

  @Override
  public void dispose( )
  {
    m_container.dispose();
  }

  @Override
  public void updateControl( )
  {
    final Feature findFeatureToSet = findFeatuereToSet();
    final Feature currentFeature = m_fc.getFeature();

    if( !ObjectUtils.equals( findFeatureToSet, currentFeature ) )
    {
      // re-create control
      m_fc.dispose();

      if( m_deleteButton != null )
      {
        m_deleteButton.dispose();
        m_deleteButton = null;
      }

      createControl( null, m_container, m_container.getStyle() );
      m_container.layout();
    }

    // Is updateControl always necessary?
    m_fc.updateControl();
  }

  @Override
  public boolean isValid( )
  {
    return m_fc.isValid();
  }

  @Override
  public void addModifyListener( final ModifyListener l )
  {
    m_fc.addModifyListener( l );
  }

  @Override
  public void removeModifyListener( final ModifyListener l )
  {
    m_fc.removeModifyListener( l );
  }

  /** Returns the used feature control. */
  public IFeatureControl getFeatureControl( )
  {
    return m_fc;
  }

  void deleteSubFeature( )
  {
    final Feature feature = findFeatuereToSet();
    if( feature == null )
      return;

    final DeleteFeatureCommand command = new DeleteFeatureCommand( feature );
    fireFeatureChange( command );
  }
}