package org.gentree.GeneticUtility;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *     This class is a representation of a player's strategy in the Prisoner's Dilemma game.
 *     It is a tree structure, where each node is a FormulaElement object.
 *     Each FormulaElement object has a value, which can be:
 *     - an integer
 *     - a variable name
 *     - an operator
 *     Each FormulaElement object has a left child, a right child and a parent.
 *     Each FormulaElement object has a reference to the Strategy object it belongs to.
 *
 */
public class Strategy implements Player {

    private static final String LAST_MOVE_STRING = "lastM";
    private static final String COOPERATION_COUNT_STRING = "coopCnt";
    private static final String DEFECTION_COUNT_STRING = "defectCnt";
    private static final String SUM_STRING = "+";
    private static final String subString = "-";
    private static final String mulString = "*";
    private int lastMove = 0;
    private int cooperationCount = 0;
    private int defectionCount = 0;
    private ArrayList<String> variableArrayList = new ArrayList<>(Arrays.asList(LAST_MOVE_STRING, COOPERATION_COUNT_STRING, DEFECTION_COUNT_STRING));
    private ArrayList<String> operationsArrayList = new ArrayList<>(Arrays.asList(SUM_STRING, subString, mulString));
    private FormulaElement start = null;

    public Strategy(FormulaElement start) {
        this.lastMove = 0;
        this.cooperationCount = 0;
        this.defectionCount = 0;
        this.start = start;
        this.start.setOwnerStrategy(this);
    }

    public Strategy() {
        this.start = new FormulaElement(null, FormulaElementType.INTEGER, "0");
    }

    public int solve(FormulaElement element) {
        FormulaElementType type = element.getType();

        if (type == FormulaElementType.INTEGER) {
            return Integer.parseInt(element.getElement());
        }

        if (type == FormulaElementType.VARIABLE) {
            return this.getLocalVariable(element.getElement());
        }
        if (type == FormulaElementType.OPERATOR) {
            if (element.getElement().compareTo(SUM_STRING) == 0) {
                //SUM
                return this.solve(element.getLeft()) + this.solve(element.getRight());
            }
            if (element.getElement().compareTo(subString) == 0) {
                //SUB
                return this.solve(element.getLeft()) - this.solve(element.getRight());
            }
            if (element.getElement().compareTo(mulString) == 0) {
                //MUL
                return this.solve(element.getLeft()) * this.solve(element.getRight());
            }
        }


        return 0;
    }

    public int getLocalVariable(String variableName) {
        if (variableName.compareTo(LAST_MOVE_STRING) == 0) {
            return this.lastMove;

        }
        if (variableName.compareTo(COOPERATION_COUNT_STRING) == 0) {
            return this.cooperationCount;
        }
        if (variableName.compareTo(DEFECTION_COUNT_STRING) == 0) {
            return this.defectionCount;
        }
        return 0;
    }

    @Override
    public int nextMove() {
        return this.solve(this.start);
    }

    @Override
    public void informPlayer(int opponentMove) {

        this.lastMove = opponentMove;
        if (opponentMove == 1) {
            this.cooperationCount++;
        } else {
            this.defectionCount++;
        }

    }

    public Strategy safeCopy() {
        return new Strategy(getStart().deepCopy());
    }


    public int getLastMove() {
        return lastMove;
    }

    public void setLastMove(int lastMove) {
        this.lastMove = lastMove;
    }

    public int getCooperationCount() {
        return cooperationCount;
    }

    public void setCooperationCount(int cooperationCount) {
        this.cooperationCount = cooperationCount;
    }

    public int getDefectionCount() {
        return defectionCount;
    }

    public void setDefectionCount(int defectionCount) {
        this.defectionCount = defectionCount;
    }

    public ArrayList<String> getVariableArrayList() {
        return variableArrayList;
    }

    public void setVariableArrayList(ArrayList<String> variableArrayList) {
        this.variableArrayList = variableArrayList;
    }

    public ArrayList<String> getOperationsArrayList() {
        return operationsArrayList;
    }

    public void setOperationsArrayList(ArrayList<String> operationsArrayList) {
        this.operationsArrayList = operationsArrayList;
    }

    public FormulaElement getStart() {
        return start;
    }

    public void setStart(FormulaElement start) {
        this.start.setOwnerStrategy(null);
        this.start = start;
        this.start.setOwnerStrategy(this);
        this.start.setParent(null);
    }

    @Override
    public void resetData() {
        this.lastMove = 0;
        this.cooperationCount = 0;
        this.defectionCount = 0;

    }

    public int calculateMaxDepth() {
        return this.start.calculateDepth();
    }


}
