package com.orielle.domain.use_case

import com.orielle.domain.model.Response
import com.orielle.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(email: String, password: String): Flow<Response<Boolean>> {
        // --- THIS IS THE FIX ---
        // We are now calling the new, renamed "signInWithEmail" function.
        return repository.signInWithEmail(email, password)
    }
}