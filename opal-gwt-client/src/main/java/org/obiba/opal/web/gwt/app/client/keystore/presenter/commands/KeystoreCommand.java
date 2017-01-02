/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.keystore.presenter.commands;

import org.obiba.opal.web.gwt.rest.client.ResponseCodeCallback;

import edu.umd.cs.findbugs.annotations.Nullable;

public interface KeystoreCommand {
  void execute(@Nullable ResponseCodeCallback success, @Nullable ResponseCodeCallback failure);
}
