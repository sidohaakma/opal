/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.keystore.support;

import javax.annotation.Nonnull;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.i18n.TranslationsUtils;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.KeyPairDisplay;
import org.obiba.opal.web.gwt.app.client.keystore.presenter.KeyPairModalSavedHandler;
import org.obiba.opal.web.gwt.app.client.support.ClientErrorDtos;
import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;

import static com.google.gwt.http.client.Response.SC_CREATED;
import static com.google.gwt.http.client.Response.SC_OK;

/**
 * Created by rhaeri on 16/12/13.
*/
public class KeyPairModalResponseCallback<T> implements ResponseCodeCallback {
  private final String defaultMessage;

  private final KeyPairModalSavedHandler savedHandler;

  private KeyPairDisplay<T> keypairDisplay;

  private static final Translations translations = GWT.create(Translations.class);

  public KeyPairModalResponseCallback(@Nonnull KeyPairDisplay<T> display, @Nonnull KeyPairModalSavedHandler handler,
      @Nonnull String message) {
    defaultMessage = message;
    keypairDisplay = display;
    savedHandler = handler;
  }

  @Override
  public void onResponseCode(Request request, Response response) {
    int statusCode = response.getStatusCode();
    if(statusCode == SC_OK || statusCode == SC_CREATED) {
      if(savedHandler != null) savedHandler.saved();
      keypairDisplay.close();
    } else {
      String msg = ClientErrorDtos.getStatus(response.getText());
      keypairDisplay.showError(null, TranslationsUtils.replaceArguments(translations.userMessageMap().get(msg)));
    }
  }
}
