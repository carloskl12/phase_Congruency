/**
 * Este archivo contiene la implementación de la Congruencia de Fase
 * @author Carlos Antonio Jacanamejoy Jamioy
 */
import PhaseCongruency.PCMonoRadix2;
import ij.IJ;
import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.GenericDialog;
import ij.plugin.filter.PlugInFilter;
import static ij.plugin.filter.PlugInFilter.DOES_ALL;
import static ij.plugin.filter.PlugInFilter.DONE;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;

/*
This implementation is inspired on Matlab code
 contibuted by Peter Kovesi 
 (https://peterkovesi.com/matlabfns/PhaseCongruency/phasecongmono.m)
*/
public class Phase_congruency implements PlugInFilter{
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
    double k=3.;
    double cutOff=0.5;
    int g=10;
    double deviationGain=1.5;
    int noiseMethod=-1;
    public double value=3;
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
        String [] outsV={"PC","F","H","E","Ph","Or"};
        boolean [] outsB={true,false,false,false,false,false};
        GenericDialog gd=new GenericDialog("Phase congruency");
        gd.addNumericField("Scales:", nScale, 0);
        gd.addToSameRow();
        gd.addNumericField("Lenght_edge:", minWaveLength, 0);
        gd.addNumericField("Mult:", mult, 2);
        gd.addToSameRow();
        gd.addNumericField("Sigma_Onf:", sigmaOnf, 2);
        gd.addNumericField("k:", k, 2);
        gd.addToSameRow();
        gd.addNumericField("Cut_Off:", cutOff, 2);
        gd.addNumericField("Gain:", g, 0);
        gd.addToSameRow();
        gd.addNumericField("Alpha:", deviationGain, 2);
        gd.addChoice("Threshold:", PCMonoRadix2.THM_METHODS, PCMonoRadix2.THM_METHODS[0]);
        gd.addToSameRow();
        gd.addNumericField("Value:", value, 0);
        
        gd.addMessage("Outputs");
        //Configura márgenes
        gd.setInsets(1, 16, 1);
        gd.addCheckboxGroup(1, 6, outsV, outsB);
        gd.addMessage("PC: Phase Congruency");
        
        gd.showDialog();
        if (gd.wasCanceled())
                return DONE;
        // Obtiene los valores
        nScale=(int) gd.getNextNumber();
        minWaveLength=(int) gd.getNextNumber();
        mult=gd.getNextNumber();
        sigmaOnf=gd.getNextNumber();
        k=gd.getNextNumber();
        cutOff=gd.getNextNumber();
        g=(int)gd.getNextNumber();
        deviationGain=gd.getNextNumber();
        noiseMethod=(int) gd.getNextChoiceIndex();
        value=gd.getNextNumber();
        outPC=gd.getNextBoolean();
        outF=gd.getNextBoolean();
        outH=gd.getNextBoolean();
        outE=gd.getNextBoolean();
        outPh=gd.getNextBoolean();
        outOr=gd.getNextBoolean();
        this.imp=imp;
        if (imp==null){
            IJ.showMessage("Error","Image is required");
            return DONE;
        }
        IJ.log("Factor: "+value);
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
        pc=new PCMonoRadix2(nScale,minWaveLength,mult,sigmaOnf,k,
        		cutOff,g,deviationGain,noiseMethod,value);
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
}
