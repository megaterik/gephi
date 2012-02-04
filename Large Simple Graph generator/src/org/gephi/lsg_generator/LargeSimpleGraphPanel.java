/*
Copyright 2008-2011 Gephi
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
package org.gephi.lsg_generator;

import java.lang.Integer;
import java.util.HashMap;
import org.gephi.lib.validation.ValidationClient;
import org.gephi.statistics.plugin.ChartUtils;
import org.gephi.ui.components.SimpleHTMLReport;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.netbeans.validation.api.builtin.Validators;
import org.netbeans.validation.api.ui.ValidationGroup;
import org.netbeans.validation.api.ui.ValidationPanel;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author megaterik
 */
public class LargeSimpleGraphPanel extends javax.swing.JPanel implements ValidationClient {

    SimpleHTMLReport htmlReport;

    /**
     * Creates new customizer LargeSimpleGraphPanel
     */
    public LargeSimpleGraphPanel() {
        initComponents();
        showButtonGroup.add(showDensityRadioButton);
        showButtonGroup.add(showCumulativeDensityRadioButton);
        showButtonGroup.add(showBothRadioButton);
    }

    void getFields(LargeSimpleGraph generator) {
        try {
            generator.setExponent(Double.parseDouble(exponentTextField.getText()));
            generator.setMaxDegree(Integer.parseInt(maxDegreeTextField.getText()));
            generator.setMinDegree(Integer.parseInt(minDegreeTextField.getText()));
            generator.setNumberOfNodes(Integer.parseInt(nodesTextField.getText()));
            generator.setShuffleRate(Double.parseDouble(shuffleTextField.getText()));
            if (showDensityRadioButton.isSelected()) {
                generator.setTypeOfReport(previewType.DEGREE_REPORT);
            } else if (showCumulativeDensityRadioButton.isSelected()) {
                generator.setTypeOfReport(previewType.CUMULATIVE_DEGREE_REPORT);
            } else if (showBothRadioButton.isSelected()) {
                generator.setTypeOfReport(previewType.BOTH_REPORT);
            }
        } catch (Exception ex) {
        }
    }

    void setFields(LargeSimpleGraph generator) {
        exponentTextField.setText(Double.toString(generator.getExponent()));
        minDegreeTextField.setText(Integer.toString(generator.getMinDegree()));;
        maxDegreeTextField.setText(Integer.toString(generator.getMaxDegree()));
        nodesTextField.setText(Integer.toString(generator.getNumberOfNodes()));
        shuffleTextField.setText(Double.toString(generator.getShuffleRate()));

        switch (generator.getTypeOfReport()) {
            case DEGREE_REPORT:
                showDensityRadioButton.setSelected(true);
                break;
            case CUMULATIVE_DEGREE_REPORT:
                showCumulativeDensityRadioButton.setSelected(true);
                break;
            case BOTH_REPORT:
                showBothRadioButton.setSelected(true);
                break;
        }
        updateExample(true);
    }

    void updateExample(boolean recalc) {
        if (recalc) {
            generateReport();
        }
        jEditorPane1.setContentType("text/html;");
        if (showBothRadioButton.isSelected()) {
            jEditorPane1.setText(degreeBothReport);
        } else if (showDensityRadioButton.isSelected()) {
            jEditorPane1.setText(degreeReport);
        } else if (showCumulativeDensityRadioButton.isSelected()) {
            jEditorPane1.setText(cumulativeDegreeReport);
        }
        jEditorPane1.setCaretPosition(0);
    }
    //html code with png picture of table for example button
    private String degreeReport;
    private String cumulativeDegreeReport;
    private String degreeBothReport;

    void generateReport() {
        HashMap<Integer, Integer> degreeDist = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> degreeCumulativeDist = new HashMap<Integer, Integer>();


        HashMap<Integer, Integer> degreeDist2 = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> degreeCumulativeDist2 = new HashMap<Integer, Integer>();

        int minDegree = Math.max(1, Integer.parseInt(minDegreeTextField.getText()));
        int maxDegree = Math.min(Integer.parseInt(nodesTextField.getText()) - 1, Integer.parseInt(maxDegreeTextField.getText()));
        int[] count = new int[maxDegree + 1];
        DistributionGenerator random = new DistributionGenerator();
        int n = Integer.parseInt(nodesTextField.getText());
        for (int i = 0; i < n; i++) {
            count[random.nextPowerLaw(minDegree, maxDegree, Double.parseDouble(exponentTextField.getText()))]++;
            }
        long sumOfEdges = 0;
        int sumOfNodes = 0;
        for (int i = 0; i <= maxDegree; i++) {
            if (count[i] > 0) {
                sumOfEdges += i * count[i];
                sumOfNodes += count[i];
                degreeDist.put(i, count[i]);
                degreeCumulativeDist.put(i, sumOfNodes);
            }
        }

        String degreeImageFile;
        String cumulativeDegreeImageFile;
        String report = "";
        {
            //Distribution series
            XYSeries dSeries = ChartUtils.createXYSeries(degreeDist, "Degree Distribution");

            XYSeriesCollection dataset1 = new XYSeriesCollection();
            dataset1.addSeries(dSeries);

            JFreeChart chart1 = ChartFactory.createXYLineChart(
                    "Degree Distribution",
                    "Degree",
                    "Count",
                    dataset1,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    true);
            ChartUtils.decorateChart(chart1);
            ChartUtils.scaleChart(chart1, dSeries, false);
            degreeImageFile = ChartUtils.renderChart(chart1, "w-degree-distribution.png");
        }

        {
            //Distribution series
            XYSeries dSeries = ChartUtils.createXYSeries(degreeCumulativeDist, "Degree Distribution");

            XYSeriesCollection dataset1 = new XYSeriesCollection();
            dataset1.addSeries(dSeries);

            JFreeChart chart1 = ChartFactory.createXYLineChart(
                    "Degree Distribution",
                    "Degree less or equal then",
                    "Count",
                    dataset1,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    true);
            ChartUtils.decorateChart(chart1);
            ChartUtils.scaleChart(chart1, dSeries, false);
            cumulativeDegreeImageFile = ChartUtils.renderChart(chart1, "w-degree-distribution-cumulative.png");
        }
        degreeBothReport = "<HTML> <BODY> Example of report "
                + "<hr>"
                + "<br> Average degree: " + (((double) sumOfEdges) / n) + ""
                + "<br> Undirected edges: " + sumOfEdges / 2 + ""
                + "<br /><br />" + cumulativeDegreeImageFile
                + "<br /><br />" + degreeImageFile
                + "</BODY></HTML>";

        degreeReport = "<HTML> <BODY> Example of report "
                + "<hr>"
                + "<br> Average degree: " + (((double) sumOfEdges) / n) + ""
                + "<br> Undirected edges: " + sumOfEdges / 2 + ""
                + "<br /><br />" + degreeImageFile
                + "</BODY></HTML>";

        cumulativeDegreeReport = "<HTML> <BODY> Example of report "
                + "<hr>"
                + "<br> Average degree: " + (((double) sumOfEdges) / n) + ""
                + "<br> Undirected edges: " + sumOfEdges / 2 + ""
                + "<br /><br />" + cumulativeDegreeImageFile
                + "</BODY></HTML>";
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the FormEditor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        showButtonGroup = new javax.swing.ButtonGroup();
        nodesTextField = new javax.swing.JTextField();
        minDegreeTextField = new javax.swing.JTextField();
        maxDegreeTextField = new javax.swing.JTextField();
        exponentTextField = new javax.swing.JTextField();
        nodesLabel = new javax.swing.JLabel();
        minDegreeLabel = new javax.swing.JLabel();
        maxDegreeLabel = new javax.swing.JLabel();
        exponentLabel = new javax.swing.JLabel();
        exampleButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jEditorPane1 = new javax.swing.JEditorPane();
        showDensityRadioButton = new javax.swing.JRadioButton();
        showCumulativeDensityRadioButton = new javax.swing.JRadioButton();
        showBothRadioButton = new javax.swing.JRadioButton();
        shuffleLabel = new javax.swing.JLabel();
        shuffleTextField = new javax.swing.JTextField();

        nodesLabel.setText(org.openide.util.NbBundle.getMessage(LargeSimpleGraphPanel.class, "nodeLabel.text")); // NOI18N

        minDegreeLabel.setText(org.openide.util.NbBundle.getMessage(LargeSimpleGraphPanel.class, "minDegreeLabel.text")); // NOI18N

        maxDegreeLabel.setText(org.openide.util.NbBundle.getMessage(LargeSimpleGraphPanel.class, "maxDegreeLabel.text")); // NOI18N

        exponentLabel.setText(org.openide.util.NbBundle.getMessage(LargeSimpleGraphPanel.class, "exponentLabel.text")); // NOI18N

        exampleButton.setText(org.openide.util.NbBundle.getMessage(LargeSimpleGraphPanel.class, "ExampleButton.text")); // NOI18N
        exampleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exampleButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setViewportView(jEditorPane1);

        showDensityRadioButton.setText("Show degree report");
        showDensityRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showDensityRadioButtonActionPerformed(evt);
            }
        });

        showCumulativeDensityRadioButton.setText("Show cumulative degree report");
        showCumulativeDensityRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showCumulativeDensityRadioButtonActionPerformed(evt);
            }
        });

        showBothRadioButton.setText("Show both");
        showBothRadioButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showBothRadioButtonActionPerformed(evt);
            }
        });

        shuffleLabel.setText("Shuffle ratio");
        shuffleLabel.setToolTipText("Number of shuffles/number of edges. Recommended ratio is 1. Decrease to get less random graph faster. Values higher than 1 doesn't improve result.");

        shuffleTextField.setToolTipText("Number of shuffles/number of edges. Decrease to get less random graph faster.  Setting higher than 1 doesn't improve results.");

        org.jdesktop.layout.GroupLayout layout = new org.jdesktop.layout.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING, false)
                        .add(exampleButton)
                        .add(showDensityRadioButton)
                        .add(layout.createSequentialGroup()
                            .add(nodesLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(nodesTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(showBothRadioButton)
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                            .add(maxDegreeLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(maxDegreeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                            .add(exponentLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(exponentTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(org.jdesktop.layout.GroupLayout.TRAILING, layout.createSequentialGroup()
                            .add(shuffleLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(shuffleTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                        .add(layout.createSequentialGroup()
                            .add(minDegreeLabel)
                            .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .add(minDegreeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, 73, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)))
                    .add(showCumulativeDensityRadioButton))
                .add(27, 27, 27)
                .add(jScrollPane1))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
            .add(layout.createSequentialGroup()
                .addContainerGap()
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(nodesLabel)
                    .add(nodesTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(minDegreeLabel)
                    .add(minDegreeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(23, 23, 23)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(maxDegreeLabel)
                    .add(maxDegreeTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.LEADING)
                    .add(exponentTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(exponentLabel))
                .add(18, 18, 18)
                .add(layout.createParallelGroup(org.jdesktop.layout.GroupLayout.BASELINE)
                    .add(shuffleTextField, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE, org.jdesktop.layout.GroupLayout.DEFAULT_SIZE, org.jdesktop.layout.GroupLayout.PREFERRED_SIZE)
                    .add(shuffleLabel))
                .addPreferredGap(org.jdesktop.layout.LayoutStyle.RELATED, 53, Short.MAX_VALUE)
                .add(showDensityRadioButton)
                .add(18, 18, 18)
                .add(showCumulativeDensityRadioButton)
                .add(18, 18, 18)
                .add(showBothRadioButton)
                .add(74, 74, 74)
                .add(exampleButton)
                .addContainerGap())
            .add(jScrollPane1)
        );
    }// </editor-fold>//GEN-END:initComponents

private void exampleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exampleButtonActionPerformed
    updateExample(true);
    //htmlReport = new SimpleHTMLReport(WindowManager.getDefault().getMainWindow(), generateReport());
}//GEN-LAST:event_exampleButtonActionPerformed

    private void showDensityRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showDensityRadioButtonActionPerformed
        updateExample(false);
    }//GEN-LAST:event_showDensityRadioButtonActionPerformed

    private void showCumulativeDensityRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showCumulativeDensityRadioButtonActionPerformed
        updateExample(false);
    }//GEN-LAST:event_showCumulativeDensityRadioButtonActionPerformed

    private void showBothRadioButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showBothRadioButtonActionPerformed
        updateExample(false);
    }//GEN-LAST:event_showBothRadioButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton exampleButton;
    private javax.swing.JLabel exponentLabel;
    private javax.swing.JTextField exponentTextField;
    private javax.swing.JEditorPane jEditorPane1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel maxDegreeLabel;
    private javax.swing.JTextField maxDegreeTextField;
    private javax.swing.JLabel minDegreeLabel;
    private javax.swing.JTextField minDegreeTextField;
    private javax.swing.JLabel nodesLabel;
    private javax.swing.JTextField nodesTextField;
    private javax.swing.JRadioButton showBothRadioButton;
    private javax.swing.ButtonGroup showButtonGroup;
    private javax.swing.JRadioButton showCumulativeDensityRadioButton;
    private javax.swing.JRadioButton showDensityRadioButton;
    private javax.swing.JLabel shuffleLabel;
    private javax.swing.JTextField shuffleTextField;
    // End of variables declaration//GEN-END:variables

    public static ValidationPanel createValidationPanel(LargeSimpleGraphPanel innerPanel) {
        ValidationPanel validationPanel = new ValidationPanel();
        validationPanel.setInnerComponent(innerPanel);

        ValidationGroup group = validationPanel.getValidationGroup();
        innerPanel.validate(group);

        return validationPanel;
    }

    @Override
    public void validate(ValidationGroup group) {
        group.add(minDegreeTextField, Validators.REQUIRE_NON_EMPTY_STRING, Validators.REQUIRE_VALID_INTEGER, Validators.numberRange(1, Integer.MAX_VALUE));
        group.add(maxDegreeTextField, Validators.REQUIRE_NON_EMPTY_STRING, Validators.REQUIRE_VALID_INTEGER, Validators.numberRange(1, Integer.MAX_VALUE));
        group.add(exponentTextField, Validators.REQUIRE_NON_EMPTY_STRING, Validators.REQUIRE_VALID_NUMBER);
        group.add(nodesTextField, Validators.REQUIRE_NON_EMPTY_STRING, Validators.REQUIRE_VALID_INTEGER, Validators.numberRange(1, Integer.MAX_VALUE));
    }
}

enum previewType {

    DEGREE_REPORT, CUMULATIVE_DEGREE_REPORT, BOTH_REPORT
}
