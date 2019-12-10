/*
* PCMonoRadix2
* Created on 17 May 2019 by Carlos Antonio Jacanamejoy Jamioy
* e-mail: carloskl12@gmail.com
*
*
* This plugin is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 2
* as published by the Free Software Foundation.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this plugin; if not, write to the Free Software
* Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package PhaseCongruency;

import ij.IJ;
import ij.process.ImageProcessor;
import java.util.Arrays;

public class PCMonoRadix2 {
    public static final int THM_MEDIAN_RAYLEIGH=0;
    public static final int THM_MODE_RAYLEIGH=1;
    public static final int THM_CUSTOM_VALUE=2;
    public static final int THM_WEIBULL=3;
    public static final String [] THM_METHODS={"Median","Mode","Custom","Weibull"};
    int nScale = 4;
    int minWaveLength = 3;
    double mult = 2.1;
    double sigmaOnf = 0.55;
    double k = 3.;
    double cutOff = 0.5;
    int g = 10;
    double alphaGain = 1.5;
    int noiseThMethod = 0;//Noise threshold method
    double value=3;

    public boolean showNoiseTreshold=true;
    public double[][] PC;
    public double[][] Ph;//Fase
    public double[][] imgF;//Componente real
    public double[][] imgH;//Transformada de hilbert
    public double[][] Or;//Orientación
    public double[][] E;//Energía
    public double[][] W;//Dispersión de frecuencias
    Radix2Processor im;
    
    /*
    * La creación de la clase es con el fin de ajustar parámetros
    * del filtro
    */
    public PCMonoRadix2(int nScale,int minWaveLength,double mult,double sigmaOnf,double k,double cutOff,int g,
    		double deviationGain,int nthMethod , double value)
    {
        this.nScale=nScale;
        this.minWaveLength=minWaveLength;
        this.mult=mult;
        this.sigmaOnf=sigmaOnf;
        this.k=k;
        this.cutOff=cutOff;
        this.g=g;
        this.alphaGain=deviationGain;
        this.noiseThMethod=nthMethod;
        this.value=value;
    }

    public PCMonoRadix2(){
        //Deja los valores por defecto.
    }

    public void run(ImageProcessor ip)
    {
        double epsilon = 0.001;
        int i, j;
        im= new Radix2Processor(ip);
        int htOrig = im.getHeight();
        int wdOrig = im.getWidth();
        int ht=htOrig;
        int wd=wdOrig;

        if (!im.cuadradaP2)
        {
            im = im.adjustRadix2();
            ht = im.getHeight();
            wd = ht;
        }
        IJ.log("maxValue: "+ip.getMax());
        double tau=0;//**
        double[][] sumAn = new double[ht][wd];//Suma de amplitudes en diferentes componentes
        double[][] sumh1 = new double[ht][wd];//Suma componentes h1 (real) H(x) parte real
        double[][] sumh2 = new double[ht][wd];//Suma componentes h2 (imaginario) H(x) parte imaginaria
        double[][] sumf = new double[ht][wd];// Suma de las componentes de imagen en espacio F(x)
        double[][] maxAn = new double[ht][wd];//Máxima amplitud entre las diferentes componentes
        // Matrices para uso temporal
        float[][] fMatrix = new float[ht][wd];
        PerComp perIm = new PerComp(im);
        perIm.perComp();//Extrae la componente periódica

        FilterGrid fGrid = new FilterGrid(wd, ht);
        fGrid.grid();

        LowPassFilter lp = new LowPassFilter(wd, ht, .45, 15);
        lp.generate();
        //Se cambia el valor en el origen
        //para evitar las divisiones por cero
        fGrid.radius[0][0] = 1;
        lp.filter[0][0] = 1;
        
        //Para hacer la transformada de fourier se 
        //se indica que ya está en frecuencia
        FFTRadix2 fft= new FFTRadix2(perIm, true);
        for (int s = 0; s < nScale; s++)
        {
            double waveLength = minWaveLength * Math.pow(mult, s);
            double f0 = 1.0 / waveLength;
            IJ.showProgress(s, nScale);
            fft.real = new double[ht][wd];
            fft.imag = new double[ht][wd];
            //------------------------------------
            // PasaBanda espacial
            // No toma el dato cero porque es cero
            for (i = 0; i < ht; i++)
                for (j = 0; j < wd; j++)
                {
                    double tmp=Math.log(fGrid.radius[i][j] / f0);
                    double tmp2=Math.log(sigmaOnf);
                    tmp2*=tmp2;
                    tmp*=tmp;
                    double fc = Math.exp(-tmp / (2 * tmp2));
                    fc*=lp.filter[i][j];
                    fft.real[i][j] = perIm.real[i][j] * fc;
                    fft.imag[i][j] = perIm.imag[i][j] * fc;
                }
            fft.real[0][0]=0;
            fft.imag[0][0]=0;
            IJ.showProgress(s, nScale);
            fft.update();
            fft.fft2();

            // Guarda la imagen de pasabanda en espacio tomando los 
            // valores del resultado que se tiene en las componentes real
            // e imaginaria
            for (i = 0; i < ht; i++)
                for (j = 0; j < wd; j++)
                    fMatrix[i][j] = (float) ((fft.real[i][j] - fft.imag[i][j]) / wd);

            // Filtro monogenico
            for (i = 0; i < ht; i++)
                for (j = 0; j < wd; j++)
                {
                    double hReal = -fGrid.yR[i] / fGrid.radius[i][j];
                    double hImag = fGrid.xR[j] / fGrid.radius[i][j];
                    double tmp=Math.log(fGrid.radius[i][j] / f0);
                    tmp*=tmp;
                    double tmp2=Math.log(sigmaOnf);
                    tmp2*=tmp2;
                    double fc = Math.exp(-tmp / (2 * tmp2));
                    fc*=lp.filter[i][j];
                    double pReal = perIm.real[i][j] * hReal - perIm.imag[i][j] * hImag;
                    double pImag = perIm.real[i][j] * hImag + perIm.imag[i][j] * hReal;
                    pReal *= fc;
                    pImag *= fc;
                    fft.real[i][j] = pReal;
                    fft.imag[i][j] = pImag;
                }
            fft.real[0][0]=0;
            fft.imag[0][0]=0;
            fft.ifftComplex();

            // Acumula los resultados
            for (i = 0; i < ht; i++)
                for (j = 0; j < wd; j++)
                {
                    double f = fMatrix[i][j];
                    double h1 = fft.real[i][j];
                    double h2 = fft.imag[i][j];
                    double an = Math.sqrt(f * f + h1 * h1 + h2 * h2);
                    sumAn[i][j] += an;
                    sumf[i][j] += f;
                    sumh1[i][j] += h1;
                    sumh2[i][j] += h2;
                    if (s == 0)
                            maxAn[i][j] = an;
                    else if (maxAn[i][j] < an)
                            maxAn[i][j] = an;
                }

            // En la escala mas pequeña se estima el ruido
            // El perfil de ruido se saca sobre la imagen de energia
            // de la primera escala (pasa bajo)
            if (s == 0)
            {
                if (noiseThMethod == THM_MEDIAN_RAYLEIGH) //Mediana
                {
                    double[] tmp = new double[ht * wd];
                    for (i = 0; i < ht; i++)
                    {
                        int off = wd * i;
                        for (j = 0; j < wd; j++)
                            tmp[off + j] = sumAn[i][j];
                    }
                    Arrays.sort(tmp);// Ordena
                    int middle = tmp.length / 2;
                    IJ.log("middle: "+middle);
                    IJ.log("middle value: "+tmp[middle]);
                    double mediana = tmp.length % 2 == 1 ? tmp[middle] : (tmp[middle - 1] + tmp[middle]) / 2.0;
                    tau = mediana / Math.sqrt(Math.log(4));//Logaritmo natural
                    IJ.log("tau: "+tau);
                } else if (noiseThMethod == THM_MODE_RAYLEIGH)
                    tau = this.rayleighmode(sumAn,50);// Halla la moda.
            }
        }
        //Reajuste de dimensiones si la imagen original no era un cuadrado
        if (!im.cuadradaP2)
        {
            //Calcula el offset desde donde se tiene la imagen original
            int x = (int) Math.round((im.getWidth() - wdOrig) / 2.0);
            int y = (int) Math.round((im.getWidth() - htOrig) / 2.0);
            
            sumAn=getPixImgOriginal(sumAn, wdOrig, htOrig, x, y);
            sumh1=getPixImgOriginal(sumh1, wdOrig, htOrig, x, y);
            sumh2=getPixImgOriginal(sumh2, wdOrig, htOrig, x, y);
            sumf= getPixImgOriginal(sumf , wdOrig, htOrig, x, y);
            maxAn=getPixImgOriginal(maxAn, wdOrig, htOrig, x, y);
        }
        
        PC = new double[htOrig][wdOrig];
        imgF=sumf;//Componente real F
        imgH=new double[htOrig][wdOrig];//Transformada de hilbert H
        E= new double[htOrig][wdOrig];//Energía
        Ph = new double[htOrig][wdOrig]; //ángulo de fase
        Or=new double[htOrig][wdOrig]; //Orientación o dirección de gradiente
        W=new double[htOrig][wdOrig];//Ponderación de la congruencia de fase

        //Calcula la energía para estimar el umbral de ruido
        for (i = 0; i < htOrig; i++)
            for (j = 0; j < wdOrig; j++)
            {
                double magH=Math.sqrt( sumh2[i][j] * sumh2[i][j] + sumh1[i][j] * sumh1[i][j]);
                imgH[i][j]=magH;
                Ph[i][j] = Math.atan2(sumf[i][j], magH);
                //double energy = Math.sqrt(sumf[i][j] * sumf[i][j] + sumh2[i][j] * sumh2[i][j] + sumh1[i][j] * sumh1[i][j]);
                double energy= Math.sqrt(sumf[i][j] * sumf[i][j] +magH*magH);
                E[i][j]=energy;
            }
        /*
        *%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
        * Definición del umbral de ruido
        */
        double t=0;
        if (noiseThMethod == THM_CUSTOM_VALUE)
            t = value;
        else if(noiseThMethod==THM_MEDIAN_RAYLEIGH || noiseThMethod==THM_MODE_RAYLEIGH  )
        {
            IJ.log("taub value:"+tau);
            double totalTau = tau * (1 - Math.pow(1 / mult, nScale)) / (1 - (1 / mult));
            t = totalTau * (Math.sqrt(Math.PI / 2) + k * Math.sqrt((4 - Math.PI) / 2));
        }else if(noiseThMethod ==THM_WEIBULL)
        {
            /*
            * %%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
             Calcula la moda, y se multiplica por el factor
            */
            double[] tmp = new double[htOrig * wdOrig];
            for (i = 0; i < htOrig; i++)
            {
                int off = wdOrig * i;
                for (j = 0; j < wdOrig; j++)
                    tmp[off + j] = E[i][j];
            }
            double moda=rayleighmode(tmp,256);
            IJ.log("Factor p:"+value+ "  Moda:"+moda);
            t = moda*value;//Calcula el umbral
        }
        // t es el valor del radio del círculo de ruido
        
        IJ.log("Noise Threshold: " + t);
        // Cuantificación final de fc.
        for (i = 0; i < htOrig; i++)
            for (j = 0; j < wdOrig; j++)
            {
                // Medida de dispersión del espectro de frecuencias W(x)
                double width = (sumAn[i][j] / (maxAn[i][j] + epsilon) - 1) / (nScale - 1);

                // Ponderación de la PC   W(x)
                double weight = 1 / (1 + Math.exp((cutOff - width) * g));
                W[i][j]=weight;
                //Halla la orientación
                double or = Math.atan(-sumh2[i][j] / sumh1[i][j]);
                if(or<0)
                    or += Math.PI ;
                Or[i][j]=or;//Orientación

                 double energy=E[i][j];
                // 1 - alfa*delta(x)
                double tmpx = Math.acos(energy / (sumAn[i][j] + epsilon));
                double tmp1 = 1 - alphaGain * tmpx;
                
                if(tmp1 < 0)
                    tmp1 =0;
                double tmp2 = energy - t;
                if(tmp2<0)
                    tmp2 = 0;
                PC[i][j] = weight * tmp1 * tmp2 / (energy + epsilon);
            }

    }

    double rayleighmode(double[] data, int nbins) {
        double mx = 0;
        double edges;
        double[] n = new double[nbins + 1];
        for (int i = 0; i < data.length; i++) {
            if (mx < data[i]) {
                mx = data[i];
            }
        }
        double inc = mx / nbins;
        for (int i = 0; i < data.length; i++) {
            edges = 0;
            int j = -1;
            while (j < nbins && data[i] >= edges) {
                edges += inc;
                j++;
            }
            if (j >= 0) {
                n[j]++;
            }
        }
        //En nj está el histograma
        int ind = 0;
        mx = 0;
        for (int i = 0; i <= nbins; i++) {
            if (n[i] > mx) {
                mx = n[i];
                ind = i;
            }
        }
        IJ.log("Indice: " + ind);
        return inc * (2 * ind - 1) / 2;
    }

    double rayleighmode(double[][] data, int nbins) {
        int ht = data.length;
        int wd = data[0].length;
        double max = 0;
        double tmp[] = new double[ht * wd];
        for (int i = 0; i < ht; i++) {
            int off = wd * i;
            for (int j = 0; j < wd; j++) {
                tmp[off + j] = data[i][j];
                if (max < tmp[off + j]) {
                    max = tmp[off + j];
                }
            }
        }
        //-----------------------------------------
        double edges;
        double[] n = new double[nbins + 1];
        double inc = max / nbins;
        for (int i = 0; i < ht * wd; i++) {
            edges = 0;
            int j = -1;
            while (j < nbins && tmp[i] >= edges) {
                edges += inc;
                j++;
            }
            if (j >= 0) {
                n[j]++;
            }
        }
        int ind = 0;
        max = 0;
        for (int i = 0; i <= nbins; i++) {
            if (n[i] > max) {
                max = n[i];
                ind = i;
            }
        }
        return inc * (2 * ind - 1) / 2;
    }
    
    double [][] getPixImgOriginal(double[][] imCuadrada,int wdOrig, int htOrig,int x,int y){
        int i,j;
        double[][] result = new double[htOrig][wdOrig];
        for (i = 0; i < htOrig; i++)
            for (j = 0; j < wdOrig; j++)
                result[i][j] = imCuadrada[i+y][j+x];
        return result;
    }
}
