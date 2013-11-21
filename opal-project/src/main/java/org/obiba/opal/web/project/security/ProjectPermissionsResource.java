/*
 * Copyright (c) 2013 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.obiba.opal.web.project.security;

import javax.annotation.Nullable;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.obiba.magma.support.MagmaEngineTableResolver;
import org.obiba.magma.support.MagmaEngineVariableResolver;
import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.project.ProjectService;
import org.obiba.opal.web.model.Opal;
import org.obiba.opal.web.security.PermissionsToAclFunction;
import org.obiba.opal.web.support.InvalidRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Predicate;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;

@Component
@Scope("request")
@Path("/project/{name}/permissions")
public class ProjectPermissionsResource {

  public static final String DOMAIN = "opal";

  // ugly: duplicate of ProjectsPermissionConverter.Permission

  private enum ProjectPermission {
    PROJECT_ALL
  }

  @Autowired
  private SubjectAclService subjectAclService;

  @Autowired
  private ProjectService projectService;

  @PathParam("name")
  private String name;

  /**
   * Get all permissions in the project.
   *
   * @param domain
   * @param type
   * @return
   */
  @GET
  @Path("/_all")
  public Iterable<Opal.Acl> getPermissions(@QueryParam("domain") @DefaultValue("opal") String domain,
      @QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type) {

    // make sure project exists
    projectService.getProject(name);

    Iterable<SubjectAclService.Permissions> permissions = Iterables
        .concat(subjectAclService.getNodeHierarchyPermissions(DOMAIN, getProjectNode(), type), Iterables
            .filter(subjectAclService.getNodeHierarchyPermissions(DOMAIN, "/datasource/" + name, type),
                new MagmaPermissionsPredicate()));

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Get all project-level permissions in the project.
   *
   * @param domain
   * @param type
   * @return
   */
  @GET
  @Path("/project")
  public Iterable<Opal.Acl> getProjectPermissions(
      @QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type) {

    // make sure project exists
    projectService.getProject(name);

    Iterable<SubjectAclService.Permissions> permissions = subjectAclService
        .getNodeHierarchyPermissions(DOMAIN, getProjectNode(), type);

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Get all permissions with PROJECT_ALL.
   *
   * @param type
   * @return
   */
  @GET
  @Path("/project/owners")
  public Iterable<Opal.Acl> getProjectOwners(
      @QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type) {
    // make sure project exists
    projectService.getProject(name);

    Iterable<SubjectAclService.Permissions> permissions = subjectAclService
        .getNodePermissions(DOMAIN, getProjectNode(), type);

    return Iterables.transform(permissions, PermissionsToAclFunction.INSTANCE);
  }

  /**
   * Add a subject with PROJECT_ALL permission.
   *
   * @param type
   * @param principal
   * @return
   */
  @POST
  @Path("/project/owners")
  public Response addProjectOwner(@QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type,
      @QueryParam("principal") String principal) {
    // make sure project exists
    projectService.getProject(name);
    validatePrincipal(principal);

    SubjectAclService.Subject subject = type.subjectFor(principal);
    subjectAclService.deleteSubjectPermissions(DOMAIN, getProjectNode(), subject);
    subjectAclService.addSubjectPermission(DOMAIN, getProjectNode(), subject, ProjectPermission.PROJECT_ALL.name());

    return Response.ok().build();
  }

  /**
   * Remove PROJECT_ALL permission from a subject.
   *
   * @param principal
   * @param type
   * @return
   */
  @DELETE
  @Path("/project/owner/{principal}")
  public Response deleteProjectOwner(@PathParam("principal") String principal,
      @QueryParam("type") @DefaultValue("USER") SubjectAclService.SubjectType type) {
    // make sure project exists
    projectService.getProject(name);

    SubjectAclService.Subject subject = type.subjectFor(principal);
    subjectAclService.deleteSubjectPermissions(DOMAIN, getProjectNode(), subject);

    return Response.ok().build();
  }

  private void validatePrincipal(String principal) {
    if(Strings.isNullOrEmpty(principal)) throw new InvalidRequestException("Principal is required.");
  }

  private String getProjectNode() {
    return "/project/" + name;
  }

  /**
   * Filter the accessible Magma objects.
   */
  static class MagmaPermissionsPredicate implements Predicate<SubjectAclService.Permissions> {
    @Override
    public boolean apply(@Nullable SubjectAclService.Permissions input) {
      try {
        String fullName = input.getNode().replace("/datasource/", "").replace("/table/", ".").replace("/view/", ".")
            .replace("/variable/", ":");
        if(input.getNode().contains("/variable/")) MagmaEngineVariableResolver.valueOf(fullName).resolveSource();
        else if(input.getNode().contains("/table/") || input.getNode().contains("/view/"))
          MagmaEngineTableResolver.valueOf(fullName).resolveTable();
        return true;
      } catch(Exception e) {
        return false;
      }
    }
  }
}
