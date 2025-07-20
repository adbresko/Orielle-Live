package com.orielle.domain.use_case

import com.google.firebase.auth.FirebaseUser
import com.orielle.domain.model.Response
import com.orielle.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    /**
     * This use case now expects and returns a Response containing a FirebaseUser object.
     */
    operator fun invoke(email: String, firstName: String, password: String): Flow<Response<FirebaseUser>> {
        return repository.signUpWithEmail(email, firstName, password)
    }
}