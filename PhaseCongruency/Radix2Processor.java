package PhaseCongruency;

import java.awt.Rectangle;

import ij.IJ;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/**
* Radix2Processor.java
* Created on 12 December 2019 by 
* - Carlos Antonio Jacanamejoy-Jamioy (e-mail:carloskl12@gmail.com) 
* - Guillermo Forero-Vargas (e-mail: mgforero@yahoo.es)
*
* Class that is created with the intention of encapsulating the common 
* methods on add-ons that are processed based on the FFT_Radix2, which 
* is on square images, and therefore adjustments must be made to 
* non-square images. This class is inspired on FFTFilter code 
* available on https://imagej.nih.gov/ij/developer/source/.
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
            this.cuadradaP2 = isPower2(this.width, this.height);
            // Para ahorrar tiempo de ejecución en imágenes grandes
            // se reajusta el factor a 1
            if(Math.max(this.height, this.width)>512)
                f=1;
        }
    }

    public boolean isPower2(int wd, int ht) {
        int i = 2;
        while (i < wd) {
            i <<= 1;
        }
        return i == wd && wd == ht;
    }

    /**
     * Ajusta la imagen a un caudrado usando tileMirror
     *
     * @return Radix2Processor
     *
     */
    public Radix2Processor adjustRadix2() {
        if (isPower2(this.width, this.height)) {
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
                && this.isPower2(width, height)) {
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
                && !this.isPower2(width, height)) {
            IJ.error("Hay un error en la gestion de las dimensiones de la imagen");
        }
        return this;
    }

    /**
     * @param bigWidth: nuevo ancho
     * @param bigHeight: nuevo alto
     * @param x: coordenada x de inicio de la imagen
     * @param y: coordenada y de inicio de la imagen
     * @return Radix2Processor : retorna la imagen con el tileMirror
    */
    public Radix2Processor tileMirror(int bigWidth, int bigHeight, int x, int y){
        ImageProcessor ipout = this.createProcessor(bigWidth, bigHeight);
        //Crea una copia del actual ip
        //ImageProcessor ip2 = this.crop();
        int w2 = this.getWidth();
        int h2 = this.getHeight();

        // how many times does ip2 fit into ipout?
        int hvLeft = x ;
        int hvRight = x+w2;
        int vvUp = y;
        
        
        //IJ.log("TileMirror New");
        int a, offset;
        //----------------------------------------------------------------------
        //The image changed
        float[] pixels = (float[]) ipout.getPixels();//new float[bigHeight*bigWidth];
        //Imagen original
        float[] pixelsR = (float[]) this.getPixels();
        //Copy the pixels in the middle of the enlarged image
        for (a = y = 0; y < h2; y++) {
            offset = (y + vvUp) * bigWidth + hvLeft;
            for (x = 0; x < w2; x++) {
                pixels[offset + x] = pixelsR[a++];
            }
        }

        // Si el ancho es impar, el lado izquierdo a reflejar es el mas grande
        // Si el alto es impar, el lado superior a reflejar es el mas grande
        // Esto se debe al redondeo utilizado para definir las coordenadas x,y
        // donde se inicia a copiar la imagen original.
        boolean widthOdd = width%2==1;//Ancho impar
        boolean heightOdd = height%2==1;//Alto impar

        //----------------------------------------------------------------------
        //Duplicate the pixels of the upper and lower parts of the image
        int yUpCopy, yUpNew, yDownCopy, yDownNew;
        yUpCopy=vvUp;
        yUpNew=yUpCopy-1;
        yDownCopy=yUpNew+height;
        yDownNew=yUpCopy+height;
        int i;
        for(i=0;i<vvUp;i++){
            if(heightOdd && i==vvUp-1){
                //Solo copia en la parte superior
                for(x=hvLeft;x<hvRight;x++)
                    pixels[yUpNew*bigWidth+x]=pixels[yUpCopy*bigWidth+x];
            }else{
                for(x=hvLeft;x<hvRight;x++){
                    pixels[yUpNew*bigWidth+x]=pixels[yUpCopy*bigWidth+x];
                    pixels[yDownNew*bigWidth+x]=pixels[yDownCopy*bigWidth+x];
                }
                yUpNew--;
                yUpCopy++;
                yDownNew++;
                yDownCopy--;
            }
            
        }
        int xLeftCopy, xLeftNew, xRightCopy,xRightNew;
        xLeftCopy=hvLeft;
        xLeftNew=hvLeft-1;
        xRightCopy=xLeftNew+width;
        xRightNew=xLeftCopy+width;
        
        for(i=0;i<hvLeft;i++){
            if(widthOdd && i ==hvLeft-1){
                for(y=0;y<bigHeight;y++)
                    pixels[y*bigWidth+xLeftNew]=pixels[y*bigWidth+xLeftCopy];
            }else{
                for(y=0;y<bigHeight;y++){
                    pixels[y*bigWidth+xLeftNew]=pixels[y*bigWidth+xLeftCopy];
                    pixels[y*bigWidth+xRightNew]=pixels[y*bigWidth+xRightCopy];
                }
                xLeftNew--;
                xLeftCopy++;
                xRightNew++;
                xRightCopy--;
                    
            }
        }
 
        Radix2Processor ipNew = new Radix2Processor(ipout);
        ipNew.originalHeight = h2;
        ipNew.originalWidth = w2;
        ipNew.cuadradaP2 = isPower2(w2, h2);
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
