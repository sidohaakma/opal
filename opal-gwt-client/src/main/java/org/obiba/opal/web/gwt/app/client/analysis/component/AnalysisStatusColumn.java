package org.obiba.opal.web.gwt.app.client.analysis.component;

import com.google.gwt.cell.client.AbstractCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.user.cellview.client.Column;
import org.obiba.opal.web.gwt.app.client.i18n.Translations;
import org.obiba.opal.web.model.client.opal.AnalysisStatusDto;
import org.obiba.opal.web.model.client.opal.OpalAnalysisResultDto;


public class AnalysisStatusColumn extends Column<OpalAnalysisResultDto, String> {

  private static final Translations translations = GWT.create(Translations.class);


  AnalysisStatusColumn() {
    super(new StatusImageCell());
  }

  static String formatForRender(OpalAnalysisResultDto dto) {
    String status = dto.getStatus().getName();
    String color = StatusImageCell.BULLET_ORANGE;

    if (AnalysisStatusDto.IN_PROGRESS.getName().equals(status)) {
      color = StatusImageCell.BULLET_BLACK;
    } else if (AnalysisStatusDto.PASSED.getName().equals(status)) {
      color = StatusImageCell.BULLET_GREEN;
    } else if (AnalysisStatusDto.FAILED.getName().equals(status)) {
      color = StatusImageCell.BULLET_RED;
    } else if (AnalysisStatusDto.ERROR.getName().equals(status)) {
      color = StatusImageCell.BULLET_RED;
    }

    return translations.analysisStatusMap().get(status) + ":" + color;
  }

  @Override
  public String getValue(OpalAnalysisResultDto dto) {
    return formatForRender(dto);
  }

  static class StatusImageCell extends AbstractCell<String> {

    public final static String BULLET_GREEN = "status-success";

    public final static String BULLET_ORANGE = "status-warning";

    public final static String BULLET_RED = "status-error";

    public final static String BULLET_BLACK = "status-default";

    protected interface Template extends SafeHtmlTemplates {
      @Template("<i class=\"icon-circle {1}\" title=\"{0}\"></i>")
      SafeHtml img(String cssClass, String title);

      @Template("<span><i class=\"icon-circle {1}\" title=\"{0}\"></i> {0}</span>")
      SafeHtml labelImg(String cssClass, String title);
    }

    @SuppressWarnings("StaticNonFinalField")
    private static Template template = GWT.create(Template.class);

    public static String renderAsString(String value) {
      SafeHtmlBuilder sb = new SafeHtmlBuilder();
      render(value, sb, true);
      return sb.toSafeHtml().asString();
    }

    /**
     * The value is expected to be encoded as TITLE:[STATUS|PROGRESS].
     * @param context
     * @param value
     * @param sb
     */
    @Override
    public void render(Context context, String value, SafeHtmlBuilder sb) {
      render(value, sb, false);
    }

    private static void render(String value, SafeHtmlBuilder sb, boolean withLabel) {
      if(value != null) {
        String[] values = value.split(":");
        // The template will sanitize the URI.
        sb.append(withLabel ? template.labelImg(values[0], values[1]) : template.img(values[0], values[1]));
      }
    }

  }
}