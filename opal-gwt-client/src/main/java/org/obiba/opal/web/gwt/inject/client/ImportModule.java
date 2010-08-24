/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.gwt.inject.client;

import org.obiba.opal.web.gwt.app.client.wizard.importdata.ImportData;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.ConclusionStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.CsvFormatStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.DestinationSelectionStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.FormatSelectionStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.IdentityArchiveStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.ValidationReportStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.presenter.XmlFormatStepPresenter;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.view.ConclusionStepView;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.view.CsvFormatStepView;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.view.DestinationSelectionStepView;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.view.FormatSelectionStepView;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.view.IdentityArchiveStepView;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.view.ValidationReportStepView;
import org.obiba.opal.web.gwt.app.client.wizard.importdata.view.XmlFormatStepView;

import com.google.gwt.inject.client.AbstractGinModule;
import com.google.inject.Singleton;

/**
 * Bind concrete implementations to interfaces within the import wizard.
 */
public class ImportModule extends AbstractGinModule {

  @Override
  protected void configure() {
    bind(ImportData.class).in(Singleton.class);

    bind(FormatSelectionStepPresenter.Display.class).to(FormatSelectionStepView.class).in(Singleton.class);
    bind(CsvFormatStepPresenter.Display.class).to(CsvFormatStepView.class).in(Singleton.class);
    bind(DestinationSelectionStepPresenter.Display.class).to(DestinationSelectionStepView.class).in(Singleton.class);
    bind(XmlFormatStepPresenter.Display.class).to(XmlFormatStepView.class).in(Singleton.class);
    bind(ValidationReportStepPresenter.Display.class).to(ValidationReportStepView.class).in(Singleton.class);
    bind(IdentityArchiveStepPresenter.Display.class).to(IdentityArchiveStepView.class).in(Singleton.class);
    bind(ConclusionStepPresenter.Display.class).to(ConclusionStepView.class).in(Singleton.class);
  }

}
