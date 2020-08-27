import java.io.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class Sudoku {

    private static final int[] binary = {511,1,2,4,8,16,32,64,128,256};
    private static final Set<Integer> lone = new HashSet<>();

    private static boolean changed = false;

    static {
        for(int i = 1; i < binary.length;i++)
        {
            lone.add(binary[i]);
        }
    }

    public static void main(String[] args) throws IOException {

        String path = args.length > 0 ? args[0] : "Problems/sudoku7.txt";

        int[][] solution = new int[9][9];

        int[][] puzzle = readData(path, solution);

        int count = 81;

        System.out.println();

        long start = System.currentTimeMillis();

        for (int[] ints : puzzle) {
            for (int anInt : ints) {
                if (anInt != binary[0]) {
                    count--;
                }
            }
        }

        solve(count,puzzle,solution);

        System.out.println("done");

        long end = System.currentTimeMillis();

        System.out.println("Runtime = " + (end - start) + " ms");
    }

    private static boolean solve(int count, int[][] puzzle, int[][] solution) {
        int iterations = 0;

        boolean solved = true;

        solver:
        while(count > 0)
        {
            changed = false;
            int original = count;
            for(int i = 1;i <= 9;i++)
            {
                for(int c = 0; c < 9;c++) {
                    count -= validateCol(c,i,puzzle,solution);
                    count -= validateRow(c,i,puzzle,solution);
                }

                for(int r = 0; r < 9;r+=3)
                {
                    for(int c = 0;c<9;c+=3)
                    {
                        count -= validateSquare(r,c,i,puzzle,solution);
                    }
                }
            }

            for(int i = 1;i <= 9;i++)
            {
                for(int r = 0; r < 9;r++)
                {
                    int col = -1,row = -1;
                    for(int  c = 0; c < 9 && (col != -2 || row != -2);c++)
                    {
                        if(col != -2 && (puzzle[r][c] & binary[i]) == binary[i])
                        {
                            if(col != -1) {
                                col = -2;
                            }
                            else {
                                col = c;
                            }
                        }

                        if(row != -2 && (puzzle[c][r] & binary[i]) == binary[i]) {
                            if (row != -1) {
                                row = -2;
                            }
                            else {
                                row = c;
                            }
                        }
                    }

                    if(row == -1 || col == -1)
                    {
                        solved = false;
                        break solver;
                    }

                    if(col != -2) {
                        if(puzzle[r][col] != binary[i])
                        {
                            puzzle[r][col] = binary[i];
                            count -= confirm(r, col, i, puzzle,solution);
                        }
                    }
                    if(row != -2) {
                        if (puzzle[row][r] != binary[i]) {
                            puzzle[row][r] = binary[i];
                            count -= confirm(row, r, i, puzzle, solution);
                        }
                    }

                }

                for(int r = 0; r < 9;r+=3)
                {
                    next:
                    for(int c = 0;c<9;c+=3)
                    {
                        int row = -1,col = -1;
                        for(int ro = r;ro < r + 3;ro++)
                        {
                            for(int co = c;co < c + 3;co++)
                            {
                                if((puzzle[ro][co] & binary[i]) == binary[i])
                                {
                                    if(row != -1 || col != -1)
                                    {
                                        continue next;
                                    }
                                    else{
                                        row = ro;
                                        col = co;
                                    }
                                }
                            }
                        }

                        if(row != -1 && col != -1 && puzzle[row][col] != binary[i])
                        {
                            puzzle[row][col] = binary[i];
                            count -= confirm(row,col,i,puzzle,solution);
                        }
                    }
                }
            }

            //print(puzzle);
            //print(solution);
            count -= findLoners(puzzle,solution);
            iterations++;

            if(count == original && !changed)
            {
                solved = false;
                break;
            }
        }

        if(solved) {
            solved = SudokuChecker.solve(solution);
            if(solved)
                print(solution);
            return solved;
        }
        else{
            return branch(count,puzzle,solution);
        }
    }

    private static boolean branch(int count, int[][] puzzle,int[][] solution)
    {
        boolean result = false;
        master:
        for(int r = 0; r < puzzle.length;r++)
        {
            for(int c = 0; c < puzzle[r].length;c++)
            {
                if(!contains(puzzle[r][c]))
                {
                    int push = 0;
                    while(push < 9)
                    {
                        boolean b = ((puzzle[r][c] >> push) & 1) == 1;
                        if(b)
                        {
                            int[][] puzzle2 = Arrays.stream(puzzle).map(int[]::clone).toArray(int[][]::new);
                            int[][] solution2 = Arrays.stream(solution).map(int[]::clone).toArray(int[][]::new);

                            solution2[r][c] = push + 1;
                            puzzle2[r][c] = binary[push + 1];

                            boolean solved = solve(count - 1,puzzle2,solution2);
                            if(solved)
                            {
                                result = true;
                                break master;
                            }
                        }
                        push++;
                    }
                }
            }
        }

        return result;
    }

    private static void print(int[][] solution)
    {
        for (int[] ints : solution) {
            for (int anInt : ints) {
                System.out.print(anInt + "\t");
            }
            System.out.println();
        }
        System.out.println("----------------------------");
    }

    private static int findLoners(int[][] puzzle, int[][] solution)
    {
        int count = 0;
        for(int r = 0; r < puzzle.length;r++)
        {
            for(int c = 0; c < puzzle[r].length;c++)
            {
                if(solution[r][c] == 0 && contains(puzzle[r][c]))
                {
                    int num = indexOf(puzzle[r][c]);
                    count += confirm(r,c,num,puzzle,solution);
                    r = 0;
                    c = 0;
                }
            }
        }

        return count;
    }

    private static int indexOf(int bin)
    {
        int counter = 1;
        while(bin > 1)
        {
            bin/=2;
            counter++;
        }
        return counter;
    }

    private static int confirm(int r, int c, int num, int[][] puzzle,int[][] solution)
    {
        changed = true;
        int count = 1;
        solution[r][c] = num;
        for(int row = 0; row < 9;row++)
        {
            if(row != r && checkCell(row,c,num,puzzle))
            {
                puzzle[row][c] -= binary[num];
                if(contains(puzzle[row][c]))
                {
                    count += confirm(row,c,indexOf(puzzle[row][c]),puzzle,solution);
                }
                //System.out.println(puzzle[row][c] + " " + num + " @ " + row + " " + c);
            }
            if(row != c && checkCell(r,row,num,puzzle))
            {
                puzzle[r][row] -= binary[num];
                if(contains(puzzle[r][row]))
                {
                    count += confirm(r,row,indexOf(puzzle[r][row]),puzzle,solution);
                }
            }
        }
        int startR = r - (r % 3);
        int startC = c - (c % 3);
        for(int row = startR;row < startR + 3;row++){
            for(int col = startC;col < startC + 3;col++)
            {
                if(row != r && col != c && checkCell(row,col,num,puzzle))
                {
                    puzzle[row][col] -= binary[num];
                    if(contains(puzzle[row][col]))
                    {
                        count+= confirm(row,col,indexOf(puzzle[row][col]),puzzle,solution);
                    }
                }
            }
        }

        return count;
    }

    private static boolean checkCell(int r, int c, int num,int[][] puzzle)
    {
        return puzzle[r][c] != binary[num] && (puzzle[r][c] & binary[num]) == binary[num];
    }


    private static int validateSquare(int row, int col,int num, int[][] puzzle,int[][] solution)
    {
        boolean cont = false;
        checker:
        for(int r = row; r < row + 3;r++)
        {
            for(int c = col;c < col + 3;c++)
            {
                if(puzzle[r][c] == binary[num])
                {
                    cont = true;
                    break checker;
                }
            }
        }
        int count = 0;
        if(cont)
        {
            for(int r = row;r < row + 3;r++)
            {
                for(int c = col; c < col + 3; c++)
                {
                    if(checkCell(r,c,num,puzzle)){
                        changed = true;
                        puzzle[r][c] -= binary[num];
                        if(contains(puzzle[r][c]))
                        {
                            count+=confirm(r,c,indexOf(puzzle[r][c]),puzzle,solution);
                        }
                    }
                }
            }
        }
        return count;
    }

    private static int validateCol(int c, int num, int[][] puzzle,int[][] solution)
    {
        boolean cont = false;
        for (int[] ints : puzzle) {
            if (ints[c] == binary[num]) {
                cont = true;
                break;
            }
        }

        int count = 0;

        if(cont)
        {
            for(int r = 0; r < puzzle.length;r++)
            {
                if(checkCell(r,c,num,puzzle)){
                    puzzle[r][c] -= binary[num];
                    changed = true;
                    if(contains(puzzle[r][c]))
                    {
                        count+=confirm(r,c,indexOf(puzzle[r][c]),puzzle,solution);
                    }
                }
            }
        }

        return count;
    }

    private static int validateRow(int r, int num, int[][] puzzle,int[][] solution)
    {
        boolean cont = false;
        for (int c = 0; c < puzzle[r].length;c++) {
            if (puzzle[r][c] == binary[num]) {
                cont = true;
                break;
            }
        }

        int count = 0;

        if(cont)
        {
            for(int c = 0; c < puzzle[0].length;c++)
            {
                if(checkCell(r,c,num,puzzle)){
                    puzzle[r][c] -= binary[num];
                    changed = true;
                    if(contains(puzzle[r][c]))
                    {
                        count+=confirm(r,c,indexOf(puzzle[r][c]),puzzle,solution);
                    }
                }
            }
        }

        return count;
    }

    private static boolean contains(int n)
    {
        return lone.contains(n);
    }

    private static int[][] readData(String path,int[][] solution) throws IOException {

        int[][] puzzle = new int[9][9];

        BufferedReader br;

        if(path.equals("command"))
        {
            br = new BufferedReader(new InputStreamReader(System.in));
        }else {
            br = new BufferedReader(new FileReader(path));
        }


        for(int r = 0; r < puzzle.length;r++)
        {
            for(int c = 0; c < puzzle[r].length;c++)
            {
                int digit = br.read() - 48;

                if(digit == -2)
                {
                    digit = 0;
                }

                if(digit < 0 || digit > 9)
                {
                    c-=1;
                    continue;
                }

                puzzle[r][c] = binary[digit];
                solution[r][c] = digit;
            }
        }

        br.close();

        return puzzle;
    }
}
