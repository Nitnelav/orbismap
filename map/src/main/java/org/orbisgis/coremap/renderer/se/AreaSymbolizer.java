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
package org.orbisgis.coremap.renderer.se;

import org.locationtech.jts.geom.Geometry;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.orbisgis.coremap.map.MapTransform;
import org.orbisgis.coremap.renderer.se.Utils.UomUtils;
import org.orbisgis.coremap.renderer.se.fill.Fill;
import org.orbisgis.coremap.renderer.se.fill.SolidFill;
import org.orbisgis.coremap.renderer.se.parameter.ParameterException;
import org.orbisgis.coremap.renderer.se.parameter.real.RealParameter;
import org.orbisgis.coremap.renderer.se.parameter.real.RealParameterContext;
import org.orbisgis.coremap.renderer.se.stroke.PenStroke;
import org.orbisgis.coremap.renderer.se.stroke.Stroke;
import org.orbisgis.coremap.renderer.se.transform.Translate;
import org.orbisgis.style.IStyleNode;

/**
 * A "AreaSymbolizer" specifies the rendering of a polygon or other area/surface geometry, 
 * including its interior fill and border stroke.</p>
 * <p>In addition of the properties inherited from <code>VectorSymbolizer</code> an <code>
 * AreaSymbolizer</code> is defined with a perpendicular offset, a <code>Stroke</code> (to draw its limit, 
 * and as a <code>StrokeNode</code>) and a <code>Fill</code> (to paint its interior, and 
 * as a <code>FillNode</code>).
 * @author Maxence Laurent, Alexis Guéganno
 */
public final class AreaSymbolizer extends FeatureSymbolizer implements FillNode, StrokeNode {

        private Translate translate;
        private RealParameter perpendicularOffset;
        private Stroke stroke;
        private Fill fill;
      
        /**
         * Build a new AreaSymbolizer, named "Area Symbolizer". It is defined with a
         * <code>SolidFill</code> and a <code>PenStroke</code>
         */
        public AreaSymbolizer() {
                super();
                setName("Area Symbolizer");
                this.setFill(new SolidFill());
                this.setStroke(new PenStroke());
        }        

        @Override
        public void setStroke(Stroke stroke) {
                if (stroke != null) {
                        stroke.setParent(this);
                }
                this.stroke = stroke;
        }

        @Override
        public Stroke getStroke() {
                return stroke;
        }

        @Override
        public void setFill(Fill fill) {
                if (fill != null) {
                        fill.setParent(this);
                }
                this.fill = fill;
        }

        @Override
        public Fill getFill() {
                return fill;
        }

        /**
         * Retrieve the geometric transformation that must be applied to the geometries.
         * @return 
         *  The transformation associated to this Symbolizer.
         */
        public Translate getTranslate() {
                return translate;
        }

        /**
         * Get the geometric transformation that must be applied to the geometries.
         * @param translate
         */
        public void setTranslate(Translate translate) {
                this.translate = translate;
                //translate.setParent(this);
        }

        /**
         * Get the current perpendicular offset associated to this Symbolizer. It allows to
         * draw polygons larger or smaller than their actual geometry. The meaning of the 
         * value is dependant of the <code>Uom</code> instance associated to this <code>Symbolizer</code>.
         * 
         * @return 
         *          The offset as a <code>RealParameter</code>. A positive value will cause the
         *          polygons to be drawn larger than their original size, while a negative value
         *          will cause the drawing of smaller polygons.
         */
        public RealParameter getPerpendicularOffset() {
                return perpendicularOffset;
        }

        /**
         * Set the current perpendicular offset associated to this Symbolizer. It allows to
         * draw polygons larger or smaller than their actual geometry. The meaning of the 
         * value is dependant of the <code>Uom</code> instance associated to this <code>Symbolizer</code>.
         * @param perpendicularOffset 
         *          The offset as a <code>RealParameter</code>. A positive value will cause the
         *          polygons to be drawn larger than their original size, while a negative value
         *          will cause the drawing of smaller polygons.
         */
        public void setPerpendicularOffset(RealParameter perpendicularOffset) {
                this.perpendicularOffset = perpendicularOffset;
                if (this.perpendicularOffset != null) {
                        this.perpendicularOffset.setContext(RealParameterContext.REAL_CONTEXT);
                        this.perpendicularOffset.setParent(this);
                }
        }

        /**
         *
         * @param g2
         * @param rs
         * @param fid
         * @throws ParameterException
         * @throws IOException error while accessing external resource
         * @throws java.sql.SQLException
         */
        @Override
        public void draw(Graphics2D g2, ResultSet rs, long fid,
                boolean selected, MapTransform mt, Geometry the_geom)
                throws ParameterException, IOException, SQLException {

                List<Shape> shapes = new LinkedList<Shape>();
                shapes.add(mt.getShape(the_geom, true));
                Map<String,Object> map = getFeaturesMap(rs, fid);
                for (Shape shp : shapes) {
                        if (this.getTranslate() != null) {
                                shp = getTranslate().getAffineTransform(map, getUom(), mt,
                                        (double) mt.getWidth(), (double) mt.getHeight()).createTransformedShape(shp);
                        }
                        if (shp != null) {
                                if (fill != null) {
                                        fill.draw(g2, map, shp, selected, mt);
                                }

                                if (stroke != null) {
                                        double offset = 0.0;
                                        if (perpendicularOffset != null) {
                                                offset = UomUtils.toPixel(perpendicularOffset.getValue(rs, fid),
                                                        getUom(), mt.getDpi(), mt.getScaleDenominator(), null);
                                        }
                                        stroke.draw(g2, map, shp, selected, mt, offset);
                                }
                        }
                }
        }

        
        @Override
        public List<IStyleNode> getChildren() {
                List<IStyleNode> ls = new ArrayList<IStyleNode>(4);
                if(this.getGeometryAttribute()!=null){
                    ls.add(this.getGeometryAttribute());
                }
                if (translate != null) {
                        ls.add(translate);
                }
                if (fill != null) {
                        ls.add(fill);
                }
                if (perpendicularOffset != null) {
                        ls.add(perpendicularOffset);
                }
                if (stroke != null) {
                        ls.add(stroke);
                }
                return ls;
        }
}
