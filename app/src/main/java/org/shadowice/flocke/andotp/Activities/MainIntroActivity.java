package org.shadowice.flocke.andotp.Activities;

import android.app.KeyguardManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.heinrichreimersoftware.materialintro.app.IntroActivity;
import com.heinrichreimersoftware.materialintro.app.SlideFragment;
import com.heinrichreimersoftware.materialintro.slide.FragmentSlide;
import com.heinrichreimersoftware.materialintro.slide.SimpleSlide;

import org.shadowice.flocke.andotp.R;
import org.shadowice.flocke.andotp.Utilities.Constants;
import org.shadowice.flocke.andotp.Utilities.Settings;

public class MainIntroActivity extends IntroActivity {
    private Settings settings;

    private EncryptionFragment encryptionFragment;
    private AuthenticationFragment authenticationFragment;

    @Override protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        settings = new Settings(this);

        encryptionFragment = new EncryptionFragment();
        authenticationFragment = new AuthenticationFragment();

        encryptionFragment.setEncryptionChangedCallback(new EncryptionFragment.EncryptionChangedCallback() {
            @Override
            public void onEncryptionChanged(Constants.EncryptionType newEncryptionType) {
                authenticationFragment.updateEncryptionType(newEncryptionType);
                settings.setEncryption(newEncryptionType);
            }
        });

        setButtonBackFunction(BUTTON_BACK_FUNCTION_BACK);

        addSlide(new SimpleSlide.Builder()
                .title(R.string.intro_slide1_title)
                .description(R.string.intro_slide1_desc)
                .image(R.mipmap.ic_launcher)
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .canGoBackward(false)
                .scrollable(false)
                .build()
        );

        addSlide(new FragmentSlide.Builder()
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .fragment(encryptionFragment)
                .build()
        );

        addSlide(new FragmentSlide.Builder()
                .background(R.color.colorPrimary)
                .backgroundDark(R.color.colorPrimaryDark)
                .fragment(authenticationFragment)
                .build()
        );
    }

    @Override
    public void onBackPressed() {
        if (getCurrentSlidePosition() != 0)
            super.onBackPressed();
    }

    public static class EncryptionFragment extends SlideFragment {
        private EncryptionChangedCallback encryptionChangedCallback = null;

        private Spinner selection;
        private TextView desc;

        private SparseArray<Constants.EncryptionType> selectionMapping;

        public EncryptionFragment() {
        }

        public void setEncryptionChangedCallback(EncryptionChangedCallback cb) {
            encryptionChangedCallback = cb;
        }

        private void generateSelectionMapping() {
            String[] encValues = getResources().getStringArray(R.array.settings_values_encryption);

            selectionMapping = new SparseArray<>();
            for (int i = 0; i < encValues.length; i++)
                selectionMapping.put(i, Constants.EncryptionType.valueOf(encValues[i].toUpperCase()));
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.component_intro_encryption, container, false);

            selection = root.findViewById(R.id.introEncryptionSelection);
            desc = root.findViewById(R.id.introEncryptionDesc);

            generateSelectionMapping();

            selection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Constants.EncryptionType encryptionType = selectionMapping.get(i);

                    if (encryptionType == Constants.EncryptionType.PASSWORD)
                        desc.setText(R.string.intro_slide2_desc_password);
                    else if (encryptionType == Constants.EncryptionType.KEYSTORE)
                        desc.setText(R.string.intro_slide2_desc_keystore);

                    if (encryptionChangedCallback != null)
                        encryptionChangedCallback.onEncryptionChanged(encryptionType);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

            return root;
        }

        public interface EncryptionChangedCallback {
            void onEncryptionChanged(Constants.EncryptionType newEncryptionType);
        }
    }

    public static class AuthenticationFragment extends SlideFragment {
        private Constants.EncryptionType encryptionType = Constants.EncryptionType.KEYSTORE;

        private TextView desc = null;
        private Spinner selection = null;
        private TextView authWarnings = null;
        private LinearLayout credentialsLayout = null;
        private TextInputEditText passwordInput = null;
        private EditText passwordConfirm = null;

        private SparseArray<Constants.AuthMethod> selectionMapping;

        public AuthenticationFragment() {
        }

        public void updateEncryptionType(Constants.EncryptionType encryptionType) {
            this.encryptionType = encryptionType;

            if (desc != null) {
                if (encryptionType == Constants.EncryptionType.KEYSTORE) {
                    desc.setText(R.string.intro_slide3_desc_keystore);
                } else if (encryptionType == Constants.EncryptionType.PASSWORD) {
                    desc.setText(R.string.intro_slide3_desc_password);

                    Constants.AuthMethod selectedMethod = selectionMapping.get(selection.getSelectedItemPosition());
                    if (selectedMethod != Constants.AuthMethod.PASSWORD && selectedMethod != Constants.AuthMethod.PIN )
                        selection.setSelection(selectionMapping.indexOfValue(Constants.AuthMethod.PASSWORD));
                }
            }
        }

        private void generateSelectionMapping() {
            Constants.AuthMethod[] authValues = Constants.AuthMethod.values();

            selectionMapping = new SparseArray<>();
            for (int i = 0; i < authValues.length; i++)
                selectionMapping.put(i, authValues[i]);
        }

        private void displayWarning(int resId) {
            displayWarning(getString(resId));
        }

        private void displayWarning(String warning) {
            authWarnings.setVisibility(View.VISIBLE);
            authWarnings.setText(warning);
        }

        private void hideWarning() {
            authWarnings.setVisibility(View.GONE);
        }

        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View root = inflater.inflate(R.layout.component_intro_authentication, container, false);

            desc = root.findViewById(R.id.introAuthDesc);
            selection = root.findViewById(R.id.introAuthSelection);
            authWarnings = root.findViewById(R.id.introAuthWarnings);
            credentialsLayout = root.findViewById(R.id.introCredentialsLayout);
            passwordInput = root.findViewById(R.id.introPasswordEdit);
            passwordConfirm = root.findViewById(R.id.introPasswordConfirm);

            generateSelectionMapping();

            final String[] authEntries = getResources().getStringArray(R.array.settings_entries_auth);
            ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(getIntroActivity(), android.R.layout.simple_spinner_item, authEntries) {
                @Override
                public boolean isEnabled(int position){
                        return encryptionType != Constants.EncryptionType.PASSWORD ||
                                position == selectionMapping.indexOfValue(Constants.AuthMethod.PASSWORD) ||
                                position == selectionMapping.indexOfValue(Constants.AuthMethod.PIN);
                }

                @Override
                public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                    View view = super.getDropDownView(position, convertView, parent);
                    TextView tv = (TextView) view;

                    tv.setEnabled(encryptionType != Constants.EncryptionType.PASSWORD ||
                            position == selectionMapping.indexOfValue(Constants.AuthMethod.PASSWORD) ||
                            position == selectionMapping.indexOfValue(Constants.AuthMethod.PIN));

                    return view;
                }
            };

            spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            selection.setAdapter(spinnerArrayAdapter);

            selection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    Constants.AuthMethod authMethod = selectionMapping.get(i);

                    if (authMethod == Constants.AuthMethod.PASSWORD || authMethod == Constants.AuthMethod.PIN) {
                        credentialsLayout.setVisibility(View.VISIBLE);
                    } else {
                        credentialsLayout.setVisibility(View.INVISIBLE);
                    }

                    updateNavigation();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {
                }
            });

            TextWatcher textWatcher = new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                    updateNavigation();
                }

                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                }

                @Override
                public void afterTextChanged(Editable editable) {
                }
            };

            passwordInput.addTextChangedListener(textWatcher);
            passwordConfirm.addTextChangedListener(textWatcher);

            return root;
        }

        @Override
        public boolean canGoForward() {
            Constants.AuthMethod authMethod = selectionMapping.get(selection.getSelectedItemPosition());

            if (authMethod == Constants.AuthMethod.PIN || authMethod == Constants.AuthMethod.PASSWORD) {
                String password = passwordInput.getText().toString();
                String confirm = passwordConfirm.getText().toString();

                return ! password.isEmpty() && ! confirm.isEmpty() && confirm.equals(password);
            } else if (authMethod == Constants.AuthMethod.DEVICE) {
                KeyguardManager km = (KeyguardManager) getContext().getSystemService(KEYGUARD_SERVICE);

                if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
                    displayWarning(R.string.settings_toast_auth_device_pre_lollipop);
                    return false;
                } else if (! km.isKeyguardSecure()) {
                    displayWarning(R.string.settings_toast_auth_device_not_secure);
                    return false;
                }

                hideWarning();

                return true;
            } else {
                hideWarning();

                return true;
            }
        }
    }
}
