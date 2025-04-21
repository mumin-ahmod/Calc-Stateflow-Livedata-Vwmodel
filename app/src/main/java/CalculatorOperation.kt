package com.example.calcprac_stateflow

//eta ekhan theke ekhanei call hoise

sealed class CalculatorOperation(val symbol: String) {
    object Add : CalculatorOperation("+")
    object Subtract : CalculatorOperation("-")
    object Multiply : CalculatorOperation("*")
    object Divide : CalculatorOperation("/")

}