package com.leafon.routine.exception

import com.leafon.common.exception.NotFoundException
import java.util.UUID

class RoutineNotFoundException(
    id: UUID,
) : NotFoundException("Routine with id $id not found")
