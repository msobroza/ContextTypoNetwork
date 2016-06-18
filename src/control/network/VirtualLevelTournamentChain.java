/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.network;

import control.NetworkControl.TypeNetwork;
import tools.interface_cuda.ContextNetwork;

/**
 *
 * @author msobroza
 */
public class VirtualLevelTournamentChain extends VirtualLevel {

    private final ContextNetwork virtualSequencesNetwork;

    public VirtualLevelTournamentChain(int idLevel, int idMainLevel, int anticipationDistance, int ngrams) {
        this.virtualSequencesNetwork = new ContextNetwork(idLevel, idMainLevel, anticipationDistance, ngrams);
        this.virtualLevelType = TypeNetwork.RANDOM_SEQUENCES_NETWORK;
        this.h=idLevel;
    }
    
    public ContextNetwork getVirtualSequencesNetwork(){
        return this.virtualSequencesNetwork;
    }
    
    public int anticipationDistance(){
        return this.virtualSequencesNetwork.distance;
    }

}
