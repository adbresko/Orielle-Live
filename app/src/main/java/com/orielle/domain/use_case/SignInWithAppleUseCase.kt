package com.orielle.domain.use_case

import com.orielle.domain.repository.AuthRepository
import javax.inject.Inject

/**
 * A use case that encapsulates the business logic for signing in a user with an Apple ID token.
 */
class SignInWithAppleUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Executes the Apple sign-in operation.
     * @param idToken The ID token received from the Apple Sign-In flow.
     * @return A Flow that emits the authentication response.
     */
    operator fun invoke(idToken: String) = repository.signInWithApple(idToken)
}
