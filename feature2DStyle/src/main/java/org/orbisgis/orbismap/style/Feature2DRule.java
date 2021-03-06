/**
 * Feature2DStyle is part of the OrbisGIS platform
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
 * Feature2DStyle is distributed under LGPL 3 license.
 *
 * Copyright (C) 2007-2014 CNRS (IRSTV FR CNRS 2488)
 * Copyright (C) 2015-2020 CNRS (Lab-STICC UMR CNRS 6285)
 *
 *
 * Feature2DStyle is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * Feature2DStyle is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with
 * Feature2DStyle. If not, see <http://www.gnu.org/licenses/>.
 *
 * For more information, please consult: <http://www.orbisgis.org/>
 * or contact directly: info_at_ orbisgis.org
 */
package org.orbisgis.orbismap.style;

import java.util.ArrayList;
import java.util.List;

import org.orbisgis.orbismap.style.common.Description;
import org.orbisgis.orbismap.style.parameter.RuleFilter;
import org.orbisgis.orbismap.style.symbolizer.PointSymbolizer;

/**
 * Rules are used to group rendering instructions by feature-property conditions and map scales.
 * Rule definitions are placed immediately inside of FeatureType- or coverage-style definitions.</p>
 * <p>According to SE 2.0, a <code>Rule</code> contains only one <code>Symbolizer</code> - but that  
 * <code>Symbolizer</code> can be a composite one. This implementation directly embedded 
 * a <code>CompositeSymbolizer</code> that will contain one -or more- actual <code>Symbolizer</code>
 * representation.
 * 
 * @author Maxence Laurent, HEIG-VD (2010-2012)
 * @author Erwan Bocher, CNRS (2010-2020)
 */
public class Feature2DRule extends StyleNode implements IRule<IFeatureSymbolizer> {

    /**
     * The name set to every rule, if not set externally.
     */
    public static final String DEFAULT_NAME = "Default Rule";
    private String name = "";
    private Description description = new Description();
    private RuleFilter expressionParameter;
    private Double minScaleDenom = null;
    private Double maxScaleDenom = null;
    private ArrayList<IFeatureSymbolizer> symbolizers;

    /**
     * Create a default, empty Rule, with a default inner (and empty) collection of Symbolizers.
     */
    public Feature2DRule() {
        symbolizers = new ArrayList<>();
        this.name=DEFAULT_NAME;
    }

    @Override
    public String toString() {
        if (name != null && !name.equalsIgnoreCase("")) {
            return name;
        } else {
            return "Untitled rule";
        }
    }

    
    /**
     * Get the <code>where</code> clause associated to this rule.
     * @return 
     *      The associated <code>where</code> clause.
     */
    public RuleFilter getFilter() {
        return expressionParameter;
    }
    
    /**
     * Replace the current inner <code>where</code> clause.
     * @param expression
     */
    public void setFilter(String expression) {
        setFilter(new RuleFilter(expression));
    }

    /**
     * Replace the current inner <code>where</code> clause.
     * @param expression
     */
    public void setFilter(RuleFilter expression) {
        if(expression!=null){
        this.expressionParameter = expression;
        this.expressionParameter.setParent(this);
        }
    }


    @Override
    public  List<IFeatureSymbolizer> getSymbolizers() {
        return symbolizers;
    }

    /**
     * Gets the maximum scale for which this <code>Rule</code> (and the features it is applied on)
     * must be displayed.
     * @return 
     * The maximum scale for rendering this Rule. The returned value is actually 
     * <b>the denominator of the scale</b>. Consequently, a value of 10 000 000
     * means a <b>scale of 1:10-million</b>
     */
    public Double getMaxScaleDenom() {
        return maxScaleDenom;
    }

    /**
     * Set the maximum scale for which this <code>Rule</code> (and the things
     * rendered from it) is displayed.
     * @param maxScaleDenom The  expected value is actually 
     * <b>the denominator of the scale</b>. Consequently, a value of 10 000 000
     * means a <b>scale of 1:10-million</b>
     */
    public void setMaxScaleDenom(Double maxScaleDenom) {
        if (maxScaleDenom != null && maxScaleDenom > 0) {
            this.maxScaleDenom = maxScaleDenom;
        } else {
            this.maxScaleDenom = null;
        }
    }

    /**
     * Gets the minimum scale for which this <code>Rule</code> (and the features it is applied on)
     * must be displayed.
     * @return 
     * The minimum scale for rendering this Rule. The returned value is actually 
     * <b>the denominator of the scale</b>. Consequently, a value of 10 000 000
     * means a <b>scale of 1:10-million</b>
     */
    public Double getMinScaleDenom() {
        return minScaleDenom;
    }

    /**
     * Set the minimum scale for which this <code>Rule</code> (and the things
     * rendered from it) is displayed.
     * @param minScaleDenom The  expected value is actually 
     * <b>the denominator of the scale</b>. Consequently, a value of 10 000 000
     * means a <b>scale of 1:10-million</b>
     */
    public void setMinScaleDenom(Double minScaleDenom) {
        if (minScaleDenom != null && minScaleDenom > 0) {
            this.minScaleDenom = minScaleDenom;
        } else {
            this.minScaleDenom = null;
        }
    }

    /**
     * This method checks that this rule is valid for a given scale.
     * returns true if {@code minScaleDenom <= scale <= maxScalDenom}
     * @param scaleDenominator The scale denominator.
     * @return 
     * <ul><li>{@code true} if {@code minScaleDenom <= scale <= maxScalDenom}
     * . Note that {@code null} values for the inner scale denominator values
     * are considered to be equivalent to 0 and positive infinity.</li>
     * <li>false otherwise.</li></ul>
     */
    public boolean isDomainAllowed(double scaleDenominator) {
        return (this.minScaleDenom == null && this.maxScaleDenom == null)
                || (this.minScaleDenom == null && this.maxScaleDenom != null && this.maxScaleDenom > scaleDenominator)
                || (this.minScaleDenom != null && scaleDenominator > this.minScaleDenom && this.maxScaleDenom == null)
                || (this.minScaleDenom != null && this.maxScaleDenom != null && scaleDenominator > this.minScaleDenom && this.maxScaleDenom > scaleDenominator);
    }

    /**
     * Get the description of this rule.
     * @return The Description of this Rule.
     * @see Description
     */
    public Description getDescription() {
        return description;
    }

    /**
     * Set the description associated to this rule.
     * @param description The new description for this.
     * @see Description
     */
    public void setDescription(Description description) {
        this.description = description;
    }

    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public List<IStyleNode> getChildren() {
            List<IStyleNode> ls = new ArrayList<IStyleNode>();  
            ls.addAll(symbolizers);
            return ls;
    }

    @Override
    public void addSymbolizer(IFeatureSymbolizer iSymbolizer) {
        iSymbolizer.setParent(this);
        symbolizers.add(iSymbolizer);
    }

    @Override
    public void addSymbolizer(int i, IFeatureSymbolizer iSymbolizer) {
        iSymbolizer.setParent(this);
        symbolizers.add(i, iSymbolizer);
    }    

    @Override
    public void initDefault() {
        PointSymbolizer pointSymbolizer = new PointSymbolizer();
        pointSymbolizer.initDefault();
        pointSymbolizer.setParent(this);
        addSymbolizer(pointSymbolizer);
        setName("Feature2D rule");
    }
}
