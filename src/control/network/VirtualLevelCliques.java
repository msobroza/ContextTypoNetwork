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
public class VirtualLevelCliques extends VirtualLevel {

    private int maxAnticipationDistance;

    public VirtualLevelCliques(int idLevel) {
        this.virtualLevelType = NetworkControl.TypeNetwork.RANDOM_CLIQUES_NETWORK;
        this.h = idLevel;
        this.maxAnticipationDistance = 0;
    }
    
    public void increaseMaxAnticipationDistance(){
        this.maxAnticipationDistance++;
    }

    public int getMaxAnticipationDistance() {
        return this.maxAnticipationDistance;
    }

}
