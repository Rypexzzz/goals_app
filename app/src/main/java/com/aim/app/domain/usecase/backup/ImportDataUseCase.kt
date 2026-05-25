package com.aim.app.domain.usecase.backup

import com.aim.app.domain.repository.BackupRepository
import javax.inject.Inject

class ImportDataUseCase @Inject constructor(
    private val repository: BackupRepository,
) {
    suspend operator fun invoke(json: String) = repository.importFromJson(json)
}
