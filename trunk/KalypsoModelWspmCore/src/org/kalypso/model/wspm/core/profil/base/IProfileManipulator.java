package org.kalypso.model.wspm.core.profil.base;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.kalypso.model.wspm.core.profil.IProfil;
import org.kalypso.model.wspm.core.profil.IProfilChange;

public interface IProfileManipulator
{
  Pair<IProfilChange[], IStatus> performProfileManipulation( IProfil profile, IProgressMonitor monitor ) throws CoreException;
}