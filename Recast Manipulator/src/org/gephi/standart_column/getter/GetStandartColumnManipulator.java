/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.standart_column.getter;

import javax.swing.Icon;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.general.PluginGeneralActionsManipulator;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author megaterik
 */
@ServiceProvider(service = PluginGeneralActionsManipulator.class)
public class GetStandartColumnManipulator implements PluginGeneralActionsManipulator{

    @Override
    public void execute() {
        
    }

    @Override
    public String getName() {
        return "Get standart column";
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
        return new GetStandartColumnManipulatorUI();
    }

    @Override
    public int getType() {
        return 200;
    }

    @Override
    public int getPosition() {
        return 1;
    }

    @Override
    public Icon getIcon() {
        return null;
    }
    
}
