/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.gephi.recast_manipulator;

import javax.swing.Icon;
import org.gephi.data.attributes.api.AttributeColumn;
import org.gephi.data.attributes.api.*;
import org.gephi.data.attributes.api.AttributeModel;
import org.gephi.data.attributes.api.AttributeTable;
import org.gephi.datalab.api.AttributeColumnsController;
import org.gephi.datalab.spi.ManipulatorUI;
import org.gephi.datalab.spi.general.GeneralActionsManipulator;
import org.openide.util.Lookup;
import org.openide.util.lookup.ServiceProvider;

/**
 *
 * @author megaterik
 */
@ServiceProvider(service = GeneralActionsManipulator.class)
public class RecastManipulator implements GeneralActionsManipulator{

    @Override
    public void execute() {
        //all actions are performed in ui
    }

    @Override
    public String getName() {
        return "Re-cast column";
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
        return new RecastManipulatorUI();
    }

    @Override
    public int getType() {
        return 300;
    }

    @Override
    public int getPosition() {
        return 0;
    }

    @Override
    public Icon getIcon() {
        return null;
    }
    
}
