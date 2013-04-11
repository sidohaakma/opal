/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.navigator.event;

import org.obiba.opal.web.model.client.magma.TableDto;
import org.obiba.opal.web.model.client.magma.VariableDto;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;

/**
 *
 */
public class VariableSelectionChangeEvent extends GwtEvent<VariableSelectionChangeEvent.Handler> {

  public interface Handler extends EventHandler {

    void onVariableSelectionChanged(VariableSelectionChangeEvent event);

  }

  private static final Type<Handler> TYPE = new Type<Handler>();

  private final TableDto table;

  private final VariableDto selection;

  private final VariableDto previous;

  private final VariableDto next;

  /**
   * @param selectedItem
   */
  public VariableSelectionChangeEvent(TableDto table, VariableDto selectedItem) {
    this(table, selectedItem, null, null);
  }

  public VariableSelectionChangeEvent(TableDto table, VariableDto selectedItem, VariableDto previous,
      VariableDto next) {
    this.table = table;
    selection = selectedItem;
    this.previous = previous;
    this.next = next;
  }

  public TableDto getTable() {
    return table;
  }

  public VariableDto getSelection() {
    return selection;
  }

  public VariableDto getPrevious() {
    return previous;
  }

  public VariableDto getNext() {
    return next;
  }

  public static Type<Handler> getType() {
    return TYPE;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onVariableSelectionChanged(this);
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return getType();
  }
}
