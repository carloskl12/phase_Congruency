# Phase Congruency
El presente repositorio contiene la implementación de 
la Congruencia de fase (Phase Congruency) en la plataforma ImageJ en 
su versión 1.52e. Esta versión se basa en parte de la propuesta realizada 
por Peter Kovesi en su [código](https://www.peterkovesi.com/matlabfns/PhaseCongruency/phasecongmono.m) disponible en su [página](https://www.peterkovesi.com/matlabfns/index.html), 
agregándole aportes tras el estudio de esta técnica, por lo cual para 
información adicional se puede revisar:

* Jacanamejoy, C. A., & Forero, M. G. (2018, November). A Note on the Phase
  Congruence Method in Image Analysis. In Iberoamerican Congress on Pattern
  Recognition (pp. 384-391). Springer, Cham.[link](https://link.springer.com/chapter/10.1007/978-3-030-13469-3_45)

* Jamioy, C. J., Meneses-Casas, N., & Forero, M. G. (2019, July). Image Feature Detection Based on Phase Congruency by Monogenic Filters with New Noise Estimation. In Iberian Conference on Pattern Recognition and Image Analysis (pp. 577-588). Springer, Cham. [link](https://link.springer.com/chapter/10.1007/978-3-030-31332-6_50)


## Instalación
Para instalar el plugin se puede revisar en la ayuda de [ImageJ](https://imagejdocu.tudor.lu/howto/plugins/how_to_install_a_plugin)

## Parámetros del plugin

![interfaz](https://github.com/carloskl12/phase_Congruency/blob/master/menusPC.png)

Consta de cuatro tipos de parámetros, primero los parámetros generales 
indicando entre paréntesis su valor por defecto:
 - **Scales** (4), hace referencia al 
  número de escalas utilizadas.

 - **Edge width** (3), corresponde al doble del ancho de los bordes mas pequeños a detectar. 
 Por definición corresponde a la longitud de la escala más pequeña a tener en cuenta.

 - **Scale factor** (2.1), factor entre las escalas sucesivas.

 - **Sigma Onf** ( 0.55), define la forma de los filtros log Gabor, afectando
  directamente el ancho de banda de los mismos.

El segundo tipo de parámetros está asociado a la función de cuantificación con 
la que se estima la congruencia de fase:

 - **Fucntion** (Abs), función seleccionada, puede ser valor absoluto (Abs) o una
  función exponencial.
 
 - **Alpha** , es un coeficiente que se utilizará en la función seleccionada.

El tercer tipo de parámetros permite ajustar la función de ponderación según la 
distribución de las escalas en el espectro de frecuencias:

 - **Cut Off** (0.5), punto de corte para penalizar la PC. Un valor de 0 implica no
  penalizar nada, y 1 corresponde a la máxima penalización debido a la
  distribución de frecuencias de la imágen.

 - **Gain** (10), ganancia que implica un aumento del contraste
  de la imagen resultante.

El cuarto tipo de parámetros permite ajustar el umbral de ruido en energía utilizado para 
evitar el ruido de fase:

 - **Method** (Median), método de estimación de ruido.

 - **Value** ( 3) , Si el método es custom, indica directamente el umbral de energía 
 a tener en cuenta. En los otros métodos corresponde aun parámetro utilizado como 
 factor.


Finalmente se indican las imágenes de salida a mostrar:

 - **Phase congruency**, imagen de congruencia de fase.
 
 - **Real component**, imagen sin la componente DC.
 
 - **Imaginary component**, imagen con desfase de 90 grados, o componente imaginaria.
 
 - **Enegry**, imagen de energía local.

 - **Phase**, imagen de fase.
 
 - **Orientation**, imagen de la orientación o magnitud del gradiente de la fase.


