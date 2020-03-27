/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.orbisgis.style.utils;

import org.orbisgis.style.parameter.ParameterException;
import org.orbisgis.style.Uom;

/**
 *
 * @author ebocher
 */
public class UomUtils {

    public static final double PT_IN_INCH = 72.0;
    public static final double MM_IN_INCH = 25.4;
    public static final double IN_IN_FOOT = 12;
    public static final double ONE_THOUSAND = 1000;
    public static final double ONE_HUNDRED = 100;

    /**
     * Convert a value to the corresponding value in pixel
     *
     * Note that converting ground unit to pixel is done by using a constant
     * scale
     *
     * @param value the value to convert
     * @param uom unit of measure for value
     * @param dpi the current resolution
     * @param scale the current scale (for converting ground meters and ground
     * feet to media units)
     * @param v100p the value to return when uom is "percent" and value is 100
     * (%)
     * @return
     * @throws ParameterException
     *
     * @todo return integer !!!
     */
    public static float toPixel(float value, Uom uom, double dpi, double scale, float v100p) throws ParameterException {
        if (uom == null) {
            return value; // no uom ? => return as Pixel !
        }

        if (dpi <= 0  && uom != Uom.PX) {
            throw new ParameterException("DPI is invalid");
        }

        switch (uom) {
            case IN:
                return (float) (value * dpi); // [IN] * [PX]/[IN] => [PX]
            case MM:
                return (float) ((value / MM_IN_INCH) * dpi); // [MM] * [IN]/[MM] * [PX]/[IN] => [PX]
            case PT: // 1PT == 1/72[IN] whatever dpi is
                return (float) ((value / PT_IN_INCH) * dpi); // 1/72[IN] * 72 *[PX]/[IN] => [PX]
            case GM:
                if (scale <= 0) {
                    throw new ParameterException("Scale is invalid");
                }
                return (float) ((value * ONE_THOUSAND * dpi) / (scale * MM_IN_INCH));
            case GFT:
                if (scale <= 0){
                    throw new ParameterException("Scale is invalid");
                }
                return (float) ((value * IN_IN_FOOT * dpi) / (scale));
            case PERCENT:
                return (float) (value * v100p / ONE_HUNDRED);
            case PX:
            default:
                return value; // [PX]
        }
    }
    
    /**
     * Convert a value to the corresponding value in pixel
     *
     * Note that converting ground unit to pixel is done by using a constant
     * scale
     *
     * @param value the value to convert
     * @param uom unit of measure for value
     * @param dpi the current resolution
     * @param scale the current scale (for converting ground meters and ground
     * feet to media units)
     * (%)
     * @return
     * @throws ParameterException
     *
     * @todo return integer !!!
     */
    public static float toPixel(float value, Uom uom, double dpi, double scale) throws ParameterException {
        if (uom == null) {
            return value; // no uom ? => return as Pixel !
        }

        if (dpi <= 0  && uom != Uom.PX) {
            throw new ParameterException("DPI is invalid");
        }
        
        if(value ==0){
            return value;
        }

        switch (uom) {
            case IN:
                return (float) (value * dpi); // [IN] * [PX]/[IN] => [PX]
            case MM:
                return (float) ((value / MM_IN_INCH) * dpi); // [MM] * [IN]/[MM] * [PX]/[IN] => [PX]
            case PT: // 1PT == 1/72[IN] whatever dpi is
                return (float) ((value / PT_IN_INCH) * dpi); // 1/72[IN] * 72 *[PX]/[IN] => [PX]
            case GM:
                if (scale <= 0) {
                    throw new ParameterException("Scale is invalid");
                }
                return (float) ((value * ONE_THOUSAND * dpi) / (scale * MM_IN_INCH));
            case GFT:
                if (scale <= 0){
                    throw new ParameterException("Scale is invalid");
                }
                return (float) ((value * IN_IN_FOOT * dpi) / (scale));
            case PERCENT:
                return value;
            case PX:
            default:
                return value; // [PX]
        }
    }

}
