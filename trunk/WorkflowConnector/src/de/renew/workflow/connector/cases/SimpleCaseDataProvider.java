package de.renew.workflow.connector.cases;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;

/**
 * @author Stefan Kurzbach
 */
public class SimpleCaseDataProvider implements ICaseDataProvider<Object>
{
  /**
   * @see de.renew.workflow.cases.ICaseDataProvider#setCurrent(org.eclipse.core.resources.IFolder)
   */
  @Override
  public void setCurrent( final ICase scenario )
  {
    try
    {
      if( scenario != null )
      {
        scenario.getFolder().refreshLocal( IFolder.DEPTH_INFINITE, new NullProgressMonitor() );
      }
    }
    catch( final Throwable th )
    {
      th.printStackTrace();
    }
  }

  /**
   * @see de.renew.workflow.cases.ICaseDataProvider#reloadModel()
   */
  @Override
  public void reloadModel( )
  {

  }

  /**
   * @see de.renew.workflow.cases.ICaseDataProvider#getModel(java.lang.Class)
   */
  @Override
  public <D extends Object> D getModel( final Class<D> modelClass )
  {
    return null;
  }

  @Override
  public boolean isDirty( )
  {
    return false;
  }

  /**
   * @see de.renew.workflow.cases.ICaseDataProvider#isDirty(java.lang.Class)
   */
  @Override
  public boolean isDirty( final Class< ? extends Object> modelClass )
  {
    return false;
  }

  /**
   * @see de.renew.workflow.cases.ICaseDataProvider#saveModel(java.lang.Class,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void saveModel( final Class< ? extends Object> modelClass, final IProgressMonitor monitor )
  {
  }

  /**
   * @see de.renew.workflow.cases.ICaseDataProvider#saveModel(org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void saveModel( final IProgressMonitor monitor )
  {

  }

  /**
   * @see de.renew.workflow.connector.cases.ICaseDataProvider#getModel(java.lang.String, java.lang.Class)
   */
  @Override
  public <D> D getModel( final String id, final Class<D> modelClass )
  {
    return null;
  }

  /**
   * @see de.renew.workflow.connector.cases.ICaseDataProvider#isDirty(java.lang.String)
   */
  @Override
  public boolean isDirty( final String id )
  {
    return false;
  }

  /**
   * @see de.renew.workflow.connector.cases.ICaseDataProvider#saveModel(java.lang.String,
   *      org.eclipse.core.runtime.IProgressMonitor)
   */
  @Override
  public void saveModel( final String id, final IProgressMonitor monitor )
  {
  }
}
