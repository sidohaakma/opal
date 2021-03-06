/*
 * Copyright (c) 2018 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web.gwt.app.client.administration.datashield.presenter;

import java.util.LinkedHashSet;
import java.util.Set;

import org.obiba.opal.web.gwt.app.client.administration.datashield.event.DataShieldROptionCreatedEvent;
import org.obiba.opal.web.gwt.app.client.presenter.ModalPresenterWidget;
import org.obiba.opal.web.gwt.app.client.validator.FieldValidator;
import org.obiba.opal.web.gwt.app.client.validator.RegExValidator;
import org.obiba.opal.web.gwt.app.client.validator.RequiredTextValidator;
import org.obiba.opal.web.gwt.app.client.validator.ViewValidationHandler;
import org.obiba.opal.web.model.client.datashield.DataShieldROptionDto;

import com.google.gwt.user.client.ui.HasText;
import com.google.inject.Inject;
import com.google.web.bindery.event.shared.EventBus;
import com.gwtplatform.mvp.client.HasUiHandlers;
import com.gwtplatform.mvp.client.PopupView;

import static org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldROptionModalPresenter.Display.FormField.NAME;
import static org.obiba.opal.web.gwt.app.client.administration.datashield.presenter.DataShieldROptionModalPresenter.Display.FormField.VALUE;

public class DataShieldROptionModalPresenter extends ModalPresenterWidget<DataShieldROptionModalPresenter.Display>
    implements DataShieldROptionModalUiHandlers {

  private Mode dialogMode;

  private MethodValidationHandler validatorHandler;

  public enum Mode {
    CREATE, UPDATE
  }

  @Inject
  public DataShieldROptionModalPresenter(Display display, EventBus eventBus) {
    super(eventBus, display);
    getView().setUiHandlers(this);
  }

  @Override
  public void save() {
    getView().clearErrors();
    if(!validatorHandler.validate()) return;
    DataShieldROptionDto dto = DataShieldROptionDto.create();
    dto.setName(getView().getName().getText());
    dto.setValue(getView().getValue().getText());
    fireEvent(new DataShieldROptionCreatedEvent(dto));
    getView().hideDialog();
  }

  @Override
  public void cancel() {
    getView().hideDialog();
  }

  public void setOption(DataShieldROptionDto optionDto) {
    getView().setName(optionDto.getName());
    getView().setValue(optionDto.getValue());
    setDialogMode(Mode.UPDATE);
  }

  @Override
  protected void onBind() {
    setDialogMode(Mode.CREATE);
    validatorHandler = new MethodValidationHandler();
  }

  private void setDialogMode(Mode mode) {
    dialogMode = mode;
    getView().setDialogMode(dialogMode);
  }

  private final class MethodValidationHandler extends ViewValidationHandler {

    private MethodValidationHandler() {}

    private Set<FieldValidator> validators;

    @Override
    protected Set<FieldValidator> getValidators() {
      validators = new LinkedHashSet<>();
      validators.add(new RequiredTextValidator(getView().getName(), "DataShieldROptionNameIsRequired", NAME.name()));
      validators.add(new RegExValidator(getView().getName(), "^[A-Za-z]\\w*([\\.]*\\w*)*$", "DataShieldROptionInvalidName",
          NAME.name()));
      validators.add(new RequiredTextValidator(getView().getValue(), "DataShieldROptionValueIsRequired", VALUE.name()));
      return validators;
    }

    @Override
    protected void showMessage(String id, String message) {
      getView().showError(message, Display.FormField.valueOf(id));
    }
  }

  public interface Display extends PopupView, HasUiHandlers<DataShieldROptionModalUiHandlers> {

    enum FormField {
      NAME,
      VALUE;
    }

    void clearErrors();

    void showError(String message, FormField formField);

    void hideDialog();

    void setDialogMode(Mode dialogMode);

    void setName(String name);

    void setValue(String value);

    HasText getName();

    HasText getValue();

    void clear();
  }

}
