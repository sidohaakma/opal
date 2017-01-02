/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.reporting.service;

import java.util.Map;

import org.obiba.opal.core.runtime.Service;

public interface ReportService extends Service {

  void render(String format, Map<String, String> parameters, String reportDesign, String reportOutput)
      throws ReportException;

}
