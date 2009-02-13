package org.kalypso.services.processing;

import java.util.List;

import net.opengeospatial.ows.CapabilitiesBaseType;
import net.opengeospatial.wps.Capabilities;
import net.opengeospatial.wps.ProcessBriefType;

import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.kalypso.services.IOGCWebService;
import org.kalypso.services.IServiceEntry;
import org.kalypso.services.ServiceEntryAdapterFactory;

public class WPSAdapterFactory extends ServiceEntryAdapterFactory implements IAdapterFactory
{

  private final ITreeContentProvider m_contentProvider = new WPSTreeContentProvider();

  private final ILabelProvider m_labelProvider = new WPSLabelProvider();

  /**
   * @see org.kalypso.services.ServiceEntryAdapterFactory#getAdapter(java.lang.Object, java.lang.Class)
   */
  @Override
  public Object getAdapter( final Object adaptableObject, final Class adapterType )
  {
    final Object adapter = super.getAdapter( adaptableObject, adapterType );
    if( adapter != null )
    {
      return adapter;
    }
    else if( ITreeContentProvider.class.equals( adapterType ) )
    {
      return m_contentProvider;
    }
    else if( ILabelProvider.class.equals( adapterType ) )
    {
      return m_labelProvider;
    }
    return null;
  }

  /**
   * @see IAdapterFactory#getAdapterList()
   */
  @Override
  public Class[] getAdapterList( )
  {
    return new Class[] { ITreeContentProvider.class, ILabelProvider.class, IServiceEntry.class };
  }

  /**
   * @see org.kalypso.services.ServiceEntryAdapterFactory#getServiceEntry(org.kalypso.services.IOGCWebService)
   */
  @Override
  protected IServiceEntry getServiceEntry( final IOGCWebService service )
  {
    return new WebProcessingServiceEntry( (IWebProcessingService) service );
  }

  /**
   * @see org.kalypso.services.ServiceEntryAdapterFactory#getServiceEntry(net.opengeospatial.ows.CapabilitiesBaseType)
   */
  @Override
  protected IServiceEntry getServiceEntry( final CapabilitiesBaseType capabilities )
  {
    return new WebProcessingServiceEntry(capabilities);
  }

  private final class WPSLabelProvider extends LabelProvider
  {
    private static final String WPS_SUFFIX = " (WPS)";

    WPSLabelProvider( )
    {
    }

    /**
     * @see org.eclipse.jface.viewers.LabelProvider#getText(java.lang.Object)
     */
    @Override
    public String getText( final Object element )
    {
      if( element instanceof IServiceEntry )
      {
        final IServiceEntry serviceEntry = (IServiceEntry) element;
        return serviceEntry.getCapabilities().getServiceIdentification().getTitle() + WPS_SUFFIX;
      }
      else if( element instanceof ProcessBriefType )
      {
        final ProcessBriefType processBrief = (ProcessBriefType) element;
        return processBrief.getTitle();
      }
      return element.toString();
    }
  }

  private final class WPSTreeContentProvider implements ITreeContentProvider
  {
    WPSTreeContentProvider( )
    {
    }

    public Object[] getChildren( final Object parentElement )
    {
      if( parentElement instanceof IServiceEntry )
      {
        final IServiceEntry serviceEntry = (IServiceEntry) parentElement;
        final Capabilities capabilities = (Capabilities) serviceEntry.getCapabilities();
        final List<ProcessBriefType> processList = capabilities.getProcessOfferings().getProcess();
        return processList.toArray( new ProcessBriefType[processList.size()] );
      }
      return new Object[0];
    }

    public Object getParent( final Object element )
    {
      return null;
    }

    public boolean hasChildren( final Object element )
    {
      return element instanceof IWebProcessingService;
    }

    public Object[] getElements( final Object inputElement )
    {
      return getChildren( inputElement );
    }

    public void dispose( )
    {
    }

    public void inputChanged( final Viewer viewer, final Object oldInput, final Object newInput )
    {
    }
  }

}
