import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DriveBackup {
    private static final String APPLICATION_NAME = "Google Drive Backup";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        InputStream in = DriveBackup.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private static String uploadFile(Drive service, String filePath, String folderId) throws IOException {
        java.io.File file = new java.io.File(filePath);
        File fileMetadata = new File();
        fileMetadata.setName(file.getName());
        if (folderId != null) {
            fileMetadata.setParents(Collections.singletonList(folderId));
        }
        FileContent mediaContent = new FileContent("application/octet-stream", file);
        File uploadedFile = service.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();
        return uploadedFile.getId();
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        List<String> sourcePaths = new ArrayList<>();
        sourcePaths.add("/mnt/c/Users/chell/Downloads/desktop.ini");

        List<String> uploadedFileIds = new ArrayList<>();

        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();

        for (String sourcePath : sourcePaths) {
            Path path = Paths.get(sourcePath);
            if (Files.isDirectory(path)) {
                Files.list(path).forEach(filePath -> {
                    try {
                        String fileId = uploadFile(service, filePath.toString(), null);
                        uploadedFileIds.add(fileId);
                        System.out.println("Arquivo enviado: " + filePath.getFileName() + " (ID: " + fileId + ")");
                    } catch (IOException e) {
                        System.err.println("Erro ao enviar arquivo: " + filePath.getFileName());
                        e.printStackTrace();
                    }
                });
            } else {
                String fileId = uploadFile(service, sourcePath, null);
                uploadedFileIds.add(fileId);
                System.out.println("Arquivo enviado: " + path.getFileName() + " (ID: " + fileId + ")");
            }
        }

        System.out.println("IDs dos arquivos enviados: " + uploadedFileIds);
    }
}