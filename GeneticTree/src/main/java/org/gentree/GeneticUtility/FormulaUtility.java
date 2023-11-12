package org.gentree.GeneticUtility;

import java.util.ArrayList;
import java.util.Random;

public class FormulaUtility {

    /**
     * Generates totally random tress, for every operator 2 recursive generate function called.
     * @param variables list of available variables
     * @param operations list of available operations
     * @param parent parent of the current node
     * @param integerLimit limit of the integer values
     * @return generated tree
     */
    public FormulaElement randomGeneration(ArrayList<String> variables, ArrayList<String> operations, FormulaElement parent, int integerLimit) {
        FormulaElement start = null;
        Random rand = new Random();

        int type = rand.nextInt(3);

        switch (type) {
            case 0 -> {
                int integer = rand.nextInt(integerLimit);//INTEGER
                start = new FormulaElement(parent, FormulaElementType.INTEGER, "" + integer);
            }
            case 1 -> {
                int variable = rand.nextInt(variables.size());//INTEGER
                start = new FormulaElement(parent, FormulaElementType.VARIABLE, variables.get(variable));
            }
            case 2 -> {
                int operation = rand.nextInt(operations.size());//OPERATOR
                start = new FormulaElement(parent, FormulaElementType.OPERATOR, operations.get(operation));
                start.setLeft(randomGeneration(variables, operations, start, integerLimit));
                start.setRight(randomGeneration(variables, operations, start, integerLimit));
            }
            default -> {
            }
        }

        return start;
    }


    public void crossover(FormulaElement formulaA, FormulaElement formulaB) {
        //WARNING: do not use

        FormulaElement pointA = this.findCrossoverPointWide(formulaA);
        FormulaElement pointB = this.findCrossoverPointWide(formulaB);

        FormulaElement tmp;

        if ((pointA.getParent() == null) || (pointB.getParent() == null)) {
            System.out.println("NOT VERY GOOD");
            tmp = formulaA;
            formulaA = formulaB;
            formulaB = tmp;
            return;
        }

        FormulaElement tmpA = pointA.safeCopy();
        FormulaElement tmpB = pointB.safeCopy();

        //FormulaElement tmpA = pointA.deepCopy();
        //FormulaElement tmpB = pointB.deepCopy();

        if (!pointA.getParent().swapChild(pointA, tmpB)) {
            System.out.println("SWAP FAILED");
        }
        if (!pointB.getParent().swapChild(pointB, tmpA)) {
            System.out.println("SWAP FAILED");
        }
    }

    public void wideCrossover(FormulaElement formulaA, FormulaElement formulaB) {
        ///WARNING: use this
        //crossover operation from root of the formulas
        FormulaElement pointA = this.findCrossoverPointWide(formulaA);
        FormulaElement pointB = this.findCrossoverPointWide(formulaB);


        FormulaElement safeA = pointA.safeCopy();
        FormulaElement safeB = pointB.safeCopy();
        if (pointA.getParent() == null) {
            pointA.getOwnerStrategy().setStart(safeB);
        } else {
            if (!pointA.getParent().swapChild(pointA, safeB)) {
                System.out.println("SWAP FAILED");
            }
        }
        if (pointB.getParent() == null) {
            pointB.getOwnerStrategy().setStart(safeA);
        } else {
            if (!pointB.getParent().swapChild(pointB, safeA)) {
                System.out.println("SWAP FAILED");
            }
        }


    }

    private ArrayList<FormulaElement> startToEndRandomPath(FormulaElement formulaA) {
        //creates a random path from root to leaf
        Random rand = new Random();

        ArrayList<FormulaElement> path = new ArrayList<FormulaElement>();

        FormulaElement next = formulaA;
        while (next != null)//travels to bottom with a random path
        {
            path.add(next);
            int leftRight = rand.nextInt(2);
            if (leftRight == 0)//LEFT
            {
                next = next.getLeft();
            } else {
                next = next.getRight();
            }
        }
        return path;

    }


    public FormulaElement findCrossoverPointWide(FormulaElement formulaA) {
        //crossover starts from root node, travels to leaf node, each node selection has a %50 change
        Random rand = new Random();

        ArrayList<FormulaElement> path = new ArrayList<FormulaElement>();

        path = this.startToEndRandomPath(formulaA);

        //randomly selects path and starts form beginning
        boolean selected = false;
        int crossoverLocatinon = 0;
        while (!selected) {
            int random = rand.nextInt(100);
            if (random > 50)//selects current point, %50
            {
                selected = true;
            } else {//if current node is not selected, goes up 1 level

                crossoverLocatinon++;
                crossoverLocatinon %= path.size();
            }
        }

        return path.get(crossoverLocatinon);
    }

    public void mutation(FormulaElement formulaA, ArrayList<String> varaibles, ArrayList<String> operations, int integerLimit) {
        //leaf to root node selection and mutation
        Random rand = new Random();

        ArrayList<FormulaElement> path = new ArrayList<FormulaElement>();

        path = this.startToEndRandomPath(formulaA);

        //select the mutation point from randomly generated path
        //start from deep point
        boolean selected = false;
        int mutationLocatinon = (path.size() - 1) % path.size();
        while (!selected) {
            int random = rand.nextInt(100);
            if (random > 50)//selects current point, %50
            {
                selected = true;
            } else {//if current node is not selected, goes up 1 level
                mutationLocatinon--;
                mutationLocatinon = Math.abs(mutationLocatinon) % path.size();//never selects the root node
            }
        }

        FormulaElement selectedNode = path.get(mutationLocatinon);

        FormulaElement mutatedPart = this.randomGeneration(varaibles, operations, selectedNode.getParent(), integerLimit);
        //runs a random generation routine to generate mutated part

        if (mutatedPart.getParent() != null) {
            if (!selectedNode.getParent().swapChild(selectedNode, mutatedPart)) {
                //System.out.println("SWAP FAILED");
            }
        } else {
            selectedNode.getOwnerStrategy().setStart(mutatedPart);
        }
    }

    public String getFormulaAsString(FormulaElement start) {
        //gets the formula's printable form
        if (start.getType() == FormulaElementType.OPERATOR) {
            return "( " + this.getFormulaAsString(start.getLeft()) + " " + start.getElement() + " " + this.getFormulaAsString(start.getRight()) + " )";
        } else {
            if (start.getLeft() != null) {
                System.out.println("ERROR");
            }
            if (start.getRight() != null) {
                System.out.println("ERROR");
            }
            return start.getElement();
        }
    }

    public FormulaElement randomGenerationWithDepth(ArrayList<String> varaibles, ArrayList<String> operations, FormulaElement parent, int integerLimit, int depth, int remainingDepth) {
        //creates tree which has a depth parameter
        FormulaElement start = null;
        Random rand = new Random();


        int deeping = rand.nextInt(depth);

        if (deeping <= remainingDepth) {
            //reach to requested depth
            int operation = rand.nextInt(operations.size());//OPERATOR
            start = new FormulaElement(parent, FormulaElementType.OPERATOR, operations.get(operation));
            start.setLeft(randomGenerationWithDepth(varaibles, operations, start, integerLimit, depth, remainingDepth - 1));
            start.setRight(randomGenerationWithDepth(varaibles, operations, start, integerLimit, depth, remainingDepth - 1));
            return start;
        }

        int type = rand.nextInt(3);


        switch (type) {
            case 0:
                int integer = rand.nextInt(integerLimit * 2);//INTEGER
                integer = integer - 2 * integerLimit;
                start = new FormulaElement(parent, FormulaElementType.INTEGER, "" + integer);
                break;
            case 1:
                int varaible = rand.nextInt(varaibles.size());//VARAIBLE
                start = new FormulaElement(parent, FormulaElementType.VARIABLE, varaibles.get(varaible));
                break;
            case 2:
                int operation = rand.nextInt(operations.size());//OPERATOR
                start = new FormulaElement(parent, FormulaElementType.OPERATOR, operations.get(operation));
                start.setLeft(randomGeneration(varaibles, operations, start, integerLimit));
                start.setRight(randomGeneration(varaibles, operations, start, integerLimit));
                break;
            default:
                break;
        }

        return start;
    }
}
