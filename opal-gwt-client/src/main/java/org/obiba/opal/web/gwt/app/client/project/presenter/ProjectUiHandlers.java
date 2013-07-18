package org.obiba.opal.web.gwt.app.client.project.presenter;

import com.gwtplatform.mvp.client.UiHandlers;

public interface ProjectUiHandlers extends UiHandlers {

  void onProjectsSelection();

  void onDatasourceSelection(String name);

  void onTableSelection(String datasource, String table);

}
