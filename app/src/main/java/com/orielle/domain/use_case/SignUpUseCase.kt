package com.orielle.domain.use_case

import com.orielle.data.repository.AuthRepository
import javax.inject.Inject

class SignUpUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(displayName: String, email: String, password: String) =
        repository.signUpWithEmailAndPassword(displayName, email, password)
}
