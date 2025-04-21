package com.example.calcprac_stateflow

import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
// contains all business logic

class CalculatorViewModel : ViewModel(){

    //StateFlow variables: state catch korar jonno variable
    private val _state = MutableStateFlow(CalculatorState())
    //state ke stateflow er sathe hook korlam
    val state: StateFlow<CalculatorState> = _state.asStateFlow()

    //LiveData variables:
    private val _message = SingleLiveEvent<String>()
    val message: LiveData<String> = _message


    fun onAction(action: CalculatorAction) {
        when (action) {
            is CalculatorAction.Number -> enterNumber(action.number)
            is CalculatorAction.Operation -> setOperation(action.operation)
            is CalculatorAction.Clear -> clear()
            is CalculatorAction.Delete -> delete()
            is CalculatorAction.Calculate -> calculate()
            is CalculatorAction.Decimal -> addDecimalPoint()
        }
    }

    private fun addDecimalPoint() {
        viewModelScope.launch {
            val currentState = _state.value

            if (currentState.operation == null) {
                // Handle decimal for first number
                when {
                    currentState.number1.isEmpty() -> {
                        _state.value = currentState.copy(
                            number1 = "0.",
                            result = ""
                        )
                    }
                    currentState.number1.contains(".") -> {
                        _message.value = "Decimal already exists!"
                    }
                    currentState.number1.length >= MAX_NUM_LENGTH -> {
                        _message.value = "Number is too long!"
                    }
                    else -> {
                        _state.value = currentState.copy(
                            number1 = currentState.number1 + ".",
                            result = ""
                        )
                    }
                }
            } else {
                // Handle decimal for second number
                when {
                    currentState.number2.isEmpty() -> {
                        _state.value = currentState.copy(
                            number2 = "0.",
                            result = ""
                        )
                    }
                    currentState.number2.contains(".") -> {
                        _message.value = "Decimal already exists!"
                    }
                    currentState.number2.length >= MAX_NUM_LENGTH -> {
                        _message.value = "Number is too long!"
                    }
                    else -> {
                        _state.value = currentState.copy(
                            number2 = currentState.number2 + ".",
                            result = ""
                        )
                    }
                }
            }
        }
    }

    private fun enterNumber(number: Int) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.operation == null) {
                if (currentState.number1.length >= MAX_NUM_LENGTH) {
                    _message.value = "Number is too long!"
                    return@launch
                }
                _state.value = currentState.copy(
                    number1 = currentState.number1 + number,
                    result = ""
                )
            } else {
                if (currentState.number2.length >= MAX_NUM_LENGTH) {
                    _message.value = "Number is too long!"
                    return@launch
                }
                _state.value = currentState.copy(
                    number2 = currentState.number2 + number,
                    result = ""
                )
            }
        }
    }

    private fun setOperation(operation: CalculatorOperation) {
        viewModelScope.launch {
            val currentState = _state.value
            if (currentState.number1.isNotBlank()) {
                _state.value = currentState.copy(operation = operation)
            }
        }
    }

    private fun clear() {
        viewModelScope.launch {
            _state.value = CalculatorState()
        }
    }


    private fun delete() {
        viewModelScope.launch {
            val currentState = _state.value
            when {
                currentState.number2.isNotBlank() -> {
                    _state.value = currentState.copy(
                        number2 = currentState.number2.dropLast(1)
                    )
                }
                currentState.operation != null -> {
                    _state.value = currentState.copy(
                        operation = null
                    )
                }
                currentState.number1.isNotBlank() -> {
                    _state.value = currentState.copy(
                        number1 = currentState.number1.dropLast(1)
                    )
                }
            }
        }
    }

    private fun calculate() {
        viewModelScope.launch {
            val currentState = _state.value
            val number1 = currentState.number1.toDoubleOrNull()
            val number2 = currentState.number2.toDoubleOrNull()
            val operation = currentState.operation

            when {
                // Case 1: Both numbers are present -> Perform the calculation
                number1 != null && number2 != null && operation != null -> {
                    val result = when (operation) {
                        is CalculatorOperation.Add -> number1 + number2
                        is CalculatorOperation.Subtract -> number1 - number2
                        is CalculatorOperation.Multiply -> number1 * number2
                        is CalculatorOperation.Divide -> number1 / number2
                        else -> {}
                    }

                    _state.value = currentState.copy(
                        result = result.toString().removeSuffix(".0"),
                        number1 = "",
                        number2 = "",
                        operation = null
                    )
                }

                // Case 2: Only number1 is present -> Just show it
                number1 != null && operation == null -> {
                    _state.value = currentState.copy(
                        result = number1.toString().removeSuffix(".0"),
                        number1 = "",
                        number2 = "",
                        operation = null
                    )
                }

                // Case 3: No numbers, but existing result -> Just retain the result
                currentState.result.isNotBlank() -> {
                    _state.value = currentState.copy(
                        result = currentState.result.removeSuffix(".0"),
                        number1 = "",
                        number2 = "",
                        operation = null
                    )
                }

                // Case 4: Nothing entered at all -> Show 0 or empty
                else -> {
                    _state.value = currentState.copy(
                        result = "0",
                        number1 = "",
                        number2 = "",
                        operation = null
                    )
                }
            }
        }
    }


    companion object {
        private const val MAX_NUM_LENGTH = 8
    }
}


sealed class CalculatorAction {
    data class Number(val number: Int) : CalculatorAction()
    data class Operation(val operation: CalculatorOperation) : CalculatorAction()

    object Clear : CalculatorAction()
    object Delete : CalculatorAction()
    object Calculate : CalculatorAction()
    object Decimal : CalculatorAction()
}

// Helper class for one-time events with LiveData
class SingleLiveEvent<T> : MutableLiveData<T>() {
    private val pending = AtomicBoolean(false)

    override fun observe(owner: LifecycleOwner, observer: Observer<in T>) {
        super.observe(owner) { t ->
            if (pending.compareAndSet(true, false)) {
                observer.onChanged(t)
            }
        }
    }

    override fun setValue(t: T?) {
        pending.set(true)
        super.setValue(t)
    }
}