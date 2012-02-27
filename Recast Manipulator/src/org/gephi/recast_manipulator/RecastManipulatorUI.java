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

import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JPanel;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.AttributeController;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.data.attributes.api.AttributeType;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.spi.DialogControls;
import org.gephi.datalab.spi.Manipulator;
import org.gephi.datalab.spi.ManipulatorUI;
import org.openide.util.Lookup;

/**
 *
 * @author megaterik
 */
public class RecastManipulatorUI extends javax.swing.JPanel implements ManipulatorUI {

    /**
     * Creates new form RecastManipulatorUI
     */
    public RecastManipulatorUI() {
        initComponents();
        initComboBox();
    }
    private ArrayList<ConvertToComboBoxType> convertToOption;
    private AttributeType[] suitableClass;//queue for auto-recasting
    private String lastColumnBeingDuplicated;
    private String lastColumnThatDuplicate;
    private String lastColumnLabel;
    private boolean lastOperationWasAutoRecast = false;
    private ArrayList<String> removedDuringAutoColumns = new ArrayList<String>();
    private ArrayList<String> addedDuringAutoColumns = new ArrayList<String>();
    private ArrayList<String> labelDuringAutoColumns = new ArrayList<String>();

    private void initComboBox() {
        convertToOption = new ArrayList<ConvertToComboBoxType>();

        convertToOption.add(new ConvertToComboBoxType("Suitable class",
                new AttributeType[]{AttributeType.INT,
                    AttributeType.LONG, AttributeType.BIGINTEGER, AttributeType.BOOLEAN, AttributeType.DOUBLE}));

        suitableClass = new AttributeType[]{AttributeType.INT,
            AttributeType.LONG, AttributeType.BIGINTEGER, AttributeType.BOOLEAN, AttributeType.DOUBLE};
        convertToOption.add(new ConvertToComboBoxType("Suitable integer class",
                new AttributeType[]{AttributeType.INT, AttributeType.LONG, AttributeType.BIGINTEGER}));

        convertToOption.add(new ConvertToComboBoxType("Byte(" + Byte.MIN_VALUE + "," + Byte.MAX_VALUE + ")",
                new AttributeType[]{AttributeType.BYTE}));

        convertToOption.add(new ConvertToComboBoxType("Short(" + Short.MIN_VALUE + "," + Short.MIN_VALUE + ")",
                new AttributeType[]{AttributeType.SHORT}));

        convertToOption.add(new ConvertToComboBoxType("Integer(" + Integer.MIN_VALUE + "," + Integer.MAX_VALUE + ")",
                new AttributeType[]{AttributeType.INT}));

        convertToOption.add(new ConvertToComboBoxType("Long(" + Long.MIN_VALUE + "," + Long.MAX_VALUE + ")",
                new AttributeType[]{AttributeType.LONG}));

        convertToOption.add(new ConvertToComboBoxType("BigInteger(" + "unlimited integer)",
                new AttributeType[]{AttributeType.BIGINTEGER}));

        convertToOption.add(new ConvertToComboBoxType("Float(decimal fraction)",
                new AttributeType[]{AttributeType.FLOAT}));

        convertToOption.add(new ConvertToComboBoxType("Double(more accurate decimal fraction)",
                new AttributeType[]{AttributeType.BOOLEAN}));

        convertToOption.add(new ConvertToComboBoxType("BigDecimal(" + "unlimited decimal)",
                new AttributeType[]{AttributeType.BIGDECIMAL}));

        convertToOption.add(new ConvertToComboBoxType("Boolean(true/false)",
                new AttributeType[]{AttributeType.BOOLEAN}));

        convertToOption.add(new ConvertToComboBoxType("Character",
                new AttributeType[]{AttributeType.CHAR}));

        convertToOption.add(new ConvertToComboBoxType("String",
                new AttributeType[]{AttributeType.STRING}));
        convertToComboBox.setModel(new DefaultComboBoxModel(convertToOption.toArray()));
    }
    private static final String NODE_TABLE = "(Node table)";
    private static final String EDGE_TABLE = "(Edge table)";

    private void initColumnComboBox() {
        AttributeController ac = Lookup.getDefault().lookup(AttributeController.class);
        ArrayList<String> columnTitle = new ArrayList<String>();
        for (AttributeColumn column : ac.getModel().getNodeTable().getColumns()) {
            columnTitle.add(column.getTitle() + NODE_TABLE);//warning: we rely that columnTitle ends with NODE_TABLE or EDGE_TABLE
        }
        for (AttributeColumn column : ac.getModel().getEdgeTable().getColumns()) {
            columnTitle.add(column.getTitle() + EDGE_TABLE);
        }
        String previousTitle = columnComboBox.getSelectedItem().toString();
        columnComboBox.setModel(new DefaultComboBoxModel((columnTitle.toArray())));
        for (int i = 0; i < columnComboBox.getItemCount(); i++) {
            if (previousTitle.equals(columnComboBox.getItemAt(i).toString())) {
                columnComboBox.setSelectedIndex(i);
                break;
            }
        }
        AttributeColumn column = getColumnByCheckBoxName(columnComboBox.getSelectedItem().toString());
        currentTypeShowLabel.setText(column.getType().toString());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        columnLabel = new javax.swing.JLabel();
        currentTypeLabel = new javax.swing.JLabel();
        convertToLabel = new javax.swing.JLabel();
        columnComboBox = new javax.swing.JComboBox();
        convertToComboBox = new javax.swing.JComboBox();
        currentTypeShowLabel = new javax.swing.JLabel();
        autoButton = new javax.swing.JButton();
        recastButton = new javax.swing.JButton();
        forceRecastCheckBox = new javax.swing.JCheckBox();
        replaceOriginalButton = new javax.swing.JButton();

        columnLabel.setText(org.openide.util.NbBundle.getMessage(RecastManipulatorUI.class, "RecastManipulatorUI.columnLabel.text")); // NOI18N

        currentTypeLabel.setText(org.openide.util.NbBundle.getMessage(RecastManipulatorUI.class, "RecastManipulatorUI.currentTypeLabel.text")); // NOI18N

        convertToLabel.setText(org.openide.util.NbBundle.getMessage(RecastManipulatorUI.class, "RecastManipulatorUI.convertToLabel.text")); // NOI18N

        columnComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        columnComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                columnComboBoxActionPerformed(evt);
            }
        });

        convertToComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        currentTypeShowLabel.setText(org.openide.util.NbBundle.getMessage(RecastManipulatorUI.class, "RecastManipulatorUI.currentTypeShowLabel.text")); // NOI18N

        autoButton.setText(org.openide.util.NbBundle.getMessage(RecastManipulatorUI.class, "RecastManipulatorUI.autoButton.text")); // NOI18N
        autoButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                autoButtonActionPerformed(evt);
            }
        });

        recastButton.setText(org.openide.util.NbBundle.getMessage(RecastManipulatorUI.class, "RecastManipulatorUI.recastButton.text")); // NOI18N
        recastButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                recastButtonActionPerformed(evt);
            }
        });

        forceRecastCheckBox.setText(org.openide.util.NbBundle.getMessage(RecastManipulatorUI.class, "RecastManipulatorUI.forceRecastCheckBox.text")); // NOI18N
        forceRecastCheckBox.setToolTipText(org.openide.util.NbBundle.getMessage(RecastManipulatorUI.class, "RecastManipulatorUI.forceRecastCheckBox.toolTipText")); // NOI18N

        replaceOriginalButton.setText(org.openide.util.NbBundle.getMessage(RecastManipulatorUI.class, "RecastManipulatorUI.replaceOriginalButton.text")); // NOI18N
        replaceOriginalButton.setEnabled(false);
        replaceOriginalButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                replaceOriginalButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(autoButton)
                            .addComponent(replaceOriginalButton)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(recastButton)
                                .addGap(18, 18, 18)
                                .addComponent(forceRecastCheckBox)))
                        .addGap(0, 16, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(convertToLabel)
                            .addComponent(currentTypeLabel)
                            .addComponent(columnLabel))
                        .addGap(65, 65, 65)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(columnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(currentTypeShowLabel)
                            .addComponent(convertToComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(columnLabel)
                    .addComponent(columnComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(currentTypeLabel)
                    .addComponent(currentTypeShowLabel))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(convertToLabel)
                    .addComponent(convertToComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(forceRecastCheckBox)
                    .addComponent(recastButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 8, Short.MAX_VALUE)
                .addComponent(replaceOriginalButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(autoButton))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void columnDublicatedEvent(String titleOld, String titleNew, String titleLabel) {
        //called only when single column was dublicated, so we should delete auto-recasting history
        if (lastOperationWasAutoRecast) {
            lastOperationWasAutoRecast = false;
            removedDuringAutoColumns.clear();
            addedDuringAutoColumns.clear();
            labelDuringAutoColumns.clear();
        }
        lastColumnBeingDuplicated = titleOld;
        lastColumnThatDuplicate = titleNew;
        lastColumnLabel = titleLabel;
        replaceOriginalButton.setEnabled(
                Lookup.getDefault().lookup(AttributeColumnsController.class).canDeleteColumn(getColumnByCheckBoxName(titleLabel)));
        replaceOriginalButton.setText("Replace " + lastColumnBeingDuplicated + " with " + lastColumnThatDuplicate);
    }
    private void recastButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_recastButtonActionPerformed
        String title = columnComboBox.getSelectedItem().toString();
        boolean duplicated = false;
        for (AttributeType type : ((ConvertToComboBoxType) (convertToComboBox.getSelectedItem())).type) {
            if (RecastImplementation.possibleToConvertColumn(getTableByCheckBoxName(title),
                    getColumnByCheckBoxName(title), type)) {
                if (RecastImplementation.recast(getTableByCheckBoxName(title),
                        getColumnByCheckBoxName(title), type, getColumnByCheckBoxName(title).getTitle() + type.toString())) {
                    columnDublicatedEvent(getColumnByCheckBoxName(title).getTitle(),
                            getColumnByCheckBoxName(title).getTitle() + type.toString(), title);
                    duplicated = true;
                    break;
                }
            }
        }
        //if we haven't duplicated column and recast is forced, try to convert without check
        if (forceRecastCheckBox.isSelected() && !duplicated) {
            for (AttributeType type : ((ConvertToComboBoxType) (convertToComboBox.getSelectedItem())).type) {
                //no possibleToConvert check, because of forceRecastCheckBox, use more euristic AttributeType.parse()
                if (RecastImplementation.recast(getTableByCheckBoxName(title),
                        getColumnByCheckBoxName(title), type, getColumnByCheckBoxName(title).getTitle() + type.toString())) {
                    columnDublicatedEvent(getColumnByCheckBoxName(title).getTitle(),
                            getColumnByCheckBoxName(title).getTitle() + type.toString(), title);
                    duplicated = true;
                    break;
                }
            }
        }
        initColumnComboBox();
    }//GEN-LAST:event_recastButtonActionPerformed

    private void autoButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_autoButtonActionPerformed
        for (int i = 0; i < columnComboBox.getItemCount(); i++) {
            String title = columnComboBox.getItemAt(i).toString();
            //recast only string type
            if (getColumnByCheckBoxName(title).getType() == AttributeType.STRING) {
                for (AttributeType type : suitableClass) {
                    if (RecastImplementation.possibleToConvertColumn(getTableByCheckBoxName(title),
                            getColumnByCheckBoxName(title), type)) {
                        if (RecastImplementation.recast(getTableByCheckBoxName(title),
                                getColumnByCheckBoxName(title), type, getColumnByCheckBoxName(title).getTitle() + type.toString())) {
                            removedDuringAutoColumns.add(getColumnByCheckBoxName(title).getTitle());
                            addedDuringAutoColumns.add(getColumnByCheckBoxName(title).getTitle() + type.toString());
                            labelDuringAutoColumns.add(title);
                            break;
                        }
                    }
                }
            }
        }
        lastOperationWasAutoRecast = true;
        replaceOriginalButton.setText("Replace all automatically recasted columns");
        replaceOriginalButton.setEnabled(true);//could be set to false during last single-column recasting
        initColumnComboBox();
    }//GEN-LAST:event_autoButtonActionPerformed

    private AttributeColumn getColumnByCheckBoxName(String name) {
        //cut EDGE_TABLE or NODE_TABLE from the end to get column title
        if (name.endsWith(NODE_TABLE)) {
            return Lookup.getDefault().lookup(AttributeController.class).getModel().
                    getNodeTable().getColumn(name.substring(0, name.length() - NODE_TABLE.length()));
        } else {
            return Lookup.getDefault().lookup(AttributeController.class).getModel().
                    getEdgeTable().getColumn(name.substring(0, name.length() - EDGE_TABLE.length()));
        }
    }

    private AttributeTable getTableByCheckBoxName(String name) {
        if (name.endsWith(NODE_TABLE)) {
            return Lookup.getDefault().lookup(AttributeController.class).getModel().getNodeTable();
        } else {
            return Lookup.getDefault().lookup(AttributeController.class).getModel().getEdgeTable();
        }
    }
    private void columnComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_columnComboBoxActionPerformed
        AttributeColumn column = getColumnByCheckBoxName(columnComboBox.getSelectedItem().toString());
        currentTypeShowLabel.setText(column.getType().toString());
    }//GEN-LAST:event_columnComboBoxActionPerformed

    private void replaceOriginalButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_replaceOriginalButtonActionPerformed
        if (!lastOperationWasAutoRecast) //just one column
        {
            RecastImplementation.move(getTableByCheckBoxName(lastColumnLabel), lastColumnBeingDuplicated, lastColumnThatDuplicate);
        } else { 
            for (int i = 0; i < removedDuringAutoColumns.size(); i++) {
                RecastImplementation.move(getTableByCheckBoxName(labelDuringAutoColumns.get(i)),
                        removedDuringAutoColumns.get(i), addedDuringAutoColumns.get(i));
            }
        }
        initColumnComboBox();
    }//GEN-LAST:event_replaceOriginalButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton autoButton;
    private javax.swing.JComboBox columnComboBox;
    private javax.swing.JLabel columnLabel;
    private javax.swing.JComboBox convertToComboBox;
    private javax.swing.JLabel convertToLabel;
    private javax.swing.JLabel currentTypeLabel;
    private javax.swing.JLabel currentTypeShowLabel;
    private javax.swing.JCheckBox forceRecastCheckBox;
    private javax.swing.JButton recastButton;
    private javax.swing.JButton replaceOriginalButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void setup(Manipulator m, DialogControls dialogControls) {
        initColumnComboBox();
    }

    @Override
    public void unSetup() {
        //do nothing, all actions can be executed through buttons
    }

    @Override
    public String getDisplayName() {
        return "Recast manupulator";
    }

    @Override
    public JPanel getSettingsPanel() {
        return this;
    }

    @Override
    public boolean isModal() {
        return true;
    }
}

class ConvertToComboBoxType {

    String title;
    AttributeType[] type;//if impossible to convert to first type, convert to second, etc

    ConvertToComboBoxType(String title, AttributeType[] type) {
        this.title = title;
        this.type = Arrays.copyOf(type, type.length);
    }

    @Override
    public String toString() {
        return title;
    }
}