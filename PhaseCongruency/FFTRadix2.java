package PhaseCongruency;

import ij.IJ;
import ij.process.ImageProcessor;

/**
* FFTRadix2.java
* Created on 12 December 2019 by 
* - Carlos Antonio Jacanamejoy-Jamioy (e-mail:carloskl12@gmail.com) 
* - Guillermo Forero-Vargas (e-mail: mgforero@yahoo.es)
*
* This class implements the FFT Radix-2 transform, 
* inspired on the original code fft.c. by Douglas L. Jones
* University of Illinois at Urbana-Champaign January 19, 1992
* http://cnx.rice.edu/content/m12016/latest/
*
* Copyright (c) 2019 by 
* - Carlos Antonio Jacanamejoy-Jamioy (e-mail:carloskl12@gmail.com) 
* - Guillermo Forero-Vargas (e-mail: mgforero@yahoo.es)
*
* This code is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3
* as published by the Free Software Foundation.
*
* This code is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this plugin; if not, write to the Free Software
* Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/

public class FFTRadix2 extends Radix2Processor {

    protected boolean isFrequencyDomain;

    //n: Ancho|alto de la imagen
    //m: log2(n)
    protected int n, m;

    //Tablas
    protected double cos[];
    protected double sin[];
    protected boolean tabla = false;
    public double real[][];
    public double imag[][];

    /**
     * Constructor del procesador, si se hace a partir de otro procesador se
     * convierte a FloatProcessor Es posible ingresar si los datos estan en el
     * dominio de la frecuencia
     */
    public FFTRadix2(ImageProcessor ip) {
        this(ip, false);
        if (ip instanceof FFTRadix2) {
            this.isFrequencyDomain = ((FFTRadix2) ip).isFrequencyDomain;
            this.real = ((FFTRadix2) ip).real.clone();
            this.imag = ((FFTRadix2) ip).imag.clone();
            this.cos = ((FFTRadix2) ip).cos.clone();
            this.sin = ((FFTRadix2) ip).sin.clone();
            this.tabla = ((FFTRadix2) ip).tabla;
            this.n = ((FFTRadix2) ip).n;
            this.m = ((FFTRadix2) ip).m;
        }
    }

    public FFTRadix2(ImageProcessor ip, boolean isFrequencyDomain) {
        super(ip);
        this.isFrequencyDomain = isFrequencyDomain;
        if (this.isPower2(width, height)) {
            n = getWidth();
            resetRoi();
            m = 0;
            int i;
            i = n;
            while (i > 1) {
                i = i >> 1;
                m++;
            }
        } else {
            IJ.error("La imagen debe ser cuadrada y potencia de dos");
            throw new RuntimeException("Error, la imagen debe ser cuadrada "
                    + "y potencia de dos");
        }
    }

    /**
     * *************************************************************
     * fft.c Douglas L. Jones University of Illinois at Urbana-Champaign January
     * 19, 1992 http://cnx.rice.edu/content/m12016/latest/
     *
     * fft: in-place radix-2 DIT DFT of a complex input
     *
     * input: n: length of FFT: must be a power of two m: n = 2**m input/output
     * x: double array of length n with real part of data y: double array of
     * length n with imag part of data
     *
     * Permission to copy and use this program is granted as long as this header
     * is included.
     ***************************************************************
     */
    public void fft(double[] x, double[] y) {
        int i, j, k, n1, n2, a;
        double c, s, t1, t2;
        // Bit-reverse
        j = 0;
        n2 = n / 2;
        for (i = 1; i < n - 1; i++) {
            n1 = n2;
            while (j >= n1) {
                j = j - n1;
                n1 = n1 / 2;
            }
            j = j + n1;

            if (i < j) {
                t1 = x[i];
                x[i] = x[j];
                x[j] = t1;
                t1 = y[i];
                y[i] = y[j];
                y[j] = t1;
            }
        }

        // FFT
        n1 = 0;
        n2 = 1;

        for (i = 0; i < m; i++) {
            n1 = n2;
            n2 = n2 + n2;
            a = 0;

            for (j = 0; j < n1; j++) {
                c = cos[a];
                s = sin[a];
                a += 1 << (m - i - 1);

                for (k = j; k < n; k = k + n2) {
                    t1 = c * x[k + n1] - s * y[k + n1];
                    t2 = s * x[k + n1] + c * y[k + n1];
                    x[k + n1] = x[k] - t1;
                    y[k + n1] = y[k] - t2;
                    x[k] = x[k] + t1;
                    y[k] = y[k] + t2;
                }
            }
        }
    }

    //Genera tabla de senos y cosenos

    protected void genTabla() {
        for (int i = 0; i < n / 2; i++) {
            cos[i] = Math.cos(-2 * Math.PI * i / n);
            sin[i] = Math.sin(-2 * Math.PI * i / n);
        }
    }

    /**
     * *************************************************************
     * Esta transformada usa la información de ip como parte real de la
     * transformada de fourier compleja Luego, si se aplica la misma
     * transformada de fourier los datos dados en el espacio de TH, se obtiene
     * nuevamente la imagen
     ***************************************************************
     */
    public void fft2() {
        ImageProcessor ip = this;
        int i, j;
        int offset;
        this.isFrequencyDomain = !this.isFrequencyDomain;
        double x[] = new double[n];
        double y[] = new double[n];
        //Matrices de coeficientes
        real = new double[n][n];
        imag = new double[n][n];

        float dt[] = (float[]) ip.getPixels();
        for (i = 0; i < n; i++) {
            offset = i * n;
            for (j = 0; j < n; j++) {
                real[i][j] = dt[offset + j];
            }
        }

        if (!tabla) {
            cos = new double[n / 2];
            sin = new double[n / 2];
            genTabla();
            tabla = true;
        }
        //Pasada horizontal
        for (i = 0; i < n; i++) {
            //Extrae pares de filas
            for (j = 0; j < n; j++) {
                x[j] = real[i][j];
                y[j] = imag[i][j];
            }
            fft(x, y);
            //Realiza los cambios
            for (j = 0; j < n; j++) {
                real[i][j] = x[j];
                imag[i][j] = y[j];
            }
        }
        //Pasada vertical
        for (i = 0; i < n; i++) {
            //Extrae pares de filas
            for (j = 0; j < n; j++) {
                x[j] = real[j][i];
                y[j] = imag[j][i];
            }
            fft(x, y);
            //Realiza los cambios
            for (j = 0; j < n; j++) {
                real[j][i] = x[j];
                imag[j][i] = y[j];
            }
        }

        //Guarda la transformada en el ip como una FHT
        //La división para n se realiza con el fin de que
        //Se use la misma salida real como entrada a la fft2
        for (i = 0; i < n; i++) {
            offset = i * n;
            for (j = 0; j < n; j++) {
                dt[offset + j] = (float) ((real[i][j] - imag[i][j]) / n);
            }
        }
    }
    //Asume que los valores estan guardados en las
    //matrices real e imaginaria

    void fftComplex() {
        int i, j;
        double x[] = new double[n];
        double y[] = new double[n];
        //Matrices de coeficientes
        if (!tabla) {
            cos = new double[n / 2];
            sin = new double[n / 2];
            genTabla();
            tabla = true;
        }
        //Pasada horizontal
        for (i = 0; i < n; i++) {
            //Extrae pares de filas
            for (j = 0; j < n; j++) {
                x[j] = real[i][j];
                y[j] = imag[i][j];
            }
            fft(x, y);
            //Realiza los cambios
            for (j = 0; j < n; j++) {
                real[i][j] = x[j];
                imag[i][j] = y[j];
            }
        }
        //Pasada vertical
        for (i = 0; i < n; i++) {
            //Extrae pares de filas
            for (j = 0; j < n; j++) {
                x[j] = real[j][i];
                y[j] = imag[j][i];
            }
            fft(x, y);
            //Realiza los cambios
            for (j = 0; j < n; j++) {
                real[j][i] = x[j];
                imag[j][i] = y[j];
            }
        }

    }

    public void ifftComplex() {
        //Aplica el conjugado y hace la trasnformada de fourier
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                imag[i][j] *= -1;
            }
        }
        fftComplex();
        //Aplica el conjugado y divide
        //Divide
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                imag[i][j] /= (-width * height);
                real[i][j] /= (width * height);
            }
        }
    }

    public void ifft2() {
        if (this.isFrequencyDomain) {
            this.fft2();
        }
    }
    //Función para pasar de FFT a FHT modificada,
    //Usada si se hacen cambios desde afuera
    //A los coeficientes reales e imaginarios
    //Para así actualizar la información propia del
    //Ip

    public void update() {
        int offset, i, j;
        float dt[] = (float[]) this.getPixels();
        for (i = 0; i < n; i++) {
            offset = i * n;
            for (j = 0; j < n; j++) {
                dt[offset + j] = (float) ((real[i][j] - imag[i][j]) / n);
            }
        }
    }

    //Función para visualizar la imagen de espectro
    //Se da en escala logaritmica
    public void espectroFFT2(boolean swap, boolean log) {
        int offset, i, j;
        float dt[] = (float[]) this.getPixels();
        if(swap)
            this.swapQuadrants();
        for (i = 0; i < n; i++) {
            offset = i * n;
            for (j = 0; j < n; j++) {
                if (log)
                    dt[offset + j] = (float) (32 * Math.log(Math.sqrt(real[i][j] * real[i][j] + imag[i][j] * imag[i][j]) / n +1));
                else
                    dt[offset + j] = (float) (Math.sqrt(real[i][j] * real[i][j] + imag[i][j] * imag[i][j]) / n );
            }
        }
        if(swap)
            this.swapQuadrants();
    }

    //Genera el espectro sin estar en escala logaritmica

    void especNFFT2() {
        int offset, i, j;
        float dt[] = (float[]) this.getPixels();
        this.swapQuadrants();
        for (i = 0; i < n; i++) {
            offset = i * n;
            for (j = 0; j < n; j++) {
                dt[offset + j] = (float) (Math.sqrt(real[i][j] * real[i][j] + imag[i][j] * imag[i][j]));
            }
        }
        this.swapQuadrants();
    }

    //Cambia cuadrantes de los arreglos de complejos y reales

    public void swapQuadrants() {
        int i, j;
        double tmp;
        int v1;
        v1 = n / 2;
        for (i = 0; i < n / 2; i++) {
            for (j = 0; j < n / 2; j++) {
                //Para real
                tmp = this.real[i][j];
                real[i][j] = real[i + v1][j + v1];
                real[i + v1][j + v1] = tmp;
                tmp = real[i + v1][j];
                real[i + v1][j] = real[i][j + v1];
                real[i][j + v1] = tmp;

                //Para imag;
                tmp = imag[i][j];
                imag[i][j] = imag[i + v1][j + v1];
                imag[i + v1][j + v1] = tmp;
                tmp = imag[i + v1][j];
                imag[i + v1][j] = imag[i][j + v1];
                imag[i][j + v1] = tmp;
            }
        }
    }

}
