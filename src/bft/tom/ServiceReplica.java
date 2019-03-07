/**
 * Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and
 * the authors indicated in the @author tags
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package bft.tom;

public class ServiceReplica {
    
    // replica ID
    private int id;	
    // Server side comunication system
    private ServerCommunicationSystem cs = null;
    private ServerViewController SVController;

    /**
     * Constructor
     *
     * @param id Replica ID
     */
    public ServiceReplica(int id) {
        this(id, "", null);
    }

    /**
     * Constructor
     *
     * @param id Replica ID
     * @param configHome Configuration directory for BFT-SMART
     * @param loader Used to load signature keys from disk
     */
    public ServiceReplica(int id, String configHome, KeyLoader loader) {
        this.id = id;
        this.SVController = new ServerViewController(id, configHome, loader);
        this.init();
    }

    // this method initializes the object
    private void init() {
        try {
            cs = new ServerCommunicationSystem(this.SVController, this);
        } catch (Exception ex) {
            logger.error("Failed to initialize replica-to-replica communication system", ex);
            throw new RuntimeException("Unable to build a communication system.");
        }
        initReplica();
    }

    private void initReplica() {
        cs.start();
    }

}