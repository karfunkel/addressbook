package org.aklein.google

import com.google.api.client.auth.oauth2.Credential
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp
import com.google.api.client.extensions.java6.auth.oauth2.FileCredentialStore
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets
import com.google.api.client.http.HttpTransport
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.JsonFactory
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.oauth2.Oauth2
import com.google.api.services.oauth2.model.Tokeninfo
import com.google.api.services.oauth2.model.Userinfo

class GoogleAuthHelper {
    static final String APPLICATION_NAME = 'Addressbook'
    static final String SCOPE_CONTACTS = 'https://www.google.com/m8/feeds'

    private static HttpTransport HTTP_TRANSPORT
    private static final JsonFactory JSON_FACTORY = new JacksonFactory()

    private static Oauth2 oauth2
    private static GoogleClientSecrets clientSecrets

    static main(def args) {
        HTTP_TRANSPORT = new NetHttpTransport()
        Credential credential = authorize()
        oauth2 = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, credential).setApplicationName(APPLICATION_NAME).build()
        // run commands
        tokenInfo(credential.getAccessToken())
        userInfo()
    }

    /** Authorizes the installed application to access user's protected data. */
    private static Credential authorize() throws Exception {
        // load client secrets
        clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, getClass().getResourceAsStream("/client_secrets.json"))
        // set up file credential store
        FileCredentialStore credentialStore = new FileCredentialStore(new File(System.getProperty("user.home"), ".addressbook/oauth2.json"), JSON_FACTORY)
        // set up authorization code flow
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, [SCOPE_CONTACTS]).setCredentialStore(credentialStore).build()
        // authorize
        return new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user")
    }

    private static void tokenInfo(String accessToken) throws IOException {
        header("Validating a token")
        Tokeninfo tokeninfo = oauth2.tokeninfo().setAccessToken(accessToken).execute()
        //println tokeninfo.toPrettyString()
        if (!tokeninfo.audience == clientSecrets.getDetails().clientId) {
            System.err.println("ERROR: audience does not match our client ID!")
        }
    }

    private static void userInfo() throws IOException {
        header("Obtaining User Profile Information")
        Userinfo userinfo = oauth2.userinfo().get().execute()
        //println(userinfo.toPrettyString())
    }

    static void header(String name) {
        //println "\n================== $name ==================\n"
    }




}