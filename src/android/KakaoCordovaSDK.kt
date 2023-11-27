package com.needer.plugin.kakao

import android.content.ActivityNotFoundException
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import com.kakao.sdk.common.KakaoSdk
import com.kakao.sdk.common.model.AuthError
import com.kakao.sdk.common.util.KakaoCustomTabsClient
import com.kakao.sdk.user.UserApiClient
import org.apache.cordova.CallbackContext
import org.apache.cordova.CordovaArgs
import org.apache.cordova.CordovaPlugin
import org.json.JSONArray
import org.json.JSONObject


class KakaoCordovaSDK : CordovaPlugin() {
  override fun pluginInitialize() {
    super.pluginInitialize()

    try {
      val e: String = super.cordova.context.packageName
      val ai: ApplicationInfo = super.cordova.context.packageManager.getApplicationInfo(e, PackageManager.GET_META_DATA)
      val bundle: Bundle = ai.metaData
      var apiKey = bundle.getString("com.kakao.sdk.AppKey")

      if (apiKey != null) {
        KakaoSdk.init(super.cordova.context, apiKey)
      }
    } catch (var6: Exception) {
      Log.d("KAKAO_SDK", "Caught non-fatal exception while retrieving apiKey: $var6")
    }
  }

  override fun execute(action: String?, args: CordovaArgs?, callbackContext: CallbackContext?): Boolean {
    when (action) {
      "login" -> {
        if (callbackContext != null) {
          this.login(callbackContext)
        }
      }

      "logout" -> {
        if (callbackContext != null) {
          this.logout(callbackContext)
        }
      }
    }

    return true
  }

  private fun login(callbackContext: CallbackContext) {
    if (UserApiClient.instance.isKakaoTalkLoginAvailable(this.cordova.context)) {
      this.cordova.activity.let {
        UserApiClient.instance.loginWithKakaoTalk(it) { token, error: Throwable? ->
          if (error != null) {
            if (error is AuthError && error.statusCode == 302) {
              this.loginWithBrowser(callbackContext)
              return@loginWithKakaoTalk
            }
            callbackContext.error(error.localizedMessage)
            return@loginWithKakaoTalk
          }
          if (token != null) {
            this.loginSuccessCallback(token.accessToken, callbackContext)
            return@loginWithKakaoTalk
          } else {
            callbackContext.error("Invalid Credential")
            return@loginWithKakaoTalk
          }
        }
      }
    } else {
      this.loginWithBrowser(callbackContext)
    }
  }

  private fun loginWithBrowser(callbackContext: CallbackContext) {
    UserApiClient.instance.loginWithKakaoAccount(this.cordova.context) { token, error ->
      if (error != null) {
        callbackContext.error(error.localizedMessage)
        return@loginWithKakaoAccount
      }

      if (token != null) {
        this.loginSuccessCallback(token.accessToken, callbackContext)
        return@loginWithKakaoAccount
      } else {
        callbackContext.error("Invalid Credential")
        return@loginWithKakaoAccount
      }
    }
  }

  private fun loginSuccessCallback(accessToken: String, callbackContext: CallbackContext) {
    UserApiClient.instance.me { user, error ->
      if (error != null) {
        callbackContext.error(error.localizedMessage)
        return@me
      }

      if (user != null) {
        val json = JSONObject()
        json.put("id", user.id)
        json.put("email", user.kakaoAccount?.email)
        json.put("result", true)
        json.put("accessToken", accessToken)
        json.put("profileImage", user.kakaoAccount?.profile?.profileImageUrl)
        callbackContext.success(json)
        return@me
      } else {
        callbackContext.error("User not found")
        return@me
      }
    }
  }

  private fun logout(callbackContext: CallbackContext) {
    UserApiClient.instance.logout() { error ->
      if (error != null) {
        callbackContext.error("Kakao Logout Error")
        return@logout
      } else {
        callbackContext.success("Kakao Logout Success")
        return@logout
      }
    }
  }
}
