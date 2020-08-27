import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class SudokuChecker {

    static Set<Integer> nums = new HashSet<>();

    static {
        for(int i = 1; i <= 9;i++)
        {
            nums.add(i);
        }
    }

    public static void main(String[] args) {

        int[][] solution = new int[9][9];

        Scanner sc = new Scanner(System.in);

        for(int r = 0; r < solution.length;r++)
        {
            for(int c = 0;c < solution[r].length;c++) {
                String s = sc.next();
                if (s.matches("\\d")) {
                    solution[r][c] = Integer.parseInt(s);
                }
                else{
                    c--;
                }
            }
        }
        sc.close();

        boolean solved = solve(solution);

        System.out.println("\n" + (solved?"VALID SOLUTION":"INVALID SOLUTION"));
    }

    static boolean solve(int[][] solution)
    {
        boolean solved = checkCols(solution);
        if(solved)
        {
            solved = checkRows(solution);
            if(solved)
            {
                solved = checkSquares(solution);
            }
        }

        return solved;
    }

    private static boolean checkSquares(int[][] solution)
    {
        for(int row = 0; row < solution.length;row+=3)
        {
            for(int col = 0; col < solution[row].length;col+=3) {
                Set<Integer> set = new HashSet<>(nums);
                for(int r = row;r < row + 3;r++) {
                    for(int c = col; c < col +3;c++) {
                        set.remove(solution[r][c]);
                    }
                }
                if (!set.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    private static boolean checkRows(int[][] solution)
    {
        for (int[] ints : solution) {
            Set<Integer> set = new HashSet<>(nums);
            for (int anInt : ints) {
                set.remove(anInt);
            }

            if (!set.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private static boolean checkCols(int[][] solution)
    {
        for(int c = 0; c < solution[0].length;c++)
        {
            Set<Integer> set = new HashSet<>(nums);
            for (int[] ints : solution) {
                set.remove(ints[c]);
            }

            if(!set.isEmpty())
            {
                return false;
            }
        }
        return true;
    }
}
