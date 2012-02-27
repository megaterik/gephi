/*
 Copyright 2008-2012 Gephi
 Authors : Taras Klaskovsky <megaterik@gmail.com>
 Website : http://www.gephi.org

 This file is part of Gephi.

 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2011 Gephi Consortium. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 3 only ("GPL") or the Common
 Development and Distribution License("CDDL") (collectively, the
 "License"). You may not use this file except in compliance with the
 License. You can obtain a copy of the License at
 http://gephi.org/about/legal/license-notice/
 or /cddl-1.0.txt and /gpl-3.0.txt. See the License for the
 specific language governing permissions and limitations under the
 License.  When distributing the software, include this License Header
 Notice in each file and include the License files at
 /cddl-1.0.txt and /gpl-3.0.txt. If applicable, add the following below the
 License Header, with the fields enclosed by brackets [] replaced by
 your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 If you wish your version of this file to be governed by only the CDDL
 or only the GPL Version 3, indicate your decision by adding
 "[Contributor] elects to include this software in this distribution
 under the [CDDL or GPL Version 3] license." If you do not indicate a
 single choice of license, a recipient has the option to distribute
 your version of this file under either the CDDL, the GPL Version 3 or
 to extend the choice of license to its licensees as provided above.
 However, if you add GPL Version 3 code and therefore, elected the GPL
 Version 3 license, then the option applies only if the new code is
 made subject to such option by the copyright holder.

 Contributor(s):

 Portions Copyrighted 2011 Gephi Consortium.
 */
package org.gephi.recast_manipulator;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.graph.api.Attributes;
import org.openide.util.Lookup;

/**
 *
 * @author megaterik
 */
public class RecastImplementation {
    /*
     * Doesn't remove non-decimal symbols or fraction, like
     * AttribyteType.parse(), because it's should be done, when force checkbox
     * is chosen
     */

    static private String formatIntType(String s) {
        if (s.endsWith(".0")) {
            return s.substring(0, s.length() - 2);
        } else {
            return s;
        }
    }

    static public boolean possibleToConvertValue(Object value, AttributeType type, StringBuilder error) {
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
                    if (!value.toString().equalsIgnoreCase("true") && !value.toString().equalsIgnoreCase("false"))
                        error.append(value.toString()).append(" should be \'true\' or \'false\'");
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
            error.append(ex.toString()).append("\n");
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

    static public boolean possibleToConvertColumn(AttributeTable table, AttributeColumn column, AttributeType type, StringBuilder error) {
        AttributeColumnsController controller = Lookup.getDefault().lookup(AttributeColumnsController.class);
        for (Attributes value : controller.getTableAttributeRows(table)) {
            if (!possibleToConvertValue(value.getValue(column.getId()), type, error)) {
                return false;
            }
        }
        if (nullTable(controller.getTableAttributeRows(table), column.getId())) {
            error.append("table is empty\n");
            return false;
        }
        return true;
    }

    static public boolean recast(AttributeTable table, AttributeColumn column, AttributeType type, String newTitle, StringBuilder error) {
        AttributeColumnsController controller = Lookup.getDefault().lookup(AttributeColumnsController.class);
        if (controller.duplicateColumn(table, column, newTitle, type) == null) {
            error.append(newTitle).append(" already exists\n");
            return false;
        } else {
            return true;
        }
    }

    static public boolean move(AttributeTable table, String oldTitle, String newTitle, StringBuilder error) {
        AttributeColumnsController controller = Lookup.getDefault().lookup(AttributeColumnsController.class);
        if (table.hasColumn(newTitle) && table.hasColumn(oldTitle) && controller.canDeleteColumn(table.getColumn(oldTitle))
                && controller.canDeleteColumn(table.getColumn(newTitle))) {
            controller.deleteAttributeColumn(table, table.getColumn(oldTitle));
            controller.duplicateColumn(table, table.getColumn(newTitle), oldTitle, table.getColumn(newTitle).getType());
            controller.deleteAttributeColumn(table, table.getColumn(newTitle));
            return true;
        }
        if (!table.hasColumn(newTitle)) {
            error.append(newTitle).append(" doesn't exist");
        }
        if (!table.hasColumn(oldTitle)) {
            error.append(oldTitle).append(" doesn't exist");
        }
        if (!controller.canDeleteColumn(table.getColumn(newTitle))) {
            error.append("Impossible to delete ").append(newTitle);
        }
        if (!controller.canDeleteColumn(table.getColumn(oldTitle))) {
            error.append("Impossible to delete ").append(oldTitle);
        }
        return false;
    }
}
