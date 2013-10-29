package org.obiba.opal.core.upgrade.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.obiba.opal.core.domain.security.SubjectAcl;
import org.obiba.opal.core.runtime.database.DatabaseRegistry;
import org.obiba.opal.core.service.OrientDbService;
import org.obiba.runtime.Version;
import org.obiba.runtime.upgrade.AbstractUpgradeStep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.metadata.schema.OType;

@SuppressWarnings("SpringJavaAutowiringInspection")
public class MoveSubjectAclToOrientUpgradeStep extends AbstractUpgradeStep {

  @Autowired
  private DatabaseRegistry databaseRegistry;

  @Autowired
  private OrientDbService orientDbService;

  @Override
  public void execute(Version currentVersion) {
    orientDbService.registerEntityClass(SubjectAcl.class);
    orientDbService.createIndex(SubjectAcl.class, "domain", OClass.INDEX_TYPE.NOTUNIQUE, OType.STRING);
    orientDbService.createIndex(SubjectAcl.class, "node", OClass.INDEX_TYPE.NOTUNIQUE, OType.STRING);
    orientDbService.createIndex(SubjectAcl.class, "principal", OClass.INDEX_TYPE.NOTUNIQUE, OType.STRING);
    orientDbService.createIndex(SubjectAcl.class, "type", OClass.INDEX_TYPE.NOTUNIQUE, OType.STRING);

    JdbcTemplate dataJdbcTemplate = new JdbcTemplate(databaseRegistry.getDataSource("opal-data", null));
    List<SubjectAcl> list = dataJdbcTemplate.query("select * from subject_acl", new RowMapper<SubjectAcl>() {
      @Override
      public SubjectAcl mapRow(ResultSet rs, int rowNum) throws SQLException {
        SubjectAcl acl = new SubjectAcl();
        acl.setDomain(rs.getString("domain"));
        acl.setNode(rs.getString("node"));
        acl.setPermission(rs.getString("permission"));
        acl.setPrincipal(rs.getString("principal"));
        acl.setType(rs.getString("type"));
        return acl;
      }
    });
    for(SubjectAcl acl : list) {
      orientDbService.save(acl);
    }
    dataJdbcTemplate.execute("drop table subject_acl");
  }
}