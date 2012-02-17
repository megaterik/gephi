/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.recast_manipulator;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.*;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.graph.api.*;
import org.openide.util.Lookup;

/**
 *
 * @author megaterik
 */
public class RecastImplementation {
    /*
     * Doesn't remove non-decimal symbols or fraction, like AttribyteType.parse(),
     * because it's should be done, when force checkbox is chosen
     */
    static private String formatIntType(String s)
    {
        if (s.endsWith(".0"))
            return s.substring(0, s.length() - 2);
        else
            return s;
    }

    static public boolean possibleToConvertValue(Object value, AttributeType type) {
        if (value == null) {
            return true;
        }

        try {
            switch (type) {
                case BIGDECIMAL:
                    BigDecimal isBigDecimal = new BigDecimal(value.toString());
                    return true;
                case BIGINTEGER:
                    BigInteger isBigInteger = new BigInteger(formatIntType(value.toString()));
                    return true;
                case CHAR:
                    return (value.toString().length() == 1);
                case BOOLEAN:
                    return (value.toString().equalsIgnoreCase("true") || value.toString().equalsIgnoreCase("false"));
                case BYTE:
                    Byte.parseByte(formatIntType(value.toString()));
                    return true;
                case DOUBLE:
                    Double.parseDouble(value.toString());
                    return true;
                case DYNAMIC_BIGDECIMAL:
                case DYNAMIC_BIGINTEGER:
                case DYNAMIC_BOOLEAN:
                case DYNAMIC_BYTE:
                case DYNAMIC_CHAR:
                case DYNAMIC_DOUBLE:
                case DYNAMIC_FLOAT:
                case DYNAMIC_INT:
                case DYNAMIC_LONG:
                case DYNAMIC_SHORT://not implemented yet
                case DYNAMIC_STRING:
                    return false;
                case FLOAT:
                    Float.parseFloat(value.toString());
                    return true;
                case INT:
                    Integer.parseInt(formatIntType(value.toString()));
                    return true;
                case LIST_BIGDECIMAL:
                case LIST_BIGINTEGER:
                case LIST_BOOLEAN:
                case LIST_BYTE:
                case LIST_CHARACTER:
                case LIST_DOUBLE:
                case LIST_FLOAT://not implemented either
                case LIST_INTEGER:
                case LIST_LONG:
                case LIST_SHORT:
                case LIST_STRING:
                    return false;
                case LONG:
                    Long.parseLong(formatIntType(value.toString()));
                case SHORT:
                    Short.parseShort(formatIntType(value.toString()));
                case STRING:
                    value.toString();
                    return true;
                case TIME_INTERVAL:
                    return false;
            }
        } catch (Exception ex)//exception meants that type is wrong
        {
        }
        return false;
    }

    static private boolean nullTable(Attributes[] values, String id) {
        for (Attributes object : values) {
            if (object.getValue(id) != null) {
                return false;
            }
        }
        return true;
    }

    static public boolean possibleToConvertColumn(AttributeTable table, AttributeColumn column, AttributeType type) {
        AttributeColumnsController controller = Lookup.getDefault().lookup(AttributeColumnsController.class);
        for (Attributes value : controller.getTableAttributeRows(table)) {
            if (!possibleToConvertValue(value.getValue(column.getId()), type)) {
                return false;
            }
        }
        if (nullTable(controller.getTableAttributeRows(table), column.getId())) {
            return false;
        }
        return true;
    }

    static public boolean recast(AttributeTable table, AttributeColumn column, AttributeType type, String newTitle) {
        AttributeColumnsController controller = Lookup.getDefault().lookup(AttributeColumnsController.class);
        return (controller.duplicateColumn(table, column,
                newTitle, type) != null);
    }

    static public boolean move(AttributeTable table, String oldTitle, String newTitle) {
        AttributeColumnsController controller = Lookup.getDefault().lookup(AttributeColumnsController.class);
        if (table.hasColumn(newTitle) && table.hasColumn(oldTitle) && controller.canDeleteColumn(table.getColumn(oldTitle))
                && controller.canDeleteColumn(table.getColumn(newTitle))) {
            controller.deleteAttributeColumn(table, table.getColumn(oldTitle));
            controller.duplicateColumn(table, table.getColumn(newTitle), oldTitle, table.getColumn(newTitle).getType());
            controller.deleteAttributeColumn(table, table.getColumn(newTitle));
            return true;
        }
        return false;
    }
}
