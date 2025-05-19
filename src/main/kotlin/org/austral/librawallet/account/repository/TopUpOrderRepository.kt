package org.austral.librawallet.account.repository

import org.austral.librawallet.account.entity.TopUpOrder
import org.springframework.data.jpa.repository.JpaRepository

interface TopUpOrderRepository : JpaRepository<TopUpOrder, Long>
