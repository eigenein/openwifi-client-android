package info.eigenein.openwifi.helpers.services;

import android.accounts.*;
import android.app.*;
import android.content.*;
import android.os.*;
import android.provider.*;
import android.util.*;
import android.widget.*;
import info.eigenein.openwifi.*;

public class Authenticator {
    private static final String LOG_TAG = Authenticator.class.getCanonicalName();

    public static final String GOOGLE_ACCOUNT_TYPE = "com.google";
    private static final String GOOGLE_AUTH_TOKEN_TYPE = "oauth2:https://www.googleapis.com/auth/userinfo.profile";

    public interface AuthenticatedHandler {
        void onAuthenticated(final String authToken, final String accountName);
    }

    @SuppressWarnings("deprecation")
    public static void authenticate(
            final Context context,
            final boolean invalidateExistingToken,
            final boolean notifyAuthFailure,
            final boolean askUser,
            final AuthenticatedHandler handler) {
        // Check the parameters.
        assert(context != null);
        assert(handler != null);

        final AccountManager accountManager = AccountManager.get(context);
        final Account[] accounts = accountManager.getAccountsByType(GOOGLE_ACCOUNT_TYPE);

        // Check if there is any account.
        Log.d(LOG_TAG, String.format("Got %s account(s).", accounts.length));
        if (accounts.length == 0) {
            // Notify the user.
            Toast.makeText(context, R.string.toast_no_google_account, Toast.LENGTH_SHORT).show();
            // Choose whether to go to the account sync settings.
            if (askUser) {
                // Go to the account settings.
                context.startActivity(new Intent(Settings.ACTION_SYNC_SETTINGS));
            } else {
                // Notify that we're not authenticated.
                handler.onAuthenticated(null, null);
            }
            return;
        }

        // TODO: Choose the account.
        final Account account = accounts[0];
        Log.d(LOG_TAG, "Account: " + account.name);

        // Display the progress.
        final ProgressDialog getAuthTokenProgressDialog = !askUser ? null : ProgressDialog.show(
                context,
                context.getString(R.string.dialog_get_auth_token_title),
                context.getString(R.string.dialog_get_auth_token_message),
                true);

        // Obtain the existing token if any.
        Log.d(LOG_TAG, "Obtaining the existing token ...");
        accountManager.getAuthToken(
                account,
                GOOGLE_AUTH_TOKEN_TYPE,
                false,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(final AccountManagerFuture<Bundle> existingTokenBundle) {
                        if (invalidateExistingToken) {
                            try {
                                final String existingAuthToken = existingTokenBundle.getResult().getString(AccountManager.KEY_AUTHTOKEN);
                                if (existingAuthToken != null) {
                                    // Invalidate existing token.
                                    Log.d(LOG_TAG, "Invalidating the existing token ...");
                                    accountManager.invalidateAuthToken(GOOGLE_ACCOUNT_TYPE, existingAuthToken);
                                }
                            } catch (Exception e) {
                                // Hide the dialog in the case of an error.
                                if (getAuthTokenProgressDialog != null) {
                                    getAuthTokenProgressDialog.hide();
                                }
                                // Re-throw the exception.
                                throw new RuntimeException("Failed to invalidate the existing token.", e);
                            }
                        }
                        // Request for new authentication token.
                        Log.d(LOG_TAG, "Obtaining the actual token ...");
                        accountManager.getAuthToken(
                                account,
                                GOOGLE_AUTH_TOKEN_TYPE,
                                notifyAuthFailure,
                                new AccountManagerCallback<Bundle>() {
                                    @Override
                                    public void run(AccountManagerFuture<Bundle> newTokenBundle) {
                                        try {
                                            final Bundle result = newTokenBundle.getResult();
                                            final Intent intent = result.getParcelable(AccountManager.KEY_INTENT);

                                            if (intent == null) {
                                                Log.d(LOG_TAG, "Getting authentication data ...");
                                                // Get the authentication data.
                                                final String authToken = result.getString(AccountManager.KEY_AUTHTOKEN);
                                                final String accountName = result.getString(AccountManager.KEY_ACCOUNT_NAME);
                                                // Pass to the handler.
                                                Log.d(LOG_TAG, "Authenticated with " + accountName);
                                                handler.onAuthenticated(authToken, accountName);
                                            } else {
                                                if (askUser) {
                                                    // Ask the user for permissions.
                                                    Log.d(LOG_TAG, "Asking for the user ...");
                                                    context.startActivity(intent);
                                                } else {
                                                    // We could not be authenticated then.
                                                    Log.d(LOG_TAG, "The user should be asked but was not.");
                                                    handler.onAuthenticated(null, null);
                                                }
                                            }
                                        } catch (Exception e) {
                                            throw new RuntimeException("Failed to obtain new token.", e);
                                        } finally {
                                            // Anyway, hide the dialog.
                                            if (getAuthTokenProgressDialog != null) {
                                                getAuthTokenProgressDialog.hide();
                                            }
                                        }
                                    }
                                },
                                null
                        );
                    }
                },
                null);
    }
}
