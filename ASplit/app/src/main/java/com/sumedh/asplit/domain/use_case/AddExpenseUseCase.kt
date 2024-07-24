package com.sumedh.asplit.domain.use_case

import com.sumedh.asplit.common.Resource
import com.sumedh.asplit.data.dto.ExpenseDao
import com.sumedh.asplit.domain.repository.ExpenseRepository
import com.sumedh.asplit.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class AddExpenseUseCase (
    private val expenseRepository: ExpenseRepository,
    private val userRepository: UserRepository
) {
    suspend fun invoke(expense: ExpenseDao) : Flow<Resource<Unit>> = flow {
        emit(Resource.Loading())
        expenseRepository.addExpense(expense)
        updateBalance(expense)
        emit(Resource.Success(Unit))
    }

    private suspend fun updateBalance(expense: ExpenseDao) {
        val equalShare = expense.amount / expense.participants.size
        val payer = userRepository.getUser(expense.payer)
        userRepository.updateUserBalance(expense.payer, payer.balance - expense.amount)

        for (participant in expense.participants) {
            val user = userRepository.getUser(participant)
            userRepository.updateUserBalance(participant, user.balance + equalShare)
        }
    }
}