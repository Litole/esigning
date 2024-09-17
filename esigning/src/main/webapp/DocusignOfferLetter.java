import com.docusign.esign.api.EnvelopesApi;
import com.docusign.esign.client.ApiClient;
import com.docusign.esign.client.ApiException;
import com.docusign.esign.model.*;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Arrays;

public class DocuSignOfferLetter {

    // Constants for authentication (must be set with your values)
    private static final String INTEGRATOR_KEY = "your-integrator-key";
    private static final String USER_ID = "your-user-id";
    private static final String AUTH_SERVER = "https://account-d.docusign.com";
    private static final String ACCOUNT_ID = "your-account-id";
    private static final String BASE_PATH = "https://demo.docusign.net/restapi";
    private static final String ACCESS_TOKEN = "your-access-token";

    // Database credentials
    private static final String DB_URL = "jdbc:mysql://localhost:3306/your_database";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";

    public static void main(String[] args) {
        try {
            // Initialize API client
            ApiClient apiClient = new ApiClient(BASE_PATH);
            apiClient.setAccessToken(ACCESS_TOKEN, 3600);

            EnvelopesApi envelopesApi = new EnvelopesApi(apiClient);

            // Prepare the recipient (internal signer)
            Signer signer = new Signer();
            signer.setEmail("signer-email@example.com");
            signer.setName("Internal Signer");
            signer.setRecipientId("1");
            signer.setRoutingOrder("1");

            // Create the document (Letter of Offer)
            Document doc = new Document();
            String documentBase64 = "Base64-encoded-contents-of-letter-of-offer";
            doc.setDocumentBase64(documentBase64);
            doc.setName("Letter of Offer");
            doc.setDocumentId("1");

            // Create SignHere tab for the internal signer
            SignHere signHere = new SignHere();
            signHere.setDocumentId("1");
            signHere.setPageNumber("1");
            signHere.setRecipientId("1");
            signHere.setTabLabel("SignHereTab");
            signHere.setXPosition("200");
            signHere.setYPosition("300");

            // Add the tab to the signer
            Tabs signerTabs = new Tabs();
            signerTabs.setSignHereTabs(Arrays.asList(signHere));
            signer.setTabs(signerTabs);

            // Create envelope definition
            EnvelopeDefinition envelopeDefinition = new EnvelopeDefinition();
            envelopeDefinition.setEmailSubject("Please sign the Letter of Offer");
            envelopeDefinition.setDocuments(Arrays.asList(doc));
            envelopeDefinition.setRecipients(new Recipients().signers(Arrays.asList(signer)));
            envelopeDefinition.setStatus("sent");

            // Send the envelope
            EnvelopeSummary envelopeSummary = envelopesApi.createEnvelope(ACCOUNT_ID, envelopeDefinition);
            System.out.println("Envelope has been sent! Envelope ID: " + envelopeSummary.getEnvelopeId());

            // Save envelope data to database
            saveEnvelopeToDatabase(envelopeSummary.getEnvelopeId(), "Letter of Offer", signer.getEmail());

        } catch (ApiException e) {
            System.err.println("Exception: " + e);
            e.printStackTrace();
        } catch (SQLException e) {
            System.err.println("SQL Exception: " + e.getMessage());
        }
    }

    // Method to save envelope details to the database
    private static void saveEnvelopeToDatabase(String envelopeId, String documentName, String signerEmail) throws SQLException {
        Connection connection = null;
        PreparedStatement preparedStatement = null;

        try {
            // Establish connection
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);

            // SQL Insert statement
            String sql = "INSERT INTO envelopes (envelope_id, document_name, signer_email) VALUES (?, ?, ?)";

            preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setString(1, envelopeId);
            preparedStatement.setString(2, documentName);
            preparedStatement.setString(3, signerEmail);

            // Execute update
            int rowsInserted = preparedStatement.executeUpdate();
            if (rowsInserted > 0) {
                System.out.println("A new envelope was inserted successfully!");
            }

        } finally {
            // Close resources
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (connection != null) {
                connection.close();
            }
        }
    }
}
