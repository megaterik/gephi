/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.standart_column.utilities;

import org.gephi.data.attributes.api.AttributeType;
import org.gephi.graph.api.Edge;
import org.gephi.graph.api.Node;

/**
 *
 * @author megaterik
 */
public class StandartColumns {

    static final public StandartColumn nodeX = new StandartColumn() {

        @Override
        public String toString() {
            return "X-coordinate(Node table)";
        }

        @Override
        public boolean isNodeTable() {
            return true;
        }

        @Override
        public AttributeType getType() {
            return AttributeType.FLOAT;
        }

        @Override
        public Object getValue(Object source) {
            return ((Node) source).getNodeData().x();
        }

        @Override
        public void setValue(Object target, Object value) {
            ((Node) target).getNodeData().setX((Float) AttributeType.FLOAT.parse(value.toString()));
        }
    };
    static final public StandartColumn nodeY = new StandartColumn() {

        @Override
        public String toString() {
            return "Y-coordinate(Node table)";
        }

        @Override
        public boolean isNodeTable() {
            return true;
        }

        @Override
        public AttributeType getType() {
            return AttributeType.FLOAT;
        }

        @Override
        public Object getValue(Object source) {
            return ((Node) source).getNodeData().y();
        }

        @Override
        public void setValue(Object target, Object value) {
            ((Node) target).getNodeData().setY((Float) AttributeType.FLOAT.parse(value.toString()));
        }
    };
    static final public StandartColumn nodeZ = new StandartColumn() {

        @Override
        public String toString() {
            return "Z-coordinate(Node table)";
        }

        @Override
        public boolean isNodeTable() {
            return true;
        }

        @Override
        public AttributeType getType() {
            return AttributeType.FLOAT;
        }

        @Override
        public Object getValue(Object source) {
            return ((Node) source).getNodeData().z();
        }

        @Override
        public void setValue(Object target, Object value) {
            ((Node) target).getNodeData().setZ((Float) AttributeType.FLOAT.parse(value.toString()));
        }
    };
    static final public StandartColumn nodeSize = new StandartColumn() {

        @Override
        public String toString() {
            return "Size(Node table)";
        }

        @Override
        public boolean isNodeTable() {
            return true;
        }

        @Override
        public AttributeType getType() {
            return AttributeType.FLOAT;
        }

        @Override
        public Object getValue(Object source) {
            return ((Node) source).getNodeData().getSize();
        }

        @Override
        public void setValue(Object target, Object value) {
            ((Node) target).getNodeData().setSize((Float) AttributeType.FLOAT.parse(value.toString()));
        }
    };
    static final public StandartColumn nodeRedColor = new StandartColumn() {

        @Override
        public String toString() {
            return "Red(Node table)";
        }

        @Override
        public boolean isNodeTable() {
            return true;
        }

        @Override
        public AttributeType getType() {
            return AttributeType.FLOAT;
        }

        @Override
        public Object getValue(Object source) {
            return ((Node) source).getNodeData().r();
        }

        @Override
        public void setValue(Object target, Object value) {
            ((Node) target).getNodeData().setR((Float) AttributeType.FLOAT.parse(value.toString()));
        }
    };
    static final public StandartColumn nodeGreenColor = new StandartColumn() {

        @Override
        public String toString() {
            return "Green(Node table)";
        }

        @Override
        public boolean isNodeTable() {
            return true;
        }

        @Override
        public AttributeType getType() {
            return AttributeType.FLOAT;
        }

        @Override
        public Object getValue(Object source) {
            return ((Node) source).getNodeData().g();
        }

        @Override
        public void setValue(Object target, Object value) {
            ((Node) target).getNodeData().setG((Float) AttributeType.FLOAT.parse(value.toString()));
        }
    };
    static final public StandartColumn nodeBlueColor = new StandartColumn() {

        @Override
        public String toString() {
            return "Blue(Node table)";
        }

        @Override
        public boolean isNodeTable() {
            return true;
        }

        @Override
        public AttributeType getType() {
            return AttributeType.FLOAT;
        }

        @Override
        public Object getValue(Object source) {
            return ((Node) source).getNodeData().b();
        }

        @Override
        public void setValue(Object target, Object value) {
            ((Node) target).getNodeData().setB((Float) AttributeType.FLOAT.parse(value.toString()));
        }
    };
    static final public StandartColumn nodeColor = new StandartColumn() {

        @Override
        public String toString() {
            return "Color(Node table)";
        }

        @Override
        public boolean isNodeTable() {
            return true;
        }

        @Override
        public AttributeType getType() {
            return AttributeType.STRING;
        }

        int toByte(float a) {
            return Math.round(a * 255);
        }

        float toFloat(int a) {
            return ((float) a) / 255f;
        }

        @Override
        public Object getValue(Object source) {
            int r = toByte(((Node) source).getNodeData().r());
            int g = toByte(((Node) source).getNodeData().g());
            int b = toByte(((Node) source).getNodeData().b());
            return ("#" + Integer.toString(r, 16) + Integer.toString(g, 16) + Integer.toString(b, 16));
        }

        @Override
        public void setValue(Object target, Object value) {
            float r = toFloat(Integer.parseInt(((String) value).substring(1, 3), 16));
            float g = toFloat(Integer.parseInt(((String) value).substring(3, 5), 16));
            float b = toFloat(Integer.parseInt(((String) value).substring(5, 7), 16));
            ((Node) target).getNodeData().setColor(r, g, b);
        }
    };
    //red, green, blue edge color currently -1, so skip them
    static final public StandartColumn edgeSize = new StandartColumn() {

        @Override
        public boolean isNodeTable() {
            return false;
        }

        @Override
        public AttributeType getType() {
            return AttributeType.FLOAT;
        }

        @Override
        public Object getValue(Object source) {
            return ((Edge) (source)).getEdgeData().getSize();
        }

        @Override
        public void setValue(Object target, Object value) {
            ((Edge)target).getEdgeData().setSize(Float.parseFloat(value.toString()));
        }

        @Override
        public String toString() {
            return "Size(Edge table)";
        }
    };
    static final public StandartColumn edgeX = new StandartColumn() {

        @Override
        public boolean isNodeTable() {
            return false;
        }

        @Override
        public AttributeType getType() {
            return AttributeType.FLOAT;
        }

        @Override
        public Object getValue(Object source) {
            return ((Edge) (source)).getEdgeData().x();
        }

        @Override
        public void setValue(Object target, Object value) {
            ((Edge)target).getEdgeData().setX(Float.parseFloat(value.toString()));
        }

        @Override
        public String toString() {
            return "X-coordinate(Edge table)";
        }
    };
    static final public StandartColumn edgeY = new StandartColumn() {

        @Override
        public boolean isNodeTable() {
            return false;
        }

        @Override
        public AttributeType getType() {
            return AttributeType.FLOAT;
        }

        @Override
        public Object getValue(Object source) {
            return ((Edge) (source)).getEdgeData().y();
        }

        @Override
        public void setValue(Object target, Object value) {
            ((Edge)target).getEdgeData().setY(Float.parseFloat(value.toString()));
        }

        @Override
        public String toString() {
            return "Y-coordinate(Edge table)";
        }
    };
    static final public StandartColumn edgeZ = new StandartColumn() {

        @Override
        public boolean isNodeTable() {
            return false;
        }

        @Override
        public AttributeType getType() {
            return AttributeType.FLOAT;
        }

        @Override
        public Object getValue(Object source) {
            return ((Edge) (source)).getEdgeData().z();
        }

        @Override
        public void setValue(Object target, Object value) {
            ((Edge)target).getEdgeData().setZ(Float.parseFloat(value.toString()));
        }

        @Override
        public String toString() {
            return "Z-coordinate(Edge table)";
        }
    };
}