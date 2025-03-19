import com.google.api.client.auth.oauth2.Credential;
import java.io.IOException;
import java.security.GeneralSecurityException;

public class Main {
    public static void main(String[] args) {
        try {
            // Autenticando o usuário
            Credential credential = AuthService.getCredentials();

            // Criando o serviço de upload
            UploadService uploadService = new UploadService(credential);

            // Fazendo upload do arquivo
            String filePath = "/path/to/your/file.txt";
            String mimeType = "text/plain";
            uploadService.uploadFile(filePath, mimeType);
        } catch (IOException | GeneralSecurityException e) {
            e.printStackTrace();
        }
    }
}
