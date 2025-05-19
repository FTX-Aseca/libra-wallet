package org.austral.librawallet.account.repository

import org.austral.librawallet.account.entity.DebinRequest
import org.springframework.data.jpa.repository.JpaRepository

interface DebinRequestRepository : JpaRepository<DebinRequest, Long>
