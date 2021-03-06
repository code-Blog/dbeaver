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

package org.jkiss.dbeaver.model.qm;

import org.jkiss.dbeaver.model.qm.meta.QMMObject;

/**
 * QM meta event
 */
public class QMMetaEvent {

    public enum Action {
        BEGIN,
        END,
        UPDATE,
    }

    private final QMMObject object;
    private final Action action;

    public QMMetaEvent(QMMObject object, Action action)
    {
        this.object = object;
        this.action = action;
    }

    public QMMObject getObject()
    {
        return object;
    }

    public Action getAction()
    {
        return action;
    }

    @Override
    public String toString()
    {
        return action + " " + object;
    }
}
