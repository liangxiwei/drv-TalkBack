package com.example.jrd48.chat.wiget;

public class PuzzleLayout {

    public static final String TAG = PuzzleLayout.class.getSimpleName();
    public static final float MSPACINGRATIO = 0.02f;
    private static final float[] sizes = {(1 - 3 * MSPACINGRATIO) / 2, (1 - 4 * MSPACINGRATIO) / 3};

    public static int max() {
        return 9;
    }

    public static float size(int count) {
        int i = 0;
        if (count > 4) {
            i = 1;
        }
        return sizes[i];
    }

    public static float[] offset(int count, int index, float dimension, float size) {
        // view 直径
        float cd = (float) dimension;
        // 边距
        float s1 = cd * size;
        // 间距
        float j = cd * MSPACINGRATIO;

        switch (count) {
            case 1:
                return new float[]{(cd - s1) / 2, (cd - s1) / 2};
            case 2:
                return new float[]{(index % 2 + 1) * j + (index % 2) * s1, (cd - s1) / 2};
            case 3:
                if (index > 0) {
                    index = index + 1;
                    return new float[]{(index % 2 + 1) * j + (index % 2) * s1, (index / 2 + 1) * j + (index / 2) * s1};
                } else {
                    return new float[]{cd / 2 - s1 / 2, j};
                }
            case 4:
                return new float[]{(index % 2 + 1) * j + (index % 2) * s1, (index / 2 + 1) * j + (index / 2) * s1};
            case 5:
                if (index > 1) {
                    return new float[]{((index + 1) % 3 + 1) * j + ((index + 1) % 3) * s1, (cd / 2 - j / 2 - s1) + (index + 1) / 3 * (j + s1)};
                } else {
                    return new float[]{(cd / 2 - j / 2 - s1) + index * (j + s1), (cd / 2 - j / 2 - s1) + index / 3 * (j + s1)};
                }
            case 6:
                return new float[]{(index % 3 + 1) * j + (index % 3) * s1, (cd / 2 - j / 2 - s1) + index / 3 * (j + s1)};
            case 7:
                if (index > 0) {
                    return new float[]{((index + 2) % 3 + 1) * j + ((index + 2) % 3) * s1, ((index + 2) / 3 + 1) * j + ((index + 2) / 3) * s1};
                } else {
                    return new float[]{(cd - j - s1) / 2, (index / 3 + 1) * j + (index / 3) * s1};
                }
            case 8:
                if (index > 1) {
                    return new float[]{((index + 1) % 3 + 1) * j + ((index + 1) % 3) * s1, ((index + 1) / 3 + 1) * j + ((index + 1) / 3) * s1};
                } else {
                    return new float[]{(cd / 2 - j / 2 - s1) + index * (j + s1), (index / 3 + 1) * j + (index / 3) * s1};
                }
            case 9:
                return new float[]{(index % 3 + 1) * j + (index % 3) * s1, (index / 3 + 1) * j + (index / 3) * s1};
            default:
                break;
        }
        return new float[]{0f, 0f};
    }

}
