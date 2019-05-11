package tictactoe;

import java.util.*;

public class Main {
    private static int[][] POSSIBLE_ROWS = new int[][]{
            {0, 1, 2},
            {3, 4, 5},
            {6, 7, 8},
            {0, 3, 6},
            {1, 4, 7},
            {2, 5, 8},
            {0, 4, 8},
            {2, 4, 6}
    };
    private static Scanner SCANNER = new Scanner(System.in);
    private static List<String> ALLOWED_PLAYERS = Arrays.asList("user", "easy", "medium", "hard");

    private char[] field = new char[]{
            ' ', ' ', ' ',
            ' ', ' ', ' ',
            ' ', ' ', ' '
    };
    private String playerType1;
    private String playerType2;
    private int xCount = 0;
    private int oCount = 0;
    private int emptyCount = 9;


    public Main(String playerType1, String playerType2) {
        this.playerType1 = playerType1;
        this.playerType2 = playerType2;
    }

    public static void main(String[] args) {
        while (true) {
            System.out.println("Input command: ");
            String commandLine = SCANNER.nextLine();
            String[] command = commandLine.split(" ");
            if (command.length < 1) {
                System.out.println("Bad parameters!");
                continue;
            }
            String action = command[0];
            switch (action) {
                case "exit":
                    return;
                case "start":
                    if (command.length != 3) {
                        System.out.println("Bad parameters!");
                        continue;
                    }
                    String playerType1 = command[1];
                    String playerType2 = command[2];

                    if (!ALLOWED_PLAYERS.contains(playerType1) || !ALLOWED_PLAYERS.contains(playerType2)) {
                        System.out.println("Bad parameters!");
                        continue;
                    }

                    Main main = new Main(playerType1, playerType2);
                    main.playGame();
                    break;
                default:
                    System.out.println("Bad parameters!");
                    continue;
            }
        }

    }

    private void playGame() {
        printField();

        while (true) {
            playerMove('X', playerType1);

            printField();
            if (isFinished()) {
                break;
            }

            playerMove('O', playerType2);

            printField();
            if (isFinished()) {
                break;
            }
        }
    }

    private static int comparePlayers(char[] field) {
        boolean xWin = false;
        boolean oWin = false;
        for (int[] row : POSSIBLE_ROWS) {
            if (field[row[0]] == field[row[1]] && field[row[1]] == field[row[2]]) {
                if (field[row[0]] == 'X') {
                    xWin = true;
                } else if (field[row[0]] == 'O') {
                    oWin = true;
                }
            }
        }

        if (xWin && oWin) {
            throw new RuntimeException("Impossible");
        }
        if (xWin) {
            return 1;
        } else if (oWin) {
            return -1;
        } else {
            return 0;
        }
    }

    private boolean isFinished() {
        int comparePlayers = comparePlayers(field);

        if (comparePlayers > 0) {
            System.out.println("X wins");
        } else if (comparePlayers < 0) {
            System.out.println("O wins");
        } else if (emptyCount == 0) {
            System.out.println("Draw");
        } else {
            return false;
        }
        return true;
    }

    private void playerMove(char sign, String type) {
        while (true) {
            int move;
            if ("user".equals(type)) {
                try {
                    move = userMove();
                    makeMove(sign, move);
                    return;
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            } else {
                move = computerMove(sign, type);
                makeMove(sign, move);
                return;
            }
        }
    }

    private void makeMove(char sign, int move) {
        if (field[move] == ' ' ^ sign != ' ') {
            throw new RuntimeException("This cell is occupied! Choose another one!");
        }

        if (sign == ' ') {
            emptyCount++;
            if (field[move] == 'X') {
                xCount--;
            } else {
                oCount--;
            }
        } else {
            emptyCount--;
            if (sign == 'X') {
                xCount++;
            } else {
                oCount++;
            }
        }

        field[move] = sign;

        if (Math.abs(xCount - oCount) > 1 || emptyCount < 0) {
            throw new RuntimeException("Impossible");
        }
    }

    private int computerMove(char sign, String type) {
        System.out.println("Making move level \"" + type + "\"");
        switch (type) {
            case "easy":
                return easyMove();
            case "medium":
                return mediumMove(sign);
            case "hard":
                return hardMove(sign);
        }
        return -1;
    }

    private int hardMove(char sign) {
        int best = -2;
        List<Integer> moves = new ArrayList<>();

        Map<Integer, Integer> moveProbability = getBestMoveProbability(sign);
        for (Map.Entry<Integer, Integer> entry : moveProbability.entrySet()) {
            Integer value = entry.getValue();
            if (value > best) {
                best = value;
                moves.clear();
                moves.add(entry.getKey());
            } else if(value == best){
                moves.add(entry.getKey());
            }
        }

        Random random = new Random();
        return moves.get(random.nextInt(moves.size()));
    }

    private Map<Integer, Integer> getBestMoveProbability(char sign) {
        char oppositeSign = sign == 'X' ? 'O' : 'X';

        Map<Integer, Integer> result = new HashMap<>();

        int shift = sign == 'X' ? 1 : -1;
        for (int i = 0; i < 9; i++) {
            if (field[i] == ' ') {
                try {
                    makeMove(sign, i);
                    int comparePlayers = comparePlayers(field);
                    if (comparePlayers == shift || emptyCount == 0) {
                        result.put(i, comparePlayers * shift);
                        break;
                    }

                    Map<Integer, Integer> opponentMoves = getBestMoveProbability(oppositeSign);
                    int min = 1;
                    for (Integer value : opponentMoves.values()) {
                        int val = value * (-1);
                        if (min > val) {
                            min = val;
                        }
                    }

                    result.put(i, min);
                    if (min == 1) {
                        break;
                    }
                } finally {
                    makeMove(' ', i);
                }
            }
        }
        return result;
    }

    private int mediumMove(char sign) {
        int move = getLastInRow(sign);
        if (move < 0) {
            char oppositeSign = sign == 'X' ? 'O' : 'X';
            move = getLastInRow(oppositeSign);
        }

        if (move < 0) {
            return easyMove();
        }

        return move;
    }

    private int getLastInRow(char sign) {
        for (int[] row : POSSIBLE_ROWS) {
            if (field[row[0]] == ' ' && field[row[1]] == sign && field[row[2]] == sign) {
                return row[0];
            }

            if (field[row[0]] == sign && field[row[1]] == ' ' && field[row[2]] == sign) {
                return row[1];
            }

            if (field[row[0]] == sign && field[row[1]] == sign && field[row[2]] == ' ') {
                return row[2];
            }
        }
        return -1;
    }

    private int easyMove() {
        Random random = new Random();

        int nextMove = random.nextInt(emptyCount);

        int emptyCount = 0;
        for (int i = 0; i < 9; i++) {
            if (field[i] == ' ') {
                if (emptyCount == nextMove) {
                    return i;
                }
                emptyCount++;
            }
        }
        return -1;
    }

    private int userMove() {
        System.out.println("Enter the coordinates:");

        String s = SCANNER.nextLine().trim();
        String[] strings = s.split(" ");

        if (strings.length != 2) {
            throw new RuntimeException("Two numbers required.");
        }

        int x;
        int y;
        try {
            x = Integer.parseInt(strings[0]);
            y = Integer.parseInt(strings[1]);
        } catch (NumberFormatException e) {
            throw new RuntimeException("You should enter numbers!", e);
        }

        if (x < 1 || x > 3 || y < 1 || y > 3) {
            throw new RuntimeException("Coordinates should be from 1 to 3!");
        }

        return (3 - y) * 3 + x - 1;
    }

    private void printField() {
        System.out.println("---------");
        System.out.println("| " + field[0] + " " + field[1] + " " + field[2] + " |");
        System.out.println("| " + field[3] + " " + field[4] + " " + field[5] + " |");
        System.out.println("| " + field[6] + " " + field[7] + " " + field[8] + " |");
        System.out.println("---------");
    }

}
