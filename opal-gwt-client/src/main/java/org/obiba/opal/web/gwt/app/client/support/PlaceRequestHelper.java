/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.support;

import java.util.List;

import org.obiba.opal.web.gwt.app.client.place.ParameterTokens;

import com.gwtplatform.mvp.shared.proxy.PlaceRequest;

public class PlaceRequestHelper {

  private PlaceRequestHelper() {
  }

  public static PlaceRequest.Builder createRequestBuilder(String nameToken) {
    return new PlaceRequest.Builder().nameToken(nameToken);
  }

  public static PlaceRequest.Builder createRequestBuilder(PlaceRequest request) {
    PlaceRequest.Builder builder = createRequestBuilderWithNameToken(request);

    for (String param : request.getParameterNames()) {
      builder.with(param, request.getParameter(param, ""));
    }

    return builder;
  }

  public static PlaceRequest.Builder createRequestBuilderWithParams(PlaceRequest request, List<String> included) {
    PlaceRequest.Builder builder = createRequestBuilderWithNameToken(request);

    for (String param : request.getParameterNames()) {
      if (included.indexOf(param) != -1) {
        builder.with(param, request.getParameter(param, ""));
      }
    }

    return builder;
  }

  private static PlaceRequest.Builder createRequestBuilderWithNameToken(PlaceRequest request) {
    return new PlaceRequest.Builder().nameToken(request.getNameToken());
  }
}
