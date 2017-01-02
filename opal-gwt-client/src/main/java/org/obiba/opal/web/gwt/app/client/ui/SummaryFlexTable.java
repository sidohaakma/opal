/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.gwt.app.client.ui;

import java.util.Collection;

import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.gwt.app.client.magma.view.SummaryTabView;
import org.obiba.opal.web.model.client.math.FrequencyDto;

import com.github.gwtbootstrap.client.ui.Icon;
import com.github.gwtbootstrap.client.ui.constants.IconType;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.ui.Label;

public class SummaryFlexTable extends DefaultFlexTable {

  private static final Translations translations = GWT.create(Translations.class);

  private int row = 0;

  public int drawHeader() {

    getFlexCellFormatter().addStyleName(row, 0, "bold-table-header");
    getFlexCellFormatter().addStyleName(row, 1, "bold-table-header");
    getFlexCellFormatter().addStyleName(row, 2, "bold-table-header");
    getFlexCellFormatter().setColSpan(row, 2, 2);
    getFlexCellFormatter().setRowSpan(row, 0, 2);
    getFlexCellFormatter().setWidth(row, 1, "70px");
    setWidget(row, 0, new Label(translations.value()));
    getFlexCellFormatter().setRowSpan(row, 1, 2);
    setWidget(row, 1, new Label(translations.frequency()));
    setWidget(row++, 2, new Label("Percentage"));

    getFlexCellFormatter().addStyleName(row, 0, "bold-table-header");
    getFlexCellFormatter().addStyleName(row, 0, "table-subheader");
    getFlexCellFormatter().setWidth(row, 0, "70px");
    getFlexCellFormatter().addStyleName(row, 1, "bold-table-header");
    getFlexCellFormatter().addStyleName(row, 1, "table-subheader");
    getFlexCellFormatter().setWidth(row, 1, "70px");

    setWidget(row, 0, new Label(translations.subtotal()));
    setWidget(row++, 1, new Label(translations.totalLabel()));

    return row;
  }

  public void drawValuesFrequencies(Collection<FrequencyDto> frequencies, String title, String emptyValueLabel,
      double subtotal, double totalOther, double total) {
    getFlexCellFormatter().setColSpan(row, 0, 4);
    getFlexCellFormatter().addStyleName(row, 0, "table-subheader");
    setWidget(row++, 0, new Label(title));

    // If no frequencies, show no values...
    if(frequencies.isEmpty()) {
      drawRow(emptyValueLabel, "0", "0%", "0%");
    } else {
      for(FrequencyDto frequency : frequencies) {
        if(frequency.hasValue()) {
          drawRow(SummaryTabView.NOT_NULL_VALUE.equals(frequency.getValue())
              ? translations.notEmpty()
              : frequency.getValue(), String.valueOf(Math.round(frequency.getFreq())),
              getPercentage(frequency.getFreq(), subtotal), formatDecimal(frequency.getPct() * 100) + "%");
        }
      }
    }

    drawOtherValuesRow(totalOther, getPercentage(totalOther, subtotal), getPercentage(totalOther, total));
    drawSubtotal(frequencies, subtotal, total);
  }

  private void drawOtherValuesRow(double frequency, String subtotal, String total) {
    if(frequency > 0) {
      setWidget(row, 0, new Icon(IconType.ELLIPSIS_HORIZONTAL));
      setWidget(row, 1, new Label(String.valueOf(Math.round(frequency))));
      setWidget(row, 2, new Label(subtotal));
      setWidget(row++, 3, new Label(total));
    }
  }

  public void drawValuesFrequencies(Collection<FrequencyDto> frequencies, String title, String emptyValueLabel,
      double subtotal, double total) {
    getFlexCellFormatter().setColSpan(row, 0, 4);
    getFlexCellFormatter().addStyleName(row, 0, "table-subheader");
    setWidget(row++, 0, new Label(title));

    // If no frequencies, show no values...
    if(frequencies.isEmpty()) {
      drawRow(emptyValueLabel, "0", "0%", "0%");
    } else {
      for(FrequencyDto frequency : frequencies) {
        if(frequency.hasValue()) {
          if(SummaryTabView.OTHER_VALUES.equals(frequency.getValue())) {
            drawOtherValuesRow(frequency.getFreq(), String.valueOf(getPercentage(frequency.getFreq(), subtotal)),
                String.valueOf(getPercentage(frequency.getFreq(), total)));
          } else {
            drawRow(SummaryTabView.NOT_NULL_VALUE.equals(frequency.getValue())
                ? translations.notEmpty()
                : frequency.getValue(), String.valueOf(Math.round(frequency.getFreq())),
                getPercentage(frequency.getFreq(), subtotal), formatDecimal(frequency.getPct() * 100) + "%");
          }
        }
      }
    }

    drawSubtotal(frequencies, subtotal, total);
  }

  private void drawRow(String col1, String col2, String col3, String col4) {
    setWidget(row, 0, new Label(col1));
    setWidget(row, 1, new Label(col2));
    setWidget(row, 2, new Label(col3));
    setWidget(row++, 3, new Label(col4));
  }

  private void drawSubtotal(Collection<FrequencyDto> frequencies, double subtotal, double total) {

    // Do not show subtotal when there is only 1 frequency value
    if(frequencies.size() > 1) {
      getFlexCellFormatter().addStyleName(row, 0, "table-subtotal");
      drawRow(translations.subtotal(), String.valueOf(Math.round(subtotal)), getPercentage(subtotal, subtotal),
          getPercentage(subtotal, total));
    }
  }

  public void drawTotal(double total) {
    getFlexCellFormatter().addStyleName(row, 0, "property-key");
    drawRow(translations.totalLabel(), String.valueOf(Math.round(total)), "-", getPercentage(total, total));
  }

  private String getPercentage(double numerator, double denominator) {
    if(denominator > 0) {
      return formatDecimal(numerator / denominator * 100) + "%";
    }

    return "0%";
  }

  private String formatDecimal(double number) {
    NumberFormat nf = NumberFormat.getFormat("#.##");
    return nf.format(number);
  }
}
