package battleship;

import java.util.Arrays;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        String[] player = {"Player 1", "Player 2"};
        BattleField[] myField = new BattleField[2];
        myField[0] = new BattleField();
        myField[1] = new BattleField();
        BattleField[] enemyField = new BattleField[2];
        enemyField[0] = new BattleField();
        enemyField[1] = new BattleField();
        Scanner scanner = new Scanner(System.in);
        Ship[][] fleet = new Ship[2][5];
        for (int p = 0; p < 2; p++) {
            System.out.println(player[p] + ", place your ships on the game field");
            myField[p].print();
            fleet[p][0] = new Ship(Ship.Type.CARRIER);
            fleet[p][1] = new Ship(Ship.Type.BATTLESHIP);
            fleet[p][2] = new Ship(Ship.Type.SUBMARINE);
            fleet[p][3] = new Ship(Ship.Type.CRUISER);
            fleet[p][4] = new Ship(Ship.Type.DESTROYER);
            for (Ship ship : fleet[p]) {
                System.out.printf("Enter the coordinates of the %s (%d cells):",
                        ship.getName(), ship.getSize());
                Position position;
                while (true) {
                    try {
                        position = strToPosition(scanner.nextLine());
                    } catch (IllegalArgumentException e) {
                        System.out.print("Error! You entered the wrong coordinates! Try again:");
                        continue;
                    }
                    if (myField[p].putIn(position, ship)) break;
                    //System.out.println(position.toString());
                }
                myField[p].print();
            }
            System.out.println("Press Enter and pass the move to another player");
            scanner.nextLine();
        }
        /*fleet[0].putIn(3, 6, 7, 6, myField);
        fleet[1].putIn(1, 1, 1, 4, myField);
        fleet[2].putIn(8, 10, 10, 10, myField);
        fleet[3].putIn(9, 2, 9, 4, myField);
        fleet[4].putIn(2, 9, 2, 10, myField);
        System.out.println("The game starts!");*/
        int active = 0;
        int enemy = 1;
        boolean gameOver = false;
        while (!gameOver) {
            enemyField[active].print();
            System.out.println("---------------------");
            myField[active].print();
            System.out.println(player[active] + ", it's your turn:");
            Cell target;
            while (true) {
                try {
                    target = strToCell(scanner.nextLine());
                    break;
                } catch (IllegalArgumentException e) {
                    System.out.println("Error! You entered the wrong coordinates! Try again:");
                }
            }
            switch (myField[enemy].fire(target)) {
                case HIT -> {
                    enemyField[active].hit(target);
                    System.out.println("You hit a ship!");
                    System.out.println("Press Enter and pass the move to another player");
                    scanner.nextLine();
                }
                case SANK -> {
                    enemyField[active].hit(target);
                    System.out.println("You sank a ship!");
                    System.out.println("Press Enter and pass the move to another player");
                    scanner.nextLine();
                }
                case WIN -> {
                    enemyField[active].hit(target);
                    System.out.println("You sank the last ship! You won. Congratulations!");
                    gameOver = true;
                }
                case MISS -> {
                    enemyField[active].miss(target);
                    System.out.println("You missed!");
                    System.out.println("Press Enter and pass the move to another player");
                    scanner.nextLine();
                }
            }
            if (active == 0) {
                active = 1;
                enemy = 0;
            } else {
                active = 0;
                enemy = 1;
            }
        }
        //myField.print();*/
    }
    private static Position strToPosition(String input) {
        if (input.length() > 7 || input.length() < 5) throw new IllegalArgumentException();
        Position position = new Position();
        // parse position
        Cell first = strToCell(input.substring(0, input.indexOf(" ")));
        Cell second = strToCell(input.substring(input.indexOf(" ") + 1));
        // normalize location
        if (first.getX() > second.getX() || first.getY() > second.getY()) {
            position.setX1(second.getX());
            position.setY1(second.getY());
            position.setX2(first.getX());
            position.setY2(first.getY());
        } else {
            position.setX1(first.getX());
            position.setY1(first.getY());
            position.setX2(second.getX());
            position.setY2(second.getY());
        }
        return position;
    }
    private static Cell strToCell(String input) {
        if (input.length() > 3 || input.length() < 2) throw new IllegalArgumentException();
        String rows = " ABCDEFGHIJ";
        Cell cell = new Cell();
        cell.setX(Integer.parseInt(input.substring(1)));
        cell.setY(rows.indexOf(input.toUpperCase().charAt(0)));
        if (cell.getX() < 1 || cell.getX() > 10) throw new IllegalArgumentException();
        if (cell.getY() < 1) throw new IllegalArgumentException();
        return cell;
    }
}
enum Shot {
    HIT,
    MISS,
    SANK,
    WIN
}
class BattleField {
    private final char[][] field;
    private final Ship[][] fleet;
    private final char[] rows = {'A','B','C','D','E','F','G','H','I','J'};
    private int fleetSize = 5;
    public BattleField() {
        this.field = new char[10][10];
        for (char[] chars : field) {
            Arrays.fill(chars, '~');
        }
        this.fleet = new Ship[10][10];
    }
    public void print() {
        for (int i = 0; i <= 10; i++) {
            for (int j = 0; j <= 10; j++) {
                if (i == 0) {
                    if (j == 0)
                        System.out.print("  ");
                    else System.out.print(j + " ");
                } else if (j == 0)
                    System.out.print(rows[i-1] + " ");
                else System.out.print(this.field[i-1][j-1] + " ");
            }
            System.out.println();
        }
        System.out.println();
    }
    public boolean free(Position position) {
        // left upper corner
        int startX = position.getX1() == 1 ? 1 : position.getX1() - 1;
        int startY = position.getY1() == 1 ? 1 : position.getY1() - 1;
        // right lower corner
        int endX = position.getX2() == 10 ? 10 : position.getX2() + 1;
        int endY = position.getY2() == 10 ? 10 : position.getY2() + 1;
        for (int i = startY; i <= endY; i++) {
            for (int j = startX; j <= endX; j++) {
                if (field[i-1][j-1] == 'O') return false;
            }
        }
        return true;
    }
    public boolean putIn(Position position, Ship ship) {
        if (!locationOK(position)) {
            System.out.print("Error! Wrong ship location! Try again:");
            return false;
        }
        if (!sizeOK(position, ship)) {
            System.out.printf("Error! Wrong length of the %s! Try again:", ship.getName());
            return false;
        }
        if (!free(position)) {
            System.out.print("Error! You placed it too close to another one. Try again:");
            return false;
        }
        ship.setPosition(position);
        for (int i = position.getY1(); i <= position.getY2(); i++) {
            for (int j = position.getX1(); j <= position.getX2(); j++) {
                field[i-1][j-1] = 'O';
                fleet[i-1][j-1] = ship;
            }
        }
       return true;
    }
    public void putIn(int X1, int Y1, int X2, int Y2, BattleField field) {
        Position position = new Position(X1, Y1, X2, Y2);
    }
    private boolean locationOK(Position position) {
        if (position.getX1() < 1 || position.getX1() > 10) return false;
        if (position.getX2() < 1 || position.getX2() > 10) return false;
        if (position.getY1() < 1 || position.getY1() > 10) return false;
        if (position.getY2() < 1 || position.getY2() > 10) return false;
        return position.getX1() == position.getX2() ||
                position.getY1() == position.getY2();
    }
    private boolean sizeOK(Position position, Ship ship) {
        if (position.getX1() == position.getX2() &&
                Math.abs(position.getY1() - position.getY2()) + 1 == ship.getSize()) return true;
        return position.getY1() == position.getY2() &&
                Math.abs(position.getX1() - position.getX2()) + 1 == ship.getSize();
    }
    public Shot fire(Cell target) {
        switch (field[target.getY() - 1][target.getX() - 1]) {
            case 'O' -> {
                hit(target);
                fleet[target.getY() - 1][target.getX() - 1].hit();
                if (fleet[target.getY() - 1][target.getX() - 1].alive())
                    return Shot.HIT;
                else {
                    fleetSize--;
                    if (shipExist())
                        return Shot.SANK;
                    else
                        return Shot.WIN;
                }
            }
            case 'X' -> {
                return Shot.HIT;
            }
            case 'M' -> {
                return Shot.MISS;
            }
            default -> {
                miss(target);
                return Shot.MISS;
            }
        }
    }
    public void hit(Cell target) {
        field[target.getY() - 1][target.getX() - 1] = 'X';
    }
    public void miss(Cell target) {
        field[target.getY() - 1][target.getX() - 1] = 'M';
    }
    public boolean shipExist() {
        return fleetSize > 0;
    }
}
class Ship {
    public enum Type {
        CARRIER("Aircraft Carrier", 5),
        BATTLESHIP("Battleship", 4),
        SUBMARINE("Submarine", 3),
        CRUISER("Cruiser", 3),
        DESTROYER("Destroyer", 2);
        private final int size;
        private final String name;
        Type(String name, int size) {
            this.size = size;
            this.name = name;
        }
        public int getSize() {
            return size;
        }
        public String getName() {
            return name;
        }
    }
    private final Type type;
    private final String name;
    private int size;
    private Position position;
    public Ship(Type type) {
        this.type = type;
        name = type.getName();
        size = type.getSize();
        position = new Position();
    }
    public Type getType() {
        return type;
    }
    public String getName() {
        return name;
    }
    public int getSize() {
        return size;
    }
    public Position getPosition() {
        return position;
    }
    public void setPosition(Position position) {
        this.position = position;
    }
    public void hit() {
        size--;
    }
    public boolean alive() {
        return size > 0;
    }
}
class Position {
    private final Cell begin; // begin
    private final Cell end; // end
    public Position() {
        begin = new Cell();
        end = new Cell();
    }
    public Position(int x1, int y1, int x2, int y2) {
        begin = new Cell(x1,y1);
        end = new Cell(x2, y2);
    }
    public int getX1() {
        return begin.getX();
    }
    public void setX1(int x) {
        begin.setX(x);
    }
    public int getY1() {
        return begin.getY();
    }
    public void setY1(int y) {
        begin.setY(y);
    }
    public int getX2() {
        return end.getX();
    }
    public void setX2(int x) {
        end.setX(x);
    }
    public int getY2() {
        return end.getY();
    }
    public void setY2(int y) {
        end.setY(y);
    }
    public String toString() {
        return "X1=" + begin.getX() + " Y1=" + begin.getY() + " X2=" + end.getX() + " Y2=" + end.getY();
    }
}
class Cell {
    private int x, y; // begin
    public Cell() {
        x = 0;
        y = 0;
    }
    public Cell(int x, int y) {
        this.x = x;
        this.y = y;
    }
    public int getX() {
        return x;
    }
    public void setX(int x) {
        this.x = x;
    }
    public int getY() {
        return y;
    }
    public void setY(int y) {
        this.y = y;
    }
    public String toString() {
        return "X1=" + x + " Y1=" + y;
    }
}

