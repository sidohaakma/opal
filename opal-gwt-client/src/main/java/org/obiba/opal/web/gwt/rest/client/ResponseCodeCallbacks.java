/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.rest.client;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;

public final class ResponseCodeCallbacks {

  public final static ResponseCodeCallback NO_OP = new ResponseCodeCallback() {
    @Override
    public void onResponseCode(Request request, Response response) {

    }
  };

  private ResponseCodeCallbacks() {
  }

}
