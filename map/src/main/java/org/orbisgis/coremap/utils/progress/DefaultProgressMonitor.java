/**
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the 
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285
 * Equipe DECIDE
 * UNIVERSITÉ DE BRETAGNE-SUD
 * Institut Universitaire de Technologie de Vannes
 * 8, Rue Montaigne - BP 561 56017 Vannes Cedex
 * 
 * OrbisGIS is distributed under GPL 3 license.
 *
 * Copyright (C) 2007-2014 CNRS (IRSTV FR CNRS 2488)
 * Copyright (C) 2015-2017 CNRS (Lab-STICC UMR CNRS 6285)
 *
 * This file is part of OrbisGIS.
 *
 * OrbisGIS is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * OrbisGIS is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * OrbisGIS. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly:
 * info_at_ orbisgis.org
 */
package org.orbisgis.coremap.utils.progress;

import java.beans.PropertyChangeListener;

/**
 * Default implementation of an ProgressMonitor.
 */
public class DefaultProgressMonitor implements IProgressMonitor {

    protected DefaultProgressMonitor parentProcess;
    private long subprocess_size;
    private double subprocess_done = 0;

    public DefaultProgressMonitor(long subprocess_size, DefaultProgressMonitor parentProcess) {
        this.subprocess_size = subprocess_size;
        this.parentProcess = parentProcess;
    }

    public void removeChild(DefaultProgressMonitor child) {
        if(parentProcess != null) {
            parentProcess.removeChild(child);
        }
    }

    @Override
    public void addPropertyChangeListener(String property, PropertyChangeListener listener) {
        if(parentProcess != null) {
            parentProcess.addPropertyChangeListener(property, listener);
        }
    }

    @Override
    public void setTaskName(String taskName) {
        if(parentProcess != null) {
            parentProcess.setTaskName(taskName);
        }
    }

    @Override
    public IProgressMonitor startTask(String taskName, long end) {
        setTaskName(taskName);
        return startTask(end);
    }

    @Override
    public IProgressMonitor startTask(long end) {
        return new DefaultProgressMonitor(end, this);
    }

    protected synchronized void pushProgression(double incProg) {
        if (subprocess_done + incProg <= subprocess_size) {
            double oldProgress = subprocess_done;
            subprocess_done += incProg;
            if (parentProcess != null) {
                parentProcess.pushProgression((incProg / subprocess_size));
            }
        }
    }

    @Override
    public void endTask() {
        pushProgression(1.0);
    }

    @Override
    public String getCurrentTaskName() {
        if(parentProcess != null) {
            return parentProcess.getCurrentTaskName();
        } else {
            return "";
        }
    }

    /**
     * Optional, When the current process is done call this method. Or let the
     * garbage collector free the object
     */
    public synchronized void processFinished() {
        if (Double.compare(subprocess_done, subprocess_size) != 0) {
            this.parentProcess
                    .pushProgression(1 - (subprocess_done / subprocess_size));
            subprocess_done = subprocess_size;
        }
    }

    @Override
    protected synchronized void finalize() throws Throwable {
        // do finalization here
        if (this.parentProcess != null) {
            processFinished();
        }
        super.finalize();
    }

    @Override
    public void progressTo(long progress) {
        pushProgression(progress - subprocess_done);
    }

    @Override
    public double getOverallProgress() {
        if(parentProcess != null) {
            return parentProcess.getOverallProgress();
        } else {
            return subprocess_done / subprocess_size;
        }
    }

    @Override
    public long getCurrentProgress() {
        return (long)Math.floor(subprocess_done);
    }

    @Override
    public long getEnd() {
        return subprocess_size;
    }

    @Override
    public boolean isCancelled() {
        return parentProcess != null && parentProcess.isCancelled();
    }

    @Override
    public void setCancelled(boolean cancelled) {
        if(parentProcess != null) {
            parentProcess.setCancelled(cancelled);
        }
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        if(parentProcess != null) {
            parentProcess.removePropertyChangeListener(listener);
        }
    }
}
