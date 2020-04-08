/**
 * Map is part of the OrbisGIS platform
 * 
 * OrbisGIS is a java GIS application dedicated to research in GIScience.
 * OrbisGIS is developed by the GIS group of the DECIDE team of the
 * Lab-STICC CNRS laboratory, see <http://www.lab-sticc.fr/>.
 *
 * The GIS group of the DECIDE team is located at :
 *
 * Laboratoire Lab-STICC – CNRS UMR 6285 Equipe DECIDE UNIVERSITÉ DE
 * BRETAGNE-SUD Institut Universitaire de Technologie de Vannes 8, Rue Montaigne
 * - BP 561 56017 Vannes Cedex
 *
 * Map is distributed under LGPL 3 license.
 *
 * Copyright (C) 2007-2014 CNRS (IRSTV FR CNRS 2488)
 * Copyright (C) 2015-2020 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * Map is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Map is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * Map. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly: info_at_ orbisgis.org
 */
package org.orbisgis.map.renderer.featureStyle.graphic;

import java.awt.Graphics2D;
import java.awt.Shape;
import org.orbisgis.map.layerModel.MapTransform;
import org.orbisgis.map.renderer.featureStyle.IStyleDrawer;
import org.orbisgis.style.graphic.ExternalGraphic;
import org.orbisgis.style.graphic.OnlineResource;
import org.orbisgis.style.parameter.ParameterException;

/**
 *
 * @author ebocher
 */
public class ExternalGraphicDrawer implements IStyleDrawer<ExternalGraphic>{

    private Shape shape;

    
        
    @Override
    public void draw(Graphics2D g2, MapTransform mapTransform, ExternalGraphic styleNode) throws ParameterException {
        
        OnlineResource onlineRessource = styleNode.getOnlineResource();
        
        if(onlineRessource!=null){
            String mimeType = styleNode.getMimeType();
            if (mimeType != null && mimeType.equalsIgnoreCase("image/svg+xml")) {
            //TODO : drawSVG(g2, at, opacity);
            } else {
            //TODO : drawJAI(g2, at, mt, opacity);
            }
        }    
    }
    
    @Override
    public Shape getShape() {
        return shape;
    }

    @Override
    public void setShape(Shape shape) {
        this.shape = shape;
    }
    
}