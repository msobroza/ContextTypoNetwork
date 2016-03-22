/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.network;

import control.NetworkControl;

/**
 *
 * @author msobroza
 */
public abstract class VirtualLevel extends Level{
    protected NetworkControl.TypeNetwork virtualLevelType;

    @Override
    public Level copy(int h) {
        return null;
    }
    
    public NetworkControl.TypeNetwork getLevelType(){
        return this.virtualLevelType;
    }
    
    

}
