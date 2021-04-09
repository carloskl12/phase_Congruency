package PhaseCongruency;

import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
* FilterGrid.java
* Created on 12 December 2019 by 
* - Carlos Antonio Jacanamejoy-Jamioy (e-mail:carloskl12@gmail.com) 
* - Guillermo Forero-Vargas (e-mail: mgforero@yahoo.es)
*
* This class implements the periodic plus smooth image decomposition filter 
* according to:
* - Moisan, L. (2011). Periodic plus smooth image decomposition.
*   Journal of Mathematical Imaging and Vision, 39(2), 161-179.
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

public class PerComp extends Radix2Processor {

    double[][] real;
    double[][] imag;

    public PerComp(ImageProcessor ip) {
        super(ip);
    }

    public void perComp() {
        if (this.isPower2(width, height)) {
            int n;//ancho y alto de la imagen
            float[] fhtdata;
            ImageProcessor ipBordes = extraeBordes(this);
            n = ipBordes.getWidth();
            //Dominio en espacio de frecuencias
            double[] dominio = new double[n];
            double inc = 2 * Math.PI / n; //Incremento
            double ac = 0;
            for (int i = 0; i < n; i++) {
                dominio[i] = ac;
                ac += inc;
            }

            FFTRadix2 fft = new FFTRadix2(ipBordes);//Ip de resultado
            fft.fft2();
            real = fft.real;
            imag = fft.imag;
            for (int i = 0; i < n; i++) {
                double funH;
                for (int j = 0; j < n; j++) {
                    funH = (2 * (2 - Math.cos(dominio[j]) - Math.cos(dominio[i])));
                    funH = funH == 0 ? 0 : 1 / (funH);
                    real[i][j] *= funH;
                    imag[i][j] *= funH;
                }
            }
            double[][] sr = real.clone();
            double[][] si = imag.clone();
            fft.setPixels(this.getPixels());
            fft.fft2();
            real = fft.real;
            imag = fft.imag;
            for (int i = 0; i < n; i++) {
                for (int j = 0; j < n; j++) {
                    real[i][j] -= sr[i][j];
                    imag[i][j] -= si[i][j];
                }
            }
            //Deja una copia en los coeficientes
            real = fft.real.clone();
            imag = fft.imag.clone();

            fft.update();
            fft.fft2();
            fhtdata = (float[]) fft.getPixels();
            this.setPixels(fhtdata);
        } else {
            //Imagen no cuadrada
            throw new RuntimeException("Error, la imagen debe ser cuadrada ");
        }
    }

    ImageProcessor extraeBordes(ImageProcessor ip) {
        int wd = ip.getWidth();
        int ht = ip.getHeight();
        if (!(ip instanceof FloatProcessor)) {
            throw new RuntimeException("Error, el procesador debe ser float ");
        }

        ImageProcessor ipBordes = ip.createProcessor(wd, ht);

        float[] dtBordes = (float[]) ipBordes.getPixels();
        float[] dtImg = (float[]) ip.getPixels();
        float val;
        // Extrae los bordes
        int offsetx = wd * (ht - 1);// Inicio ultima fila
        int offsety = 0;
        //Filas superior-inferior.
        for (int i = 0; i < wd; i++) {
            val = (dtImg[i] - dtImg[offsetx]);
            dtBordes[i] = val;
            val = (-dtBordes[i]);
            dtBordes[offsetx] = val;
            offsetx++;
        }

        //Columnas izquierda-derecha
        for (int i = 0; i < ht; i++) {
            val = dtBordes[offsety] + dtImg[offsety] - dtImg[offsety + wd - 1];
            dtBordes[offsety] = val;
            val = dtBordes[offsety + wd - 1] - dtBordes[offsety];
            dtBordes[offsety + wd - 1] = val;
            offsety += wd;
        }

        return ipBordes;
    }
}
