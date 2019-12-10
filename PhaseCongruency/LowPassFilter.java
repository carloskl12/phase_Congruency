package PhaseCongruency;
import ij.IJ;

public class LowPassFilter implements Cloneable {

    int width;
    int height;
    int n;
    double cutoff;
    double[][] filter;

    /**
     *
     * @param width:Ancho
     * @param height: Alto
     * @param cutoff: Frecuencia de corte (0-0.5)
     * @param n: orden del filtro
     */
    LowPassFilter(int width, int height, double cutoff, int n) {
        if (cutoff > 0.5 || cutoff < 0) {
            IJ.error("La frecuencia de corte debe ser de 0-0.5");
        }
        this.width = width;
        this.height = height;
        this.cutoff = cutoff;
        this.n = n;

    }

    public void generate() {
        FilterGrid grid = new FilterGrid(width, height);
        filter = new double[height][width];
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                //Aplica la función del filtro
                filter[i][j] = 1 / (1 + Math.pow(grid.radius[i][j] / cutoff, 2 * n));
            }
        }
    }

    public Object clone() {
        Object obj = null;
        try {
            obj = super.clone();
        } catch (CloneNotSupportedException ex) {
            System.out.println(" No se puede duplicar");
        }
        return obj;
    }
}
