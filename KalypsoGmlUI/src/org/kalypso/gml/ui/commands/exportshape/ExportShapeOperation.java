package org.kalypso.gml.ui.commands.exportshape;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.kalypso.contribs.eclipse.jface.operation.ICoreRunnableWithProgress;
import org.kalypso.gml.ui.i18n.Messages;
import org.kalypso.shape.IShapeData;
import org.kalypso.shape.ShapeWriter;
import org.kalypso.shape.dbf.DBaseException;
import org.kalypso.shape.deegree.IShapeDataFactory;

/**
 * @author Gernot Belger
 */
public final class ExportShapeOperation implements ICoreRunnableWithProgress
{
  private final String m_shapeFileBase;

  private final IShapeDataFactory m_shapeDataFactory;

  private final boolean m_writePrj;

  public ExportShapeOperation( final String shapeFileBase, final IShapeDataFactory shapeDataFactory, final boolean writePrj )
  {
    m_shapeFileBase = shapeFileBase;
    m_shapeDataFactory = shapeDataFactory;
    m_writePrj = writePrj;
  }

  @Override
  public IStatus execute( final IProgressMonitor monitor ) throws CoreException, InvocationTargetException
  {
    monitor.beginTask( Messages.getString("ExportShapeOperation_0"), 100 ); //$NON-NLS-1$
    try
    {
      final IShapeData dataProvider = m_shapeDataFactory.createData();

      final ShapeWriter shapeWriter = new ShapeWriter( dataProvider );
      shapeWriter.write( m_shapeFileBase, new SubProgressMonitor( monitor, 90 ) );

      if( m_writePrj )
        shapeWriter.writePrj( m_shapeFileBase, new SubProgressMonitor( monitor, 10 ) );
      else
        monitor.worked( 10 );
    }
    catch( final CoreException e )
    {
      throw e;
    }
    catch( final DBaseException e )
    {
      throw new InvocationTargetException( e, "Failed to create default shape provider" ); //$NON-NLS-1$
    }
    catch( final Exception e )
    {
      throw new InvocationTargetException( e );
    }
    finally
    {
      monitor.done();
    }

    return Status.OK_STATUS;
  }
}