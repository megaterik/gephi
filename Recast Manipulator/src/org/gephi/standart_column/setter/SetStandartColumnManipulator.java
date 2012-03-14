/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.standart_column.setter;

import javax.swing.Icon;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.general.PluginGeneralActionsManipulator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author megaterik
 */
@ServiceProvider(service = PluginGeneralActionsManipulator.class)
public class SetStandartColumnManipulator implements PluginGeneralActionsManipulator {

    @Override
    public void execute() {
    }

    @Override
    public String getName() {
        return "Set standart column";
    }

    @Override
    public String getDescription() {
        return null;
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    public ManipulatorUI getUI() {
        return new SetStandartColumnManipulatorUI();
    }

    @Override
    public int getType() {
        return 200;
    }

    @Override
    public int getPosition() {
        return 2;
    }

    @Override
    public Icon getIcon() {
        return null;
    }
}
