import Foundation
import KakaoSDKAuth
import KakaoSDKCommon
import KakaoSDKUser

class KakaoCordovaSDK: CDVPlugin {

  override func pluginInitialize() {
    let key = Bundle.main.object(forInfoDictionaryKey: "KAKAO_APP_KEY") as? String
    if let key = key {
      KakaoSDK.initSDK(appKey: key)
      print("KakaoSDK initSDK.")
      // NotificationCenter.default.addObserver(self, selector: #selector(handleOpenURL(_:)), name: NSNotification.Name.CDVPluginHandleOpenURLWithAppSourceAndAnnotation, object: nil)
    }
  }

  override func handleOpenURL(_ notification: Notification!) {
    print("KakaoSDK handleOpenURL.")
    if let _url = notification.object as? NSURL {
      if let _url = _url.absoluteString {
        if let url = URL(string: _url) {
          if (AuthApi.isKakaoTalkLoginUrl(url)) {
            AuthController.handleOpenUrl(url: url)
          }
        }
      }
    }
  }

  @objc(login:) func login(command: CDVInvokedUrlCommand) {
    DispatchQueue.main.async {
      print("KakaoSDK login.")
      /*if (UserApi.isKakaoTalkLoginAvailable()) {
        UserApi.shared.loginWithKakaoTalk(
          launchMethod: .CustomScheme, completion: {
            (oauthToken, error) in
            self.loginCallback(oauthToken: oauthToken, error: error, callbackId: command.callbackId)
          }
        )
      } else {*/
        UserApi.shared.loginWithKakaoAccount(
          completion: {
            (oauthToken, error) in
            self.loginCallback(oauthToken: oauthToken, error: error, callbackId: command.callbackId)
          }
        )
      //}
    }
  }

  func loginCallback(oauthToken: OAuthToken?, error: Error?, callbackId: String) {
    print("KakaoSDK loginCallback.")
    if error != nil {
      let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error?.localizedDescription)
      self.commandDelegate.send(result, callbackId: callbackId)
    } else if let oauthToken = oauthToken {
      UserApi.shared.me() {
        (user, error) in
        if error != nil {
          let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: error?.localizedDescription)
          self.commandDelegate.send(result, callbackId: callbackId)
        } else {
          let result = CDVPluginResult(
            status: CDVCommandStatus_OK,
            messageAs: ["id": user?.id as Any, "result": true, "email": user?.kakaoAccount?.email as Any, "accessToken": oauthToken.accessToken, "profileImage": user?.kakaoAccount?.profile?.profileImageUrl!.absoluteString as Any]
          )
          self.commandDelegate.send(result, callbackId: callbackId)
        }
      }
    }
  }

  @objc(logout:) func logout(command: CDVInvokedUrlCommand) {
    DispatchQueue.main.async {
      print("KakaoSDK logout.")
      UserApi.shared.logout {
        (error) in
        if let error = error {
          let result = CDVPluginResult(status: CDVCommandStatus_ERROR, messageAs: "Logout Error")
          self.commandDelegate.send(result, callbackId: command.callbackId)
        } else {
          let result = CDVPluginResult(status: CDVCommandStatus_OK, messageAs: "Logout Done")
          self.commandDelegate.send(result, callbackId: command.callbackId)
        }
      }
    }
  }
}
