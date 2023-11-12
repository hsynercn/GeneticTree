package org.gentree.GeneticUtility;

import org.junit.jupiter.api.Assertions;

import static org.junit.jupiter.api.Assertions.*;

class SessionTest {

    @org.junit.jupiter.api.Test
    void sessionStart() {
        FormulaElement rootElement = new FormulaElement(null, FormulaElementType.OPERATOR, "+");

        FormulaElement left_1_Element = new FormulaElement(rootElement, FormulaElementType.INTEGER, "150");

        FormulaElement right_1_Element = new FormulaElement(rootElement, FormulaElementType.INTEGER, "50");

        rootElement.setLeft(left_1_Element);
        rootElement.setRight(right_1_Element);

        Strategy strategy = new Strategy(rootElement);

        Assertions.assertEquals(200, strategy.nextMove());

    }
}