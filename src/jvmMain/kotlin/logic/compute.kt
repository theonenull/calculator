package logic

/*
fun compute(string: String) {
     if(string)
}*/



import java.util.Stack



fun evaluateExpression(expression: String): Double {
    val sanitizedExpression = expression.replace(" ", "") // ɾ���ո�
    val tokens = tokenize(sanitizedExpression)
    val postfix = infixToPostfix(tokens)
    return evaluatePostfix(postfix)
}

fun tokenize(expression: String): List<String> {
    val operators = setOf("+", "-", "*", "/", "(", ")")
    val tokens = mutableListOf<String>()
    var currentToken = ""
    for (char in expression) {
        val charStr = char.toString()
        if (operators.contains(charStr)) {
            if (currentToken.isNotEmpty()) {
                tokens.add(currentToken)
            }
            tokens.add(charStr)
            currentToken = ""
        } else {
            currentToken += char
        }
    }
    if (currentToken.isNotEmpty()) {
        tokens.add(currentToken)
    }
    return tokens
}

fun infixToPostfix(infix: List<String>): List<String> {
    val precedence = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2)
    val output = mutableListOf<String>()
    val operatorStack = Stack<String>()

    for (token in infix) {
        if (token.toDoubleOrNull() != null) {
            output.add(token)
        } else if (token == "(") {
            operatorStack.push(token)
        } else if (token == ")") {
            while (operatorStack.isNotEmpty() && operatorStack.peek() != "(") {
                output.add(operatorStack.pop())
            }
            operatorStack.pop()
        } else {
            while (operatorStack.isNotEmpty() && precedence.getOrDefault(token, 0) <= precedence.getOrDefault(operatorStack.peek(), 0)) {
                output.add(operatorStack.pop())
            }
            operatorStack.push(token)
        }
    }

    while (operatorStack.isNotEmpty()) {
        output.add(operatorStack.pop())
    }

    return output
}

fun evaluatePostfix(postfix: List<String>): Double {
    val stack = Stack<Double>()
    for (token in postfix) {
        if (token.toDoubleOrNull() != null) {
            stack.push(token.toDouble())
        } else {
            if (stack.size < 2) {
                throw IllegalArgumentException("������ $token ��Ҫ����������������ջ�еĲ��������㡣")
            }
            val operand2 = stack.pop()
            val operand1 = stack.pop()
            val result = when (token) {
                "+" -> operand1 + operand2
                "-" -> operand1 - operand2
                "*" -> operand1 * operand2
                "/" -> operand1 / operand2
                else -> throw IllegalArgumentException("��֧�ֵĲ�����: $token")
            }
            stack.push(result)
        }
    }
    if (stack.size != 1) {
        throw IllegalArgumentException("���ʽ��Ч����ջ��Ӧ��ֻ��һ�����ֵ������ջ���� ${stack.size} ��ֵ��")
    }
    return stack.pop()
}
