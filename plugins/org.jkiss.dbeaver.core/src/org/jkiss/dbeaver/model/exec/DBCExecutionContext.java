/*
 * Copyright (C) 2010-2013 Serge Rieder
 * serge@jkiss.org
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jkiss.dbeaver.model.exec;

import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.model.DBPCloseableObject;
import org.jkiss.dbeaver.model.DBPDataSource;
import org.jkiss.dbeaver.model.DBPDataSourceInfo;
import org.jkiss.dbeaver.model.DBPObject;
import org.jkiss.dbeaver.model.runtime.DBRProgressMonitor;
import org.jkiss.dbeaver.model.struct.DBSDataSourceContainer;

/**
 * Execution context.
 * Provides access to execution sessions.
 * Usually contains some kind of physical database connection inside
 */
public interface DBCExecutionContext extends DBPObject,DBPCloseableObject
{
    String getContextName();
    /**
     * Owner datasource
     */
    DBPDataSource getDataSource();

    /**
     * Checks this context is really connected to remote database.
     * Usually DBSDataSourceContainer.getDataSource() returns datasource only if datasource is connected.
     * But in some cases (e.g. connection invalidation) datasource remains disconnected for some period of time.
     * @return true if underlying connection is alive.
     */
    boolean isConnected();

    /**
     * Opens new session
     * @param monitor progress monitor
     * @param purpose context purpose
     * @param task task description
     * @return execution context
     */
    DBCSession openSession(DBRProgressMonitor monitor, DBCExecutionPurpose purpose, String task);

    /**
     * Checks context is alive and reconnects if needed.
     *
     * @throws org.jkiss.dbeaver.DBException on any error
     * @param monitor progress monitor
     */
    void invalidateContext(DBRProgressMonitor monitor) throws DBException;

}