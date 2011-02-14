/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.core.service.impl;

import org.obiba.core.service.PersistenceManager;
import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.service.SubjectAclService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;

@Component
public class DefaultSubjectAclService implements SubjectAclService {

  private final PersistenceManager persistenceManager;

  @Autowired
  public DefaultSubjectAclService(@Qualifier("opal-data") PersistenceManager persistenceManager) {
    this.persistenceManager = persistenceManager;
  }

  @Override
  public void deleteNodePermissions(String domain, String node) {
    for(SubjectAcl acl : persistenceManager.match(new SubjectAcl(domain, node, null, null))) {
      persistenceManager.delete(acl);
    }
  }

  @Override
  public void addSubjectPermissions(String domain, String node, String subject, Iterable<String> permissions) {
    for(String permission : permissions) {
      addSubjectPermission(domain, node, subject, permission);
    }
  }

  @Override
  public void addSubjectPermission(String domain, String node, String subject, String permission) {
    if(subject == null) throw new IllegalArgumentException("subject cannot be null");
    if(permission == null) throw new IllegalArgumentException("permission cannot be null");
    persistenceManager.save(new SubjectAcl(domain, node, subject, permission));
  }

  @Override
  public Iterable<String> getSubjectPermissions(String domain, String node, String subject) {
    if(node == null) throw new IllegalArgumentException("node cannot be null");
    if(subject == null) throw new IllegalArgumentException("subject cannot be null");

    SubjectAcl template = new SubjectAcl(domain, node, subject, null);
    return Iterables.transform(persistenceManager.match(template), new Function<SubjectAcl, String>() {

      @Override
      public String apply(SubjectAcl from) {
        return from.getPermission();
      }

    });
  }

  @Override
  public Iterable<SubjectPermission> getSubjectPermissions(final String subject) {

    SubjectAcl template = new SubjectAcl(null, null, subject, null);
    return Iterables.transform(persistenceManager.match(template), new Function<SubjectAcl, SubjectPermission>() {

      @Override
      public SubjectPermission apply(final SubjectAcl from) {
        return new SubjectPermission() {

          @Override
          public String getDomain() {
            return from.getDomain();
          }

          @Override
          public String getNode() {
            return from.getNode();
          }

          @Override
          public Iterable<String> getPermissions() {
            return getSubjectPermissions(from.getDomain(), from.getNode(), subject);
          }

        };
      }

    });
  }

  @Override
  public Iterable<NodePermission> getNodePermissions(final String domain, final String node) {

    SubjectAcl template = new SubjectAcl(domain, node, null, null);
    return Iterables.transform(persistenceManager.match(template), new Function<SubjectAcl, NodePermission>() {

      @Override
      public NodePermission apply(final SubjectAcl from) {
        return new NodePermission() {

          @Override
          public String getDomain() {
            return from.getDomain();
          }

          @Override
          public String getSubject() {
            return from.getSubject();
          }

          @Override
          public Iterable<String> getPermissions() {
            return getSubjectPermissions(from.getDomain(), node, from.getSubject());
          }

        };
      }

    });
  }
}
