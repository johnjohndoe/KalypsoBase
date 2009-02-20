package org.apache.commons.vfs;

import org.apache.commons.vfs.provider.FileProvider;

public interface VFSProviderExtension {
	public FileProvider getProvider();

	public void init(final FileSystemManager manager);
}
