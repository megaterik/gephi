/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.standart_column.utilities;

import org.gephi.data.attributes.api.AttributeType;

/**
 *
 * @author megaterik
 */
public interface StandartColumn {

    @Override
    String toString();

    boolean isNodeTable();

    AttributeType getType();

    Object getValue(Object source);//from node or edge

    void setValue(Object target, Object value);
}