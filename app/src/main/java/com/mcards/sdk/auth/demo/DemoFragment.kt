package com.mcards.sdk.auth.demo

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar
import com.mcards.sdk.auth.AuthSdk
import com.mcards.sdk.auth.AuthSdkProvider
import com.mcards.sdk.auth.demo.databinding.FragmentDemoBinding
import com.mcards.sdk.auth.model.auth.DeepLink
import com.mcards.sdk.auth.model.auth.User
import com.mcards.sdk.auth.model.metadata.UserMetadata
import com.mcards.sdk.core.model.AuthTokens
import com.mcards.sdk.core.network.model.SdkResult
import com.mcards.sdk.core.util.LoggingCallback
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable

private const val TEST_PHONE_NUMBER = "4052938132"

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class DemoFragment : Fragment() {

    private var _binding: FragmentDemoBinding? = null
    private val binding get() = _binding!!
    private var userPhoneNumber = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDemoBinding.inflate(inflater)
        return binding.root
    }

    @SuppressLint("CheckResult")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val loginCallback = object : AuthSdk.Auth0Callback {
            override fun onSuccess(
                user: User,
                tokens: AuthTokens,
                regionChanged: Boolean,
                cardId: String?
            ) {
                // TODO access needed user and token data:

                val accessToken = tokens.accessToken
                val idToken = tokens.idToken
                userPhoneNumber = user.userClaim.phoneNumber
                val userRegion = user.regionClaim.name
                // etc

                getUserProfileMetadata()
            }

            override fun onFailure(message: String) {
                Snackbar.make(view, message, Snackbar.LENGTH_LONG).show()
            }
        }

        val authSdk = AuthSdkProvider.getInstance()

        //optional, to use your standard logging methods
        authSdk.setLoggingCallback(object : LoggingCallback {
            override fun log(t: Throwable) {
                //TODO log exception
            }

            override fun log(msg: String) {
                //TODO log message
            }
        })

        binding.quickLoginBtn.setOnClickListener {
            if (userPhoneNumber.isBlank()) {
                // Basic login, displays the auth0 UI to the user and returns auth tokens after
                // successful login. All the login methods will first attempt an automatic session
                // refresh without forcing credentials entry if possible, unless
                // forceReauthentication() has been called.
                authSdk.auth0Authenticate(requireContext(), TEST_PHONE_NUMBER, loginCallback)
            } else {
                // if you already have the user's phone number, use this login overload to
                // prepopulate it on the auth0 login screen:
                authSdk.auth0Authenticate(requireContext(), userPhoneNumber, loginCallback)
            }

            val deepLink = DeepLink.getDummyBalanceTransfer()

            // if the user is logging in via a firebase dynamic link or deep link, use this
            // override to pass the data in the deep link to our system:
            //authSdk.login(requireContext(), deepLink, loginCallback)

            // if you have the user's phone number and a relevant deeplink:
            //authSdk.login(requireContext(), deepLink, userPhoneNumber, loginCallback)
        }

        binding.credsLoginBtn.setOnClickListener {
            // Forces the user to enter login credentials the next time you call any login()
            // method. Must be called again before each successive login if you want to keep
            // forcing full creds entry.
            authSdk.forceAuth0Reauthentication(requireContext())
            authSdk.auth0Authenticate(requireContext(), TEST_PHONE_NUMBER, loginCallback)
        }

        binding.logoutBtn.setOnClickListener {
            authSdk.mCardsLogout(requireContext())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : SingleObserver<SdkResult<Boolean>> {
                    override fun onSubscribe(d: Disposable) {
                        activity?.runOnUiThread {
                            binding.progressBar.visibility = View.VISIBLE
                        }
                    }

                    override fun onError(e: Throwable) {
                        activity?.runOnUiThread {
                            binding.progressBar.visibility = View.GONE
                        }
                    }

                    override fun onSuccess(t: SdkResult<Boolean>) {
                        val msg = if (t.isSuccessful) {
                            "Logged out"
                        } else {
                            t.errorMsg!!
                        }

                        activity?.runOnUiThread {
                            Snackbar.make(view, msg, BaseTransientBottomBar.LENGTH_LONG).show()
                            binding.progressBar.visibility = View.GONE
                        }

                        //TODO take any needed action on successful logout
                    }
                })
        }
    }

    @SuppressLint("CheckResult")
    private fun getUserProfileMetadata() {
        // example SDK operation
        // in this case, since login was handled by the AuthSdk, it already has the token so we can
        // immediately make API calls without having to set the token as a separate step
        AuthSdkProvider.getInstance().getUserMetadata()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : SingleObserver<SdkResult<UserMetadata>> {
                override fun onSubscribe(d: Disposable) {
                    activity?.runOnUiThread {
                        binding.progressBar.visibility = View.VISIBLE
                    }
                }

                override fun onError(e: Throwable) {
                    activity?.runOnUiThread {
                        Snackbar.make(requireView(), e.localizedMessage!!, BaseTransientBottomBar.LENGTH_LONG)
                            .show()
                        binding.progressBar.visibility = View.GONE
                    }
                }

                override fun onSuccess(t: SdkResult<UserMetadata>) {
                    t.result?.let {
                        val requiresAddress = it.profileMetadata?.requiresAddress
                        //TODO take some action based on the ProfileMetadata
                        Snackbar.make(requireView(), "Logged in and fetched profile metadata " +
                                "successfully", BaseTransientBottomBar.LENGTH_LONG).show()
                    } ?: t.errorMsg?.let {
                        activity?.runOnUiThread {
                            Snackbar.make(requireView(), it, BaseTransientBottomBar.LENGTH_LONG)
                                .show()
                        }
                    }

                    activity?.runOnUiThread {
                        binding.progressBar.visibility = View.GONE
                    }
                }
            })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
