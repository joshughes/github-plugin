package com.cloudbees.jenkins;

import hudson.Extension;
import hudson.Util;
import hudson.model.AbstractDescribableImpl;
import hudson.model.Descriptor;
import hudson.util.FormValidation;
import hudson.util.Secret;
import org.kohsuke.github.GitHub;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.io.IOException;

/**
 * Credential to access GitHub.
 *
 * @author Kohsuke Kawaguchi
 */
public class Credential extends AbstractDescribableImpl<Credential> {
    public final String username;
    public final String apiUrl;
    public final String apiToken;
    public final Secret password;

    @DataBoundConstructor
    public Credential(String username, Secret password, String apiUrl, String apiToken) {
        this.username = username;
        this.password = password;
        this.apiUrl = apiUrl;
        this.apiToken = apiToken;
    }

    public GitHub login() throws IOException {
        if (Util.fixEmpty(apiUrl) != null) {
            return GitHub.connectToEnterprise(apiUrl,username,apiToken);
        }
        return GitHub.connect(username,apiToken,password.getPlainText());
    }

    @Extension
    public static class DescriptorImpl extends Descriptor<Credential> {
        @Override
        public String getDisplayName() {
            return ""; // unused
        }

        public FormValidation doValidate(@QueryParameter String apiUrl, @QueryParameter String username, @QueryParameter Secret password, @QueryParameter String apiToken) throws IOException {
            GitHub gitHub;
            if (Util.fixEmpty(apiUrl) != null) {
                gitHub = GitHub.connectToEnterprise(apiUrl,apiToken);
            } else {
                gitHub = GitHub.connect(username,apiToken,Secret.toString(password));
            }

            if (gitHub.isCredentialValid())
                return FormValidation.ok("Verified");
            else
                return FormValidation.error("Failed to validate the account");
        }
    }
}
