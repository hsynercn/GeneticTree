package org.gentree.GeneticUtility;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

/**
 * Genetic tools and randomization for the genetic algorithm.
 */
public class PopulationController {

    //Genetic tools and randomization

    private ArrayList<Strategy> pool = new ArrayList<Strategy>();//gene pool

    //selected for next generation
    private int scopeSize;


    //integer limit of the formulas
    private int integerLimit;


    //session number before end
    private int iterationLimit;


    //formula depth
    private int maxDepth;

    private int mutationChange;

    private HashMap<String, Integer> populationRecorder = new HashMap<String, Integer>();

    private FormulaUtility formulaUtility = new FormulaUtility();

    private Strategy tempStrategy = new Strategy();

    private HashMap<Strategy, Integer> scoreTable = new HashMap<Strategy, Integer>();//scores of the strategies

    public PopulationController(int scopeSize, int gameIterationLimit, int maxDepth, int mutationChange) {
        this.scopeSize = scopeSize;
        this.iterationLimit = gameIterationLimit;
        this.integerLimit = iterationLimit;
        this.maxDepth = maxDepth;
        this.mutationChange = mutationChange;

    }

    /**
     * Initializes the gene pool with random formulas/strategies.
     */
    public void initialize() {
        this.scoreTable.clear();
        for (int i = 0; i < this.scopeSize; i++) {
            FormulaElement formula = formulaUtility.randomGenerationWithDepth(tempStrategy.getVariableArrayList(),
                    tempStrategy.getOperationsArrayList(),
                    null,
                    this.integerLimit,
                    this.maxDepth,
                    this.maxDepth);
            Strategy strategy = new Strategy(formula);
            this.pool.add(strategy);
            this.scoreTable.put(strategy, 0);
        }

    }

    private void resetScoreTable() {
        this.scoreTable.replaceAll((k, v) -> 0);
    }

    /**
     * Starts the game session between the strategies. Every strategy plays with every other strategy in the pool.
     * Strategy also plays with itself.
     */
    public void encounterCycle() {
        Session session = new Session();

        this.resetScoreTable();

        for (int i = 0; i < this.pool.size(); i++) {
            for (int j = i; j < this.pool.size(); j++) {
                int[] result = new int[2];
                if (i == j)//strategy playing with itself
                {
                    Strategy copy = this.pool.get(i).safeCopy();
                    result = session.sessionStart(this.pool.get(i), copy, this.integerLimit);
                    this.scoreTable.put(this.pool.get(i), this.scoreTable.get(this.pool.get(i)) + result[0]);
                } else {
                    if (this.pool.get(i) == this.pool.get(j)) {
                        //same objects in one session not logical and not wanted
                        System.out.println("WARNING: PLAYERS ARE SAME OBJECT");
                    }
                    result = session.sessionStart(this.pool.get(i), this.pool.get(j), this.integerLimit);
                    this.scoreTable.put(this.pool.get(i), this.scoreTable.get(this.pool.get(i)) + result[0]);
                    this.scoreTable.put(this.pool.get(j), this.scoreTable.get(this.pool.get(j)) + result[1]);
                }
            }
        }
    }

    public void selectNextGeneration() {
        //selects the highest scores to full scope, starts form score table's beginning
        this.pool.clear();
        System.out.println("SCORE SIZE" + this.scoreTable.size());
        this.pool = this.selectHighScores(this.scopeSize, this.scoreTable);

    }

    public ArrayList<Strategy> selectHighScores(int size, HashMap<Strategy, Integer> tablePar) {
        //selects top N scores, not recommended
        ArrayList<Strategy> selected = new ArrayList<Strategy>();
        int sort = size;

        if (size > tablePar.size()) {
            sort = tablePar.size();
        }

        for (int i = 0; i < sort; i++)//select top scores, scope size
        {
            int tmpScore = Integer.MIN_VALUE;
            Strategy tmpStrategy = null;
            for (Strategy key : tablePar.keySet()) {
                if (tmpScore < tablePar.get(key)) {
                    tmpScore = tablePar.get(key);
                    tmpStrategy = key;
                }

            }
            tablePar.remove(tmpStrategy);
            selected.add(tmpStrategy);
            System.out.println("NEW GENERATION: " + formulaUtility.getFormulaAsString(tmpStrategy.getStart()));

        }
        return selected;
    }

    /**
     * Selects the highest scores to full scope, starts form score table's end.
     * Lexicographic Parsimony Pressure: We want to minimize the size of the genetic load.
     */
    public void selectNextGenerationWithPressure()//Lexicographic Parsimony Pressure
    {
        this.pool.clear();
        System.out.println("SCORE SIZE" + this.scoreTable.size());

        this.pool = this.selectHighScores(this.scopeSize + this.scopeSize / 2, this.scoreTable);

        this.scoreTable.clear();

        for (Strategy strategy : this.pool) {
            this.scoreTable.put(strategy, strategy.getStart().calculateDepth());
        }

        this.pool.clear();
        this.pool = this.selectLowScore(this.scopeSize, this.scoreTable);


    }

    public ArrayList<Strategy> selectLowScore(int size, HashMap<Strategy, Integer> tablePar) {
        //used for pressure
        ArrayList<Strategy> selected = new ArrayList<Strategy>();
        int sort = size;
        if (size > tablePar.size()) {
            sort = tablePar.size();
        }
        for (int i = 0; i < sort; i++) {
            int tmpDEPTH = Integer.MAX_VALUE;
            Strategy tmpStrategy = null;
            for (Strategy key : tablePar.keySet()) {
                if (tmpDEPTH > tablePar.get(key)) {
                    tmpDEPTH = tablePar.get(key);
                    tmpStrategy = key;
                }
            }
            tablePar.remove(tmpStrategy);
            selected.add(tmpStrategy);
        }
        return selected;
    }

    private int calculateTotalScore(HashMap<Strategy, Integer> table) {
        //sum of all scores
        int totalScore = 0;
        for (Strategy key : table.keySet()) {
            totalScore += table.get(key);
        }
        return totalScore;
    }

    private void sortListViaScoreTable(ArrayList<Strategy> poolPar, HashMap<Strategy, Integer> tablePar) {

        System.out.println("SCORE SIZE" + tablePar.size());

        for (int x = 0; x < poolPar.size(); x++) {
            int tmpScore = tablePar.get(poolPar.get(x));
            int select = x;
            for (int y = x; y < poolPar.size(); y++) {
                if (tmpScore < tablePar.get(poolPar.get(y))) {
                    tmpScore = tablePar.get(poolPar.get(y));
                    select = y;
                }
            }
            Strategy swap = poolPar.get(select);
            poolPar.set(select, poolPar.get(x));
            poolPar.set(x, swap);
        }

    }

    public void selectNextGenerationRandomWithPressure()//Lexicographic Parsimony Pressure
    {
        //random selection of top N scores via score percentage
        System.out.println("SCORE SIZE" + this.scoreTable.size());
        Random random = new Random();
        ArrayList<Strategy> buffer = new ArrayList<Strategy>();

        int totalScore = this.calculateTotalScore(this.scoreTable);

        this.sortListViaScoreTable(this.pool, this.scoreTable);

        System.out.println("***********************SORTED:");
        this.printTest();

        int j = 0;

        //selects strategies via score percentage
        while (buffer.size() <= (this.scopeSize + this.scopeSize / 2)) {
            int roulette = random.nextInt(totalScore);
            int selectedScore = this.scoreTable.get(this.pool.get(j));
            if (roulette < selectedScore) {
                buffer.add(this.pool.get(j));
                this.pool.remove(j);
            }
            j++;
            j = j % (this.scopeSize + this.scopeSize / 2);
        }

        this.pool.clear();
        this.scoreTable.clear();
        this.pool = buffer;


        for (Strategy strategy : this.pool) {
            this.scoreTable.put(strategy, strategy.getStart().calculateDepth());
        }
        this.pool.clear();

        //Pressure applied
        for (int i = 0; i < this.scopeSize; i++) {
            int tmpDEPTH = Integer.MAX_VALUE;
            Strategy tmpStrategy = null;
            for (Strategy key : this.scoreTable.keySet()) {
                if (tmpDEPTH > this.scoreTable.get(key)) {
                    tmpDEPTH = this.scoreTable.get(key);
                    tmpStrategy = key;
                }

            }

            this.pool.add(tmpStrategy);
            this.scoreTable.remove(tmpStrategy);
        }
        System.out.println("AFTER SELECTION");
        this.printTest();
        System.out.println("*********************");
    }


    public void selectNextGenerationWithMinimumAcceptance(int min) {
        //WARNING: unlimited pool size and scope size, eliminates the strategies blow minimum score
        this.pool.clear();
        System.out.println("SCORE SIZE " + this.scoreTable.size());
        for (Strategy key : this.scoreTable.keySet()) {
            if (min <= this.scoreTable.get(key)) {
                //tmpScore = this.scoreTable.get(key);
                this.pool.add(key);
            }
        }
        this.scoreTable.clear();

    }

    public void generatePool() {
        //creates a N*N sized gene pool
        Random rand = new Random();
        this.scoreTable.clear();
        ArrayList<Strategy> newPool = new ArrayList<Strategy>();
        for (int i = 0; i < this.pool.size(); i++) {

            for (int j = i; j < this.pool.size(); j++) {
                Strategy a = this.pool.get(i);
                Strategy safeCopyA = a.safeCopy();
                Strategy b = this.pool.get(j);
                Strategy safeCopyB = b.safeCopy();

                this.formulaUtility.crossover(safeCopyA.getStart(), safeCopyB.getStart());
                int mutateA = rand.nextInt(100);
                int mutateB = rand.nextInt(100);
                if (mutateA <= this.mutationChange) {
                    formulaUtility.mutation(safeCopyA.getStart(), this.tempStrategy.getVariableArrayList(), this.tempStrategy.getOperationsArrayList(), this.integerLimit);
                }
                if (mutateB <= this.mutationChange) {
                    formulaUtility.mutation(safeCopyB.getStart(), this.tempStrategy.getVariableArrayList(), this.tempStrategy.getOperationsArrayList(), this.integerLimit);
                }
                newPool.add(safeCopyB);
                newPool.add(safeCopyA);
                this.scoreTable.put(safeCopyB, 0);
                this.scoreTable.put(safeCopyA, 0);
            }
        }
        this.pool = newPool;
    }

    /**
     * Creates N*N sized gene pool, starts from root node of the formulas to crossover.
     * NOTE: Most efficient algorithm of the controller.
     */
    public void generatePoolWideCrossover() {
        //wide crossover means starts from root node of the formulas
        Random rand = new Random();
        this.scoreTable.clear();
        ArrayList<Strategy> newPool = new ArrayList<Strategy>();
        for (int i = 0; i < this.pool.size(); i++) {

            for (int j = i; j < this.pool.size(); j++) {
                Strategy a = this.pool.get(i);
                Strategy safeCopyA = a.safeCopy();
                Strategy b = this.pool.get(j);
                Strategy safeCopyB = b.safeCopy();

                this.formulaUtility.wideCrossover(safeCopyA.getStart(), safeCopyB.getStart());
                int mutateA = rand.nextInt(100);
                int mutateB = rand.nextInt(100);
                if (mutateA <= this.mutationChange) {
                    formulaUtility.mutation(safeCopyA.getStart(), this.tempStrategy.getVariableArrayList(), this.tempStrategy.getOperationsArrayList(), this.integerLimit);
                }
                if (mutateB <= this.mutationChange) {
                    formulaUtility.mutation(safeCopyB.getStart(), this.tempStrategy.getVariableArrayList(), this.tempStrategy.getOperationsArrayList(), this.integerLimit);
                }
                newPool.add(safeCopyB);
                newPool.add(safeCopyA);
                this.scoreTable.put(safeCopyB, 0);
                this.scoreTable.put(safeCopyA, 0);
            }
        }

        this.pool = newPool;
    }

    private void recordPool(ArrayList<Strategy> poolPar) {
        for (Strategy strategy : poolPar) {
            String formulaString = formulaUtility.getFormulaAsString(strategy.getStart());
            if (this.populationRecorder.get(formulaString) == null) {
                this.populationRecorder.put(formulaString, 1);
            } else {
                this.populationRecorder.put(formulaString, this.populationRecorder.get(formulaString) + 1);
            }
        }
    }

    private void printRecordPool() {
        for (String key : this.populationRecorder.keySet()) {
            System.out.println("MATERIAL: " + key + " COUNT: " + this.populationRecorder.get(key));

        }
    }

    public void printTest() {
        for (Strategy strategy : this.pool) {
            System.out.println("DEPTH " + strategy.calculateMaxDepth() + " ,SCORE: " + this.scoreTable.get(strategy) + " ,GENETIC:" + formulaUtility.getFormulaAsString(strategy.getStart()));
        }
    }

    public void lifeCycleRandomizedPressurizedWide(int generationLimit) {
        //Most efficient algorithm of the controller, randomized start
        this.initialize();//random initialize
        for (int i = 0; i < generationLimit; i++) {
            System.out.println("GENERATION NUMBER: " + i);
            this.generatePoolWideCrossover();//wide crossover
            this.encounterCycle();

            this.printTest();

            //selection
            this.selectNextGenerationRandomWithPressure();
            this.recordPool(this.pool);

        }
        this.printTest();
    }

    public void initiatedLifeCycleRandomizedPressurizedWide(int generationLimit, ArrayList<Strategy> prePool) {
        //Most efficient algorithm of the controller, can import a pre gene pool
        this.pool = prePool;
        System.out.println("START");
        for (int i = 0; i < generationLimit; i++) {
            System.out.println("GENERATION NUMBER: " + i);
            this.generatePoolWideCrossover();
            this.encounterCycle();
            this.selectNextGenerationRandomWithPressure();
            this.recordPool(this.pool);

        }
        this.printRecordPool();
    }

    public ArrayList<Strategy> getPool() {
        return pool;
    }

    public void setPool(ArrayList<Strategy> pool) {
        this.pool = pool;
    }

    public int getScopeSize() {
        return scopeSize;
    }

    public void setScopeSize(int scopeSize) {
        this.scopeSize = scopeSize;
    }

    public int getIntegerLimit() {
        return integerLimit;
    }

    public void setIntegerLimit(int integerLimit) {
        this.integerLimit = integerLimit;
    }

    public int getIterationLimit() {
        return iterationLimit;
    }

    public void setIterationLimit(int iterationLimit) {
        this.iterationLimit = iterationLimit;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void setMaxDepth(int maxDepth) {
        this.maxDepth = maxDepth;
    }

    public FormulaUtility getFormulaUtility() {
        return formulaUtility;
    }

    public void setFormulaUtility(FormulaUtility formulaUtility) {
        this.formulaUtility = formulaUtility;
    }

    public Strategy getTempStrategy() {
        return tempStrategy;
    }

    public void setTempStrategy(Strategy tempStrategy) {
        this.tempStrategy = tempStrategy;
    }

    public HashMap<Strategy, Integer> getScoreTable() {
        return scoreTable;
    }

    public void setScoreTable(HashMap<Strategy, Integer> scoreTable) {
        this.scoreTable = scoreTable;
    }

}
