package com.mcards.sdk.auth.demo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.Snackbar
import com.mcards.sdk.auth.AuthSdk
import com.mcards.sdk.auth.AuthSdkProvider
import com.mcards.sdk.auth.AuthViewModel
import com.mcards.sdk.auth.demo.databinding.FragmentFirstBinding
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
import com.mcards.sdk.core.model.AuthTokens

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null
    private val binding get() = _binding!!
    private var userPhoneNumber = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFirstBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val vm = AuthViewModel.get(requireActivity())
        requireActivity().runOnUiThread {
            vm.metadataResponse.observe(viewLifecycleOwner) { response ->
                response?.getData()?.let {
                    val metadata = it
                    val requiresAddress = metadata.requiresAddress
                }
            }
        }

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

                //since login was handled by the AuthSdk, it already has the token so we can
                // immediately make API calls
                vm.requestProfileMetadata()
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
                // if you already have the user's phone number, use this login overload to prepopulate
                // it on the auth0 login screen:
                authSdk.login(requireContext(), userPhoneNumber, loginCallback)
            }

            // if the user is logging in via a firebase dynamic link or deep link, use this
            // override to pass the data in the deep link to our system:
            val deepLink = getDummyDeepLink(LinkType.CARD_LINKED)
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

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
