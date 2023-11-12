package org.gentree.GeneticUtility;

import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class PopulationController {

    //Genetic tools and randomization

    private ArrayList<Strategy> pool = new ArrayList<Strategy>();//gene pool

    private TextArea textArea = null;//print terminals
    private TextArea textAreaResult = null;//print terminals

    private int scopeSize;//selected for next generation

    private int integerLimit;//integer limit of the formulas

    private int iterationLimit;//session number before end

    private int maxDepth;//formula depth

    private int mutationChange;

    private HashMap<String, Integer> populationRecorder = new HashMap<String, Integer>();

    private FormulaUtility fUtility = new FormulaUtility();

    private Strategy tempStrgy = new Strategy();

    private HashMap<Strategy, Integer> scoreTable = new HashMap<Strategy, Integer>();//scores of the strategies

    public PopulationController(int scopeSize, int gameIterationLimit, int maxDepth, int mutationChange) {
        this.scopeSize = scopeSize;
        this.iterationLimit = gameIterationLimit;
        this.integerLimit = iterationLimit;
        this.maxDepth = maxDepth;
        this.mutationChange = mutationChange;

    }

    public void initialize() {
        //random initialization
        this.scoreTable.clear();
        for (int i = 0; i < this.scopeSize; i++) {
            FormulaElement formula = fUtility.randomGenerationWithDepth(tempStrgy.getVariableArrayList(), tempStrgy.getOperationsArrayList(), null, this.integerLimit, this.maxDepth, this.maxDepth);
            Strategy strategy = new Strategy(formula);
            this.pool.add(strategy);
            this.scoreTable.put(strategy, 0);
        }

    }

    private void resetScoreTable() {
        for (Strategy key : this.scoreTable.keySet()) {
            this.scoreTable.put(key, 0);

        }
    }

    public void encounterCycle() {
        //strategies plays the game
        Session session = new Session();

        this.resetScoreTable();

        int result[] = new int[2];

        for (int i = 0; i < this.pool.size(); i++) {
            for (int j = i; j < this.pool.size(); j++) {
                if (i == j)//strategy playing with itself
                {
                    Strategy copy = this.pool.get(i).safeCopy();
                    result = session.sessionStart(this.pool.get(i), copy, this.integerLimit);
                    this.scoreTable.put(this.pool.get(i), this.scoreTable.get(this.pool.get(i)) + result[0]);
                } else {
                    if (this.pool.get(i) == this.pool.get(j)) {
                        System.out.println("WARNING: PALYERS ARE SAME OBJECT");
                        //same objects in one session not logical and not wanted
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
        //selects top n scores, not recommended
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
            System.out.println("NEW GENERATION: " + fUtility.getFormulaAsString(tmpStrategy.getStart()));

        }
        return selected;
    }

    public void selectNextGenerationWithPressure()//Lexicographic Parsimony Pressure
    {
        this.pool.clear();
        System.out.println("SCORE SIZE" + this.scoreTable.size());

        this.pool = this.selectHighScores(this.scopeSize + this.scopeSize / 2, this.scoreTable);

        this.scoreTable.clear();

        for (int i = 0; i < this.pool.size(); i++) {
            this.scoreTable.put(this.pool.get(i), this.pool.get(i).getStart().calculateDepth());
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

    private void sortLisyViaScoreTable(ArrayList<Strategy> poolPar, HashMap<Strategy, Integer> tablePar) {

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
        //random selection of top N scores via score percantage
        System.out.println("SCORE SIZE" + this.scoreTable.size());
        Random random = new Random();
        ArrayList<Strategy> buffer = new ArrayList<Strategy>();

        int totalScore = this.calculateTotalScore(this.scoreTable);

        this.sortLisyViaScoreTable(this.pool, this.scoreTable);

        System.out.println("***********************SORTED:");
        this.printTest();
        this.printOuterMonitor(-1);


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


        for (int i = 0; i < this.pool.size(); i++) {
            this.scoreTable.put(this.pool.get(i), this.pool.get(i).getStart().calculateDepth());
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
        //creates a N*N sized gene pool, NOT IN USE
        Random rand = new Random();
        this.scoreTable.clear();
        ArrayList<Strategy> newPool = new ArrayList<Strategy>();
        for (int i = 0; i < this.pool.size(); i++) {

            for (int j = i; j < this.pool.size(); j++) {
                Strategy a = this.pool.get(i);
                Strategy safeCopyA = a.safeCopy();
                Strategy b = this.pool.get(j);
                Strategy safeCopyB = b.safeCopy();

                this.fUtility.crossover(safeCopyA.getStart(), safeCopyB.getStart());
                int mutateA = rand.nextInt(100);
                int mutateB = rand.nextInt(100);
                if (mutateA <= this.mutationChange) {
                    System.out.println("HAIL MUTATION");
                    fUtility.mutation(safeCopyA.getStart(), this.tempStrgy.getVariableArrayList(), this.tempStrgy.getOperationsArrayList(), this.integerLimit);
                }
                if (mutateB <= this.mutationChange) {
                    System.out.println("HAIL MUTATION");
                    fUtility.mutation(safeCopyB.getStart(), this.tempStrgy.getVariableArrayList(), this.tempStrgy.getOperationsArrayList(), this.integerLimit);
                }
                newPool.add(safeCopyB);
                newPool.add(safeCopyA);
                this.scoreTable.put(safeCopyB, 0);
                this.scoreTable.put(safeCopyA, 0);
            }
        }
        this.pool = newPool;
    }

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

                this.fUtility.wideCrossover(safeCopyA.getStart(), safeCopyB.getStart());
                int mutateA = rand.nextInt(100);
                int mutateB = rand.nextInt(100);
                if (mutateA <= this.mutationChange) {
                    System.out.println("HAIL MUTATION");
                    fUtility.mutation(safeCopyA.getStart(), this.tempStrgy.getVariableArrayList(), this.tempStrgy.getOperationsArrayList(), this.integerLimit);
                }
                if (mutateB <= this.mutationChange) {
                    System.out.println("HAIL MUTATION");
                    fUtility.mutation(safeCopyB.getStart(), this.tempStrgy.getVariableArrayList(), this.tempStrgy.getOperationsArrayList(), this.integerLimit);
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
        for (int i = 0; i < poolPar.size(); i++) {
            String formulaString = fUtility.getFormulaAsString(poolPar.get(i).getStart());
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

    private void printOuterMonitor(int iterationNumber) {

        if (this.textArea != null) {
            if (iterationNumber != -1)
                this.textArea.append("\nGENERATION: " + iterationNumber + "\n");
            else
                this.textArea.append("\nSORTED:\n");
            for (int i = 0; i < this.pool.size(); i++) {
                this.textArea.append("DEPTH " + this.pool.get(i).calculateMaxDepth() + " SCORE: " + this.scoreTable.get(this.pool.get(i)) + " " + fUtility.getFormulaAsString(this.pool.get(i).getStart()) + "\n");
            }
        }
    }

    private void printOuterMonitorResults() {
        if (this.textAreaResult != null) {
            this.textAreaResult.append("RESULTS \n");
            for (String key : this.populationRecorder.keySet()) {
                this.textAreaResult.append(" COUNT: " + this.populationRecorder.get(key) + "  MATERIAL: " + key + "\n");
            }
        }
    }

    public void printTest() {
        for (int i = 0; i < this.pool.size(); i++) {
            System.out.println("DEPTH " + this.pool.get(i).calculateMaxDepth() + " SCORE: " + this.scoreTable.get(this.pool.get(i)) + " " + fUtility.getFormulaAsString(this.pool.get(i).getStart()));
        }
    }

    public void lifeCycle(int generationLimit) {
        //WARNING: NOT IN USE
        this.initialize();
        for (int i = 0; i < generationLimit; i++) {
            this.generatePool();
            this.encounterCycle();
            System.out.println("NEXT LIFE************************************");
            this.printTest();
            this.selectNextGeneration();

        }
        this.printTest();

    }

    public void lifeCyclePressurized(int generationLimit) {
        //WARNING: NOT IN USE
        this.initialize();
        for (int i = 0; i < generationLimit; i++) {
            this.generatePool();
            this.encounterCycle();
            System.out.println("NEXT LIFE************************************");
            this.printTest();
            this.selectNextGenerationWithPressure();

        }
        this.printTest();

    }

    public void lifeCycleRandomizedPressurized(int generationLimit) {
        //WARNING: NOT IN USE
        this.initialize();
        for (int i = 0; i < generationLimit; i++) {
            System.out.println("GENERATION NUMBER: " + i);
            this.generatePool();
            this.encounterCycle();

            this.printTest();
            this.selectNextGenerationRandomWithPressure();

        }
        this.printTest();

    }

    public void lifeCycleRandomizedPressurizedWide(int generationLimit) {
        //Most efficient algorithm of the controller, randomized start
        this.initialize();//random initialize
        this.printOuterMonitor(0);
        for (int i = 0; i < generationLimit; i++) {
            System.out.println("GENERATION NUMBER: " + i);
            this.generatePoolWideCrossover();//wide crossover
            this.encounterCycle();

            this.printTest();
            this.printOuterMonitor(i);
            this.selectNextGenerationRandomWithPressure();//seelction
            this.recordPool(this.pool);

        }
        this.printTest();
        this.printOuterMonitor(generationLimit);
        this.printOuterMonitorResults();

    }

    public void initiatedLifeCycleRandomizedPressurizedWide(int generationLimit, ArrayList<Strategy> prePool) {
        //Most efficient algorithm of the controller, can import a pre gene pool
        this.pool = prePool;
        System.out.println("START");
        this.printOuterMonitor(0);
        for (int i = 0; i < generationLimit; i++) {
            System.out.println("GENERATION NUMBER: " + i);
            this.generatePoolWideCrossover();
            this.encounterCycle();
            this.printOuterMonitor(i);
            this.selectNextGenerationRandomWithPressure();
            this.recordPool(this.pool);

        }
        this.printOuterMonitor(generationLimit);
        this.printOuterMonitorResults();
        this.printRecordPool();
    }

    public void lifeCycleFree(int generationLimit) {
        //WARNING: NOT IN USE, specially, unlimited scope size, eliminates the under 0
        this.initialize();
        for (int i = 0; i < generationLimit; i++) {
            this.generatePool();
            this.encounterCycle();
            System.out.println("NEXT LIFE************************************");
            this.printTest();
            this.selectNextGenerationWithMinimumAcceptance(0);
        }
        this.printTest();

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

    public FormulaUtility getfUtility() {
        return fUtility;
    }

    public void setfUtility(FormulaUtility fUtility) {
        this.fUtility = fUtility;
    }

    public Strategy getTempStrgy() {
        return tempStrgy;
    }

    public void setTempStrgy(Strategy tempStrgy) {
        this.tempStrgy = tempStrgy;
    }

    public HashMap<Strategy, Integer> getScoreTable() {
        return scoreTable;
    }

    public void setScoreTable(HashMap<Strategy, Integer> scoreTable) {
        this.scoreTable = scoreTable;
    }

    public TextArea getTextArea() {
        return textArea;
    }

    public void setTextArea(TextArea textArea) {
        this.textArea = textArea;
    }

    public TextArea getTextAreaResult() {
        return textAreaResult;
    }

    public void setTextAreaResult(TextArea textAreaResult) {
        this.textAreaResult = textAreaResult;
    }


}
