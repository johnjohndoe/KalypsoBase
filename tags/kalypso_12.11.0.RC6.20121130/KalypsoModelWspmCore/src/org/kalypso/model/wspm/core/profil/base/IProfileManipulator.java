package org.kalypso.model.wspm.core.profil.base;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.model.wspm.core.profil.IProfile;
import org.kalypso.model.wspm.core.profil.IProfileChange;

public interface IProfileManipulator
{
  Pair<IProfileChange[], IStatus> performProfileManipulation( IProfile profile, IProgressMonitor monitor ) throws CoreException;
}