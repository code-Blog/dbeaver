/*
 * Copyright (C) 2010-2013 Serge Rieder serge@jkiss.org
 * Copyright (C) 2011-2012 Eugene Fradkin eugene.fradkin@gmail.com
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
package org.jkiss.dbeaver.ext.oracle.tools.maintenance;

import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.IWorkbenchWindow;
import org.jkiss.dbeaver.DBException;
import org.jkiss.dbeaver.ext.oracle.model.OracleTable;
import org.jkiss.dbeaver.ext.oracle.model.OracleTableIndex;
import org.jkiss.dbeaver.model.DBUtils;
import org.jkiss.dbeaver.model.struct.DBSObject;
import org.jkiss.dbeaver.tools.IExternalTool;
import org.jkiss.dbeaver.ui.UIUtils;

import java.util.Collection;
import java.util.List;

/**
 * Gather statistics
 */
public class OracleToolGatherStatistics implements IExternalTool
{
    @Override
    public void execute(IWorkbenchWindow window, IWorkbenchPart activePart, Collection<DBSObject> objects) throws DBException
    {
        if (!objects.isEmpty()) {
            SQLDialog dialog = new SQLDialog(activePart.getSite(), objects);
            dialog.open();
        }
    }

    static class SQLDialog extends OracleMaintenanceDialog<DBSObject> {

        private Spinner samplePercent;

        public SQLDialog(IWorkbenchPartSite partSite, Collection<DBSObject> selectedTables)
        {
            super(partSite, "Gather statistics", selectedTables);
        }

        @Override
        protected void generateObjectCommand(List<String> lines, DBSObject object) {
            if (object instanceof OracleTable) {
                OracleTable table = (OracleTable)object;
                String sql = "BEGIN \n" +
                    " DBMS_STATS.GATHER_TABLE_STATS (\n" +
                    " OWNNAME => '" + DBUtils.getQuotedIdentifier(table.getSchema()) + "',\n" +
                    " TABNAME => '" + DBUtils.getQuotedIdentifier(table) + "',\n" +
                    " estimate_percent => " + samplePercent.getSelection() + "\n" +
                    " );\n" +
                    "END;";
                lines.add(sql);
            } else if (object instanceof OracleTableIndex) {
                OracleTableIndex index = (OracleTableIndex)object;
                String sql = "ALTER INDEX " + index.getFullQualifiedName() + " COMPUTE STATISTICS";
                lines.add(sql);
            }
        }

        @Override
        protected void createControls(Composite parent) {
            Group optionsGroup = UIUtils.createControlGroup(parent, "Options", 1, 0, 0);
            optionsGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            samplePercent = UIUtils.createLabelSpinner(optionsGroup, "Sample Percent", 5, 0, 100);
            samplePercent .addSelectionListener(SQL_CHANGE_LISTENER);

            createObjectsSelector(parent);
        }
    }

}