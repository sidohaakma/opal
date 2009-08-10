/*******************************************************************************
 * Copyright 2008(c) The OBiBa Consortium. All rights reserved.
 * 
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.datasource.onyx;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.obiba.onyx.engine.variable.IVariablePathNamingStrategy;
import org.obiba.onyx.engine.variable.Variable;
import org.obiba.onyx.engine.variable.VariableData;
import org.obiba.onyx.engine.variable.VariableDataSet;
import org.obiba.onyx.engine.variable.util.VariableFinder;
import org.obiba.onyx.util.data.Data;
import org.obiba.opal.core.domain.data.DataPoint;
import org.obiba.opal.core.domain.data.Dataset;
import org.obiba.opal.core.domain.data.Entity;
import org.obiba.opal.core.domain.metadata.Catalogue;
import org.obiba.opal.datasource.DatasourceService;
import org.obiba.opal.datasource.util.DatasourceUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemStreamException;
import org.springframework.batch.item.ItemStreamReader;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class OnyxDatasetReader extends AbstractOnyxReader<Dataset> implements ItemStreamReader<Dataset> {

  private static final Logger log = LoggerFactory.getLogger(OnyxDatasetReader.class);

  private String catalogueName;

  private DatasourceService datasourceService;

  private Iterator<String> entryIterator;

  private Catalogue catalogue;

  private IVariablePathNamingStrategy variablePathNamingStrategy;

  public void setDatasourceService(DatasourceService datasourceService) {
    this.datasourceService = datasourceService;
  }

  public void setCatalogueName(String catalogueName) {
    this.catalogueName = catalogueName;
  }

  public void setVariablePathNamingStrategy(IVariablePathNamingStrategy variablePathNamingStrategy) {
    this.variablePathNamingStrategy = variablePathNamingStrategy;
  }

  @Override
  protected void doOpen(ExecutionContext executionContext) throws ItemStreamException {
    super.doOpen(executionContext);
    this.entryIterator = getDataInputStrategy().listEntries().iterator();
  }

  public Dataset read() throws Exception, UnexpectedInputException, ParseException {
    if(this.catalogue == null) {
      this.catalogue = datasourceService.loadCatalogue(catalogueName);
    }
    if(entryIterator.hasNext()) {
      String entryName = entryIterator.next();
      while(isParticipantEntry(entryName) == false && entryIterator.hasNext()) {
        entryName = entryIterator.next();
      }
      if(isParticipantEntry(entryName)) {
        log.info("Processing entry {}", entryName);
        VariableDataSet variableDataSetRoot = readVariableDataset(entryName);

        Entity entity = datasourceService.fetchEntity(entryName.replace(".xml", ""));

        Dataset dataset = new Dataset(entity, catalogue, variableDataSetRoot.getExportDate());

        Map<Variable, List<Integer>> occurrencesMap = new HashMap<Variable, List<Integer>>();
        for(VariableData vd : variableDataSetRoot.getVariableDatas()) {
          handleVariableData(dataset, vd, occurrencesMap);
        }
        return dataset;
      }
    }
    return null;
  }

  /**
   * Given a <code>Dataset</code> and <code>VariableData</code>, recursively adds the necessary
   * <code>DataPoint</code>s to the <code>Dataset</code>
   * 
   * @param dataset dataset
   * @param variableData variableData
   * @param occurrencesMap map of variables to occurrences
   */
  private void handleVariableData(Dataset dataset, VariableData variableData, Map<Variable, List<Integer>> occurrencesMap) {
    VariableFinder variableFinder = VariableFinder.getInstance(variableRoot, variablePathNamingStrategy);
    Variable variable = variableFinder.findVariable(variableData.getVariablePath());

    List<Data> datas = variableData.getDatas();

    // If the variable has a repeatable ancestor, determine the occurrence we are dealing with.
    Integer occurrenceId = null;
    Variable repeatableAncestor = getRepeatableAncestor(variable);
    if(repeatableAncestor != null) {
      occurrenceId = getNormalizedOccurrenceId(repeatableAncestor, variableData, occurrencesMap);
    }

    // If the variable is itself repeatable, add its occurrences to the occurrencesMap.
    if(variable.isRepeatable()) {
      occurrencesMap.put(variable, extractOccurrences(datas));
    }

    // Determine the value.
    // CASE 1: Variable is multiple. Value is a comma-separated list of the data values.
    // CASE 2: Variable is not multiple and not repeatable. Value is the (single) data value.
    // CASE 3: Variable is repeatable. Variable has no value of its own.
    String value = null;
    if(variable.isMultiple()) {
      List<String> multipleDataValues = new ArrayList<String>();
      for(Data data : datas) {
        multipleDataValues.add(data.getValueAsString());
      }
      value = DatasourceUtil.getDataPointValue(multipleDataValues);
    } else if(!variable.isRepeatable()) {
      if(datas.size() != 0) {
        value = datas.get(0).getValueAsString();
      }
    }

    // Create and add a DataPoint, EXCEPT in the following two cases:
    // CASE 1: Variable is repeatable (in which case it has no data of its own).
    // CASE 2: Variable has a repeatable ancestor and the data is contained in its children (this filters out bogus
    // variableData which are simply containers).
    if(!variable.isRepeatable() && !(repeatableAncestor != null && !variableData.getVariableDatas().isEmpty())) {
      String variablePathWithoutParams = variablePathNamingStrategy.getVariablePath(variableData.getVariablePath());
      addDataPoint(dataset, variablePathWithoutParams, value, occurrenceId);
    }

    // Recurse on children.
    if(!variableData.getVariableDatas().isEmpty()) {
      for(VariableData child : variableData.getVariableDatas()) {
        handleVariableData(dataset, child, occurrencesMap);
      }
    }
  }

  private void addDataPoint(Dataset dataset, String variablePath, String value, Integer occurrenceId) {
    DataPoint dataPoint = new DataPoint(dataset, variablePath, value, occurrenceId);
    dataset.getDataPoints().add(dataPoint);
  }

  private Variable getRepeatableAncestor(Variable variable) {
    Variable repeatableAncestor = null;

    Variable ancestor = variable.getParent();

    while(ancestor != null) {
      if(ancestor.isRepeatable()) {
        repeatableAncestor = ancestor;
        break;
      }

      ancestor = ancestor.getParent();
    }

    return repeatableAncestor;
  }

  private List<Integer> extractOccurrences(List<Data> datas) {
    List<Integer> occurrenceIds = new ArrayList<Integer>();
    for(Data data : datas) {
      occurrenceIds.add(Integer.valueOf(data.getValueAsString()));
    }

    return occurrenceIds;
  }

  private Integer getOccurrenceId(String variablePath) {
    Map<String, String> parameters = variablePathNamingStrategy.getParameters(variablePath);
    if(parameters != null && parameters.size() > 0) {
      String parentName = parameters.keySet().iterator().next();
      List<String> parts = variablePathNamingStrategy.getNormalizedNames(variablePath);
      parts.remove(parts.size() - 1);
      Collections.reverse(parts);
      for(Iterator<String> iterator = parts.iterator(); iterator.hasNext();) {
        String string = iterator.next();
        if(string.equals(parentName) == false) {
          iterator.remove();
        } else {
          break;
        }
      }
      Collections.reverse(parts);

      StringBuilder parentPath = new StringBuilder();
      for(String part : parts) {
        if(parentPath.length() > 0) {
          parentPath.append(variablePathNamingStrategy.getPathSeparator());
        }
        parentPath.append(part);
      }

      String occurrenceId = parameters.values().iterator().next();
      return Integer.valueOf(occurrenceId);
    }
    return null;
  }

  private Integer getNormalizedOccurrenceId(Variable parent, VariableData variableData, Map<Variable, List<Integer>> occurrencesMap) {
    Integer occurrenceId = getOccurrenceId(variableData.getVariablePath());

    if(occurrenceId != null) {
      List<Integer> parentOccurrenceIds = occurrencesMap.get(parent);
      if(parentOccurrenceIds != null) {
        for(Integer aParentOccurrenceId : parentOccurrenceIds) {
          if(aParentOccurrenceId.equals(occurrenceId)) {
            return aParentOccurrenceId;
          }
        }
      }
    }

    return null;
  }
}
