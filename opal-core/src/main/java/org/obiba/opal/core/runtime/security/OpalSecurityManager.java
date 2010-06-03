/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.runtime.security;

import java.util.Set;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.Realm;
import org.apache.shiro.realm.text.IniRealm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.collect.ImmutableSet;

@Component
public class OpalSecurityManager extends DefaultSecurityManager {

  @Autowired
  public OpalSecurityManager(Set<Realm> securityRealms) {
    super(ImmutableSet.<Realm> builder().add(new IniRealm(System.getProperty("OPAL_HOME") + "/conf/shiro.ini")).addAll(securityRealms).build());
  }

  public void start() {
    SecurityUtils.setSecurityManager(this);
  }

  public void stop() {
    // Destroy the security manager.
    SecurityUtils.setSecurityManager(null);
    destroy();
  }
}
