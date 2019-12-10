package PhaseCongruency;

import java.awt.Rectangle;

import ij.IJ;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
 * Clase que se crea con la finalidad de encapsular los métodos comunes sobre
 * plugins que se trabajan en base a la FFT_Radix2, que es sobre imágenes
 * cuadradas, y por tanto se deben realizar ajustes en imágenes no cuadradas
 *
 * La función adjustRadix2,está basada en la función filter en el código de la
 * clase FFTFilter, del paquete ij.plugin.filter.
 * https://imagej.nih.gov/ij/developer/source/
 *
 * @author Carlos Jacanamejoy Grupo de Investigación Naturatu Universidad de
 * Ibague Junio de 2016
 */
public class Radix2Processor extends FloatProcessor {

    protected boolean cuadradaP2; //Si originalmente la imagen es cuadradaP2
    //Usados por la transforamda
    protected int originalWidth;
    protected int originalHeight;
    // Factor para hacer el tileMirror, en caso de
    // que una imagen es muy grande este factor 
    // se convierte en 1 para evitar demasiado
    // costo computacional, en cambio si la 
    // imagen es pequeña el problema de los extremos
    // se hace mas notorio y por ello este factor
    // es de 1.5
    protected double f=1.5;

    public Radix2Processor(ImageProcessor ip) {
        super(ip.getWidth(), ip.getHeight(), (float[]) ((ip instanceof FloatProcessor) ? ip.duplicate().getPixels() : ip.convertToFloat().getPixels()), null);
        if (ip instanceof Radix2Processor) {
            originalWidth = ((Radix2Processor) ip).getOriginalWidth();
            originalHeight = ((Radix2Processor) ip).getOriginalHeight();
            cuadradaP2 = ((Radix2Processor) ip).getCuadradaP2();
            f=((Radix2Processor) ip).getf();
        } else {
            this.originalHeight = this.height;
            this.originalWidth = this.width;
            this.cuadradaP2 = esCuadradaP2(this.width, this.height);
            // Para ahorrar tiempo de ejecución en imágenes grandes
            // se reajusta el factor a 1
            if(Math.max(this.height, this.width)>512)
                f=1;
        }
    }

    public boolean esCuadradaP2(int wd, int ht) {
        int i = 2;
        while (i < wd) {
            i <<= 1;
        }
        return i == wd && wd == ht;
    }

    /**
     * Ajusta la imagen a un caudrado usando tileMirror
     *
     * @return: el processor ajustado
     *
     */
    public Radix2Processor adjustRadix2() {
        if (esCuadradaP2(this.width, this.height)) {
            return this;//NO se hizo ajuste porque ya es cuadrada
        }
        Rectangle roiRect = this.getRoi();
        int maxN = (int) (f*Math.max(roiRect.width, roiRect.height));

        /*
         * tile mirrored image to power of 2 size first determine smallest power
         * 2 >= 1.5 * image width/height factor of 1.5 to avoid wrap-around
         * effects of Fourier Trafo
         */
        int i = 2;
        while (i < maxN) {
            i *= 2;
        }
        // fit image into power of 2 size
        Rectangle fitRect = new Rectangle();
        fitRect.x = (int) Math.round((i - roiRect.width) / 2.0);
        fitRect.y = (int) Math.round((i - roiRect.height) / 2.0);
        fitRect.width = roiRect.width;
        fitRect.height = roiRect.height;

        // put image (ROI) into power 2 size image
        // mirroring to avoid wrap around effects
        return tileMirror(i, i, fitRect.x, fitRect.y);
    }

    public Radix2Processor originalImage() {
        if (width != originalWidth && height != originalHeight
                && this.esCuadradaP2(width, height)) {
            //Extrae la imagen original
            ImageProcessor ipR;
            int i = 2;
            int maxN = (int) (f*Math.max(originalWidth, originalHeight));
            while (i < maxN) {
                i *= 2;
            }
            Rectangle fitRect = new Rectangle();
            Rectangle orRect = this.getRoi();
            fitRect.x = (int) Math.round((i - originalWidth) / 2.0);
            fitRect.y = (int) Math.round((i - originalHeight) / 2.0);
            fitRect.width = originalWidth;
            fitRect.height = originalHeight;
            this.setRoi(fitRect);
            ipR = this.crop();
            this.setRoi(orRect);
            Radix2Processor ipNew = new Radix2Processor(ipR);
            return ipNew;
        }
        if (width != originalWidth && height != originalHeight
                && !this.esCuadradaP2(width, height)) {
            IJ.error("Hay un error en la gestion de las dimensiones de la imagen");
        }
        return this;
    }

    /**
     *
     * @param width: nuevo ancho
     * @param height: nuevo alto
     * @param x: coordenada x de inicio de la imagen
     * @param y: coordenada y de inicio de la imagen
     * @return: retorna la imagen con el tileMirror
     */
    private Radix2Processor tileMirror(int width, int height, int x, int y) {
        if (x < 0 || x > (width - 1) || y < 0 || y > (height - 1)) {
            IJ.error("Image to be tiled is out of bounds.");
            return null;
        }

        ImageProcessor ipout = this.createProcessor(width, height);
        //Crea una copia del actual ip
        ImageProcessor ip2 = this.crop();
        int w2 = ip2.getWidth();
        int h2 = ip2.getHeight();

        // how many times does ip2 fit into ipout?
        int i1 = (int) Math.ceil(x / (double) w2);
        int i2 = (int) Math.ceil((width - x) / (double) w2);
        int j1 = (int) Math.ceil(y / (double) h2);
        int j2 = (int) Math.ceil((height - y) / (double) h2);
        
        // tile
        if ((i1 % 2) > 0.5) {
            ip2.flipHorizontal();
        }
        if ((j1 % 2) > 0.5) {
            ip2.flipVertical();
        }

        for (int i = -i1; i < i2; i += 2) {
            for (int j = -j1; j < j2; j += 2) {
                ipout.insert(ip2, x - i * w2, y - j * h2);
            }
        }

        ip2.flipHorizontal();
        for (int i = -i1 + 1; i < i2; i += 2) {
            for (int j = -j1; j < j2; j += 2) {
                ipout.insert(ip2, x - i * w2, y - j * h2);
            }
        }

        ip2.flipVertical();
        for (int i = -i1 + 1; i < i2; i += 2) {
            for (int j = -j1 + 1; j < j2; j += 2) {
                ipout.insert(ip2, x - i * w2, y - j * h2);
            }
        }

        ip2.flipHorizontal();
        for (int i = -i1; i < i2; i += 2) {
            for (int j = -j1 + 1; j < j2; j += 2) {
                ipout.insert(ip2, x - i * w2, y - j * h2);
            }
        }
        Radix2Processor ipNew = new Radix2Processor(ipout);
        ipNew.originalHeight = h2;
        ipNew.originalWidth = w2;
        ipNew.cuadradaP2 = esCuadradaP2(w2, h2);
        return ipNew;
    }

    public int getOriginalWidth() {
        return originalWidth;
    }

    public int getOriginalHeight() {
        return originalHeight;
    }

    public boolean getCuadradaP2() {
        return cuadradaP2;
    }
    public double getf(){
        return f;
    }
}
