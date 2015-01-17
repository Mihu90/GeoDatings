/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ro.mihaisurdeanu.geodatings.utilities;

import android.content.Context;
import android.content.res.Resources;

import com.google.android.gms.common.ConnectionResult;
import ro.mihaisurdeanu.geodatings.R;

/**
 * Clasa prin care se mapeaza mai multe coduri de eroare cu mesaje specifice.
 */
public class LocationServiceErrorMessages {

    public static String getErrorString(Context context, int errorCode) {
        // Pentru a obtine mesajele va trebui sa avem acces la resurse.
        Resources resources = context.getResources();

        // Mesajul de eroare ce va fi afisat
        String errorString;

        // Mesajul de eroare va fi diferit in functie de codul de eroare
        switch (errorCode) {
            case ConnectionResult.DEVELOPER_ERROR:
                errorString = resources.getString(R.string.connection_error_misconfigured);
                break;
            case ConnectionResult.INTERNAL_ERROR:
                errorString = resources.getString(R.string.connection_error_internal);
                break;
            case ConnectionResult.INVALID_ACCOUNT:
                errorString = resources.getString(R.string.connection_error_invalid_account);
                break;
            case ConnectionResult.LICENSE_CHECK_FAILED:
                errorString = resources.getString(R.string.connection_error_license_check_failed);
                break;
            case ConnectionResult.NETWORK_ERROR:
                errorString = resources.getString(R.string.connection_error_network);
                break;
            case ConnectionResult.RESOLUTION_REQUIRED:
                errorString = resources.getString(R.string.connection_error_needs_resolution);
                break;
            case ConnectionResult.SERVICE_DISABLED:
                errorString = resources.getString(R.string.connection_error_disabled);
                break;
            case ConnectionResult.SERVICE_INVALID:
                errorString = resources.getString(R.string.connection_error_invalid);
                break;
            case ConnectionResult.SERVICE_MISSING:
                errorString = resources.getString(R.string.connection_error_missing);
                break;
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                errorString = resources.getString(R.string.connection_error_outdated);
                break;
            case ConnectionResult.SIGN_IN_REQUIRED:
                errorString = resources.getString(R.string.connection_error_sign_in_required);
                break;
            default:
                errorString = resources.getString(R.string.connection_error_unknown);
                break;
        }

        return errorString;
    }
}
