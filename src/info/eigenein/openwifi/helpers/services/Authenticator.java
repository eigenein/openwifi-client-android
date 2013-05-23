package info.eigenein.openwifi.helpers.services;

import android.accounts.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.provider.*;
import android.widget.*;
import info.eigenein.openwifi.*;

public class Authenticator {
    public static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    private static final String GOOGLE_AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

    public interface AuthenticatedHandler {
        void onAuthenticated(final String authToken, final String accountName);
    }

    @SuppressWarnings("deprecation")
    public static void authenticate(
            final Context context,
            final boolean invalidateExisting,
            final boolean notifyAuthFailure,
            final boolean askUser,
            final AuthenticatedHandler handler) {
        // Check the parameters.
        assert(context != null);
        assert(handler != null);

        final AccountManager accountManager = AccountManager.get(context);
        final Account[] accounts = accountManager.getAccountsByType(GOOGLE_ACCOUNT_TYPE);

        // Check if there is any account.
        if (accounts.length == 0) {
            if (askUser) {
                // Notify the user.
                Toast.makeText(context, R.string.toast_no_google_account, Toast.LENGTH_SHORT).show();
                // Go to the account settings.
                context.startActivity(new Intent(Settings.ACTION_SYNC_SETTINGS));
            } else {
                // Notify that not authenticated.
                handler.onAuthenticated(null, null);
            }
            return;
        }

        // Display the progress.
        final ProgressDialog getAuthTokenProgressDialog = !askUser ? null : ProgressDialog.show(
                context,
                context.getString(R.string.dialog_get_auth_token_title),
                context.getString(R.string.dialog_get_auth_token_message),
                true);
        // Obtain new token.
        accountManager.getAuthToken(
                accounts[0], // TODO
                GOOGLE_AUTH_TOKEN_TYPE,
                notifyAuthFailure,
                new android.accounts.AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> bundleAccountManager) {
                        try {
                            // Hide the progress if shown.
                            if (getAuthTokenProgressDialog != null) {
                                getAuthTokenProgressDialog.dismiss();
                            }

                            final Bundle result = bundleAccountManager.getResult();
                            final Intent intent = result.getParcelable(AccountManager.KEY_INTENT);

                            if (intent != null && askUser) {
                                // Ask the user for credentials and etc.
                                context.startActivity(intent);
                            } else {
                                // Get the authentication data.
                                final String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
                                final String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                                if (!invalidateExisting) {
                                    // Call the handler with the authentication token.
                                    handler.onAuthenticated(authToken, accountName);
                                } else {
                                    // Invalidate the existing token.
                                    accountManager.invalidateAuthToken(GOOGLE_ACCOUNT_TYPE, authToken);
                                    // Authenticate again.
                                    authenticate(context, false, notifyAuthFailure, askUser, handler);
                                }
                            }
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to process the authentication result.", e);
                        }
                    }
                },
                null);
    }
}
