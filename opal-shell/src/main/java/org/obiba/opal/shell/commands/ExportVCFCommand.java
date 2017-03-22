/*
 * Copyright (c) 2017 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.shell.commands;

import com.google.common.base.Stopwatch;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.obiba.opal.core.domain.Project;
import org.obiba.opal.core.runtime.NoSuchServiceException;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.ProjectService;
import org.obiba.opal.core.service.VCFSamplesMappingService;
import org.obiba.opal.shell.commands.options.ExportVCFCommandOptions;
import org.obiba.opal.spi.ServicePlugin;
import org.obiba.opal.spi.vcf.VCFStore;
import org.obiba.opal.spi.vcf.VCFStoreException;
import org.obiba.opal.spi.vcf.VCFStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;

@CommandUsage(description = "Export one or more VCF files from a project into a destination folder.",
    syntax = "Syntax: export-vcf --project PROJECT --destination DESTINATION NAMES")
public class ExportVCFCommand extends AbstractOpalRuntimeDependentCommand<ExportVCFCommandOptions> {

  private static final Logger log = LoggerFactory.getLogger(ExportVCFCommand.class);

  private static final DateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmss");

  @Autowired
  private ProjectService projectService;

  @Autowired
  private OpalRuntime opalRuntime;

  @Autowired
  private VCFSamplesMappingService vcfSamplesMappingService;

  private VCFStore store;

  //
  // AbstractOpalRuntimeDependentCommand Methods
  //

  @Override
  public int execute() {
    Stopwatch stopwatch = Stopwatch.createStarted();

    String names = String.join(", ", options.getNames());

    if (options.hasTable())
      getShell().printf("Exporting VCF/BCF files '%s' from project '%s' into '%s', filtered by '%s'...", names, options.getProject(), options.getDestination(), options.getTable());
    else
      getShell().printf("Exporting VCF/BCF files '%s' from project '%s' into '%s'...", names, options.getProject(), options.getDestination());
    Project project = projectService.getProject(getOptions().getProject());
    if (!opalRuntime.hasServicePlugins()) throw new NoSuchServiceException(VCFStoreService.SERVICE_TYPE);
    if (!project.hasVCFStoreService()) {
      getShell().printf("The project '%s' has no VCF store", options.getProject());
      return 1;
    }
    setVCFStore(project.getVCFStoreService(), project.getName());

    try {
      exportVCF();
    } catch (Exception e) {
      log.error("Cannot export VCF/BCF files from project {} into {}: {}", options.getProject(), options.getDestination(), names, e);
      getShell().printf("Cannot export VCF files: %s", e.getMessage());
      log.info("Export VCF/BCF failed in {}", stopwatch.stop());
      return 1;
    }

    log.info("Export VCF/BCF succeeded in {}", stopwatch.stop());
    return 0;
  }

  //
  // Methods
  //

  private void setVCFStore(String serviceName, String name) {
    if (!opalRuntime.hasServicePlugins()) throw new NoSuchElementException("No service plugin is available");
    ServicePlugin servicePlugin = opalRuntime.getServicePlugin(serviceName);
    if (!opalRuntime.isVCFStorePluginService(servicePlugin)) throw new NoSuchElementException("No VCF store service is available");

    VCFStoreService service = (VCFStoreService) servicePlugin;
    if (!service.hasStore(name)) service.createStore(name);
    store = service.getStore(name);
  }

  private void exportVCF() throws IOException, VCFStoreException {
    FileObject fileObject = resolveFileInFileSystem(options.getDestination());
    if (fileObject.exists() && fileObject.getType() == FileType.FILE)
      throw new IllegalArgumentException("Not a valid path to VCF file: " + options.getDestination());
    if (!fileObject.exists()) fileObject.createFolder();
    if (!fileObject.isWriteable()) throw new IllegalArgumentException("Export destination is not writable: " + options.getDestination());
    String timestamp = DATE_FORMAT.format(new Date());
    String destinationFolderName = store.getName() + "-vcf-" + timestamp;
    File destinationFolder = new File(opalRuntime.getFileSystem().getLocalFile(fileObject), destinationFolderName);
    destinationFolder.mkdirs();
    getShell().printf(String.format("Exporting VCF/BCF files in: %s", options.getDestination() + File.separator + destinationFolderName));

    int total = options.getNames().size() + 1;
    getShell().progress(String.format("Exporting VCF/BCF file(s): %s", String.join(", ", options.getNames())), 0, total, 0);

    List<String> filterSampleIds = options.hasTable() ?
        vcfSamplesMappingService.getFilteredSampleIds(options.getProject(), options.getTable(), options.isCaseControl()) :
        Lists.newArrayList();

    int count = 1;
    for (String vcfName : options.getNames()) {
      getShell().progress(String.format("Exporting VCF/BCF file: %s", vcfName), count, total, (count*100)/total);
      VCFStore.VCFSummary summary = store.getVCFSummary(vcfName);
      String vcfFileName = vcfName + "." + summary.getFormat().name().toLowerCase() + ".gz";
      getShell().printf(String.format("Exporting VCF/BCF file: %s", vcfFileName));
      File vcfFile = new File(destinationFolder, vcfFileName);
      if (filterSampleIds.isEmpty())
        store.readVCF(vcfName, new FileOutputStream(vcfFile));
      else
        store.readVCF(vcfName, new FileOutputStream(vcfFile), filterSampleIds);
      count++;
    }
    getShell().progress(String.format("VCF/BCF file(s) export completed."), total, total, 100);
  }

  FileObject resolveFileInFileSystem(String path) throws FileSystemException {
    if (Strings.isNullOrEmpty(path)) return null;
    return opalRuntime.getFileSystem().getRoot().resolveFile(path);
  }

  @Override
  public String toString() {
    return "export-vcf -p '" + getOptions().getProject() + "' -d '" + options.getDestination() + "' " + String.join(", ", options.getNames());
  }

}
