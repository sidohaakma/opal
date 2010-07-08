/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.widgets.view;

import java.util.ArrayList;
import java.util.List;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableSelectorPresenter;
import org.obiba.opal.web.gwt.app.client.widgets.presenter.TableSelectorPresenter.TableSelectionType;
import org.obiba.opal.web.model.client.DatasourceDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiTemplate;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.DockLayoutPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

/**
 *
 */
public class TableSelectorView extends DialogBox implements TableSelectorPresenter.Display {
  //
  // Constants
  //

  private static final String DIALOG_HEIGHT_MULTIPLE = "26em";

  private static final String DIALOG_HEIGHT_SINGLE = "12em";

  private static final String DIALOG_WIDTH = "30em";

  private static final String TABLE_LIST_WIDTH = "20em";

  private static final int VISIBLE_COUNT = 10;

  //
  // Static Variables
  //

  private static TableSelectorViewUiBinder uiBinder = GWT.create(TableSelectorViewUiBinder.class);

  private static Translations translations = GWT.create(Translations.class);

  //
  // Instance Variables
  //

  @UiField
  ListBox datasourceList;

  @UiField
  ListBox tableList;

  @UiField
  Button selectButton;

  @UiField
  Button cancelButton;

  @UiField
  SimplePanel tablePanel;

  @UiField
  Label instructionsLabel;

  @UiField
  Label tableLabel;

  @UiField
  CheckBox selectAllCheckbox;

  private DockLayoutPanel content;

  //
  // Constructors
  //

  public TableSelectorView() {
    setText(translations.tableSelectorTitle());
    setWidth(DIALOG_WIDTH);

    content = uiBinder.createAndBindUi(this);
    content.setWidth(DIALOG_WIDTH);
    add(content);

    selectAllCheckbox.setText(translations.selectAllLabel());
    datasourceList.setVisibleItemCount(1);
    initContent();

    addHandlers();
  }

  //
  // FileSelectorPresenter.Display Methods
  //

  @Override
  public void showDialog() {
    center();
    show();
  }

  @Override
  public void hideDialog() {
    hide();
  }

  @Override
  public void setTableSelectionType(TableSelectionType mode) {
    // tableList.setVisibleItemCount(1) does not work as supposed to
    if(mode.equals(TableSelectionType.MULTIPLE) != tableList.isMultipleSelect()) {
      ListBox newTableList = new ListBox(mode.equals(TableSelectionType.MULTIPLE));
      tablePanel.setWidget(newTableList);
      tableList = newTableList;
      initContent();
    }
  }

  @Override
  public void renderDatasources(List<DatasourceDto> datasources) {
    datasourceList.clear();
    for(DatasourceDto d : datasources) {
      datasourceList.addItem(d.getName());
    }
    if(datasources.size() > 0) {
      renderTables(datasources.get(0));
    }
  }

  @Override
  public void renderTables(DatasourceDto datasource) {
    tableList.clear();
    selectAllCheckbox.setValue(false);
    for(int i = 0; i < datasource.getTableArray().length(); i++) {
      tableList.addItem(datasource.getTableArray().get(i));
    }
  }

  public void startProcessing() {
  }

  public void stopProcessing() {
  }

  public Widget asWidget() {
    return this;
  }

  //
  // Methods
  //

  private void initContent() {
    if(tableList.isMultipleSelect()) {
      tableList.setVisibleItemCount(VISIBLE_COUNT);
      tableList.setWidth(TABLE_LIST_WIDTH);
      instructionsLabel.setText(translations.multipleTableSelectionInstructionsLabel());
      tableLabel.setText(translations.tablesLabel() + ":");
      selectAllCheckbox.setVisible(true);
    } else {
      instructionsLabel.setText(translations.singleTableSelectionInstructionsLabel());
      tableLabel.setText(translations.tableLabel() + ":");
      selectAllCheckbox.setVisible(false);
    }
    setHeight(tableList.isMultipleSelect() ? DIALOG_HEIGHT_MULTIPLE : DIALOG_HEIGHT_SINGLE);
    content.setHeight(tableList.isMultipleSelect() ? DIALOG_HEIGHT_MULTIPLE : DIALOG_HEIGHT_SINGLE);
  }

  private void addHandlers() {
    selectAllCheckbox.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        if(selectAllCheckbox.getValue()) {
          for(int i = 0; i < tableList.getItemCount(); i++) {
            tableList.setItemSelected(i, true);
          }
        } else {
          for(int i = 0; i < tableList.getItemCount(); i++) {
            tableList.setItemSelected(i, false);
          }
        }
      }
    });

    tableList.addChangeHandler(new ChangeHandler() {

      @Override
      public void onChange(ChangeEvent event) {
        selectAllCheckbox.setValue(false);
      }
    });

    selectButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        hideDialog();
      }
    });

    cancelButton.addClickHandler(new ClickHandler() {

      @Override
      public void onClick(ClickEvent event) {
        hideDialog();
      }
    });
  }

  //
  // Inner Classes / Interfaces
  //

  @UiTemplate("TableSelectorView.ui.xml")
  interface TableSelectorViewUiBinder extends UiBinder<DockLayoutPanel, TableSelectorView> {
  }

  @Override
  public HasChangeHandlers getDatasourceList() {
    return datasourceList;
  }

  @Override
  public int getSelectedDatasourceIndex() {
    return datasourceList.getSelectedIndex();
  }

  @Override
  public List<Integer> getSelectedTableIndices() {
    List<Integer> selections = new ArrayList<Integer>();

    for(int i = 0; i < tableList.getItemCount(); i++) {
      if(tableList.isItemSelected(i)) {
        selections.add(i);
      }
    }

    return selections;
  }

  @Override
  public HasClickHandlers getSelectButton() {
    return selectButton;
  }

}