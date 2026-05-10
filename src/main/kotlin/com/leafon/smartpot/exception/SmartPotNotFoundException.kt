package com.leafon.smartpot.exception

import java.util.UUID

class SmartPotNotFoundException(
    id: UUID,
) : RuntimeException("SmartPot with id $id not found")
