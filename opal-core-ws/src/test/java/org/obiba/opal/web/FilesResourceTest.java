/*
 * Copyright (c) 2012 OBiBa. All rights reserved.
 *
 * This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.obiba.opal.web;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.vfs2.FileContent;
import org.apache.commons.vfs2.FileName;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileType;
import org.hamcrest.Matchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.obiba.opal.core.runtime.OpalRuntime;
import org.obiba.opal.core.service.SubjectAclService;
import org.obiba.opal.fs.OpalFileSystem;
import org.obiba.opal.fs.impl.DefaultOpalFileSystem;
import org.obiba.opal.web.model.Opal.FileDto;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertThat;

@SuppressWarnings({ "OverlyLongMethod", "PMD.NcssMethodCount" })
public class FilesResourceTest {

  private OpalRuntime opalRuntimeMock;

  private SubjectAclService subjectAclServiceMock;

  private OpalFileSystem fileSystem;

  private FilesResource filesResource;

  private FileObject fileObjectMock;

  private FileItem fileItemMock;

  private UriInfo uriInfoMock;

  /**
   * Delete these files in tearDown().
   */
  private final Collection<String> filesCreatedByTest = new ArrayList<String>();

  @Before
  public void setUp() throws URISyntaxException {
    opalRuntimeMock = createMock(OpalRuntime.class);
    subjectAclServiceMock = createMock(SubjectAclService.class);

    String rootDir = getClass().getResource("/test-file-system").toURI().toString();
    File emptyDir = new File(rootDir.replace("file:", ""), "folder4/folder41");
    if(!emptyDir.exists()) {
      Assert.assertEquals(true, emptyDir.mkdirs());
    }
    fileSystem = new DefaultOpalFileSystem(rootDir);
    filesResource = new FilesResource();
    filesResource.setOpalRuntime(opalRuntimeMock);
    filesResource.setSubjectAclService(subjectAclServiceMock);

    fileItemMock = createMock(FileItem.class);
    fileObjectMock = createMock(FileObject.class);

    uriInfoMock = createMock(UriInfo.class);
  }

  @After
  public void tearDown() throws FileSystemException {
    // Delete any files created by the test.
    for(String filePath : filesCreatedByTest) {
      FileObject file = fileSystem.getRoot().resolveFile(filePath);
      if(file.exists()) {
        file.delete();
      }
    }
  }

  @Test
  public void testGetFileSystem() throws FileSystemException {

    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalRuntimeMock);

    FileSystemResource fsResource = new FileSystemResource();
    fsResource.setOpalRuntime(opalRuntimeMock);
    FileDto rootFileDto = fsResource.getFileSystem();

    Assert.assertEquals("root", rootFileDto.getName());
    Assert.assertEquals(FileDto.FileType.FOLDER, rootFileDto.getType());
    Assert.assertEquals("/", rootFileDto.getPath());

  }

  @Test
  @Ignore
  public void verifyThatAllFilesAndFoldersInDtoStructureExistInFileSystem() throws FileSystemException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalRuntimeMock);

    FileSystemResource fsResource = new FileSystemResource();
    fsResource.setOpalRuntime(opalRuntimeMock);

    FileDto rootFileDto = fsResource.getFileSystem();

    int childrenCounter = 0;
    childrenCounter = verifyThatChildrenExistInFileSystem(rootFileDto, childrenCounter);

    // File count in Dto structure should be the same as file count in file system.
    Assert.assertEquals(20, childrenCounter);

    verify(opalRuntimeMock);
  }

  private int verifyThatChildrenExistInFileSystem(FileDto folder, int childrenCounter) throws FileSystemException {
    FileObject correspondingFileObj;
    int counter = childrenCounter;
    for(FileDto child : folder.getChildrenList()) {
      counter++;
      correspondingFileObj = fileSystem.getRoot().resolveFile(child.getPath());
      Assert.assertTrue(correspondingFileObj.exists());
      if(child.getType() == FileDto.FileType.FOLDER) {
        counter = verifyThatChildrenExistInFileSystem(child, childrenCounter);
      }
    }
    return counter;
  }

  @Ignore("SecurityManager dependency not satisfied")
  @Test
  public void testGetFoldersDetailsInFileSystem() throws FileSystemException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).atLeastOnce();

    replay(opalRuntimeMock);

    checkGetFileDetailsResponse("/", "folder1", "folder2", "folder3", "folder4", "folder5", "file2.txt", "folder11",
        "file11.txt", "file21.txt", "folder31", "folder41", "file41.txt", "file42.txt", "file43.txt", "file51.txt");
    checkGetFileDetailsResponse("/folder1/folder11", "folder111", "file111.txt", "file1111.txt", "file1112.txt");
    checkGetFileDetailsResponse("/folder1/folder11/folder111", "file1111.txt", "file1112.txt");
    checkGetFileDetailsResponse("/folder2", "file21.txt");
    checkGetFileDetailsResponse("/folder3", "folder31", "file311.txt");
    checkGetFileDetailsResponse("/folder4", "folder41", "file41.txt", "file42.txt", "file43.txt");
    checkGetFileDetailsResponse("/folder5", "file51.txt");

    verify(opalRuntimeMock);

  }

  private void checkGetFileDetailsResponse(String path, String... expectedFolderContentArray)
      throws FileSystemException {

    Set<String> expectedFolderContent = new HashSet<String>(Arrays.asList(expectedFolderContentArray));
    Response response = filesResource.getFileDetails(path);

    // Make sure response is OK
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

    FileDto folder = (FileDto) response.getEntity();
    List<FileDto> folderContent = folder.getChildrenList();

    // Check folder content recursively two levels down.
    checkFolderContent(expectedFolderContent, folderContent, 2);

    Assert.assertTrue(expectedFolderContent.isEmpty());
  }

  private void checkFolderContent(Set<String> expectedFolderContent, Iterable<FileDto> folderContent, int level) {
    // Make sure folder content is as expected.
    for(FileDto oneFileOrFolder : folderContent) {
      Assert.assertTrue(expectedFolderContent.contains(oneFileOrFolder.getName()));
      expectedFolderContent.remove(oneFileOrFolder.getName());
      if(level > 0 && oneFileOrFolder.getChildrenCount() > 0) {
        checkFolderContent(expectedFolderContent, oneFileOrFolder.getChildrenList(), level - 1);
      }

    }
  }

  @Ignore("SecurityManager dependency not satisfied")
  @Test
  public void testGetFile_GetCompressedFolderFromFileSystem() throws IOException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).atLeastOnce();
    replay(opalRuntimeMock);

    checkCompressedFolder("/folder1", "folder1", "folder1/folder11", "folder1/file11.txt", "folder1/folder11/folder111",
        "folder1/folder11/file111.txt", "folder1/folder11/folder111/file1111.txt",
        "folder1/folder11/folder111/file1112.txt");
    checkCompressedFolder("/folder2", "folder2", "folder2/file21.txt");
    checkCompressedFolder("/folder3", "folder3", "folder3/folder31", "folder3/folder31/file311.txt");
    checkCompressedFolder("/folder4", "folder4", "folder4/folder41", "folder4/file41.txt", "folder4/file42.txt",
        "folder4/file43.txt");
    checkCompressedFolder("/folder5", "folder5", "folder5/file51.txt");
    checkCompressedFolder("/", "/", "folder1", "folder1/folder11", "folder1/file11.txt", "folder1/folder11/folder111",
        "folder1/folder11/file111.txt", "folder1/folder11/folder111/file1111.txt",
        "folder1/folder11/folder111/file1112.txt", "folder2", "folder2/file21.txt", "folder3", "folder3/folder31",
        "folder3/folder31/file311.txt", "folder4", "folder4/folder41", "folder4/file41.txt", "folder4/file42.txt",
        "folder4/file43.txt", "folder5", "folder5/file51.txt", "file2.txt");

    verify(opalRuntimeMock);

  }

  @SuppressWarnings("unchecked")
  private void checkCompressedFolder(String folderPath, String... expectedFolderContentArray) throws IOException {
    Response response = filesResource.getFile(folderPath, null);
    ZipFile zipfile = new ZipFile(((File) response.getEntity()).getPath());

    // Check that all folders and files exist in the compressed archive that represents the folder.
    ZipEntry zipEntry;
    for(String anExpectedFolderContentArray : expectedFolderContentArray) {
      zipEntry = zipfile.getEntry(anExpectedFolderContentArray);
      Assert.assertNotNull(zipEntry);
    }

    Enumeration<ZipEntry> zipEnum = (Enumeration<ZipEntry>) zipfile.entries();
    int count = 0;

    while(zipEnum.hasMoreElements()) {
      zipEnum.nextElement();
      count++;
    }

    // Make sure that they are no unexpected files in the compressed archive.
    Assert.assertEquals(expectedFolderContentArray.length, count);

    zipfile.close();
  }

  @Test
  public void testGetPathThatDoesNotExist() throws FileSystemException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalRuntimeMock);

    Response response = filesResource.getFileDetails("/folder1/folder2");
    Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock);

  }

  @Test
  @Ignore
  public void testUploadFileToFileSystem() throws FileUploadException, IOException, URISyntaxException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();
    expect(fileItemMock.getName()).andReturn("fileToUpload.txt").atLeastOnce();
    expect(fileItemMock.getInputStream()).andReturn(getClass().getResourceAsStream("/files-to-upload/fileToUpload.txt"))
        .once();
    expect(uriInfoMock.getBaseUriBuilder()).andReturn(UriBuilder.fromPath("/"));

    FilesResource fileResource = new FilesResource() {
      @Override
      protected FileItem getUploadedFile(HttpServletRequest request) throws FileUploadException {
        return fileItemMock;
      }
    };
    fileResource.setOpalRuntime(opalRuntimeMock);
    fileResource.setSubjectAclService(subjectAclServiceMock);

    replay(opalRuntimeMock, fileItemMock, uriInfoMock);

    // Upload the file.
    String destinationPath = "/folder1/folder11/folder111";
    Response response = fileResource.uploadFile(destinationPath, uriInfoMock, null);
    filesCreatedByTest.add(destinationPath);

    // Verify that the service response is CREATED.
    Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock, fileItemMock, uriInfoMock);

    // Verify that the file was uploaded at the right path in the file system.
    Assert.assertTrue(fileSystem.getRoot().resolveFile("/folder1/folder11/folder111/fileToUpload.txt").exists());
    // clean up
    fileSystem.getRoot().resolveFile("/folder1/folder11/folder111/fileToUpload.txt").delete();
  }

  @Test
  public void testUploadFileNoContentSubmitted() throws FileSystemException, FileUploadException, URISyntaxException {

    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalRuntimeMock);

    FilesResource fileResource = new FilesResource() {
      @Override
      protected FileItem getUploadedFile(HttpServletRequest request) throws FileUploadException {
        return null;
      }
    };
    fileResource.setOpalRuntime(opalRuntimeMock);
    fileResource.setSubjectAclService(subjectAclServiceMock);

    Response response = fileResource.uploadFile("/", uriInfoMock, null);
    Assert.assertEquals(Status.BAD_REQUEST.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock);

  }

  @Test
  public void testUploadFile_ReturnsNotFoundResponseWhenUploadDestinationDoesNotExist()
      throws IOException, FileUploadException, URISyntaxException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();

    FilesResource fileResource = new FilesResource() {
      @Override
      protected FileItem getUploadedFile(HttpServletRequest request) throws FileUploadException {
        return fileItemMock;
      }
    };
    fileResource.setOpalRuntime(opalRuntimeMock);
    filesResource.setSubjectAclService(subjectAclServiceMock);

    replay(opalRuntimeMock, fileItemMock, uriInfoMock);

    // Upload the file.
    String destinationPath = "/folder1/folder11/folder111/patate";
    Response response = fileResource.uploadFile(destinationPath, uriInfoMock, null);
    filesCreatedByTest.add(destinationPath);

    // Verify that the service response is CREATED.
    Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock, fileItemMock, uriInfoMock);
  }

  @Test
  public void testDeleteFile_FileDoesNotExist() throws FileSystemException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalRuntimeMock);

    Response response = filesResource.deleteFile("/folder1/folder2/filethatdoesnotexist.txt");
    Assert.assertEquals(Status.NOT_FOUND.getStatusCode(), response.getStatus());

    verify(opalRuntimeMock);
  }

  @Test
  @Ignore
  public void testDeleteFile_CannotDeleteFolderWithContent() throws FileSystemException {
    expect(opalRuntimeMock.getFileSystem()).andReturn(fileSystem).once();

    replay(opalRuntimeMock);

    Response response = filesResource.deleteFile("/folder1");
    Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
    Assert.assertEquals("cannotDeleteNotEmptyFolder", response.getEntity());

    verify(opalRuntimeMock);

  }

  @Test
  @Ignore
  public void testDeleteFile_CannotDeleteReadOnlyFile() throws FileSystemException {
    expect(fileObjectMock.getType()).andReturn(FileType.FILE).atLeastOnce();
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.isWriteable()).andReturn(false).atLeastOnce();

    replay(fileObjectMock);

    Response response = getFileResource().deleteFile("path");
    Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
    Assert.assertEquals("cannotDeleteReadOnlyFile", response.getEntity());

    verify(fileObjectMock);

  }

  @Test
  public void testDeleteFile_FileDeletedSuccessfully() throws FileSystemException {
    expect(fileObjectMock.getType()).andReturn(FileType.FILE).atLeastOnce();
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.isWriteable()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.delete()).andReturn(true).atLeastOnce();

    replay(fileObjectMock);

    Response response = getFileResource().deleteFile("path");
    Assert.assertEquals(Status.OK.getStatusCode(), response.getStatus());

    verify(fileObjectMock);

  }

  @Test
  public void testDeleteFile_CouldNotDeleteFile() throws FileSystemException {
    expect(fileObjectMock.getType()).andReturn(FileType.FILE).atLeastOnce();
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.isWriteable()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.delete()).andThrow(new FileSystemException("test")).atLeastOnce();

    replay(fileObjectMock);

    Response response = getFileResource().deleteFile("path");
    Assert.assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus());
    Assert.assertEquals("couldNotDeleteFileError", response.getEntity());

    verify(fileObjectMock);
  }

  private FilesResource getFileResource() {
    FilesResource resource = new FilesResource() {
      @Override
      protected FileObject resolveFileInFileSystem(String path) throws FileSystemException {
        return fileObjectMock;
      }
    };
    resource.setOpalRuntime(opalRuntimeMock);
    resource.setSubjectAclService(subjectAclServiceMock);
    return resource;
  }

  @Test
  public void testCreateFolder_CannotCreateFolderPathAlreadyExist() throws FileSystemException, URISyntaxException {
    expect(fileObjectMock.getType()).andReturn(FileType.FOLDER).atLeastOnce();
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.resolveFile("folder11")).andReturn(fileSystem.getRoot().resolveFile("/folder1/folder11"))
        .atLeastOnce();

    replay(fileObjectMock);

    Response response = getFileResource().createFolder("folder1", "folder11", uriInfoMock);
    Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
    Assert.assertEquals("cannotCreateFolderPathAlreadyExist", response.getEntity());

    verify(fileObjectMock);
  }

  @Test
  public void testCreateFolder_CannotCreateFolderParentIsReadOnly() throws FileSystemException, URISyntaxException {
    expect(fileObjectMock.getType()).andReturn(FileType.FOLDER).atLeastOnce();
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();
    FileObject childFolderMock = createMock(FileObject.class);
    expect(childFolderMock.exists()).andReturn(false).atLeastOnce();
    FileObject parentFolderMock = createMock(FileObject.class);
    expect(childFolderMock.getParent()).andReturn(parentFolderMock).atLeastOnce();
    expect(parentFolderMock.isWriteable()).andReturn(false).atLeastOnce();
    expect(fileObjectMock.resolveFile("folder")).andReturn(childFolderMock).atLeastOnce();

    replay(fileObjectMock, parentFolderMock, childFolderMock);

    Response response = getFileResource().createFolder("folder1", "folder", uriInfoMock);
    Assert.assertEquals(Status.FORBIDDEN.getStatusCode(), response.getStatus());
    Assert.assertEquals("cannotCreateFolderParentIsReadOnly", response.getEntity());

    verify(fileObjectMock, parentFolderMock, childFolderMock);
  }

  @Test
  public void testCreateFolder_FolderCreatedSuccessfully() throws FileSystemException, URISyntaxException {
    expect(fileObjectMock.getType()).andReturn(FileType.FOLDER).atLeastOnce();
    expect(fileObjectMock.exists()).andReturn(true).atLeastOnce();

    FileObject childFolderMock = createMock(FileObject.class);

    FileName fileNameMock = createMock(FileName.class);
    expect(fileNameMock.getBaseName()).andReturn("folder").atLeastOnce();
    expect(fileNameMock.getPath()).andReturn("folder1/folder").atLeastOnce();

    expect(childFolderMock.getName()).andReturn(fileNameMock).atLeastOnce();
    expect(childFolderMock.exists()).andReturn(false).atLeastOnce();

    FileContent mockContent = createMock(FileContent.class);
    expect(childFolderMock.getContent()).andReturn(mockContent).atLeastOnce();
    expect(mockContent.getLastModifiedTime()).andReturn((long) 1).atLeastOnce();

    childFolderMock.createFolder();
    FileObject parentFolderMock = createMock(FileObject.class);
    expect(childFolderMock.getParent()).andReturn(parentFolderMock).atLeastOnce();
    expect(childFolderMock.isReadable()).andReturn(true).atLeastOnce();
    expect(childFolderMock.isWriteable()).andReturn(true).atLeastOnce();
    expect(parentFolderMock.isWriteable()).andReturn(true).atLeastOnce();
    expect(fileObjectMock.resolveFile("folder")).andReturn(childFolderMock).atLeastOnce();
    expect(uriInfoMock.getBaseUriBuilder()).andReturn(UriBuilder.fromPath("/"));

    replay(fileObjectMock, uriInfoMock, parentFolderMock, childFolderMock, fileNameMock, mockContent);

    Response response = getFileResource().createFolder("folder1", "folder", uriInfoMock);
    Assert.assertEquals(Status.CREATED.getStatusCode(), response.getStatus());

    verify(fileObjectMock, uriInfoMock, parentFolderMock, childFolderMock, fileNameMock, mockContent);
  }

  @Test
  public void testCharsetsAvailable() throws Exception {
    Response charSets = filesResource.getAvailableCharsets();
    assertThat(charSets.getEntity().toString(), Matchers.containsString("UTF-8"));
  }
}
