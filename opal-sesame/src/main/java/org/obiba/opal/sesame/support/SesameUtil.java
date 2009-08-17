/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.sesame.support;

import javax.xml.namespace.QName;

import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;

/**
 *
 */
public final class SesameUtil {

  public final static URI toUri(QName qname) {
    return new URIImpl(qname.getNamespaceURI() + qname.getLocalPart());
  }

  public final static QName toQName(URI uri) {
    return new QName(uri.getNamespace(), uri.getLocalName());
  }
}
