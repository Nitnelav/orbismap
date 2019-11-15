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
package org.orbisgis.coremap.map;

import org.locationtech.jts.awt.PointTransformation;
import org.locationtech.jts.awt.ShapeWriter;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.locationtech.jts.geom.GeometryFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapTransform implements PointTransformation {
        private static final Logger LOGGER = LoggerFactory.getLogger(MapTransform.class);
        private static RenderingHints screenHints;
        private boolean adjustExtent;
        private BufferedImage image = null;
        private Envelope adjustedExtent = null;
        private AffineTransform trans = new AffineTransform();
        private AffineTransform transInv = new AffineTransform();
        private Envelope extent;
        private ArrayList<TransformListener> listeners = new ArrayList<TransformListener>();
        private ShapeWriter converter;
        private double dpi;
        private static final double DEFAULT_DPI = 96.0;       
        private double MAXPIXEL_DISPLAY = 0 ;
        static final GeometryFactory GEOMETRY_FACTORY = new GeometryFactory();

        static {
                Map<RenderingHints.Key, Object> hints = new HashMap<>();
                hints.put(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
                hints.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
                hints.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                screenHints = new RenderingHints(hints);
        }

        public MapTransform() {
                adjustExtent = true;
                if(!GraphicsEnvironment.isHeadless()) {
                    this.dpi = Toolkit.getDefaultToolkit().getScreenResolution();
                } else {
                    LOGGER.trace("Headless graphics environment, set current DPI to 96.0");
                    this.dpi = DEFAULT_DPI;
                } 
        }

        /**
         * When true, the rendered map will always respects the CRS aspect ratio
         * When false, the Map extent will be bound to the output extent and may re-scale the map
         * @return
         */
        public boolean isAdjustExtent() {
                return adjustExtent;
        }

        public void setAdjustExtent(boolean adjustExtent) {
                this.adjustExtent = adjustExtent;
        }

        /**
         * Sets the painted image
         *
         * @param newImage The image where we will draw anything from now.
         */
        public void setImage(BufferedImage newImage) {
                image = newImage;
                calculateAffineTransform();
        }
        
        /**
         * 
         * @param renderingKey
         * @param renderingValue 
         */
        public void addRenderingHint(RenderingHints.Key renderingKey, Object renderingValue){
            screenHints.put(renderingKey, renderingValue);
        }

        /**
         * Gets the current {@code RenderingHints}
         * @return the current {@link RenderingHints}
         */
        public RenderingHints getRenderingHints() {
                return screenHints;
        }

        /**
         * Gets the painted image
         *
         * @return The image where we've drawn things.
         */
        public BufferedImage getImage() {
                return image;
        }

        /**
         * @return The currently configured dot-per-inch measure.
         */
        public double getDpi() {
                return dpi;
        }

        /**
         *
         * @param dpi The new dot-per-inch measure as a double.
         */
        public void setDpi(double dpi) {
                this.dpi = dpi;
        }

        /**
         * Gets the extent used to calculate the transformation. This extent is the
         * same as the set one but adjusted to have the same ratio than the image
         *
         * @return
         */
        public Envelope getAdjustedExtent() {
                return adjustedExtent;
        }

        /**
         *
         * @throws RuntimeException
         */
        private void calculateAffineTransform() {
                if (extent == null) {
                        return;
                } else if (image == null || getWidth() == 0 || getHeight() == 0) {
                        return;
                }

                if (adjustExtent) {
                        double escalaX = getWidth() / extent.getWidth();
                        double escalaY = getHeight() / extent.getHeight();

                        double xCenter = extent.getMinX() + extent.getWidth() / 2.0;
                        double yCenter = extent.getMinY() + extent.getHeight() / 2.0;
                        adjustedExtent = new Envelope();

                        double scale;
                        if (escalaX < escalaY) {
                                scale = escalaX;
                                double newHeight = getHeight() / scale;
                                double newX = xCenter - (extent.getWidth() / 2.0);
                                double newY = yCenter - (newHeight / 2.0);
                                adjustedExtent = new Envelope(newX, newX + extent.getWidth(), newY,
                                        newY + newHeight);
                        } else {
                                scale = escalaY;
                                double newWidth = getWidth() / scale;
                                double newX = xCenter - (newWidth / 2.0);
                                double newY = yCenter - (extent.getHeight() / 2.0);
                                adjustedExtent = new Envelope(newX, newX + newWidth, newY, newY
                                        + extent.getHeight());
                        }

                        trans.setToIdentity();
                        trans.concatenate(AffineTransform.getScaleInstance(scale, -scale));
                        trans.concatenate(AffineTransform.getTranslateInstance(-adjustedExtent.getMinX(),
                                -adjustedExtent.getMinY() - adjustedExtent.getHeight()));
                } else {
                        adjustedExtent = new Envelope(extent);
                        trans.setToIdentity();
                        double scaleX = getWidth() / extent.getWidth();
                        double scaleY = getHeight() / extent.getHeight();

                        /**
                         * Map Y axis grows downward but CRS grows upward => -1
                         */
                        trans.concatenate(AffineTransform.getScaleInstance(scaleX, -scaleY));
                        trans.concatenate(AffineTransform.getTranslateInstance(-extent.getMinX(), -extent.getMinY() - extent.getHeight()));
                }
                try {
                        transInv = trans.createInverse();
                } catch (NoninvertibleTransformException ex) {
                        transInv = null;
                        throw new RuntimeException(ex);
                }
        }

        /**
         * Gets the height of the drawn image
         *
         * @return The height of the image
         */
        public int getHeight() {
                if (image == null) {
                        return 0;
                } else {
                        return image.getHeight();
                }
        }

        /**
         * Gets the width of the drawn image
         *
         * @return The width of the image
         */
        public int getWidth() {
                if (image == null) {
                        return 0;
                } else {
                        return image.getWidth();
                }
        }

        /**
         * Sets the extent of the transformation. This extent is not used directly
         * to calculate the transformation but is adjusted to obtain an extent with
         * the same ratio than the image
         *
         * @param newExtent The new base extent.
         */
        public void setExtent(Envelope newExtent) {
                if ((newExtent != null)
                        && ((newExtent.getWidth() == 0) || (newExtent.getHeight() == 0))) {
                        newExtent.expandBy(10);
                }
                Envelope oldExtent = this.extent;
                boolean modified = true;
                /* Set extent when Envelope is modified */
                if (extent != null) {
                        if (extent.equals(newExtent)) {
                                modified = false;
                        }
                }
                if (modified) {
                        this.extent = newExtent;
                        calculateAffineTransform();
                        listeners.forEach((listener) -> {
                            listener.extentChanged(oldExtent, this);
                    });
                }
        }

        /**
         * Replaces the inner image with a new one with the specified size.
         *
         * @param width The width of the new image
         * @param height The height of the new image.
         */
        public void resizeImage(int width, int height) {
                int oldWidth = getWidth();
                int oldHeight = getHeight();
                GraphicsConfiguration configuration = GraphicsEnvironment.getLocalGraphicsEnvironment().
                        getDefaultScreenDevice().getDefaultConfiguration();
                image = configuration.createCompatibleImage(width, height,
                        BufferedImage.TYPE_INT_ARGB);
                calculateAffineTransform();
                listeners.forEach((listener) -> {
                    listener.imageSizeChanged(oldWidth, oldHeight, this);
            });
        }

    public static GeometryFactory getGeometryFactory() {
        return GEOMETRY_FACTORY;
    }
        
        

        /**
         * Gets this transformation
         *
         * @return
         */
        public AffineTransform getAffineTransform() {
                return trans;
        }

        /**
         * Gets the extent
         *
         * @return
         */
        public Envelope getExtent() {
                return extent;
        }

        /**
         * Transforms an envelope in map units to image units
         *
         * @param geographicEnvelope The {@link Envelope} in map units.
         * @return Rectangle2DDouble The envelope in image units as a {@link Rectangle2DDouble}.
         */
        public Rectangle2DDouble toPixel(Envelope geographicEnvelope) {
                final Point2D lowerRight = new Point2D.Double(geographicEnvelope.getMaxX(), geographicEnvelope.getMinY());
                final Point2D upperLeft = new Point2D.Double(geographicEnvelope.getMinX(), geographicEnvelope.getMaxY());

                final Point2D ul = trans.transform(upperLeft, null);
                final Point2D lr = trans.transform(lowerRight, null);

                return new Rectangle2DDouble(ul.getX(), ul.getY(), lr.getX()
                        - ul.getX(), lr.getY() - ul.getY());
        }

        /**
         * Transforms an image coordinate in pixels into a map coordinate
         *
         * @param i The x ordinate of the point in the image
         * @param j The y ordinate of the point in the image
         * @return The Point2D instance on the map computed from the given coordinates.
         */
        public Point2D toMapPoint(int i, int j) {
                if (transInv != null) {
                        return transInv.transform(new Point2D.Double(i, j), null);
                } else {
                        throw new RuntimeException("NonInvertibleMatrix");
                }
        }

        public Shape fromMapToWorld(Polygon p) {
                if (transInv != null) {
                        return transInv.createTransformedShape(p);
                } else {
                        throw new RuntimeException("NonInvertibleMatrix");
                }
        }

        /**
         * Transforms the specified map point to an image pixel
         *
         * @param point
         * @return
         */
        public Point fromMapPoint(Point2D point) {
                Point2D ret = trans.transform(point, null);
                return new Point((int) ret.getX(), (int) ret.getY());
        }

        /**
         * Sets the scale denominator, the Map extent is updated
         * @param denominator
         */
        public void setScaleDenominator(double denominator) {
                if (!adjustedExtent.isNull()) {
                        double currentScale = getScaleDenominator();
                        Coordinate center = getExtent().centre();
                        double expandFactor = (denominator/currentScale);
                        Envelope nextScaleEnvelope = new Envelope(center);
                        nextScaleEnvelope.expandBy(expandFactor*getExtent().getWidth()/2.,expandFactor*getExtent().getHeight()/2.);
                        setExtent(nextScaleEnvelope);
                }
        }
        /**
         *
         * @return The Image width in meter
         */
        private double getImageMeters() {
                double metersByPixel = 0.0254 / dpi;
                return getWidth() * metersByPixel;
        }
        /**
         * Gets the scale denominator. If the scale is 1:1000 this method returns
         * 1000. The scale is not absolutely precise and errors of 2% have been
         * measured.
         *
         * @return
         */
        public double getScaleDenominator() {
                if (adjustedExtent.isNull()) {
                        return 0;
                } else {
                        return adjustedExtent.getWidth() / getImageMeters();
                }
        }

        /**
         * Adds a listener waiting for transformation changes.
         * @param listener The new {@code TransformListener}.
         */
        public void addTransformListener(TransformListener listener) {
                listeners.add(listener);
        }

        /**
         * Removes the given listener of the associated TransformListener instances.
         * @param listener The listener to be removed.
         */
        public void removeTransformListener(TransformListener listener) {
                listeners.remove(listener);
        }

        @Override
        public void transform(Coordinate src, Point2D dest) {
                dest.setLocation(src.x, src.y);
                trans.transform(dest, dest);
        }

        /**
         * Gets the JTS {@code ShapeWriter} used for decimation and geometry simplifications we make before rendering
         * @return The currently used {@code ShapeWriter} instance.
         */
        public ShapeWriter getShapeWriter() {
                if (converter == null) {
                        converter = new ShapeWriter(this);
                        converter.setRemoveDuplicatePoints(true);
                        MAXPIXEL_DISPLAY = 0.5 / (25.4 / getDpi());
                }
                /**
                * Choose a fairly conservative decimation distance to avoid visual artifacts
                * TODO : decimation must be activate in relation with the crs to prevent rendering bug
                */
                //Double dec = adjustedExtent == null ? 0 : MAXPIXEL_DISPLAY / getScaleDenominator();
                //converter.setDecimation(dec);
                return converter;
        }

        /**
         * Gets the AWT {@link Shape}  we'll use to represent {@code geom} on the map.
         * @param geom The geometry we want to draw.
         * @param generalize If true we'll perform generalization
         * @return An AWT Shape instance.
         */
        public Shape getShape(Geometry geom, boolean generalize) {
                if (generalize) {
                        Rectangle2DDouble rectangle2dDouble = toPixel(geom.getEnvelopeInternal());
                        if ((rectangle2dDouble.getHeight() <= MAXPIXEL_DISPLAY)
                                && (rectangle2dDouble.getWidth() <= MAXPIXEL_DISPLAY)) {
                                if(geom.getDimension()==1){
                                     Coordinate[] coords = geom.getCoordinates();
                                     return getShapeWriter().toShape(geom.getFactory().createLineString(
                                             new Coordinate[]{coords[0], coords[coords.length-1]}));
                                }
                                else{
                                return rectangle2dDouble;
                                }
                        }
                }
                return getShapeWriter().toShape(geom);
        }

        public void redraw() {
            listeners.forEach((listener) -> {
                listener.extentChanged(this.adjustedExtent, this);
            });
        }
    
}
