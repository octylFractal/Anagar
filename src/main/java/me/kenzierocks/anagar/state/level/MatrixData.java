package me.kenzierocks.anagar.state.level;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.google.auto.value.AutoValue;

@AutoValue
abstract class MatrixData {

    private static final Random CHAR_RAND = new Random();
    private static final int SPACE_PLUS_ONE = ' ' + 1;
    private static final int PERCENT_NON_SPACE = 30;

    private static String getRandomString(int length) {
        StringBuilder b = new StringBuilder();
        for (int j = 0; j < length; j++) {
            if (CHAR_RAND.nextInt(100) > PERCENT_NON_SPACE) {
                b.append((char) (CHAR_RAND.nextInt('~' - SPACE_PLUS_ONE) + SPACE_PLUS_ONE));
            } else {
                b.append(' ');
            }
        }
        return b.toString();
    }

    public static final MatrixData random(int width, int height) {
        List<String> grid = new ArrayList<>(height);
        for (int i = 0; i < height; i++) {
            grid.add(getRandomString(width));
        }
        return new AutoValue_MatrixData(grid, width, height);
    }

    public static final MatrixData fromGrid(List<String> grid) {
        return new AutoValue_MatrixData(grid, grid.get(0).length(), grid.size());
    }

    abstract List<String> getGrid();

    abstract int getWidth();

    abstract int getHeight();

    MatrixData advance() {
        List<String> grid = getGrid();
        grid.remove(grid.size() - 1);
        grid.add(0, getRandomString(getWidth()));
        return fromGrid(grid);
    }

}
