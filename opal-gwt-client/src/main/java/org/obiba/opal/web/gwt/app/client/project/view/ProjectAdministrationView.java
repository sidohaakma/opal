/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.project.view;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.project.presenter.ProjectAdministrationPresenter;
import org.obiba.opal.web.model.client.magma.DatasourceDto;
import org.obiba.opal.web.model.client.opal.ProjectDto;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.HasText;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.ViewImpl;

public class ProjectAdministrationView extends ViewImpl implements ProjectAdministrationPresenter.Display {

  interface Binder extends UiBinder<Widget, ProjectAdministrationView> {}

  @UiField
  HasText name;

  @UiField
  ListBox datasourceType;

  @Inject
  public ProjectAdministrationView(Binder uiBinder, Translations translations) {
    initWidget(uiBinder.createAndBindUi(this));
    for(int i = 0; i < datasourceType.getItemCount(); i++) {
      datasourceType.setItemText(i, translations.datasourceTypeMap().get(datasourceType.getValue(i)));
    }
  }

  @Override
  public void setProject(ProjectDto project) {
    name.setText(project.getName());
    DatasourceDto datasource = project.getDatasource();
    for(int i = 0; i < datasourceType.getItemCount(); i++) {
      if (datasourceType.getValue(i).equals(datasource.getType())) {
        datasourceType.setSelectedIndex(i);
        break;
      }
    }
  }

  @UiHandler("saveIdentification")
  void onSaveIdentification(ClickEvent event) {

  }

  @UiHandler("saveStorage")
  void onSaveStorage(ClickEvent event) {

  }

  @UiHandler("deleteProject")
  void onDeleteProject(ClickEvent event) {

  }

}
