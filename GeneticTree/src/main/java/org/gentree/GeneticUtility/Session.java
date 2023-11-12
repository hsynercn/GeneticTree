package org.gentree.GeneticUtility;

/**
 * Calculates the results of the session with prisoners dilemma rules.
 * It applies the hardcoded score table to 4 possible cases.
 */
public class Session {

    int[][] caseTable;

    //0 for cooperation, 1 for defection
    private final static int COOPERATION = 0;
    private final static int DEFECTION = 1;

    public Session() {
        this.caseTable = new int[][]{{0, 0}, {0, 0}};
        //if the players cooperate and other player cooperate, then both get 3
        this.caseTable[COOPERATION][COOPERATION] = 3;

        //if the players cooperate and other player defect, then the player get -1 and other player get 5
        this.caseTable[COOPERATION][DEFECTION] = -1;

        //if the players defect and other player cooperate, then the player get 5 and other player get -1
        this.caseTable[DEFECTION][COOPERATION] = 5;

        //if the players defect and other player defect, then both get 1
        this.caseTable[DEFECTION][DEFECTION] = 1;
    }

    public int[] sessionStart(Player playerA, Player playerB, int iterationNumber) {
        int[] results = new int[2];

        playerA.resetData();
        playerB.resetData();
        for (int i = 0; i < iterationNumber; i++) {
            int a = playerA.nextMove();
            int b = playerB.nextMove();

            int aSelection = a < 0 ? DEFECTION : COOPERATION;
            int bSelection = b < 0 ? DEFECTION : COOPERATION;

            results[0] += this.caseTable[aSelection][bSelection];
            results[1] += this.caseTable[bSelection][aSelection];


            if (a >= 0) {
                playerB.informPlayer(1);
            } else {
                playerB.informPlayer(-1);
            }


            if (b >= 0) {
                playerA.informPlayer(1);
            } else {
                playerA.informPlayer(-1);
            }

        }
        playerA.resetData();
        playerB.resetData();
        return results;
    }

}
