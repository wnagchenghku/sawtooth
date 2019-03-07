/**
Copyright (c) 2007-2013 Alysson Bessani, Eduardo Alchieri, Paulo Sousa, and the authors indicated in the @author tags

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package bft.reconfiguration;

import java.net.SocketAddress;

import bft.reconfiguration.util.TOMConfiguration;
import bft.reconfiguration.views.View;
import java.security.Provider;

/**
 *
 * @author eduardo
 */
public class ViewController {

    protected View currentView = null;
    private TOMConfiguration staticConf;

    public ViewController(int procId) {
        this.staticConf = new TOMConfiguration(procId);
    }

    
    public ViewController(int procId, String configHome) {
        this.staticConf = new TOMConfiguration(procId, configHome);
    }

    public View getCurrentView(){
        return this.currentView;
    }

    public void reconfigureTo(View newView) {
        this.currentView = newView;
    }

    public TOMConfiguration getStaticConf() {
        return staticConf;
    }

    public boolean isCurrentViewMember(int id) {
        return getCurrentView().isMember(id);
    }
}