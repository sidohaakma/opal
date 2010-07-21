/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.app.client.presenter;

import net.customware.gwt.presenter.client.EventBus;
import net.customware.gwt.presenter.client.place.Place;
import net.customware.gwt.presenter.client.place.PlaceRequest;
import net.customware.gwt.presenter.client.widget.WidgetDisplay;
import net.customware.gwt.presenter.client.widget.WidgetPresenter;

import org.obiba.opal.web.gwt.app.client.event.SessionCreatedEvent;
import org.obiba.opal.web.gwt.app.client.event.SessionExpiredEvent;
import org.obiba.opal.web.gwt.rest.client.DefaultResourceRequestBuilder;
import org.obiba.opal.web.gwt.rest.client.RequestCredentials;
import org.obiba.opal.web.gwt.rest.client.ResourceRequestBuilderFactory;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;

import com.google.gwt.core.client.JavaScriptObject;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasKeyUpHandlers;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.dom.client.KeyUpHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.ui.HasValue;
import com.google.inject.Inject;

public class LoginPresenter extends WidgetPresenter<LoginPresenter.Display> {

  public interface Display extends WidgetDisplay {

    public void focusOnUserName();

    public void clear();

    public void showErrorMessage();

    public HasValue<String> getUserName();

    public HasValue<String> getPassword();

    public HasClickHandlers getSignIn();

    public HasKeyUpHandlers getUserNameTextBox();

    public HasKeyUpHandlers getPasswordTextBox();

  }

  private final RequestCredentials credentials;

  private DefaultResourceRequestBuilder<? extends JavaScriptObject> failedRequest;

  @Inject
  public LoginPresenter(Display display, EventBus eventBus, RequestCredentials credentials) {
    super(display, eventBus);
    this.credentials = credentials;
  }

  @Override
  public Place getPlace() {
    return null;
  }

  @Override
  protected void onBind() {
    display.getSignIn().addClickHandler(new ClickHandler() {
      public void onClick(ClickEvent event) {
        createSecurityResource();
      }
    });
    display.getUserNameTextBox().addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          createSecurityResource();
        }
      }
    });
    display.getPasswordTextBox().addKeyUpHandler(new KeyUpHandler() {
      @Override
      public void onKeyUp(KeyUpEvent event) {
        if(event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
          createSecurityResource();
        }
      }
    });

    super.registerHandler(eventBus.addHandler(SessionExpiredEvent.getType(), new SessionExpiredEvent.Handler() {

      @Override
      public void onSessionExpired(SessionExpiredEvent event) {
        failedRequest = event.getFailedRequest();
      }
    }));
  }

  private void createSecurityResource() {
    createSecurityResource(display.getUserName().getValue(), display.getPassword().getValue());
  }

  @Override
  protected void onPlaceRequest(PlaceRequest request) {
  }

  @Override
  protected void onUnbind() {
  }

  @Override
  public void refreshDisplay() {
  }

  @Override
  public void revealDisplay() {
    display.focusOnUserName();
  }

  private void createSecurityResource(String username, String password) {
    ResponseCodeCallback authError = new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        display.showErrorMessage();
      }
    };

    ResourceRequestBuilderFactory.newBuilder().forResource("/auth/sessions").post().withCallback(403, authError).withCallback(401, authError).withCallback(201, new ResponseCodeCallback() {

      @Override
      public void onResponseCode(Request request, Response response) {
        // When a 201 happens, we should have credentials, but we'll test anyway.
        if(credentials.hasCredentials()) {
          display.clear();
          eventBus.fireEvent(new SessionCreatedEvent(response.getHeader("Location")));
          resendFailedRequest();
        } else {
          display.showErrorMessage();
        }
      }
    }).withFormBody("username", username, "password", password).send();
  }

  /**
   * If a request required authentication, re-send that same request after the user has been authenticated.
   */
  private void resendFailedRequest() {
    if(failedRequest != null) {
      failedRequest.send();
      failedRequest = null;
    }
  }
}
