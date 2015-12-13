/*
 * DBeaver - Universal Database Manager
 * Copyright (C) 2010-2015 Serge Rieder (serge@jkiss.org)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License (version 2)
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package org.jkiss.dbeaver.ui.resources;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.*;
import org.jkiss.code.Nullable;
import org.jkiss.dbeaver.DBeaverPreferences;
import org.jkiss.dbeaver.Log;
import org.jkiss.dbeaver.core.DBeaverCore;
import org.jkiss.dbeaver.model.DBPDataSourceContainer;
import org.jkiss.dbeaver.ui.editors.sql.SQLEditorInput;
import org.jkiss.dbeaver.utils.ContentUtils;
import org.jkiss.utils.CommonUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ResourceUtils
 */
public class ResourceUtils {

    static final Log log = Log.getLog(ResourceUtils.class);

    public static final String SCRIPT_FILE_EXTENSION = "sql"; //$NON-NLS-1$

    public static class ResourceInfo {
        private final IResource resource;
        private final File localFile;
        private final List<ResourceInfo> children;

        public IResource getResource() {
            return resource;
        }

        public File getLocalFile() {
            return localFile;
        }

        public List<ResourceInfo> getChildren() {
            return children;
        }

        public ResourceInfo(IFile file) {
            resource = file;
            localFile = file.getLocation().toFile();
            children = null;
        }
        public ResourceInfo(IFolder folder) {
            resource = folder;
            localFile = folder.getLocation().toFile();
            children = new ArrayList<>();
        }
    }

    public static IFolder getScriptsFolder(IProject project, boolean forceCreate) throws CoreException
    {
    	if (project == null) {
    		IStatus status = new Status(IStatus.ERROR, DBeaverCore.getCorePluginID(), "No active project to locate Script Folder");
			throw new CoreException(status);
		}
        final IFolder scriptsFolder = DBeaverCore.getInstance().getProjectRegistry().getResourceDefaultRoot(project, ScriptsHandlerImpl.class);
        if (!scriptsFolder.exists() && forceCreate) {
            scriptsFolder.create(true, true, new NullProgressMonitor());
        }
        return scriptsFolder;
    }

    @Nullable
    public static ResourceInfo findRecentScript(IProject project, @Nullable DBPDataSourceContainer container) throws CoreException
    {
        List<ResourceInfo> scripts = new ArrayList<>();
        findScriptList(getScriptsFolder(project, false), container, scripts);
        long recentTimestamp = 0l;
        ResourceInfo recentFile = null;
        for (ResourceInfo file : scripts) {
            if (file.localFile.lastModified() > recentTimestamp) {
                recentTimestamp = file.localFile.lastModified();
                recentFile = file;
            }
        }
        return recentFile;
    }

    private static void findScriptList(IFolder folder, @Nullable DBPDataSourceContainer container, List<ResourceInfo> result)
    {
        try {
            for (IResource resource : folder.members()) {
                if (resource instanceof IFile && SCRIPT_FILE_EXTENSION.equals(resource.getFileExtension())) {
                    if (SQLEditorInput.getScriptDataSource((IFile) resource) == container) {
                        result.add(new ResourceInfo((IFile) resource));
                    }
                } else if (resource instanceof IFolder) {
                    findScriptList((IFolder) resource, container, result);
                }

            }
        } catch (CoreException e) {
            log.debug(e);
        }
    }

    public static List<ResourceInfo> findScriptTree(IFolder folder, @Nullable DBPDataSourceContainer container)
    {
        List<ResourceInfo> result = new ArrayList<>();
        try {
            for (IResource resource : folder.members()) {
                if (resource instanceof IFile && SCRIPT_FILE_EXTENSION.equals(resource.getFileExtension())) {
                    if (SQLEditorInput.getScriptDataSource((IFile) resource) == container) {
                        result.add(new ResourceInfo((IFile) resource));
                    }
                } else if (resource instanceof IFolder) {
                    final ResourceInfo folderInfo = new ResourceInfo((IFolder) resource);
                    if (findChildScripts(folderInfo, container)) {
                        result.add(folderInfo);
                    }
                }

            }
        } catch (CoreException e) {
            log.debug(e);
        }
        return result;
    }

    private static boolean findChildScripts(ResourceInfo folder, @Nullable DBPDataSourceContainer container) throws CoreException {
        boolean hasScripts = false;
        for (IResource resource : ((IFolder)folder.resource).members()) {
            if (resource instanceof IFile && SCRIPT_FILE_EXTENSION.equals(resource.getFileExtension())) {
                if (SQLEditorInput.getScriptDataSource((IFile) resource) == container) {
                    folder.children.add(new ResourceInfo((IFile) resource));
                    hasScripts = true;
                }
            } else if (resource instanceof IFolder) {
                final ResourceInfo folderInfo = new ResourceInfo((IFolder) resource);
                if (findChildScripts(folderInfo, container)) {
                    folder.children.add(folderInfo);
                    hasScripts = true;
                }
            }

        }
        return hasScripts;
    }

    public static IFile createNewScript(IProject project, @Nullable IFolder folder, @Nullable DBPDataSourceContainer dataSourceContainer) throws CoreException
    {
        final IProgressMonitor progressMonitor = new NullProgressMonitor();

        // Get folder
        IFolder scriptsFolder = folder;
        if (scriptsFolder == null) {
            scriptsFolder = getScriptsFolder(project, true);
            if (dataSourceContainer != null && dataSourceContainer.getPreferenceStore().getBoolean(DBeaverPreferences.SCRIPT_AUTO_FOLDERS)) {
                IFolder dbFolder = scriptsFolder.getFolder(CommonUtils.escapeFileName(dataSourceContainer.getName()));
                if (dbFolder != null) {
                    if (!dbFolder.exists()) {
                        dbFolder.create(true, true, progressMonitor);
                    }
                    scriptsFolder = dbFolder;
                }
            }
        }

        // Make new script file
        IFile tempFile = ContentUtils.getUniqueFile(scriptsFolder, "Script", SCRIPT_FILE_EXTENSION);
        tempFile.create(new ByteArrayInputStream(new byte[]{}), true, progressMonitor);

        // Save ds container reference
        if (dataSourceContainer != null) {
            SQLEditorInput.setScriptDataSource(tempFile, dataSourceContainer);
        }

        return tempFile;
    }
}