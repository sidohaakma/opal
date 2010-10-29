/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.report.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateDeletedEvent;
import org.obiba.opal.web.gwt.app.client.report.event.ReportTemplateSelectedEvent;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.model.client.opal.ReportTemplateDto;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.http.client.Response;
import com.google.gwt.view.client.SelectionModel.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel.SelectionChangeHandler;
import com.google.inject.Inject;

public class ReportTemplateListPresenter extends WidgetPresenter<ReportTemplateListPresenter.Display> {

  public interface Display extends WidgetDisplay {
    void setReportTemplates(JsArray<ReportTemplateDto> templates);

    ReportTemplateDto getSelectedReportTemplate();

    HandlerRegistration addSelectReportTemplateHandler(SelectionChangeHandler handler);
  }

  @Inject
  public ReportTemplateListPresenter(final Display display, final EventBus eventBus) {
    super(display, eventBus);
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
  }

  @Override
  protected void onBind() {
    initUiComponents();
    addHandlers();
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  private void initUiComponents() {
    ResourceRequestBuilderFactory.<JsArray<ReportTemplateDto>> newBuilder().forResource("/report-templates").get().withCallback(new ReportTemplatesResourceCallback()).send();
  }

  private void addHandlers() {
    super.registerHandler(getDisplay().addSelectReportTemplateHandler(new ReportTemplateSelectionChangeHandler()));
    super.registerHandler(eventBus.addHandler(ReportTemplateDeletedEvent.getType(), new ReportTemplateDeletedHandler()));
  }

  private class ReportTemplateDeletedHandler implements ReportTemplateDeletedEvent.Handler {

    @Override
    public void onReportTemplateDeleted(ReportTemplateDeletedEvent event) {
      initUiComponents();
    }

  }

  private class ReportTemplateSelectionChangeHandler implements SelectionChangeHandler {

    @Override
    public void onSelectionChange(SelectionChangeEvent event) {
      ReportTemplateDto selectedReportTemplate = getDisplay().getSelectedReportTemplate();
      if(selectedReportTemplate != null) {
        eventBus.fireEvent(new ReportTemplateSelectedEvent(selectedReportTemplate));
      }
    }

  }

  private class ReportTemplatesResourceCallback implements ResourceCallback<JsArray<ReportTemplateDto>> {

    @Override
    public void onResource(Response response, JsArray<ReportTemplateDto> templates) {
      getDisplay().setReportTemplates(templates);
    }

  }

}
