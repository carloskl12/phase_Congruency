/**
* Phase_congruency.java
* Created on 12 December 2019, updated on 1 April 2021 by 
* - Carlos Antonio Jacanamejoy-Jamioy (e-mail:carloskl12@gmail.com) 
* - Guillermo Forero-Vargas (e-mail: mgforero@yahoo.es)
*
* This plug-in is inspired on Matlab code contibuted by Peter Kovesi 
* (https://peterkovesi.com/matlabfns/PhaseCongruency/phasecongmono.m)
*
* Function: finds the phase congruency of the input image by
* monogenic filters.
* 
* Input: any type of image or stack. The image is not modified.
* Output: One filtered float images or stacks
*
* Copyright (c) 2019 by 
* - Carlos Antonio Jacanamejoy-Jamioy (e-mail:carloskl12@gmail.com) 
* - Guillermo Forero-Vargas (e-mail: mgforero@yahoo.es)
*
* This plugin is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License version 3
* as published by the Free Software Foundation.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this plugin; if not, write to the Free Software
* Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
*/
import PhaseCongruency.PCMonoRadix2;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import static ij.plugin.filter.PlugInFilter.DOES_ALL;
import static ij.plugin.filter.PlugInFilter.DONE;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import java.awt.AWTEvent;
import java.awt.Choice;
import java.awt.TextField;
import java.util.Vector;

/*
This implementation 
*/
public class Phase_congruency implements PlugInFilter,DialogListener{
    String helpUrl ="https://github.com/carloskl12/phase_Congruency";
    protected ImagePlus imp;
    private int width;
    private int height;
    
    public String name;
    boolean stack=false;

    PCMonoRadix2 pc;
    String titulo="";
    String extension="";

    int nScale=4;
    int minWaveLength=3;
    double mult=2.1;
    double sigmaOnf=0.55;
    //double k=3.;
    double cutOff=0.5;
    int g=10;
    double deviationGain=1.5;
    int noiseMethod=0;
    public double value=3;
    
    int pcQuantification=0;
    
    boolean outPC=true;
    boolean outF=false;
    boolean outH=false;
    boolean outE=false;
    boolean outPh=false;
    boolean outOr=false;
    //Stacks de imágenes
    ImageStack stackPC;
    ImageStack stackF;
    ImageStack stackH;
    ImageStack stackE;
    ImageStack stackPh;
    ImageStack stackOr;

    int mode=0;
    double tau=0;
    double phi=3;
    
    
    @Override
    public int setup(String arg,ImagePlus imp) 
    {
        if (arg.equals("about")) 
        {
            showAbout();
            return DONE;
        }
        //Imagenes de salida que se pueden mostrar
        String [] outsV={"Phase Congruency","Enegry","Real Component","Phase",
            "Imaginary Component","Orientation"};
        boolean [] outsB={true,false,false,false,false,false};
        GenericDialog gd=new GenericDialog("Monogenic Phase Congruency");
        gd.addMessage("Global parameters");
        gd.addNumericField("Scales:", nScale, 0);
        gd.addToSameRow();
        gd.addNumericField("Edge_width*2:", minWaveLength, 0);
        gd.addNumericField("Scale_factor:", mult, 2);
        gd.addToSameRow();
        gd.addNumericField("Sigma_Onf:", sigmaOnf, 2);
        
        gd.addMessage("Phase Congruency Quantification");
        gd.addChoice("Function:",PCMonoRadix2.PCQ_FUNCTION, PCMonoRadix2.PCQ_FUNCTION[0]);
        gd.addToSameRow();
        gd.addNumericField("Alpha:", deviationGain, 2);
        
        gd.addMessage("Fequency distribution wheighting function ");
        gd.addNumericField("Cut_Off:", cutOff, 2);
        gd.addToSameRow();
        gd.addNumericField("Gain:", g, 0);
           

        gd.addMessage("Noise threshold estimator");
        gd.addChoice("Method:", PCMonoRadix2.THM_METHODS, PCMonoRadix2.THM_METHODS[0]);
        gd.addToSameRow();
        gd.addNumericField("Value:", value, 2);
        //gd.addToSameRow();
        //gd.addNumericField("k:", k, 2);
        
        gd.addMessage("Outputs");
        //Configura márgenes
        gd.setInsets(1, 16, 1);
        gd.addCheckboxGroup(4, 2, outsV, outsB);
        gd.addHelp(helpUrl);
        
        gd.addDialogListener(this);
        
        gd.showDialog();
        if (gd.wasCanceled())
                return DONE;
        // Obtiene los valores
        nScale=(int) gd.getNextNumber();
        minWaveLength=(int) gd.getNextNumber();
        mult=gd.getNextNumber();
        sigmaOnf=gd.getNextNumber();
        
        pcQuantification=(int) gd.getNextChoiceIndex();
        deviationGain=gd.getNextNumber();
        
        // Weight ponderation parameters
        cutOff=gd.getNextNumber();
        g=(int)gd.getNextNumber();    
        
        noiseMethod=(int) gd.getNextChoiceIndex();
        value=gd.getNextNumber();
        //k=gd.getNextNumber();
        
        outPC=gd.getNextBoolean();
        outE=gd.getNextBoolean();
        outF=gd.getNextBoolean();
        outPh=gd.getNextBoolean();
        outH=gd.getNextBoolean();
        outOr=gd.getNextBoolean();
        
        this.imp=imp;
        if (imp==null){
            IJ.showMessage("Error","Image is required");
            return DONE;
        }
        //IJ.log("Factor: "+value);
        width=imp.getWidth();
        height=imp.getHeight();
        titulo=imp.getTitle();
        //Verifica la extensión y la retira del titulo
        String[] parts = titulo.toLowerCase().split("[\\.]");
        if(parts.length>1)
        {
            extension="."+parts[parts.length-1];
            parts=titulo.split("[\\.]");
            titulo="";
            for(int i =0;i<parts.length-1;i++)
            {
                if(i>0)
                    titulo+=".";
                titulo+=parts[i];
            }
        }
        if(outPC)
            stackPC=new ImageStack(width,height);
        if(outF)
            stackF=new ImageStack(width,height);
        if(outH)
            stackH=new ImageStack(width,height);
        if(outE)
            stackE=new ImageStack(width,height);
        if(outPh)
            stackPh=new ImageStack(width,height);
        if(outOr)
            stackOr=new ImageStack(width,height);
        return DOES_ALL;
    }
    
    @Override
    public void run(ImageProcessor ip)
    {
        //pc=new PCMonoRadix2(nScale,minWaveLength,mult,sigmaOnf,k,
        //		cutOff,g,deviationGain,noiseMethod,value);
        pc=new PCMonoRadix2(nScale,minWaveLength,mult,sigmaOnf,
                pcQuantification, deviationGain,
                cutOff,g,
                noiseMethod,value);
        if(imp.getStackSize()>1)
            pc.showNoiseTreshold=false;
        for (int i=1;i<=imp.getStackSize();i++)
        {
            IJ.showStatus("Phase Congruency (Stack):"+i+"/"+imp.getStackSize());
            ImageProcessor ipn=imp.getStack().getProcessor(i).convertToFloatProcessor();
            //Ejecuta la congruencia de fase
            pc.run(ipn);
            if(outPC)
                addResult(stackPC,pc.PC);
            if(outF)
                addResult(stackF,pc.imgF);
            if(outH)
                addResult(stackH,pc.imgH);
            if(outE)
                addResult(stackE,pc.E);
            if(outPh)
                addResult(stackPh,pc.Ph);
            if(outOr)
                addResult(stackOr,pc.Or);

        }
        if(outPC)
        {
            ImagePlus imPC=new ImagePlus(titulo+"_PC"+extension,stackPC);
            imPC.show();
        }
        if(outF)
        {
            ImagePlus imPC=new ImagePlus(titulo+"_F"+extension,stackF);
            imPC.show();
        }
        if(outH)
        {
            ImagePlus imPC=new ImagePlus(titulo+"_H"+extension,stackH);
            imPC.show();
        }
        if(outE)
        {
            ImagePlus imPC=new ImagePlus(titulo+"_E"+extension,stackE);
            imPC.show();
        }
        if(outPh)
        {
            ImagePlus imPC=new ImagePlus(titulo+"_PH"+extension,stackPh);
            imPC.show();
        }
        if(outOr)
        {
            ImagePlus imPC=new ImagePlus(titulo+"_Or"+extension,stackOr);
            imPC.show();
        }

        IJ.showProgress(1.0);
    }
    /*
    * Función para agregar al stack una imagen dada en
    * un array bidimensional con valores double
    */
    void addResult(ImageStack imstk, double[][] values){
        int size=width*height;
        FloatProcessor ipR= new FloatProcessor (width,height);
        float [] dt=(float[]) ipR.getPixels();
        float max=-10;
        float min=255;
        for(int ii=0;ii<size;ii++)			
        {
            dt[ii]=(float) values[(int) Math.ceil(ii/width)][ii%width];
            if(dt[ii]<min)
                min=dt[ii];
            if(dt[ii]>max)
                max=dt[ii];
            
        }
        ipR.setMinAndMax(min, max);
        imstk.addSlice(ipR);
    }
    
    public void showAbout()
    {
        IJ.showMessage("Phase_Congruency",
        "Implementación de Phase_Congruency basada el la propuesta de Peter Kovesi");
    }

    @Override
    public boolean dialogItemChanged(GenericDialog gd, AWTEvent e) {
        Vector numeric = gd.getNumericFields();
        
        Vector choices = gd.getChoices();
        
        //nScale (0)
        //minWaveLength (1)
        // mult (2)
        // sigmaOnf (3)
        // Alpha (4)
        // cutOff (5)
        // g (6)
        // value (7)
        
        // pcQuantification (0)
        // noiseMethod  (1) 
        
        TextField tf;
        //String theText;
        // Verifica la función de cuantificación si ha cambiado
        Choice thisChoice = (Choice)(choices.elementAt(0));
	int index = thisChoice.getSelectedIndex();
        if(index != this.pcQuantification){
            this.pcQuantification=index;
            tf = (TextField)numeric.elementAt(4);
            //double alpha = gd.parseDouble(tf.getText());
            switch(this.pcQuantification){
                case PCMonoRadix2.PCQ_EXPONENTIAL:
                    this.deviationGain=4;
                    break;
                case PCMonoRadix2.PCQ_ABS:
                    this.deviationGain=1.5;
                    break;
            }
            tf.setText(""+this.deviationGain);
        }
        
        //Verifica el método de estimación de ruido
        thisChoice = (Choice)(choices.elementAt(1));
	index = thisChoice.getSelectedIndex();
        if(index != this.noiseMethod){
            this.noiseMethod=index;
            tf = (TextField)numeric.elementAt(7);
            //double alpha = gd.parseDouble(tf.getText());
            switch(this.noiseMethod){
                case PCMonoRadix2.THM_MEDIAN_RAYLEIGH:
                    this.value=3;
                    break;
                case PCMonoRadix2.THM_MODE_RAYLEIGH:
                    this.value=3;
                    break;
                case PCMonoRadix2.THM_CUSTOM_VALUE:
                    this.value=0.1;
                    break;
                case PCMonoRadix2.THM_WEIBULL:
                    this.value=3;
                    break;
            }
            tf.setText(""+this.value);
        }

        return true;
    }

}
