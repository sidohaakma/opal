/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.sesame.report;

import java.util.Set;

import org.obiba.opal.elmo.concepts.DataItem;

public interface DataItemSet {

  public String getName();

  public boolean hasOccurrence();

  public Set<DataItem> getDataItems();

}
