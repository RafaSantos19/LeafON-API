package com.leafon.alert.exception

import com.leafon.common.exception.NotFoundException
import java.util.UUID

class AlertNotFoundException(
    id: UUID,
) : NotFoundException("Alert with id $id not found")
