package org.obiba.opal.web.analysis;

import org.obiba.opal.core.domain.OpalAnalysis;
import org.obiba.opal.core.service.*;
import org.obiba.opal.web.model.Projects;
import org.obiba.opal.web.model.Projects.OpalAnalysisDto;
import org.obiba.opal.web.model.Projects.OpalAnalysisDto.Builder;
import org.obiba.opal.web.model.Projects.OpalAnalysisResultDto;
import org.obiba.opal.web.model.Projects.OpalAnalysisResultsDto;
import org.obiba.opal.web.project.Dtos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.activation.MimetypesFileTypeMap;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.BufferedOutputStream;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class TableAnalysisResource {

  private final OpalAnalysisService analysisService;
  private final OpalAnalysisResultService analysisResultService;
  private final AnalysisExportService analysisExportService;

  private String datasourceName;
  private String tableName;

  @Autowired
  public TableAnalysisResource(OpalAnalysisService analysisService,
                                OpalAnalysisResultService analysisResultService,
                                AnalysisExportService analysisExportService) {

    this.analysisService = analysisService;
    this.analysisResultService = analysisResultService;
    this.analysisExportService = analysisExportService;
  }

  @GET
  @Path("/analyses")
  public Projects.OpalAnalysesDto getProjectTableAnalyses() {
    return Projects.OpalAnalysesDto.newBuilder()
      .addAllAnalyses(
        StreamSupport
          .stream(analysisService.getAnalysesByDatasourceAndTable(datasourceName, tableName).spliterator(), false)
          .map(analysis -> Dtos.asDto(analysis).build()).collect(Collectors.toList()))
        .build();
  }

  @GET
  @Path("/analysis/{analysisId}")
  public OpalAnalysisDto getAnalysis(@PathParam("analysisId") String analysisId,
                                     @QueryParam("lastResult") @DefaultValue("false") boolean lastResult) {

    getAnalysis(analysisId);
    Builder builder = Dtos.asDto(getAnalysis(analysisId));

    builder.addAllAnalysisResults(
      StreamSupport
        .stream(analysisResultService.getAnalysisResults(analysisId, lastResult).spliterator(), false)
        .map(analysisResult -> Dtos.asDto(analysisResult).build())
        .collect(Collectors.toList()));

    return builder.build();
  }

  @GET
  @Path("/analysis/{analysisId}/results")
  public OpalAnalysisResultsDto getAnalysisResults(@PathParam("analysisId") String analysisId,
                                                   @QueryParam("lastResult") @DefaultValue("false") boolean lastResult) {
    return OpalAnalysisResultsDto.newBuilder()
        .addAllAnalysisResults(
          StreamSupport
            .stream(analysisResultService.getAnalysisResults(analysisId, lastResult).spliterator(), false)
            .map(analysisResult -> Dtos.asDto(analysisResult).build()).collect(Collectors.toList()))
        .build();
  }

  @GET
  @Path("/analysis/{analysisId}/result/{rid}")
  public OpalAnalysisResultDto getAnalysisResult(@PathParam("analysisId") String analysisId, @PathParam("rid") String rid) {
    return Dtos.asDto(analysisResultService.getAnalysisResult(analysisId, rid)).build();
  }

  @GET
  @Path("/analyses/_export")
  @Produces("application/zip")
  public Response exportTableAnalysis(@QueryParam("all") @DefaultValue("false") boolean all) {
    StreamingOutput outputStream =
      stream -> analysisExportService.exportProjectTableAnalyses(
        datasourceName,
        tableName,
        new BufferedOutputStream(stream),
        !all);

    String fileName = String.format("%s-%s-analsis.zip", datasourceName, tableName);
    String mimeType = new MimetypesFileTypeMap().getContentType(fileName);
    return Response.ok(outputStream, mimeType)
      .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build();
  }

  @GET
  @Path("/analysis/{analysisId}/_export")
  @Produces("application/zip")
  public Response exportTableAnalysis(@PathParam("analysisId") String analysisId,
                                      @QueryParam("all") @DefaultValue("false") boolean all) {
    getAnalysis(analysisId);

    StreamingOutput outputStream =
      stream -> analysisExportService.exportProjectAnalysis(analysisId, new BufferedOutputStream(stream), !all);

    String fileName = String.format("%s-%s-analsis.zip", datasourceName, tableName);
    String mimeType = new MimetypesFileTypeMap().getContentType(fileName);
    return Response.ok(outputStream, mimeType)
      .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"").build();
  }

  private OpalAnalysis getAnalysis(String analysisId) throws NoSuchAnalysisException, NoSuchDatasourceTableAnalysisException {
    OpalAnalysis analysis = analysisService.getAnalysis(analysisId);

    if (analysis == null) {
      throw new NoSuchAnalysisException(analysisId);
    }

    if (datasourceName.equals(analysis.getDatasource()) && tableName.equals(analysis.getTable())) {
      return analysis;
    }

    throw new NoSuchDatasourceTableAnalysisException(datasourceName, tableName, analysisId);
  }
  
  void setDatasourceName(String value) {
    datasourceName = value;
  }

  void setTableName(String value) {
    tableName = value;
  }
}
