package controllers

import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.db.evolutions.Evolutions
import play.api.{Application, Configuration}
import play.api.libs.ws._
import play.api.test._
import play.api.test.Helpers._
import play.test.{WithApplication, WithServer}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.db.DBApi
import play.api.mvc.Result

class UserAuthSpec extends PlaySpec {

  def WithMockDB(body: play.api.Application => Unit): WithServer = {
    new WithServer {
      val application: play.api.Application = new GuiceApplicationBuilder().build()

      lazy val dbApi = application.injector.instanceOf[DBApi]

      Evolutions.applyEvolutions(dbApi.database("default"))

      body(application)

      Evolutions.cleanupEvolutions(dbApi.database("default"))
      dbApi.database("default").shutdown()
    }
  }

  "passwords don't match" in WithMockDB { application =>
    val request = FakeRequest(POST, "/signUp")
      .withFormUrlEncodedBody(
        "Username"         -> "test_name",
        "Name"             -> "First Last",
        "Password"         -> "password123",
        "Confirm Password" -> "qwertyuiasdf"
      )

    val requestWithToken = CSRFTokenHelper.addCSRFToken(request)

    val signUpController = application.injector.instanceOf[SignUpController]

    val result = await(signUpController.handleSignUp.apply(requestWithToken))
    result.header.status mustBe BAD_REQUEST
  }

  "valid sign up request" in WithMockDB { application =>
    val request = FakeRequest(POST, "/signUp")
      .withFormUrlEncodedBody(
        "Username"         -> "test_name",
        "Name"             -> "First Last",
        "Password"         -> "password123",
        "Confirm Password" -> "password123"
      )

    val requestWithToken = CSRFTokenHelper.addCSRFToken(request)

    val signUpController = application.injector.instanceOf[SignUpController]

    val result = await(signUpController.handleSignUp.apply(requestWithToken))

    result.header.status mustBe SEE_OTHER
  }

  "username already taken" in WithMockDB { application =>
    // sign up
    val signUpRequest = FakeRequest(POST, "/signUp")
      .withFormUrlEncodedBody(
        "Username"         -> "test_name",
        "Name"             -> "First Last",
        "Password"         -> "password",
        "Confirm Password" -> "password"
      )

    val signUpRequestWithToken = CSRFTokenHelper.addCSRFToken(signUpRequest)

    val signUpController = application.injector.instanceOf[SignUpController]

    val result1 = await(signUpController.handleSignUp.apply(signUpRequestWithToken))

    result1.header.status mustBe SEE_OTHER

    // sign up as different user but with same username
    val signUpRequestBad = FakeRequest(POST, "/signUp")
      .withFormUrlEncodedBody(
        "Username" -> "test_name",
        "Name"     -> "First2 Last2",
        "Password" -> "password",
        "Password" -> "password"
      )

    val signUpRequestBadWithToken = CSRFTokenHelper.addCSRFToken(signUpRequestBad)

    val result2 = await(signUpController.handleSignUp.apply(signUpRequestBadWithToken))

    result2.header.status mustBe BAD_REQUEST

  }

  "sign in with nonexistent user" in WithMockDB { application =>
    val signInRequest = FakeRequest(POST, "/signIn")
      .withFormUrlEncodedBody(
        "Username" -> "nonexistent",
        "Password" -> "password"
      )

    val signInController = application.injector.instanceOf[SignInController]

    val signInRequestWithToken = CSRFTokenHelper.addCSRFToken(signInRequest)

    val result = await(signInController.handleSignIn.apply(signInRequestWithToken))

    result.header.status mustBe BAD_REQUEST
  }

  "valid sign in attempt" in WithMockDB { application =>
    // Sign up first
    val signUpRequest = FakeRequest(POST, "/signUp")
      .withFormUrlEncodedBody(
        "Username"         -> "test_name",
        "Name"             -> "First Last",
        "Password"         -> "password",
        "Confirm Password" -> "password"
      )

    val signUpRequestWithToken = CSRFTokenHelper.addCSRFToken(signUpRequest)

    val signUpController = application.injector.instanceOf[SignUpController]

    val signUpResult = await(signUpController.handleSignUp.apply(signUpRequestWithToken))

    signUpResult.header.status mustBe SEE_OTHER

    // Sign in as same user
    val signInRequest = FakeRequest(POST, "/signIn")
      .withFormUrlEncodedBody(
        "Username" -> "test_name",
        "Password" -> "password"
      )

    val signInRequestWithToken = CSRFTokenHelper.addCSRFToken(signInRequest)

    val signInController = application.injector.instanceOf[SignInController]

    val signInResult = await(signInController.handleSignIn.apply(signInRequestWithToken))

    signInResult.header.status mustBe SEE_OTHER
  }

}
