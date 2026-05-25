package com.aim.app.domain.usecase.backup

import com.aim.app.domain.repository.BackupRepository
import javax.inject.Inject

class ExportDataUseCase @Inject constructor(
    private val repository: BackupRepository,
) {
    suspend operator fun invoke(): String = repository.exportToJson()
}
