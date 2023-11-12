package org.gentree;

import org.gentree.GeneticUtility.*;

import java.util.ArrayList;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    public static void main(String[] args) {
        // Press Alt+Enter with your caret at the highlighted text to see how
        // IntelliJ IDEA suggests fixing it.
        int maxDepth = 15;
        int gameIterationLimit = 10;
        int mutationChange = 12;
        int generationLimit = 10;
        FormulaUtility utility = new FormulaUtility();
        Strategy strategyFoll = new Strategy(new FormulaElement(null, FormulaElementType.INTEGER, "1"));
        Strategy strategyTitForTat = new Strategy(new FormulaElement(null, FormulaElementType.VARIABLE, "lastM"));
        Strategy strategyTraitor = new Strategy(new FormulaElement(null, FormulaElementType.INTEGER, "-10"));
        Strategy strategyRevengeful = new Strategy(new FormulaElement(null, FormulaElementType.VARIABLE, "defectCnt"));
        Strategy strategyRandom = new Strategy(utility.randomGeneration(strategyFoll.getVariableArrayList(), strategyFoll.getOperationsArrayList(), null, maxDepth));

        ArrayList<Strategy> prePool = new ArrayList<Strategy>();
        prePool.add(strategyFoll);
        prePool.add(strategyTitForTat);
        prePool.add(strategyTraitor);
        prePool.add(strategyRevengeful);
        prePool.add(strategyRandom);
        PopulationController pController = new PopulationController(5, gameIterationLimit, maxDepth, mutationChange);
        pController.initiatedLifeCycleRandomizedPressurizedWide(generationLimit, prePool);
    }
}