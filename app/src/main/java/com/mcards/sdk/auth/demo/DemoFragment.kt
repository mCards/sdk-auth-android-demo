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
import com.mcards.sdk.auth.model.auth.DeepLink.LinkData
import com.mcards.sdk.auth.model.auth.DeepLink.LinkMetadata
import com.mcards.sdk.auth.model.auth.DeepLink.LinkMetadata.AuthenticationStatus
import com.mcards.sdk.auth.model.auth.DeepLink.LinkMetadata.LinkActivity
import com.mcards.sdk.auth.model.auth.DeepLink.LinkMetadata.LinkCard
import com.mcards.sdk.auth.model.auth.DeepLink.LinkMetadata.LinkNotification
import com.mcards.sdk.auth.model.auth.DeepLink.LinkMetadata.LinkUser
import com.mcards.sdk.auth.model.auth.DeepLink.LinkType
import com.mcards.sdk.auth.model.auth.User
import com.mcards.sdk.auth.model.profile.ProfileMetadata
import com.mcards.sdk.core.model.AuthTokens
import com.mcards.sdk.core.network.SdkResult
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.SingleObserver
import io.reactivex.rxjava3.disposables.Disposable

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

        val loginCallback = object : AuthSdk.LoginCallback {
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

        binding.quickLoginBtn.setOnClickListener {
            if (userPhoneNumber.isBlank()) {
                // Basic login, displays the auth0 UI to the user and returns auth tokens after
                // successful login. All the login methods will first attempt an automatic session
                // refresh without forcing credentials entry if possible, unless
                // forceReauthentication() has been called.
                authSdk.login(requireContext(), loginCallback)
            } else {
                // if you already have the user's phone number, use this login overload to
                // prepopulate it on the auth0 login screen:
                authSdk.login(requireContext(), userPhoneNumber, loginCallback)
            }

            val deepLink = getDummyDeepLink(LinkType.CARD_LINKED)

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
            authSdk.forceReauthentication(requireContext())
            authSdk.login(requireContext(), loginCallback)
        }

        binding.logoutBtn.setOnClickListener {
            authSdk.logout(requireContext())
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
        AuthSdkProvider.getInstance().getUserProfileMetadata()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeWith(object : SingleObserver<SdkResult<ProfileMetadata>> {
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

                override fun onSuccess(t: SdkResult<ProfileMetadata>) {
                    t.result?.let {
                        val requiresAddress = it.requiresAddress
                        //TODO take some action based on the ProfileMetadata
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

    @Deprecated("We are migrating away from this deeplink implementation in 2025")
    private fun getDummyDeepLink(linkType: LinkType): DeepLink {
        val linkData = LinkData(linkType, "dummy link uuid")
        val linkMetadata = LinkMetadata(
            "us",
            AuthenticationStatus(false),
            LinkUser("dummy phone number"),
            LinkCard("dummy card uuid"),
            LinkActivity("dummy activity uuid"),
            LinkNotification("dummy notification title", "dummy notification body")
        )

        return DeepLink(linkData, linkMetadata)
    }
}
