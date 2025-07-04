package com.orielle.domain.use_case

import com.orielle.data.repository.AuthRepository
import javax.inject.Inject

/**
 * A use case that encapsulates the business logic for signing up a new user.
 * This class has a single responsibility: to orchestrate the sign-up process
 * by calling the AuthRepository.
 *
 * @param repository The authentication repository, provided by Hilt.
 */
class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * Executes the sign-up operation.
     * The `operator fun invoke` allows us to call this class as if it were a function.
     *
     * @param email The user's email address.
     * @param password The user's chosen password.
     * @return A Flow that emits the authentication response.
     */
    operator fun invoke(email: String, password: String) =
        repository.signUpWithEmailAndPassword(email, password)
}
