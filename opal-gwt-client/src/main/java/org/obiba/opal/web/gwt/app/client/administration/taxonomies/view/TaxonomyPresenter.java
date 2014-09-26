/*
 * Copyright (c) 2014 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.taxonomies.view;

import javax.annotation.Nullable;

import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyCreatedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomyDeletedEvent;
import org.obiba.opal.web.gwt.app.client.administration.taxonomies.event.TaxonomySelectedEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationEvent;
import org.obiba.opal.web.gwt.app.client.event.ConfirmationRequiredEvent;
import org.obiba.opal.web.gwt.app.client.event.NotificationEvent;
import org.obiba.opal.web.gwt.app.client.fs.event.FileDownloadRequestEvent;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationMessages;
import org.obiba.opal.web.gwt.rest.client.ResourceCallback;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;
import org.obiba.opal.web.gwt.rest.client.UriBuilders;
import org.obiba.opal.web.model.client.opal.TaxonomyDto;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

import static com.google.gwt.http.client.Response.SC_NOT_FOUND;
import static com.google.gwt.http.client.Response.SC_OK;

public class TaxonomyPresenter extends PresenterWidget<TaxonomyPresenter.Display> implements TaxonomyUiHandlers {

  private Runnable actionRequiringConfirmation;

  private TaxonomyDto taxonomy;

  private final TranslationMessages translationMessages;

  @Inject
  public TaxonomyPresenter(Display display, EventBus eventBus, TranslationMessages translationMessages) {
    super(eventBus, display);
    this.translationMessages = translationMessages;
    getView().setUiHandlers(this);
  }

  @Override
  protected void onBind() {
    super.onBind();
    initUiComponents();
    addHandlers();
  }

  @Override
  protected void onUnbind() {
    super.onUnbind();
    taxonomy = null;
  }

  @Override
  public void onEdit() {
  }

  @Override
  public void onDownload() {
    String downloadUrl = UriBuilders.SYSTEM_CONF_TAXONOMY.create().build(taxonomy.getName()) + "/_download";
    fireEvent(new FileDownloadRequestEvent(downloadUrl));
  }

  @Override
  public void onDelete() {
    actionRequiringConfirmation = new Runnable() {
      @Override
      public void run() {
        String name = taxonomy.getName();
        ResourceRequestBuilderFactory.newBuilder() //
            .forResource(UriBuilders.SYSTEM_CONF_TAXONOMY.create().build(name)) //
            .withCallback(SC_OK, new RemoveTaxonomyResponseCallBack()) //
            .withCallback(SC_NOT_FOUND, new TaxonomyNotFoundCallBack(name)) //
            .delete().send();
      }
    };
    fireEvent(ConfirmationRequiredEvent
        .createWithMessages(actionRequiringConfirmation, translationMessages.removeTaxonomy(),
            translationMessages.confirmDeleteTaxonomy()));
  }

  //
  // Private methods
  //

  @SuppressWarnings("unchecked")
  private void initUiComponents() {
    getView().setTaxonomy(null);
  }

  private void addHandlers() {
    addRegisteredHandler(TaxonomySelectedEvent.getType(), new TaxonomySelectedEvent.TaxonomySelectedHandler() {

          @Override
          public void onTaxonomySelected(TaxonomySelectedEvent event) {
            TaxonomyDto taxonomy = event.getTaxonomy();
            if(taxonomy == null) {
              getView().setTaxonomy(null);
            } else {
              refreshTaxonomy(taxonomy);
            }
          }
        });

    addRegisteredHandler(ConfirmationEvent.getType(), new ConfirmationEventHandler());
    addRegisteredHandler(TaxonomyCreatedEvent.getType(), new TaxonomyCreatedUpdatedHandler());
    //addRegisteredHandler(TaxonomyUpdatedEvent.getType(), new TaxonomyCreatedUpdatedHandler());
  }

  private void authorize() {
    // TODO
  }

  private void refreshTaxonomy(TaxonomyDto taxonomyDto) {
    refreshTaxonomy(taxonomyDto.getName());
  }

  private void refreshTaxonomy(String name) {
    ResourceRequestBuilderFactory.<TaxonomyDto>newBuilder() //
        .forResource(UriBuilders.SYSTEM_CONF_TAXONOMY.create().build(name)) //
        .withCallback(new TaxonomyFoundCallBack()) //
        .withCallback(SC_NOT_FOUND, new TaxonomyNotFoundCallBack(name)) //
        .get().send();
  }

  private class RemoveTaxonomyResponseCallBack implements ResponseCodeCallback {
    @Override
    public void onResponseCode(Request request, Response response) {
      fireEvent(new TaxonomyDeletedEvent(taxonomy));
    }
  }

  private class TaxonomyNotFoundCallBack implements ResponseCodeCallback {

    private final String taxonomyName;

    private TaxonomyNotFoundCallBack(String taxonomyName) {
      this.taxonomyName = taxonomyName;
    }

    @Override
    public void onResponseCode(Request request, Response response) {
      fireEvent(NotificationEvent.newBuilder().error("TaxonomyCannotBeFound").args(taxonomyName).build());
    }
  }

  private class TaxonomyCreatedUpdatedHandler implements TaxonomyCreatedEvent.TaxonomyCreatedHandler {
    @Override
    public void onTaxonomyCreated(TaxonomyCreatedEvent event) {
      //refreshTaxonomy(event.);
    }
  }

  class ConfirmationEventHandler implements ConfirmationEvent.Handler {

    @Override
    public void onConfirmation(ConfirmationEvent event) {
      if(actionRequiringConfirmation != null && event.getSource().equals(actionRequiringConfirmation) &&
          event.isConfirmed()) {
        actionRequiringConfirmation.run();
        actionRequiringConfirmation = null;
      }
    }
  }

  private class TaxonomyFoundCallBack implements ResourceCallback<TaxonomyDto> {

    @Override
    public void onResource(Response response, TaxonomyDto resource) {
      taxonomy = resource;
      getView().setTaxonomy(resource);
      authorize();
    }
  }

  public interface Display extends View, HasUiHandlers<TaxonomyUiHandlers> {

    void setTaxonomy(@Nullable TaxonomyDto taxonomy);

  }

}
