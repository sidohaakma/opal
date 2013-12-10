/*******************************************************************************
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package org.obiba.opal.web.security;

import java.util.List;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.hibernate.validator.internal.engine.ConstraintViolationImpl;
import org.hibernate.validator.internal.engine.path.PathImpl;
import org.obiba.opal.core.domain.security.SubjectCredentials;
import org.obiba.opal.core.service.security.SubjectCredentialsService;
import org.obiba.opal.web.model.Opal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

@Component
@Path("/system/subject-credentials")
public class SubjectCredentialsResource {

  @Autowired
  private SubjectCredentialsService subjectCredentialsService;

  @GET
  public List<Opal.SubjectCredentialsDto> getUsers() {
    return Lists.newArrayList(Iterables
        .transform(subjectCredentialsService.getSubjectCredentials(SubjectCredentials.Type.USER),
            new Function<SubjectCredentials, Opal.SubjectCredentialsDto>() {
              @Override
              public Opal.SubjectCredentialsDto apply(SubjectCredentials subjectCredentials) {
                return Dtos.asDto(subjectCredentials);
              }
            }));
  }

  @POST
  public Response createUser(Opal.SubjectCredentialsDto dto) {
    SubjectCredentials subjectCredentials = Dtos.fromDto(dto);
    if(subjectCredentialsService.getSubjectCredentials(subjectCredentials.getName()) != null) {
      ConstraintViolation<SubjectCredentials> violation = ConstraintViolationImpl
          .forBeanValidation("{org.obiba.opal.core.validator.Unique.message}", "must be unique",
              SubjectCredentials.class, subjectCredentials, subjectCredentials, subjectCredentials,
              PathImpl.createPathFromString("name"), null, null);
      throw new ConstraintViolationException(Sets.newHashSet(violation));
    }
    if(subjectCredentials.getType() == SubjectCredentials.Type.USER) {
      subjectCredentials.setPassword(subjectCredentialsService.hashPassword(dto.getPassword()));
    }
    subjectCredentialsService.save(subjectCredentials);
    return Response.ok().build();
  }
}