package com.sumedh.asplit.domain.use_case

import com.sumedh.asplit.common.Resource
import com.sumedh.asplit.domain.model.SettlementTransaction
import com.sumedh.asplit.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SettleBalancesUseCase (
    private val userRepository: UserRepository
) {
    suspend fun invoke(transactions: List<SettlementTransaction>) : Flow<Resource<Unit>> = flow{
        for (transaction in transactions) {
            val payer = userRepository.getUser(transaction.payer)
            val receiver = userRepository.getUser(transaction.receiver)

            userRepository.updateUserBalance(transaction.payer, payer.balance - transaction.amount)
            userRepository.updateUserBalance(transaction.receiver, receiver.balance + transaction.amount)
        }
    }
}