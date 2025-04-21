package com.example.calcprac_stateflow

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.calcprac_stateflow.databinding.ActivityCalculatorBinding
import kotlinx.coroutines.flow.collect

class MainActivity : AppCompatActivity() {

    private val viewModel: CalculatorViewModel by viewModels()
    private lateinit var binding: ActivityCalculatorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root) // ✅ ViewBinding setup

        setupClickListeners()
        observeViewModel()
    }

    private fun setupClickListeners() {
        // Number buttons
        binding.btn0.setOnClickListener { viewModel.onAction(CalculatorAction.Number(0)) }
        binding.btn1.setOnClickListener { viewModel.onAction(CalculatorAction.Number(1)) }
        binding.btn2.setOnClickListener { viewModel.onAction(CalculatorAction.Number(2)) }
        binding.btn3.setOnClickListener { viewModel.onAction(CalculatorAction.Number(3)) }
        binding.btn4.setOnClickListener { viewModel.onAction(CalculatorAction.Number(4)) }
        binding.btn5.setOnClickListener { viewModel.onAction(CalculatorAction.Number(5)) }
        binding.btn6.setOnClickListener { viewModel.onAction(CalculatorAction.Number(6)) }
        binding.btn7.setOnClickListener { viewModel.onAction(CalculatorAction.Number(7)) }
        binding.btn8.setOnClickListener { viewModel.onAction(CalculatorAction.Number(8)) }
        binding.btn9.setOnClickListener { viewModel.onAction(CalculatorAction.Number(9)) }

        binding.btnDot.setOnClickListener { viewModel.onAction(CalculatorAction.Decimal) }


        // Operation buttons
        binding.btnAdd.setOnClickListener {
            viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.Add))
        }
        binding.btnSubtract.setOnClickListener {
            viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.Subtract))
        }
        binding.btnMultiply.setOnClickListener {
            viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.Multiply))
        }
        binding.btnDivide.setOnClickListener {
            viewModel.onAction(CalculatorAction.Operation(CalculatorOperation.Divide))
        }

        // Other buttons
        binding.btnEquals.setOnClickListener { viewModel.onAction(CalculatorAction.Calculate) }
        binding.btnClear.setOnClickListener { viewModel.onAction(CalculatorAction.Clear) }
        binding.btnDelete.setOnClickListener { viewModel.onAction(CalculatorAction.Delete) }
    }

    private fun observeViewModel() {
        lifecycleScope.launchWhenStarted {
            viewModel.state.collect { state ->
                updateDisplay(state)
            }
        }

        viewModel.message.observe(this) { message ->
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateDisplay(state: CalculatorState) {
        val displayText = buildString {
            append(state.number1)
            if (state.operation != null) {
                append(" ${state.operation.symbol} ")
            }
            append(state.number2)
            if (state.result.isNotBlank()) {
                append(" = ${state.result}")
            }
        }

        binding.tvResult.text = if (displayText.isBlank()) "0" else displayText
    }
}
