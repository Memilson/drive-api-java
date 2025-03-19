import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;

public class UploadService {
    private final Drive driveService;

    public UploadService(Credential credential) {
        this.driveService = new Drive.Builder(
                GoogleNetHttpTransport.newTrustedTransport(),
                GsonFactory.getDefaultInstance(),
                credential)
                .setApplicationName("Google Drive API Java Quickstart")
                .build();
    }

    public void uploadFile(String filePath, String mimeType) throws IOException {
        java.io.File file = new java.io.File(filePath);
        if (!file.exists()) {
            throw new FileNotFoundException("File not found: " + filePath);
        }

        FileContent mediaContent = new FileContent(mimeType, file);
        File fileMetadata = new File();
        fileMetadata.setName(file.getName());

        File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        System.out.println("File uploaded successfully with ID: " + uploadedFile.getId());
    }
}
