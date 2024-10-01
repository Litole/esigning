import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.Configuration;
import com.docusign.esign.client.auth.OAuth;
import com.docusign.esign.model.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.UUID;

@WebServlet("/PrepareLetterServlet")
@MultipartConfig
public class PrepareLetterServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    // DocuSign OAuth & API credentials
    private static final String DOCUSIGN_INTEGRATOR_KEY = "YOUR_INTEGRATOR_KEY";
    private static final String DOCUSIGN_ACCOUNT_ID = "YOUR_ACCOUNT_ID";
    private static final String DOCUSIGN_USER_ID = "YOUR_USER_ID";
    private static final String DOCUSIGN_OAUTH_BASE_PATH = "https://account.docusign.com";
    private static final String DOCUSIGN_API_BASE_PATH = "https://demo.docusign.net/restapi";

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String customerId = request.getParameter("customerId");
        Part filePart = request.getPart("document");
        
        // Save the uploaded file to a temporary location
        File tempFile = saveFileToTempLocation(filePart);
        
        // Create an envelope and get the signing URL from DocuSign
        String signingUrl = sendToDocuSign(tempFile, customerId);
        
        // Notify the customer by sending OTP (optional step, left as is)
        String otp = generateOtp();
        sendOtpToCustomer(customerId, otp);
        
        // Clean up temporary file
        tempFile.delete();
        
        // Redirect to DocuSign signing URL
        response.sendRedirect(signingUrl);
    }

    private File saveFileToTempLocation(Part filePart) throws IOException {
        String fileName = Paths.get(filePart.getSubmittedFileName()).getFileName().toString();
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir"));
        Path tempFilePath = Files.createTempFile("upload-" + UUID.randomUUID().toString() + "-", "-" + fileName);
        try (InputStream input = filePart.getInputStream()) {
            Files.copy(input, tempFilePath, StandardCopyOption.REPLACE_EXISTING);
        }
        return tempFilePath.toFile();
    }

    private String sendToDocuSign(File file, String customerId) throws IOException {
        try {
            // Step 1: Authenticate with DocuSign
            ApiClient apiClient = new ApiClient(DOCUSIGN_API_BASE_PATH);
            OAuth.OAuthToken oAuthToken = apiClient.requestJWTUserToken(DOCUSIGN_INTEGRATOR_KEY, DOCUSIGN_USER_ID, Collections.singletonList("signature"), Paths.get("path/to/privateKey.pem"), 3600);
            apiClient.setAccessToken(oAuthToken.getAccessToken(), oAuthToken.getExpiresIn());
            Configuration.setDefaultApiClient(apiClient);

            // Step 2: Create Envelope
            EnvelopeDefinition envelopeDefinition = new EnvelopeDefinition();
            envelopeDefinition.setEmailSubject("Please sign the Letter of Offer");

            // Add document to envelope
            Document document = new Document();
            document.setDocumentBase64(Base64.getEncoder().encodeToString(Files.readAllBytes(file.toPath())));
            document.setName("Letter of Offer");
            document.setFileExtension("docx"); // or "pdf"
            document.setDocumentId("1");

            envelopeDefinition.setDocuments(Collections.singletonList(document));

            // Add recipient (signer)
            Signer signer = new Signer();
            signer.setEmail("customer-email@example.com"); // Use customer email
            signer.setName("Customer Name");
            signer.setRecipientId("1");

            // Create a SignHere tab for the signer
            SignHere signHere = new SignHere();
            signHere.setAnchorString("/sn1/");
            signHere.setAnchorUnits("pixels");
            signHere.setAnchorXOffset("20");
            signHere.setAnchorYOffset("10");

            Tabs tabs = new Tabs();
            tabs.setSignHereTabs(Collections.singletonList(signHere));
            signer.setTabs(tabs);

            envelopeDefinition.setRecipients(new Recipients().signers(Collections.singletonList(signer)));
            envelopeDefinition.setStatus("sent"); // Send the envelope immediately

            // Step 3: Create and send the envelope using Envelopes API
            EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);
            EnvelopeSummary envelopeSummary = envelopesApi.createEnvelope(DOCUSIGN_ACCOUNT_ID, envelopeDefinition);

            // Step 4: Generate the signing URL (Recipient View URL)
            RecipientViewRequest viewRequest = new RecipientViewRequest();
            viewRequest.setReturnUrl("http://your-app-url/returnPage"); // URL to redirect after signing
            viewRequest.setAuthenticationMethod("none");
            viewRequest.setEmail("customer-email@example.com");
            viewRequest.setUserName("Customer Name");
            viewRequest.setRecipientId("1");

            ViewUrl recipientView = envelopesApi.createRecipientView(DOCUSIGN_ACCOUNT_ID, envelopeSummary.getEnvelopeId(), viewRequest);

            return recipientView.getUrl(); // Return the signing URL
        } catch (Exception e) {
            throw new IOException("Error sending to DocuSign: " + e.getMessage(), e);
        }
    }

    private String generateOtp() {
        return "123456"; // Simulate OTP generation
    }

    private void sendOtpToCustomer(String customerId, String otp) {
        System.out.println("Sending OTP " + otp + " to customer ID " + customerId);
        // Implement actual OTP sending logic
    }
}
